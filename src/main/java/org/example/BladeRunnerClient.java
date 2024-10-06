package org.example;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class BladeRunnerClient extends Client<MessageEnums.CCPStatus, MessageEnums.CCPAction> {
    private final AtomicInteger zone = new AtomicInteger();
    private volatile boolean isCurrentlyMapped;
    private volatile boolean collision;

    public BladeRunnerClient(InetAddress clientAddress, int clientPort, String id,
            int sequenceNumber) {
        super(clientAddress, clientPort, id, sequenceNumber);
        this.isCurrentlyMapped = false;
    }

    public Integer getZone() {
        return zone.get();
    }

    @Override
    public String getExpectedStatus() {
        return expectedStatus.toString();
    }

    public void updateStatus(MessageEnums.CCPStatus newStatus) {
        super.updateStatus(newStatus, "BR");
    }

    public void sendExecuteMessage(MessageEnums.CCPAction action) {
        super.sendExecuteMessage(action, "CCP");
    }

    public void sendAcknowledgeMessage(MessageEnums.AKType akType) {
        super.sendAcknowledgeMessage("CCP", akType);
    }

    public void sendStatusMessage() {
        super.sendStatusMessage("CCP");
    }

    public void registerClient() {
        super.registerClient("Blade Runner");
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
}
