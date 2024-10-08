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
                    handleStatMessage(client, receiveMessage);
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
                    handleStatMessage(client, receiveMessage);
                    client.sendAcknowledgeMessage(MessageEnums.AKType.AKST);
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
                    handleStatMessage(client, receiveMessage);
                    break;
                case "AKEX":
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
                case "CCP":
                    client = new BladeRunnerClient(address, port, receiveMessage.clientID,
                            receiveMessage.sequenceNumber);
                    break;
                case "CPC": {
                    // Temp zone code
                    String[] id = receiveMessage.clientID.split("CP");
                    int zone = Integer.parseInt(id[1]);
                    client = new CheckpointClient(address, port, receiveMessage.clientID,
                            receiveMessage.sequenceNumber, zone);
                    break;
                }
                case "STC": {
                    String[] id = receiveMessage.clientID.split("ST");
                    int zone = Integer.parseInt(id[1]);
                    client = new StationClient(address, port, receiveMessage.clientID,
                            receiveMessage.sequenceNumber, zone);
                    break;
                }
                default:
                    logger.log(Level.WARNING, "Unknown client type: {0}",
                            receiveMessage.clientType);
                    break;
            }

            if (client != null) {
                client.registerClient();
                client.sendAcknowledgeMessage(MessageEnums.AKType.AKIN);
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

    private <S extends Enum<S>, A extends Enum<A> & MessageEnums.ActionToStatus<S>> void handleStatMessage(
            Client<S, A> client, ReceiveMessage receiveMessage) {
        A lastAction = client.getLastActionSent();
        S expectedStatus = null;

        if (lastAction != null)
            expectedStatus = lastAction.getStatus();

        try {
            S recievedStatus =
                    Enum.valueOf(client.currentStatus.getDeclaringClass(), receiveMessage.status);

            // If client reports ERR
            if (recievedStatus.toString().equals("ERR")) {
                systemStateManager.addUnresponsiveClient(client.getId(), ReasonEnum.CLIENTERR);
            } else if (expectedStatus != null && !expectedStatus.equals(recievedStatus)) {
                // If client is not in expected state then there is a problem
                systemStateManager.addUnresponsiveClient(client.getId(), ReasonEnum.WRONGSTATUS);
                logger.log(Level.SEVERE, "Client {0} did not update status to {1} from {2}",
                        new Object[] {client.getId(), expectedStatus, receiveMessage.status});
            }

            // If the current stat message sequence number is the highest then the stats
            // missed should = 0
            if (client.getLatestStatusMessageCount() < receiveMessage.sequenceNumber) {
                client.updateLatestStatusMessageCount(receiveMessage.sequenceNumber);
                client.resetMissedStats();
            }

            client.updateStatus(recievedStatus);
        } catch (IllegalArgumentException e) {
            // Handle case where the status in receiveMessage is invalid
            logger.log(Level.SEVERE, "Invalid status: received {0} for client {1}",
                    new Object[] {receiveMessage.status, client.getId()});
        }

        logger.log(Level.INFO, "Received STAT message from Blade Runner: {0}", receiveMessage.clientID);
    }
}
