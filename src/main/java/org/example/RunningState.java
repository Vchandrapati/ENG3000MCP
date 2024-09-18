package org.example;

import java.util.*;

public class RunningState implements SystemStateInterface{
    private boolean allRunning = false;

    private static SystemState nextState = SystemState.RUNNING;
    private static long timeBetweenRunning = 500;

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
                trainClient.sendExecuteMessage(SpeenEnum.SLOW);
            }
            logger.info("All Trains are now moving at speed 1");
        }
        else {
            logger.info("No trains connected!");
        }
    }

    @Override
    public long getTimeToWait() {
        return timeBetweenRunning;
    }

    @Override
    public SystemState getNextState() {
        return nextState;
    }

    @Override
    public void reset() {
        allRunning = false;
    }

}