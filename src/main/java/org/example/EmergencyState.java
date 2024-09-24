package org.example;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.*;

public class EmergencyState implements SystemStateInterface {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // All time units in milliseconds
    private static final long EMERGENCY_TIMEOUT = 600000; // 10 minutes
    private static final long TIME_BETWEEN_RUNNING = 1000;
    private static final long TIME_BETWEEN_SENDING_STOP = 5000; //5 seconds

    private static final SystemState NEXT_STATE = SystemState.MAPPING;

    //the time when counter started
    private final long timeOnStart;
    private long timeOnStop;
    private final Database db = Database.getInstance();
    private static final BlockingQueue<String> clientMessageQueue = new LinkedBlockingQueue<>();
    private List<TrainClient> trains;

    public EmergencyState() {
        timeOnStart = System.currentTimeMillis();
        timeOnStop = TIME_BETWEEN_SENDING_STOP;
        trains = null;
    }

    @Override
    public boolean performOperation() {
        //if it has been EMERGENCY_TIMEOUT minutes within emergency state, the client/s has failed to reconnect in time and proceed to next state
        if(System.currentTimeMillis() - timeOnStart >= EMERGENCY_TIMEOUT) {
            return true;
        } else {
            trains = db.getTrainClients();
            stopAllTrains();
            return checkIfAllReconnected();
        }
    }

    //goes through the stat message queue, if the string is of a dead client, that client has reconnected, remove from the dead list
    //if that client is also a train add to restartup list
    //if the queue is empty and the dead list is empty, go to next state
    private boolean checkIfAllReconnected() {
        while (!clientMessageQueue.isEmpty()) {
            String clientID = clientMessageQueue.poll();
            if (db.isClientUnresponsive(clientID)) {
                logger.log(Level.INFO, "Client has {0} has reconnected", clientID);
                db.removeClientFromUnresponsive(clientID);
            }
        }

        boolean result = db.isUnresponsiveEmpty();
        if(result) logger.log(Level.INFO, "All clients have reconnected moving to state {0}", NEXT_STATE);
        return result;
    }

    public static void addMessage(String id) {
        clientMessageQueue.add(id);
    }

    //tells each train to stop every 5 seconds
    private void stopAllTrains() {
        if(System.currentTimeMillis() - timeOnStop >= TIME_BETWEEN_SENDING_STOP || timeOnStop == 5000) {
            timeOnStop = System.currentTimeMillis();
            for (TrainClient trainClient : trains) {
                trainClient.sendExecuteMessage(SpeedEnum.STOP);
            }
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

    public long getStateTimeout() {
        return System.currentTimeMillis() - timeOnStart;
    }
}
