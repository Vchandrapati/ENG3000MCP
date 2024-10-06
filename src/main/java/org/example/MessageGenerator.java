package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageGenerator {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private MessageGenerator() {
        throw new IllegalStateException("Utility class");
    }

    private static SendMessage preGen(String clientType, String clientID, Integer sequenceNumber) {
        SendMessage message = new SendMessage();
        message.clientType = clientType;
        message.clientID = clientID;
        message.sequenceNumber = sequenceNumber;
        return message;
    }

    // Used to ack initialisation
    public static String generateAcknowledgeInitiationMessage(String clientType, String clientID, Integer sequenceNumber) {
        SendMessage message = preGen(clientType, clientID, sequenceNumber);
        message.message = "AKIN";
        return convertToJson(message);
    }

    public static String generateAcknowledgeStatusMessage(String clientType, String clientID, Integer sequenceNumber) {
        SendMessage message = preGen(clientType, clientID, sequenceNumber);
        message.message = "AKST";
        return convertToJson(message);
    }

    public static String generateStatusMessage(String clientType, String clientID, Integer sequenceNumber) {
        SendMessage message = preGen(clientType, clientID, sequenceNumber);
        message.message = "STRQ";
        return convertToJson(message);
    }

    // This will need refactoring
    public static String generateCCPExecuteMessage(String clientType, String clientID, Integer sequenceNumber, ActionEnum action) {
        SendMessage message = preGen(clientType, clientID, sequenceNumber);
        message.message = "EXEC";
        message.action = action;
        return convertToJson(message);
    }

    private static String convertToJson(SendMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to convert message to JSON", e);
            return "{}"; // Return an empty JSON object as a fallback
        }
    }
}
