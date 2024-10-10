package org.example.messages;

import java.util.concurrent.atomic.AtomicInteger;

public class SequenceNumberManager {
    private final AtomicInteger outgoingSequenceNumber;
    private final AtomicInteger incomingSequenceNumber;

    public SequenceNumberManager (int initialSequenceNumber) {
        outgoingSequenceNumber = new AtomicInteger(0);
        incomingSequenceNumber = new AtomicInteger(initialSequenceNumber);
    }

    public int getNextOutgoingSequenceNumber () {
        return outgoingSequenceNumber.getAndIncrement();
    }
}
