package org.example;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunningState implements SystemStateInterface {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final SystemState nextState = SystemState.RUNNING;

    private boolean allRunning;
    private static final long TIME_BETWEEN_RUNNING = 500;

    public RunningState() {
        allRunning = false;
    }

    @Override
    public boolean performOperation() {
        if(!allRunning) {
            try {
                moveAllTrains();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to grab trains from database");
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
                logger.log(Level.INFO, "All Trains are now moving at speed 1");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to move trains");
            SystemStateManager.getInstance().setState(SystemState.EMERGENCY);
        }
    }

    @Override
    public long getTimeToWait() {
        return TIME_BETWEEN_RUNNING;
    }

    @Override
    public SystemState getNextState() {
        return nextState;
    }
}