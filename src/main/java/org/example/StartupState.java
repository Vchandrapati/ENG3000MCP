package org.example;

import java.util.logging.Level;

public class StartupState extends MappingState {
    // ten minutes in milliseconds
    private static final long STARTUP_CONNECTION_TIME_PERIOD = 600000; 

    // Flag to start early before 10-minute timer has finished
    private static boolean startEarly = false;

    //the time when counter 10-minute timer started
    private long timeOnStart = System.currentTimeMillis();

    // checks if mapping is ready to occur
    @Override
    protected boolean checkReadyToMap() {
        long elapsedTime = System.currentTimeMillis() - timeOnStart;

        // Check if early start or timeout
        if ((elapsedTime >= STARTUP_CONNECTION_TIME_PERIOD || startEarly)) {
            trainsToMap = db.getTrainClients();
            logger.log(Level.INFO, "Mapping conditions met, proceeding to move {0} trains", trainsToMap.size());
            return true;
        }
        return false;
    }

    public static void startEarly() {
        startEarly = true;
    }

    @Override
    public void reset() {
        super.reset();
        startEarly = false;
    }
    
}