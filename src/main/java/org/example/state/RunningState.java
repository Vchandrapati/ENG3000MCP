package org.example.state;

import java.util.logging.Level;
import java.util.logging.Logger;

// If no issues this state performs the operation of the system normally
public class RunningState implements SystemStateInterface {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final SystemState nextState = SystemState.EMERGENCY;
    private static final long TIME_BETWEEN_RUNNING = 500;

    private boolean allRunning;

    // constructor
    public RunningState () {
        allRunning = false;
    }

    // Performs the operation of this state at set intervals according to TIME_BETWEEN_RUNNING
    // If returns true then system goes to NEXT_STATE
    @Override
    public boolean performOperation () {
        if (!allRunning) {
            logger.log(Level.INFO, "System mapped, startup handed to Processor");
            allRunning = true;
        }

        return false;
    }

    @Override
    public long getTimeToWait () {
        return TIME_BETWEEN_RUNNING;
    }

    @Override
    public SystemState getNextState () {
        return nextState;
    }
}
