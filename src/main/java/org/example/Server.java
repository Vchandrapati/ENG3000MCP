package org.example;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Constants {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    public final List<Client> clients = new CopyOnWriteArrayList<>();
    private DatagramSocket serverSocket;
    private volatile boolean connectionListener;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
                    logger.log(Level.SEVERE, "Error receiving or processing packet", e);
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
            for (Client client : clients) {
                String statusMessage = MessageGenerator.generateStatusMessage(client.id, client.id, System.currentTimeMillis());
                client.sendMessage(statusMessage);  // Send status request message
            }
        }, 0, 2, TimeUnit.SECONDS);
    }


    // Closes the active threads safely
    public void stop() {
        try {
            if(serverSocket != null) {
                connectionListener = false;
                serverSocket.close();
            }
            logger.info("Server shutdown successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error shutting down server", e);
        }
    }
}

