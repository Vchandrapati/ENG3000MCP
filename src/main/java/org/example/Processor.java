package org.example;

import org.example.client.BladeRunnerClient;
import org.example.client.CheckpointClient;
import org.example.client.StationClient;
import org.example.events.*;
import org.example.messages.MessageEnums;
import org.example.client.ReasonEnum;
import org.example.state.SystemState;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Processor {
    private final EventBus eventBus;
    private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final Database db;
    private int totalBlocks;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private SystemState currentState;
    private boolean mappingStateTriggered = false;

    public Processor(EventBus eventBus, Database db) {
        this.eventBus = eventBus;
        this.db = db;
        eventBus.subscribe(StateChangeEvent.class, this::updateState);
        eventBus.subscribe(TripEvent.class, this::checkpointTripped);
        eventBus.subscribe(BladeRunnerStopEvent.class, this::bladeRunnerStopped);
    }

    private void updateState(StateChangeEvent event) {
        currentState = event.getState();

        if (currentState == SystemState.RUNNING)
            scheduler.schedule(this::startBladeRunners, 2, TimeUnit.SECONDS);
    }

    private void checkpointTripped(TripEvent event) {
        if (currentState == SystemState.WAITING || currentState == SystemState.MAPPING) {
            logger.log(Level.INFO, "Sent to mapping state");
            mappingStateTriggered = true;
            return;
        }

        int checkpointTripped = event.getLocation();
        boolean untrip = event.isUntrip();
        totalBlocks = db.getBlockCount();
        if (!isNextBlockValid(checkpointTripped)) {
            logger.log(Level.WARNING, "Inconsistent checkpoint trip : {0} on trip boolean : {1}",
                    new Object[] { checkpointTripped, untrip });
            eventBus.publish(new ClientErrorEvent("CP0" + checkpointTripped, ReasonEnum.INCORTRIP));
            return;
        }

        mappingStateTriggered = false;

        Optional<BladeRunnerClient> reversingBladeRunner = getBladeRunner(checkpointTripped);
        boolean reversing = reversingBladeRunner.isPresent()
                && reversingBladeRunner.get().getStatus() == MessageEnums.CCPStatus.RSLOWC;

        // checks if the checkpoint before tripped checkpoint contains a blade runner
        if (!reversing) {
            int previousCheckpoint = calculateNextBlock(checkpointTripped, -1);
            if (!db.isBlockOccupied(previousCheckpoint)) {
                String id = (checkpointTripped > 9) ? "CP" + checkpointTripped
                        : "CP0" + checkpointTripped;
                logger.log(Level.WARNING,
                        "Inconsistent checkpoint trip : {0} on trip boolean : {1}",
                        new Object[] { id, untrip });
                eventBus.publish(new ClientErrorEvent(id, ReasonEnum.INCORTRIP));
            } else {
                logger.log(Level.FINEST, "forward reached");
                handleTrip(checkpointTripped, previousCheckpoint, untrip);
            }
        } else {
            logger.log(Level.FINEST, "Reversing reached");
            reverseTrip(reversingBladeRunner.get(), checkpointTripped, untrip);
        }
    }

    private void startBladeRunners() {
        List<BladeRunnerClient> bladeRunners = db.getBladeRunnerClients();

        bladeRunners.sort((br1, br2) -> Integer.compare(br2.getZone(), br1.getZone()));

        for (BladeRunnerClient br : bladeRunners) {
            int nextCheckpoint = calculateNextBlock(br.getZone(), 1);

            if(!db.isBlockOccupied(nextCheckpoint)) {
                br.sendExecuteMessage(MessageEnums.CCPAction.FFASTC);
            } else {
                br.sendExecuteMessage(MessageEnums.CCPAction.STOPC);
            }

        }
    }

    public void handleTrip(int checkpointTripped, int previousCheckpoint, boolean untrip) {
        // get the blade runner of the block before the current tripped checkpoint
        Optional<BladeRunnerClient> bladeRunnerOptional = getBladeRunner(previousCheckpoint);

        if (bladeRunnerOptional.isEmpty()) {
            logger.log(Level.WARNING, "Tried to get blade runner at checkpoint {0} but failed",
                    previousCheckpoint);
            return;
        }

        BladeRunnerClient bladeRunner = bladeRunnerOptional.get();

        // checks if tripped block is full, if so stop
        if (db.isBlockOccupied(checkpointTripped)) {
            bladeRunnerOptional.get().sendExecuteMessage(MessageEnums.CCPAction.STOPC);
            if (untrip) {
                String id = bladeRunnerOptional.get().getId();
                logger.log(Level.WARNING, "Multiple blade runners in the same zone, includes : {0}",
                        id);
                eventBus.publish(new ClientErrorEvent(id, ReasonEnum.COLLISION));
            }
            return;
        }

        // checks if next block is full, if so stop only if untrip
        int nextCheckpoint = calculateNextBlock(checkpointTripped, 1);
        if (isCheckpointStation(nextCheckpoint)) {
            if (untrip)
                bladeRunner.sendExecuteMessage(MessageEnums.CCPAction.FSLOWC);

            notifyStation(bladeRunner, db.getStationIfExist(nextCheckpoint).get());
            // give station blade runner
        }

        if (db.isBlockOccupied(nextCheckpoint) && untrip && bladeRunner.getLastActionSent() != MessageEnums.CCPAction.FSLOWC && currentState != SystemState.MAPPING) {
            bladeRunnerOptional.get().sendExecuteMessage(MessageEnums.CCPAction.STOPC);
        }

        // only change zone if untrip
        if (untrip) {
            db.updateBladeRunnerBlock(bladeRunner.getId(), checkpointTripped);
            bladeRunner.changeZone(checkpointTripped);

            if (isCheckpointStation(checkpointTripped) && !bladeRunner.isDockedAtStation()) { // overshot
                                                                                              // Station
                bladeRunnerOverShot(bladeRunner, checkpointTripped);
            } else {
                bladeRunner.setDockedAtStation(false);
                checkForTraffic(previousCheckpoint);
            }
        }
    }

    private void reverseTrip(BladeRunnerClient reversingBladeRunner, int checkpointTripped,
            boolean untrip) {
        // checks if train is reversing "legally"
        if (!isCheckpointStation(checkpointTripped)
                && reversingBladeRunner.getStatus() == MessageEnums.CCPStatus.RSLOWC) {
            // train was reversing randomly
            logger.log(Level.WARNING, "blade Runner reversing when it shouldn't");
            reversingBladeRunner.sendExecuteMessage(MessageEnums.CCPAction.FFASTC);
            reversingBladeRunner.updateStatus(MessageEnums.CCPStatus.FFASTC);
            return;
        }

        int previousBlock = calculateNextBlock(checkpointTripped, -1);

        if (!untrip && db.isBlockOccupied(previousBlock)) {
            // bladeRunner is reversing but the previous block has a bladeRunner in it. Must
            // stop
            reversingBladeRunner.sendExecuteMessage(MessageEnums.CCPAction.STOPC);
            reversingBladeRunner.updateStatus(MessageEnums.CCPStatus.STOPC);
        }

        if (untrip) {
            reversingBladeRunner.sendExecuteMessage(MessageEnums.CCPAction.FSLOWC);
            reversingBladeRunner.updateStatus(MessageEnums.CCPStatus.FSLOWC);
            logger.log(Level.INFO, "blade Runner reversing but previous block occupied");

            db.updateBladeRunnerBlock(reversingBladeRunner.getId(), previousBlock);
            reversingBladeRunner.changeZone(previousBlock);
        }
    }

    private boolean isCheckpointStation(int checkpoint) {
        return db.getStationIfExist(checkpoint).isPresent();
    }

    private boolean isSmartStation(StationClient sc) {
        if (sc == null) {
            return false;
        }
        return sc.getId().contains("A");
    }

    private Optional<BladeRunnerClient> getBladeRunner(int checkpoint) {
        String bladeRunnerID = db.getLastBladeRunnerInBlock(checkpoint);

        if (bladeRunnerID == null) {
            logger.log(Level.WARNING, "Tried to get blade runner at checkpoint {0} but failed",
                    checkpoint);
            return Optional.empty();
        }

        return db.getClient(bladeRunnerID, BladeRunnerClient.class);
    }

    // frees ONE blade runner behind the current, he will subsequently free the rest
    // by moving
    private void checkForTraffic(int checkpoint) {
        // check block behind
        int blockBefore = calculateNextBlock(checkpoint, -1);
        if (db.isBlockOccupied(blockBefore)) {
            Optional<BladeRunnerClient> bladeRunnerOptional = getBladeRunner(blockBefore);
            if (bladeRunnerOptional.isPresent() && bladeRunnerOptional.get().getLastActionSent() != MessageEnums.CCPAction.RSLOWC)
                bladeRunnerOptional
                        .ifPresent(br -> br.sendExecuteMessage(MessageEnums.CCPAction.FFASTC));
        }
    }

    private int calculateNextBlock(int checkpoint, int direction) {
        totalBlocks = db.getBlockCount();

        while (true) {
            checkpoint += direction;
            if (checkpoint > totalBlocks)
                checkpoint = 1;

            if (checkpoint < 1)
                checkpoint = totalBlocks;

            if (isNextBlockValid(checkpoint)) {
                return checkpoint;
            }
        }
    }

    private boolean isNextBlockValid(int checkpoint) {
        String cpId = checkpoint > 9 ? "CP" + checkpoint : "CP0" + checkpoint;
        String stId = checkpoint > 9 ? "ST" + checkpoint : "ST0" + checkpoint;
        String smartstId = checkpoint > 9 ? "STA" + checkpoint : "STA" + checkpoint;
        return db.getClient(cpId, CheckpointClient.class).isPresent()
                || db.getClient(stId, StationClient.class).isPresent()
                || db.getClient(smartstId, StationClient.class).isPresent();
    }

    private void trainAligned() {
        // Nothing needed at the moment
    }

    private void bladeRunnerStopped(BladeRunnerStopEvent event) {
        Optional<BladeRunnerClient> bladeRunnerOp = db.getClient(event.getId(), BladeRunnerClient.class);
        if (bladeRunnerOp.isPresent()) {
            BladeRunnerClient bladeRunner = bladeRunnerOp.get();
            bladeRunner.sendExecuteMessage(MessageEnums.CCPAction.STOPO);
            bladeRunner.updateStatus(MessageEnums.CCPStatus.STOPO);

            int stationCheckpoint = calculateNextBlock(bladeRunner.getZone(), 1);
            Optional<StationClient> sc = db.getStationIfExist(stationCheckpoint);
            logger.log(Level.FINEST, "ST0{0}", stationCheckpoint);

            if (sc.isPresent()) {
                scheduler.schedule(() -> stationBuffer(bladeRunner, sc.get()), 5, TimeUnit.SECONDS);
                if (isSmartStation(sc.get())) {
                    StationClient station = sc.get();
                    station.sendExecuteMessage(MessageEnums.STCAction.OPEN);
                    station.updateStatus(MessageEnums.STCStatus.ONOPEN);
                }
            }

            // time for 5 seconds or whatever
            bladeRunner.setDockedAtStation(true);
            // set speed to forward/ back to forward
        }
    }

    private void bladeRunnerOverShot(BladeRunnerClient bladeRunner, int bladeRunnerZone) {
        Optional<BladeRunnerClient> br = getBladeRunner(calculateNextBlock(bladeRunnerZone, -2));

        if (br.isPresent()) {
            br.get().sendExecuteMessage(MessageEnums.CCPAction.STOPC);
            br.get().updateStatus(MessageEnums.CCPStatus.STOPC);
        }

        bladeRunner.sendExecuteMessage(MessageEnums.CCPAction.RSLOWC);
        bladeRunner.updateStatus(MessageEnums.CCPStatus.RSLOWC);
    }

    private void stationBuffer(BladeRunnerClient br, StationClient station) {
        br.sendExecuteMessage(MessageEnums.CCPAction.STOPC);
        br.updateStatus(MessageEnums.CCPStatus.STOPC);
        br.sendExecuteMessage(MessageEnums.CCPAction.FFASTC);
        br.updateStatus(MessageEnums.CCPStatus.FFASTC);

        if (isSmartStation(station)) {
            station.sendExecuteMessage(MessageEnums.STCAction.CLOSE);
            station.updateStatus(MessageEnums.STCStatus.OFF);
        }
    }

    private void notifyStation(BladeRunnerClient br, StationClient sc) {
        String id = br.getId();
    }
}
