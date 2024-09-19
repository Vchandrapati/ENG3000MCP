package org.example;

import java.util.List;
import java.util.logging.Logger;
import java.util.concurrent.*;

public class EmergencyState implements SystemStateInterface {

    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // All time units in milliseconds
    private static final long EMERGENCY_TIMEOUT = 300000; // 5 minutes
    private static final long TIME_BETWEEN_RUNNING = 1000;
    private final Database db = Database.getInstance();

    private static BlockingQueue<String> clientMessageQueue = new LinkedBlockingQueue<>();

    private boolean startedStopping = false;
    private List<TrainClient> trains = null;

    //the time when counter started
    private final long timeOnStart = System.currentTimeMillis();

    @Override
    public boolean performOperation() {
        //if it has been 5 minutes within emergency state, the client/s has failed to reconnect in time and proceed to next state
        if(System.currentTimeMillis() - timeOnStart >= EMERGENCY_TIMEOUT) {
            return true;
        }
        else if(startedStopping) {
            return checkIfAllReconnected();
        }
        else {
            trains = db.getTrainClients();
            if(trains != null && !trains.isEmpty()) {
                startedStopping = true;
                stopAllTrains();
            }
            else {
                logger.warning("Finding no trains in database");
            }
        }
        return false;
    }

    //goes through the stat message queue, if the string is of a dead client, that client has reconnected, remove from the dead list
    //if that client is also a train add to restartup list
    //if the queue is empty and the dead list is empty, go to next state
    private boolean checkIfAllReconnected() {
        while(!clientMessageQueue.isEmpty()) {
            String client = clientMessageQueue.poll();
            if(db.isClientUnresponsive(client)) {
                if(client.contains("BR")) db.addClientToReconnecting(client);
                db.removeClientFromUnresponsive(client);
            }
        }
        boolean temp = db.isUnresponsiveEmpty();
        return temp;
    }

    public static void addMessage(String id) {
        clientMessageQueue.add(id);
    }

    //tells each train to stop at next station
    private void stopAllTrains() {
        for (TrainClient trainClient : trains) {
            trainClient.sendExecuteMessage(SpeedEnum.STOPNEXTSTATION);
        }
    }

    @Override
    public long getTimeToWait() {
        return TIME_BETWEEN_RUNNING;
    }

    @Override
    public SystemState getNextState() {
        if(SystemStateManager.getInstance().hasCompletedStartup()) return SystemState.RESTARTUP;
        return SystemState.STARTUP;
    }

    @Override
    public void reset() {
        startedStopping = false;
        trains = null;
        SystemStateManager.getInstance().resetERROR();
    }
}
