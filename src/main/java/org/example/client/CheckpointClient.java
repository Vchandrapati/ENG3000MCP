package org.example.client;

import org.example.messages.MessageEnums;
import org.example.messages.MessageEnums.CPCAction;

public class CheckpointClient extends StationAndCheckpoint<MessageEnums.CPCStatus, CPCAction> {
    private final int location;

    public CheckpointClient(String id, MessageGenerator messageGenerator,
            MessageSender messageSender, int location, int sequenceNumber) {
        super(id, messageGenerator, messageSender, sequenceNumber);
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

        updateStatus(action.getStatus());
        lastExecMessageSent = action.toString();
        String message = messageGenerator.generateExecuteMessage(type, super.getId(),
                outgoingSequenceNumber.getAndIncrement(), String.valueOf(action));

        sendMessage(message, "EXEC");

    }
}
