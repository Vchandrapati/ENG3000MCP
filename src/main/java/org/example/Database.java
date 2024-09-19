package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final ConcurrentHashMap<String, Client> clients;
    private final ConcurrentHashMap<String, Integer> trainBlockMap;

    // Set of all train client IDs
    private final HashSet<String> allTrains;
    // Set of all unresponsive or "dead" clients
    private final HashSet<String> unresponsiveClients;
    // Set of all train clients waiting to reconnect
    private final HashSet<String> waitingToReconnectTrains;
    private final AtomicInteger numberOfCheckpoints;
    private final AtomicInteger numberOfStations;


    private Database() {
        clients = new ConcurrentHashMap<>();
        trainBlockMap = new ConcurrentHashMap<>();

        allTrains = new HashSet<>();
        unresponsiveClients = new HashSet<>();
        waitingToReconnectTrains = new HashSet<>();

        numberOfCheckpoints = new AtomicInteger(0);
        numberOfStations = new AtomicInteger(0);
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

    // Method to return all clients which are unresponsive or "dead"
    public List<Client> getUnresponsiveClients() {
        List<Client> deadClients = new ArrayList<>();
        for (String id : unresponsiveClients) {
            Client client = clients.get(id);
            if (client != null) {
                deadClients.add(client);
            }
        }

        return deadClients;
    }

    public List<TrainClient> getTrainsWaitingToReconnect() {
        List<TrainClient> waitingClients = new ArrayList<>();
        for (String id : unresponsiveClients) {
            Client client = clients.get(id);

            if (client != null && clients.get(id) instanceof TrainClient) {
                waitingClients.add((TrainClient) client);
            } else {
                logger.log(Level.SEVERE, "A non train in waitingToReconnectTrains set: {0}", id);
            }
        }

        return waitingClients;
    }

    public Map<String, Integer> getTrainBlockMap() {
        return new HashMap<>(trainBlockMap);
    }

    public void clearUnresponsiveClients() {
        unresponsiveClients.clear();
    }

    // Add any client with this method
    public void addClient(String id, Client client) {
        // Will attempt to add a client
        // If absent it will happen, however, if it is present the previous client will
        // be handed over
        Client prevValue = clients.putIfAbsent(id, client);
        clients.putIfAbsent(id, client);

        // If there was a previous cient log the error
        if (prevValue != null) {
            String message = "Attempted to add duplicate client with id: " + id;
            logger.log(Level.WARNING, message);
            throw new IllegalArgumentException(message);
        }

        // If there was no previous client then add the client to the correct lists
        if (id.startsWith("BR")) {
            allTrains.add(id);
        }

        if (id.startsWith("CP")) {
            numberOfCheckpoints.getAndIncrement();
        }

        if (id.startsWith("ST")) {
            numberOfStations.getAndIncrement();
        }
    }

    // Get any client with this method
    public Client getClient(String id) {
        return clients.get(id);
    }

    public void addClientToUnresponsive(String id) {
        unresponsiveClients.add(id);
    }

    public void removeClientFromUnresponsive(String id) {
        unresponsiveClients.remove(id);
    }

    public boolean isClientUnresponsive(String id) {
        return unresponsiveClients.contains(id);
    }

    public boolean isUnresponsiveEmpty() {
        return unresponsiveClients.isEmpty();
    }

    public void addClientToReconnecting(String id) {
        if (!id.startsWith("BR")) {
            logger.log(Level.WARNING, "Attempted to add a non-train to waiting to reconnect: {0}", id);
        }
        waitingToReconnectTrains.add(id);
    }

    public void removeClientFromReconnecting(String id) {
        if (!id.startsWith("BR")) {
            logger.log(Level.WARNING, "Attempted to remove a non-train to waiting to reconnect: {0}", id);
        }
        waitingToReconnectTrains.remove(id);
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


    
    public List<TrainClient> getTrainClients() {
        List<TrainClient> trains = new ArrayList<>();
        for (String id : allTrains) {
            if (clients.get(id) instanceof TrainClient) {
                trains.add((TrainClient) clients.get(id));
            } else {
                logger.log(Level.SEVERE, "A non train in allTrains set: {0}", id);
            }
        }

        return trains;
    }

    public String getTrainStatus(String trainId) {
        Client client = getClient(trainId);
        if (client instanceof TrainClient) {
            TrainClient trainClient = (TrainClient) client;
            return trainClient.getStatus();
        }
        return "Unknown";
    }

    public List<Client> getClients() {
        return new ArrayList<>(clients.values());
    }

    public int getTrainCount() {
        return allTrains.size();
    }

    public Integer getCheckpointCount() {
        return numberOfCheckpoints.get();
    }

    public Integer getStationCount() {
        return numberOfStations.get();
    }
}