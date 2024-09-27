package org.example;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmergencyState implements SystemStateInterface {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // All time units in milliseconds
    private static final long EMERGENCY_TIMEOUT = 600000; // 10 minutes
    private static final long TIME_BETWEEN_RUNNING = 1000;
    private static final long TIME_BETWEEN_SENDING_STOP = 5000; //5 seconds
    private static final long MINIMUM_EMERGENCY_TIME = 5000; //5 seconds

    private static final SystemState NEXT_STATE = SystemState.MAPPING;
    private static final BlockingQueue<String> clientMessageQueue = new LinkedBlockingQueue<>();

    //the time when counter started
    private final long timeOnStart;
    private final Database db = Database.getInstance();
    private long timeOnStop;
    private List<BladeRunnerClient> bladeRunners;

    public EmergencyState() {
        timeOnStart = System.currentTimeMillis();
        timeOnStop = TIME_BETWEEN_SENDING_STOP;
        bladeRunners = null;
    }

    public static void addMessage(String id) {
        clientMessageQueue.add(id);
    }

    @Override
    public boolean performOperation() {
        //if it has been EMERGENCY_TIMEOUT minutes within emergency state, the client/s has failed to reconnect in time and proceed to next state
        if (System.currentTimeMillis() - timeOnStart >= EMERGENCY_TIMEOUT) {
            return true;
        } else {
            bladeRunners = db.getBladeRunnerClients();
            stopAllBladeRunners();
            return checkIfAllReconnected();
        }
    }

    //goes through the stat message queue, if the string is of a dead client, that client has reconnected, remove from the dead list
    //if that client is also a BladeRunner add to restartup list
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

        //Ensures a minimum time in emergency mode
        if(System.currentTimeMillis() - timeOnStart >= MINIMUM_EMERGENCY_TIME) {
            if (result) logger.log(Level.INFO, "All clients have reconnected");
            return result;
        }
        else {
            return false;
        }
    }

    //tells each BladeRunner to stop every 5 seconds
    private void stopAllBladeRunners() {
        if (System.currentTimeMillis() - timeOnStop >= TIME_BETWEEN_SENDING_STOP || timeOnStop == 5000) {
            timeOnStop = System.currentTimeMillis();
            for (BladeRunnerClient BladeRunnerClient : bladeRunners) {
                BladeRunnerClient.sendExecuteMessage(SpeedEnum.STOP);
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
