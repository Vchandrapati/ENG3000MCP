package org.example;

import java.net.Socket;

public class TrainClient extends Client {

    public TrainClient(Socket clientSocket, String id) {
        super(clientSocket, id);
    }
    @Override
    public void start() {
        new Thread(this::readWrapper).start();
    }

    public void readWrapper() {
        while(running) {
            String input = readMessage();
            if(!input.isEmpty()) {

            }
        }
    }
}
