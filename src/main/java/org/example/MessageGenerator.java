package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageGenerator {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String generateAcknowledgesMessage(String clientType, String clientID, Long timestamp) {
        SendMessage message = new SendMessage();
        message.setClientType(clientType);
        message.setMessage("AKIN");
        message.setClientID(clientID);
        message.setTimestamp(timestamp);

        return convertToJson(message);
    }

    public static String generateStatusMessage(String clientType, String clientID, Long timestamp) {
        SendMessage message = new SendMessage();
        message.setClientType(clientType);
        message.setMessage("STAT");
        message.setClientID(clientID);
        message.setTimestamp(timestamp);

        return convertToJson(message);
    }

    public static String generateExecuteMessage(String clientType, String clientID, Long timestamp, int speed) {
        SendMessage message = new SendMessage();
        message.setClientType(clientType);
        message.setMessage("EXEC");
        message.setClientID(clientID);
        message.setTimestamp(timestamp);

        switch (speed) {
            case 1:
                message.setAction("SLOW");
                break;
            case 0:
                message.setAction("STOP");
                break;
            default:
                message.setAction("FAST");
                break;
        }

        return convertToJson(message);
    }

    public static String generateDoorMessage(String clientType, String clientID, Long timestamp, boolean doorOpen) {
        SendMessage message = new SendMessage();
        message.setClientType(clientType);
        message.setMessage("DOOR");
        message.setClientID(clientID);
        message.setTimestamp(timestamp);

        if (doorOpen) {
            message.setAction("OPEN");
        } else {
            message.setAction("CLOSE");
        }

        return convertToJson(message);
    }

    public static String generateIRLEDMessage(String clientType, String clientID, Long timestamp, boolean IROn) {
        SendMessage message = new SendMessage();
        message.setClientType(clientType);
        message.setMessage("IRLD");
        message.setClientID(clientID);
        message.setTimestamp(timestamp);

        if (IROn) {
            message.setAction("ON");
        } else {
            message.setAction("OFF");
        }

        return convertToJson(message);

    }

    // not used anymore ig?
    public static String generateKillMessage(String clientType, String clientID, Long timestamp) {
        SendMessage message = new SendMessage();
        message.setClientType(clientType);
        message.setMessage("KILL");
        message.setClientID(clientID);
        message.setTimestamp(timestamp);

        // return convertToJson(message);
        return null;
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
