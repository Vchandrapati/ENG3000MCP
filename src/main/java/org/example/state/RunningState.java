package org.example.state;

import org.example.Database;
import org.example.client.BladeRunnerClient;
import org.example.client.ReasonEnum;
import org.example.messages.MessageEnums;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// If no issues this state performs the operation of the system normally
public class RunningState implements SystemStateInterface {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final Database db = Database.getInstance();

    private static final SystemState nextState = SystemState.RUNNING;
    private static final long TIME_BETWEEN_RUNNING = 500;
    private static final long TIME_BETWEEN_SENDING = 2000;
    private static final long WAIT = 3000;

    private List<BladeRunnerClient> bladeRunners;
    private int curBR;
    private long startTime;
    private long runningStartTime;
    private boolean grab;
    private boolean allRunning;

    // constructor
    public RunningState() {
        allRunning = false;
        grab = false;
        bladeRunners = null;
        startTime = 0;
        curBR = 0;
        runningStartTime = System.currentTimeMillis();
    }

    // Performs the operation of this state at set intervals according to TIME_BETWEEN_RUNNING
    // If returns true then system goes to NEXT_STATE
    @Override
    public boolean performOperation() {
        if (!grab && System.currentTimeMillis() - runningStartTime >= WAIT) {
            bladeRunners = grabAllBladeRunners();
        }
        if (grab && !allRunning) {
            moveAllBladeRunners();
        }
        return false;
    }

    // sends a one time message to all BladeRunners to make them move, makes them move in order
    private List<BladeRunnerClient> grabAllBladeRunners() {
        try {
            grab = true;
            List<BladeRunnerClient> grabbedBladeRunners = db.getBladeRunnerClients();
            if (grabbedBladeRunners != null) {
                Collections.sort(grabbedBladeRunners,
                        (br1, br2) -> Integer.compare(br2.getZone(), br1.getZone()));
                return grabbedBladeRunners;
            } 
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to move BladeRunners");
            SystemStateManager.getInstance().addUnresponsiveClient("SYSTEM", ReasonEnum.INTERNAL);
        }
        return new ArrayList<>();
    }

    private boolean moveAllBladeRunners() {
        if (System.currentTimeMillis() - startTime >= TIME_BETWEEN_SENDING || startTime == 0) {
            if (bladeRunners == null || bladeRunners.isEmpty()) {
                allRunning = true;
                return true;
            }
            if (curBR < bladeRunners.size()) {
                startTime = System.currentTimeMillis();
                bladeRunners.get(curBR++).sendExecuteMessage(MessageEnums.CCPAction.FFASTC);
            }
        }
        return false;
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
