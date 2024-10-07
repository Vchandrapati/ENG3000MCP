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
import org.example.MessageEnums.CCPStatus;

public abstract class Client<S extends Enum<S>, A extends Enum<A>> {
    protected static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    S expectedStatus;
    private final String id;
    private final InetAddress clientAddress;
    private final int clientPort;

    protected AtomicInteger sequenceNumberOutgoing;
    protected AtomicInteger sequenceNumberIncoming;
    protected Integer latestStatusMessage;
    private AtomicInteger missedStats;

    protected HashMap<Integer, String> incomingMessages;
    protected HashMap<Integer, String> outgoingMessages;

    protected boolean registered = false;
    protected Set<ReasonEnum> unresponsiveReasons;

    protected Client(InetAddress clientAddress, int clientPort, String id, int sequenceNumber) {
        this.id = id;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;

        // May or may not need
        this.sequenceNumberIncoming = new AtomicInteger(sequenceNumber);
        this.sequenceNumberOutgoing = new AtomicInteger(0);
        this.latestStatusMessage = -1;
        this.missedStats = new AtomicInteger(0);

        incomingMessages = new HashMap<>();
        outgoingMessages = new HashMap<>();
        unresponsiveReasons = new HashSet<>();
    }

    // Why is this needed? -Eugene
    public abstract String getExpectedStatus();

    public boolean isUnresponsive() {
        if (missedStats.get() >= 3) {
            return true;
        }
        return false;
    }

    public void updateStatus(S newStatus, String clientType) {
        this.expectedStatus = newStatus;
        logger.log(Level.INFO, "Updated status for {0} to {1}", new Object[] {id, newStatus});
    }

    public S getStatus() {
        return expectedStatus;
    }

    public void updateLatestStatusMessageCount(Integer count) {
        latestStatusMessage = count;
    }

    public void incrementMissedStats() {
        this.missedStats.getAndIncrement();
    }

    // Is this fine vikil? - Eugene
    public void resetMissedStats() {
        this.missedStats = new AtomicInteger(0);
    }

    public Integer getLatestStatusMessageCount() {
        return latestStatusMessage;
    }

    public void sendExecuteMessage(A action, String clientType) {
        String message = MessageGenerator.generateExecuteMessage(clientType, id,
                sequenceNumberOutgoing.getAndIncrement(), String.valueOf(action));
        sendMessage(message, "EXEC");
    }

    public void sendAcknowledgeMessage(String clientType, MessageEnums.AKType akType) {
        String message = MessageGenerator.generateAcknowledgeMessage(clientType, id,
                sequenceNumberOutgoing.getAndIncrement(), akType);
        sendMessage(message, String.valueOf(akType));
        registered = true;
    }

    public void sendStatusMessage(String clientType) {
        String message = MessageGenerator.generateStatusMessage(clientType, id,
                sequenceNumberOutgoing.getAndIncrement());
        sendMessage(message, "STAT");
    }

    public void sendMessage(String message, String type) {
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
        return incomingMessages.get(sequenceNumberIncoming.get());
    }
}
