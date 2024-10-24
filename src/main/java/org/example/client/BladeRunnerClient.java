package org.example.client;

import org.example.messages.MessageEnums;
import org.example.messages.MessageEnums.CCPAction;

import java.util.concurrent.atomic.AtomicInteger;

public class BladeRunnerClient extends AbstractClient<MessageEnums.CCPStatus, CCPAction> {
    private final AtomicInteger zone = new AtomicInteger();
    private volatile boolean isCurrentlyMapped;
    private volatile boolean collision;
    private volatile boolean dockedAtStation;


    public BladeRunnerClient (String id, MessageGenerator messageGenerator,
                              MessageSender messageSender, int sequenceNumber) {
        super(id, messageGenerator, messageSender, sequenceNumber);
        this.updateStatus(MessageEnums.CCPStatus.STOPC);
        this.type = "CCP";
        this.isCurrentlyMapped = false;
        this.dockedAtStation = false;
    }


    public Integer getZone () {
        return zone.get();
    }

    public void changeZone (int zone) {
        this.zone.set(zone);
        isCurrentlyMapped = true;
    }

    public boolean isUnmapped () {
        return !isCurrentlyMapped;
    }

    public boolean getCollision () {
        return collision;
    }

    public void setCollision (boolean hasCollide) {
        collision = hasCollide;
    }

    public boolean isDockedAtStation () {
        return dockedAtStation;
    }

    public void setDockedAtStation (Boolean b) {
        dockedAtStation = b;
    }

    @Override
    public void sendExecuteMessage (CCPAction action) {
        this.lastActionSent = action;
        updateStatus(action.getStatus());
        String message = messageGenerator.generateExecuteMessage(type, super.getId(),
                outgoingSequenceNumber.getAndIncrement(), action.toString());
        sendMessage(message, "EXEC");

    }
}
