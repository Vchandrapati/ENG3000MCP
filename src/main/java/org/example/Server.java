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

/**
 * Singleton instance that maintains a lookup table for clients.
 * The table maps client connection details (IP and port) to corresponding component information.
 * Uses the {@link Pair} class to store and manage client connection details and component information.
 */

public class Server implements Constants {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final Thread connectClientThread;
//    private  final Thread manageClientThread;
    private  final ScheduledExecutorService sendClientThread;
    private volatile boolean connectClientRunning = true;
    private volatile boolean manageClientRunning = true;
    private volatile boolean sendClientRunning = true;
    private ServerSocket serverSocket;
    private final List<Client> clients;
    private final MessageHandler messageHandler;
    private final Database db;
    public Server(Database db) {
        this.db = db;

        startServer();

        clients = new CopyOnWriteArrayList<>();

        //starts a thread that only listens for client connections
        connectClientThread = new Thread(this::connectClients);
        connectClientThread.start();

//        //starts a thread that processes client inputs
//        manageClientThread = new Thread(this::manageClientsInput);
//        manageClientThread.start();

        sendClientThread = Executors.newScheduledThreadPool(1);
        startClientPoller();

        this.messageHandler = new MessageHandler();
    }

    private void startClientPoller() {
        Runnable pingTask = () -> {
            if(sendClientRunning) {
                for (Client client : clients) {
                    if(client instanceof TrainClient) {
                        client.sendMessage("STATUS");
                    }
                }
            }
        };

        sendClientThread.scheduleAtFixedRate(pingTask, 0, 2, TimeUnit.SECONDS); // Ping clients every second
    }

    private void connectClients() {
        ClientTable clientTable = ClientTable.getInstance();

        while (connectClientRunning) {
            logger.info("Server listening for clients on port: " + PORT);
            try {
                Socket clientSocket = serverSocket.accept();
                InetAddress clientAddress = clientSocket.getInetAddress();
                int clientPort = clientSocket.getPort();
                String clientType = clientTable.getComponent(clientAddress.getHostAddress(), clientPort);

                if (clientType != null) {
                    clients.add(getClient(clientType, clientAddress, clientPort));
                    logger.info("Accepted new client from " + clientType);
                } else {
                    logger.warning(String.format("Unknown client connection from IP: %s, Port %d", clientAddress.getHostAddress(), clientPort));
                }

            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error accepting client connection ", e);
            }
        }
    }

    private static Client getClient(String clientType, InetAddress clientAddress, int clientPort) throws IOException {
        String componentType = clientType.split(" ")[0];

        Socket newClientSocket = new Socket(clientAddress, clientPort);
        return switch(componentType) {
            case "LED" -> new CheckpointClient(newClientSocket, clientType);
            case "BR" -> new TrainClient(newClientSocket, clientType);
            case "ST" -> new StationClient(newClientSocket, clientType);
            default -> throw new IOException(clientType);
        };
    }

//    // Checks each client if they have sent a message
//    private void manageClientsInput() {
//        while(manageClientRunning) {
//            //adds all connected clients that are waiting
//            for (Client client : clients) {
//                String input = client.readMessage();
//
//                if(!input.isEmpty())
//                    messageHandler.handleMessage(client, input, db, visServer);
//            }
//        }
//    }

    // Initialises the server
    private void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            logger.info("Server completed startup");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting up server", e);
        }
    }

    // Closes the active threads safely
    public void stop() {
        try {
            if(serverSocket != null) {
                connectClientRunning = false;
                manageClientRunning = false;
                sendClientRunning = false;

                serverSocket.close();

                // manageClientThread.join();
                connectClientThread.join();
                sendClientThread.shutdownNow();

                closeClients();
            }
            logger.info("Server shutdown successfully");
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Error shutting down thread", e);
            Thread.currentThread().interrupt();
        }catch (IOException e) {
            logger.log(Level.SEVERE, "Error shutting down server", e);
        }
    }

    public void communicate(int id, String message) {
        clients.get(id).sendMessage(message);
    }

    private void closeClients() {
        for (Client client : clients) {
            client.close();
        }
    }
}

