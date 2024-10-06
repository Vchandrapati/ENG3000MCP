package org.example;

import java.net.InetAddress;
import java.util.logging.Level;

public class CheckpointClient extends Client<MessageEnums.CPCStatus, MessageEnums.CPCAction> {
    private boolean tripped;
    private final int location;

    public CheckpointClient(InetAddress clientAddress, int clientPort, String id,
            int sequenceNumber, int location) {
        super(clientAddress, clientPort, id, sequenceNumber);
        this.location = location;
        this.tripped = false;
    }

    public int getLocation() {
        return location;
    }

    @Override
    public String getExpectedStatus() {
        return expectedStatus.toString();
    }

    public void updateStatus(MessageEnums.CPCStatus newStatus) {
        super.updateStatus(newStatus, "CP");
    }

    public void sendExecuteMessage(MessageEnums.CPCAction action) {
        super.sendExecuteMessage(action, "CPC");
    }

    public void sendStatusMessage() {
        super.sendStatusMessage("CPC");
    }

    public void sendAcknowledgeMessage(String clientType, MessageEnums.AKType akType) {
        super.sendAcknowledgeMessage(clientType, akType);
    }

    public void registerClient() {
        super.registerClient("Checkpoint");
    }

    // public void setTripped() {
    // this.tripped = true;
    // }

    // public void resetTrip() {
    // this.tripped = false;
    // }

    // public boolean isTripped() {
    // return tripped;
    // }
}
