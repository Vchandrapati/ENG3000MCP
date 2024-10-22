package org.example.state;

import org.example.Database;
import org.example.events.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// Manages the states of the system
public class SystemStateManager {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final Database db = Database.getInstance();
    private final Map<SystemState, Supplier<SystemStateInterface>> stateMap;

    // Holds the current state and the current state concrete implementation
    private static volatile SystemStateManager instance = null;
    private SystemState currentState;
    private SystemStateInterface currentStateConcrete;
    private boolean error = false;
    private long timeWaited = System.currentTimeMillis();
    private final EventBus eventBus;

    // Initial state
    private SystemStateManager(EventBus eventBus) {
        this.eventBus = eventBus;


        stateMap = new EnumMap<>(SystemState.class);
        stateMap.put(SystemState.WAITING, WaitingState::new);
        stateMap.put(SystemState.MAPPING, () -> new MappingState(eventBus));
        stateMap.put(SystemState.RUNNING, RunningState::new);
        stateMap.put(SystemState.EMERGENCY, EmergencyState::new);


        setState(SystemState.WAITING);

        eventBus.subscribe(NewStateEvent.class, this::updateState);
        eventBus.subscribe(ClientErrorEvent.class, this::addUnresponsiveClient);
        eventBus.subscribe(TripEvent.class, this::handleTrip);
    }

    public void updateState(NewStateEvent event) {
        setState(event.getNewState());
    }

    /**
     * Provides the singleton instance of SystemStateManager. Initializes the instance if it doesn't
     * exist.
     *
     * @param eventBus The EventBus instance required for initialization.
     * @return The singleton instance of SystemStateManager.
     */
    public static SystemStateManager getInstance(EventBus eventBus) {
        if (instance == null) { // First check (no locking)
            synchronized (SystemStateManager.class) {
                if (instance == null) { // Second check (with locking)
                    instance = new SystemStateManager(eventBus);
                }
            }
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
            error = false;
            logger.log(Level.WARNING, "Error detected while in state {0}", currentState);
            setState(SystemState.EMERGENCY);
        }
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
        eventBus.publish(new StateChangeEvent(newState));

        logger.log(Level.INFO, "Changing to system state {0}", newState);
        return true;
    }


    // Takes trips/untrips from processor only in WAITING and MAPPING state
    public void handleTrip(TripEvent event) {
        // if in waiting phase, no mans land, anything could happen MCP does nothing but waits for
        // connections
        // will take any trips from Processor and void them
        if (currentState == SystemState.WAITING) {
            return;
        }

        // if in the appropriate state of MAPPING only
        if (currentState == SystemState.MAPPING) {
            ((MappingState) currentStateConcrete).addTrip(event.getLocation(), event.isUntrip());
            logger.log(Level.INFO, "System state manager has detected untrip {0}",
                    event.getLocation());
        }
    }

    // Takes a string id of a client id
    // adds a client to the unresponsive client list in the database
    // only does this if in not in the waiting state
    public void addUnresponsiveClient(ClientErrorEvent event) {
        if (currentState != SystemState.WAITING && event.getId() != null
                && event.getReason() != null
                && db.addUnresponsiveClient(event.getId(), event.getReason())) {
            logger.log(Level.WARNING, "Client {0} has {1}",
                    new Object[] {event.getId(), event.getReason()});
            error = true;
        }
    }
}
