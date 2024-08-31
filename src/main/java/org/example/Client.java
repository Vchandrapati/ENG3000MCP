package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    protected DatagramSocket clientSocket;
    protected MessageHandler messageHandler;
    protected InetAddress clientAddress;
    protected int clientPort;
    protected String id;
    protected volatile boolean running = true;

    protected Client(InetAddress clientAddress, int clientPort, String id) {
        try {
            this.clientSocket = new DatagramSocket();
            this.id = id;
            this.clientAddress = clientAddress;
            this.clientPort = clientPort;
            messageHandler = new MessageHandler();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to start client IO", e);
        }
    }

    public abstract void start();

    public String readMessage() {
        byte[] buffer = new byte[1024];
        DatagramPacket recievePacket = new DatagramPacket(buffer, buffer.length);
        try {
            clientSocket.receive(recievePacket);
            return new String(recievePacket.getData(), 0, recievePacket.getLength());
        } catch (IOException e) {
            logger.log(Level.SEVERE, String.format("Failed to read input of client %d", id), e);
        }
        return null;
    }

    public void sendMessage(String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            clientSocket.send(sendPacket);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to send message", e);
        }
    }

    public void close() {
        try {
            running = false;
            if (clientSocket != null) clientSocket.close();
            logger.info(String.format("Connection to client %d closed", id));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to close client", e);
        }
    }
}