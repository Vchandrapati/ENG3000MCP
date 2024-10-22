package org.example;

import org.example.events.EventBus;
import org.example.messages.ClientFactory;
import org.example.messages.MessageHandler;
import org.example.messages.Server;
import org.example.messages.StatusScheduler;
import org.example.state.SystemStateManager;
import org.example.visualiser.CommandHandler;
import org.example.visualiser.VisualiserScreen;

import javax.swing.*;

public class App {
    private static Server server;
    private static StatusScheduler statScheduler;
    private static SystemStateManager systemStateManager;
    private static VisualiserScreen screen;
    private static ClientFactory clientFactory;
    private static final EventBus eventBus = EventBus.getInstance();
    private static Processor processor;
    private static MessageHandler messageHandler;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            screen = new VisualiserScreen(eventBus);
            screen.setVisible(true);
            startMCP();
        });
    }

    public static void startMCP() {
        new Thread(() -> {
            systemStateManager = SystemStateManager.getInstance(eventBus);
            server = Server.getInstance(eventBus);
            messageHandler = new MessageHandler(eventBus);
            processor = new Processor(eventBus, Database.getInstance());
            clientFactory = new ClientFactory(eventBus);
            clientFactory.readFromFile("locations.txt");
            statScheduler = new StatusScheduler(eventBus);
            statScheduler.startStatusScheduler();
        }).start();
    }

    // shutdown entire program
    public static void shutdown() {
        server.shutdown();
        statScheduler.shutdown();
        eventBus.shutdown();
        systemStateManager.shutdown();
        CommandHandler.shutdown();
        Thread.currentThread().interrupt();
    }
}
