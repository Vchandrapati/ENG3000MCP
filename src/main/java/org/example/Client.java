package org.example;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Client {
    protected static final Logger logger = Logger.getLogger(Client.class.getName());
    protected DatagramSocket clientSocket;
    protected MessageHandler messageHandler;
    public InetAddress clientAddress;
    public int clientPort;
    protected String id;
    private volatile boolean statReturned = false;

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

    public void processPacket(DatagramPacket packet) throws UnsupportedEncodingException {
        String message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
        if (!message.isEmpty()) {
            message = message.replaceAll("[^\\x20-\\x7E]", " ");
            messageHandler.handleMessage(message);
        }
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
            if (clientSocket != null)
                clientSocket.close();
            logger.info(String.format("Connection to client %d closed", id));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to close client", e);
        }
    }

    public abstract void registerClient();

    public boolean lastStatReturned() {
        return statReturned;
    }

    public void setStatReturned(boolean statReturned) {
        this.statReturned = statReturned;
    }
}