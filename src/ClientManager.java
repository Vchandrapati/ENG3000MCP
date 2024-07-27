import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientManager {
    private List<Client> clients;
    private int numOfClients;
    
    public ClientManager() {
        clients = new ArrayList<>();
    }

    private class Client{
        Socket clientSocket;
        PrintWriter out;
        BufferedReader in;
        int id;

        public Client(Socket clientSocket) {
            try {
                this.clientSocket = clientSocket;
                this.out = new PrintWriter(clientSocket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                this.id = numOfClients;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addClient(Socket client) {
        clients.add(new Client(client));
    }

    public int getNumOfClients() {
        return numOfClients;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void closeClients() {
        for (Client client : clients) {
            if(client != null) {
                try {
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
}
