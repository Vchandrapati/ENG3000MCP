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

public class Server {
    static final int PORT = 6666;

    private VisualiserServer visServer;

    private Database database;

    private final Thread connectClientThread;
    private  final Thread manageClientThread;
    private  final ScheduledExecutorService sendClientThread;

    private volatile boolean connectClientRunning = true;

    private volatile boolean manageClientRunning = true;

    private volatile boolean sendClientRunning = true;

    private ServerSocket serverSocket;

    private List<Client> clients;
    public Server(VisualiserServer vis, int w, int h) {
        this.visServer = vis;
        database = new Database(new Ring(w, h));
        vis.updateRing(database.getRing());

        startServer();

        clients = new CopyOnWriteArrayList<>();

        //starts a thread that only listens for client connections
        connectClientThread = new Thread(() -> connectClients());
        connectClientThread.start();

        //starts a thread that processes client inputs
        manageClientThread = new Thread(() -> manageClientsInput());
        manageClientThread.start();

        sendClientThread = Executors.newScheduledThreadPool(1);
        startClientPoller();
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
            System.out.println("Waiting for new client");
            try {
                Socket clientSocket = serverSocket.accept();
                clients.add(new Client(clientSocket, clients.size()));
            } catch (IOException e) {
                if(connectClientRunning) System.out.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }

    //checks each client if they have sent a message
    private void manageClientsInput() {
        while(manageClientRunning) {
            //adds all connected clients that are waiting
            for (Client client : clients) {
                String input = client.readClient();
                processClientsInput(client, input);
            }
        }
    }

    private void processClientsInput(Client client, String input) {
        if (client.lastMessage.isEmpty()) {
            if (input.equals("trainInit") || input.equals("stationInit")) {
                handleInitialClientMessages(client, input);
            } else {
                handleClientMessages(client, input);
            }
        }
    }

    private void handleInitialClientMessages(Client client, String input) {
        if (input.equals("trainInit")) {
            client.sendMessage("ack trainInit");
            client.lastMessage = "trainInit";
            client.clientType = Client.type.TRAIN;
        } else if (input.equals("stationInit")) {
            client.sendMessage("ack stationInit");
            client.lastMessage = "stationInit";
            client.clientType = Client.type.STATION;
        }
    }

    private void handleClientMessages(Client client, String input) {
        String[] inputArr = input.split(",");
        if (client.lastMessage.equals("trainInit") && inputArr.length == 5 && inputArr[0].equals("train")) {
            handleTrainMessage(client, inputArr);
        } else if (client.lastMessage.equals("stationInit") && inputArr.length == 5 && inputArr[0].equals("station")) {
                handleStationMessage(client, inputArr);
        } else if (inputArr[0].equals("ping")) {
            handlePingMessage(client, inputArr);
        } else {
            client.sendMessage("Error, client gave wrong message");
        }
    }

    private void handleTrainMessage(Client client, String[] inputArr) {
        double angle = Double.parseDouble(inputArr[2]);
        double speed = Double.parseDouble(inputArr[3]);
        Train newTrain = new Train(0, 0, speed, angle);
        database.addTrain(newTrain);
        visServer.updateTrains(database.getTrains());
        client.sendMessage("Train confirmed!");
        System.out.println(inputArr[4]);
        client.lastMessage = "Train confirmed";
    }

    private void handleStationMessage(Client client, String[] inputArr) {
        //TODO
    }

    private void handlePingMessage(Client client, String[] inputArr) {
        int id = client.id;
        Train t = database.getTrain(id);
        t.speed = Double.valueOf(inputArr[1]);
        database.updateTrain(t);
        System.out.println(inputArr[1] + " Given speed of train : Train : " + client.id);
    }

    //initialises the server
    private void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //shutdowns the active threads safely
    public void stop(){
        try {
            visServer.stop();
            if(serverSocket != null) {
                connectClientRunning = false;
                manageClientRunning = false;
                sendClientRunning = false;

                manageClientThread.join();
                connectClientThread.join();
                sendClientThread.shutdownNow();

                serverSocket.close();
                closeClients();
            }
            System.out.println("Server has stopped");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeClients() {
        for (Client client : clients) {
            client.close();
        }
    }

    private static class Client {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String readClient() {
            String clientInput = "";
            try {
                if(input.ready()) clientInput = input.readLine();
                else return "";
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed to read client " + id + " input");
            }
            return clientInput;
        }

        public void sendMessage(String s) {
            try {
                output.println(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void close() {
            try {
                output.println("Close");
                if(clientSocket != null) clientSocket.close();
                output.close();
                input.close();
                System.out.println("Client " + id + " closed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

