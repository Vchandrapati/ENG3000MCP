package org.example;

import java.net.InetAddress;
import java.util.logging.Level;

public class StationClient extends Client<MessageEnums.STCStatus, MessageEnums.STCAction> {
    private final int location;

    public StationClient(InetAddress clientAddress, int clientPort, String id, int sequenceNumber, int location) {
        super(clientAddress, clientPort, id, sequenceNumber);
        this.location = location;
    }

    public int getLocation() {
        return location;
    }

    @Override
    public String getStatus() {
        return status.toString();
    }

    public void updateStatus(MessageEnums.STCStatus newStatus) {
        super.updateStatus(newStatus, "ST");
    }

    public void sendExecuteMessage(MessageEnums.STCAction action) {
        super.sendExecuteMessage(action, "STC");
    }

    public void registerClient() {
        super.registerClient("Station");
    }
}
