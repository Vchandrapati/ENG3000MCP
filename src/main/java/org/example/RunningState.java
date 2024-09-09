package org.example;

public class RunningState implements SystemStateInterface{

    @Override
    public boolean performOperation() {
        return false;
    }

    @Override
    public long getTimeToWait() {
        return 1000;
    }

    @Override
    public SystemState getNextState() {
        return SystemState.EMERGENCY;
    }
    
}
