package org.example.client;

import org.example.messages.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractClient<S extends Enum<S>, A extends Enum<A>> {
    protected static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected MessageGenerator messageGenerator;
    protected MessageSender messageSender;
    protected final String id;

    A lastActionSent;
    S currentStatus;
    protected String type;
    protected String lastExecMessageSent;

    private String lastResponse;
    AtomicInteger outgoingSequenceNumber = new AtomicInteger(0);
    AtomicInteger incomingSequenceNumber;
    protected int latestStatusMessage;
    private final AtomicInteger missedStats;
    protected boolean expectingStat;

    protected int expectingAKEXByThis;

    protected boolean registered = false;
    protected Set<ReasonEnum> unresponsiveReasons;

    protected AbstractClient(String id, MessageGenerator messageGenerator,
            MessageSender messageSender, int sequenceNumber) {
        this.id = id;
        this.messageGenerator = messageGenerator;
        this.messageSender = messageSender;

        this.latestStatusMessage = -1;
        this.missedStats = new AtomicInteger(0);
        this.lastActionSent = null;
        this.expectingStat = false;

        expectingAKEXByThis = Integer.MAX_VALUE;

        unresponsiveReasons = new HashSet<>();
    }

    public void expectingAKEXBy(int nowSequence) {
        expectingAKEXByThis = nowSequence + 2;
    }

    public boolean isMissedAKEX(int curSequence) {
        if (curSequence >= expectingAKEXByThis) {
            return true;
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public S getStatus() {
        return currentStatus;
    }

    public void updateStatus(S newStatus) {
        this.currentStatus = newStatus;
        logger.log(Level.INFO, "Updated status for {0} to {1}", new Object[] {id, newStatus});
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
        String message = messageGenerator.generateAcknowledgeMessage(type, id,
                outgoingSequenceNumber.getAndIncrement(), akType);
        sendMessage(message, String.valueOf(akType));
        registered = true;
    }

    public void sendStatusMessage() {
        String message = messageGenerator.generateStatusMessage(type, id,
                outgoingSequenceNumber.getAndIncrement());
        sendMessage(message, "STAT");
    }

    public void sendMessage(String message, String type) {
        messageSender.send(message, type);
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
