package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class Database implements Runnable {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static volatile Database db;
    private BlockingQueue<Runnable> taskQueue;
    private static ConcurrentHashMap<String, TrainClient> trains;
    private static ConcurrentHashMap<String, StationClient> stations;
    private static ConcurrentHashMap<String, CheckpointClient> checkpoints;
    private static ConcurrentHashMap<String, Integer> trainBlockMap;

    private static Boolean running = false;

    private Database() {
        running = true;

        trains = new ConcurrentHashMap<>();
        stations = new ConcurrentHashMap<>();
        checkpoints = new ConcurrentHashMap<>();
        trainBlockMap = new ConcurrentHashMap<>();

        taskQueue = new LinkedBlockingQueue<>();

        Thread dbThread = new Thread(this);
        dbThread.start();
    }

    public static synchronized Database getInstance() {
        if (db == null)
            db =  new Database();

        return db;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Runnable task = taskQueue.take();
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        running = false;
        Thread.currentThread().interrupt();
    }

    private void submitTask(Runnable r) {
        try {
            taskQueue.put(r);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("Task submission interrupted.");
        }
    }

    public void addTrain(String id, TrainClient tr) {
        submitTask(() -> {
            TrainClient prevValue = trains.putIfAbsent(id, tr);
            if (prevValue != null) {
                logger.warning("Attempted to add duplicate train with id: " + id);
            }
        });
    }

    public void addStation(String id, StationClient st) {
        submitTask(() -> {
            StationClient prevValue = stations.putIfAbsent(id, st);
            if (prevValue != null) {
                logger.warning("Attempted to add duplicate station with id: " + id);
            }
        });
    }

    public void addCheckpoint(String id, CheckpointClient ch) {
        submitTask(() -> {
            CheckpointClient prevValue = checkpoints.putIfAbsent(id, ch);
            if (prevValue != null) {
                logger.warning("Attempted to add duplicate checkpoint with id: " + id);
            }
        });
    }

    public TrainClient getTrain(String id) {
        final TrainClient[] result = new TrainClient[1];
        submitTask(() -> result[0] = trains.get(id));
        return result[0];
    }

    public StationClient getStation(String id) {
        final StationClient[] result = new StationClient[1];
        submitTask(() -> result[0] = stations.get(id));
        return result[0];
    }

    public CheckpointClient getCheckpoint(String id) {
        final CheckpointClient[] result = new CheckpointClient[1];
        submitTask(() -> result[0] = checkpoints.get(id));
        return result[0];
    }

    public CheckpointClient getHit() {
        for (CheckpointClient checkpoint : checkpoints.values()) {
            if (checkpoint.isTripped()) {
                return checkpoint;
            }
        }

        return null;
    }

    public void updateTrainBlock(String traindId, int newBlock) {
        submitTask(() -> trainBlockMap.put(traindId, newBlock));
    }

    public void getTrainblock(String traindId) {
        submitTask(() -> trainBlockMap.get(traindId));
    }

    public boolean isBlockOccupied(int blockId) {
        final boolean[] result = new boolean[1];
        submitTask(() -> result[0] = checkpoints.containsValue(blockId));
        return result[0];
    }

    public String getLastTrainInBlock(int blockId) {
        return trainBlockMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(blockId))
                .map(Map.Entry::getKey)
                .reduce((first, second) -> second)
                .orElse(null);
    }

    public int getTrainCount() {
        return trains.size();
    }
    public int getStationCount() {
        return stations.size();
    }
    public int getCheckpointCount() {
        return checkpoints.size();
    }
}
