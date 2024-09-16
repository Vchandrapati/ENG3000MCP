
package org.example;

public class App {
    private static Server server;
    public static volatile boolean isRunning = true;
    private static SystemStateManager systemStateManager;

    public static void main(String[] args) {
        LoggerConfig.setupLogger();
        start();
        new CommandHandler();
    }

    public static void start() {
        new Thread(() -> {
            systemStateManager = SystemStateManager.getInstance();
            server = Server.getInstance();
            server.startStatusScheduler();

            // main loop for program
            while(isRunning) {
                systemStateManager.run();
            }
        }).start();
    }

    // shutdown entire program
    public static void shutdown() {
        server.shutdown();
        isRunning = false;
        Thread.currentThread().interrupt();
    }
}
