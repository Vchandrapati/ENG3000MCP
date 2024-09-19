package org.example;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public abstract class Client {
    protected static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final InetAddress clientAddress;
    private final int clientPort;
    protected final String id;
    private final AtomicBoolean statReturned = new AtomicBoolean(false);
    private final AtomicBoolean statSent = new AtomicBoolean(false);
    protected boolean registered = false;

    protected Client(InetAddress clientAddress, int clientPort, String id) {
        this.id = id;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
    }

    public void sendMessage(String message, String type) {
        Server.getInstance().sendMessageToClient(this, message, type);

    }

    public boolean isTrainClient() {
        return this instanceof TrainClient;
    }

    public abstract void registerClient();

    protected abstract void sendStatusMessage(long timestamp);
    protected abstract void sendAcknowledgeMessage();

    public boolean lastStatReturned() {
        return statReturned.get();
    }

    public boolean lastStatMSGSent() {
        return statSent.get();
    }

    public void setStatReturned(boolean statReturned) {
        this.statReturned.set(statReturned);
    }

    public void setStatSent(boolean statSent) {
        this.statSent.set(statSent);
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