package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestClient implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    double min = 0.01;
    double max = 0.5;
    double range = max - min;
    double speed = (Math.random() * range) + min;
    private boolean running = true;
    int id;
    private String ip;
    private int port;
    private double startingDistance;

    public TestClient(String ip, int port, double startingDistance) {
        this.ip = ip;
        this.port = port;
        this.startingDistance = startingDistance;
    }

    public void startConnection() throws Exception {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        print("Connected to " + ip + " on " + port);

        sendMessage("trainInit");
        String message = readMessage();
        if (message.equals("ACK trainInit")) sendMessage("train," + startingDistance + "," + speed);
        new Thread(this::listener).start();
    }

    public void listener() {
        try {
            while (running) {
                String message = readMessage();
                if (message != null) {
                    String[] input = message.split(",");
                    message = input[0];
                    if (message.equals("ID")) {
                        sendMessage("CONFIRM");
                        id = Integer.parseInt(input[1]);
                        System.out.println(id);
                    }
                    if (message.equals("STATUS")) {
                        sendMessage("STATUS," + id + "," + speed);
                    }
                    if (message.equals("SPD")) {
                        speed = Double.parseDouble(input[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        try {
            out.print(msg + "\n");
            out.flush();
            System.out.println("Client sends " + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readMessage() {
        try {
            String resp = in.readLine();
            System.out.println("Client recieved " + resp);
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void print(String msg) {
        System.out.println(msg);
    }

    public void stopConnection() throws IOException {
        running = false;
        in.close();
        out.close();
        clientSocket.close();
    }

    @Override
    public void run() {
        try {
            startConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int numberOfClients = 2; // Set the number of clients you want to spawn
        String ip = "localhost";
        int port = 6666;

        double trackLength = 2.2566; // Total length of the track in meters
        double spacing = trackLength / numberOfClients; // Equal spacing around the track

        for (int i = 0; i < numberOfClients; i++) {
            double startingDistance = i * spacing;
            TestClient client = new TestClient(ip, port, startingDistance);
            new Thread(client).start();
        }
    }
}
