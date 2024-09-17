package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

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
                default:
                    logger.warning(String.format("Unknown client type: %s", receiveMessage.clientType));
            }
        } catch (Exception e) {
            logger.severe("Failed to handle message: " + e.getMessage());
        }
    }

    // Handles all checkpoint messages
    public void handleCheckpointMessage(String message) {
        String[] msg = message.split(" ");
        CheckpointClient client = db.getCheckpoint(msg[0]);

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

    private void handleCCPMessage(ReceiveMessage receiveMessage) throws ExecutionException, InterruptedException {
        TrainClient client = db.getTrain(receiveMessage.clientID);
        // Different behaviour based on what the message command is
        switch (receiveMessage.message) {
            case "CCIN":
                client.sendAcknowledgeMessage();
                logger.info("Received CCIN message from Blade Runner: " + receiveMessage.clientID);
                break;
            case "STAT":
                client.updateStatus(receiveMessage.status.toUpperCase());
                client.setStatReturned(true);
                logger.info("Received STAT command from Blade Runner: " + receiveMessage.clientID);
                break;
            default:
                logger.warning("Unknown Blade Runner message: " + receiveMessage.message);
        }
    }

    private void handleStationMessage(ReceiveMessage receiveMessage) throws ExecutionException, InterruptedException {
        StationClient client = db.getStation(receiveMessage.clientID);
        // Different behaviour based on what the message command is
        switch (receiveMessage.message) {
            case "DOOR":
                client.updateStatus(receiveMessage.status.toUpperCase());
                logger.info("Received STIN message from Station: " + receiveMessage.clientID);
                break;
            case "STAT":
                client.setStatReturned(true);
                logger.info("Received STAT message from Station: " + receiveMessage.clientID);
                break;
            default:
                logger.warning("Unknown Station message: " + receiveMessage.message);
        }
    }
}
