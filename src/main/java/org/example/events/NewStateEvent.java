package org.example.events;

import org.example.state.SystemState;

public class NewStateEvent implements Event {
    private final SystemState newState;

    public NewStateEvent(SystemState newState) {
        this.newState = newState;
    }

    public SystemState getNewState() {
        return newState;
    }
}
