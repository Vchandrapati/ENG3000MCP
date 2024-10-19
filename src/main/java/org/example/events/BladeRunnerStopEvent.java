package org.example.events;

public class BladeRunnerStopEvent implements Event {
    private final String id;

    public BladeRunnerStopEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
