package org.example;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class CheckpointClient extends Client {
    private Integer location;
    protected Integer intID;
    private Status status;
    private boolean tripped;

    private enum Status {
        Alive,
        Dead
    }

    public CheckpointClient(InetAddress clientAddress, int clientPort, String id) {
        super(clientAddress, clientPort, id);
        location = 0;
        String idString = id.substring(2);
        intID = Integer.parseInt(idString);
        status = Status.Alive;
        this.tripped = false;
    }

    public Integer getIntID(){
        return intID;
    }
    
    public Integer getLocation() {
        return location;
    }

    // Uses an alternate process packet method as the messages are strings
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
