package org.example;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.example.MessageEnums.CCPAction;
import org.example.MessageEnums.CCPStatus;

public class StatHandler {
    private static final long STAT_INTERVAL_SECONDS = 2000; // Set time later
    private final Object lock = new Object();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Database db = Database.getInstance();
    private static final SystemStateManager systemStateManager = SystemStateManager.getInstance();
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static StatHandler getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final StatHandler INSTANCE = new StatHandler();
    }

    // send stats at specified intervals
    public void startStatusScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            List<Client> clients = db.getClients();
            synchronized (lock) {
                for (Client client : clients) {
                    if (Boolean.TRUE.equals(client.isRegistered())) {
                        client.sendStatusMessage();
                        checkIfClientIsUnresponsive(client);
                    }
                }
            }
        }, 0, STAT_INTERVAL_SECONDS, TimeUnit.MILLISECONDS);
    }

    private void checkIfClientIsUnresponsive(Client client) {
        if (client.checkResponsive()) {
            SystemStateManager.getInstance().addUnresponsiveClient(client.getId(),
                    ReasonEnum.NOSTAT);
        }
    }

    public <S extends Enum<S>, A extends Enum<A> & MessageEnums.ActionToStatus<S>> void handleStatMessage(
            Client<S, A> client, ReceiveMessage receiveMessage) {

        A lastAction = client.getLastActionSent();
        S expectedStatus = null;
        CCPStatus alternateStatus = null;
        Boolean altPath = false;

        if (lastAction != null) {
            expectedStatus = lastAction.getStatus();

            if (receiveMessage.clientType.equals("CCP")) {
                if (lastAction.equals(MessageEnums.CCPAction.FSLOWC)
                        || lastAction.equals(MessageEnums.CCPAction.RSLOWC)) {
                    alternateStatus = MessageEnums.CCPStatus.STOPC;
                }
            }
        }


        try {
            S recievedStatus =
                    Enum.valueOf(client.currentStatus.getDeclaringClass(), receiveMessage.status);

            // If client reports ERR
            if (recievedStatus.toString().equals("ERR")) {
                systemStateManager.addUnresponsiveClient(client.getId(), ReasonEnum.CLIENTERR);
            }

            if (!(alternateStatus == null) && recievedStatus.equals(alternateStatus)) {
                // Ashton should get his STOPC
                 Processor.bladeRunnerStopped(receiveMessage.clientID);

                logger.log(Level.FINEST, "Got it", "null");
            }

            if (altPath && expectedStatus != null && !expectedStatus.equals(recievedStatus)) {
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
