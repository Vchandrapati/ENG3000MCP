package org.example.client;

import org.example.messages.MessageSender;

public abstract class StationAndCheckpoint<S extends Enum<S>, A extends Enum<A>>
        extends AbstractClient<S, A> {

    public StationAndCheckpoint(String id, MessageGenerator messageGenerator,
            MessageSender messageSender, int sequenceNumber) {
        super(id, messageGenerator, messageSender, sequenceNumber);

    }

    public abstract int getLocation();

    protected abstract void sendExecuteMessage(A action);
}
