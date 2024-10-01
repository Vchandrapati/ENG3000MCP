package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MappingState implements SystemStateInterface {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // All time units in milliseconds
    private static final long BLADE_RUNNER_MAPPING_RETRY_TIMEOUT = 15000; // 15 seconds
    private static final long TIME_BETWEEN_RUNNING = 500;
    private static final long DELAY_BEFORE_START = 2000;

    private static final int MAX_RETRIES = 3;

    private static final SystemState NEXT_STATE = SystemState.RUNNING;

    private final List<BladeRunnerClient> bladeRunnersToMap;
    private final Database db = Database.getInstance();
    private boolean startMapping;
    private BladeRunnerClient currentBladeRunner;
    private int currentBladeRunnerIndex;
    private boolean hasSent;
    private long currentBladeRunnerStartTime;
    private int retryAttemps;
    private long startTime;

    // Constructor
    public MappingState() {
        retryAttemps = 0;
        hasSent = false;
        startMapping = false;
        currentBladeRunner = null;
        currentBladeRunnerIndex = 0;
        currentBladeRunnerStartTime = 0;
        startTime = System.currentTimeMillis();
        bladeRunnersToMap = new ArrayList<>();
    }

    // Performs the operation of this state at set intervals according to TIME_BETWEEN_RUNNING
    // If returns true then system goes to NEXT_STATE
    @Override
    public boolean performOperation() {
        // Will grab all unmapped trains once
        if (!startMapping && System.currentTimeMillis() - startTime >= DELAY_BEFORE_START) {
            grabBladeRunners();
            startMapping = true;
        }

        if (startMapping) {
            // If the blade runner timeout has occured
            // will change the state
            checkIfBladeRunnerIsDead();

            // maps the clients, returns true if all are mapped
            return mapClients();
        }
        return false;
    }



    // proceeds to map all BladeRunner clients one by one, return true if no BladeRunners or all
    // BladeRunners are mapped
    private boolean mapClients() {

        // If no clients are available, complete mapping
        if (bladeRunnersToMap.isEmpty()) {
            logger.log(Level.WARNING, "No BladeRunners to map");
            return true;
        }

        // if no there are blade runners to map, and mapping has not started yet
        // get the first blader runner
        if (currentBladeRunner == null) {
            currentBladeRunner = bladeRunnersToMap.get(currentBladeRunnerIndex);
        }

        boolean isCurrentBladeRunnerMapped = processCurrentBladeRunner();

        if (isCurrentBladeRunnerMapped) {
            if (++currentBladeRunnerIndex >= bladeRunnersToMap.size()) {
                logger.log(Level.INFO, "All clients have been mapped");
                return true;
            }
            currentBladeRunner = bladeRunnersToMap.get(currentBladeRunnerIndex);
        }

        return false;
    }

    // Grabs all unmapped blade runners, and also blade runners that have collided
    private void grabBladeRunners() {
        List<BladeRunnerClient> tempBladeRunnersToMap = db.getBladeRunnerClients();
        for (BladeRunnerClient BladeRunnerClient : tempBladeRunnersToMap) {
            // returns true if the blade runner has had a collision
            if (BladeRunnerClient.collision(false, null)) {
                bladeRunnersToMap.addFirst(BladeRunnerClient);
            }
            // returns true if the blade runner is unmapped
            else if (BladeRunnerClient.isUnmapped()) {
                bladeRunnersToMap.add(BladeRunnerClient);
            }
        }
    }

    // if a BladeRunner does not map in a minute time, send stop message to current
    // BladeRunner and go to waiting state
    // * Returns true if the timeout has occured */
    private void checkIfBladeRunnerIsDead() {
        if (retryAttemps > MAX_RETRIES) {
            SystemStateManager.getInstance().addUnresponsiveClient(currentBladeRunner.getId(),
                    ReasonEnum.MAPTIMEOUT);
            logger.log(Level.SEVERE, "Blade runner failed to be mapped in time");
        }
    }

    // Processes the current BladeRunner to move to next checkpoint, keeps trying until it reaches
    private boolean processCurrentBladeRunner() {
        if (!hasSent) {
            sendBladeRunnerToNextCheckpoint(false);
        } else {
            int tempTrip = SystemStateManager.getInstance().getLastTrip();
            if (tempTrip != -1) {
                stopBladeRunnerAtCheckpoint(tempTrip);
                return true;
            }

            // every BLADE_RUNNER_MAPPING_RETRY_TIMEOUT seconds, while not mapped, will try to move
            // the blade runner again
            if (System.currentTimeMillis() - currentBladeRunnerStartTime > (retryAttemps + 1)
                    * BLADE_RUNNER_MAPPING_RETRY_TIMEOUT) {
                sendBladeRunnerToNextCheckpoint(true);
                retryAttemps++;
            }
        }
        return false;
    }



    // Send speed message to the current BladeRunner
    private void sendBladeRunnerToNextCheckpoint(boolean retry) {
        if (!hasSent) {
            currentBladeRunnerStartTime = System.currentTimeMillis();
        }
        String retryString = (retry) ? "retry" : "";
        logger.log(Level.INFO, "{0} moving {1}",
                new Object[] {retryString, currentBladeRunner.getId()});

        if (currentBladeRunner.collision(false, null)) {
            currentBladeRunner.sendExecuteMessage(SpeedEnum.BACKWARDS);
        } else {
            currentBladeRunner.sendExecuteMessage(SpeedEnum.SLOW);
        }

        hasSent = true;
    }

    // Tells the current BladeRunner to stop when a checkpoint has been detected
    private void stopBladeRunnerAtCheckpoint(int zone) {
        if (currentBladeRunner.collision(false, null)) {
            int totalBlocks = db.getCheckpointCount();
            zone = ((zone + totalBlocks - 2) % totalBlocks) + 1;
        }
        logger.log(Level.INFO, "BladeRunner {0} mapped to zone {1}",
                new Object[] {currentBladeRunner.getId(), zone});
        currentBladeRunner.sendExecuteMessage(SpeedEnum.STOP);
        currentBladeRunner.changeZone(zone);
        currentBladeRunner.collision(false, new Object());
        db.updateBladeRunnerBlock(currentBladeRunner.getId(), zone);
    }

    @Override
    public long getTimeToWait() {
        return TIME_BETWEEN_RUNNING;
    }

    @Override
    public SystemState getNextState() {
        return NEXT_STATE;
    }
}
