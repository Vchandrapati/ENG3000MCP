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
