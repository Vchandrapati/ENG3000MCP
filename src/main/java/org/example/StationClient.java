package org.example;

import java.net.Socket;

public class StationClient extends Client {
    private volatile Integer location;
    private volatile Status status;

    private enum Status {
        Alive,
        Dead
    }

    public StationClient(Socket clientSocket, String id, Integer loc) {
        super(clientSocket, id);
        location = loc;
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
