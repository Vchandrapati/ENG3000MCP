package org.example.client;

import org.example.events.EventBus;
import org.example.events.SendPacketEvent;

import java.net.InetAddress;

public class MessageSender {
    private final InetAddress clientAddress;
    private final int clientPort;
    private final String clientId;
    private final EventBus eventBus;

    public MessageSender (InetAddress clientAddress, int clientPort,
                          String clientID, EventBus eventBus) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.clientId = clientID;
        this.eventBus = eventBus;
    }

    public void send (String message, String type) {
        eventBus.publish(new SendPacketEvent(clientAddress, clientPort, clientId, message, type));
    }
}
