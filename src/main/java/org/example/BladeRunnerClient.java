package org.example;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.MessageEnums.CCPAction;

public class BladeRunnerClient extends Client<MessageEnums.CCPStatus, MessageEnums.CCPAction> {
    private final AtomicInteger zone = new AtomicInteger();
    private volatile boolean isCurrentlyMapped;
    private volatile boolean collision;
    private volatile boolean dockedAtstation;


    public BladeRunnerClient(InetAddress clientAddress, int clientPort, String id,
            int sequenceNumber) {
        super(clientAddress, clientPort, id, sequenceNumber);
        // Everyone starts like this but maybe they dont is the thing
        this.updateExpectedStatus(MessageEnums.CCPStatus.STOPC);
        this.isCurrentlyMapped = false;
        this.type = "BR";
        this.dockedAtstation = false;
    }

    public Integer getZone() {
        return zone.get();
    }

    public void changeZone(int zone) {
        this.zone.set(zone);
        isCurrentlyMapped = true;
    }

    public void unmap() {
        isCurrentlyMapped = false;
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
        return dockedAtstation;
    }

    public void setDockedAtStation(Boolean b) {
        dockedAtstation = b;
    }

    @Override
    public void sendExecuteMessage(CCPAction action) {
        this.lastActionSent = action;
        lastExecMessageSent = "EXEC " + action.toString();
        String message = MessageGenerator.generateExecuteMessage(type, super.getId(),
                sequenceNumberOutgoing.getAndIncrement(), String.valueOf(action));
        sendMessage(message, "EXEC");

    }
}
