package org.example;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Processor implements Runnable, Constants{
    private static final Logger logger = Logger.getLogger(Processor.class.getName());
    private static Processor instance = null;
    private volatile boolean running = true;
    private Thread processorThread;
    private Database db;
    private Server server;

    private Processor(Database db, Server server) {
        this.db = db;
        this.server = server;
    }

    public static synchronized Processor getInstance(Database db, Server server) {
        if(instance == null)
            instance = new Processor(db, server);

        return instance;
    }

    public void start() {
        if(processorThread == null || !processorThread.isAlive()) {
            processorThread = new Thread(this::processTrains);
            processorThread.start();
        }
    }

    public void stop() {
        running = false;
        if(processorThread != null)
            try {
                processorThread.join();
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Error shutting down the thread", e);
            }
    }

    @Override
    public void run() {
        while(running) {
            processTrains();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Error sleeping Thread", e);
            }
        }
    }

    private void processTrains() {
        while(running) {
            synchronized (this) {
                Map<Integer, Train> trains = db.getTrains();
                if(trains.size() > 1) { // Only checks if there's more then 1 train on the track
                    trains.forEach((trainID, train) -> {
                        double safeDistance = 0.3; // Minimum safe distance which 10cm in front and behind the train
                        trains.entrySet().stream()
                                .filter(entry -> !entry.getKey().equals(trainID))
                                .forEach(entry -> {
                                    double otherTrainDistance = entry.getValue().distance;

                                    // Check if the current train is too close to a train in front
                                    if (otherTrainDistance >= train.distance && otherTrainDistance <= train.distance + safeDistance) {
                                        server.communicate(entry.getValue().id, "SPD," + entry.getValue().speed);
                                        train.speed = entry.getValue().speed;

                                        System.out.println("Train " + trainID + " is to Train " + entry.getKey());
                                    }
                                });
                    });
                }
            }
        }
    }
}
