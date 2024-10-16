package org.example.state;

import org.example.Database;
import org.example.client.ReasonEnum;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// Manages the states of the system
public class SystemStateManager {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    // TODO
    // private static final Database db = Database.getInstance();
    private static Database db = Database.getInstance();
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

    // Holds the current state and the current state concrete implementation
    private SystemState currentState;
    private SystemStateInterface currentStateConcrete;
    private long timeWaited = System.currentTimeMillis();


    public void injectDatabase(Database db) {
        this.db = db;
    }

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
    public boolean run() {
        long timeToWait = currentStateConcrete.getTimeToWait();
        boolean performedAction = false;

        if (System.currentTimeMillis() - timeWaited >= timeToWait) {
            // if the current state returns true, means it has finished and will be changed
            // to its next appropriate state
            if (currentStateConcrete.performOperation()) {
                setState(currentStateConcrete.getNextState());
                performedAction = true;
            }

            timeWaited = System.currentTimeMillis();
        }
        return performedAction;
    }

    // gets current state
    public SystemState getState() {
        return currentState;
    }

    // Sets the state of the program to the given one
    public boolean setState(SystemState newState) {
        if (newState == null || currentState == newState) {
            return false;
        }

        currentState = newState;
        currentStateConcrete = stateMap.get(newState).get();

        logger.log(Level.INFO, "Changing to system state {0}", newState);
        return true;
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
            logger.log(Level.INFO, "System state manager has detected untrip {0}", trippedSensor);
            return true;
        }
        return false;
    }

    // Takes a string id of a client id
    // adds a client to the unresponsive client list in the database
    // only does this if in not in the waiting state
    public boolean addUnresponsiveClient(String id, ReasonEnum reason) {
        if (currentState != SystemState.WAITING && id != null && reason != null
                && db.addUnresponsiveClient(id, reason)) {
            logger.log(Level.WARNING, "Client {0} has {1}", new Object[] {id, reason});
            if (currentState != SystemState.EMERGENCY) {
                logger.log(Level.WARNING, "Error detected while in state {0}", currentState);
                setState(SystemState.EMERGENCY);
            }
            return true;
        }
        return false;
    }
}
