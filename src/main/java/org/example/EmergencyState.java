package org.example;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class EmergencyState implements SystemStateInterface {
    //Sends stop to all trains
    //TO BE COMPLETED

    private static long timeToWait = 500;
    private static SystemState nextState = SystemState.RESTARTUP;
    private static long timeout = 10000;

    private Database db = Database.getInstance();
    private boolean startedStopping = false;
    private List<TrainClient> trains = null;

    @Override
    public boolean performOperation() {
        if(startedStopping) {
            return haveAllStopped();
        }
        else {
            startedStopping = true;
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

    private boolean haveAllStopped() {
        //TODO
        return false;
    }

    @Override
    public long getTimeToWait() {
        return timeToWait;
    }

    @Override
    public SystemState getNextState() {
        return nextState;
    }

    @Override
    public void reset() {
        return;
    }
        
}
