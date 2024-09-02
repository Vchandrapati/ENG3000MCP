package org.example;

import java.net.InetAddress;
import java.net.Socket;

public class TrainClient extends Client {
    private volatile Integer zone;
    private volatile Status status;

    private enum Status {
        Alive,
        Dead
    }

    public TrainClient(InetAddress clientAddress, int clientPort, String id) {
        super(clientAddress, clientPort, id);
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

    public void sendExecuteMessage(int speed) {
        String message = MessageGenerator.generateExecuteMessage("ccp", id, System.currentTimeMillis(), speed);
        sendMessage(message);
    }

    public void sendAcknowledgeMessage() {
        String message = MessageGenerator.generateAcknowledgesMessage("ccp", id, System.currentTimeMillis());
        sendMessage(message);
    }

    public void sendDorrMessage(boolean doorOpen) {
        String message = MessageGenerator.generateDoorMessage("ccp", id, System.currentTimeMillis(), doorOpen);
        sendMessage(message);
    }
}
