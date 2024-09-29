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
            int checkpointTemp = checkpointTripped -1;
            if (checkpointTemp == 0) {
                checkpointTemp = HIGHEST_CHECKPOINT;
            }

            if (!db.isBlockOccupied(checkpointTemp)) {
                logger.log(Level.WARNING, "inconsistent checkpoint trip");
                String id = (checkpointTripped > 9) ? "CH" + checkpointTripped : "CH0" + checkpointTripped;
                SystemStateManager.getInstance().addUnresponsiveClient(id, ReasonEnum.INCORTRIP);
            } else {
                if (!untrip) {
                    handletrip(checkpointTripped, false);
                } else {
                    handletrip(checkpointTripped, true);
                }
            }
        }
    }

    public void handletrip(int checkpoint, boolean untrip) {
        try {
            String bladeRunnerID = checkpoint == 1 ? db.getLastBladeRunnerInBlock(HIGHEST_CHECKPOINT)
                    : db.getLastBladeRunnerInBlock(checkpoint - 1);
            Optional<BladeRunnerClient> opBladeRunner = db.<BladeRunnerClient>getClient(bladeRunnerID,
                    BladeRunnerClient.class);
            System.out.println(bladeRunnerID);

            BladeRunnerClient bladeRunner;
            if (opBladeRunner.isPresent()) {
                bladeRunner = opBladeRunner.get();
            } else {
                logger.log(Level.SEVERE, "Attempted to get non-existant bladerunner: {0}", bladeRunnerID);
                return;
            }

            if (untrip) {
                db.updateBladeRunnerBlock(bladeRunnerID, checkpoint);
                bladeRunner.changeZone(checkpoint);

                int checkNextBlock = calculateNextBlock(checkpoint);
                // check if next block or current block is occupied
                if (db.isBlockOccupied(checkNextBlock) || db.isBlockOccupied(checkpoint)) {
                    bladeRunner.sendExecuteMessage(SpeedEnum.STOP);
                    bladeRunner.updateStatus("STOPPED");
                }
                checkForTraffic(checkpoint);
            } else {
                if (db.isBlockOccupied(checkpoint)) {
                    bladeRunner.sendExecuteMessage(SpeedEnum.STOP);
                    bladeRunner.updateStatus("STOPPED");
                }
            }

        }catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error: ", e);
        }
    }

    public void checkForTraffic(int block) {
        int currentBlock = block;
        // Check if block is occupied, if it is rerun handle BladeRunner speed for that
        // BladeRunner

        if (db.isBlockOccupied(currentBlock - 2)) {
            handletrip(currentBlock - 1, true);
        }
    }

    private int calculateNextBlock(int checkpoint) {
        totalBlocks = db.getCheckpointCount();
        int nextBlock = (checkpoint) % totalBlocks;
        if (nextBlock == 0) {
            return 1;
        } else {
            return nextBlock;
        }
    }

}

