package org.example;

import java.net.InetAddress;
import java.util.logging.Level;

public class CheckpointClient extends Client {
    private final Integer location;
    private boolean tripped;

    public CheckpointClient(InetAddress clientAddress, int clientPort, String id, int location) {
        super(clientAddress, clientPort, id);
        this.location = location;
        this.tripped = false;
    }

    public Integer getLocation() {
        return location;
    }

    @Override
    public void sendStatusMessage(long timestamp) {
        String message = MessageGenerator.generateStatusMessage("ccp", id, System.currentTimeMillis());
        sendMessage(message, "STAT");
    }

    @Override
    public void sendAcknowledgeMessage() {
        String message = MessageGenerator.generateAcknowledgesMessage("checkpoint", id, System.currentTimeMillis());
        sendMessage(message, "ACK");
        registered = true;
    }

    @Override
    public void registerClient() {
        Database.getInstance().addClient(this.id, this);
        logger.info("Added new checkpoint to database: " + Database.getInstance().getCheckpointCount());
    }

    public void setTripped() {
        this.tripped = true;
    }

    public void resetTrip() {
        this.tripped = false;
    }

    public boolean isTripped() {
        return tripped;
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
            case ReasonEnum.INCORTRIP:
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
}
