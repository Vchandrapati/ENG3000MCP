package org.example;

public class App {
    private static Server server;
    public volatile static boolean isRunning = true;
    private static SystemStateManager systemStateManager;

    public static void main(String[] args) {
        LoggerConfig.setupLogger();
        start();
        new CommandHandler();
    }

    //shutsdown entire program
    public static void shutdown() {
        server.shutdown();
        Database.getInstance().shutdown();
        isRunning = false;
    }

    public static void start() {
        new Thread(() -> {
            systemStateManager = SystemStateManager.getInstance();
            server = new Server();
            server.startStatusScheduler();
            //main loop for program
            while(isRunning) {
                systemStateManager.run();
            }
        }).start();
    }
}
