package org.example;

import java.net.InetAddress;
import java.net.Socket;

public class StationClient extends Client {
    private volatile Integer location;
    private volatile Status status;

    private enum Status {
        Alive,
        Dead
    }

    public StationClient(InetAddress clientAddress, int clientPort, String id) {
        super(clientAddress, clientPort, id);
        location = 0;
    }

    public Integer getLocation() {
        return location;
    }

    public void sendDoorMessage(boolean doorOpen) {
        String message = MessageGenerator.generateDoorMessage("station", id, System.currentTimeMillis(), doorOpen);
        sendMessage(message);
    }

    public void sendIRLEDMessage(boolean LEDOn) {
        String message = MessageGenerator.generateIRLEDMessage("station", id, System.currentTimeMillis(), LEDOn);
        sendMessage(message);
    }
}
