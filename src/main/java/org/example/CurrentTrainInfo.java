package org.example;

import java.util.logging.Logger;
import java.util.logging.Level;

public class CurrentTrainInfo {
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final TrainClient train;
    private boolean hasSent;
    private long timeSinceSent;

    public CurrentTrainInfo(TrainClient train) {
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
        timeSinceSent = System.currentTimeMillis();
        logger.log(Level.INFO, String.format("%s Moving %s", retry, train.id));
        train.sendExecuteMessage(SpeedEnum.SLOW);
        hasSent = true;
    }

    // Tells the current train to stop when a checkpoint has been detected
    private void stopTrainAtCheckpoint(int zone) {
        logger.log(Level.INFO, "Train " + train.id + " has been mapped to zone " + zone);
        train.sendExecuteMessage(SpeedEnum.STOP);
        train.changeZone(zone);
    }


    // If the current train has not responded after timeout time then retry
    private void retrySending() {
        hasSent = false;
        sendTrainToNextCheckpoint("Retry");
    }
}