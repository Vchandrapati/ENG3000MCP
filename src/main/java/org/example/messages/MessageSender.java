package org.example.messages;

import java.net.InetAddress;

public class MessageSender {
    private final Server server;
    private final InetAddress clientAddress;
    private final int clientPort;
    private final String clientId;

    public MessageSender(Server server, InetAddress clientAddress, int clientPort,
                         String clientID) {
        this.server = server;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.clientId = clientID;
    }

    public void send(String message, String messageType) {
        server.sendMessageToClient(clientAddress, clientPort, message, messageType, clientId);
    }
}
