package org.example;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class Database {
    private static final Logger logger = Logger.getLogger(Database.class.getName());
    private static volatile Database db;
    private final ConcurrentHashMap<String, TrainClient> trains;
    private final ConcurrentHashMap<String, StationClient> stations;
    private final ConcurrentHashMap<String, CheckpointClient> checkpoints;
    private final ConcurrentHashMap<String, Integer> trainBlockMap;
    //list of unresponsive trains, because all unrespovive client reconnection is handled by server, only need trains for restartup
    private final List<String> unresponsiveClients; 
    private final ExecutorService executor;

    private int maxTrainAmount = 5;
    private int maxStationAmount = 10;
    private int maxCheckpointAmount = 10;

    private Database() {
        trains = new ConcurrentHashMap<>();
        stations = new ConcurrentHashMap<>();
        checkpoints = new ConcurrentHashMap<>();
        trainBlockMap = new ConcurrentHashMap<>();
        unresponsiveClients = new CopyOnWriteArrayList<>();

        executor = Executors.newCachedThreadPool();
    }

    public static synchronized Database getInstance() {
        if (db == null)
            db = new Database();

        return db;
    }

    //shutdowns the database thread
    public void shutdown() {
        executor.shutdown();
    }

    public void addUnresponsiveClient(String id) {
        executor.submit(() -> {
            unresponsiveClients.add(id);
        });
    }

    //gets all unresponsive train clients, resets it after being grabbed
    public Future<List<TrainClient>> getUnresposiveClient() {
        return executor.submit(() -> {
            List<TrainClient> trainClients = new CopyOnWriteArrayList<>();
            for (String string : unresponsiveClients) {
                trainClients.add(this.getTrain(string).get());
            }   
            this.unresponsiveClients.clear();
            return trainClients;
        });
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

    //gets the list of connected trains
    public Future<List<TrainClient>> getTrains() {
        return executor.submit(() -> {
            List<TrainClient> list = new ArrayList<>();
            trains.forEach((K, V) -> list.add(V));
            return list;
        });
    }

    //returns a tripped checkpoint
    public Future<CheckpointClient> getLastTrip() {
        return executor.submit(() -> {
            CheckpointClient[] trippedCheckpoint = new CheckpointClient[1];
            checkpoints.forEach((K,V) -> {
                if(V.isTripped()) trippedCheckpoint[0] = V;
            });
            return trippedCheckpoint[0];
        });
    }


    //Gets for current client size
    public Integer getTrainCount() {
        return trains.size();
    }

    public Integer getStationCount() {
        return stations.size();
    }

    public Integer getCheckpointCount() {
        return checkpoints.size();
    }


    //Sets and Gets for max BR, ST and CH amounts

    public void setMaxBR(Integer amount) {
        this.maxTrainAmount = amount;
    }

    public void setMaxST(Integer amount) {
        this.maxStationAmount = amount;
    }

    public void setMaxCH(Integer amount) {
        this.maxCheckpointAmount = amount;
    }

    public int getMaxBR() {
        return this.maxTrainAmount;
    }

    public int getMaxST() {
        return this.maxStationAmount;
    }

    public int getMaxCH() {
        return this.maxCheckpointAmount;
    }

    //triggers restartup
    public void TESTING(String name, TrainClient tr) {
        addTrain(name, tr);
        addUnresponsiveClient(name);
        SystemStateManager.getInstance().setState(SystemState.RESTARTUP);
    }
}
