package org.example;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//If no issues this state performs the operation of the system normally
public class RunningState implements SystemStateInterface {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final SystemState nextState = SystemState.RUNNING;

    private boolean allRunning;

    private static final long TIME_BETWEEN_RUNNING = 500;

    //constructor
    public RunningState() {
        allRunning = false;
    }

    //Performs the operation of this state at set intervals according to TIME_BETWEEN_RUNNING
    //If returns true then system goes to NEXT_STATE
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

    //sends a one time message to all trains to make them move
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