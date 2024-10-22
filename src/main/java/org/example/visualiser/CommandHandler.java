package org.example.visualiser;

import org.example.App;
import org.example.Database;
import org.example.client.AbstractClient;
import org.example.client.ReasonEnum;
import org.example.events.ClientErrorEvent;
import org.example.events.EventBus;
import org.example.events.NewStateEvent;
import org.example.events.StateChangeEvent;
import org.example.state.SystemState;

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
    private SystemState currentState;
    private EventBus eventBus;

    // Set of commands
    static {
        commands = new HashSet<>();
        commands.add("start mapping");
        commands.add("quit");
        commands.add("override emergency");
        commands.add("start waiting");
        commands.add("disconnect <ID>");
        commands.add("help");
    }

    private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

    public CommandHandler(EventBus eventBus) {
        Thread commandThread = new Thread(this, "CommandHandler-Thread");
        commandThread.start();
        this.eventBus = eventBus;

        eventBus.subscribe(StateChangeEvent.class, this::updateCurrentState);
    }

    public void updateCurrentState (StateChangeEvent event) {
        this.currentState = event.getState();
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
                executeCommand(input);
                logger.log(Level.INFO, "Command executed: {0}", input);

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
                if (currentState == SystemState.WAITING)
                    eventBus.publish(new NewStateEvent(SystemState.MAPPING));
                else
                    throw new InvalidCommandException("Not in waiting state");
                break;
            case "override emergency":
                if (currentState == SystemState.EMERGENCY)
                    eventBus.publish(new NewStateEvent(SystemState.MAPPING));
                else
                    throw new InvalidCommandException("Not in emergency state");
                break;
            case "start waiting":
                if (currentState != SystemState.WAITING)
                    eventBus.publish(new NewStateEvent(SystemState.WAITING));
                else
                    throw new InvalidCommandException("Already in waiting state");
                break;
            default:

                if (input.contains("disconnect")) {
                    processDisconnect(input);
                } else {
                    throw new InvalidCommandException("Invalid command");
                }
        }
    }

    private void processDisconnect(String input) throws InvalidCommandException {
        String[] array = input.split(" ");
        if (array.length == 2 && array[0].equals("disconnect")
                && Database.getInstance().getClient(array[1], AbstractClient.class).isPresent()) {
            eventBus.publish(new ClientErrorEvent(array[1], ReasonEnum.TODISCONNECT));
        } else {
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
