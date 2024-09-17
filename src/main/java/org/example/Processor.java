package org.example;

import java.util.concurrent.ExecutionException;

import java.util.logging.Logger;

public class Processor {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final Database db = Database.getInstance();
    private static final int TOTAL_BLOCKS = db.getCheckpointCount();
    public void sensorTripped(int sensorTripped) {
        if(!SystemStateManager.getInstance().needsTrip(sensorTripped)) 
            handleTrainSpeed(sensorTripped);
    }

    public void handleTrainSpeed(int sensor) {
        try {
            String trainID = db.getLastTrainInBlock(sensor - 1);
            TrainClient train = db.getTrain(trainID);

            db.updateTrainBlock(trainID, sensor);
            train.changeZone(sensor);
            train.updateStatus("STARTED");

            // Check if block in front is occupied and stop if it is
            int checkNextBlock = calculateNextBlock(sensor);

            if (db.isBlockOccupied(checkNextBlock)) {
                train.sendExecuteMessage(SpeedEnum.STOP);
                train.updateStatus("STOPPED");
            }

            int previousBlock = calculatePreviousBlock(sensor);
            checkForTraffic(previousBlock);
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e.getMessage());
            SystemStateManager.getInstance().setState(SystemState.EMERGENCY);
        }
    }

    public void checkForTraffic(int block) {
        int currentBlock = block;
        // Check if block is occupied, if it is rerun handle Train speed for that train
        while (db.isBlockOccupied(block)){
            // +1 because handleTrainSpeed gets the train behind the sensor being passed
            handleTrainSpeed(currentBlock + 1);
            currentBlock = calculatePreviousBlock(currentBlock + 1);
        }
    }

    private int calculateNextBlock(int sensor) {
        int nextBlock = (sensor + 1) % TOTAL_BLOCKS;
        return nextBlock == 0 ? 1 : nextBlock;
    }

    private int calculatePreviousBlock(int sensor) {
        int previousBlock = (sensor - 2) % TOTAL_BLOCKS;
        return previousBlock == 0 ? db.getCheckpointCount() : previousBlock;
    }
}