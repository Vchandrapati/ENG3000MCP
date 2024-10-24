package org.example.events;

import org.example.state.SystemState;

public record NewStateEvent(SystemState newState) implements Event {
}
