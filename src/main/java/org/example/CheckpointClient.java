package org.example;

import java.net.DatagramPacket;
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

    public Integer getLocation() {
        return location;
    }

    public void sendStatus() {
        sendMessage("status");
    }

    @Override
    public void processPacket(DatagramPacket packet) {
        String message = new String(packet.getData(), 0, packet.getLength());
        if (!message.isEmpty()) {
            messageHandler.handleChekcpointMessage(message);
        }
    }
}
