package org.example;

import java.net.InetAddress;

public class StationClient extends CheckpointClient {
    private volatile DoorStatus status;

    private enum DoorStatus {
        OPEN,
        CLOSE
    }

    public StationClient(InetAddress clientAddress, int clientPort, String id) {
        super(clientAddress, clientPort, id);
    }

    public void updateStatus(String newStatus) {
        try {
            status = StationClient.DoorStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            logger.severe(String.format("Tried to assign unknown status: %s for train %s", newStatus, id));
        }
    }

    public Integer getLocation() {
        return super.getLocation();
    }

    // For sending a message about updating the status of the door
    // True: Door is open
    // False: Door is closed
    public void sendDoorMessage(boolean doorOpen) {
        String message = MessageGenerator.generateDoorMessage("station", id, System.currentTimeMillis(), doorOpen);
        sendMessage(message, "DOOR");
    }

    // To tell the station what the status of the LED should be
    // True: LED is on
    // False: LED is off
    public void sendIRLEDMessage(boolean on) {
        String message = MessageGenerator.generateIRLEDMessage("station", id, System.currentTimeMillis(), on);
        sendMessage(message, "IRLED");
    }

    @Override
    public void sendStatusMessage(String id, Long timestamp) {
        String message = MessageGenerator.generateStatusMessage("ccp", id, System.currentTimeMillis());
        sendMessage(message, "STAT");
    }

    @Override
    public void registerClient() {
        Database.getInstance().addStation(this.id, this);
    }
}