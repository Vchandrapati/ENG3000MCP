package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.InetAddress;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageHandler {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Database db = Database.getInstance();
    private final Processor processor = new Processor();

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
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to parse message: {0}", message);
            logger.log(Level.SEVERE, "Exception: ", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error handling message from {0}:{1}", new Object[] {address, port});
            logger.log(Level.SEVERE, "Exception: ", e);
        }
    }

    // Handles all checkpoint messages
    private void handleCheckpointMessage(ReceiveMessage receiveMessage, InetAddress address, int port) {
        Optional<CheckpointClient> opClient = db.getClient(receiveMessage.clientID, CheckpointClient.class);
        CheckpointClient client;

        if (opClient.isPresent()) {
            client = opClient.get();
        } else {
            logger.log(Level.SEVERE, "Attempted to get non-existant bladerunner: {0}", receiveMessage.clientID);
            return;
        }

        switch (receiveMessage.message) {
            case "TRIP":
                if (!client.isTripped()) {
                    client.setTripped();
                    processor.checkpointTripped(client.getLocation(), false);
                    logger.log(Level.INFO, "Received TRIP command from Checkpoint: {0}", receiveMessage.clientID);
                }
                break;
            case "CHIN":
                handleInitialise(receiveMessage, address, port);
                logger.log(Level.INFO, "Received STAT message from Checkpoint: {0}", receiveMessage.clientID);
                break;
            case "STAT":
                if (SystemStateManager.getInstance().getState() == SystemState.EMERGENCY) {
                    SystemStateManager.getInstance().sendEmergencyPacketClientID(receiveMessage.clientID);
                }
                client.setStatReturned(true);
                client.setStatSent(true);
                logger.log(Level.INFO, "Received STAT command from Checkpoint: {0}", receiveMessage.clientID);
                break;
            case "UNTRIP":
                if (client.isTripped()) {
                    client.resetTrip();
                    processor.checkpointTripped(client.getLocation(), true);
                    logger.log(Level.INFO, "Received UNTRIP command from Checkpoint: {0}", receiveMessage.clientID);
                }
                break;
            default:
                logger.log(Level.SEVERE, "Failed to handle checkpoint message: {0}", receiveMessage);
                break;
        }
    }

    private void handleCCPMessage(ReceiveMessage receiveMessage, InetAddress address, int port) {
        Optional<BladeRunnerClient> opClient = db.getClient(receiveMessage.clientID, BladeRunnerClient.class);
        BladeRunnerClient client;

        if (opClient.isPresent()) {
            client = opClient.get();
        } else {
            logger.log(Level.SEVERE, "Attempted to get non-existant bladerunner: {0}", receiveMessage.clientID);
            return;
        }

        switch (receiveMessage.message) {
            case "STAT":
                if (SystemStateManager.getInstance().getState() == SystemState.EMERGENCY) {
                    SystemStateManager.getInstance().sendEmergencyPacketClientID(receiveMessage.clientID);
                }
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
        Optional<StationClient> opClient = db.getClient(receiveMessage.clientID, StationClient.class);
        StationClient client;

        if (opClient.isPresent()) {
            client = opClient.get();
        } else {
            logger.log(Level.SEVERE, "Attempted to get non-existant bladerunner: {0}", receiveMessage.clientID);
            return;
        }


        // Different behaviour based on what the message command is
        switch (receiveMessage.message) {
            case "DOOR":
                client.updateStatus(receiveMessage.status.toUpperCase());
                logger.log(Level.INFO, "Received STIN message from Station: {0}", receiveMessage.clientID);
                break;
            case "STAT":
                if (SystemStateManager.getInstance().getState() == SystemState.EMERGENCY)
                    SystemStateManager.getInstance().sendEmergencyPacketClientID(receiveMessage.clientID);
                client.setStatReturned(true);
                client.setStatSent(true);
                logger.log(Level.INFO, "Received STAT message from Station: {0}", receiveMessage.clientID);
                break;
            case "STIN":
                handleInitialise(receiveMessage, null, 0);
                logger.log(Level.INFO, "Received STIN message from Station: {0}", receiveMessage.clientID);
                break;
            default:
                logger.log(Level.WARNING, "Unknown station message: {0}", receiveMessage.clientID);
        }
    }

    private void handleInitialise(ReceiveMessage receiveMessage, InetAddress address, int port) {
        try {
            Client client = null;
            switch (receiveMessage.clientType) {
                case "ccp":
                    client = new BladeRunnerClient(address, port, receiveMessage.clientID);
                    break;
                case "checkpoint":
                    client = new CheckpointClient(address, port, receiveMessage.clientID, receiveMessage.location);
                    break;
                case "station":
                    client = new StationClient(address, port, receiveMessage.clientID, receiveMessage.location);
                    break;
                default:
                    logger.log(Level.WARNING, "Unknown client type: {0}", receiveMessage.clientType);
                    break;
            }

            if (client != null) {
                client.registerClient();
                client.sendAcknowledgeMessage();
                logger.log(Level.INFO, "Initialised new client: {0}", receiveMessage.clientID);
            }

            // if a client joins while not in waiting state, goes to emergency mode
            if (SystemStateManager.getInstance().getState() != SystemState.WAITING) {
                    SystemStateManager.getInstance().addUnresponsiveClient(receiveMessage.clientID, ReasonEnum.INVALDCONNECT);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to handle message");
            logger.log(Level.SEVERE, "Exception: ", e);
        }
    }
}
