package org.example;

import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

public class TrainClient extends Client {
    private volatile Integer zone;
    private volatile Status status;

    private enum Status {
        STOPPED,
        STARTED,
        ON,
        OFF,
        ERR,
        CRASH,
        STOPPED_AT_STATION
    }

    public TrainClient(InetAddress clientAddress, int clientPort, String id) {
        super(clientAddress, clientPort, id);
    }

    public Integer getZone() {
        return zone;
    }

    public void updateStatus(String newStatus) {
        try {
            status = Status.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            logger.severe(String.format("Tried to assign unknown status: %s for train %s", newStatus, id));
        }
    }

    public void sendExecuteMessage(int speed) {
        String message = MessageGenerator.generateExecuteMessage("ccp", id, System.currentTimeMillis(), speed);
        sendMessage(message);
    }

    public void sendAcknowledgeMessage() {
        String message = MessageGenerator.generateAcknowledgesMessage("ccp", id, System.currentTimeMillis());
        sendMessage(message);
    }

    public void sendDoorMessage(boolean doorOpen) {
        String message = MessageGenerator.generateDoorMessage("ccp", id, System.currentTimeMillis(), doorOpen);
        sendMessage(message);
    }

    @Override
    public void registerClient() {
        Database.getInstance().addTrain(this.id, this);
    }

    public void changeZone(int zone) {
        this.zone = zone;
    }
}
