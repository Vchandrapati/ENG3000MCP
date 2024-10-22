package org.example.state;

import org.example.Database;

import java.util.logging.Level;
import java.util.logging.Logger;

// Initial waiting phase, either exits when ten minutes is over or started by command
// No emergency mode can happen in this phase
public class WaitingState implements SystemStateInterface {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static Database db = Database.getInstance();

    // All time units in milliseconds
    private static final long STARTUP_CONNECTION_TIME_PERIOD = 600000; // ten minutes
    private static final long TIME_ON_START = System.currentTimeMillis(); // the time when counter
                                                                          // 10-minute timer started
    private static final long TIME_BETWEEN_RUNNING = 5000; // 5 seconds

    private static final int MAX_BLADERUNNERS = 5;
    private static final int MAX_CHECKSTATIONS = 10;

    // Next state of this state
    private static final SystemState NEXT_STATE = SystemState.MAPPING;

    // Performs the operation of this state at set intervals according to TIME_BETWEEN_RUNNING
    // If returns true then system goes to NEXT_STATE
    @Override
    public boolean performOperation() {
        long elapsedTime = System.currentTimeMillis() - TIME_ON_START;

        // Checks if the timeout has occured or if the correct amount of clients have joined
        if (elapsedTime >= STARTUP_CONNECTION_TIME_PERIOD
                || (db.getBladeRunnerCount() == MAX_BLADERUNNERS
                        && db.getCheckpointCount() + db.getStationCount() == MAX_CHECKSTATIONS)) {
            logger.log(Level.INFO, "Timeout state has occured");
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
}


