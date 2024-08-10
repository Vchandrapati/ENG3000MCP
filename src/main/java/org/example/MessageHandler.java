package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Logger;

public class MessageHandler {
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void handleMessage(Client client, String message, Database db) {
        try {
            ReceiveMessage recieveMessage = objectMapper.readValue(message, ReceiveMessage.class);

            switch (recieveMessage.clientType) {
                case "blade_runner":
                    handleBladeRunnerMessage(recieveMessage, client, db);
                    break;
                case "station":
                    handleStationMessage(recieveMessage, client, db);
                    break;
                default:
                    logger.warning("Invalid client: " + recieveMessage.clientType);
            }
        } catch (Exception e) {
            logger.severe("Failed to handle message: " + e.getMessage());
        }

    }

    private void handleStationMessage(ReceiveMessage recieveMessage, Client client, Database db) {
        switch (recieveMessage.message) {
            case "STIN":
                handleStationInitialstion(recieveMessage);
                break;
            case "STAT":
                handleStationStatus(recieveMessage);
                break;
            default:
                logger.warning("Unknown message: " + recieveMessage.message);
                break;
        }
    }

    private void handleBladeRunnerMessage(ReceiveMessage recieveMessage, Client client, Database db) {
    }
}
