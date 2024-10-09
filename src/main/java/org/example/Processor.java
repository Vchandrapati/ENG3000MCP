package org.example;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

// [x] need to check backwards

public class Processor {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final Database db = Database.getInstance();
    private static int totalBlocks;

    private Processor() {}

    public static void checkpointTripped(int checkpointTripped, boolean untrip) {
        SystemStateManager systemStateManager = SystemStateManager.getInstance();
        totalBlocks = db.getBlockCount();

        if (!isNextBlockValid(checkpointTripped)) {
            logger.log(Level.WARNING, "Inconsistent checkpoint trip : {0} on trip boolean : {1}",
                    new Object[] { checkpointTripped, untrip });
            return;
        }

        if (systemStateManager.needsTrip(checkpointTripped, untrip)) {
            logger.log(Level.WARNING, "Thomas to do, not sure what needs trip is");
            return;
        }

        // checks if the checkpoint before tripped checkpoint contains a blade runner
        int previousCheckpoint = calculateNextBlock(checkpointTripped, -1);
        if (!db.isBlockOccupied(previousCheckpoint)) {
            String id = (checkpointTripped > 9) ? "CP" + checkpointTripped : "CP0" + checkpointTripped;
            logger.log(Level.WARNING, "Inconsistent checkpoint trip : {0} on trip boolean : {1}", new Object[] {id, untrip});
            systemStateManager.addUnresponsiveClient(id, ReasonEnum.INCORTRIP);
        } else {
            handleTrip(checkpointTripped, previousCheckpoint, untrip);
        }
    }

    private static void handleTrip(int checkpointTripped, int previousCheckpoint, boolean untrip) {
        // get the blade runner of the block before the current tripped block
        Optional<BladeRunnerClient> bladeRunnerOptional = getBladeRunner(previousCheckpoint);

        if (bladeRunnerOptional.isEmpty()) {
            logger.log(Level.WARNING, "Tried to get blade runner at checkpoint {0} but failed", previousCheckpoint);
            return;
        }

        BladeRunnerClient bladeRunner = bladeRunnerOptional.get();

        // checks if tripped block is full, if so stop
        if (db.isBlockOccupied(checkpointTripped)) {
            bladeRunnerOptional.get().sendExecuteMessage(MessageEnums.CCPAction.STOPC);
            if (untrip) {
                String id = bladeRunnerOptional.get().getId();
                logger.log(Level.WARNING, "Multiple blade runners in the same zone, includes : {0}", id);
                SystemStateManager.getInstance().addUnresponsiveClient(id, ReasonEnum.COLLISION);
            }
            return;
        }

        // checks if next block is full, if so stop only if untrip
        int nextCheckpoint = calculateNextBlock(checkpointTripped, 1);
        if (db.isBlockOccupied(nextCheckpoint) && untrip) {
            bladeRunnerOptional.get().sendExecuteMessage(MessageEnums.CCPAction.STOPC);
        }

        if (isNextCheckpointStation(nextCheckpoint)) {
            bladeRunner.sendExecuteMessage(MessageEnums.CCPAction.FSLOWC);
        }

        // only change zone if untrip
        if (untrip) {
            db.updateBladeRunnerBlock(bladeRunner.getId(), checkpointTripped);
            bladeRunner.changeZone(checkpointTripped);

            if(isNextCheckpointStation(checkpointTripped) && !bladeRunner.stoppedAtStation()){
                
            }

            checkForTraffic(previousCheckpoint);
        }
    }

    private static boolean isNextCheckpointStation (int nextCheckpoint) {
        String id = nextCheckpoint > 9 ? "ST" + nextCheckpoint : "ST0" + nextCheckpoint;
        return db.getClient(id, StationClient.class).isPresent();
    }

    private static Optional<BladeRunnerClient> getBladeRunner(int checkpoint) {
        String bladeRunnerID = db.getLastBladeRunnerInBlock(checkpoint);

        if (bladeRunnerID == null) {
            logger.log(Level.WARNING, "Tried to get blade runner at checkpoint {0} but failed", checkpoint);
            return Optional.empty();
        }

        return db.getClient(bladeRunnerID, BladeRunnerClient.class);
    }

    // frees ONE blade runner behind the current, he will subsequently free the rest
    // by moving
    private static void checkForTraffic(int checkpoint) {
        // check block behind
        int blockBefore = calculateNextBlock(checkpoint, -1);
        if (db.isBlockOccupied(blockBefore)) {
            Optional<BladeRunnerClient> bladeRunnerOptional = getBladeRunner(blockBefore);
            bladeRunnerOptional.ifPresent(br -> br.sendExecuteMessage(MessageEnums.CCPAction.FFASTC));
        }
    }

    public static int calculateNextBlock(int checkpoint, int direction) {
        totalBlocks = db.getBlockCount();

        while(true) {
            checkpoint += direction;
            if (checkpoint > totalBlocks)
                checkpoint = 1;

            if (checkpoint < 1)
                checkpoint = totalBlocks;

            if(isNextBlockValid(checkpoint)) {
                return checkpoint;
            }
        }
    }

    private static boolean isNextBlockValid (int checkpoint) {
        String cpId = checkpoint == totalBlocks ? "CP" + checkpoint : "CP0" + checkpoint;
        String stId = checkpoint == totalBlocks ? "ST" + checkpoint : "ST0" + checkpoint;
        return db.getClient(cpId, CheckpointClient.class).isPresent() || db.getClient(stId, StationClient.class).isPresent();
    }

    private static void trainAligned() {
        // Nothing needed at the moment
    }


    public static void bladeRunnerStopped(BladeRunnerClient BladeRunner){
        //open doors
        //time for 5 seconds or whatever
        BladeRunner.stoppedAtStation();
    }
    

    public static void trainUnaligned(int stationCheckpoint) {
        Optional<BladeRunnerClient> br = getBladeRunner(stationCheckpoint - 1);
        Optional<BladeRunnerClient> brOverShot = getBladeRunner(stationCheckpoint);

        // stop bladeRunner behind the station checkpoint incase there is one
        Optional<BladeRunnerClient> bladeRunnerBehind = getBladeRunner(stationCheckpoint - 2);

        if (bladeRunnerBehind.isPresent()) {
            bladeRunnerBehind.get().sendExecuteMessage(MessageEnums.CCPAction.STOPC);
            bladeRunnerBehind.get().updateStatus(MessageEnums.CCPStatus.STOPC);
        }

        if (br.isEmpty() && brOverShot.isEmpty()) {
            logger.log(Level.WARNING, "Tried to get blade runner at checkpoint {0} but failed",
                    stationCheckpoint);
            return;
        }

        if (br.isPresent()) {
            // train has undershot, not our responsibility to align i dont think
        }

        if (brOverShot.isPresent()) { // train has overshot
            brOverShot.get().sendExecuteMessage(MessageEnums.CCPAction.RSLOWC);
            brOverShot.get().updateStatus(MessageEnums.CCPStatus.RSLOWC);
        }
    }
}

