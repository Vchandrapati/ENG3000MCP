package org.example;

import java.util.List;
import java.util.logging.Logger;

public abstract class MappingState implements SystemStateInterface{
    protected static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // All time units in milliseconds
    protected static final long TRAIN_MAPPING_TIMEOUT = 15_000; // 15 seconds
    protected static final long TIME_BETWEEN_RUNNING = 500;
    protected static final SystemState NEXT_STATE = SystemState.RUNNING;

    protected List<TrainClient> trainsToMap;
    protected boolean startMapping = false;
    protected CurrentTrainInfo currentTrainInfo = null;
    protected int currentTrainIndex = 0;

    protected final Database db = Database.getInstance();
    @Override
    public boolean performOperation() {
        if (startMapping) {
            return mapClients();
        } else {
            if (checkReadyToMap())
                startMapping = true;
        }
        return false;
    }

    protected abstract boolean checkReadyToMap();

    protected boolean mapClients() {
        // Double check if all clients mapped
        if (trainsToMap == null || trainsToMap.isEmpty()) {
            logger.warning("No trains to map.");
            return true;
        }

        if (currentTrainInfo == null) {
            if (currentTrainIndex < trainsToMap.size()) {
                currentTrainInfo = new CurrentTrainInfo(trainsToMap.get(currentTrainIndex));
            } else {
                logger.info("All clients have been processed. Proceeding to the next state.");
                return true;
            }
        } else if (currentTrainInfo.process(TRAIN_MAPPING_TIMEOUT)) {
            currentTrainIndex++;
            if (currentTrainIndex >= trainsToMap.size()) {
                logger.info("All clients have been remapped. Proceeding to the next state.");
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
