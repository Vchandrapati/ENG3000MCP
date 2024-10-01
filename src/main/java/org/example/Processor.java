package org.example;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Processor {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final Database db = Database.getInstance();
    private int totalBlocks = db.getCheckpointCount();

    public void checkpointTripped(int checkpointTripped, boolean untrip) {
        SystemStateManager systemStateManager = SystemStateManager.getInstance();
        totalBlocks = db.getCheckpointCount();

        if (systemStateManager.needsTrip(checkpointTripped, untrip)) return;

        if (!db.isBlockOccupied(calculatePreviousBlock(checkpointTripped)) || checkpointTripped > totalBlocks || checkpointTripped <= 0) {
            logger.log(Level.WARNING, "Inconsistent checkpoint trip");
            String id = checkpointTripped > 9 ? "CH" + checkpointTripped : "CH0" + checkpointTripped;
            systemStateManager.addUnresponsiveClient(id, ReasonEnum.INCORTRIP);
        } else {
            handleTrip(checkpointTripped, untrip);
        }
    }

    public void handleTrip(int curBlock, boolean untrip) {
        try {
            Optional<BladeRunnerClient> optionalBladeRunner = getBladeRunner(curBlock);

            if (optionalBladeRunner.isEmpty()) {
                logger.log(Level.SEVERE, "Attempted to get non-existent bladerunner");
                return;
            }

            BladeRunnerClient bladeRunnerClient = optionalBladeRunner.get();

            if (untrip) {
                int nextBlock = calculateNextBlock(curBlock);
                // check if current block is occupied if so set system state to emergency
                if (db.isBlockOccupied(curBlock))
                    SystemStateManager.getInstance().setState(SystemState.EMERGENCY);

                // check if next block is occupied if so stops trains and creates queue
                if (db.isBlockOccupied(nextBlock)) {
                    bladeRunnerClient.sendExecuteMessage(SpeedEnum.STOP);
                    bladeRunnerClient.updateStatus("STOPPED");
                }

                db.updateBladeRunnerBlock(bladeRunnerClient.getId(), curBlock);
                bladeRunnerClient.changeZone(curBlock);
                checkForTraffic(curBlock);
            } else {
                if (db.isBlockOccupied(curBlock)) {
                    bladeRunnerClient.sendExecuteMessage(SpeedEnum.STOP);
                    bladeRunnerClient.updateStatus("STOPPED");
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error: ", e);
        }
    }

    public Optional<BladeRunnerClient> getBladeRunner(int checkpoint) {
        int previousCheckpoint = calculatePreviousBlock(checkpoint);
        String bladeRunnerID = db.getLastBladeRunnerInBlock(previousCheckpoint);

        if (db.isBlockOccupied(calculatePreviousBlock(checkpoint)))
            return db.getClient(bladeRunnerID, BladeRunnerClient.class);

        return Optional.empty();
    }

    public void checkForTraffic(int block) {
        int trafficBlock = calculatePreviousBlock(block);
        Optional<BladeRunnerClient> bladeRunner = getBladeRunner(trafficBlock);

        bladeRunner.ifPresent(br -> {
            br.sendExecuteMessage(SpeedEnum.SLOW);
            br.updateStatus("STARTED");
        });
        //no traffic, do nothing
    }

    private int calculateNextBlock(int checkpoint) {
        return (checkpoint % totalBlocks) + 1;
    }

    private int calculatePreviousBlock(int checkpoint) {
        return ((checkpoint + totalBlocks - 2) % totalBlocks) + 1;
    }
}

