package org.example;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class StatRequester {
    public void startStatusScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            List<Client> clients = db.getClients();
            synchronized (lock) {
                for (Client client : clients) {
                    if (Boolean.TRUE.equals(client.isRegistered())) {
                        client.setStatReturned(false);
                        client.setStatSent(true);
                        client.sendStatusMessage();
                    }
                }
            }

            try {
                Thread.sleep(TIMEOUT);
                checkForMissingResponse(clients, );
            } catch (InterruptedException e) {
                logger.log(Level.INFO, "Error waiting for stat");
                Thread.currentThread().interrupt();
            }
        }, 0, STAT_INTERVAL_SECONDS, TimeUnit.MILLISECONDS);
    }

    /**
     * Checks for clients that have not responded to a status request sent within a
     * timeframe.
     * If a client has not responded, logs an error and updates the system state to
     * EMERGENCY.
     * For unresponsive BladeRunner clients, adds them to the unresponsive client
     * list in
     * the database.
     *
     * @param clients the list of clients to check
     */

    private void checkForMissingResponse(List<Client> clients, Long sendTime) {
        synchronized (lock) {
            for (Client client : clients) {
                if (Boolean.TRUE.equals(!client.lastStatReturned() && client.isRegistered())
                        && client.lastStatMSGSent()) {
                    logger.log(Level.WARNING, "No STAT response from {0} sent at {1}",
                            new Object[] { client.getId(), sendTime });

                    // If a client is unresponsive
                    SystemStateManager.getInstance().addUnresponsiveClient(client.getId(), ReasonEnum.NOSTAT);
                }
            }
        }
    }
}
