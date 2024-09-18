package org.example;

import java.util.*;
import java.util.logging.Logger;

public class RunningState implements SystemStateInterface {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private boolean allRunning = false;
    private static final SystemState nextState = SystemState.RUNNING;
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

    private void moveAllTrains() {
        try {
            List<TrainClient> trains = Database.getInstance().getTrainClients();
            if(trains != null && !trains.isEmpty()) {
                allRunning = true;
                for (TrainClient trainClient : trains) {
                    trainClient.sendExecuteMessage(SpeedEnum.SLOW);
                }
                logger.info("All Trains are now moving at speed 1");
            }
        } catch (Exception e) {
            logger.severe("Failed to move trains: " + e.getMessage());
            SystemStateManager.getInstance().setState(SystemState.EMERGENCY);
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