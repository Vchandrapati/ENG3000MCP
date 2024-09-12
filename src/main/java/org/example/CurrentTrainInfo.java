package org.example;

import java.util.*;
import java.util.logging.Logger;

public class CurrentTrainInfo {
    static final Logger logger = Logger.getLogger(SystemStateInterface.class.getName());
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
            return false;
        } 
        else {
            int tempTrip = SystemStateManager.getInstance().getLastTrip();
            if (tempTrip != -1) {
                stopTrainAtCheckpoint(tempTrip);
                return true;
            }
            if (System.currentTimeMillis() - timeSinceSent > trainStartupTimeout) {
                retrySending();
            }
            return false;
        }
    }

    // Send speed message to the current train
    private void sendTrainToNextCheckpoint(String retry) {
        if (train == null) {
            return;
        }
        logger.info(retry + " Moving " + train.id);
        long tempTime = System.currentTimeMillis();
        String message = MessageGenerator.generateExecuteMessage("ccp", train.id, tempTime, SpeenEnum.SLOW);
        train.sendMessage(message);
        timeSinceSent = tempTime;
        hasSent = true;
    }

    // Tells the current train to stop when a checkpoint has been detected
    private void stopTrainAtCheckpoint(int zone) {
        logger.info("Train " + train.id + " has been mapped to zone " + zone);
        long tempTime = System.currentTimeMillis();
        String message = MessageGenerator.generateExecuteMessage("ccp", train.id, tempTime,
                SpeenEnum.STOP);
        train.sendMessage(message);
        train.changeZone(zone);
        hasSent = true;
    }

    // If the current train has not responded after timeout time then retry
    private void retrySending() {
        hasSent = false;
        sendTrainToNextCheckpoint("Retry");
    }
}