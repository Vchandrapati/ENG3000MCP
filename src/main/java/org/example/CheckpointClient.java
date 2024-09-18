package org.example;

import java.net.InetAddress;

public class CheckpointClient extends Client {
    private final Integer location;
    protected Integer intID;
    private Status status;
    private boolean tripped;

    private enum Status {
        ALIVE,
        DEAD
    }

    public CheckpointClient(InetAddress clientAddress, int clientPort, String id) {
        super(clientAddress, clientPort, id);
        location = 0;
        String idString = id.substring(2);
        intID = Integer.parseInt(idString);
        status = Status.ALIVE;
        this.tripped = false;
    }

    public Integer getIntID() {
        return intID;
    }

    public Integer getLocation() {
        return location;
    }

    @Override
    public void sendStatusMessage(long timestamp) {
        String message = MessageGenerator.generateStatusMessage("ccp", id, System.currentTimeMillis());
        sendMessage(message, "STAT");
    }

    public void sendAcknowledgeMessage() {
        String message = MessageGenerator.generateAcknowledgesMessage("checkpoint", id, System.currentTimeMillis());
        sendMessage(message, "ACK");
        registered = true;
    }

    @Override
    public void registerClient() {
        Database.getInstance().addClient(this.id, this);
    }

    public void setTripped() {
        this.tripped = true;
    }

    public void reset() {
        this.tripped = false;
    }

    public boolean isTripped() {
        return tripped;
    }
}
