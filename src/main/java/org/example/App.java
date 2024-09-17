
package org.example;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.net.InetAddress;
import java.util.logging.Logger;

public class App {
    private static Server server;
    public static volatile boolean isRunning = true;
    private static SystemStateManager systemStateManager;
    private static VisualiserScreen screen;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            screen = new VisualiserScreen();
            screen.setVisible(true);
            LoggerConfig.setupLogger(screen.getLogArea());
            startMCP();
        });

        new CommandHandler();
    }

    public static void startMCP() {
        TrainClient c1 = new TrainClient(InetAddress.getLoopbackAddress(), 0000, "BR01");
        TrainClient c2 = new TrainClient(InetAddress.getLoopbackAddress(), 0000, "BR02");
        TrainClient c3 = new TrainClient(InetAddress.getLoopbackAddress(), 0000, "BR03");

        Database.getInstance().addTrain(c1.id, c1);
        Database.getInstance().addTrain(c2.id, c2);
        Database.getInstance().addTrain(c3.id, c3);
        Database.getInstance().updateTrainBlock(c1.getId(), 0);
        Database.getInstance().updateTrainBlock(c2.getId(), 2);
        Database.getInstance().updateTrainBlock(c3.getId(), 5);

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
