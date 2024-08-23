package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    protected Socket clientSocket;
    protected PrintWriter output;
    protected BufferedReader input;
    protected MessageHandler messageHandler;
    protected String id;
    protected volatile boolean running = true;

    protected Client(Socket clientSocket, String id) {
        try {
            this.clientSocket = clientSocket;
            this.output = new PrintWriter(clientSocket.getOutputStream(), true);
            this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.id = id;
            messageHandler = new MessageHandler();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to start client IO", e);
        }
    }

    public abstract void start();

    public String readMessage() {
        String clientInput = "";
        try {
            if (input.ready()) clientInput = input.readLine();
        } catch (IOException e) {
            logger.log(Level.SEVERE, String.format("Failed to read input of client %d", id), e);
        }
        return clientInput;
    }

    public void sendMessage(String message) {
        try {
            output.println(message);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to send message", e);
        }
    }

    public void close() {
        try {
            running = false;
            output.println("Close");
            if (clientSocket != null) clientSocket.close();
            output.close();
            input.close();
            logger.info(String.format("Connection to client %d closed", id));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to close client", e);
        }
    }
}