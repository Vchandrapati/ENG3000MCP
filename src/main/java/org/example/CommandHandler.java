package org.example;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles commands from the console given by the user
 */
public class CommandHandler implements Runnable {
    private static final Set<String> commands;
    private static boolean isRunning = true;

    // Set of commands
    static {
        commands = new HashSet<>();
        commands.add("start mapping");
        commands.add("quit");
        commands.add("override emergency");
        commands.add("start waiting");
        commands.add("help");
    }

    private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

    public CommandHandler() {
        Thread commandThread = new Thread(this, "CommandHandler-Thread");
        commandThread.start();
    }

    // Main loop for command handler
    @Override
    public void run() {
        logger.log(Level.INFO,
                "MCP online, please input a command, type help to see a list of commands");

        while (isRunning) {
            try {
                // tries to get command from queue if exists
                String input = commandQueue.take();

                // if valid processes the command and prints to console
                if (commands.contains(input)) {
                    executeCommand(input);
                    logger.log(Level.INFO, "Command executed: {0}", input);
                } else {
                    logger.log(Level.WARNING, "Invalid command: {0}", input);
                }
                // if command is invalid throw exception
            } catch (InvalidCommandException e) {
                logger.log(Level.WARNING, "Invalid command");
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "An unexpected error occurred: ", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void processInput(String input) {
        try {
            commandQueue.put(input); // Submit the command to the queue
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Failed to submit command: ", e);
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }

    // takes an input string and executes its command, if invalid throw exception
    private void executeCommand(String input) throws InvalidCommandException {
        // using the substring find the command
        switch (input) {
            case "help":
                help();
                break;
            case "quit":
                App.shutdown();
                break;
            case "start mapping":
                if (SystemStateManager.getInstance().getState() == SystemState.WAITING)
                    SystemStateManager.getInstance().setState(SystemState.MAPPING);
                else
                    throw new InvalidCommandException("Not in waiting state");
                break;
            case "override emergency":
                if (SystemStateManager.getInstance().getState() == SystemState.EMERGENCY)
                    SystemStateManager.getInstance().setState(SystemState.MAPPING);
                else
                    throw new InvalidCommandException("Not in emergency state");
                break;
            case "start waiting":
                if (SystemStateManager.getInstance().getState() != SystemState.WAITING)
                    SystemStateManager.getInstance().setState(SystemState.WAITING);
                else
                    throw new InvalidCommandException("Already in waiting state");
                break;
            default:
                throw new InvalidCommandException("Invalid command");
        }
    }

    // Prints all set commands to the console
    private void help() {
        StringBuilder commandString = new StringBuilder();
        commandString.append("\n").append("List of valid commands");

        for (String command : commands)
            commandString.append("\n").append(command);

        logger.log(Level.INFO, "{0}", commandString);
    }

    // Exception for invalid commands
    private class InvalidCommandException extends Exception {
        public InvalidCommandException(String message) {
            logger.log(Level.WARNING, message);
        }
    }

    public static void shutdown() {
        isRunning = false;
    }
}
