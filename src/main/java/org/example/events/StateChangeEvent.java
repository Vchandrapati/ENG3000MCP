package org.example.events;

import org.example.state.SystemState;

public record StateChangeEvent(SystemState state) implements Event {
}
