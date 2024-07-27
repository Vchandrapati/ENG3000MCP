import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    final int port = 6666;

    private VisualiserServer visServer;
    private Database database;

    private Thread connectClientThread;
    private volatile boolean connectClientRunning = true;

    private Thread manageClientThread;
    private volatile boolean manageClientRunning = true;

    private Thread sendClientThread;
    private volatile boolean sendClientRunning = true;

    private ServerSocket serverSocket;

    private List<Client> waitingClients;
    private List<Client> clients;

    public Server(VisualiserServer vis, int w, int h) {
        this.visServer = vis;
        database = new Database(new Ring(w, h));
        vis.updateRing(database.getRing());

        bootServer(); 

        waitingClients = new ArrayList<>();
        clients = new ArrayList<>();

        //starts a thread that only listens for client connections
        connectClientThread = new Thread(() -> connectClients());
        connectClientThread.start();

        //starts a thread that processes client inputs
        manageClientThread = new Thread(() -> manageClientsInput());
        manageClientThread.start();

        //starts a thread that communicates things to clients
        sendClientThread = new Thread(() -> manageClientsOutput());
        sendClientThread.start();
    }

    //checks each client if they have sent a message
    private void manageClientsOutput() {
        long time = System.currentTimeMillis();
        long timeInterval = 500; // pings clients every half a second
        while(sendClientRunning) {
            if (System.currentTimeMillis() - time >= timeInterval) {
                time = System.currentTimeMillis();
                //synchronized (clients) {
                if(clients.size() != 0) {
                    for (Client client : clients) {
                        if(client.type.equals("train")) {
                            client.sendMessage("ping");
                        }
                    }
                }
            }
        }
    }

    //checks each client if they have sent a message
    private void manageClientsInput() {
        while(manageClientRunning) {
            //adds all connected clients that are waiting
            addWaitingClients();
            //goes throuh each client
            for (Client client : clients) {
                String input = client.readClient();
                //if the client has never messaged before
                if(input == "") continue;
                if(client.lastMessage.equals("")) {
                    if(input.equals("trainInit")) {
                        client.sendMessage("ack trainInit");
                        client.lastMessage = "trainInit";
                        client.type = "train";
                        continue;
                    }
                    if(input.equals("stationInit")) {
                        
                    }
                } 
                String[] inputArr = input.split(",");
                if(client.lastMessage.equals("trainInit")){
                    //"train,angle,speed,status" message protocol format
                    //FIX THIS, doesnt check some stuff
                    if(inputArr.length == 5) {
                        if(inputArr[0].equals("train")) {
                            double angle = Double.valueOf(inputArr[2]); 
                            double speed = Double.valueOf(inputArr[3]); 
                            Train newTrain = new Train(0, 0, speed, angle);
                            database.addTrain(newTrain);
                            visServer.updateTrains(database.getTrains());
                            client.sendMessage("Train confirmed!");
                            System.out.println(inputArr[4]);
                            client.lastMessage = "Train confirmed";
                            continue;
                        }
                    }
                }
                if(inputArr[0].equals("ping")) {
                    int id = client.id;
                    Train t = database.getTrain(id);
                    t.speed = Double.valueOf(inputArr[1]);
                    database.updateTrain(t);
                    System.out.println(inputArr[1] + " Given speed of train : Train : " + client.id);
                }
                client.sendMessage("Error, client gave wrong message");
            }
        }
    }

    private void addWaitingClients() {
        synchronized (waitingClients) {
            for (Client client : waitingClients) {
                System.out.println("Added new client " + client.id);
                clients.add(client);
            }
            waitingClients.clear();
        }
    }

    private void connectClients() {
        while (connectClientRunning) {
            System.out.println("Waiting for new client");
            try {
                Socket clientSocket = serverSocket.accept();
                synchronized (waitingClients) {
                    waitingClients.add(new Client(clientSocket, clients.size()));
                }
            } catch (IOException e) {
                if(connectClientRunning) System.out.println("Error accepting client connection: " + e.getMessage());
            }
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
                serverSocket.close();
                connectClientThread.join();
                sendClientThread.join();
                closeClients();
            }
            System.out.println("Server has stopped");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //initialises the server
    private void bootServer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is running");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeClients() {
        for (Client client : clients) {
            if(client != null) {
                try {
                    client.out.println("Close");
                    client.clientSocket.close();
                    client.out.close();
                    client.in.close();
                    System.out.println("Client " + client.id + " closed");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Client{
        Socket clientSocket;
        PrintWriter out;
        BufferedReader in;
        int id;
        String lastMessage = "";
        String type = "";

        public Client(Socket clientSocket, int num) {
            try {
                this.clientSocket = clientSocket;
                this.out = new PrintWriter(clientSocket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                this.id = num;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String readClient() {
            String clientInput = "";
            try {
                if(in.ready()) clientInput = in.readLine();
                else return "";
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed to read client " + id + " input");
            }
            return clientInput;
        }

        public void sendMessage(String s) {
            try {
                out.println(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
