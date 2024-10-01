package org.example;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            return;
        }

        if (systemStateManager.needsTrip(checkpointTripped, untrip)) {
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
            bladeRunnerOptional.ifPresent(br -> br.sendExecuteMessage(SpeedEnum.SLOW));
        }
    }

    public static int calculatePreviousBlock(int checkpoint) {
        checkpoint = ((checkpoint + totalBlocks - 2) % totalBlocks) + 1;
        while (!isCheckpointValid(checkpoint)) {
            checkpoint = ((checkpoint + totalBlocks - 2) % totalBlocks) + 1;
        }
        return checkpoint;
    }

    private static boolean isCheckpointValid(int checkpoint) {
        if (checkpoint < 1 || checkpoint > MAX_BLOCKS) {
            return false;
        }

        String id = (checkpoint > 9) ? "CP" + checkpoint : "CP0" + checkpoint;
        if (db.getClient(id, CheckpointClient.class).isEmpty()) {
            return false;
        }

        return true;
    }
}
