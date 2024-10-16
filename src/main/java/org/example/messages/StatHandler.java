package org.example.messages;

import org.example.Database;
import org.example.Processor;
import org.example.client.AbstractClient;
import org.example.client.ReasonEnum;
import org.example.state.SystemState;
import org.example.state.SystemStateManager;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatHandler {
    private long STAT_INTERVAL_SECONDS;
    private Object lock;
    private ScheduledExecutorService scheduler;
    private Database db;
    private SystemStateManager systemStateManager;
    private Logger logger;

    public StatHandler() {
        STAT_INTERVAL_SECONDS = 2000; // Set time later
        lock = new Object();
        scheduler = Executors.newScheduledThreadPool(1);
        db = Database.getInstance();
        systemStateManager = SystemStateManager.getInstance();
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    // send stats at specified intervals
    public void startStatusScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            List<AbstractClient> clients = db.getClients();
            synchronized (lock) {
                for (AbstractClient client : clients) {
                    client.sendStatusMessage();
                    client.nowExpectingStat();
                    checkIfClientIsUnresponsive(client);
                }
            }
        }, 0, STAT_INTERVAL_SECONDS, TimeUnit.MILLISECONDS);
    }

    private void checkIfClientIsUnresponsive(AbstractClient client) {
        if (client.checkResponsive()) {
            systemStateManager.addUnresponsiveClient(client.getId(), ReasonEnum.NOSTAT);
        }
    }

    public <S extends Enum<S>, A extends Enum<A> & MessageEnums.ActionToStatus<S>> void handleStatMessage(
            AbstractClient<S, A> client, ReceiveMessage receiveMessage) {


        A lastAction = client.getLastActionSent();
        S expectedStatus = null;
        MessageEnums.CCPStatus alternateStatus = null;
        boolean altPath = false;

        if (lastAction != null) {
            expectedStatus = lastAction.getStatus();

            if (receiveMessage.clientType.equals("CCP")
                    && (lastAction.equals(MessageEnums.CCPAction.FSLOWC)
                            || lastAction.equals(MessageEnums.CCPAction.RSLOWC))) {
                alternateStatus = MessageEnums.CCPStatus.STOPC;
            }
        }

        try {
            S recievedStatus =
                    Enum.valueOf(client.getStatus().getDeclaringClass(), receiveMessage.status);

            if (!client.isExpectingStat()) {
                client.sendAcknowledgeMessage(MessageEnums.AKType.AKST);
            }

            // If client reports ERR
            if (recievedStatus.toString().equals("ERR")) {
                systemStateManager.addUnresponsiveClient(client.getId(), ReasonEnum.CLIENTERR);
                altPath = true;
            }

            // For specifically FSLOWC and RSLOWC case
            if (alternateStatus != null && recievedStatus.equals(alternateStatus)
                    && systemStateManager.getState().equals(SystemState.RUNNING)) {
                // Ashton should get his STOPC
                Processor.bladeRunnerStopped(receiveMessage.clientID);

                altPath = true;
            }

            String clientLastExec = "";
            if(client.getLastActionSent() != null){
                clientLastExec = client.getLastActionSent().toString();
            }

            // For DOOR stat response when no response needed
            if (!altPath
                    && (receiveMessage.status.equals("ONOPEN") && clientLastExec.equals("OPEN"))
                    && (receiveMessage.status.equals("ON") && clientLastExec.equals("CLOSE"))) {
                altPath = true;
            }

            if (!altPath && expectedStatus != null && !expectedStatus.equals(recievedStatus)) {
                // If client is not in expected state then there is a problem
                systemStateManager.addUnresponsiveClient(client.getId(), ReasonEnum.WRONGSTATUS);
                logger.log(Level.SEVERE, "Client {0} did not update status to {1} from {2}",
                        new Object[] {client.getId(), expectedStatus, receiveMessage.status});
            }

            // If the current stat message sequence number is the highest then the stats
            // missed should = 0
            if (client.getLatestStatusMessageCount() < receiveMessage.sequenceNumber) {
                client.updateLatestStatusMessageCount(receiveMessage.sequenceNumber);
                client.resetMissedStats();
            }


            client.noLongerExpectingStat();
            client.updateStatus(recievedStatus);
        } catch (IllegalArgumentException e) {
            // Handle case where the status in receiveMessage is invalid
            logger.log(Level.SEVERE, "Invalid status: received {0} for client {1}",
                    new Object[] {receiveMessage.status, client.getId()});
        }

        logger.log(Level.INFO, "Received STAT message from Client: {0}", receiveMessage.clientID);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
