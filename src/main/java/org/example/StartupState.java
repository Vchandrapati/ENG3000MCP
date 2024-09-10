package org.example;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.*;

public class StartupState implements SystemStateInterface {
    //All time units in milliseconds
    private static long trainStartupTimeout = 10000; 
    private static long timeBetweenRunning = 500; 
    private static long startupConnectionTimePeriod = 600000; //ten minutes
    private long timeOnStart = System.currentTimeMillis();

    //Next state to be performed after this one is completed, if not interrupted
    private static SystemState nextState = SystemState.RUNNING;

    //max amount of trains, checkpoints and stations that will be on the track
    private final Database db = Database.getInstance();
    private int maxTrains = db.getMaxBR();
    private int maxStations = db.getMaxST();
    private int maxCheckpoints = db.getMaxCH();

    private int currentTrain = 0;
    //Holds the information for processing the current train
    private CurrentTrainInfo currentTrainInfo = null;

    private boolean startMapping = false;

    //Holds each trainclient currently connected to the server
    private List<TrainClient> trains;

    //flag is starting early before 10 minute timer or before all supposed to be clients have joined
    private static boolean startEarly = false;

    //private static 

    @Override
    public boolean performOperation() {
        // Check if the required number of trains, stations, and checkpoints are connected
        int curTrains = db.getTrainCount();
        int curStations = db.getStationCount();
        int curCheckpoints = db.getCheckpointCount();

        //if the program has started mapping
        if(startMapping) return startMapping(); // Start mapping process once all clients are connected
        //else check if ready to map and return false
        try {
            checkReadyToMap(curTrains, curStations, curCheckpoints);
        } catch (Exception e) {
            logger.info("Error retrieving trains info from database");
        }
        return false;
    }

    //checks if mapping is ready to occur
    private void checkReadyToMap(int curTrains, int curStations, int curCheckpoints) throws Exception {
        if( (curTrains == maxTrains && curStations == maxStations && curCheckpoints == maxCheckpoints) || //If all clients have connected
        (System.currentTimeMillis() - timeOnStart >= startupConnectionTimePeriod) || //If the timeout of 10 minutes has occured
        (startEarly)) //If prompted to start early through the console
        {
            trains = db.getTrains().get();
            if(trains != null && trains.size() > 0) startMapping = true; 
        }
    }


    //maps the current train
    private boolean startMapping() {
        if(currentTrainInfo == null) {
            currentTrainInfo = new CurrentTrainInfo(trains.get(currentTrain));
        }
        else if(currentTrainInfo.process()) {
            currentTrain++;
            if (currentTrain > maxTrains) {
                return true;
            }
        }
        return false;
    }

    public static void startEarly() {
        startEarly = true;
    }

    public static void trippedSensor(int checkpoint) {

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

    private class CurrentTrainInfo {
        private final TrainClient train;
        private boolean hasSent;
        private long timeSinceSent;

        public CurrentTrainInfo(TrainClient train) {
            this.train = train;
        }

        //Processes the current train to move to next checkpoint, keeps trying until it reaches
        private boolean process() {
            if (!hasSent) {
                sendTrainToNextCheckpoint();
            } else if (System.currentTimeMillis() - timeSinceSent > trainStartupTimeout) {
                retrySending();
            } else {
                CheckpointClient hitClient = null;
                try {
                    // Retrieve the tripped Client using Future.get(), which blocks until the result is available
                    Future<CheckpointClient> hitClientFuture = db.getLastTrip();
                    hitClient = hitClientFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    // Handle exceptions from Future.get()
                    logger.severe("Error retrieving train from database: " + e.getMessage());
                }
                if (hitClient != null) {
                    stopTrainAtCheckpoint(hitClient);
                    hitClient.reset();
                    return true;
                }
            }

            return false;
        }

        //Send speed message to the current train
        private void sendTrainToNextCheckpoint() {
            if(train == null) {
                return;
            }
            logger.info("Sending train to move to " + currentTrainInfo.train.id);
            long tempTime = System.currentTimeMillis();
            String message = MessageGenerator.generateExecuteMessage("ccp", train.id, tempTime, 1);
            train.sendMessage(message);
            timeSinceSent = tempTime;
            hasSent = true;
        }

        //Tells the current train to stop when a checkpoint has been detected
        private void stopTrainAtCheckpoint(CheckpointClient hitClient) {
            String message = MessageGenerator.generateExecuteMessage("ccp", train.id, System.currentTimeMillis(), 0);
            train.sendMessage(message);
            train.changeZone(hitClient.getLocation());
            hasSent = true;
        }

        //If the current train has not responded after timeout time then retry
        private void retrySending() {
            hasSent = false;
            sendTrainToNextCheckpoint();
        }
    }
}
