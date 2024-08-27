package org.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Constants {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final List<Client> clients = new CopyOnWriteArrayList<>();
    private ServerSocket serverSocket;
    private volatile boolean connectionListener;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Server() {
        connectionListener = true;
        try {
            serverSocket = new ServerSocket(PORT);
            connectionListener();
            startStatusScheduler();
            logger.info("Server completed startup and listnening on PORT: " + PORT);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting up server", e);
        }
    }

    private void connectionListener() {
        new Thread(() -> {
            while (connectionListener) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    InetAddress clientAddress = clientSocket.getInetAddress();
                    int clientPort = clientSocket.getPort();
                    String clientType = ClientTable.getInstance().getComponent(clientAddress.getHostAddress(),
                            clientPort);

                    if (clientType != null) {
                        Client client = createClient(clientType, clientAddress, clientPort);
                        clients.add(client);
                        client.start(); // Start the client's read thread
                        logger.info("Accepted and started client: " + clientType);
                    } else {
                        logger.warning(
                                "Unknown client connection: " + clientAddress.getHostAddress() + ":" + clientPort);
                        clientSocket.close(); // Close the connection if not recognized
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private static Client createClient(String clientType, InetAddress clientAddress, int clientPort)
            throws IOException {
        String componentType = clientType.split(" ")[0];

        Socket newClientSocket = new Socket(clientAddress, clientPort);
        return switch (componentType) {
            case "LED" -> new CheckpointClient(newClientSocket, clientType, null);// null harcode
            case "BR" -> new TrainClient(newClientSocket, clientType);
            case "ST" -> new StationClient(newClientSocket, clientType, null);// null harcode
            default -> throw new IOException(clientType);
        };
    }

    private void startStatusScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            for (Client client : clients) {
                String statusMessage = MessageGenerator.generateStatusMessage(client.id, client.id,
                        System.currentTimeMillis());
                client.sendMessage(statusMessage); // Send status request message
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    // Closes the active threads safely
    public void stop() {
        try {
            if (serverSocket != null) {
                connectionListener = false;
                serverSocket.close();
            }
            logger.info("Server shutdown successfully");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error shutting down server", e);
        }
    }
}
