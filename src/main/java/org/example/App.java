package org.example;

public class App {
    private static Server server;
    private volatile static boolean isRunning = true;

    public static void main(String[] args) {
        LoggerConfig.setupLogger();
        new CommandHandler();
    }

    //For running the program without the terminal
    public static void mainTest() {
        LoggerConfig.setupLogger();
        start();
    }

    //shutsdown entire program
    public static void shutdown() {
        server.shutdown();
        Database.getInstance().shutdown();
        isRunning = false;
    }

    //starts entire program
    public static void start() {
        Thread mainThread = new Thread(() -> {
            SystemStateManager systemStateManager = SystemStateManager.getInstance();
            server = new Server();
            server.startStatusScheduler();
            //main loop for program
            while(isRunning) {
                systemStateManager.run();
            }
        });
        mainThread.start();
    }
}
