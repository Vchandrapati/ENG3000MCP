package org.example;

import java.util.*;

public class RunningState implements SystemStateInterface{
    private boolean allRunning = false;

    @Override
    public boolean performOperation() {
        if(!allRunning) {
            try {
                moveAllTrains();
            } catch (Exception e) {
                logger.warning("Failed to grab trains from database");
            }
        }
        return false;
    }

    private void moveAllTrains() throws Exception {
        List<TrainClient> trains = db.getTrains().get();
        if(trains != null && trains.size() > 0) {
            allRunning = true;
            for (TrainClient trainClient : trains) {
                trainClient.sendExecuteMessage(1);
            }
            logger.info("All Trains are now moving at speed 1");
        }
    }

    @Override
    public long getTimeToWait() {
        return 1000;
    }

    @Override
    public SystemState getNextState() {
        return SystemState.EMERGENCY;
    }

    @Override
    public void reset() {
        return;
    }
    
}
