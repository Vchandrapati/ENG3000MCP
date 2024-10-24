package org.example.events;

import org.example.client.ReasonEnum;

public record ClientErrorEvent(String id, ReasonEnum reason) implements Event {
}
