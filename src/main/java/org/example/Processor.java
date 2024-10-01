package org.example;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Processor {

    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final Database db = Database.getInstance();
    private int totalBlocks = db.getCheckpointCount();

    public void checkpointTripped(int checkpointTripped, boolean untrip) {
        if (!SystemStateManager.getInstance().needsTrip(checkpointTripped, untrip)) {
            totalBlocks = db.getCheckpointCount();
            if (!db.isBlockOccupied(calculatePreviousBlock(checkpointTripped))) {
                logger.log(Level.WARNING, "inconsistent checkpoint trip");
                String id = (checkpointTripped > 9) ? "CH" + checkpointTripped : "CH0" + checkpointTripped;
                SystemStateManager.getInstance().addUnresponsiveClient(id, ReasonEnum.INCORTRIP);
            } else {
                if(!untrip) {
                    handletrip(checkpointTripped, false);
                } else {
                    handletrip(checkpointTripped, true);
                }
            }
        }
    }

    public void handletrip(int checkpoint, boolean untrip) {
        try {
            Optional<BladeRunnerClient> bladeRunner = getBladeRunner(checkpoint);


            if (bladeRunner.isPresent()) {
                BladeRunnerClient bladeRunnerClient = bladeRunner.get();

                if (untrip) {
                    int checkNextBlock = calculateNextBlock(checkpoint);
                    // check if next block or current block is occupied
                    if(db.isBlockOccupied(checkpoint)){
                        bladeRunnerClient.sendExecuteMessage(SpeedEnum.STOP);
                        bladeRunnerClient.updateStatus("STOPPED");
                        SystemStateManager.getInstance().setState(SystemState.EMERGENCY);
                    }

                    if (db.isBlockOccupied(checkNextBlock)) {
                        bladeRunnerClient.sendExecuteMessage(SpeedEnum.STOP);
                        bladeRunnerClient.updateStatus("STOPPED");
                    }
                    db.updateBladeRunnerBlock(bladeRunnerClient.getId(), checkpoint);
                    bladeRunnerClient.changeZone(checkpoint);
                    checkForTraffic(checkpoint);
                } else {
                    if (db.isBlockOccupied(checkpoint)) {
                        bladeRunnerClient.sendExecuteMessage(SpeedEnum.STOP);
                        bladeRunnerClient.updateStatus("STOPPED");
                    }
                }
            } else {
                logger.log(Level.SEVERE, "Attempted to get non-existant bladerunner");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error: ", e);
        }
    }

    public Optional<BladeRunnerClient> getBladeRunner(int checkpoint) {
        totalBlocks = db.getCheckpointCount();
        String bladeRunnerID = checkpoint == 1 ? db.getLastBladeRunnerInBlock(totalBlocks)
                : db.getLastBladeRunnerInBlock(checkpoint - 1);
        Optional<BladeRunnerClient> opBladeRunner = Optional.empty();


        if (db.isBlockOccupied(calculatePreviousBlock(checkpoint))) {
            opBladeRunner = db.getClient(bladeRunnerID, BladeRunnerClient.class);
        }
        Optional<BladeRunnerClient> bladeRunner = Optional.empty();
        if (opBladeRunner.isPresent()) {
            bladeRunner = Optional.of(opBladeRunner.get());
        }
        return bladeRunner;
    }

    public void checkForTraffic(int block) {
        // Check if block is occupied, if it is rerun handle BladeRunner speed for that
        // BladeRunner
        int trafficBlock = calculatePreviousBlock(block);

        Optional<BladeRunnerClient> bladeRunner = getBladeRunner(trafficBlock);
        ;
        if (bladeRunner.isPresent()) {
            bladeRunner.get().sendExecuteMessage(SpeedEnum.SLOW);
            bladeRunner.get().updateStatus("STARTED");
        }
//no traffic, do nothing

    }

    private int calculateNextBlock(int checkpoint) {
        int nextBlock = (checkpoint + 1) % totalBlocks;
        if (nextBlock == 0) {
            return 1;
        } else {
            return nextBlock;
        }

    }

    private int calculatePreviousBlock(int checkpoint) {
        int previousBlock = (checkpoint - 1) % totalBlocks;
        if (previousBlock == 0) {
            return totalBlocks;
        } else {
            return previousBlock;
        }
    }

}

