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

    final int highestCheckpoint = 10;

    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final Database db = Database.getInstance();
    private static final int HIGHEST_CHECKPOINT = 10;
    private int totalBlocks = db.getCheckpointCount();

    
    public void checkpointTripped(int checkpointTripped, boolean untrip) {
        //ASHTON USE BOTH TRIP AND UNTRIP
        //TODO
        if (!SystemStateManager.getInstance().needsTrip(checkpointTripped, untrip)) {
            if (!untrip) handleBladeRunnerSpeed(checkpointTripped);
        }
    }

    public void handleBladeRunnerSpeed(int checkpoint) {
        try {
            // need to check total blocks each time, may change due to connecting or
            // disconnect checkpoints
            totalBlocks = db.getCheckpointCount();

            // [2] what about a BladeRunner in the first section?, you will get -1 no?
            String bladeRunnerID = checkpoint == 1 ? db.getLastBladeRunnerInBlock(HIGHEST_CHECKPOINT)
                    : db.getLastBladeRunnerInBlock(checkpoint - 1);

            updateBladeRunnerId(checkpoint, bladeRunnerID);
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e);
            SystemStateManager.getInstance().setState(SystemState.EMERGENCY);
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
        while (db.isBlockOccupied(block)) {
            // +1 because handleBladeRunnerSpeed gets the BladeRunner behind the checkpoint
            // being passed
            handleBladeRunnerSpeed(currentBlock + 1);
            currentBlock = calculatePreviousBlock(currentBlock + 1);
        }
    }

    private int calculateNextBlock(int checkpoint) {
        int nextBlock = (checkpoint) % totalBlocks;
        return nextBlock == 0 ? 1 : nextBlock;
    }

    private int calculatePreviousBlock(int checkpoint) {
        int previousBlock = (checkpoint - 2) % totalBlocks;
        return previousBlock == 0 ? db.getCheckpointCount() : previousBlock;
    }
}