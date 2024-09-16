import static org.example.Server.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.util.*;

public class TestClient {
    List<Client> clients;

    int trains = 5;
    int checkStation = 10;

    public TestClient() {
        createClients();
    }

    private void createClients() {
        try {
            for (int i = 0; i < trains; i++) {
                Client train = new Client(InetAddress.getByName(DOMAIN), PORT, "BR0" + i); 
                clients.add(train);
            }
            for (int i = 0; i < checkStation; i++) {
                Client train = new Client(InetAddress.getByName(DOMAIN), PORT, "CHST0" + i); 
                clients.add(train);
            }
        } catch (Exception e) {} 
    }

    public static void main(String[] args) {
        new TestClient();  
    }
    
    private class Client {
        protected DatagramSocket clientSocket;
        public InetAddress clientAddress;
        public int clientPort;
        protected String id;
        private volatile boolean statReturned = false;

        public Client(InetAddress clientAddress, int clientPort, String id) {
            try {
                this.clientSocket = new DatagramSocket();
                this.id = id;
                this.clientAddress = clientAddress;
                this.clientPort = clientPort;
            } catch (IOException e) {}
    }

        public void sendMessage(String message) {
            try {
                byte[] buffer = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
                clientSocket.send(sendPacket);
            } catch (Exception e) {
            }
        }
    }
}
