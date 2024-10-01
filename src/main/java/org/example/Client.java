package org.example;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.logging.Level;

public abstract class Client {
    protected static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected final String id;
    private final InetAddress clientAddress;
    private final int clientPort;
    private final AtomicBoolean statReturned = new AtomicBoolean(false);
    private final AtomicBoolean statSent = new AtomicBoolean(false);
    protected boolean registered = false;
    private volatile Status status;
    private String lastMessageSent;

    private enum Status {
        ON,
        OFF,
        ERR,
    }

    protected Client(InetAddress clientAddress, int clientPort, String id) {
        status = Status.ON;
        this.id = id;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
    }

    public void sendMessage(String message, String type) {
        lastMessageSent = type;
        Server.getInstance().sendMessageToClient(this, message, type);
    }

    public boolean isBladeRunnerClient() {
        return this instanceof BladeRunnerClient;
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

    public void updateStatus(String newStatus) {
        try {
            status = Status.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Tried to assign unknown status: {0} for train {1}", new Object[]{newStatus, id});
        }
    }

    public String getStatus() {
        return status.toString();
    }

    public String getLastMessageSent() {
        return lastMessageSent;
    }
}