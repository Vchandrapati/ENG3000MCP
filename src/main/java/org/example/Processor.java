package org.example;

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
                Thread.currentThread().interrupt();
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
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processTrains() {
        //TODO
    }
}
