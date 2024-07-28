import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    static final int PORT = 6666;
    private final VisualiserServer visServer;
    private final Database database;
    private final Thread connectClientThread;
    private  final Thread manageClientThread;
    private  final ScheduledExecutorService sendClientThread;
    private volatile boolean connectClientRunning = true;
    private volatile boolean manageClientRunning = true;
    private volatile boolean sendClientRunning = true;
    private ServerSocket serverSocket;
    private final List<Client> clients;
    private final MessageHandler messageHandler;
    public Server(VisualiserServer vis, int w, int h) {
        this.visServer = vis;
        database = new Database(new Ring(w, h));
        vis.updateRing(database.getRing());

        startServer();

        clients = new CopyOnWriteArrayList<>();

        //starts a thread that only listens for client connections
        connectClientThread = new Thread(this::connectClients);
        connectClientThread.start();

        //starts a thread that processes client inputs
        manageClientThread = new Thread(this::manageClientsInput);
        manageClientThread.start();

        sendClientThread = Executors.newScheduledThreadPool(1);
        startClientPoller();

        messageHandler = new MessageHandler();
    }

    private void startClientPoller() {
        Runnable pingTask = () -> {
            if(sendClientRunning) {
                for (Client client : clients) {
                    if(client.clientType == Client.type.TRAIN) {
                        client.sendMessage("STATUS");
                    }
                }
            }
        };

        sendClientThread.scheduleAtFixedRate(pingTask, 0, 1, TimeUnit.SECONDS); // Ping clients every second
    }

    private void connectClients() {
        while (connectClientRunning) {
            logger.info("Server listening for clients on port: " + PORT);
            try {
                Socket clientSocket = serverSocket.accept();
                logger.info("Accepted new client connection: " + clientSocket.getRemoteSocketAddress());
                clients.add(new Client(clientSocket, clients.size()));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error accepting client connection", e);
            }
        }
    }

    // Checks each client if they have sent a message
    private void manageClientsInput() {
        while(manageClientRunning) {
            //adds all connected clients that are waiting
            for (Client client : clients) {
                String input = client.readClient();
                messageHandler.handleMessage(client, input, database, visServer);
            }
        }
    }

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
            visServer.stop();
            if(serverSocket != null) {
                connectClientRunning = false;
                manageClientRunning = false;
                sendClientRunning = false;

                serverSocket.close();

                manageClientThread.join();
                connectClientThread.join();
                sendClientThread.shutdownNow();

                closeClients();
            }
            logger.info("Server shutdown successfully");
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Error shutting down thread", e);
        }catch (IOException e) {
            logger.log(Level.SEVERE, "Error shutting down server", e);
        }
    }

    private void closeClients() {
        for (Client client : clients) {
            client.close();
        }
    }

    class Client {
        private static final Logger logger = Logger.getLogger(Client.class.getName());
        Socket clientSocket;
        PrintWriter output;
        BufferedReader input;
        int id;
        String lastMessage = "";
        enum type {
            TRAIN, STATION, UNKNOWN
        }

        type clientType;

        public Client(Socket clientSocket, int num) {
            try {
                this.clientType = type.UNKNOWN;
                this.clientSocket = clientSocket;
                this.output = new PrintWriter(clientSocket.getOutputStream(), true);
                this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                this.id = num;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to start client IO", e);
            }
        }

        public String readClient() {
            String clientInput = "";
            try {
                if(input.ready()) clientInput = input.readLine();
                else return "";
            } catch (Exception e) {
                logger.log(Level.SEVERE, String.format("Failed to read input of client %d", id), e);
            }
            return clientInput;
        }

        public void sendMessage(String s) {
            try {
                output.println(s);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to send message", e);
            }
        }

        public void close() {
            try {
                output.println("Close");
                if(clientSocket != null) clientSocket.close();
                output.close();
                input.close();
                logger.info(String.format("Connection to client %d close", id));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to close client", e);
            }
        }
    }
}

