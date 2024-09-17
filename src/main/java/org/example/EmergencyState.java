package org.example;

import java.util.List;
import java.util.logging.Logger;

public class EmergencyState implements SystemStateInterface {
    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    //Sends stop to all trains
    //TO BE COMPLETED

    private static long timeToWait = 500;
    private static SystemState nextState = SystemState.RESTARTUP;
    private static long timeout = 10000;

    private Database db = Database.getInstance();
    private boolean startedStopping = false;
    private List<TrainClient> trains = null;

    @Override
    public boolean performOperation() {
        if(startedStopping) {
            return haveAllStopped();
        }
        else {
            startedStopping = true;
            List<TrainClient> trains = db.getTrains();
        }
        return false;
    }

    private boolean haveAllStopped() {
        //TODO
        return false;
    }

    @Override
    public long getTimeToWait() {
        return timeToWait;
    }

    @Override
    public SystemState getNextState() {
        return nextState;
    }

    @Override
    public void reset() {
        return;
    }
        
}
