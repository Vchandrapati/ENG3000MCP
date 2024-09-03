package org.example;

public class SystemStateManager {
    private static SystemStateManager instance;
    private SystemState currentState;

    private SystemStateManager() {
        currentState = SystemState.STARTUP; // Initial state
    }

    public static synchronized SystemStateManager getInstance() {
        if (instance == null) {
            instance = new SystemStateManager();
        }
        return instance;
    }

    public synchronized void setState(SystemState newState) {
        this.currentState = newState;
    }

    public synchronized SystemState getState() {
        return this.currentState;
    }
}

