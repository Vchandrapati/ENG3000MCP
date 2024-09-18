package org.example;

import java.util.logging.Logger;

public class CurrentTrainInfo {
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final Client train;
    private boolean hasSent;
    private long timeSinceSent;

    public CurrentTrainInfo(Client train) {
        this.train = train;
    }

    // Processes the current train to move to next checkpoint, keeps trying until it
    // reaches
    public boolean process(long trainStartupTimeout) {
        if (!hasSent) {
            sendTrainToNextCheckpoint("");
        } else {
            int tempTrip = SystemStateManager.getInstance().getLastTrip();
            if (tempTrip != -1) {
                stopTrainAtCheckpoint(tempTrip);
                return true;
            }
            if (System.currentTimeMillis() - timeSinceSent > trainStartupTimeout) {
                retrySending();
            }
        }
        return false;
    }

    // Send speed message to the current train
    private void sendTrainToNextCheckpoint(String retry) {
        logger.info(String.format("%s Moving %s", retry, train.id));
        sendTrainMessage(-1);
        hasSent = true;
    }

    // Tells the current train to stop when a checkpoint has been detected
    private void stopTrainAtCheckpoint(int zone) {
        logger.info("Train " + train.id + " has been mapped to zone " + zone);
        sendTrainMessage(zone);
        hasSent = true;
    }

    private void sendTrainMessage(int zone) {
        String message = MessageGenerator.generateExecuteMessage("ccp", train.id, System.currentTimeMillis(), SpeedEnum.SLOW);
        train.sendMessage(message, "TRAIN");

        try {
            TrainClient trainTemp = (TrainClient) train;
            if (zone != -1)
                trainTemp.changeZone(zone);
        } catch (Exception e) {
            logger.warning("Tried to cast Client to trainClient, failed");
        }
    }

    // If the current train has not responded after timeout time then retry
    private void retrySending() {
        hasSent = false;
        sendTrainToNextCheckpoint("Retry");
    }
}