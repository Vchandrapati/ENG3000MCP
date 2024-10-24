package org.example.state;

import org.example.Database;
import org.example.events.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// Manages the states of the system
public class SystemStateManager implements Runnable {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    // Holds the current state and the current state concrete implementation
    private static SystemStateManager instance;
    private final Map<SystemState, Supplier<SystemStateInterface>> stateMap;
    private final EventBus eventBus;
    private final Database db;
    private SystemState currentState;
    private volatile boolean isRunning;
    private SystemStateInterface currentStateConcrete;
    private boolean error = false;
    private long timeWaited = System.currentTimeMillis();

    // Initial state
    private SystemStateManager (EventBus eventBus) {
        this.eventBus = eventBus;
        isRunning = true;

        db = Database.getInstance();

        stateMap = new EnumMap<>(SystemState.class);
        stateMap.put(SystemState.WAITING, WaitingState::new);
        stateMap.put(SystemState.MAPPING, () -> new MappingState(eventBus));
        stateMap.put(SystemState.RUNNING, RunningState::new);
        stateMap.put(SystemState.EMERGENCY, EmergencyState::new);

        setState(SystemState.WAITING);

        eventBus.subscribe(NewStateEvent.class, this::updateState);
        eventBus.subscribe(ClientErrorEvent.class, this::addUnresponsiveClient);
        eventBus.subscribe(TripEvent.class, this::handleTrip);

        Thread systemStateThread = new Thread(this, "systemStateThread-Thread");
        systemStateThread.start();
    }

    /**
     * Provides the singleton instance of SystemStateManager. Initializes the instance if it doesn't
     * exist.
     *
     * @param eventBus The EventBus instance required for initialization.
     * @return The singleton instance of SystemStateManager.
     */
    public static SystemStateManager getInstance (EventBus eventBus) {
        if (instance == null) {
            instance = new SystemStateManager(eventBus);
        }
        return instance;
    }

    public void updateState (NewStateEvent event) {
        setState(event.newState());
    }

    @Override
    public void run () {
        while (isRunning) {
            runLoop();
        }
    }

    public void shutdown () {
        isRunning = false;
    }

    // If it is time for the current state to run its perform its operation,
    // otherwise checkChange
    public void runLoop () {
        long timeToWait = currentStateConcrete.getTimeToWait();

        if (System.currentTimeMillis() - timeWaited >= timeToWait) {
            // if the current state returns true, means it has finished and will be changed
            // to its next appropriate state
            if (currentStateConcrete.performOperation()) {
                setState(currentStateConcrete.getNextState());
            }

            timeWaited = System.currentTimeMillis();
        }

        checkChange();
    }


    // Checks to see if the system needs to go to emergency state, if already don't
    public void checkChange () {
        if (error && currentState != SystemState.EMERGENCY) {
            error = false;
            logger.log(Level.WARNING, "Error detected while in state {0}", currentState);
            setState(SystemState.EMERGENCY);
        }
    }

    // Sets the state of the program to the given one
    public void setState (SystemState newState) {
        if (newState == null || currentState == newState) {
            return;
        }

        currentState = newState;
        currentStateConcrete = stateMap.get(newState).get();
        eventBus.publish(new StateChangeEvent(newState));

        logger.log(Level.INFO, "Changing to system state {0}", newState);
    }


    // Takes trips/un-trips from processor only in WAITING and MAPPING state
    public void handleTrip (TripEvent event) {
        if (currentState == SystemState.WAITING) {
            return;
        }

        // if in the appropriate state of MAPPING only
        if (currentState == SystemState.MAPPING) {
            ((MappingState) currentStateConcrete).addTrip(event.location(), event.untrip());
            logger.log(Level.INFO, "System state manager has detected a trip {0}",
                    event.location());
        }
    }

    // Takes a string id of a client id
    // adds a client to the unresponsive client list in the database
    // only does this if in not in the waiting state
    public void addUnresponsiveClient (ClientErrorEvent event) {
        if (currentState != SystemState.WAITING && event.id() != null
                && event.reason() != null
                && db.addUnresponsiveClient(event.id(), event.reason())) {
            logger.log(Level.WARNING, "Client {0} has {1}",
                    new Object[] {event.id(), event.reason()});
            error = true;
        }
    }
}
