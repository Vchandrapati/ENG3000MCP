package org.example;

import org.example.events.EventBus;
import org.example.messages.ClientFactory;
import org.example.messages.Server;
import org.example.messages.StatusScheduler;
import org.example.state.SystemStateManager;
import org.example.visualiser.CommandHandler;
import org.example.visualiser.VisualiserScreen;

import javax.swing.*;

public class App {
    private static volatile boolean isRunning = true;
    private static Server server;
    private static StatusScheduler statScheduler;
    private static SystemStateManager systemStateManager;
    private static VisualiserScreen screen;
    private static ClientFactory clientFactory;
    private static final EventBus eventBus = EventBus.getInstance();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Database.coom();
            screen = new VisualiserScreen(eventBus);
            screen.setVisible(true);
            startMCP();
        });
    }

    public static void startMCP() {
        new Thread(() -> {
            systemStateManager = SystemStateManager.getInstance(eventBus);
            clientFactory = new ClientFactory(eventBus);
            clientFactory.readFromFile("locations.txt");
            server = Server.getInstance(eventBus);
            statScheduler = new StatusScheduler(eventBus);
            statScheduler.start();

            while (isRunning()) {
                systemStateManager.run();
            }
        }).start();
    }

    // shutdown entire program
    public static void shutdown() {
        server.shutdown();
        statScheduler.shutdown();
        eventBus.shutdown();
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
