package org.example;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//If no issues this state performs the operation of the system normally
public class RunningState implements SystemStateInterface {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final SystemState nextState = SystemState.RUNNING;
    private static final long TIME_BETWEEN_RUNNING = 500;
    private boolean allRunning;

    //constructor
    public RunningState() {
        allRunning = false;
    }

    //Performs the operation of this state at set intervals according to TIME_BETWEEN_RUNNING
    //If returns true then system goes to NEXT_STATE
    @Override
    public boolean performOperation() {
        if (!allRunning) {
            try {
                moveAllBladeRunners();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to grab BladeRunners from database");
            }
        }
        return false;
    }

    //sends a one time message to all BladeRunners to make them move
    private void moveAllBladeRunners() {
        try {
            List<BladeRunnerClient> bladeRunners = Database.getInstance().getBladeRunnerClients();
            if (bladeRunners != null && !bladeRunners.isEmpty()) {
                allRunning = true;
                for (BladeRunnerClient BladeRunnerClient : bladeRunners) {
                    BladeRunnerClient.sendExecuteMessage(SpeedEnum.SLOW);
                }
                logger.log(Level.INFO, "All BladeRunners are now moving at speed 1");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to move BladeRunners");
            SystemStateManager.getInstance().setState(SystemState.EMERGENCY);
        }
    }

    @Override
    public long getTimeToWait() {
        return TIME_BETWEEN_RUNNING;
    }

    @Override
    public SystemState getNextState() {
        return nextState;
    }
}