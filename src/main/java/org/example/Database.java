package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class Database {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static volatile Database db;
    private BlockingQueue<Runnable> taskQueue;
    public static ConcurrentHashMap<String, TrainClient> trains;
    private static ConcurrentHashMap<String, StationClient> stations;
    private static ConcurrentHashMap<String, CheckpointClient> checkpoints;
    private static ConcurrentHashMap<String, Integer> trainBlockMap;
    private static ConcurrentHashMap<String, Integer> stationLocations;

    private static Boolean running = false;

    private ExecutorService executor;

    private Database() {
        running = true;

        trains = new ConcurrentHashMap<>();
        stations = new ConcurrentHashMap<>();
        checkpoints = new ConcurrentHashMap<>();
        trainBlockMap = new ConcurrentHashMap<>();
        stationLocations = new ConcurrentHashMap<>();

        taskQueue = new LinkedBlockingQueue<>();

        executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized Database getInstance() {
        if (db == null)
            db = new Database();

        return db;
    }

    public void addTrain(String id, TrainClient tr) {
        executor.submit(() -> {
            TrainClient prevValue = trains.putIfAbsent(id, tr);
            if (prevValue != null) {
                logger.warning("Attempted to add duplicate train with id: " + id);
            }
        });
    }

    public void addStation(String id, StationClient st) {
        executor.submit(() -> {
            StationClient prevValue = stations.putIfAbsent(id, st);
            if (prevValue != null) {
                logger.warning("Attempted to add duplicate station with id: " + id);
            }
        });
    }

    public void addCheckpoint(String id, CheckpointClient ch) {
        executor.submit(() -> {
            CheckpointClient prevValue = checkpoints.putIfAbsent(id, ch);
            if (prevValue != null) {
                logger.warning("Attempted to add duplicate checkpoint with id: " + id);
            }
        });
    }

    public Future<TrainClient> getTrain(String id) {
        return executor.submit(() -> {
            return trains.get(id);
        });
    }

    public Future<StationClient> getStation(String id) {
        return executor.submit(() -> {
            return stations.get(id);
        });
    }

    public Future<CheckpointClient> getCheckpoint(String id) {
        return executor.submit(() -> {
            return checkpoints.get(id);
        });
    }

    // Deprecated function
    // public CheckpointClient getHit() {
    // for (CheckpointClient checkpoint : checkpoints.values()) {
    // if (checkpoint.isTripped()) {
    // return checkpoint;
    // }
    // }

    // return null;
    // }

    public void updateTrainBlock(String traindId, int newBlock) {
        executor.submit(() -> trainBlockMap.put(traindId, newBlock));
    }

    public void getTrainblock(String traindId) {
        executor.submit(() -> trainBlockMap.get(traindId));
    }

    // This function is not being used right im pretty sure
    public Future<Boolean> isBlockOccupied(int blockId) {
        return executor.submit(() -> {
            return checkpoints.containsValue(blockId);
        });
    }

    public Future<String> getLastTrainInBlock(int blockId) {
        return executor.submit(() -> {
            return trainBlockMap.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().equals(blockId))
                    .map(Map.Entry::getKey)
                    .reduce((first, second) -> second)
                    .orElse(null);
        });
    }

    public Future<Integer> getTrainCount() {
        return executor.submit(() -> {
            return trains.size();
        });
    }

    public Future<Integer> getStationCount() {
        return executor.submit(() -> {
            return stations.size();
        });
    }

    public Future<Integer> getCheckpointCount() {
        return executor.submit(() -> {
            return checkpoints.size();
        });
    }
}
