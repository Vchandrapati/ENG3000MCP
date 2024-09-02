package org.example;

import java.net.InetAddress;

public class CheckpointClient extends Client {
    private volatile Integer location;
    private volatile Status status;

    private enum Status {
        Alive,
        Dead
    }

    public CheckpointClient(InetAddress clientAddress, int clientPort, String id) {
        super(clientAddress, clientPort, id);
        location = 0;
        status = Status.Alive;
    }

    public Integer getLocation() {
        return location;
    }
}
