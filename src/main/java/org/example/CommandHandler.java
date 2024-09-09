package org.example;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandHandler implements Runnable{
    //Class for handling commands in the console given by the user

    private static final List<String> commands;
    private static final Logger logger = Logger.getLogger(CommandHandler.class.getName());
    private volatile static boolean isRunning = true;
    private boolean startedStartup = false;
    private Thread commandThread;
    private Database db = Database.getInstance();

    //Set of commands
    static {
        commands = new ArrayList<>();
        commands.add("start");
        commands.add("quit");
        commands.add("add brmax");
        commands.add("add stmax");
        commands.add("add chmax");
        commands.add("help");
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

        //Gets a substring of the command
        String command = "";
        for (String string : commands) {
            if(input.contains(string)) {
                command = string;
                break;
            }
        }

        //using the substring find the command
        switch(command) {
            case "":
                return false;
            case "help":
                help();
                return true;
            case "quit":
                isRunning = false;
                App.shutdown();
                return true;
            case "start":
                if(!startedStartup) StartupState.startEarly();
                else throw new InvalidCommandException("Has already been used");
                return true;
            default:
                return false;
        }
    }


    //does not work, only here if needed again later
    private boolean deprecatedCommands(String command, String input) throws InvalidCommandException {
        switch(command) {
            case "":
                return false;
            case "help":
                help();
                return true;
            case "quit":
                isRunning = false;
                App.shutdown();
                return true;
            case "start":
                App.start();
                return true;
            case "add brmax":
                db.setMaxBR(add(input));
                return true;
            case "add stmax":
                db.setMaxBR(add(input));
                return true;
            case "add chmax":
                 db.setMaxBR(add(input));
                return true;
            default:
                return false;
        }
    }



    //processes getting and parsing the number to add the current add command
    private int add(String input) throws InvalidCommandException{
        try {
            String[] tokens = input.split(" ");
            if (tokens.length != 3) {
                throw new InvalidCommandException("Missing argument for adding");
            }
            else {
                int amount = Integer.parseInt(tokens[2]);
                return amount;
            }
        } catch (Exception e) {
            throw new InvalidCommandException("Error handling command");
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
