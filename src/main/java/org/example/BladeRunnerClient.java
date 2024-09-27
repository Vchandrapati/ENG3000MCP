package org.example;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class BladeRunnerClient extends Client {

    private enum Status {
        STOPPED,
        STARTED,
        ON,
        OFF,
        ERR,
        CRASH,
        STOPPED_AT_STATION
    }
    private final AtomicInteger zone = new AtomicInteger();
    private volatile Status status;
    private volatile boolean isCurrentlyMapped;

    public BladeRunnerClient(InetAddress clientAddress, int clientPort, String id) {
        super(clientAddress, clientPort, id);
        this.status = Status.ON;
        this.isCurrentlyMapped = false;
    }

    public Integer getZone() {
        return zone.get();
    }

    public String getStatus() {
        return status.toString();
    }

    public void updateStatus(String newStatus) {
        try {
            status = Status.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Tried to assign unknown status: {0} for train {1}",
                    new Object[] { newStatus, id });
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

    // For sending an acknowledgment message to the CCP
    @Override
    public void sendAcknowledgeMessage() {
        String message = MessageGenerator.generateAcknowledgesMessage("ccp", id, System.currentTimeMillis());
        sendMessage(message, "ACK");
        registered = true;
    }

    // For sending a status message to the CCP
    @Override
    public void sendStatusMessage(long timestamp) {
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
        Database.getInstance().addClient(this.id, this);
        logger.info("Added new BladeRunner to database: " + Database.getInstance().getBladeRunnerCount());

    }

    public void changeZone(int zone) {
        this.zone.set(zone);
        isCurrentlyMapped = true;
    }

    public void unmap() {
        isCurrentlyMapped = false;
    }

    public boolean isUnmapped() {
        return !isCurrentlyMapped;
    }
}
