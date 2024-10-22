package org.example.messages;

import org.example.events.EventBus;
import org.example.events.PacketEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages network communication with clients over UDP. It handles incoming packets, maintains a
 * list of connected clients, and schedules status checks.
 *
 * <p>
 * Utilises the Singleton pattern to ensure only one instance of the Server exists.
 */
public class Server implements Runnable {
    public static final int PORT = 3001;
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final int BUFFER_SIZE = 1024;
    private static volatile Server instance = null;

    private static final BlockingQueue<DatagramPacket> mailbox = new LinkedBlockingQueue<>();
    private final AtomicBoolean serverRunning;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private DatagramSocket serverSocket;
    private final EventBus eventBus;

    private Server(EventBus eventBus) {
        serverRunning = new AtomicBoolean(true);
        this.eventBus = eventBus;

        try {
            serverSocket = new DatagramSocket(PORT);
            logger.info("Server completed startup and listening on PORT: " + PORT);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting up server", e);
        }
    }

    public static Server getInstance(EventBus eventBus) {
        if (instance == null) {
            synchronized (Server.class) {
                if (instance == null) {
                    instance = new Server(eventBus);
                    Thread serverThread = new Thread(instance, "Server-Thread");
                    serverThread.start();
                }
            }
        }

        return instance;
    }

    @Override
    public void run() {
        startConnectionListener();
    }

    private void startConnectionListener() {
        executorService.execute(this::connectionListener);
        executorService.execute(this::packetProcessor);
    }

    /**
     * Listens for incoming UDP packets and processes them. If a client is recognized, processes the
     * packet with the existing client. Otherwise, creates a new client instance and registers it.
     */
    private void connectionListener() {
        while (serverRunning.get()) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                serverSocket.receive(receivePacket);

                if (receivePacket.getLength() > 0) mailbox.add(receivePacket);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error receiving packet", e);
            }
        }
    }

    private void packetProcessor() {
        while (serverRunning.get()) {
            try {
                DatagramPacket receivePacket = mailbox.take();
                eventBus.publish(new PacketEvent(receivePacket));
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Packet processor was interrupted", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error processing packet", e);
            }
        }
        logger.log(Level.INFO, "Packet processor terminated");
    }

    // Closes the active threads safely
    public void shutdown() {
        try {
            if (serverSocket != null) {
                serverRunning.set(false);
                serverSocket.close();
                scheduler.shutdownNow();
                executorService.shutdownNow();

                logger.log(Level.INFO, "Server shutdown complete");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error shutting down server", e);
        }
    }
}
