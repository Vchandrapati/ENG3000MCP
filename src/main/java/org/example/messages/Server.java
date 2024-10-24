package org.example.messages;

import org.example.events.EventBus;
import org.example.events.PacketEvent;
import org.example.events.SendPacketEvent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
public class Server {
    public static final int PORT = 2000;
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final int BUFFER_SIZE = 1024;
    private static final Object lock = new Object();
    private static final BlockingQueue<DatagramPacket> mailbox = new LinkedBlockingQueue<>();
    private static final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private static volatile Server instance = null;
    private final AtomicBoolean serverRunning;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final EventBus eventBus;
    private DatagramSocket serverSocket;

    private Server (EventBus eventBus) {
        serverRunning = new AtomicBoolean(true);
        this.eventBus = eventBus;

        eventBus.subscribe(SendPacketEvent.class, this::send);

        try {
            serverSocket = new DatagramSocket(PORT);
            logger.info("Server completed startup and listening on PORT: " + PORT);

            executorService.execute(this::connectionListener);
            executorService.execute(this::packetProcessor);
            executorService.execute(this::logWriter);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting up server", e);
        }
    }

    public static Server getInstance (EventBus eventBus) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new Server(eventBus);
                }
            }
        }

        return instance;
    }

    /**
     * Listens for incoming UDP packets and processes them. If a client is recognized, processes the
     * packet with the existing client. Otherwise, creates a new client instance and registers it.
     */
    private void connectionListener () {
        while (serverRunning.get()) {
            try {
                DatagramPacket receivePacket =
                        new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                serverSocket.receive(receivePacket);

                if (receivePacket.getLength() > 0) {
                    mailbox.add(receivePacket);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error receiving packet", e);
            }
        }
    }

    private void packetProcessor () {
        while (serverRunning.get()) {
            try {
                DatagramPacket receivePacket = mailbox.take();
                String message =
                        new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
                submitLog(message, "Received");
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

    public void send (SendPacketEvent event) {
        try {
            byte[] buffer = event.message().getBytes();
            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length,
                    event.clientAddress(), event.clientPort());
            serverSocket.send(sendPacket);
            submitLog(event.message(), "Sent");
            logger.log(Level.INFO, "Sent {0} to client at: {1}",
                    new Object[] {event.type(), event.id()});
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send message to client: {0}", event.id());
            logger.log(Level.SEVERE, "Exception: ", e);
        }
    }

    // Closes the active threads safely
    public void shutdown () {
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

    private void submitLog (String message, String action) {
        String logMessage = action + ": " + message;
        try {
            logQueue.put(logMessage);  // Add to queue for processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Failed to log message", e);
        }
    }

    private void logWriter () {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("messages.txt", true))) {
            while (serverRunning.get() || !logQueue.isEmpty()) {
                writer.write(logQueue.take());
                writer.newLine();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception: ", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Exception: ", e);
        }
    }
}
