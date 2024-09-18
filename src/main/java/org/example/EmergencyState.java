package org.example;

import java.util.List;
import java.util.logging.Logger;
import java.util.concurrent.*;

public class EmergencyState implements SystemStateInterface {

    protected static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // All time units in milliseconds
    protected static final long EMERGENCY_TIMEOUT = 300000; // 5 minutes
    protected static final long TIME_BETWEEN_RUNNING = 1000; 

    protected final Database db = Database.getInstance();

    private static BlockingQueue<String> clientMessageQueue = new LinkedBlockingQueue<>();

    protected static boolean startedStopping = false;
    protected static List<TrainClient> trains = null;

    @Override
    public boolean performOperation() {
        if(startedStopping) {
            return checkIfAllReconnected();
        }
        else {
            List<TrainClient> trains = db.getTrainClients();
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

    //goes through the stat message queue, if a dead client blahahdhad
    private boolean checkIfAllReconnected() {
        while(!clientMessageQueue.isEmpty()) {
            String client = clientMessageQueue.poll();
            if(db.isClientUnresponsive(client)) {
                if(client.contains("BR")) db.addClientToReconnecting(client);
                db.removeClientFromUnresponsive(client);
            }
        }
        if(db.isUnresponsiveEmpty()) return true;
        return false;
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
    }
}
