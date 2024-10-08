package org.example;

import java.net.InetAddress;

public class CheckpointClient extends Client<MessageEnums.CPCStatus, MessageEnums.CPCAction> {
    private boolean tripped;
    private final int location;

    public CheckpointClient(InetAddress clientAddress, int clientPort, String id, int sequenceNumber, int location) {
        super(clientAddress, clientPort, id, sequenceNumber);
        this.updateStatus(MessageEnums.CPCStatus.OFF);
        this.location = location;
        this.tripped = false;
        this.type = "CPC";
    }

    public int getLocation() {
        return location;
    }

    @Override
    public String getExpectedStatus() {
        return expectedStatus.toString();
    }

    public void sendExecuteMessage(MessageEnums.CPCAction action) {
        this.updateStatus(MessageEnums.convertActionToStatus(action));
        super.sendExecuteMessage(action);
    }

    public void registerClient() {
        super.registerClient();
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
