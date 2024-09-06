package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class Database {
    private static final Logger logger = Logger.getLogger(Database.class.getName());
    private static volatile Database db;
    private final ConcurrentHashMap<String, TrainClient> trains;
    private final ConcurrentHashMap<String, StationClient> stations;
    private final ConcurrentHashMap<String, CheckpointClient> checkpoints;
    private final ConcurrentHashMap<String, Integer> trainBlockMap;
    private final ExecutorService executor;

    private Database() {
        trains = new ConcurrentHashMap<>();
        stations = new ConcurrentHashMap<>();
        checkpoints = new ConcurrentHashMap<>();
        trainBlockMap = new ConcurrentHashMap<>();

        executor = Executors.newCachedThreadPool();
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
        return executor.submit(() -> trains.get(id));
    }

    public Future<StationClient> getStation(String id) {
        return executor.submit(() -> stations.get(id));
    }

    public Future<CheckpointClient> getCheckpoint(String id) {
        return executor.submit(() -> checkpoints.get(id));
    }

    public void updateTrainBlock(String traindId, int newBlock) {
        executor.submit(() -> trainBlockMap.put(traindId, newBlock));
    }

    public void getTrainblock(String traindId) {
        executor.submit(() -> trainBlockMap.get(traindId));
    }

    public Future<Boolean> isBlockOccupied(int blockId) {
        return executor.submit(() -> trainBlockMap.containsValue(blockId));
    }

    public Future<String> getLastTrainInBlock(int blockId) {
        return executor.submit(() -> trainBlockMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(blockId))
                .map(Map.Entry::getKey)
                .reduce((first, second) -> second)
                .orElse(null));
    }

    public Integer getTrainCount() {
        return trains.size();
    }

    public Integer getStationCount() {
        return stations.size();
    }

    public Integer getCheckpointCount() {
        return checkpoints.size();
    }
}
