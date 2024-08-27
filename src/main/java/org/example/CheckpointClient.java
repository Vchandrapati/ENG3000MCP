package org.example;

import java.net.Socket;

public class CheckpointClient extends Client {
    private volatile Integer location;
    private volatile Status status;

    private enum Status {
        Alive,
        Dead
    }

    public CheckpointClient(Socket clientSocket, String id, Integer loc) {
        super(clientSocket, id);
        location = location;
        status = Status.Alive;
    }

    @Override
    public void start() {
        new Thread(this::readWrapper).start();
    }

    public void readWrapper() {
        while (running) {
            String input = readMessage();
            if (!input.isEmpty()) {

            }
        }
    }

    public Integer getLocation() {
        return location;
    }
}
