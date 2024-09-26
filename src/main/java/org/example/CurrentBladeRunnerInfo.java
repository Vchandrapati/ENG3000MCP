package org.example;

import java.util.logging.Level;
import java.util.logging.Logger;

//Holds the data for the current BladeRunner being mapped
public class CurrentBladeRunnerInfo {
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private final BladeRunnerClient bladeRunner;
    private boolean hasSent;
    private long timeSinceSent;

    public CurrentBladeRunnerInfo(BladeRunnerClient bladeRunner) {
        this.bladeRunner = bladeRunner;
    }

    // Processes the current BladeRunner to move to next checkpoint, keeps trying until it reaches
    public boolean process (long bladeRunnerStartupTimeout) {
        if (!hasSent) {
            sendBladeRunnerToNextCheckpoint("");
        } else {
            int tempTrip = SystemStateManager.getInstance().getLastTrip();
            if (tempTrip != -1) {
                stopBladeRunnerAtCheckpoint(tempTrip);
                return true;
            }

            if (System.currentTimeMillis() - timeSinceSent > bladeRunnerStartupTimeout) {
                retrySending();
            }
        }
        
        return false;
    }

    // Send speed message to the current BladeRunner
    private void sendBladeRunnerToNextCheckpoint(String retry) {
        timeSinceSent = System.currentTimeMillis();
        logger.log(Level.INFO, "{0} moving {1}", new Object[]{retry, bladeRunner.id});
        bladeRunner.sendExecuteMessage(SpeedEnum.SLOW);
        hasSent = true;
    }

    // Tells the current BladeRunner to stop when a checkpoint has been detected
    private void stopBladeRunnerAtCheckpoint(int zone) {
        logger.log(Level.INFO, "BladeRunner {0} mapped to zone {1}", new Object[]{bladeRunner.id, zone});
        bladeRunner.sendExecuteMessage(SpeedEnum.STOP);
        bladeRunner.changeZone(zone);
        Database.getInstance().updateBladeRunnerBlock(bladeRunner.id, zone);
    }


    // If the current BladeRunner has not responded after timeout time then retry
    private void retrySending() {
        hasSent = false;
        sendBladeRunnerToNextCheckpoint("Retry");
    }
}