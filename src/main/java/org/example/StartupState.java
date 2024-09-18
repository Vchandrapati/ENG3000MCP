package org.example;

public class StartupState extends MappingState {
    // ten minutes in milliseconds
    private static final long STARTUP_CONNECTION_TIME_PERIOD = 600000; 

    // Flag to start early before 10 minute timer has finished
    private static boolean startEarly = false;

    //the time when counter 10 minute timer started
    private final long timeOnStart = System.currentTimeMillis();

    // checks if mapping is ready to occur
    @Override
    protected boolean checkReadyToMap() {
        long elapsedTime = System.currentTimeMillis() - timeOnStart;

        // Check if early start or timeout
        if ((elapsedTime >= STARTUP_CONNECTION_TIME_PERIOD || startEarly)) {
            logger.info("Mapping conditions met, attempting to return trains from database");
            trainsToMap = db.getTrains();
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