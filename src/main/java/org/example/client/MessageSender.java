package org.example.client;

import org.example.events.EventBus;
import org.example.events.SendPacketEvent;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageSender {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final InetAddress clientAddress;
    private final int clientPort;
    private final String clientId;
    private EventBus eventBus;

    public MessageSender(InetAddress clientAddress, int clientPort,
                         String clientID, EventBus eventBus) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.clientId = clientID;
        this.eventBus = eventBus;
    }

    public void send(String message, String type) {
        eventBus.publish(new SendPacketEvent(clientAddress, clientPort, clientId, message, type));
    }
}
