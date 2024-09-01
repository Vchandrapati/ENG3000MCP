package org.example;

import java.net.InetAddress;
import java.net.Socket;

public class StationClient extends Client {
    private volatile Integer location;
    private volatile Status status;

    private enum Status {
        Alive,
        Dead
    }

    public StationClient(InetAddress clientAddress, int clientPort, String id) {
        super(clientAddress, clientPort, id);
        location = 0;
    }

    public Integer getLocation() {
        return location;
    }
}
