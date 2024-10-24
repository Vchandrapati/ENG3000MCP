package org.example.events;

public record TripEvent(int location, boolean untrip) implements Event {
}
