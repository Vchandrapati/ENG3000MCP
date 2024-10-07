package org.example;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class StatRequester {
    private static final long STAT_INTERVAL_SECONDS = 2000; // Set time later
    private final Object lock = new Object();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Database db = Database.getInstance();

    private StatRequester() {

    }

    public static StatRequester getInstance() {
        return Holder.INSTANCE;
    }

    // send stats at specified intervals
    public void startStatusScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            List<Client> clients = db.getClients();
            synchronized (lock) {
                for (Client client : clients) {
                    if (Boolean.TRUE.equals(client.isRegistered())) {
                        client.sendStatusMessage(client.getId());
                        checkIfClientIsUnresponsive(client);
                    }
                }
            }
        }, (long) 0, STAT_INTERVAL_SECONDS, TimeUnit.MILLISECONDS);
    }

    private void checkIfClientIsUnresponsive(Client client) {
        client.incrementMissedStats();
        if (client.isUnresponsive()) {
            SystemStateManager.getInstance().addUnresponsiveClient(client.getId(),
                    ReasonEnum.NOSTAT);
        }

    }

    public void shutdown() {
        scheduler.close();
    }

    private static class Holder {
        private static final StatRequester INSTANCE = new StatRequester();
    }
}
