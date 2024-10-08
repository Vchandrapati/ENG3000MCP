package org.example;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;

public class EmergencyState implements SystemStateInterface {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // All time units in milliseconds
    private static final long EMERGENCY_TIMEOUT = 600000; // 10 minutes
    private static final long EMERGENCY_TIMEOUT_AFTER_WAIT = 300000; // 5 minutes
    private static final long TIME_BETWEEN_RUNNING = 1000;
    private static final long TIME_BETWEEN_SENDING_STOP = 5000; // 5 seconds
    private static final long MINIMUM_EMERGENCY_TIME = 5000; // 5 seconds

    private static final SystemState NEXT_STATE = SystemState.MAPPING;

    // the time when counter started
    private final long timeOnStart;
    private final Database db = Database.getInstance();
    private long timeOnStop;
    private boolean afterTimeout;

    // Emergency mode can wait forever until client is fixed or
    // be given a ten minute timeout
    private static final boolean WAIT_FOREVER = false;

    public EmergencyState() {
        timeOnStart = System.currentTimeMillis();
        timeOnStop = 0;
        afterTimeout = false;
    }

    @Override
    public boolean performOperation() {
        // if it has been EMERGENCY_TIMEOUT minutes within emergency state, the client/s has failed
        // to reconnect in time and proceed to next state

        // continues to wait forever until client is fixed
        if (WAIT_FOREVER) {
            stopAllBladeRunners();
            return checkIfAllAreFixed();
        } else {
            // continues to wait for 10 minutes until client is fixed
            // if not just proceeds as normal as if the problem did not occur
            if (afterTimeout || System.currentTimeMillis() - timeOnStart >= EMERGENCY_TIMEOUT) {
                if (!afterTimeout) {
                    afterTimeout = true;

                    List<String> clients = new ArrayList<>(db.getAllUnresponsiveClientIDs());
                    for (int i = clients.size() - 1; i > -1; i--) {
                        BladeRunnerClient clientInstance = db.getClient(clients.get(i), BladeRunnerClient.class).get();
                        if(clientInstance != null) {
                            clientInstance.sendExecuteMessage(MessageEnums.CCPAction.DISCONNECT);
                            db.purge(clients.get(i));
                        }
                    }
                }
                else {

                }
            } else {
                stopAllBladeRunners();
                return checkIfAllAreFixed();
            }
        }
    }

    // goes through the stat message queue, if the string is of a dead client, that client has
    // reconnected, remove from the dead list
    // if that client is also a BladeRunner add to restartup list
    // if the queue is empty and the dead list is empty, go to next state
    private boolean checkIfAllAreFixed() {
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

    private void processEachUnresponsiveClient() {
        List<String> clients = new ArrayList<>(db.getAllUnresponsiveClientIDs());

        for (int i = clients.size() - 1; i > -1; i--) {
            String client = clients.get(i);
            dealWithReasons(client, new ArrayList<>(db.getClientReasons(client)));
        }
    }

    private void dealWithReasons(String client, List<ReasonEnum> reasons) {
        for (int i = reasons.size() - 1; i > -1; i--) {
            ReasonEnum reason = reasons.get(i);
            // if client is null then SYSTEM was given
            if (client == null) {
                dealSystemReason(client, reason);
            } else {
                dealClientReason(client, reason);
            }
        }
    }

    private void dealSystemReason(String client, ReasonEnum reason) {
        switch (reason) {
            case ReasonEnum.SYSTEMOVER: {
                db.removeReason(client, reason);
                break;
            }
            case ReasonEnum.INTERNAL: {
                db.removeReason(client, reason);
                break;
            }
            default: {
                logger.log(Level.INFO, "Invalid reason {0} for : {1}",
                        new Object[] {reason, client});
                break;
            }
        }
    }

    private void dealClientReason(String client, ReasonEnum reason) {
        switch (reason) {
            case ReasonEnum.INVALCONNECT: // same as no stat for now
            case ReasonEnum.NOSTAT: {
                Client<?, ?> clientInstance = db.getClient(client, Client.class).get();
                if (!clientInstance.checkResponsive()) {
                    logger.log(Level.INFO, "Has fixed issue {0} for client : {1}",
                            new Object[] {reason, client});
                    db.removeReason(client, reason);
                }
                break;
            }
            case ReasonEnum.CLIENTERR: {
                Client<?, ?> clientInstance = db.getClient(client, Client.class).get();
                if (clientInstance.getLastActionSent() != MessageEnums.CCPStatus.ERR) {
                    db.removeReason(client, reason);
                }
                break;
            }
            case ReasonEnum.COLLISION: {
                // set collision boolean to true for mapping
                BladeRunnerClient clientInstance =
                        db.getClient(client, BladeRunnerClient.class).get();
                clientInstance.collision(true, new Object());
                logger.log(Level.INFO,
                        "Has recognised issue {0} for client : {1}, will be fixed in mapping",
                        new Object[] {reason, client});
                db.removeReason(client, reason);
                break;
            }
            case ReasonEnum.INCORTRIP: {
                // dont really know what to do with this one, same as timeout for now
                break;
            }
            case ReasonEnum.WRONGMESSAGE: {
                // dont really know what to do with this one, same as timeout for now
                break;
            }
            case ReasonEnum.MAPTIMEOUT: {
                // wait for human override
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
    private void stopAllBladeRunners() {
        if (System.currentTimeMillis() - timeOnStop >= TIME_BETWEEN_SENDING_STOP
                || timeOnStop == 0) {
            List<BladeRunnerClient> bladeRunners = db.getBladeRunnerClients();
            timeOnStop = System.currentTimeMillis();
            for (BladeRunnerClient BladeRunnerClient : bladeRunners) {
                BladeRunnerClient.sendExecuteMessage(MessageEnums.CCPAction.FSLOWC);
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
