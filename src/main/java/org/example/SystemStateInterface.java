package org.example;

public interface SystemStateInterface {
    boolean performOperation();
    long getTimeToWait();
    SystemState getNextState();
}