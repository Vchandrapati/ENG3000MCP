package org.example;

import java.util.logging.Logger;

public interface SystemStateInterface {
    static final Logger logger = Logger.getLogger(SystemStateInterface.class.getName());
    final Database db = Database.getInstance();

    public boolean performOperation();
    public long getTimeToWait();
    public SystemState getNextState();
    public void reset();
}