package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageHandler {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Database db = Database.getInstance();

    // Handles messages from CCPs and stations
    public void handleMessage(String message, InetAddress address, int port) {
        try {
            ReceiveMessage receiveMessage = objectMapper.readValue(message, ReceiveMessage.class);
            // Handle based on the client type
            switch (receiveMessage.clientType) {
                case "ccp":
                    handleCCPMessage(receiveMessage, address, port);
                    break;
                case "station":
                    handleStationMessage(receiveMessage);
                    break;
                case "checkpoint":
                    handleCheckpointMessage(receiveMessage, address, port);
                    break;
                default:
                    logger.log(Level.WARNING, "Unknown client type: {0}", receiveMessage.clientType);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to handle message", e);
        }
    }

    // Handles all checkpoint messages
    public void handleCheckpointMessage(ReceiveMessage receiveMessage, InetAddress address, int port) {
        CheckpointClient client = (CheckpointClient) db.getClient(receiveMessage.clientID);

        Processor processor = new Processor();
        switch (receiveMessage.message) {
            case "TRIP":
                // location is fucked
                processor.sensorTripped(Integer.parseInt(receiveMessage.location));
                break;
            case "CHIN":
                handleInitialise(receiveMessage, address, port);
                logger.log(Level.INFO, "Received STAT message from Checkpoint: {0}", receiveMessage.clientID);
                break;
            case "STAT":
                client.setStatReturned(true);
                logger.log(Level.INFO, "Received STAT command from Checkpoint: {0}", receiveMessage.clientID);
                break;
            default:
                logger.log(Level.SEVERE, "Failed to handle checkpoint message: {0}", receiveMessage);
                break;
        }
    }

    private void handleCCPMessage(ReceiveMessage receiveMessage, InetAddress address, int port) {
        TrainClient client = (TrainClient) db.getClient(receiveMessage.clientID);

        switch (receiveMessage.message) {
            case "STAT":
                client.updateStatus(receiveMessage.status.toUpperCase());
                client.setStatReturned(true);
                client.setStatSent(true);
                logger.log(Level.INFO, "Received STAT message from Blade Runner: {0}", receiveMessage.clientID);
                break;

            case "CCIN":
                handleInitialise(receiveMessage, address, port);
                logger.log(Level.INFO, "Received CCIN message from Blade Runner: {0}", receiveMessage.clientID);
                break;

            default:
                logger.log(Level.WARNING, "Unknown CCP message: {0}", receiveMessage.message);
                break;
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

    public void handleInitialise(ReceiveMessage receiveMessage, InetAddress address, int port) {
        try {
            switch (receiveMessage.clientType) {
                case "ccp":
                    TrainClient trainClient = new TrainClient(address, port, receiveMessage.clientID);
                    trainClient.registerClient();
                    trainClient.sendAcknowledgeMessage();
                    logger.log(Level.INFO, "Received CCIN message from CCP and created new client: {0}",
                            receiveMessage.clientID);
                    break;

                case "checkpoint":
                    CheckpointClient checkClient = new CheckpointClient(address, port, receiveMessage.clientID);
                    checkClient.registerClient();
                    checkClient.sendAcknowledgeMessage();
                    logger.log(Level.INFO, "Received CHIN message from Checkpoint and created new client: {0}",
                            receiveMessage.clientID);

                    break;

                default:
                    logger.log(Level.INFO, "Unknown client type: {0}", receiveMessage.clientType);
                    break;

            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to handle message", e);
        }
    }
}
