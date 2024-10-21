package org.example.events;

import org.example.state.SystemState;

public class StateChangeEvent implements Event {
    private final SystemState state;

    public StateChangeEvent(SystemState state) {
        this.state = state;
    }

    public SystemState getState() {
        return state;
    }
}
