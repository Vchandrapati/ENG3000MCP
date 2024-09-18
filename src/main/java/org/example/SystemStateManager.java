package org.example;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class SystemStateManager {
    private static volatile SystemStateManager instance;
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    //holds the current state and the current state concrete implementation
    private SystemState currentState;
    private SystemStateInterface currentStateConcrete;

    private boolean completedStartup = false;

    private static final int NO_TRIP = -1;
    private int lastTrip = NO_TRIP;

    private boolean ERROR = false;

    private long timeWaited = System.currentTimeMillis();
    private static final Map<SystemState, Supplier<SystemStateInterface>> stateMap;

    static {
        stateMap = new EnumMap<>(SystemState.class);
        stateMap.put(SystemState.STARTUP, StartupState::new);
        stateMap.put(SystemState.RESTARTUP, RestartupState::new);
        stateMap.put(SystemState.RUNNING, RunningState::new);
        stateMap.put(SystemState.EMERGENCY, EmergencyState::new);
    }

    //Initial state
    private SystemStateManager() {
        setState(SystemState.STARTUP);
    }

    public static SystemStateManager getInstance() {
        if(instance == null) instance = new SystemStateManager();
        return instance;
    }

    //Sets the state of the program to the given one
    public void setState(SystemState newState) {
        if(currentState == newState) return;
        
        if(currentStateConcrete != null )
            currentStateConcrete.reset();

        currentState = newState;
        currentStateConcrete = stateMap.get(newState).get();

        logger.info("Changing to system state " + newState);
    }

    //gets current state
    public SystemState getState() {
        return currentState;
    }

    public boolean needsTrip(int trippedSensor) {
        if(currentState == SystemState.STARTUP || currentState == SystemState.RESTARTUP) {
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

    public boolean hasCompletedStartup() {
        return completedStartup;
    }

    //Takes a string id of a client id
    public void addUnressponsiveClient(String id) {
        ERROR = true;
        //TODO, have to change this code to be correct
        //db.addDeadClient(db.getClientById(id));
    }

    //For every stat message recieved during emergency mode
    //Takes a string id of a client id
    public void sendEmergencyPacketClientID(String id) {
        
    }

    //Checks to see if the system needs to go to emergency state, if already dont 
    private void checkChange() {
        if(ERROR && currentState != SystemState.EMERGENCY) 
            setState(SystemState.EMERGENCY);
    }

    // If it is time for the current state to run its perform its operation, otherwise checkChange
    public void run() {
        long timeToWait = currentStateConcrete.getTimeToWait();

        if(System.currentTimeMillis() - timeWaited >= timeToWait) {
            //if the current state returns true, means it has finished and will be changed to its next appropriate state
            if(currentStateConcrete.performOperation()) {
                if(currentState == SystemState.STARTUP)
                    completedStartup = true;

                setState(currentStateConcrete.getNextState());
            }
            timeWaited = System.currentTimeMillis();
        }
        else {
            //if state is not ready to run, check if there is any changes that may affect the current state
            checkChange();
        }
    }
}