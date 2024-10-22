package org.example.messages;

import org.example.events.Event;

public class TripEvent implements Event {
    private final int tripLocation;
    private final boolean untrip;

    public TripEvent(final int tripLocation, final boolean untrip) {
        this.tripLocation = tripLocation;
        this.untrip = untrip;
    }

    public int getTripLocation() {
        return tripLocation;
    }

    public boolean isUntrip() {
        return untrip;
    }
}
