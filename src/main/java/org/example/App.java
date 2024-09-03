package org.example;

import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    public static void main(String[] args) {
        LoggerConfig.setupLogger();
        Logger logger = Logger.getLogger(App.class.getName());
        Server server = new Server();
        SystemStateManager systemStateManager = SystemStateManager.getInstance();

        StartupState startupState = new StartupState();
        boolean startupComplete = false;

        while (!startupComplete) {
            startupComplete = startupState.performOperation();

            try {
                Thread.sleep(1000); // Wait for a short time before checking again
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Startup process interrupted", e);
            }
        }

        systemStateManager.setState(SystemState.RUNNING);
        server.startStatusScheduler();
        logger.info("System is now in RUNNING mode.");

    }
}
