package org.example;

import java.util.logging.Logger;

public interface SystemStateInterface {
    static final Logger logger = Logger.getLogger(CommandHandler.class.getName());

    public boolean performOperation();
    public long getTimeToWait();
    public SystemState getNextState();
    public void reset();
}