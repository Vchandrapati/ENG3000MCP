package org.example;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.charset.*;

/**
 * Manages network communication with clients over UDP.
 * It handles incoming packets, maintains a list of connected clients,
 * and schedules status checks.
 *
 * <p>
 * Utilises the Singleton pattern to ensure only one instance of the Server
 * exists.
 */
public class Server implements Runnable {
    private static final Database db = Database.getInstance();
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private DatagramSocket serverSocket;
    private final AtomicBoolean serverRunning;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    public static final int PORT = 6666;
    private static final int TIMEOUT = 5000;
    private static final int BUFFER_SIZE = 1024;
    private static final int STAT_INTERVAL_SECONDS = 5000;
    private static final BlockingQueue<DatagramPacket> mailbox = new LinkedBlockingQueue<>();
    private final MessageHandler messageHandler = new MessageHandler();
    private final String clientErrorReason = "disconnected";

    private Server() {
        serverRunning = new AtomicBoolean(true);
        try {
            serverSocket = new DatagramSocket(PORT);
            logger.info("Server completed startup and listening on PORT: " + PORT);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting up server", e);
        }
    }

    /**
     * Holder class for implementing the Singleton pattern.
     */
    private static class Holder {
        private static final Server INSTANCE = new Server();

        static {
            Thread serverThread = new Thread(INSTANCE, "Server-Thread");
            serverThread.start();
        }
    }

    @Override
    public void run() {
        startConnectionListener();
        startStatusScheduler();
    }

    /**
     * Returns the singleton instance of the Server.
     *
     * @return the singleton Server instance
     */
    public static Server getInstance() {
        return Holder.INSTANCE;
    }

    private void startConnectionListener() {
        executorService.execute(this::connectionListener);
        executorService.execute(this::packetProcessor);
    }

    /**
     * Listens for incoming UDP packets and processes them.
     * If a client is recognized, processes the packet with the existing client.
     * Otherwise, creates a new client instance and registers it.
     */
    private void connectionListener() {
        while (serverRunning.get()) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                serverSocket.receive(receivePacket);
                if (receivePacket.getLength() > 0)
                    mailbox.add(receivePacket);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error receiving packet", e);
            }
        }
    }

    private void packetProcessor() {
        while (serverRunning.get()) {
            try {
                DatagramPacket receivePacket = mailbox.take();
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
                messageHandler.handleMessage(message, receivePacket.getAddress(), receivePacket.getPort());
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Packet processor was interrupted", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error processing packet", e);
            }
        }

        logger.log(Level.INFO, "Packet processor terminated");
    }

    public void startStatusScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            long sendTime = System.currentTimeMillis();

            List<Client> clients = db.getClients();
            for (Client client : clients) {
                synchronized (client) {
                    if (Boolean.TRUE.equals(client.isRegistered())) {
                        client.setStatReturned(false);
                        client.sendStatusMessage(System.currentTimeMillis());
                    }
                }
            }

            try {
                Thread.sleep(TIMEOUT);
                checkForMissingResponse(clients, sendTime);
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
     * For unresponsive train clients, adds them to the unresponsive client list in
     * the database.
     *
     * @param clients the list of clients to check
     */

    
    private void checkForMissingResponse(List<Client> clients, Long sendTime) {
        for (Client client : clients) {
            synchronized (client) {
                if (Boolean.TRUE.equals(!client.lastStatReturned() && client.isRegistered()) && client.lastStatMSGSent()) {
                    logger.log(Level.WARNING, "No STAT response from {0} sent at {1}", new Object[]{client.getId(), sendTime});

                    // If a client is unresponsive
                    SystemStateManager.getInstance().addUnresponsiveClient(client.getId(), clientErrorReason);
                }
            }
        }
    }

    public void sendMessageToClient(Client client, String message, String type) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, client.getClientAddress(),
                    client.getClientPort());
            serverSocket.send(sendPacket);
            logger.log(Level.INFO, "Sent {0} to client: {1}", new Object[]{type, client.id});
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send message to client {0}", client.getId());
            logger.log(Level.SEVERE, "Exception: ", e);
        }
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
