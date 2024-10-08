package org.example;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

// [x] need to check backwards

public class Processor {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final Database db = Database.getInstance();
    private static int totalBlocks;
    private static final int MAX_BLOCKS = 10;

    private Processor() {}

    public static void checkpointTripped(int checkpointTripped, boolean untrip) {
        SystemStateManager systemStateManager = SystemStateManager.getInstance();
        totalBlocks = db.getCheckpointCount();

        if (!isCheckpointValid(checkpointTripped)) {
            logger.log(Level.WARNING, "Inconsistent checkpoint trip : {0} on trip boolean : {1}",
                    new Object[] {checkpointTripped, untrip});
            return;
        }

        if (systemStateManager.needsTrip(checkpointTripped, untrip)) {
            logger.log(Level.WARNING, "Thomas to do, not sure what needstrip is");
            return;
        }

        // checks if the checkpoint before tripped checkpoint contains a blade runner
        int previousCheckpoint = calculatePreviousBlock(checkpointTripped);
        if (!db.isBlockOccupied(previousCheckpoint)) {
            String id =
                    (checkpointTripped > 9) ? "CP" + checkpointTripped : "CP0" + checkpointTripped;
            logger.log(Level.WARNING, "Inconsistent checkpoint trip : {0} on trip boolean : {1}",
                    new Object[] {id, untrip});
            systemStateManager.addUnresponsiveClient(id, ReasonEnum.INCORTRIP);
        } else {
            handleTrip(checkpointTripped, previousCheckpoint, untrip);
        }
    }

    private static void handleTrip(int checkpointTripped, int previousCheckpoint, boolean untrip) {
        // get the blade runner of the block before the current tripped block
        Optional<BladeRunnerClient> bladeRunnerOptional = getBladeRunner(previousCheckpoint);

        if (bladeRunnerOptional.isEmpty()) {
            logger.log(Level.WARNING, "Tried to get blade runner at checkpoint {0} but failed",
                    previousCheckpoint);
            return;
        }

        BladeRunnerClient bladeRunner = bladeRunnerOptional.get();

        // checks if tripped block is full, if so stop
        if (db.isBlockOccupied(checkpointTripped)) {
            bladeRunnerOptional.get().sendExecuteMessage(SpeedEnum.STOP);
            if (untrip) {
                String id = bladeRunnerOptional.get().getId();
                logger.log(Level.WARNING, "Multiple blade runners in the same zone, includes : {0}",
                        id);
                SystemStateManager.getInstance().addUnresponsiveClient(id, ReasonEnum.COLLISION);
            }
            return;
        }

        // checks if next block is full, if so stop only if untrip
        if (db.isBlockOccupied(calculateNextBlock(checkpointTripped)) && untrip) {
            bladeRunnerOptional.get().sendExecuteMessage(SpeedEnum.STOP);
        }

        // only change zone if untrip
        if (untrip) {
            db.updateBladeRunnerBlock(bladeRunner.getId(), checkpointTripped);
            bladeRunner.changeZone(checkpointTripped);
            checkForTraffic(previousCheckpoint);
        }
    }

    private static Optional<BladeRunnerClient> getBladeRunner(int checkpoint) {
        String bladeRunnerID = db.getLastBladeRunnerInBlock(checkpoint);

        if (bladeRunnerID == null) {
            logger.log(Level.WARNING, "Tried to get blade runner at checkpoint {0} but failed",
                    checkpoint);
            return Optional.empty();
        }

        return db.getClient(bladeRunnerID, BladeRunnerClient.class);
    }

    // frees ONE blade runner behind the current, he will subsequently free the rest by moving
    private static void checkForTraffic(int checkpoint) {
        // check block behind
        int blockBefore = calculatePreviousBlock(checkpoint);
        if (db.isBlockOccupied(blockBefore)) {
            Optional<BladeRunnerClient> bladeRunnerOptional = getBladeRunner(blockBefore);
            bladeRunnerOptional.ifPresent(br -> br.sendExecuteMessage(SpeedEnum.FAST));
            bladeRunnerOptional.ifPresent(br -> br.updateStatus("STARTED"));
        }
    }

    public static int calculatePreviousBlock(int checkpoint) {
        totalBlocks = db.getCheckpointCount();

        while(true) {
            checkpoint--;
            if (checkpoint == 0) {
                checkpoint = MAX_BLOCKS;
            }
            
            if(isCheckpointValid(checkpoint)) {
                return checkpoint;
            }
        }
    }

    public static int calculateNextBlock(int checkpoint) {
        totalBlocks = db.getCheckpointCount();

        while(true) {
            checkpoint++;
            if (checkpoint == MAX_BLOCKS) {
                checkpoint = 1;
            }
            
            if(isCheckpointValid(checkpoint)) {
                return checkpoint;
            }
        }
    }

    private static boolean isCheckpointValid(int checkpoint) {
        if (checkpoint < 1 || checkpoint > MAX_BLOCKS) {
            return false;
        }

        String id = (checkpoint == MAX_BLOCKS) ? "CP" + checkpoint : "CP0" + checkpoint;
        if (db.getClient(id, CheckpointClient.class).isEmpty()) {
            return false;
        }

        return true;
    }

    private static void trainAligned(){
        //Nothing needed at the moment
    }

    private static void trainUnaligned(int stationCheckpoint){
        Optional<BladeRunnerClient> br = getBladeRunner(stationCheckpoint-1);
        Optional<BladeRunnerClient> brOverShot = getBladeRunner(stationCheckpoint);
        

        if(br.isEmpty() && brOverShot.isEmpty()){
            logger.log(Level.WARNING, "Tried to get blade runner at checkpoint {0} but failed",
            stationCheckpoint);
            return;
        }

        if(br.isPresent()){
            //train has undershot, not our responsibility to align i dont think
        }

        if(brOverShot.isPresent()){ //train has overshot
            brOverShot.get().sendExecuteMessage(SpeedEnum.BACKWARDS);
            brOverShot.get().updateStatus("REVERSING");
        }
    }

}
