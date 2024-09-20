package org.example;

import java.util.logging.Level;

import java.util.*;

public class StartupState extends MappingState {
    // ten minutes in milliseconds
    private static final long STARTUP_CONNECTION_TIME_PERIOD = 600000; 

    // the time when counter 10-minute timer started
    private static long timeOnStart = System.currentTimeMillis();

    // checks if mapping is ready to occur
    @Override
    protected boolean checkReadyToMap() {
        long elapsedTime = System.currentTimeMillis() - timeOnStart;

        // Check if early start or timeout
        if ((elapsedTime >= STARTUP_CONNECTION_TIME_PERIOD || SystemStateManager.getInstance().hasStartedEarly())) {
            //Gets all trains
            List<TrainClient> tempTrainsToMap = db.getTrainClients();

            //Only adds trains that have not been mapped, reconnected trains will have this boolean set to false
            for (TrainClient trainClient : tempTrainsToMap) {
                if(!trainClient.isCurrentlyMapped()) {
                    trainsToMap.add(trainClient);
                }
            }

            logger.log(Level.INFO, "Mapping conditions met, proceeding to move {0} trains", trainsToMap.size());
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        super.reset();
    }
    
}