package org.example;

import java.net.InetAddress;

public class CheckpointClient extends Client {
    private final Integer location;
    private final Status status;
    private boolean tripped;

    public CheckpointClient(InetAddress clientAddress, int clientPort, String id, int location) {
        super(clientAddress, clientPort, id);
        this.location = location;
        status = Status.ALIVE;
        this.tripped = false;
    }

    public Integer getLocation() {
        return location;
    }

    @Override
    public void sendStatusMessage(long timestamp) {
        String message = MessageGenerator.generateStatusMessage("ccp", id, System.currentTimeMillis());
        sendMessage(message, "STAT");
    }

    @Override
    public void sendAcknowledgeMessage() {
        String message = MessageGenerator.generateAcknowledgesMessage("checkpoint", id, System.currentTimeMillis());
        sendMessage(message, "ACK");
        registered = true;
    }

    @Override
    public void registerClient() {
        Database.getInstance().addClient(this.id, this);
        logger.info("Added new checkpoint to database: " + Database.getInstance().getCheckpointCount());
    }

    public void setTripped() {
        this.tripped = true;
    }

    public void resetTrip() {
        this.tripped = false;
    }

    public boolean isTripped() {
        return tripped;
    }

    private enum Status {
        ALIVE,
        DEAD
    }
}
