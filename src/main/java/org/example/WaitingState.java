package org.example;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

public class WaitingState implements SystemStateInterface {

    //Initial waiting phase, either exits when ten minutes is over or started by command
    //No emergency mode can happen in this phase

    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // All time units in milliseconds
    private static final long STARTUP_CONNECTION_TIME_PERIOD = 600000; // ten minutes
    private static final long TIME_ON_START = System.currentTimeMillis(); // the time when counter 10-minute timer started
    private static final long TIME_BETWEEN_RUNNING = 5000; //5 seconds

    private static final SystemState NEXT_STATE = SystemState.MAPPING;



    @Override
    public boolean performOperation() {
        long elapsedTime = System.currentTimeMillis() - TIME_ON_START;

        // Check if early start or timeout
        if ((elapsedTime >= STARTUP_CONNECTION_TIME_PERIOD || SystemStateManager.getInstance().hasStartedEarly())) {
            return true;
        }
        return false;
    }


    @Override
    public long getTimeToWait() {
        return TIME_BETWEEN_RUNNING;
    }

    @Override
    public SystemState getNextState() {
        return NEXT_STATE;
    }

    @Override
    public void reset() {
        //Nothing to reset
    }

}



