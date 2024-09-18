package org.example;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public abstract class Client {
    protected static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected MessageHandler messageHandler;
    private InetAddress clientAddress;
    private int clientPort;
    protected String id;
    private volatile boolean statReturned = false;
    private volatile boolean statSent = false;
    protected boolean registered = false;

    protected Client(InetAddress clientAddress, int clientPort, String id) {
        this.id = id;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        messageHandler = new MessageHandler();
    }

    public void sendMessage(String message, String type) {
        Server.getInstance().sendMessageToClient(this, message, type);
    }

    public abstract void registerClient();
    protected abstract void sendStatusMessage(String id, Long timestamp);

    public boolean lastStatReturned() {
        return statReturned;
    }

    public boolean lastStatMSGSent(){
        return statSent;
    }

    public void setStatReturned(boolean statReturned) {
        this.statReturned = statReturned;
    }

    public void setStatSent(boolean statSent){
        this.statSent = statSent;
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

    public Boolean isRegistered() {
        return registered;
    }
}