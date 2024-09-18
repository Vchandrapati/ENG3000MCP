package org.example;

import java.net.InetAddress;

public class TrainClient extends Client {
    private volatile Integer zone;
    private volatile Status status;

    private enum Status {
        STOPPED,
        STARTED,
        ON,
        OFF,
        ERR,
        CRASH,
        STOPPED_AT_STATION
    }

    public TrainClient(InetAddress clientAddress, int clientPort, String id) {
        super(clientAddress, clientPort, id);
    }

    public Integer getZone() {
        return zone;
    }

    public String getStatus() {
        return status.toString();
    }

    public void updateStatus(String newStatus) {
        try {
            status = Status.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            logger.severe(String.format("Tried to assign unknown status: %s for train %s", newStatus, id));
        }
    }

    // For sending a message telling the CCP what speed the bladerunner should go at
    // 0: Stop
    // 1: Slow
    // 2+: Fast
    public void sendExecuteMessage(SpeedEnum speed) {
        String message = MessageGenerator.generateExecuteMessage("ccp", id, System.currentTimeMillis(), speed);
        sendMessage(message, "EXEC");
    }

    // For sending an acknowledge message to the CCP
    public void sendAcknowledgeMessage() {
        String message = MessageGenerator.generateAcknowledgesMessage("ccp", id, System.currentTimeMillis());
        sendMessage(message, "ACK");
        registered = true;
    }

    // For sending an status message to the CCP
    @Override
    public void sendStatusMessage(String id, Long timestamp) {
        String message = MessageGenerator.generateStatusMessage("ccp", id, System.currentTimeMillis());
        sendMessage(message, "STAT");
    }

    // For sending a message about updating the status of the door
    // True: Door is open
    // False: Door is closed
    public void sendDoorMessage(boolean doorOpen) {
        String message = MessageGenerator.generateDoorMessage("ccp", id, System.currentTimeMillis(), doorOpen);
        sendMessage(message, "DOOR");
    }

    @Override
    public void registerClient() {
        Database.getInstance().addClient(this.id, this, super.getClientAddress(), id);
        logger.info("Added new train to database: " + Database.getInstance().getTrainCount());

    }

    public void changeZone(int zone) {
        this.zone = zone;
    }
}
