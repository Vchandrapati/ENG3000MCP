package org.example;

import java.net.InetAddress;
import java.util.logging.Level;

public class StationClient extends CheckpointClient {
    private volatile DoorStatus status;

    public StationClient(InetAddress clientAddress, int clientPort, String id, int location) {
        super(clientAddress, clientPort, id, location);
    }

    @Override
    public void updateStatus(String newStatus) {
        try {
            status = StationClient.DoorStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            logger.severe(String.format("Tried to assign unknown status: %s for BladeRunner %s", newStatus, id));
        }
    }

    // For sending a message about updating the status of the door
    // True: Door is open
    // False: Door is closed
    public void sendDoorMessage(boolean doorOpen) {
        String message = MessageGenerator.generateDoorMessage("station", id, System.currentTimeMillis(), doorOpen);
        sendMessage(message, "DOOR");
    }

    // To tell the station what the status of the LED should be
    // True: LED is on
    // False: LED is off
    public void sendIRLEDMessage(boolean on) {
        String message = MessageGenerator.generateIRLEDMessage("station", id, System.currentTimeMillis(), on);
        sendMessage(message, "IRLED");
    }

    @Override
    public void registerClient() {
        Database.getInstance().addClient(this.id, this);
        logger.info("Added new station to database: " + Database.getInstance().getStationCount());
    }

    @Override
    public void addReason(ReasonEnum r) {
        String[] temp = {id, r.toString()};
        Boolean valid = true;
        switch (r) {
            case ReasonEnum.NOSTAT:
                break;
            case ReasonEnum.WRONGMESSAGE:
                break;
            case ReasonEnum.INVALCONNECT:
                break;
            case ReasonEnum.CLIENTERR:
                break;
            default:
                valid = false;
                logger.log(Level.WARNING, "Attempted to add error {1} to {0} which is invalid", temp);
                break;
        }

        if (valid) {
            unresponsiveReasons.add(r);
        }

    }

    private enum DoorStatus {
        OPEN, CLOSE
    }
}
