package org.example;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
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
    private volatile Statuses status;
    private String lastMessageSent;
    protected HashSet<ReasonEnum> unresponsiveReasons;



    protected Client(InetAddress clientAddress, int clientPort, String id) {
        status = Statuses.ON;
        this.id = id;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        unresponsiveReasons = new HashSet<>();
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

    protected abstract void addReason(ReasonEnum r);

    public HashSet<ReasonEnum> getUnresponsiveReasons() {
        return unresponsiveReasons;
    }

    public void removeReason(ReasonEnum r) {
        unresponsiveReasons.remove(r);
    }

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

    public void updateStatus(Statuses newStatus) {
        try {
            status = newStatus;
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Tried to assign unknown status: {0} for train {1}", new Object[] {newStatus, id});
        }
    }

    public String getStatus() {
        return status.toString();
    }

    public String getLastMessageSent() {
        return lastMessageSent;
    }
}
