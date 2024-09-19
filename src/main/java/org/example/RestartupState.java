package org.example;

import java.util.logging.Level;

/**
 * This state occurs after the emergency state has happened, assumes all current WORKING trains are stationary
 * This state only occurs when a client has not responded to their status message after 2 seconds
 * Retries connecting with the lost client/s
 */
public class RestartupState extends MappingState {
    @Override
    protected boolean checkReadyToMap() {
        trainsToMap = db.getTrainsWaitingToReconnect();
        logger.log(Level.INFO, "Ready to restartup with {0} trains", trainsToMap.size());
        return true;
    }
}
