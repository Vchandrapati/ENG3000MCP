package org.example;

import java.util.concurrent.ExecutionException;

public class StartupState extends MappingState {
    // All time units in milliseconds
    private static final int TRAIN_COUNT = 5;
    private static final int STATION_COUNT = 5;
    private static final int CHECKPOINT_COUNT = 5;
    private static final long STARTUP_CONNECTION_TIME_PERIOD = 600000; // ten minutes
    // Flag to start early before 10 minute timer has finished
    private static boolean startEarly = false;
    private final long timeOnStart = System.currentTimeMillis();

    // checks if mapping is ready to occur
    @Override
    protected boolean checkReadyToMap() {
        long elapsedTime = System.currentTimeMillis() - timeOnStart;

        // Check if early start or if clients have connected
        if ((elapsedTime >= STARTUP_CONNECTION_TIME_PERIOD || startEarly) || (db.getTrainCount() == TRAIN_COUNT
                && db.getStationCount() == STATION_COUNT && db.getCheckpointCount() == CHECKPOINT_COUNT)) {
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