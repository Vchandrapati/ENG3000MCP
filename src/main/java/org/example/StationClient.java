package org.example;

import java.net.InetAddress;

public class StationClient extends CheckpointClient {
    private volatile DoorStatus status;

    private enum DoorStatus {
        OPEN,
        CLOSE
    }

    public StationClient(InetAddress clientAddress, int clientPort, String id, int location) {
        super(clientAddress, clientPort, id, location);
    }

    public void updateStatus(String newStatus) {
        try {
            status = StationClient.DoorStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            logger.severe(String.format("Tried to assign unknown status: %s for train %s", newStatus, id));
        }
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
    public void registerClient() {
        Database.getInstance().addClient(this.id, this);
        logger.info("Added new station to database: " + Database.getInstance().getStationCount());
    }
}