package org.example.messages;

import org.example.client.ReasonEnum;
import org.example.state.SystemStateManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
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
    public static final int PORT = 2000;

    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final int BUFFER_SIZE = 1024;

    private static final BlockingQueue<DatagramPacket> mailbox = new LinkedBlockingQueue<>();
    private final AtomicBoolean serverRunning;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final MessageHandler messageHandler = new MessageHandler();
    private DatagramSocket serverSocket;


    //TODO

    public Server() {
        serverRunning = new AtomicBoolean(true);
        try {
            serverSocket = new DatagramSocket(PORT);
            logger.info("Server completed startup and listening on PORT: " + PORT);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting up server", e);
        }
    }

    /**
     * Returns the singleton instance of the Server.
     *
     * @return the singleton Server instance
     */
    public static Server getInstance() {
        return Holder.INSTANCE;
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
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength(),
                        StandardCharsets.UTF_8);

                messageHandler.handleMessage(message, receivePacket.getAddress(),
                        receivePacket.getPort());
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Packet processor was interrupted", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error processing packet", e);
            }
        }
        logger.log(Level.INFO, "Packet processor terminated");
    }

    public void sendMessageToClient(InetAddress address, int port, String message, String type,
                                    String clientID) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length,
                    address, port);
            serverSocket.send(sendPacket);
            logger.log(Level.INFO, "Sent {0} to client at: {1}", new Object[] {type, clientID});
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send message to client: {0}", clientID);
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
}
