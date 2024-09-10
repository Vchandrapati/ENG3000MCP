package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class MessageHandler {
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Database db = Database.getInstance();
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
                    logger.warning(String.format("Unknown client type: %s", recieveMessage.clientType));
            }
        } catch (Exception e) {
            logger.severe("Failed to handle message: " + e.getMessage());
        }
    }

    // Handles all checkpoint messages
    public void handleCheckpointMessage(String message) {
        String[] msg = message.split(" ");
        CheckpointClient client = null;
        try {
            client = db.getCheckpoint(msg[0]).get();
        } catch (Exception e) {
            logger.severe(String.format("Error getting client: %s", e.getMessage()));
        }

        Processor processor = new Processor();
        switch (message) {
            case "trip":
                processor.sensorTripped(client.getIntID());
                break;
            case "ok":
                if (Arrays.stream(msg).anyMatch(s -> s.equals("ERR"))) {
                    //TODO
                }
                break;
            case "notok":
                break;
            default:
                logger.severe("Failed to handle checkpoint message: " + message);
                break;
        }
    }

    private void handleCCPMessage(RecieveMessage recieveMessage) throws ExecutionException, InterruptedException {
        TrainClient client = db.getTrain(recieveMessage.clientID).get();
        // Different behaviour based on what the message command is
        switch (recieveMessage.message) {
            case "CCIN":
                client.sendAcknowledgeMessage();
                logger.info("Received CCIN message from Blade Runner: " + recieveMessage.clientID);
                break;
            case "STAT":
                client.updateStatus(recieveMessage.status.toUpperCase());
                client.setStatReturned(true);
                logger.info("Received STAT command from Blade Runner: " + recieveMessage.clientID);
                break;
            default:
                logger.warning("Unknown Blade Runner message: " + recieveMessage.message);
        }
    }

    private void handleStationMessage(RecieveMessage recieveMessage) throws ExecutionException, InterruptedException {
        StationClient client = db.getStation(recieveMessage.clientID).get();
        // Different behaviour based on what the message command is
        switch (recieveMessage.message) {
            case "DOOR":
                client.updateStatus(recieveMessage.status.toUpperCase());
                logger.info("Received STIN message from Station: " + recieveMessage.clientID);
                break;
            case "STAT":
                client.setStatReturned(true);
                logger.info("Received STAT message from Station: " + recieveMessage.clientID);
                break;
            default:
                logger.warning("Unknown Station message: " + recieveMessage.message);
        }
    }
}
