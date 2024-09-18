package org.example;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages network communication with clients over UDP.
 * It handles incoming packets, maintains a list of connected clients,
 * and schedules status checks.
 *
 * <p>Utilises the Singleton pattern to ensure only one instance of the Server exists.
 * It also implements the {@link Constants} interface for configuration.
 */
public class Server implements Constants, Runnable {
    private static final Database db = Database.getInstance();
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public final Map<Integer, Client> clients = new ConcurrentHashMap<>();
    private DatagramSocket serverSocket;
    private volatile boolean serverRunning;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final int TIMEOUT = 5000;
    private static final int BUFFER_SIZE = 1024;
    private static final int STAT_INTERVAL_SECONDS = 5000;
    private static BlockingQueue<DatagramPacket> mailbox = new LinkedBlockingQueue<>();

    private Server() {
        serverRunning = true;
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
        Thread listenerThread = new Thread(this::connectionListener, "Server-ConnectionListener");
        listenerThread.start();

        Thread packetProcessor = new Thread(this::packetProcessor, "Packet-Processor-Thread");
        packetProcessor.start();
    }

    /**
     * Listens for incoming UDP packets and processes them.
     * If a client is recognized, processes the packet with the existing client.
     * Otherwise, creates a new client instance and registers it.
     */
    private void connectionListener() {
        while (serverRunning){
            try {
                DatagramPacket receivePacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                serverSocket.receive(receivePacket);
                if (receivePacket.getLength() > 0)
                    logger.info("Packet recieved");

                mailbox.add(receivePacket);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error receiving packet", e);
                
            }
        }
    }

    private void packetProcessor() {
        while (serverRunning) {
            try {
                DatagramPacket receivePacket = mailbox.take();
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                Client client = findClient(clientAddress, clientPort);
        
                if (client != null) {
                    client.processPacket(receivePacket);
                    logger.info("Packet processed for client: " + client.id);
                } else if (client == null) {
                    // Checks if client exists in the whitelist
                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
                    MessageHandler mg = new MessageHandler();
                    mg.handleInitilise(message, clientAddress, clientPort);
                }
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
        }
    }
    private Client findClient(InetAddress clientAddress, int clientPort) {
        List<Client> clients = Database.getInstance().getClients();
        if (clients.isEmpty()) return null;

        Optional<Client> client = clients.stream().filter(c -> c.getClientPort() == clientPort && c.getClientAddress() == clientAddress).findFirst();
        logger.warning("Address: " + clientAddress + " Port: " + clientPort);
        logger.warning(client.get() + "");
        if (!client.isPresent())
            return null;

        return client.get();
    }

    private static Client createClient(String clientType, InetAddress clientAddress, int clientPort) throws IOException {
        String componentType = clientType.split(" ")[0];

        if (componentType.contains("CP"))
            return new CheckpointClient(clientAddress, clientPort, clientType);
        else if (componentType.contains("BR"))
            return new TrainClient(clientAddress, clientPort, clientType);
        else if (componentType.contains("ST"))
            return new StationClient(clientAddress, clientPort, clientType);
        else
            throw new IOException(clientType);
    }

    public void startStatusScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            long sendTime = System.currentTimeMillis();
            
            List<Client> clients = Database.getInstance().getClients();
            for (Client client : clients) {
                if (client.isRegistered()) {
                    client.setStatReturned(false);
                    client.sendStatusMessage(client.id, System.currentTimeMillis());
                }
            }

            try {
                Thread.sleep(5000);
                checkForMissingResponse(sendTime);
            } catch (Exception e) {
                logger.info("Error waiting for stat");
            }
        }, 0, STAT_INTERVAL_SECONDS, TimeUnit.MILLISECONDS);
    }


    /**
     * Checks for clients that have not responded to a status request sent within a timeframe.
     * If a client has not responded, logs an error and updates the system state to EMERGENCY.
     * For unresponsive train clients, adds them to the unresponsive client list in the database.
     *
     * @param sendTime the time the status request was sent
     */
    private void checkForMissingResponse(long sendTime) {
        clients.values().forEach(client -> {
            boolean hasFailed = false;
            if (!client.lastStatReturned() && client.isRegistered() && client.lastStatMsgSent()) {
                logger.severe(String.format("No STAT response from %s sent at %d", client.getId(), sendTime));
                hasFailed = true;
                //if a train is unresponsive
                if(client.getId().contains("BR")) db.addUnresponsiveClient(client.getId());
            }

            if(hasFailed) SystemStateManager.getInstance().setState(SystemState.EMERGENCY);
        });
    }

    public void sendMessageToClient(Client client, String message, String type) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, client.getClientAddress(), client.getClientPort());
            serverSocket.send(sendPacket);
            logger.info(String.format("Sent %s to client: %s", type, client.id));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send message to client " + client.getId(), e);
        }
    }

    // Closes the active threads safely
    public void shutdown() {
        try {
            if(serverSocket != null) {
                serverRunning = false;
                serverSocket.close();
                scheduler.shutdown();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error shutting down server", e);
        }
    }
}

