package org.example;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Client<S extends Enum<S>, A extends Enum<A>> {
    protected static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    S status;
    private final String id;
    private final InetAddress clientAddress;
    private final int clientPort;
    private final AtomicBoolean statReturned = new AtomicBoolean(false);
    private final AtomicBoolean statSent = new AtomicBoolean(false);
    protected final AtomicInteger sequenceNumber;
    protected boolean registered = false;
    private String lastMessageSent;
    protected Set<ReasonEnum> unresponsiveReasons;

    protected Client(InetAddress clientAddress, int clientPort, String id, int sequenceNumber) {
        this.id = id;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.sequenceNumber = new AtomicInteger(sequenceNumber + 1);
        unresponsiveReasons = new HashSet<>();
    }

    public abstract String getStatus();

    public void updateStatus(S newStatus, String clientType) {
        this.status = newStatus;
        logger.log(Level.INFO, "Updated status for {0}{1} to {2}", new Object[] {clientType, id, newStatus});
    }

    public void sendExecuteMessage(A action, String clientType) {
        String message = MessageGenerator.generateExecuteMessage(clientType, id, sequenceNumber.getAndIncrement(), String.valueOf(action));
        sendMessage(message, "EXEC");
    }

    public void sendAcknowledgeMessage(String clientType, MessageEnums.AKType akType) {
        String message = MessageGenerator.generateAcknowledgeMessage(clientType, id, sequenceNumber.getAndIncrement(), akType);
        sendMessage(message, String.valueOf(akType));
        registered = true;
    }

    public void sendStatusMessage(String clientType) {
        String message = MessageGenerator.generateStatusMessage(clientType, id, sequenceNumber.getAndIncrement());
        sendMessage(message, "STAT");
    }

    public void sendMessage(String message, String type) {
        lastMessageSent = type;
        Server.getInstance().sendMessageToClient(this, message, type);
    }

    public void registerClient(String clientType) {
        Database.getInstance().addClient(this.id, this);
        logger.log(Level.INFO, "Added new {0} to database", clientType);
    }

    public void addReason(ReasonEnum reason) {
        unresponsiveReasons.add(reason);
        logger.log(Level.INFO, "Added reason {0}", reason);
    }

    public Set<ReasonEnum> getUnresponsiveReasons() {
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

    public String getLastMessageSent() {
        return lastMessageSent;
    }
}
