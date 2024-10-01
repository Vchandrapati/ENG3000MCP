package org.example;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Processor {

    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final Database db = Database.getInstance();
    private static final int HIGHEST_CHECKPOINT = 10;
    private int totalBlocks = db.getCheckpointCount();

    public void checkpointTripped(int checkpointTripped, boolean untrip) {
        if (!SystemStateManager.getInstance().needsTrip(checkpointTripped, untrip)) {
            if (!db.isBlockOccupied(calculateNextBlock(checkpointTripped, false))) {
                logger.log(Level.WARNING, "Inconsistent checkpoint trip");
                String id = checkpointTripped > 9 ? "CH" + checkpointTripped : "CH0" + checkpointTripped;
                SystemStateManager.getInstance().addUnresponsiveClient(id, ReasonEnum.INCORTRIP);
            } else {
                handleTrip(checkpointTripped, untrip);
            }
        }
    }

    public void handleTrip(int checkpoint, boolean untrip) {
        try {
            Optional<BladeRunnerClient> bladeRunner = getBladeRunner(checkpoint);

            if (bladeRunner.isPresent()) {
                if (untrip) {
                    int checkNextBlock = calculateNextBlock(checkpoint, true);
                    // check if next block or current block is occupied
                    if (db.isBlockOccupied(checkNextBlock) || db.isBlockOccupied(checkpoint)) {
                        db.updateBladeRunnerBlock(bladeRunner.get().getId(), checkpoint);
                        bladeRunner.get().changeZone(checkpoint);
                        bladeRunner.get().sendExecuteMessage(SpeedEnum.STOP);
                        bladeRunner.get().updateStatus("STOPPED");
                    } else {
                        db.updateBladeRunnerBlock(bladeRunner.get().getId(), checkpoint);
                        bladeRunner.get().changeZone(checkpoint);
                    }

                    checkForTraffic(checkpoint);
                } else {
                    if (db.isBlockOccupied(checkpoint)) {
                        bladeRunner.get().sendExecuteMessage(SpeedEnum.STOP);
                        bladeRunner.get().updateStatus("STOPPED");
                    }
                }
            } else {
                logger.log(Level.SEVERE, "Attempted to get non-existent bladerunner");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error: ", e);
        }
    }

        public Optional<BladeRunnerClient> getBladeRunner ( int checkpoint){
        totalBlocks = db.getCheckpointCount();

        String bladeRunnerID = checkpoint == 1 ? db.getLastBladeRunnerInBlock(totalBlocks) : db.getLastBladeRunnerInBlock(checkpoint - 1);
        Optional<BladeRunnerClient> opBladeRunner = Optional.empty();

        if (db.isBlockOccupied(calculateNextBlock(checkpoint, false)))
                opBladeRunner = db.getClient(bladeRunnerID,BladeRunnerClient.class);

        Optional<BladeRunnerClient> bladeRunner = Optional.empty();
        if (opBladeRunner.isPresent())
            bladeRunner = opBladeRunner;

        return bladeRunner;
    }

    public void checkForTraffic(int block) {
        // Check if block is occupied, if it is rerun handle BladeRunner speed for that
        // BladeRunner
        int trafficBlock = calculateNextBlock(block, false);
        Optional<BladeRunnerClient> bladeRunner = getBladeRunner(trafficBlock);

        if (bladeRunner.isPresent()) {
            bladeRunner.get().sendExecuteMessage(SpeedEnum.SLOW);
            bladeRunner.get().updateStatus("STARTED");
        }

        //no traffic, do nothing
    }

    private int calculateNextBlock(int checkpoint, boolean next) {
        int totalBlocks = db.getCheckpointCount();

        if (next) // Calculate next block
            return (checkpoint % totalBlocks) + 1;
        else // Calculate previous block
            return ((checkpoint + totalBlocks - 2) % totalBlocks) + 1;
    }
}

