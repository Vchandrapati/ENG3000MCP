package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;

public class MessageHandler {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Database db = Database.getInstance();

    // Handles messages from CCPs and stations
    public void handleMessage(String message) {
        try {
            ReceiveMessage receiveMessage = objectMapper.readValue(message, ReceiveMessage.class);
            // Handle based on the client type
            switch (receiveMessage.clientType) {
                case "ccp":
                    handleCCPMessage(receiveMessage);
                    break;
                case "station":
                    handleStationMessage(receiveMessage);
                    break;
                case "checkpoint":
                    handleCheckpointMessage(receiveMessage);
                    break;
                default:
                    logger.log(Level.WARNING, "Unknown client type: {0}", receiveMessage.clientType);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to handle message: {0}", e.getMessage());
        }
    }

    // Handles all checkpoint messages
    public void handleCheckpointMessage(ReceiveMessage message) {
        CheckpointClient client = (CheckpointClient) db.getClient(message.clientID);

        Processor processor = new Processor();
        switch (message.message) {
            case "TRIP":
                // location is fucked
                processor.sensorTripped(Integer.parseInt(message.location));
                break;
            case "STIN":
                // TODO
                break;
            case "STAT":
                client.setStatReturned(true);
                logger.log(Level.INFO, "Received STAT command from Checkpoint: {0}", message.clientID);
                break;
            default:
                logger.log(Level.SEVERE, "Failed to handle checkpoint message: {0}", message);
                break;
        }
    }

    private void handleCCPMessage(ReceiveMessage receiveMessage) {
        TrainClient client;

        switch (receiveMessage.message) {
            case "STAT":
                client = (TrainClient) db.getClient(receiveMessage.clientID);
                client.updateStatus(receiveMessage.status.toUpperCase());
                client.setStatReturned(true);
                client.setStatSent(true);
                logger.log(Level.INFO, "Received STAT message from Blade Runner: {0}", receiveMessage.clientID);
                break;
        }
        if (receiveMessage.message.equals("STAT")) {

        } else {
            logger.log(Level.WARNING, "Unknown Blade Runner message: {0}", receiveMessage.message);
        }
    }

    private void handleStationMessage(ReceiveMessage receiveMessage) {
        StationClient client = (StationClient) db.getClient(receiveMessage.clientID);
        // Different behaviour based on what the message command is
        switch (receiveMessage.message) {
            case "DOOR":
                client.updateStatus(receiveMessage.status.toUpperCase());
                logger.log(Level.INFO, "Received STIN message from Station: {0}", receiveMessage.clientID);
                break;
            case "STAT":
                client.setStatReturned(true);
                logger.log(Level.INFO, "Received STAT message from Station: {0}", receiveMessage.clientID);
                break;
            default:
                logger.log(Level.WARNING, "Unknown Station message: {0}", receiveMessage.clientID);
        }
    }

    // Deprecated - Eugene
    public void handleInitialise(String message, InetAddress ip, int port) {
        try {
            ReceiveMessage receiveMessage = objectMapper.readValue(message, ReceiveMessage.class);
            // Handle based on the client type
            switch (receiveMessage.clientType) {
                case "ccp":
                    if (receiveMessage.message.equals("STAT")) {
                        break;
                    }
                    TrainClient client = new TrainClient(ip, port, receiveMessage.clientID);
                    client.id = receiveMessage.clientID;
                    client.registerClient();
                    client.sendAcknowledgeMessage();
                    logger.log(Level.INFO, "Received CCIN message from CCP and created new client: {0}",
                            receiveMessage.clientID);
                    break;

                default:
                    logger.log(Level.INFO, "Unknown client type: {0}", receiveMessage.clientType);
                    break;
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to handle message: {0}", e.getMessage());
        }
    }
}
