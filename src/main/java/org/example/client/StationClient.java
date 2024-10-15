package org.example.client;
import org.example.messages.MessageEnums;
import org.example.messages.MessageEnums.STCAction;
import org.example.messages.MessageSender;

public class StationClient extends AbstractClient<MessageEnums.STCStatus, STCAction> {
    private final int location;

    public StationClient (String id, MessageGenerator messageGenerator, MessageSender messageSender, int location) {
        super(id, messageGenerator, messageSender);
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
        lastExecMessageSent = action.toString();
        String message = messageGenerator.generateExecuteMessage(type, super.getId(),
                outgoingSequenceNumber.getAndIncrement(), String.valueOf(action));
        sendMessage(message, "EXEC");
    }

    public void sendDoorMessage (STCAction action) {
        this.lastActionSent = action;
        String message = messageGenerator.generateDoorMessage(type, super.getId(),
                outgoingSequenceNumber.getAndIncrement(), String.valueOf(action));
        sendMessage(message, "DOOR");
    }

}
