package org.example;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// Manages the states of the system
public class SystemStateManager {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final Database db = Database.getInstance();
    private static final Map<SystemState, Supplier<SystemStateInterface>> stateMap;

    // singleton instance of class
    private static SystemStateManager instance;

    static {
        stateMap = new EnumMap<>(SystemState.class);
        stateMap.put(SystemState.WAITING, WaitingState::new);
        stateMap.put(SystemState.MAPPING, MappingState::new);
        stateMap.put(SystemState.RUNNING, RunningState::new);
        stateMap.put(SystemState.EMERGENCY, EmergencyState::new);
    }

    // holds the current state and the current state concrete implementation
    private SystemState currentState;
    private SystemStateInterface currentStateConcrete;
    private boolean error = false;
    private long timeWaited = System.currentTimeMillis();

    // Initial state
    private SystemStateManager() {
        setState(SystemState.WAITING);
    }

    // gets instance of system state manager, if none makes one
    public static synchronized SystemStateManager getInstance() {
        if (instance == null) {
            instance = new SystemStateManager();
        }
        return instance;
    }

    // If it is time for the current state to run its perform its operation,
    // otherwise checkChange
    public void run() {
        long timeToWait = currentStateConcrete.getTimeToWait();

        if (System.currentTimeMillis() - timeWaited >= timeToWait) {
            // if the current state returns true, means it has finished and will be changed
            // to its next appropriate state
            if (currentStateConcrete.performOperation()) {
                setState(currentStateConcrete.getNextState());
            }

            timeWaited = System.currentTimeMillis();
        } else {
            checkChange();
        }
    }

    // Checks to see if the system needs to go to emergency state, if already don't
    private void checkChange() {
        if (error && currentState != SystemState.EMERGENCY) {
            logger.log(Level.WARNING, "Error detected while in state {0}", currentState);
            setState(SystemState.EMERGENCY);
        }
    }

    // gets current state
    public SystemState getState() {
        return currentState;
    }

    // Sets the state of the program to the given one
    public void setState(SystemState newState) {
        if (currentState == newState) {
            return;
        }
        if (currentState == SystemState.EMERGENCY) {
            error = false;
        }

        currentState = newState;
        currentStateConcrete = stateMap.get(newState).get();

        logger.log(Level.INFO, "Changing to system state {0}", newState);
    }


    // Takes trips/untrips from processor only in WAITING and MAPPING state
    public boolean needsTrip(int trippedSensor, boolean untrip) {
        // if in waiting phase, no mans land, anything could happen MCP does nothing but waits for
        // connections
        // will take any trips from Processor and void them
        if (currentState == SystemState.WAITING) {
            return true;
        }

        // if in the appropriate state of MAPPING only
        if (currentState == SystemState.MAPPING) {
            MappingState.addTrip(trippedSensor, untrip);
            logger.log(Level.INFO, "System state manager has detected untrip {0}",
                            trippedSensor);
            return true;
        }
        return false;
    }

    // Takes a string id of a client id
    // adds a client to the unresponsive client list in the database
    // only does this if in not in the waiting state
    public void addUnresponsiveClient(String id, ReasonEnum reason) {
        if (currentState != SystemState.WAITING) {
            logger.log(Level.WARNING, "Client {0} has {1}", new Object[] {id, reason});
            error = true;
            db.addUnresponsiveClient(id, reason);
        }
    }
}
