package org.example;

import java.io.IOException;
import java.net.*;
import java.util.Map;
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
    public final Map<InetAddress, Client> clients = new ConcurrentHashMap<>();
    private DatagramSocket serverSocket;
    private volatile boolean connectionListener;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final long TIMEOUT = 5000;
    private static final int BUFFER_SIZE = 1024;
    private static final int STAT_INTERVAL_SECONDS = 2;

    private Server() {
        connectionListener = true;
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
    }

    /**
     * Listens for incoming UDP packets and processes them.
     * If a client is recognized, processes the packet with the existing client.
     * Otherwise, creates a new client instance and registers it.
     */
    private void connectionListener() {
        while (connectionListener){
            try {
                DatagramPacket receivePacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                serverSocket.receive(receivePacket);

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                Client client = findClient(clientAddress, clientPort);

                if (client != null) {
                    client.processPacket(receivePacket);
                    logger.info("Packet processed for client: " + client.id);
                } else {
                    // Checks if client exists in the whitelist
                    String clientType = ClientTable.getInstance().getComponent(clientAddress.getHostAddress(), clientPort);
                    Client newClient = createClient(clientType, clientAddress, clientPort);
                    newClient.registerClient();
                    clients.put(clientAddress, newClient);
                    newClient.processPacket(receivePacket);
                    logger.info("New client created and packet processed for client: " + newClient.id);
                }
            } catch (IOException e) {
                if(!connectionListener) {
                    logger.log(Level.SEVERE,"Unexpected error in connection listner", e);
                }
                else {
                    logger.log(Level.SEVERE, "Error receiving or processing packet", e);
                }
            }
        }
    }

    private Client findClient(InetAddress clientAddress, int clientPort) {
        Client ret = clients.get(clientAddress);
        if (ret.getClientPort() == clientPort)
            return ret;

        return null;
    }

    private static Client createClient(String clientType, InetAddress clientAddress, int clientPort) throws IOException {
        String componentType = clientType.split(" ")[0];

        if (componentType.contains("LED"))
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
            for (Client client : clients.values()) {
                client.setStatReturned(false);
                String statusMessage = MessageGenerator.generateStatusMessage(client.id, client.id, System.currentTimeMillis());
                client.sendMessage(statusMessage);  // Send status request message
            }

            scheduler.schedule(() -> checkForMissingResponse(sendTime), TIMEOUT, TimeUnit.MILLISECONDS);
        }, 0, STAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
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
            if (!client.lastStatReturned()) {
                logger.severe(String.format("No STAT response from %s sent at %d", client.getId(), sendTime));
                hasFailed = true;
                //if a train is unresponsive
                if(client.getId().contains("BR")) db.addUnresponsiveClient(client.getId());
            }
            if(hasFailed) SystemStateManager.getInstance().setState(SystemState.EMERGENCY);
        });
    }

    public void sendMessageToClient(Client client, String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, client.getClientAddress(), client.getClientPort());
            serverSocket.send(sendPacket);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send message to client " + client.getId(), e);
        }
    }

    // Closes the active threads safely
    public void shutdown() {
        try {
            if(serverSocket != null) {
                connectionListener = false;
                serverSocket.close();
                scheduler.shutdown();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error shutting down server", e);
        }
    }
}

