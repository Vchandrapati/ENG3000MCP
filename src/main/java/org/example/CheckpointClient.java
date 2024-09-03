package org.example;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class CheckpointClient extends Client {
    private Integer location;
    private Status status;
    private boolean tripped;

    private enum Status {
        Alive,
        Dead
    }

    public CheckpointClient(InetAddress clientAddress, int clientPort, String id) {
        super(clientAddress, clientPort, id);
        location = 0;
        status = Status.Alive;
        this.tripped = false;
    }

    public Integer getLocation() {
        return location;
    }

    @Override
    public void processPacket(DatagramPacket packet) {
        String message = new String(packet.getData(), 0, packet.getLength());
        if (!message.isEmpty()) {
            messageHandler.handleCheckpointMessage(message);
        }
    }

    @Override
    public void registerClient() {
        Database.getInstance().addCheckpoint(this.id, this);
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
