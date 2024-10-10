package org.example;

import javax.swing.*;

public class App {
    private static volatile boolean isRunning = true;
    private static Server server;
    private static StatHandler statReq;
    private static SystemStateManager systemStateManager;
    private static VisualiserScreen screen;
    private static ClientCreator clinetCreator;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            screen = new VisualiserScreen();
            screen.setVisible(true);
            startMCP();
        });
    }

    public static void startMCP() {
        new Thread(() -> {
            systemStateManager = SystemStateManager.getInstance();
            clinetCreator = ClientCreator.getInstance();
            clinetCreator.readFromFile("src\\main\\java\\org\\example\\locations.txt");
            server = Server.getInstance();
            statReq = StatHandler.getInstance();
            statReq.startStatusScheduler();


            // main loop for program
            while (isRunning()) {
                systemStateManager.run();
            }
        }).start();
    }

    // shutdown entire program
    public static void shutdown() {
        server.shutdown();
        statReq.shutdown();
        setRunning(false);
        CommandHandler.shutdown();
        Thread.currentThread().interrupt();
    }

    // Getter for isRunning
    public static boolean isRunning() {
        return isRunning;
    }

    // Setter for isRunning with additional validation if needed
    public static void setRunning(boolean running) {
        isRunning = running;
    }
}
