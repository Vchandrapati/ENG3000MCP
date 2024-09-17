package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Database {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final ConcurrentHashMap<String, TrainClient> trains;
    private final ConcurrentHashMap<String, StationClient> stations;
    private final ConcurrentHashMap<String, CheckpointClient> checkpoints;
    private final ConcurrentHashMap<String, Integer> trainBlockMap;
    private final Set<String> unresponsiveClients;

    private Database() {
        trains = new ConcurrentHashMap<>();
        stations = new ConcurrentHashMap<>();
        checkpoints = new ConcurrentHashMap<>();
        trainBlockMap = new ConcurrentHashMap<>();
        unresponsiveClients = ConcurrentHashMap.newKeySet();
    }

    /**
     * Holder class for implementing the Singleton pattern.
     */
    private static class Holder {
        private static final Database INSTANCE = new Database();
    }

    /**
     * Returns the singleton instance of the Server.
     *
     * @return the singleton Server instance
     */
    public static Database getInstance() {
        return Holder.INSTANCE;
    }

    public void addUnresponsiveClient(String id) {
        unresponsiveClients.add(id);
    }

    public List<TrainClient> getUnresponsiveClients() {
        List<TrainClient> trainClients = new ArrayList<>();
        for (String id : unresponsiveClients) {
            TrainClient client = trains.get(id);
            if (client != null) {
                trainClients.add(client);
            }
        }

        return trainClients;
    }

    public Map<String, Integer> getTrainBlockMap() {
        return new HashMap<>(trainBlockMap);
    }
    public void clearUnresponsiveClients() {
        unresponsiveClients.clear();
    }

    public void addTrain(String id, TrainClient tr) {
        TrainClient prevValue = trains.putIfAbsent(id, tr);
        if (prevValue != null) {
            String message = "Attempted to add duplicate train with id: " + id;
            logger.warning(message);
            throw new IllegalArgumentException(message);
        }
    }

    public void addStation(String id, StationClient st) {
        StationClient prevValue = stations.putIfAbsent(id, st);
        if (prevValue != null) {
            String message = "Attempted to add duplicate station with id: " + id;
            logger.warning(message);
            throw new IllegalArgumentException(message);
        }
    }

    public void addCheckpoint(String id, CheckpointClient ch) {
        CheckpointClient prevValue = checkpoints.putIfAbsent(id, ch);
        if (prevValue != null) {
            String message = "Attempted to add duplicate checkpoint with id: " + id;
            logger.warning(message);
            throw new IllegalArgumentException(message);
        }
    }

    public TrainClient getTrain(String id) {
        return trains.get(id);
    }

    public StationClient getStation(String id) {
        return stations.get(id);
    }

    public CheckpointClient getCheckpoint(String id) {
        return checkpoints.get(id);
    }

    public void updateTrainBlock(String trainId, int newBlock) {
        trainBlockMap.put(trainId, newBlock);
    }

    public boolean isBlockOccupied(int blockId) {
        return trainBlockMap.containsValue(blockId);
    }

    public String getLastTrainInBlock(int blockId) {
        return trainBlockMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(blockId))
                .map(Map.Entry::getKey)
                .reduce((first, second) -> second)
                .orElse(null);
    }

    public List<TrainClient> getTrains() {
        return new ArrayList<>(trains.values());
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
