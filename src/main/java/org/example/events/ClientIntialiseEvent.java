package org.example.events;

import org.example.messages.ReceiveMessage;

import java.net.InetAddress;

public class ClientIntialiseEvent implements Event {
    private final ReceiveMessage receiveMessage;
    private final InetAddress address;
    private final int port;

    public ClientIntialiseEvent(ReceiveMessage receiveMessage, InetAddress address, int port) {
        this.receiveMessage = receiveMessage;
        this.address = address;
        this.port = port;
    }

    public ReceiveMessage getReceiveMessage() {
        return receiveMessage;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
