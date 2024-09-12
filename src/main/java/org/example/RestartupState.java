package org.example;

import java.util.List;

public class RestartupState implements SystemStateInterface {
    //This state occurs after the emergency state has happened, assumes all current WORKING trains are stationary
    //This state only occurs when a client has not responded to their status message after 2 seconds
    //Retries connecting with the lost client/s

    //Milliseconds
    private static long trainRetartupTimeout = 15000; // 15 seconds
    private static long timeBetweenRunning = 500;

    private static SystemState nextState = SystemState.RUNNING;
    private List<TrainClient> unresponseClients;

    private boolean hasStarted = false;

    private CurrentTrainInfo currentTrainInfo = null;
    private int currentTrain = 0;

    public boolean performOperation() {
        if(hasStarted) return startMapping();
        else {
            try {
                unresponseClients = db.getUnresposiveClient().get();
                if(unresponseClients != null && unresponseClients.size() > 0) hasStarted = true;
            } catch (Exception e) {
                logger.warning("Failed to grab unresponsive trains from database");
            }
        }
        return false;
    }

   // maps the current train
   private boolean startMapping() {
    if (currentTrainInfo == null) {
        currentTrainInfo = new CurrentTrainInfo(unresponseClients.get(currentTrain));
    } else if (currentTrainInfo.process(trainRetartupTimeout)) {
        currentTrain++;
        if (currentTrain > unresponseClients.size() - 1) {
            logger.info("All trains mapped proceeding to running state! ");
            return true;
        }
        currentTrainInfo = new CurrentTrainInfo(unresponseClients.get(currentTrain));
    }
    return false;
}

    public long getTimeToWait() {
        return timeBetweenRunning;
    }

    public SystemState getNextState() {
        return nextState;
    }

    @Override
    public void reset() {
        return;
    }
}
