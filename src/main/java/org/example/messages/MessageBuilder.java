package org.example.messages;

public interface MessageBuilder {
    String generateAcknowledgeMessage(String clientType, String clientID, int sequenceNumber,
            MessageEnums.AKType akType);

    String generateStatusMessage(String clientType, String clientID, int sequenceNumber);

    String generateExecuteMessage(String clientType, String clientID, int sequenceNumber,
            String action);

    String generateDoorMessage(String clientType, String clientID, int sequenceNumber,
            String action);

    String generateStationNotificationMessage(String clientType, String clientID,
            int sequenceNumber, String action, String CCPID);
}
