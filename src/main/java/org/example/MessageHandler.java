package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Logger;

public class MessageHandler {
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Database db = Database.getInstance();

    // Handles messages from CCPs and stations
    public void handleMessage(String message) {
        try {
            RecieveMessage recieveMessage = objectMapper.readValue(message, RecieveMessage.class);

            // Handle based on the client type
            switch (recieveMessage.clientType) {
                case "ccp":
                    handleCCPMessage(recieveMessage);
                    break;
                case "station":
                    handleStationMessage(recieveMessage);
                    break;
                default:
                    logger.warning("Unknown client type: " + recieveMessage.clientType);
            }
        } catch (Exception e) {
            logger.severe("Failed to handle message: " + e.getMessage());
        }
    }

    // Handles all checkpoint messages
    public void handleCheckpointMessage(String message) {
        switch (message) {
            case "trip":
                // Code here when trip occurs
                break;
            case "ok":
                // Code here as response to ok
                break;
            default:
                logger.severe("Failed to handle checkpoint message: " + message);
                break;
        }
    }

    private void handleCCPMessage(RecieveMessage recieveMessage) {
        TrainClient client = db.getTrain(recieveMessage.clientID);
        // Different behaviour based on what the message command is
        switch (recieveMessage.message) {
            case "CCIN":
                client.sendAcknowledgeMessage();
                logger.info("Received CCIN message from Blade Runner: " + recieveMessage.clientID);
                break;
            case "STAT":
                client.updateStatus(recieveMessage.status.toUpperCase());
                logger.info("Received STAT command from Blade Runner: " + recieveMessage.clientID);
                break;
            default:
                logger.warning("Unknown Blade Runner message: " + recieveMessage.message);
        }
    }

    private void handleStationMessage(RecieveMessage recieveMessage) {
        StationClient client = db.getStation(recieveMessage.clientID);
        // Different behaviour based on what the message command is
        switch (recieveMessage.message) {
            case "DOOR":
                client.updateStatus(recieveMessage.status.toUpperCase());
                logger.info("Received STIN message from Station: " + recieveMessage.clientID);
                break;
            case "STAT":
                // Handle station status update
                logger.info("Received STAT message from Station: " + recieveMessage.clientID);
                break;
            default:
                logger.warning("Unknown Station message: " + recieveMessage.message);
        }
    }
}
