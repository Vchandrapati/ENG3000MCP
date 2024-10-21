package org.example.events;

import org.example.client.ReasonEnum;

public class ClientErrorEvent implements Event {
    private final String id;
    private final ReasonEnum reason;

    public ClientErrorEvent(String id, ReasonEnum reason) {
        this.id = id;
        this.reason = reason;
    }

    public String getId() {
        return id;
    }

    public ReasonEnum getReason() {
        return reason;
    }
}
