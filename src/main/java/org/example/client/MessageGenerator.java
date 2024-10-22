package org.example.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.messages.MessageBuilder;
import org.example.messages.MessageEnums;
import org.example.messages.SendMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageGenerator implements MessageBuilder {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private SendMessage preGen(String clientType, String clientID, Integer sequenceNumber) {
        SendMessage message = new SendMessage();
        message.clientType = clientType;
        message.clientID = clientID;
        message.sequenceNumber = sequenceNumber;
        return message;
    }

    private String convertToJson(SendMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to convert message to JSON", e);
            return "{}"; // Return an empty JSON object as a fallback
        }
    }

    @Override
    public String generateAcknowledgeMessage(String clientType, String clientID, int sequenceNumber,
            MessageEnums.AKType akType) {
        SendMessage message = preGen(clientType, clientID, sequenceNumber);
        message.message = akType.toString();
        return convertToJson(message);
    }

    @Override
    public String generateStatusMessage(String clientType, String clientID, int sequenceNumber) {
        SendMessage message = preGen(clientType, clientID, sequenceNumber);
        message.message = MessageType.STRQ.toString();
        return convertToJson(message);
    }

    @Override
    public String generateExecuteMessage(String clientType, String clientID, int sequenceNumber,
            String action) {
        SendMessage message = preGen(clientType, clientID, sequenceNumber);
        message.message = MessageType.EXEC.toString();
        message.action = action;
        return convertToJson(message);
    }



    @Override
    public String generateDoorMessage(String clientType, String clientID, int sequenceNumber,
            String action) {
        SendMessage message = preGen(clientType, clientID, sequenceNumber);
        message.message = MessageType.DOOR.toString();
        message.action = action;
        return convertToJson(message);
    }

    // Enums for types of message types
    public enum MessageType {
        EXEC, STRQ, DOOR
    }

    @Override
    public String generateStationNotificationMessage(String clientType, String clientID,
            int sequenceNumber, String action, String CCPID) {
        SendMessage message = preGen(clientType, clientID, sequenceNumber);
        message.message = MessageType.EXEC.toString();
        message.action = action;
        message.arrivingBR = CCPID;
        return convertToJson(message);
    }
}
