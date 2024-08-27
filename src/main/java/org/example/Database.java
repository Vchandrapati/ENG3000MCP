package org.example;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class Database implements Runnable {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static volatile Database db;

    private BlockingQueue<Runnable> taskQueue;

    private static volatile HashMap<String, TrainClient> trains;
    private static volatile HashMap<String, StationClient> stations;
    private static volatile HashMap<String, CheckpointClient> checkpoints;

    private static Boolean running = false;

    private Database() {
        running = true;

        trains = new HashMap<String, TrainClient>();
        stations = new HashMap<String, StationClient>();
        checkpoints = new HashMap<String, CheckpointClient>();

        taskQueue = new LinkedBlockingQueue<>();

        Thread dbThread = new Thread(this);
        dbThread.start();
    }

    public static synchronized Database getInstance() {
        if (db == null) {
            return new Database();
        }
        return db;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Runnable task = taskQueue.take();
                task.run();

            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
        }
    }

    public void stutdown() {
        running = false;
        submitTask(() -> {
        });
    }

    private void submitTask(Runnable r) {
        try {
            taskQueue.put(r);
        } catch (InterruptedException e) {
            // TODO: handle exception
        }
    }

    public void addTrain(String id, TrainClient tr) {
        submitTask(() -> {
            TrainClient prevValue = trains.putIfAbsent(id, tr);
            if (prevValue != null) {
                // Log
            }
        });
    }

    public void addStation(String id, StationClient st) {
        submitTask(() -> {
            StationClient prevValue = stations.putIfAbsent(id, st);
            if (prevValue != null) {
                // Log
            }
        });
    }

    public void addCheckpoint(String id, CheckpointClient ch) {
        submitTask(() -> {
            CheckpointClient prevValue = checkpoints.putIfAbsent(id, ch);
            if (prevValue != null) {
                // Log
            }
        });
    }

    public TrainClient getTrain(String id) {
        final TrainClient[] result = new TrainClient[1];

        submitTask(() -> {
            result[0] = trains.get(id);
        });

        return result[0];
    }

    public StationClient getStation(String id) {
        final StationClient[] result = new StationClient[1];

        submitTask(() -> {
            result[0] = stations.get(id);
        });

        return result[0];
    }

    public CheckpointClient getCheckpoint(String id) {
        final CheckpointClient[] result = new CheckpointClient[1];

        submitTask(() -> {
            result[0] = checkpoints.get(id);
        });

        return result[0];
    }

}
