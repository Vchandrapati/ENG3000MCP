package org.example;

import java.net.InetAddress;
import java.net.Socket;

public class TrainClient extends Client {
    private volatile Integer zone;
    private volatile Status status;

    private enum Status {
        Alive,
        Dead
    }

    public TrainClient(InetAddress clientAddress, int clientPort, String id) {
        super(clientAddress, clientPort, id);
    }

    public Integer getZone() {
        return zone;
    }

    public void changeStatusToDead() {
        status = Status.Dead;
    }

    public void changeStatusToAlive() {
        status = Status.Alive;
    }
}
