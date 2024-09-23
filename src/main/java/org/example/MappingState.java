package org.example;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MappingState implements SystemStateInterface{
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    //if a train cannot be mapped, stop that train and go to waiting

    // All time units in milliseconds
    private static final long TRAIN_MAPPING_DEATH_TIMEOUT = 60000; // 1 minute
    private static long CURRENT_TRAIN_START_TIME = System.currentTimeMillis();
    private static final long TRAIN_MAPPING_RETRY_TIMEOUT = 15000; // 15 seconds
    private static final long TIME_BETWEEN_RUNNING = 500; //1 second
    private static final SystemState NEXT_STATE = SystemState.RUNNING;

    private List<TrainClient> trainsToMap = new ArrayList<>();
    private boolean startMapping = false;
    private CurrentTrainInfo currentTrainInfo = null;
    private int currentTrainIndex = 0;

    private final Database db = Database.getInstance();
    
    @Override
    public boolean performOperation() {
        if(!startMapping) {
            //grab all train clients
            List<TrainClient> tempTrainsToMap = db.getTrainClients();

            //get all unmapped trains
            for (TrainClient trainClient : tempTrainsToMap) {
                if(!trainClient.isCurrentlyMapped()) {
                    trainsToMap.add(trainClient);
                }
            }
            startMapping = true;
        }

        if(startMapping) {
            //if a train does not map in a minute time, send stop message to current train and go to waiting state
            long elapsedTime = System.currentTimeMillis() - CURRENT_TRAIN_START_TIME;
            if(elapsedTime >= TRAIN_MAPPING_DEATH_TIMEOUT) {
                trainsToMap.get(currentTrainIndex).sendExecuteMessage(SpeedEnum.STOP);
                SystemStateManager.getInstance().setState(SystemState.WAITING);
                logger.log(Level.SEVERE, "Blade runner failed to be mapped in time");
            }
            return mapClients();
        }

        return false;
    }

    protected boolean mapClients() {
         //If no clients are available, complete mapping
        if (trainsToMap.isEmpty() || trainsToMap == null) {
            logger.warning("No trains to map.");
            return true;
        }

        if (currentTrainInfo == null) {
            if (currentTrainIndex < trainsToMap.size()) 
                currentTrainInfo = new CurrentTrainInfo(trainsToMap.get(currentTrainIndex));
        } 
        else if (currentTrainInfo.process(TRAIN_MAPPING_RETRY_TIMEOUT)) {
            currentTrainIndex++;
            if (currentTrainIndex >= trainsToMap.size()) {
                logger.info("All clients have been mapped.");
                return true;
            }
            currentTrainInfo = new CurrentTrainInfo(trainsToMap.get(currentTrainIndex));
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
        startMapping = false;
        currentTrainInfo = null;
        currentTrainIndex = 0;
        trainsToMap = null;
    }
}
