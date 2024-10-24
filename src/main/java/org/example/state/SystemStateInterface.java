package org.example.state;

public interface SystemStateInterface {
    boolean performOperation ();

    long getTimeToWait ();

    SystemState getNextState ();
}