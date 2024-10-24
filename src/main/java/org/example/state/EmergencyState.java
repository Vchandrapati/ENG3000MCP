package org.example.state;

import org.example.Database;
import org.example.client.AbstractClient;
import org.example.client.BladeRunnerClient;
import org.example.client.ReasonEnum;
import org.example.messages.MessageEnums;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmergencyState implements SystemStateInterface {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    // All time units in milliseconds
    private static final long EMERGENCY_TIMEOUT = 300000; // 5 minutes
    private static final long TIME_BETWEEN_RUNNING = 1000;
    private static final long TIME_BETWEEN_SENDING_STOP = 5000; // 5 seconds
    private static final long MINIMUM_EMERGENCY_TIME = 5000; // 5 seconds

    private static final SystemState NEXT_STATE = SystemState.MAPPING;
    private static final Database db = Database.getInstance();
    // the time when counter started
    private long timeOnStart;
    private long timeOnStop;
    private boolean disconnection = false;
    private boolean disconnectOnce = false;

    public EmergencyState () {
        timeOnStart = System.currentTimeMillis();
        timeOnStop = 0;
    }


    @Override
    public boolean performOperation () {
        // if it has been EMERGENCY_TIMEOUT minutes within emergency state, the client/s has failed
        // to reconnect in time and proceed to next state

        stopAllBladeRunners();
        if (disconnection || System.currentTimeMillis() - timeOnStart >= EMERGENCY_TIMEOUT) {
            if (!disconnectOnce) {
                disconnection = true;
                disconnectOnce = true;
                timeOnStart = System.currentTimeMillis();
                disconnectClients();
            } else
                return System.currentTimeMillis() - timeOnStart >= EMERGENCY_TIMEOUT;
            return false;
        } else {
            return checkIfAllAreFixed();
        }
    }

    private void disconnectClients () {
        List<String> clients = new ArrayList<>(db.getAllUnresponsiveClientIDs());
        for (int i = clients.size() - 1; i > -1; i--) {
            Optional<BladeRunnerClient> clientInstance = db.getClient(clients.get(i), BladeRunnerClient.class);
            if (clientInstance.isPresent()) {
                clientInstance.get().sendExecuteMessage(MessageEnums.CCPAction.DISCONNECT);
                db.fullPurge(clients.get(i));
            }
        }
    }

    // goes through the stat message queue, if the string is of a dead client, that client has
    // reconnected, remove from the dead list
    // if that client is also a BladeRunner add to restartup list
    // if the queue is empty and the dead list is empty, go to next state
    private boolean checkIfAllAreFixed () {
        processEachUnresponsiveClient();

        boolean result = db.isUnresponsiveEmpty();

        // Ensures a minimum time in emergency mode
        if (System.currentTimeMillis() - timeOnStart >= MINIMUM_EMERGENCY_TIME) {
            if (result) {
                logger.log(Level.INFO, "All malfunctioning clients now resolved");
            }
            return result;
        } else {
            return false;
        }
    }

    @SuppressWarnings({"rawTypes", "unchecked", "rawtypes"})
    private void processEachUnresponsiveClient () {
        List<String> clients = new ArrayList<>(db.getAllUnresponsiveClientIDs());

        for (int i = clients.size() - 1; i > -1; i--) {
            String client = clients.get(i);
            dealWithReasons(client, new ArrayList<>(db.getClientReasons(client)));
        }
    }

    private void dealWithReasons (String client, List<ReasonEnum> reasons) {
        for (int i = reasons.size() - 1; i > -1; i--) {
            ReasonEnum reason = reasons.get(i);
            dealClientReason(client, reason);
        }
    }

    @SuppressWarnings("rawtypes")
    private void dealClientReason (String client, ReasonEnum reason) {
        Optional<AbstractClient> clientInstance = db.getClient(client,
                AbstractClient.class);

        if (clientInstance.isEmpty()) return;

        switch (reason) {
            case ReasonEnum.INVALCONNECT, NOSTAT: {
                if (!clientInstance.get().checkResponsive()) {
                    logger.log(Level.INFO, "Has fixed issue {0} for client : {1}",
                            new Object[] {reason, client});
                    db.removeReason(client, reason);
                }
                break;
            }
            case ReasonEnum.CLIENTERR: {
                if (clientInstance.get().getLastActionSent() != MessageEnums.CCPStatus.ERR) {
                    logger.log(Level.INFO, "Has fixed issue {0} for client : {1}",
                            new Object[] {reason, client});
                    db.removeReason(client, reason);
                }
                break;
            }
            case ReasonEnum.COLLISION: {
                BladeRunnerClient br = (BladeRunnerClient) clientInstance.get();
                br.setCollision(true);

                logger.log(Level.INFO,
                        "Has recognised issue {0} for client : {1}, will be fixed in mapping",
                        new Object[] {reason, client});
                db.removeReason(client, reason);
                break;
            }
            case ReasonEnum.INCORTRIP, WRONGMESSAGE, MAPTIMEOUT: {
                // don't really know what to do with this one, same as timeout for now
                break;
            }
            case ReasonEnum.TODISCONNECT: {
                disconnection = true;
                break;
            }
            default: {
                logger.log(Level.INFO, "Invalid reason {0} for client : {1}",
                        new Object[] {reason, client});
                break;
            }
        }

    }

    // Grabs all the trains each time and tells each
    // performs this every 5 seconds, and instantly when going into emergency mode
    // grabs all the trains each time as new trains could connect unexpectedly
    private void stopAllBladeRunners () {
        if (System.currentTimeMillis() - timeOnStop >= TIME_BETWEEN_SENDING_STOP
                || timeOnStop == 0) {
            List<BladeRunnerClient> bladeRunners = db.getBladeRunnerClients();
            timeOnStop = System.currentTimeMillis();
            for (org.example.client.BladeRunnerClient BladeRunnerClient : bladeRunners) {
                BladeRunnerClient.sendExecuteMessage(MessageEnums.CCPAction.STOPC);
            }
        }
    }

    @Override
    public long getTimeToWait () {
        return TIME_BETWEEN_RUNNING;
    }

    @Override
    public SystemState getNextState () {
        return NEXT_STATE;
    }
}
