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
        if(!SystemStateManager.getInstance().needsTrip(sensorTripped)) 
            handleTrainSpeed(sensorTripped);
    }

    public void handleTrainSpeed(int sensor) {
        try {
            String trainID = db.getLastTrainInBlock(sensor - 1).get();

            TrainClient t = db.getTrain(trainID).get();
            db.updateTrainBlock(trainID, sensor);
            t.changeZone(sensor);
            t.updateStatus("STARTED");


            //check if block in front is occupied and stop if it is
            int checkNextBlock = (sensor + 1) % 11;
            if (checkNextBlock == 0) {
                checkNextBlock = 1;
            }

            if (db.isBlockOccupied(checkNextBlock).get()) {
                t.sendExecuteMessage(SpeenEnum.STOP);
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
            logger.severe("No train in zone before " + e.getMessage());
            SystemStateManager.getInstance().setState(SystemState.EMERGENCY);
        }
    }


    public void checkForTraffic(int block) throws InterruptedException, ExecutionException{
        //check if block is occupied, if it is rerun handle Train speed for that train
        if(db.isBlockOccupied(block).get()){
            //+1 because handleTrainSpeed gets the train behind the sensor being passed
            handleTrainSpeed(block + 1);
        }
    }
}