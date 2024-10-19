package org.example.messages;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageSender {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final InetAddress clientAddress;
    private final int clientPort;
    private final String clientId;
    private DatagramSocket serverSocket;

    public MessageSender(InetAddress clientAddress, int clientPort,
                         String clientID) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.clientId = clientID;

        try {
            serverSocket = new DatagramSocket(3001);
            logger.info("Server completed startup and listening on PORT: " + 3001);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting up server", e);
        }
    }

    public void send(String message, String type) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            serverSocket.send(sendPacket);
            logger.log(Level.INFO, "Sent {0} to client at: {1}", new Object[] {type, clientId});
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send message to client: {0}", clientId);
            logger.log(Level.SEVERE, "Exception: ", e);
        }
    }
}
