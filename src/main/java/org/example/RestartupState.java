package org.example;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class RestartupState implements SystemStateInterface {
    //This state occurs after the emergency state has happened, assumes all current WORKING trains are stationary
    //This state only occurs when a client has not responded to their status message after 2 seconds
    //Retries connecting with the lost client/s

    //TO BE COMPLETED

    private static long timeToWait = 500;
    private static SystemState nextState = SystemState.RUNNING;
    private static long timeout = 10000;

    private final Database db = Database.getInstance();
    private int maxTrains = db.getMaxBR();
    private int maxStations = db.getMaxCH();
    private int maxCheckpoints = db.getMaxST();

    private int currentTrain = 0;

    public boolean performOperation() {
        // Check if the required number of trains, stations, and checkpoints are connected
        int curTrains = db.getTrainCount();
        int curStations = db.getStationCount();
        int curCheckpoints = db.getCheckpointCount();

        return false;
    }

    public long getTimeToWait() {
        return RestartupState.timeToWait;
    }

    public SystemState getNextState() {
        return RestartupState.nextState;
    }

    @Override
    public void reset() {
        return;
    }
}
