package org.example;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Processor {
    private static final Logger logger = Logger.getLogger(Processor.class.getName());
    int trainCount = 5;
    int checkPointCount = 10;
    int sensorAmount = 10;
    int STOP = 0;
    Database db = Database.getInstance();

    // this needs to get updated every time vikil sends sensor trip
    public void sensorTripped(int sensorTripped) {
        if(SystemStateManager.getInstance().getState() == SystemState.STARTUP) {
            StartupState.trippedSensor(sensorTripped);
        }
        else {
            handleTrainSpeed(sensorTripped);
        }
    }

    public void handleTrainSpeed(int sensor) {
        try {
            String trainID = db.getLastTrainInBlock(sensor - 1).get();

            TrainClient t = db.getTrain(trainID).get();
            db.updateTrainBlock(trainID, sensor);

            //check if current block is already occupied and stop if it is(SHOULD NEVER BE THE CASE)
            if (db.isBlockOccupied(sensor).get()) {
                t.sendExecuteMessage(STOP);
                t.updateStatus("STOPPED");
            }

            //check if block in front is occupied and stop if it is
            int checkNextBlock = (sensor + 1) % 11;
            if (checkNextBlock == 0) {
                checkNextBlock = 1;
            }

            if (db.isBlockOccupied(checkNextBlock).get()) {
                t.sendExecuteMessage(STOP);
                t.updateStatus("STOPPED");
            }

            //get che
            int blockBeforeTrainsOriginalLocation = (sensor - 2) % 11;
            if (blockBeforeTrainsOriginalLocation == 0) {
                blockBeforeTrainsOriginalLocation = db.getCheckpointCount();
            }
            //as train is moving out of block, check if previous block was being held up by current train
            checkForTraffic(blockBeforeTrainsOriginalLocation);
        } catch (Exception e) {
            logger.severe("Item does not exist in database " + e.getMessage());
        }
    }


    public void checkForTraffic(int block) throws InterruptedException, ExecutionException{
        //check if block is occupied, if it is rerun handle Train speed for that traun
        if(db.isBlockOccupied(block).get()){
            //+1 because handleTrainSpeed gets the train behind the sensor being passed
            handleTrainSpeed(block + 1);
        }
    }
}