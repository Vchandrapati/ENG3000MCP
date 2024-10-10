package org.example.client;

import org.example.messages.MessageEnums;
import org.example.messages.MessageEnums.CPCAction;
import org.example.messages.MessageSender;

public class CheckpointClient extends AbstractClient<MessageEnums.CPCStatus, CPCAction> {
    private final int location;

    public CheckpointClient (String id, MessageGenerator messageGenerator,
                             MessageSender messageSender, int initialSequenceNumber, int location) {
        super(id, messageGenerator, messageSender, initialSequenceNumber);
        this.updateStatus(MessageEnums.CPCStatus.OFF);
        this.location = location;
        this.type = "CPC";
    }

    public int getLocation() {
        return location;
    }

    @Override
    public void sendExecuteMessage(CPCAction action) {
        if (!action.equals(MessageEnums.CPCAction.BLINK)) {
            this.lastActionSent = action;
        }

        lastExecMessageSent = action.toString();
        String message = messageGenerator.generateExecuteMessage(type, super.getId(),
                sequenceNumberManager.getNextOutgoingSequenceNumber(), String.valueOf(action));

        sendMessage(message, "EXEC");

    }
}
