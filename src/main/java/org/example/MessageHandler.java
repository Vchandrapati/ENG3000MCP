package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageHandler {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Database db = Database.getInstance();
    private static final SystemStateManager systemStateManager = SystemStateManager.getInstance();


    // ET plans

    // 1. Cut based on message type
    // 2. Cut based on client type
    // 3. Update related data and notify correct subsystems


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
                    handleSTCMessage(receiveMessage, address, port);
                    break;
                case "checkpoint":
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
            logger.log(Level.SEVERE,
                    "Unexpected error handling message from {0}:{1} \nException: {2}",
                    new Object[] {address, port, e.getMessage()});
        }
    }

    // Handles all checkpoint messages
    private void handleCPCMessage(ReceiveMessage receiveMessage, InetAddress address, int port) {
        db.getClient(receiveMessage.clientID, CheckpointClient.class).ifPresentOrElse(client -> {
            // Client is present
            switch (receiveMessage.message) {
                case "TRIP":
                    switch (MessageEnums.CPCStatus.valueOf(receiveMessage.action)) {
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

                    logger.log(Level.INFO, "Received TRIP command from Checkpoint: {0}",
                            receiveMessage.clientID);
                    break;
                case "STAT":

                    // If client reports ERR
                    if (client.getStatus().equals(MessageEnums.CPCStatus.ERR)) {
                        systemStateManager.addUnresponsiveClient(client.getId(),
                                ReasonEnum.CLIENTERR);
                        // If different status to expected
                    } else if (!client.getStatus()
                            .equals(MessageEnums.CPCStatus.valueOf(receiveMessage.action))) {
                        systemStateManager.addUnresponsiveClient(client.getId(),
                                ReasonEnum.WRONGSTATUS);
                    }



                    // Reset the count
                    if (client.getLatestStatusMessageCount() < receiveMessage.sequenceNumber) {
                        client.updateLatestStatusMessageCount(receiveMessage.sequenceNumber);
                        client.resetMissedStats();
                    }

                    logger.log(Level.INFO, "Received STAT command from Checkpoint: {0}",
                            receiveMessage.clientID);
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
                logger.log(Level.INFO, "Received CHIN message from Checkpoint: {0}",
                        receiveMessage.clientID);
            } else {
                logger.log(Level.SEVERE, "Attempted to get non-existent checkpoint: {0}",
                        receiveMessage.clientID);
            }
        });
    }


    private void handleCCPMessage(ReceiveMessage receiveMessage, InetAddress address, int port) {
        db.getClient(receiveMessage.clientID, BladeRunnerClient.class).ifPresentOrElse(client -> {
            // Client is present
            switch (receiveMessage.message) {
                case "STAT":

                    // If client reports ERR
                    if (client.getStatus().equals(MessageEnums.CCPStatus.ERR)) {
                        systemStateManager.addUnresponsiveClient(client.getId(),
                                ReasonEnum.CLIENTERR);
                        // If client is not in expected state then there is a probelm
                    } else if (!client.getStatus()
                            .equals(MessageEnums.CCPStatus.valueOf(receiveMessage.status))) {
                        systemStateManager.addUnresponsiveClient(client.getId(),
                                ReasonEnum.WRONGSTATUS);
                    }


                    // If the current stat message sequence number is the highest then the stats
                    // missed should = 0
                    if (client.getLatestStatusMessageCount() < receiveMessage.sequenceNumber) {
                        client.updateLatestStatusMessageCount(receiveMessage.sequenceNumber);
                        client.resetMissedStats();
                    }

                    logger.log(Level.INFO, "Received STAT message from Blade Runner: {0}",
                            receiveMessage.clientID);
                    client.sendAcknowledgeMessage(MessageEnums.AKType.AKST);
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
            // Client is present
            switch (receiveMessage.message) {
                case "STAT":

                    // If client reports ERR
                    if (client.getStatus().equals(MessageEnums.STCStatus.ERR)) {
                        systemStateManager.addUnresponsiveClient(client.getId(),
                                ReasonEnum.CLIENTERR);
                        // If client is not in expected state then there is a probelm
                    } else if (!client.getStatus()
                            .equals(MessageEnums.STCStatus.valueOf(receiveMessage.status))) {
                        systemStateManager.addUnresponsiveClient(client.getId(),
                                ReasonEnum.WRONGSTATUS);
                    }



                    // If the current stat message sequence number is the highest then the stats
                    // missed should = 0
                    if (client.getLatestStatusMessageCount() < receiveMessage.sequenceNumber) {
                        client.updateLatestStatusMessageCount(receiveMessage.sequenceNumber);
                        client.resetMissedStats();
                    }

                    logger.log(Level.INFO, "Received STAT message from Station: {0}",
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
        try {
            Client<?, ?> client = null;
            switch (receiveMessage.clientType) {
                case "ccp":
                    client = new BladeRunnerClient(address, port, receiveMessage.clientID,
                            receiveMessage.sequenceNumber);
                    break;
                case "checkpoint":
                    client = new CheckpointClient(address, port, receiveMessage.clientID,
                            receiveMessage.sequenceNumber, 0);
                    break;
                case "station":
                    client = new StationClient(address, port, receiveMessage.clientID,
                            receiveMessage.sequenceNumber, 0);
                    break;
                default:
                    logger.log(Level.WARNING, "Unknown client type: {0}",
                            receiveMessage.clientType);
                    break;
            }

            if (client != null) {
                client.registerClient(receiveMessage.clientType);
                client.sendAcknowledgeMessage(receiveMessage.clientType, MessageEnums.AKType.AKIN);
                logger.log(Level.INFO, "Initialised new client: {0}", receiveMessage.clientID);
                // if a client joins while not in waiting state, goes to emergency mode
                if (SystemStateManager.getInstance().getState() != SystemState.WAITING) {
                    SystemStateManager.getInstance().addUnresponsiveClient(receiveMessage.clientID,
                            ReasonEnum.INVALCONNECT);
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to handle message");
            logger.log(Level.SEVERE, "Exception: ", e);
        }
    }
}
