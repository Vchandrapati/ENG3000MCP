package org.example;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Processor {

    // POTENTIAL ISSUES TO BE FIXED IN PROCESSOR

    // [1] Setting status of clients, and not using them if dead, like checkpoints
    // for example

    // [3]why are we starting trains when they should alredy be on? ??? Where is this sir

    // [5] no logging, needs to be proper format, and more logs please

    // [6] line 52, if this is uncommented, it will run the normal handle
    // BladeRunner
    // code, then rerun it no matter what, causing the BladeRunner
    // to be section +1 then what it should be

    // [7] need to deal with trip and untrip btw

    //[8] If a checkpoint trips, but there is no train before, the code dies, need a null check

    //[9] for checking if the current block is occupied, u put the current zone in that train and then check instead of other way around

    //[10] does not check for traffic?

    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final Database db = Database.getInstance();
    private static final int HIGHEST_CHECKPOINT = 10;
    private int totalBlocks = db.getCheckpointCount();


    public void checkpointUntripped(int checkpointTripped) {
        if (!SystemStateManager.getInstance().needsTrip(checkpointTripped, true)) {
            handleUntripped(checkpointTripped, false);
        }
    }
    
    public void checkpointTripped(int checkpointTripped) {
        if (!SystemStateManager.getInstance().needsTrip(checkpointTripped, true)) {
            handleTripped(checkpointTripped, false);
        }
    }

    public void handleUntripped(int checkpoint, boolean tripped) {
        try {
            String bladeRunnerID = checkpoint == 1 ? db.getLastBladeRunnerInBlock(HIGHEST_CHECKPOINT)
                    : db.getLastBladeRunnerInBlock(checkpoint - 1);
            Optional<BladeRunnerClient> opBladeRunner = db.<BladeRunnerClient>getClient(bladeRunnerID,
                    BladeRunnerClient.class);

            BladeRunnerClient bladeRunner;
            if (opBladeRunner.isPresent()) {
                bladeRunner = opBladeRunner.get();
            } else {
                logger.log(Level.SEVERE, "Attempted to get non-existant bladerunner: {0}", bladeRunnerID);
                return;
            }

            db.updateBladeRunnerBlock(bladeRunnerID, checkpoint);
            bladeRunner.changeZone(checkpoint);

            int checkNextBlock = calculateNextBlock(checkpoint);
            // check if next block or current block is occupied
            if (db.isBlockOccupied(checkNextBlock) || db.isBlockOccupied(checkpoint)) {
                bladeRunner.sendExecuteMessage(SpeedEnum.STOP);
                bladeRunner.updateStatus("STOPPED");
            }

            checkForTraffic(checkpoint);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error: ", e);
        }
    }


    private void handleTripped(int checkpoint, boolean tripped) {
        try {
            String bladeRunnerID = checkpoint == 1 ? db.getLastBladeRunnerInBlock(HIGHEST_CHECKPOINT)
                    : db.getLastBladeRunnerInBlock(checkpoint - 1);
            Optional<BladeRunnerClient> opBladeRunner = db.<BladeRunnerClient>getClient(bladeRunnerID,
                    BladeRunnerClient.class);

            BladeRunnerClient bladeRunner;
            if (opBladeRunner.isPresent()) {
                bladeRunner = opBladeRunner.get();
            } else {
                logger.log(Level.SEVERE, "Attempted to get non-existant bladerunner: {0}", bladeRunnerID);
                return;
            }
            // check if next block or current block is occupied
            if (db.isBlockOccupied(checkpoint)) {
                bladeRunner.sendExecuteMessage(SpeedEnum.STOP);
                bladeRunner.updateStatus("STOPPED");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error: ", e);
        }
    }


    private void updateBladeRunnerId(int checkpoint, String bladeRunnerID) {
        try {
            Optional<BladeRunnerClient> opBladeRunner = db.<BladeRunnerClient>getClient(bladeRunnerID,
                    BladeRunnerClient.class);
            BladeRunnerClient bladeRunner;
            if (opBladeRunner.isPresent()) {
                bladeRunner = opBladeRunner.get();
            } else {
                logger.log(Level.SEVERE, "Attempted to get non-existant bladerunner: {0}", bladeRunnerID);
                return;
            }

            db.updateBladeRunnerBlock(bladeRunnerID, checkpoint);
            bladeRunner.changeZone(checkpoint);

            // Check if block in front is occupied and stop if it is
            int checkNextBlock = calculateNextBlock(checkpoint + 1);

            // check if next block or current block is occupied
            if (db.isBlockOccupied(checkNextBlock) || db.isBlockOccupied(checkpoint)) {
                bladeRunner.sendExecuteMessage(SpeedEnum.STOP);
                bladeRunner.updateStatus("STOPPED");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error: ", e);
        }
    }

    public void checkForTraffic(int block) {
        int currentBlock = block;
        // Check if block is occupied, if it is rerun handle BladeRunner speed for that
        // BladeRunner

        if(db.isBlockOccupied(currentBlock - 2)){
            handleUntripped(currentBlock - 1, true);
        }
    }

    private int calculateNextBlock(int checkpoint) {
        totalBlocks = db.getCheckpointCount();
        int nextBlock = (checkpoint) % totalBlocks;
        if(nextBlock == 0){
            return 1;
        } else {
            return nextBlock;
        }
    }

    private int calculatePreviousBlock(int checkpoint) {
        int previousBlock = (checkpoint - 2) % totalBlocks;
        return previousBlock == 0 ? db.getCheckpointCount() : previousBlock;
    }
}