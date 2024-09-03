package org.example;

import java.io.IOException;
import java.util.logging.*;

public class LoggerConfig {

    public static void setupLogger() {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        // Remove default console handler to avoid duplicate logs
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }

        try {
            // Create a console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFormatter(new SimpleFormatter());

            // Create a FileHandler for logging to a file
            FileHandler fileHandler = new FileHandler("app.log", true); // Use ".log" as the file extension
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());

            // Add handlers to the logger
            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);

            // Set the logger level to ALL to log everything
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize logger handler.", e);
        }
    }
}
