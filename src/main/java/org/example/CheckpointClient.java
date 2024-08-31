package org.example;

import java.net.InetAddress;
import java.net.Socket;

public class CheckpointClient extends Client {
    private volatile Integer location;
    private volatile Status status;

    private enum Status {
        Alive,
        Dead
    }

    public CheckpointClient(InetAddress clientAddress, int clientPort, String id) {
        super(clientAddress, clientPort, id);
        location = 0;
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
