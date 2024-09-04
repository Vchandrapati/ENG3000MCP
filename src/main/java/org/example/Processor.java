package org.example;

import java.util.HashMap;

public class Processor {

    int trainCount = 5;
    int checkPointCount = 10;
    int sensorAmount = 10;
    int STOP = 0;
    Database db = Database.getInstance();

    // this needs to get updated every time vikil sends sensor trip
    public void sensorTripped(int sensorTripped){
        handleTrainSpeed(sensorTripped);
    }

    public void handleTrainSpeed(int sensor) {
        String trainID = db.getLastTrainInBlock(sensor-1);
        TrainClient t = db.getTrain(trainID);
        db.updateTrainBlock(trainID, sensor);

        //check if current block is already occupied and stop if it is(SHOULD NEVER BE THE CASE)
        if(db.isBlockOccupied(sensor)){
            t.sendExecuteMessage(STOP);
        }

        //check if block in front is occupied and stop if it is
        int checkNextBlock = (sensor+1)%11;
        if(checkNextBlock == 0){
            checkNextBlock = 1;
        }

        if(db.isBlockOccupied(checkNextBlock)){
            t.sendExecuteMessage(STOP);
        }     

        //get che
        int blockBeforeTrainsOriginalLocation = (sensor-2)%11;
        if(blockBeforeTrainsOriginalLocation == 0){
            blockBeforeTrainsOriginalLocation = db.getCheckpointCount();
        }
        //as train is moving out of block, check if previous block was being held up by current train
        checkForTraffic(blockBeforeTrainsOriginalLocation);
    }

    public void checkForTraffic(int block){
        //check if block is occupied, if it is rerun handle Train speed for that traun
        if(db.isBlockOccupied(block)){
            //+1 because handleTrainSpeed gets the train behind the sensor being passed
            handleTrainSpeed(block + 1);
        } 
    }
}