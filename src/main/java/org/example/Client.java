package org.example;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public abstract class Client {
    protected static final Logger logger = Logger.getLogger(Client.class.getName());
    protected MessageHandler messageHandler;
    private InetAddress clientAddress;
    private int clientPort;
    protected String id;
    private volatile boolean statReturned = false;

    protected Client(InetAddress clientAddress, int clientPort, String id) {
        this.id = id;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        messageHandler = new MessageHandler();
    }

    public void processPacket(DatagramPacket packet) {
        String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
        if (!message.isEmpty()) {
            message = message.replaceAll("[^\\x20-\\x7E]", " ");
            messageHandler.handleMessage(message);
        }
    }

    public void sendMessage(String message) {
        Server.getInstance().sendMessageToClient(this, message);
    }

    public abstract void registerClient();

    public boolean lastStatReturned() {
        return statReturned;
    }

    public void setStatReturned(boolean statReturned) {
        this.statReturned = statReturned;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public int getClientPort() {
        return clientPort;
    }

    public String getId() {
        return id;
    }
}