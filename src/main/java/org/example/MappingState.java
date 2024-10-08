package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.*;

public class MappingState implements SystemStateInterface {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // All time units in milliseconds
    private static final long BLADE_RUNNER_MAPPING_RETRY_TIMEOUT = 15000; // 15 seconds
    private static final long TIME_BETWEEN_RUNNING = 500;
    private static final long DELAY_BEFORE_START = 2000;

    private static final int MAX_RETRIES = 3;

    private static final SystemState NEXT_STATE = SystemState.RUNNING;

    private static final BlockingQueue<String> tripQueue = new LinkedBlockingQueue<>();

    private final List<BladeRunnerClient> bladeRunnersToMap;
    private final Database db = Database.getInstance();
    private boolean startMapping;
    private BladeRunnerClient currentBladeRunner;
    private int currentBladeRunnerIndex;
    private boolean hasSent;
    private long currentBladeRunnerStartTime;
    private int retryAttemps;
    private long startTime;

    private int currentTrip;
    private boolean backwards;

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
        backwards = false;
        currentTrip = -1;
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

    // Processes the current BladeRunner to move to next checkpoint, keeps trying until it reaches
    private boolean processCurrentBladeRunner() {
        if (!hasSent) {
            sendBladeRunnerToNextCheckpoint(false);
        } else {
            try {
                String[] tripInfo = tripQueue.take().split(",");
                if (tripInfo.length == 2) {
                    int tripZone = Integer.parseInt(tripInfo[0]);
                    if(currentTrip == -1 || currentTrip == tripZone) {
                        currentTrip = tripZone;
                    }
                    else {
                        String str = (tripZone == 10) ? "CP10" : "CP" + tripZone;
                        SystemStateManager.getInstance().addUnresponsiveClient(str, ReasonEnum.INCORTRIP);
                        logger.log(Level.WARNING, "Checkpoint : {0} has had inconsistent trip", str);
                        return false;
                    }
                    boolean untrip = Boolean.parseBoolean(tripInfo[1]);
                    return stopBladeRunnerAtCheckpoint(tripZone, untrip);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error taking trip from trip queue");
                Thread.currentThread().interrupt();
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

        //if the blader runner has had a collision go to reverse
        if (currentBladeRunner.collision(false, null)) {
            currentBladeRunner.sendExecuteMessage(MessageEnums.CCPAction.RSLOWC);
            backwards = true;
        } else {
            currentBladeRunner.sendExecuteMessage(MessageEnums.CCPAction.FFASTC);
        }

        hasSent = true;
    }

    // Tells the current BladeRunner to stop when a checkpoint has been detected
    private boolean stopBladeRunnerAtCheckpoint(int zone, boolean untrip) {

        //if this blade runner was detected to be in a collision, it will be going backwards
        //thus want the checkpoint before instead of infront
        if (backwards) {
            zone = Processor.calculatePreviousBlock(zone);
        }

        //if a normal trip, then change to slow
        //do not have to worry about backwards, because cannot go fast backwards
        if (!untrip && !backwards) {
            currentBladeRunner.sendExecuteMessage(MessageEnums.CCPAction.FSLOWC);
        }
        else {
            //if a untrip, then send stop 
            currentTrip = -1;
            currentBladeRunner.sendExecuteMessage(MessageEnums.CCPAction.STOPC);
            logger.log(Level.INFO, "BladeRunner {0} mapped to zone {1}",
                new Object[] {currentBladeRunner.getId(), zone});
        }

        currentBladeRunner.changeZone(zone);
        currentBladeRunner.collision(false, new Object());
        db.updateBladeRunnerBlock(currentBladeRunner.getId(), zone);

        return untrip;
    }

    // if a BladeRunner does not map in a minute time, send stop message to current
    // BladeRunner and go to waiting state
    // * Returns true if the timeout has occurred */
    private void checkIfBladeRunnerIsDead() {
        if (retryAttemps > MAX_RETRIES) {
            SystemStateManager.getInstance().addUnresponsiveClient(currentBladeRunner.getId(), ReasonEnum.MAPTIMEOUT);
            logger.log(Level.SEVERE, "Blade runner failed to be mapped in time");
        }
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

    public static void addTrip(int trip, boolean untrip) {
        StringBuilder str = new StringBuilder();
        str.append(trip);
        str.append(",");
        str.append(untrip);
        tripQueue.add(str.toString());
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
