package org.example;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SystemStateManager {
    private static SystemStateManager instance;
    private static final Logger logger = Logger.getLogger(SystemStateManager.class.getName());

    //holds the current state and the current state concrete implmenetation
    private SystemState currentState;
    private SystemStateInterface currentStateConcrete;
    private boolean completedStartup = false;

    private int lastTrip = -1;

    private long timeWaited = System.currentTimeMillis();

    //Initial state
    private SystemStateManager() {
        setState(SystemState.STARTUP);
    }

    public static synchronized SystemStateManager getInstance() {
        if (instance == null) {
            instance = new SystemStateManager();
        }
        return instance;
    }

    //Sets the state of the program to the given one
    public synchronized void setState(SystemState newState) {
        if (currentState == newState) {
            return;
        }

        if(currentStateConcrete != null )currentStateConcrete.reset();
        currentState = newState;

        if(newState == SystemState.STARTUP && !completedStartup) currentStateConcrete = new StartupState();
        else if(newState == SystemState.RESTARTUP) currentStateConcrete = new RestartupState();
        else if(newState == SystemState.RUNNING) currentStateConcrete = new RunningState();
        else currentStateConcrete = new EmergencyState();

        logger.info("Changing to system state " + newState);
    }

    //gets current state
    public synchronized SystemState getState() {
        return currentState;
    }

    public boolean needsTrip(int trippedSensor) {
        if(currentState == SystemState.STARTUP || currentState == SystemState.STARTUP) {
            lastTrip = trippedSensor;
            return true;
        }
        return false;
    }

    public int getLastTrip() {
        int tempTrip = lastTrip;
        lastTrip = -1;
        return tempTrip;
    }

    //For emergency state, message handler can check if a status has not been responded 
    // or other issue to do SystemStateManager.setState(SystemState.Emergency)

    //Checks to see if the system needs to change states
    private synchronized void checkChange() {
        //TODO
        //check if a client has not responded to a status message
        //if so change to emergency state
    }

    //If it is time for the current state to run its perform its operation, otherwise checkChange
    public synchronized void run() {
        long timeToWait = currentStateConcrete.getTimeToWait();

        if(System.currentTimeMillis() - timeWaited >= timeToWait) {
            //if the current state returns true, means it has finished and will be changed to its next appropriate state
            if(currentStateConcrete.performOperation()) {
                if(currentState == SystemState.STARTUP) completedStartup = true;
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