package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageGenerator {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public static String generateAcknowledgesMessage(String clientType, String clientID, Long timestamp) {
        SendMessage message = new SendMessage();
        message.clientType = clientType;
        message.message = "AKIN";
        message.clientID = clientID;
        message.timestamp = timestamp;

        return convertToJson(message);
    }

    public static String generateStatusMessage(String clientType, String clientID, Long timestamp) {
        SendMessage message = new SendMessage();
        message.clientType = clientType;
        message.message = "STAT";
        message.clientID = clientID;
        message.timestamp = timestamp;

        return convertToJson(message);
    }

    public static String generateExecuteMessage(String clientType, String clientID, Long timestamp, SpeenEnum speed) {
        SendMessage message = new SendMessage();
        message.clientType = clientType;
        message.message = "EXEC";
        message.clientID = clientID;
        message.timestamp = timestamp;

        switch (speed) {
            case SLOW:
                message.action = "SLOW";
                break;
            case STOP:
                message.action = "STOP";
                break;
            case FAST:
                message.action = "FAST";
                break;
            case STOPNEXTSTATION:
                message.action = "Undefined rn (STOPNEXTSTATION)";
                break;
            default:
                message.action = "STOP";
                logger.log(Level.WARNING, "No speed defined set to STOP");
                break;
        }

        return convertToJson(message);
    }

    public static String generateDoorMessage(String clientType, String clientID, Long timestamp, boolean doorOpen) {
        SendMessage message = new SendMessage();
        message.clientType = clientType;
        message.message = "DOOR";
        message.clientID = clientID;
        message.timestamp = timestamp;

        if (doorOpen) {
            message.action = "OPEN";
        } else {
            message.action = "CLOSE";
        }

        return convertToJson(message);
    }

    public static String generateIRLEDMessage(String clientType, String clientID, Long timestamp, boolean on) {
        SendMessage message = new SendMessage();
        message.clientType = clientType;
        message.message = "IRLD";
        message.clientID = clientID;
        message.timestamp = timestamp;

        if (on) {
            message.action = "ON";
        } else {
            message.action = "OFF";
        }

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
