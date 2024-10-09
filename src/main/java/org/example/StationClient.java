package org.example;

import java.net.InetAddress;
import org.example.MessageEnums.CPCAction;
import org.example.MessageEnums.STCAction;

public class StationClient extends Client<MessageEnums.STCStatus, MessageEnums.STCAction> {
    private final int location;

    public StationClient(InetAddress clientAddress, int clientPort, String id, int sequenceNumber,
            int location) {
        super(clientAddress, clientPort, id, sequenceNumber);
        // Everyone starts like this but maybe they dont is the thing
        this.updateStatus(MessageEnums.STCStatus.OFF);
        this.location = location;
        this.type = "STC";
    }

    public int getLocation() {
        return location;
    }

    @Override
    public void sendExecuteMessage(STCAction action) {
        if (!action.equals(MessageEnums.STCAction.BLINK)) {
            this.lastActionSent = action;
        }
        lastExecMessageSent = "EXEC " + action.toString();
        String message = MessageGenerator.generateExecuteMessage(type, super.getId(),
                sequenceNumberOutgoing.getAndIncrement(), String.valueOf(action));
        sendMessage(message, "EXEC");
    }

    public void sendDOORMessage(STCAction action) {
        this.lastActionSent = action;

        String message = MessageGenerator.generateDOORMessage(type, super.getId(),
                sequenceNumberOutgoing.getAndIncrement(), String.valueOf(action));

        sendMessage(message, "DOOR");
    }

}
