package org.example;

/**
 * This state occurs after the emergency state has happened, assumes all current WORKING trains are stationary
 * This state only occurs when a client has not responded to their status message after 2 seconds
 * Retries connecting with the lost client/s
 */
public class RestartupState extends MappingState {

    @Override
    protected boolean checkReadyToMap() {
        trainsToMap = db.getUnresponsiveClients();
        if (trainsToMap != null && !trainsToMap.isEmpty())
            return true;
        else {
            logger.info("No unresponsive clients found. Proceeding to next state.");
            return false;
        }
    }
}
