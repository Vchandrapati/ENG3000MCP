package org.example;

import java.util.*;
import java.util.logging.Logger;

public class CommandHandler implements Runnable{
    //Class for handling commands in the console given by the user

    private static final List<String> commands;
    private static final Logger logger = Logger.getLogger(CommandHandler.class.getName());
    private volatile static boolean isRunning = true;
    private boolean startedStartup = false;
    private Thread commandThread;

    //Set of commands
    static {
        commands = new ArrayList<>();
        commands.add("start startup");
        commands.add("quit");
        commands.add("help");
        commands.add("start running");
        //For adding syntax is "add brmax x" where x is the number to add
    }

    public CommandHandler() {
        commandThread = new Thread(this);
        commandThread.start();
    }
    
    //Main loop for command handler
    @Override
    public void run() {
        logger.info("MCP online, please input a command, type help to see a list of commands");
        Scanner scanner = new Scanner(System.in);

        while(isRunning) {
            try {
                //tries to get console input
                String input = scanner.nextLine();
                //if valid processes the command and prints to console
                if (isValid(input)) {
                    logger.info("Valid command executed: " + input);
                } else {
                    logger.warning("Invalid command: " + input);
                }
            //if command is invalid throw exception
            } catch (InvalidCommandException e) {
                logger.severe("Error: " + e.getMessage());
            } catch (Exception e) {
                logger.severe("An unexpected error occurred: " + e.getMessage());
            }
        }
        scanner.close();
    }

    //takes an input string and executes its command, if invalid throw exception
    private boolean isValid(String input) throws InvalidCommandException {
        //using the substring find the command
        switch(input) {
            case "":
                return false;
            case "help":
                help();
                return true;
            case "quit":
                isRunning = false;
                App.shutdown();
                return true;
            case "start startup":
                if(!startedStartup) StartupState.startEarly();
                else throw new InvalidCommandException("Has already been used");
                return true;
            case "start running":
                if(!startedStartup) SystemStateManager.getInstance().setState(SystemState.RUNNING);
                else throw new InvalidCommandException("Has already been used");
                return true;
            default:
                throw new InvalidCommandException("Invalid command");
        }
    }

    //prints all set commmnds to the console
    private void help() {
        String commandString = "";
        for (String string : commands) {
            commandString += "\n" + string;
        }
        logger.info(commandString);
    }

    //Exception for invalid commands
    class InvalidCommandException extends Exception {
        public InvalidCommandException(String message) {
            super(message);
        }
    }
}
