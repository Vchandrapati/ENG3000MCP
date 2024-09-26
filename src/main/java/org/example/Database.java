package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    // All client objects
    private final ConcurrentHashMap<String, Client> clients;
    private final ConcurrentHashMap<String, Integer> bladeRunnerBlockMap;

    // Set of all BladeRunner client IDs
    private final HashSet<String> allBladeRunners;
    // Set of all unresponsive or "dead" clients
    private final HashSet<String> unresponsiveClients;

    private final AtomicInteger numberOfCheckpoints;
    private final AtomicInteger numberOfStations;

    private Database() {
        clients = new ConcurrentHashMap<>();
        bladeRunnerBlockMap = new ConcurrentHashMap<>();

        allBladeRunners = new HashSet<>();
        unresponsiveClients = new HashSet<>();

        numberOfCheckpoints = new AtomicInteger(0);
        numberOfStations = new AtomicInteger(0);
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

    // Add any client with this method
    public void addClient(String id, Client client) {
        // Will attempt to add a client
        // If absent it will happen, however, if it is present the previous client will
        // be handed over
        Client prevValue = clients.putIfAbsent(id, client);
        clients.putIfAbsent(id, client);

        // If there was a previous client log the error
        if (prevValue != null) {
            String message = "Attempted to add duplicate client with id: " + id;
            logger.log(Level.WARNING, message);
            throw new IllegalArgumentException(message);
        }

        // If there was no previous client then add the client to the correct lists
        if (id.startsWith("BR")) {
            allBladeRunners.add(id);
        }

        if (id.startsWith("CP")) {
            numberOfCheckpoints.getAndIncrement();
        }

        if (id.startsWith("ST")) {
            numberOfStations.getAndIncrement();
        }
    }

    // Get any client with this method
    public <T extends Client> T getClient(String id, Class<T> type) {
        Client c = clients.get(id);

        if (c == null) {
            return null;
        }

        if (type.isInstance(c)) {
            return type.cast(c);
        } else {
            logger.log(Level.SEVERE, "Client with ID: " + id + " is not of type: " + type.getName());
            return null;
        }
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

    public void updateBladeRunnerBlock(String bladeRunnerId, int newBlock) {
        bladeRunnerBlockMap.put(bladeRunnerId, newBlock);
    }

    public boolean isBlockOccupied(int blockId) {
        return bladeRunnerBlockMap.containsValue(blockId);
    }

    public String getLastBladeRunnerInBlock(int blockId) {
        return bladeRunnerBlockMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(blockId))
                .map(Map.Entry::getKey)
                .reduce((first, second) -> second)
                .orElse(null);
    }

    public List<BladeRunnerClient> getBladeRunnerClients() {
        List<BladeRunnerClient> bladeRunners = new ArrayList<>();
        for (String id : allBladeRunners) {
            if (clients.get(id) instanceof BladeRunnerClient bladeRunnerClient) {
                bladeRunners.add(bladeRunnerClient);
            } else {
                logger.log(Level.SEVERE, "A non BladeRunner in allBladeRunners set: {0}", id);
            }
        }

        return bladeRunners;
    }

    public List<Client> getClients() {
        return new ArrayList<>(clients.values());
    }

    public int getBladeRunnerCount() {
        return allBladeRunners.size();
    }

    public Integer getCheckpointCount() {
        return numberOfCheckpoints.get();
    }

    public Integer getStationCount() {
        return numberOfStations.get();
    }

    /**
     * Holder class for implementing the Singleton pattern.
     */
    private static class Holder {
        private static final Database INSTANCE = new Database();
    }
}