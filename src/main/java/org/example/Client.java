package org.example;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Client<S extends Enum<S>, A extends Enum<A>> {
    protected static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    A lastActionSent;
    S currentStatus;
    private final String id;
    private final InetAddress clientAddress;
    private final int clientPort;
    protected String type;
    protected String lastExecMessageSent;
    private String lastResponse;

    protected AtomicInteger sequenceNumberOutgoing;
    protected AtomicInteger sequenceNumberIncoming;
    protected Integer latestStatusMessage;
    private final AtomicInteger missedStats;
    protected boolean expectingStat;

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
        this.lastActionSent = null;
        this.expectingStat = false;

        incomingMessages = new HashMap<>();
        outgoingMessages = new HashMap<>();
        unresponsiveReasons = new HashSet<>();

        this.startupSequence();
    }

    public void startupSequence() {
        this.registerClient();
        this.sendAcknowledgeMessage(MessageEnums.AKType.AKIN);
        logger.log(Level.INFO, "Initialised new {0}: {1}", new Object[] {this.type, this.id});
    }

    public boolean checkResponsive() {
        this.missedStats.getAndIncrement();
        if (missedStats.get() >= 3) {
            addReason(ReasonEnum.NOSTAT);
            return true;
        }

        return false;
    }

    public boolean isExpectingStat() {
        return expectingStat;
    }

    public void noLongerExpectingStat() {
        expectingStat = false;
    }

    public void nowExpectingStat() {
        expectingStat = true;
    }

    public void resetMissedStats() {
        this.missedStats.set(0);
    }

    public void updateExpectedStatus(S newStatus) {
        this.currentStatus = newStatus;
        logger.log(Level.INFO, "Updated status for {0} to {1}", new Object[] {id, newStatus});
    }

    public S getStatus() {
        return currentStatus;
    }

    public A getLastActionSent() {
        return lastActionSent;
    }

    public void updateLatestStatusMessageCount(Integer count) {
        latestStatusMessage = count;
    }

    public Integer getLatestStatusMessageCount() {
        return latestStatusMessage;
    }

    protected abstract void sendExecuteMessage(A action);

    public void sendAcknowledgeMessage(MessageEnums.AKType akType) {
        String message = MessageGenerator.generateAcknowledgeMessage(type, id,
                sequenceNumberOutgoing.getAndIncrement(), akType);
        sendMessage(message, String.valueOf(akType));
        registered = true;
    }

    public void sendStatusMessage() {
        String message = MessageGenerator.generateStatusMessage(type, id,
                sequenceNumberOutgoing.getAndIncrement());
        sendMessage(message, "STAT");
    }

    public void sendMessage(String message, String type) {
        Server.getInstance().sendMessageToClient(this, message, type);
    }

    public void registerClient() {
        Database.getInstance().addClient(this.id, this);
        logger.log(Level.INFO, "Added new {0} to database", type);
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

    public String getLastExecMessageSent() {
        return lastExecMessageSent;
    }

    public int getSequenceCount() {
        return sequenceNumberOutgoing.get();
    }

    public int getMissedStatCount() {
        return missedStats.get();
    }

    public void setLastResponse(String lastResponse) {
        this.lastResponse = lastResponse;
    }

    public String getLastResponse() {
        return lastResponse;
    }
}
