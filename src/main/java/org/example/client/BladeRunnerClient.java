package org.example.client;

import java.util.concurrent.atomic.AtomicInteger;

import org.example.messages.MessageEnums;
import org.example.messages.MessageEnums.CCPAction;
import org.example.messages.MessageSender;

public class BladeRunnerClient extends AbstractClient<MessageEnums.CCPStatus, CCPAction> {
    private final AtomicInteger zone = new AtomicInteger();
    private volatile boolean isCurrentlyMapped;
    private volatile boolean collision;
    private volatile boolean dockedAtStation;


    public BladeRunnerClient (String id, MessageGenerator messageGenerator,
                              MessageSender messageSender, int initialSequenceNumber) {
        super(id, messageGenerator, messageSender, initialSequenceNumber);
        this.updateStatus(MessageEnums.CCPStatus.STOPC);
        this.type = "BR";
        this.isCurrentlyMapped = false;
        this.dockedAtStation = false;
    }

    public Integer getZone() {
        return zone.get();
    }

    public void changeZone(int zone) {
        this.zone.set(zone);
        isCurrentlyMapped = true;
    }

    public boolean isUnmapped() {
        return !isCurrentlyMapped;
    }

    public boolean collision(boolean hasCollide, Object o) {
        if (o != null) {
            collision = hasCollide;
        }
        return collision;
    }

    public boolean isDockedAtStation() {
        return dockedAtStation;
    }

    public void setDockedAtStation(Boolean b) {
        dockedAtStation = b;
    }

    @Override
    public void sendExecuteMessage(CCPAction action) {
        this.lastActionSent = action;
        String message = messageGenerator.generateExecuteMessage(type, super.getId(),
                sequenceNumberManager.getNextOutgoingSequenceNumber(), action.toString());
        sendMessage(message, "EXEC");

    }
}
