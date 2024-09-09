package org.example;

public class SystemStateManager {
    private static SystemStateManager instance;

    //holds the current state and the current state concrete implmenetation
    private SystemState currentState;
    private SystemStateInterface currentStateConcrete;

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

        this.currentState = newState;

        if(newState == SystemState.STARTUP) currentStateConcrete = new StartupState();
        else if(newState == SystemState.RESTARTUP) currentStateConcrete = new RestartupState();
        else if(newState == SystemState.RUNNING) currentStateConcrete = new RunningState();
        else currentStateConcrete = new EmergencyState();
    }

    //gets current state
    public synchronized SystemState getState() {
        return this.currentState;
    }

    //Checks to see if the system needs to change states
    private synchronized void checkChange() {
        //TODO
        //check if a client has not responded to a status message
        //if so change to emergency state
    }

    //If it is time for the current state to run its performs its operation, otherwise checkChange
    public synchronized void run() {
        long timeToWait = currentStateConcrete.getTimeToWait();

        if(System.currentTimeMillis() - timeWaited >= timeToWait) {
            //if the current state returns true, means it has finished and will be changed to its next appropriate state
            if(currentStateConcrete.performOperation()) setState(currentStateConcrete.getNextState());
            timeWaited = System.currentTimeMillis();
        }
        else {
            //if state is not ready to run, check if there is any changes that may affect the current state
            checkChange();
        }
    }
}

