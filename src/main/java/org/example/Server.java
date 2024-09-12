package org.example;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Constants {

    private static final Database db = Database.getInstance();
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    public final List<Client> clients = new CopyOnWriteArrayList<>();
    private DatagramSocket serverSocket;
    private volatile boolean connectionListener;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final long TIMEOUT = 5000;
    private final int STAT_INTERVAL = 2;

    public Server() {
        connectionListener = true;
        try {
            serverSocket = new DatagramSocket(PORT);
            connectionListener();

            logger.info("Server completed startup and listnening on PORT: " + PORT);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting up server", e);
        }
    }

    private void connectionListener() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (connectionListener){
                try {
                    DatagramPacket recievePacket = new DatagramPacket(buffer, buffer.length);
                    serverSocket.receive(recievePacket);

                    InetAddress clientAddress = recievePacket.getAddress();
                    int clientPort = recievePacket.getPort();
                    Client client = findClient(clientAddress, clientPort);

                    if (client != null) {
                        client.processPacket(recievePacket);
                        logger.info("Packet processed for client: " + client.id);
                    } else {
                        String clientType = ClientTable.getInstance().getComponent(clientAddress.getHostAddress(), clientPort);
                        Client newClient = createClient(clientType, clientAddress, clientPort);
                        newClient.registerClient();
                        clients.add(newClient);
                        newClient.processPacket(recievePacket);
                        logger.info("New client created and packet processed for client: " + newClient.id);
                    }
                } catch (IOException e) {
                    if(connectionListener == false) {
                        logger.info("Closing server mid listening");
                    }
                    else {
                        logger.log(Level.SEVERE, "Error receiving or processing packet", e);
                    }
                }
            }
        }).start();
    }

    private Client findClient(InetAddress clientAddress, int clientPort) {
        for (Client client : clients) {
            if (client.clientAddress.equals(clientAddress) && client.clientPort == clientPort)
                return client;
        }

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
            for (Client client : clients) {
                client.setStatReturned(false);
                String statusMessage = MessageGenerator.generateStatusMessage(client.id, client.id, System.currentTimeMillis());
                client.sendMessage(statusMessage);  // Send status request message
            }

            scheduler.schedule(() -> checkForMissingResponse(sendTime), TIMEOUT, TimeUnit.MILLISECONDS);
        }, 0, STAT_INTERVAL, TimeUnit.SECONDS);
    }


    //Checks if a client has not responded to a STAT message in 2 seconds, if so go to emergency
    //If a train client has gone rogue, add that to the unresponsive client list in the database
    private void checkForMissingResponse(long sendTime) {
        clients.forEach(client -> {
            boolean hasFailed = false;
            if (!client.lastStatReturned()) {
                logger.severe(String.format("No STAT response from %s sent at %d", client.id, sendTime));
                hasFailed = true;
                //if a train is unresponsive
                if(client.id.contains("BR")) db.addUnresponsiveClient(client.id);
            }
            if(hasFailed) SystemStateManager.getInstance().setState(SystemState.EMERGENCY);
        });
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

