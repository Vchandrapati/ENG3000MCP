package org.example.events;

import java.net.InetAddress;

public class SendPacketEvent implements Event {
    private final InetAddress clientAddress;
    private final int clientPort;
    private final String id;
    private final String message;
    private final String type;

    public SendPacketEvent(InetAddress clientAddress, int clientPort, String id, String message,
                           String type) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.id = id;
        this.message = message;
        this.type = type;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public int getClientPort() {
        return clientPort;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
}
