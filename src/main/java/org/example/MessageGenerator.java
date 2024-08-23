package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageGenerator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String generateStatusMessage(String clientType, String clientID, Long timestamp) {
        SendMessage message = new SendMessage();
        message.setClientType(clientType);
        message.setMessage("STATUS");
        message.setClientID(clientID);
        message.setTimestamp(timestamp);

        return convertToJson(message);
    }

    public static String generateSpeedMessage(String clientType, String clientID, Long timestamp, int speed) {
        SendMessage message = new SendMessage();
        message.setClientType(clientType);
        message.setMessage("SPED");
        message.setClientID(clientID);
        message.setTimestamp(timestamp);
        message.setStatus("ON"); // Example status, you can customize this

        return convertToJson(message);
    }

    public static String generateKillMessage(String clientType, String clientID, Long timestamp) {
        SendMessage message = new SendMessage();
        message.setClientType(clientType);
        message.setMessage("KILL");
        message.setClientID(clientID);
        message.setTimestamp(timestamp);

        return convertToJson(message);
    }

    private static String convertToJson(SendMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
