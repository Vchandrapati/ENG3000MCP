package org.example;

import org.example.client.BladeRunnerClient;
import org.example.client.CheckpointClient;
import org.example.client.MessageGenerator;
import org.example.client.StationClient;
import org.example.messages.MessageEnums;
import org.example.messages.MessageSender;
import org.example.client.ReasonEnum;
import org.example.state.SystemStateManager;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// [x] need to check backwards

public class Processor {

    public Processor(Database db, SystemStateManager systemStateManager) {
        this.db = db;
        this.systemStateManager = systemStateManager;
    }

    private static SystemStateManager systemStateManager;
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static Database db = Database.getInstance();
    private static int totalBlocks;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void checkpointTripped(int checkpointTripped, boolean untrip) {
        SystemStateManager systemStateManager = SystemStateManager.getInstance();
        totalBlocks = db.getBlockCount();

        if (!isNextBlockValid(checkpointTripped)) {
            logger.log(Level.WARNING, "Inconsistent checkpoint trip : {0} on trip boolean : {1}",
                    new Object[] {checkpointTripped, untrip});
            return;
        }

        if (systemStateManager.needsTrip(checkpointTripped, untrip)) {
            logger.log(Level.WARNING, "Sent to mapping state");
            return;
        }

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
                        new Object[] {id, untrip});
                systemStateManager.addUnresponsiveClient(id, ReasonEnum.INCORTRIP);
            } else {
                handleTrip(checkpointTripped, previousCheckpoint, untrip);
                logger.log(Level.FINEST, "forward reached");
            }
        } else {
            reverseTrip(reversingBladeRunner.get(), checkpointTripped, untrip);
            logger.log(Level.FINEST, "Reversing reached");
        }
    }

    public static void handleTrip(int checkpointTripped, int previousCheckpoint, boolean untrip) {
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
                SystemStateManager.getInstance().addUnresponsiveClient(id, ReasonEnum.COLLISION);
            }
            return;
        }

        // checks if next block is full, if so stop only if untrip
        int nextCheckpoint = calculateNextBlock(checkpointTripped, 1);

        if (isCheckpointStation(nextCheckpoint)) {
            bladeRunner.sendExecuteMessage(MessageEnums.CCPAction.FSLOWC);
            notifyStation(bladeRunner, db.getStationIfExist(nextCheckpoint).get());
            // give station blade runner
        }

        // if (isCheckpointStation(checkpointTripped)) {
        // bladeRunner.sendExecuteMessage(MessageEnums.CCPAction.FSLOWC);
        // //give station blade runner
        // }


        if (db.isBlockOccupied(nextCheckpoint) && untrip) {
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

        // for Test
        logger.log(Level.FINEST, "succesfully exited handleTrip");
    }

    public static void reverseTrip(BladeRunnerClient reversingBladeRunner, int checkpointTripped,
            boolean untrip) {
        // checks if train is reversing "legally"
        if (!isCheckpointStation(checkpointTripped)
                && reversingBladeRunner.getStatus() == MessageEnums.CCPStatus.RSLOWC) {
            // train was reversing randomly
            reversingBladeRunner.sendExecuteMessage(MessageEnums.CCPAction.FFASTC);
            reversingBladeRunner.updateStatus(MessageEnums.CCPStatus.FFASTC);
            return;
        }

        int previousBlock = calculateNextBlock(checkpointTripped, -1);

        if (!untrip && db.isBlockOccupied(previousBlock)) {
            // bladeRunner is reversing but the previous block has a bladeRunner in it. Must stop
            reversingBladeRunner.sendExecuteMessage(MessageEnums.CCPAction.STOPC);
            reversingBladeRunner.updateStatus(MessageEnums.CCPStatus.STOPC);
        }


        if (untrip) {
            reversingBladeRunner.sendExecuteMessage(MessageEnums.CCPAction.FSLOWC);
            reversingBladeRunner.updateStatus(MessageEnums.CCPStatus.FSLOWC);

            db.updateBladeRunnerBlock(reversingBladeRunner.getId(), previousBlock);
            reversingBladeRunner.changeZone(previousBlock);
        }
    }

    public static boolean isCheckpointStation(int checkpoint) {
            if(db.getStationIfExist(checkpoint).isEmpty()){
                return false;
            }
            return true;
    }

    private static boolean isSmartStation(StationClient sc){
        if(sc.getId().contains("A")){
            return true;
        }
        return false;
    }



    public static Optional<BladeRunnerClient> getBladeRunner(int checkpoint) {
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
    public static void checkForTraffic(int checkpoint) {
        // check block behind
        int blockBefore = calculateNextBlock(checkpoint, -1);
        if (db.isBlockOccupied(blockBefore)) {
            Optional<BladeRunnerClient> bladeRunnerOptional = getBladeRunner(blockBefore);
            bladeRunnerOptional
                    .ifPresent(br -> br.sendExecuteMessage(MessageEnums.CCPAction.FFASTC));
        }
    }

    public static int calculateNextBlock(int checkpoint, int direction) {
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

    public static boolean isNextBlockValid(int checkpoint) {
        String cpId = checkpoint > 9 ? "CP" + checkpoint : "CP0" + checkpoint;
        String stId = checkpoint > 9 ? "ST" + checkpoint : "ST0" + checkpoint;
        return db.getClient(cpId, CheckpointClient.class).isPresent()
                || db.getClient(stId, StationClient.class).isPresent();
    }

    private static void trainAligned() {
        // Nothing needed at the moment
    }

    public static void bladeRunnerStopped(String bladeRunnerID) {
        Optional<BladeRunnerClient> bladeRunnerOp =
                db.getClient(bladeRunnerID, BladeRunnerClient.class);
        if (bladeRunnerOp.isPresent()) {
            BladeRunnerClient bladeRunner = bladeRunnerOp.get();
            bladeRunner.sendExecuteMessage(MessageEnums.CCPAction.STOPO);
            bladeRunner.updateStatus(MessageEnums.CCPStatus.STOPO);

            int stationCheckpoint = calculateNextBlock(bladeRunner.getZone(), 1);
            Optional<StationClient> sc = db.getStationIfExist(stationCheckpoint);
            logger.log(Level.FINEST, "ST0{0}", stationCheckpoint);
            if (sc.isPresent() && isSmartStation(sc.get())) {
                StationClient station = sc.get();
                station.sendExecuteMessage(MessageEnums.STCAction.OPEN);
                station.updateStatus(MessageEnums.STCStatus.ONOPEN);
                scheduler.schedule(() -> stationBuffer(bladeRunner, station), 5, TimeUnit.SECONDS);
            }


            // time for 5 seconds or whatever
            bladeRunner.setDockedAtStation(true);
            // set speed to forward/ back to forward
        }
    }

    public static void bladeRunnerOverShot(BladeRunnerClient bladeRunner, int bladeRunnerZone) {
        Optional<BladeRunnerClient> br = getBladeRunner(calculateNextBlock(bladeRunnerZone, -2));

        if (br.isPresent()) {
            br.get().sendExecuteMessage(MessageEnums.CCPAction.STOPC);
            br.get().updateStatus(MessageEnums.CCPStatus.STOPC);
        }

        bladeRunner.sendExecuteMessage(MessageEnums.CCPAction.RSLOWC);
        bladeRunner.updateStatus(MessageEnums.CCPStatus.RSLOWC);
    }


    private static void stationBuffer(BladeRunnerClient br, StationClient station) {
        br.sendExecuteMessage(MessageEnums.CCPAction.FFASTC);
        br.updateStatus(MessageEnums.CCPStatus.FFASTC);
        station.sendExecuteMessage(MessageEnums.STCAction.CLOSE);
        station.updateStatus(MessageEnums.STCStatus.OFF);
    }

    private static void notifyStation(BladeRunnerClient br, StationClient sc){
        String id = br.getId();
        
        //TODO
        sc.sendMessage("Blade Runner coming", id);

    }


    public static void lastEXECResend(BladeRunnerClient br){
         MessageEnums.CCPAction action = br.getLastActionSent();
        br.sendExecuteMessage(action);
    } 
}
