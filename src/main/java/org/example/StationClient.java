package org.example;

import java.net.InetAddress;
import java.net.Socket;

public class StationClient extends Client {
    private volatile Integer location;
    private volatile DoorStatus status;

    private enum DoorStatus {
        OPEN,
        CLOSE
    }

    public StationClient(InetAddress clientAddress, int clientPort, String id) {
        super(clientAddress, clientPort, id);
        location = 0;
    }

    public void updateStatus(String newStatus) {
        try {
            status = StationClient.DoorStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            logger.severe(String.format("Tried to assign unknown status: %s for train %s", newStatus, id));
        }
    }

    public Integer getLocation() {
        return location;
    }

    // For sending a message about updating the status of the door
    // True: Door is open
    // False: Door is closed
    public void sendDoorMessage(boolean doorOpen) {
        String message = MessageGenerator.generateDoorMessage("station", id, System.currentTimeMillis(), doorOpen);
        sendMessage(message);
    }

    // To tell the station what the status of the LED should be
    // True: LED is on
    // False: LED is off
    public void sendIRLEDMessage(boolean LEDOn) {
        String message = MessageGenerator.generateIRLEDMessage("station", id, System.currentTimeMillis(), LEDOn);
        sendMessage(message);
    }

    @Override
    public void registerClient() {
        Database.getInstance().addStation(this.id, this);
    }
}
