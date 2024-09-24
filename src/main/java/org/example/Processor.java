package org.example;

import java.util.logging.Logger;

public class Processor {

    //POTENTIAL ISSUES TO BE FIXED IN PROCESSOR

    //[1] Setting status of clients, and not using them if dead, like checkpoints for example
    
    //[2] line 29

    //[3] line 38, why are we starting trains when they should alredy be on?

    //[4] never checks if the sensor tripped zone is full already

    //[5] no logging, needs to be proper format, and more logs please

    //[6] line 52, if this is uncommented, it will run the normal handle train code, then rerun it no matter what, causing the train
    // to be section +1 then what it should be 

    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final Database db = Database.getInstance();
    private static int TOTAL_BLOCKS = db.getCheckpointCount();

    public void sensorTripped(int sensorTripped) {
        if (!SystemStateManager.getInstance().needsTrip(sensorTripped))
            handleTrainSpeed(sensorTripped);
    }

    public void handleTrainSpeed(int sensor) {
        try {
            //need to check total blocks each time, may change due to connecting or disconnect checkpoints
            TOTAL_BLOCKS = db.getCheckpointCount();

            //[2] what about the a train in the firt section?, you will get -1 no?
            String trainID = db.getLastTrainInBlock(sensor - 1);
            // Maybe put an error to catch this
            TrainClient train = (TrainClient) db.getClient(trainID);

            db.updateTrainBlock(trainID, sensor);
            train.changeZone(sensor);
            //train.updateStatus("STARTED");

            // Check if block in front is occupied and stop if it is
            int checkNextBlock = calculateNextBlock(sensor + 1);

            if (db.isBlockOccupied(checkNextBlock)) {
                train.sendExecuteMessage(SpeedEnum.STOP);
                train.updateStatus("STOPPED");
            }

            //this is always running?
            //int previousBlock = calculatePreviousBlock(sensor);
            //checkForTraffic(previousBlock);
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e);
            SystemStateManager.getInstance().setState(SystemState.EMERGENCY);
        }
    }

    public void checkForTraffic(int block) {
        int currentBlock = block;
        // Check if block is occupied, if it is rerun handle Train speed for that train
        while (db.isBlockOccupied(block)) {
            // +1 because handleTrainSpeed gets the train behind the sensor being passed
            handleTrainSpeed(currentBlock + 1);
            currentBlock = calculatePreviousBlock(currentBlock + 1);
        }
    }

    private int calculateNextBlock(int sensor) {
        int nextBlock = (sensor) % TOTAL_BLOCKS;
        return nextBlock == 0 ? 1 : nextBlock;
    }

    private int calculatePreviousBlock(int sensor) {
        int previousBlock = (sensor - 2) % TOTAL_BLOCKS;
        return previousBlock == 0 ? db.getCheckpointCount() : previousBlock;
    }
}