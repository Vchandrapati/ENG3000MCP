package org.example.events;

public class TripEvent implements Event {
    private final int location;
    private final boolean untrip;

    public TripEvent(int location, boolean untrip) {
        this.location = location;
        this.untrip = untrip;
    }

    public int getLocation() {
        return location;
    }

    public boolean isUntrip() {
        return untrip;
    }
}
