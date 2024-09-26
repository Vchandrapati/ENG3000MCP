package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MappingState implements SystemStateInterface {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // All time units in milliseconds
    private static final long BLADE_RUNNER_MAPPING_DEATH_TIMEOUT = 60000; // 1 minute
    private static final long BLADE_RUNNER_MAPPING_RETRY_TIMEOUT = 15000; // 15 seconds
    private static final long TIME_BETWEEN_RUNNING = 500;

    private static final SystemState NEXT_STATE = SystemState.RUNNING;

    private final List<BladeRunnerClient> bladeRunnersToMap;
    private final Database db = Database.getInstance();
    private boolean startMapping;
    private CurrentBladeRunnerInfo currentBladeRunnerInfo;
    private int currentBladeRunnerIndex;
    private long currentBladeRunnerStartTime;

    //Constructor
    public MappingState() {
        currentBladeRunnerStartTime = System.currentTimeMillis();
        bladeRunnersToMap = new ArrayList<>();
        startMapping = false;
        currentBladeRunnerInfo = null;
        currentBladeRunnerIndex = 0;
    }

    //Performs the operation of this state at set intervals according to TIME_BETWEEN_RUNNING
    //If returns true then system goes to NEXT_STATE
    @Override
    public boolean performOperation() {
        if (!startMapping) {
            //grab all BladeRunner clients
            List<BladeRunnerClient> tempBladeRunnersToMap = db.getBladeRunnerClients();

            //get all unmapped BladeRunners
            for (BladeRunnerClient BladeRunnerClient : tempBladeRunnersToMap) {
                if (!BladeRunnerClient.isUnmapped()) {
                    bladeRunnersToMap.add(BladeRunnerClient);
                }
            }
            startMapping = true;
        }


        // if a BladeRunner does not map in a minute time, send stop message to current BladeRunner and go to waiting state
        long elapsedTime = System.currentTimeMillis() - currentBladeRunnerStartTime;
        if (elapsedTime >= BLADE_RUNNER_MAPPING_DEATH_TIMEOUT) {
            bladeRunnersToMap.get(currentBladeRunnerIndex).sendExecuteMessage(SpeedEnum.STOP);
            SystemStateManager.getInstance().setState(SystemState.WAITING);
            logger.log(Level.SEVERE, "Blade runner failed to be mapped in time");
        }

        return mapClients();
    }

    //proceeds to map all BladeRunner clients one by one, return true if no BladeRunners or all BladeRunners are mapped
    protected boolean mapClients() {
        //If no clients are available, complete mapping
        if (bladeRunnersToMap.isEmpty()) {
            logger.log(Level.WARNING, "No BladeRunners to map");
            return true;
        }

        if (currentBladeRunnerInfo == null) {
            if (currentBladeRunnerIndex < bladeRunnersToMap.size())
                currentBladeRunnerInfo = new CurrentBladeRunnerInfo(bladeRunnersToMap.get(currentBladeRunnerIndex));
        } else if (currentBladeRunnerInfo.process(BLADE_RUNNER_MAPPING_RETRY_TIMEOUT)) {
            currentBladeRunnerStartTime = System.currentTimeMillis();
            currentBladeRunnerIndex++;
            if (currentBladeRunnerIndex >= bladeRunnersToMap.size()) {
                logger.log(Level.INFO, "All clients have been mapped");
                return true;
            }
            currentBladeRunnerInfo = new CurrentBladeRunnerInfo(bladeRunnersToMap.get(currentBladeRunnerIndex));
        }
        return false;
    }

    @Override
    public long getTimeToWait() {
        return TIME_BETWEEN_RUNNING;
    }

    @Override
    public SystemState getNextState() {
        return NEXT_STATE;
    }

    @Override
    public long getStateTimeout() {
        return System.currentTimeMillis() - currentBladeRunnerStartTime;
    }
}
