package org.example;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.*;

public class EmergencyState implements SystemStateInterface {

    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected static final SystemState NEXT_STATE = SystemState.MAPPING;

    // All time units in milliseconds
    private static final long EMERGENCY_TIMEOUT = 600000; // 10 minutes
    private static final long TIME_BETWEEN_RUNNING = 1000;
    private final Database db = Database.getInstance();

    private static BlockingQueue<String> clientMessageQueue = new LinkedBlockingQueue<>();

    private List<TrainClient> trains = null;

    //the time when counter started
    private final long timeOnStart = System.currentTimeMillis();

    @Override
    public boolean performOperation() {
        //if it has been EMERGENCY_TIMEOUT minutes within emergency state, the client/s has failed to reconnect in time and proceed to next state
        if(System.currentTimeMillis() - timeOnStart >= EMERGENCY_TIMEOUT) {
            return true;
        }
        else {
            trains = db.getTrainClients();
            stopAllTrains();
            return checkIfAllReconnected();
        }
    }

    //goes through the stat message queue, if the string is of a dead client, that client has reconnected, remove from the dead list
    //if that client is also a train add to restartup list
    //if the queue is empty and the dead list is empty, go to next state
    private boolean checkIfAllReconnected() {
        while(!clientMessageQueue.isEmpty()) {
            String clientID = clientMessageQueue.poll();
            if(db.isClientUnresponsive(clientID)) {
                logger.log(Level.INFO, "Client has {0} has reconnected", clientID);
                if(clientID.contains("BR")) {
                    db.addClientToReconnecting(clientID);
                }
                db.removeClientFromUnresponsive(clientID);
            }
        }
        return db.isUnresponsiveEmpty();
    }

    public static void addMessage(String id) {
        clientMessageQueue.add(id);
    }

    //tells each train to stop at next station
    private void stopAllTrains() {
        for (TrainClient trainClient : trains) {
            trainClient.sendExecuteMessage(SpeedEnum.STOP);
        }
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
        trains = null;
        SystemStateManager.getInstance().resetERROR();
    }
}
