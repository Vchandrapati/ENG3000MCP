package org.example;

import java.util.*;

public class StartupState implements SystemStateInterface {
    // All time units in milliseconds
    private static long trainStartupTimeout = 15000; // 15 seconds
    private static long timeBetweenRunning = 500; // 1/2 second
    private static long startupConnectionTimePeriod = 600000; // ten minutes

    private long timeOnStart = System.currentTimeMillis();

    // Next state to be performed after this one is completed, if not interrupted
    private static SystemState nextState = SystemState.RUNNING;

    private int currentTrain = 0;
    // Holds the information for processing the current train
    private CurrentTrainInfo currentTrainInfo = null;

    private boolean startMapping = false;

    // Holds each trainclient currently connected to the server
    private List<TrainClient> trains;

    // flag to start early before 10 minute timer has finished
    private static boolean startEarly = false;

    @Override
    public boolean performOperation() {
        // if the program has started mapping
        if (startMapping)
            return startMapping(); 
        // else check if ready to map and return false
        else {
            try {
                // Check if the required number of clients are connected
                int curTrains = db.getTrainCount();
                int curStations = db.getStationCount();
                int curCheckpoints = db.getCheckpointCount();
                checkReadyToMap(curTrains, curStations, curCheckpoints);
            } catch (Exception e) {
                logger.warning("Failed to grab trains from database");
            }
        }
        return false;
    }

    // checks if mapping is ready to occur
    private void checkReadyToMap(int curTrains, 
                                 int curStations, 
                                 int curCheckpoints) throws Exception {

        // If the timeout of 10 minutes has occured or started early
        if ((System.currentTimeMillis() - timeOnStart >= startupConnectionTimePeriod) ||                                                             
            (startEarly)) 
            {
                logger.info("Mapping conditions met, trying to grab trains");
                trains = db.getTrains().get();
                if (trains != null && trains.size() > 0) startMapping = true;
            }
    }

    // maps the current train
    private boolean startMapping() {
        if (currentTrainInfo == null) {
            currentTrainInfo = new CurrentTrainInfo(trains.get(currentTrain));
        } else if (currentTrainInfo.process(trainStartupTimeout)) {
            currentTrain++;
            if (currentTrain > trains.size() - 1) {
                logger.info("All trains mapped proceeding to " + getNextState().toString());
                return true;
            }
            currentTrainInfo = new CurrentTrainInfo(trains.get(currentTrain));
        }
        return false;
    }

    public static void startEarly() {
        startEarly = true;
    }

    @Override
    public long getTimeToWait() {
        return StartupState.timeBetweenRunning;
    }

    @Override
    public SystemState getNextState() {
        return StartupState.nextState;
    }

    @Override
    public void reset() {
        StartupState.startEarly = false;
    }
}