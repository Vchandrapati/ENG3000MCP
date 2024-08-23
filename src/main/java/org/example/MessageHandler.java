package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Logger;

public class MessageHandler {
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Database db = Database.getInstance();
    public void handleMessage(String message) {
        try {
            RecieveMessage recieveMessage = objectMapper.readValue(message, RecieveMessage.class);

            // Handle based on the client type
            switch (recieveMessage.clientType) {
                case "blade_runner":
                    handleBladeRunnerMessage(recieveMessage);
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

    private void handleBladeRunnerMessage(RecieveMessage recieveMessage) {
        switch (recieveMessage.message) {
            case "AKIN":
                // Acknowledge initialization
                logger.info("Received AKIN message from Blade Runner: " + recieveMessage.clientID);
                break;
            case "KILL":
                // Handle immediate stop command
                logger.info("Received KILL command from Blade Runner: " + recieveMessage.clientID);
                // Implement stop logic here
                break;
            default:
                logger.warning("Unknown Blade Runner message: " + recieveMessage.message);
        }
    }

    private void handleStationMessage(RecieveMessage recieveMessage) {
        switch (recieveMessage.message) {
            case "STIN":
                // Handle station initialization
                logger.info("Received STIN message from Station: " + recieveMessage.clientID);
                break;
            case "STAT":
                // Handle station status update
                logger.info("Received STAT message from Station: " + recieveMessage.clientID);
                // Update database or other logic
                break;
            default:
                logger.warning("Unknown Station message: " + recieveMessage.message);
        }
    }
}
