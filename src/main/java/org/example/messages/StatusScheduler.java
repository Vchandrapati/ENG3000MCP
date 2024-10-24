package org.example.messages;

import org.example.Database;
import org.example.client.AbstractClient;
import org.example.client.ReasonEnum;
import org.example.events.ClientErrorEvent;
import org.example.events.EventBus;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatusScheduler {
    private static final long STAT_INTERVAL_SECONDS = 2;
    private final ScheduledExecutorService scheduler;
    private final Database db = Database.getInstance();
    private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final EventBus eventBus;

    public StatusScheduler (EventBus eventBus) {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.eventBus = eventBus;

        logger.log(Level.INFO, "StatusScheduler started");
    }

    // send stats at specified intervals
    @SuppressWarnings("rawtypes")
    public void startStatusScheduler () {
        scheduler.scheduleAtFixedRate(() -> {
            List<AbstractClient> clients = db.getClients();
            for (AbstractClient client : clients) {
                client.sendStatusMessage();
                client.nowExpectingStat();
                checkIfClientIsUnresponsive(client);
            }
        }, 0, STAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @SuppressWarnings("rawtypes")
    private void checkIfClientIsUnresponsive (AbstractClient client) {
        if (client.checkResponsive()) { // Assuming checkResponsive returns false if unresponsive
            eventBus.publish(new ClientErrorEvent(client.getId(), ReasonEnum.NOSTAT));
            logger.warning("Client " + client.getId() + " marked as unresponsive due to no stat.");
        }
    }

    public void shutdown () {
        scheduler.shutdown();
    }
}
