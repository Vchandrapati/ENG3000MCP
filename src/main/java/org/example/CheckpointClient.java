package org.example;

import java.net.DatagramPacket;
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
    public void sendStatusMessage(String id, Long timestamp) {
        String message = MessageGenerator.generateStatusMessage("ccp", id, System.currentTimeMillis());
        sendMessage(message, "STAT");
    }

    @Override
    public void registerClient() {
        Database.getInstance().addClient(this.id, this, super.getClientAddress(), super.getClientPort() + "");
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
