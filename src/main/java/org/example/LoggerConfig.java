package org.example;

import javax.swing.*;
import java.io.IOException;
import java.util.logging.*;

public class LoggerConfig {

    public static void setupLogger(JTextArea logArea) {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        try {
            // Create a console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFormatter(new SimpleFormatter());

            // Create a FileHandler for logging to a file
            FileHandler fileHandler = new FileHandler("app.log", true); // Use ".log" as the file extension
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());

            // Create the custom LogTextAreaHandler
            ScreenLogHandler textAreaHandler = new ScreenLogHandler(logArea);
            textAreaHandler.setLevel(Level.ALL);

            // Remove default handlers
            logger.setUseParentHandlers(false);

            // Add handlers to the logger
            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);
            logger.addHandler(textAreaHandler);

            // Set the logger level to ALL to log everything
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize logger handler.", e);
        }
    }
}
