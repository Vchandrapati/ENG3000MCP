package org.example;

import java.io.IOException;
import java.util.logging.*;

public class LoggerConfig {
    public static void setupLogger() {
        try {
            // Create a console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);


            // Create a FileHandler for logging to a file
            FileHandler fileHandler = new FileHandler("app.txt", true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());

            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

            // Add handler to logger
            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);

            // Set the logger level to ALL to log everything
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, "Failed to initialise logger handler.", e);
        }
    }
}
