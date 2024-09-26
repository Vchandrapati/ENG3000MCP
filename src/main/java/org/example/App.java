package org.example;

import javax.swing.*;

public class App {
    private static volatile boolean isRunning = true;
    private static Server server;
    private static SystemStateManager systemStateManager;
    private static VisualiserScreen screen;

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
            server = Server.getInstance();
            // main loop for program
            while (isRunning()) {
                systemStateManager.run();
            }
        }).start();
    }

    // shutdown entire program
    public static void shutdown() {
        server.shutdown();
        setRunning(false);
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