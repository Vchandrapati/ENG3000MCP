package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
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
                default:
                    logger.warning(String.format("Unknown client type: %s", receiveMessage.clientType));
            }
        } catch (Exception e) {
            logger.severe("Failed to handle message: ");
            e.printStackTrace();
        }
    }

    // Handles all checkpoint messages
    public void handleCheckpointMessage(ReceiveMessage message) {
        CheckpointClient client = db.getCheckpoint(message.clientID);

        Processor processor = new Processor();
        switch (message.message) {
            case "TRIP":
                //location is fucked 
                processor.sensorTripped(Integer.parseInt(message.location));
                break;
            case "STIN":
                //NO todo to be found
                //TODO
                break;
            case "STAT":
                client.setStatReturned(true);
                logger.info("Received STAT command from Checkpoint: " + message.clientID);
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
            case "STAT":
                client.updateStatus(receiveMessage.status.toUpperCase());
                client.setStatReturned(true);
                client.setStatSent(true);
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

    public void handleInitilise(String message, InetAddress ip, int port) {
        try {
            ReceiveMessage receiveMessage = objectMapper.readValue(message, ReceiveMessage.class);
            // Handle based on the client type
            switch (receiveMessage.clientType) {
                case "ccp":
                    logger.warning(receiveMessage.message + " look here");
                    TrainClient client = new TrainClient(ip, port, receiveMessage.clientID);
                    client.id = receiveMessage.clientID;
                    client.registerClient();
                    client.sendAcknowledgeMessage();
                    logger.info("Received CCIN message from Blade Runner: " + receiveMessage.clientID);
                    logger.info("New client created and packet processed for client: " + client.id);
                    break;            
                default:
                    logger.warning(String.format("Unknown client type: %s", receiveMessage.clientType));
            }
        } catch (Exception e) {
            logger.severe("Failed to handle message: ");
            e.printStackTrace();
        }
    }
}
