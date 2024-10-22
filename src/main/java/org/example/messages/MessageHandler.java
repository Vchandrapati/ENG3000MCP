package org.example.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.*;
import org.example.client.*;
import org.example.events.*;
import org.example.state.SystemState;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageHandler {
    private Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private ObjectMapper objectMapper = new ObjectMapper();
    private Database db = Database.getInstance();
    private final EventBus eventBus;
    private SystemState currentState;

    public MessageHandler(EventBus eventBus) {
        this.eventBus = eventBus;

        eventBus.subscribe(StateChangeEvent.class, this::updateCurrentState);
        eventBus.subscribe(PacketEvent.class, this::handleMessage);
    }

    private void updateCurrentState(StateChangeEvent event) {
        currentState = event.getState();
    }

    public void handleMessage(PacketEvent event) {
        DatagramPacket packet = event.getPacket();
        String message = new String(packet.getData(), 0, packet.getLength(),
                StandardCharsets.UTF_8);

        try {
            ReceiveMessage receiveMessage = objectMapper.readValue(message, ReceiveMessage.class);

            // Handle based on the client type
            switch (receiveMessage.clientType) {
                case "CCP":
                    handleCCPMessage(receiveMessage, packet.getAddress(), packet.getPort());
                    break;
                case "STC":
                    handleSTCMessage(receiveMessage, packet.getAddress(), packet.getPort());
                    break;
                case "CPC":
                    handleCPCMessage(receiveMessage, packet.getAddress(), packet.getPort());
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
                    new Object[] {packet.getAddress(), packet.getPort(), e});
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
                            eventBus.publish(new TripEvent(client.getLocation(), false));
                            break;
                        case MessageEnums.CPCStatus.OFF:
                            client.updateStatus(MessageEnums.CPCStatus.OFF);
                            eventBus.publish(new TripEvent(client.getLocation(), true));
                            break;
                        case MessageEnums.CPCStatus.ERR:
                            eventBus.publish(new ClientErrorEvent(client.getId(), ReasonEnum.CLIENTERR));
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
                    client.expectingAKEXBy(receiveMessage.sequenceNumber);
                    break;
                default:
                    logger.log(Level.SEVERE, "Failed to handle checkpoint message: {0}",
                            receiveMessage);
                    break;
            }

            if (client.isMissedAKEX(receiveMessage.sequenceNumber)) {
                // Has missed the AKEX timing
            }
        }, () -> {
            // Client is not present
            if ("CPIN".equals(receiveMessage.message)) {
                eventBus.publish(new ClientIntialiseEvent(receiveMessage, address, port));
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
                    break;
                case "AKEX":
                    client.expectingAKEXBy(receiveMessage.sequenceNumber);
                    break;
                default:
                    logger.log(Level.WARNING, "Unknown CCP message: {0}", receiveMessage.message);
                    break;
            }

            if (client.isMissedAKEX(receiveMessage.sequenceNumber)) {
                // Has missed the AKEX timing
            }
        }, () -> {
            // Client is not present
            if ("CCIN".equals(receiveMessage.message)) {
                eventBus.publish(new ClientIntialiseEvent(receiveMessage, address, port));
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
                    client.expectingAKEXBy(receiveMessage.sequenceNumber);
                    break;
                case "TRIP":
                    switch (MessageEnums.STCStatus.valueOf(receiveMessage.status)) {
                        case MessageEnums.STCStatus.ON:
                            client.updateStatus(MessageEnums.STCStatus.ON);
                            eventBus.publish(new TripEvent(client.getLocation(), false));
                            break;
                        case MessageEnums.STCStatus.OFF:
                            client.updateStatus(MessageEnums.STCStatus.OFF);
                            eventBus.publish(new TripEvent(client.getLocation(), true));
                            break;
                        case MessageEnums.STCStatus.ERR:
                            eventBus.publish(new ClientErrorEvent(client.getId(), ReasonEnum.CLIENTERR));
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
                eventBus.publish(new ClientIntialiseEvent(receiveMessage, address, port));
                logger.log(Level.INFO, "Received STIN message from Station: {0}",
                        receiveMessage.clientID);
            } else {
                logger.log(Level.SEVERE, "Attempted to get non-existent station: {0}",
                        receiveMessage.clientID);
            }
        });
    }

    public <S extends Enum<S>, A extends Enum<A> & MessageEnums.ActionToStatus<S>> void handleStatMessage(
            AbstractClient<S, A> client, ReceiveMessage receiveMessage) {


        A lastAction = client.getLastActionSent();
        MessageEnums.CCPStatus alternateStatus = null;

        if (lastAction != null && receiveMessage.clientType.equals("CCP")
                && (lastAction.equals(MessageEnums.CCPAction.FSLOWC)
                || lastAction.equals(MessageEnums.CCPAction.RSLOWC))) {
            alternateStatus = MessageEnums.CCPStatus.STOPC;
        }


        try {
            S recievedStatus = Enum.valueOf(client.getStatus().getDeclaringClass(), receiveMessage.status);

            if (!client.isExpectingStat()) {
                client.sendAcknowledgeMessage(MessageEnums.AKType.AKST);
            }

            // If client reports ERR
            if (recievedStatus.toString().equals("ERR")) {
                eventBus.publish(new ClientErrorEvent(client.getId(), ReasonEnum.CLIENTERR));
            }

            // For specifically FSLOWC and RSLOWC case
            if (recievedStatus.equals(alternateStatus) && currentState.equals(SystemState.RUNNING)) {
                eventBus.publish(new BladeRunnerStopEvent(receiveMessage.clientID));
            }

            // If the current stat message sequence number is the highest then the stats
            // missed should = 0
            if (client.getLatestStatusMessageCount() < receiveMessage.sequenceNumber) {
                client.updateLatestStatusMessageCount(receiveMessage.sequenceNumber);
                client.resetMissedStats();
            }


            client.noLongerExpectingStat();
            client.updateStatus(recievedStatus);
        } catch (IllegalArgumentException e) {
            // Handle case where the status in receiveMessage is invalid
            logger.log(Level.SEVERE, "Invalid status: received {0} for client {1}",
                    new Object[] {receiveMessage.status, client.getId()});
        }

        logger.log(Level.INFO, "Received STAT message from Client: {0}", receiveMessage.clientID);
    }
}
