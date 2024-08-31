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

    @Override
    public void start() {
        new Thread(this::readWrapper).start();
    }

    public void readWrapper() {
        while (running) {
            String input = readMessage();
            if (input != null && !input.isEmpty()) {
                // Use MessageHandler to process the message
                messageHandler.handleMessage(input);
            }
        }
    }

    public Integer getLocation() {
        return location;
    }
}
