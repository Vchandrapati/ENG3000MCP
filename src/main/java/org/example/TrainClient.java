package org.example;

import java.net.Socket;

public class TrainClient extends Client {
    private volatile Integer zone;
    private volatile Status status;

    private enum Status {
        Alive,
        Dead
    }

    public TrainClient(Socket clientSocket, String id) {
        super(clientSocket, id);
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

    public Integer getZone() {
        return zone;
    }

    public void changeStatusToDead() {
        status = Status.Dead;
    }

    public void changeStatusToAlive() {
        status = Status.Alive;
    }
}
