package org.example;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.*;

public class StartupState implements SystemStateInterface {
    //State for startup, maps all train locations to a zone

    private static long timeToWait = 500;
    private static SystemState nextState = SystemState.RUNNING;
    private static long timeout = 10000;

    private final Database db = Database.getInstance();
    private int maxTrains = db.getMaxBR();
    private int maxStations = db.getMaxCH();
    private int maxCheckpoints = db.getMaxST();

    private int currentTrain = 0;
    private CurrentTrainInfo currentTrainInfo = null;
    private boolean startMapping = false;
    private List<TrainClient> trains;

    public boolean performOperation() {
        // Check if the required number of trains, stations, and checkpoints are connected
        int curTrains = db.getTrainCount();
        int curStations = db.getStationCount();
        int curCheckpoints = db.getCheckpointCount();

        if(startMapping) {
            return startMapping(); // Start mapping process once all clients are connected
        }
        // Wait until all required clients are connected
        else if(curTrains == maxTrains && curStations == maxStations && curCheckpoints == maxCheckpoints) {
            startMapping = true;
            try {
                // Retrieve the trains using Future.get(), which blocks until the result is available
                Future<List<TrainClient>> futureTrains = db.getTrains();
                trains = futureTrains.get();
            } catch (InterruptedException | ExecutionException e) {
                // Handle exceptions from Future.get()
                logger.severe("Error retrieving train from database: " + e.getMessage());
            }

        }
        return false;
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

    public long getTimeToWait() {
        return StartupState.timeToWait;
    }

    public SystemState getNextState() {
        return StartupState.nextState;
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
            } else if (System.currentTimeMillis() - timeSinceSent > timeout) {
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
                System.out.println();
            }
            System.out.println("Sending train to move to " + currentTrainInfo.train.id);
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
