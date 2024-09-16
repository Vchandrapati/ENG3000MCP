package org.example;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles commands from the console given by the user
 */
public class CommandHandler implements Runnable{
    private static final Set<String> commands;
    private static final Logger logger = Logger.getLogger(CommandHandler.class.getName());
    private static volatile boolean isRunning = true;
    private boolean startedStartup = false;
    private Thread commandThread;
    //Set of commands
    static {
        commands = new HashSet<>();
        commands.add("start startup");
        commands.add("quit");
        commands.add("help");
        commands.add("start running");
    }

    public CommandHandler() {
        commandThread = new Thread(this);
        commandThread.start();
    }
    
    //Main loop for command handler
    @Override
    public void run() {
        logger.info("MCP online, please input a command, type help to see a list of commands");


        while(isRunning) {
            try (Scanner scanner = new Scanner(System.in);) {
                //tries to get console input
                String input = scanner.nextLine();
                //if valid processes the command and prints to console
                if (commands.contains(input)) {
                    executeCommand(input);
                    logger.log(Level.INFO, "Command executed: {0}", input);
                } else {
                    logger.log(Level.WARNING,"Invalid command: {0}",  input);
                }
            //if command is invalid throw exception
            } catch (InvalidCommandException e) {
                logger.severe("Error: " + e.getMessage());
            } catch (Exception e) {
                logger.severe("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    //takes an input string and executes its command, if invalid throw exception
    private void executeCommand(String input) throws InvalidCommandException {
        // using the substring find the command
        switch(input) {
            case "help":
                help();
                break;
            case "quit":
                App.shutdown();
                break;
            case "start startup":
                if(!startedStartup) StartupState.startEarly();
                else throw new InvalidCommandException("Has already been used");
                break;
            case "start running":
                if(!startedStartup) SystemStateManager.getInstance().setState(SystemState.RUNNING);
                else throw new InvalidCommandException("Has already been used");
                break;
            default:
                throw new InvalidCommandException("Invalid command");
        }
    }

    // Prints all set commands to the console
    private void help() {
        StringBuilder commandString = new StringBuilder();
        for (String command : commands) {
            commandString.append("\n").append(command);
        }

        logger.info(commandString.toString());
    }

    //Exception for invalid commands
    private static class InvalidCommandException extends Exception {
        public InvalidCommandException(String message) {
            super(message);
        }
    }
}
