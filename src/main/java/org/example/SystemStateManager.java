package org.example;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

//Manages the states of the system
public class SystemStateManager {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final Database db = Database.getInstance();

    //singleton instance of class
    private static volatile SystemStateManager instance;

    //holds the current state and the current state concrete implementation
    private SystemState currentState;
    private SystemStateInterface currentStateConcrete;

    private static final int NO_TRIP = -1;
    private int lastTrip = NO_TRIP;

    private boolean error = false;

    private long timeWaited = System.currentTimeMillis();

    private static final Map<SystemState, Supplier<SystemStateInterface>> stateMap;

    private final String clientErrorReason = "double trip";

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

    //gets instance of system state manager, if none makes one
    public static SystemStateManager getInstance() {
        if(instance == null) instance = new SystemStateManager();
        return instance;
    }

    // If it is time for the current state to run its perform its operation, otherwise checkChange
    public void run() {
        long timeToWait = currentStateConcrete.getTimeToWait();

        if (System.currentTimeMillis() - timeWaited >= timeToWait) {
            //if the current state returns true, means it has finished and will be changed to its next appropriate state
            if (currentStateConcrete.performOperation()) {
                setState(currentStateConcrete.getNextState());
            }

            timeWaited = System.currentTimeMillis();
        } else {
            checkChange();
        }
    }

    //Checks to see if the system needs to go to emergency state, if already don't
    private void checkChange() {
        if(error && currentState != SystemState.EMERGENCY) {
            logger.log(Level.WARNING, "Error detected while in state {0}", currentState);
            setState(SystemState.EMERGENCY);
        }
    }

    //Sets the state of the program to the given one
    public void setState(SystemState newState) {
        if(currentState == newState) return;
        if(currentState == SystemState.EMERGENCY) error = false;

        currentState = newState;
        currentStateConcrete = stateMap.get(newState).get();

        logger.log(Level.INFO, "Changing to system state {0}", newState);
    }

    //gets current state
    public SystemState getState() {
        return currentState;
    }

    //For processor, if in mapping state all trips need to be redirected here
    public boolean needsTrip(int trippedSensor, boolean untrip) {
        if(currentState == SystemState.WAITING) return false;
        if(currentState == SystemState.MAPPING) {
            if(lastTrip == -1) {
                //accepts both trip and untrip for mapping, but only cares about untrip
                if(untrip) {
                    lastTrip = trippedSensor;
                    logger.log(Level.INFO, "System state manager has detected untrip {0}", trippedSensor);
                    return true;
                }
                //what if multiple checkpoints are dead before that, then the zone will be a combination of multiple checkpoint zones
                //TODO
                return false;
            }
            logger.log(Level.WARNING, "Multiple trips have occured in mapping, one at a time should happen");
            resetTrips();
            setState(SystemState.EMERGENCY);
            return false;
        }
        return false;
    }

    //returns the last trip and resets it after
    public int getLastTrip() {
        int tempTrip = lastTrip;
        lastTrip = NO_TRIP;
        return tempTrip;
    }

    //if the start early command is entered, the system is put into mapping if appriopriate
    public void startEarly() {
        if(currentState == SystemState.WAITING) {
            setState(SystemState.MAPPING);
            logger.log(Level.INFO, "Starting early");
        }
        else {
            logger.log(Level.FINE, "Tried to start mapping, must be in state WAITING");
        }
    }


    //Takes a string id of a client id
    //adds a unresponsive client to the unresponsive client list in the database
    //only does this if in not in the waiting state
    public void addUnresponsiveClient(String id, String reason) {
        if(!db.isClientUnresponsive(id)) {
            logger.log(Level.WARNING, "Client {0} has {1}", new Object[]{id, reason});
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
    }

    //For every stat message received during emergency mode
    //Takes a string id of a client id
    //Adds a string if of a client of a packet received during emergency mode to the emergency mode message queue
    public void sendEmergencyPacketClientID(String id) {
        EmergencyState.addMessage(id);
    }

    //returns the current timer for the current state
    //if returns -1 means state has no appropriate time, 
    public long getCurrentStateTimeout() {
        return currentStateConcrete.getStateTimeout();
    }

    public void resetTrips() {
        //reset all trips, in case any false trips happened
        List<Client> clients = db.getClients();
        for (Client client : clients) {
            synchronized(client) {
                if (client.id.contains("CH")) {
                    CheckpointClient check = (CheckpointClient) client;
                    check.resetTrip();
                }
            }
        }
    }
}