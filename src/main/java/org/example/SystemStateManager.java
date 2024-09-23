package org.example;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SystemStateManager {
    private static volatile SystemStateManager instance;
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final Database db = Database.getInstance();

    //holds the current state and the current state concrete implementation
    private SystemState currentState;
    private SystemStateInterface currentStateConcrete;

    private static final int NO_TRIP = -1;
    private int lastTrip = NO_TRIP;

    private boolean error = false;

    private long timeWaited = System.currentTimeMillis();
    private static final Map<SystemState, Supplier<SystemStateInterface>> stateMap;

    static {
        stateMap = new EnumMap<>(SystemState.class);
        stateMap.put(SystemState.WAITING, WaitingState::new);
        stateMap.put(SystemState.MAPPING, MappingState::new);
        stateMap.put(SystemState.RUNNING, RunningState::new);
        stateMap.put(SystemState.EMERGENCY, EmergencyState::new);
    }

    //Initial state
    private SystemStateManager() {
        setState(SystemState.WAITING);
    }

    public static SystemStateManager getInstance() {
        if(instance == null) instance = new SystemStateManager();
        return instance;
    }

    //Sets the state of the program to the given one
    public void setState(SystemState newState) {
        if(currentState == newState) return;
        
        if(currentState == SystemState.EMERGENCY) error = false;

        currentState = newState;
        currentStateConcrete = stateMap.get(newState).get();

        logger.info("Changing to system state " + newState);
    }

    //gets current state
    public SystemState getState() {
        return currentState;
    }

    public boolean needsTrip(int trippedSensor) {
        if(currentState == SystemState.MAPPING) {
            lastTrip = trippedSensor;
            return true;
        }
        return false;
    }

    public int getLastTrip() {
        int tempTrip = lastTrip;
        lastTrip = NO_TRIP;
        return tempTrip;
    }

    public void startEarly() {
        if(currentState == SystemState.WAITING) {
            setState(SystemState.MAPPING);
        }
        else {
            logger.log(Level.FINE, "Tried to start mapping, must be in state WAITING");
        }
    }


    //Takes a string id of a client id
    //adds a unresponsive client to the unresponsive client list in the database
    //only does this if in not in the waiting state
    public void addUnresponsiveClient(String id) {
        logger.log(Level.WARNING, "Client {0} has disconnected", id);
        if(currentState != SystemState.WAITING) {
            error = true;
            Client curClient = db.getClient(id);
            if(curClient.isTrainClient()) {
                TrainClient train = (TrainClient) curClient;
                train.unmap();
            }
            db.addUnresponsiveClient(id);
        }
    }

    //For every stat message received during emergency mode
    //Takes a string id of a client id
    //Adds a string if of a client of a packet received during emergency mode to the emergency mode message queue
    public void sendEmergencyPacketClientID(String id) {
        EmergencyState.addMessage(id);
    }

    //Checks to see if the system needs to go to emergency state, if already don't
    private void checkChange() {
        if(error && currentState != SystemState.EMERGENCY)
            setState(SystemState.EMERGENCY);
    }

    // If it is time for the current state to run its perform its operation, otherwise checkChange
    public void run() {
        long timeToWait = currentStateConcrete.getTimeToWait();

        if(System.currentTimeMillis() - timeWaited >= timeToWait) {
            //if the current state returns true, means it has finished and will be changed to its next appropriate state
            if(currentStateConcrete.performOperation()) {
                setState(currentStateConcrete.getNextState());
            }
            timeWaited = System.currentTimeMillis();
        }
        else {
            checkChange();
        }
    }
}