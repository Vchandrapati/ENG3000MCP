package org.example.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.*;
import org.example.StatHandler;
import org.example.client.BladeRunnerClient;
import org.example.client.CheckpointClient;
import org.example.client.ReasonEnum;
import org.example.client.StationClient;
import org.example.state.SystemStateManager;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageHandler {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Database db = Database.getInstance();
    private static final SystemStateManager systemStateManager = SystemStateManager.getInstance();
    private static final StatHandler statHandler = StatHandler.getInstance();

    public void handleMessage(String message, InetAddress address, int port) {
        try {
            ReceiveMessage receiveMessage = objectMapper.readValue(message, ReceiveMessage.class);

            // Handle based on the client type
            switch (receiveMessage.clientType) {
                case "CCP":
                    handleCCPMessage(receiveMessage, address, port);
                    break;
                case "STC":
                    handleSTCMessage(receiveMessage, address, port);
                    break;
                case "CPC":
                    handleCPCMessage(receiveMessage, address, port);
                    break;
                default:
                    logger.log(Level.WARNING, "Unknown client type: {0}",
                            receiveMessage.clientType);
            }
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to parse message: {0} \nException: {1}",
                    new Object[] {message, e.getMessage()});
        } catch (Exception e) {
            logger.log(Level.FINEST, "{0}", message);
            logger.log(Level.SEVERE,
                    "Unexpected error handling message from {0}:{1} \nException: {2}",
                    new Object[] {address, port, e});

            e.printStackTrace();
        }
    }

    // Handles all checkpoint messages
    private void handleCPCMessage(ReceiveMessage receiveMessage, InetAddress address, int port) {
        db.getClient(receiveMessage.clientID, CheckpointClient.class).ifPresentOrElse(client -> {
            client.setLastResponse(receiveMessage.message);
            // Client is present
            switch (receiveMessage.message) {
                case "TRIP":
                    switch (MessageEnums.CPCStatus.valueOf(receiveMessage.status)) {
                        case MessageEnums.CPCStatus.ON:
                            client.updateStatus(MessageEnums.CPCStatus.ON);
                            Processor.checkpointTripped(client.getLocation(), false);
                            break;
                        case MessageEnums.CPCStatus.OFF:
                            client.updateStatus(MessageEnums.CPCStatus.OFF);
                            Processor.checkpointTripped(client.getLocation(), true);
                            break;
                        case MessageEnums.CPCStatus.ERR:
                            systemStateManager.addUnresponsiveClient(client.getId(),
                                    ReasonEnum.CLIENTERR);
                            break;
                        default:
                            break;

                    }

                    client.sendAcknowledgeMessage(MessageEnums.AKType.AKTR);
                    logger.log(Level.INFO, "Received TRIP command from Checkpoint: {0}",
                            receiveMessage.clientID);
                    break;
                case "STAT":
                    statHandler.handleStatMessage(client, receiveMessage);
                    break;
                case "AKEX":
                    break;
                default:
                    logger.log(Level.SEVERE, "Failed to handle checkpoint message: {0}",
                            receiveMessage);
                    break;
            }
        }, () -> {
            // Client is not present
            if ("CPIN".equals(receiveMessage.message)) {
                handleInitialise(receiveMessage, address, port);

                logger.log(Level.INFO, "Received CPIN message from Checkpoint: {0}",
                        receiveMessage.clientID);
            } else {
                logger.log(Level.SEVERE, "Attempted to get non-existent checkpoint: {0}",
                        receiveMessage.clientID);
            }
        });
    }


    private void handleCCPMessage(ReceiveMessage receiveMessage, InetAddress address, int port) {
        db.getClient(receiveMessage.clientID, BladeRunnerClient.class).ifPresentOrElse(client -> {
            client.setLastResponse(receiveMessage.message);
            // Client is present
            switch (receiveMessage.message) {
                case "STAT":
                    statHandler.handleStatMessage(client, receiveMessage);

                    break;
                case "AKEX":
                    break;
                default:
                    logger.log(Level.WARNING, "Unknown CCP message: {0}", receiveMessage.message);
                    break;
            }
        }, () -> {
            // Client is not present
            if ("CCIN".equals(receiveMessage.message)) {
                handleInitialise(receiveMessage, address, port);
                logger.log(Level.INFO, "Received CCIN message from Blade Runner: {0}",
                        receiveMessage.clientID);
            } else {
                logger.log(Level.SEVERE, "Attempted to get non-existent bladerunner: {0}",
                        receiveMessage.clientID);
            }
        });
    }

    private void handleSTCMessage(ReceiveMessage receiveMessage, InetAddress address, int port) {
        db.getClient(receiveMessage.clientID, StationClient.class).ifPresentOrElse(client -> {
            client.setLastResponse(receiveMessage.message);
            // Client is present
            switch (receiveMessage.message) {
                case "STAT":
                    statHandler.handleStatMessage(client, receiveMessage);
                    break;
                case "AKEX":
                    break;
                case "TRIP":
                    switch (MessageEnums.STCStatus.valueOf(receiveMessage.status)) {
                        case MessageEnums.STCStatus.ON:
                            client.updateStatus(MessageEnums.STCStatus.ON);
                            Processor.checkpointTripped(client.getLocation(), false);
                            break;
                        case MessageEnums.STCStatus.OFF:
                            client.updateStatus(MessageEnums.STCStatus.OFF);
                            Processor.checkpointTripped(client.getLocation(), true);
                            break;
                        case MessageEnums.STCStatus.ERR:
                            systemStateManager.addUnresponsiveClient(client.getId(),
                                    ReasonEnum.CLIENTERR);
                            break;
                        default:
                            break;

                    }

                    client.sendAcknowledgeMessage(MessageEnums.AKType.AKTR);
                    logger.log(Level.INFO, "Received TRIP command from Checkpoint: {0}",
                            receiveMessage.clientID);
                    break;
                default:
                    logger.log(Level.WARNING, "Unknown station message: {0}",
                            receiveMessage.message);
                    break;
            }
        }, () -> {
            // Client is not present
            if ("STIN".equals(receiveMessage.message)) {
                handleInitialise(receiveMessage, address, port);
                logger.log(Level.INFO, "Received STIN message from Station: {0}",
                        receiveMessage.clientID);
            } else {
                logger.log(Level.SEVERE, "Attempted to get non-existent station: {0}",
                        receiveMessage.clientID);
            }
        });
    }

    // I dont think anything needs to change here except location
    private void handleInitialise(ReceiveMessage receiveMessage, InetAddress address, int port) {
        ClientFactory.getInstance().handleInitialise(receiveMessage, address, port);
    }
}
