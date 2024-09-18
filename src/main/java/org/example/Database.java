package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.InetAddress;

public class Database {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final ConcurrentHashMap<String, Client> clients;
    private final ConcurrentHashMap<String, Integer> trainBlockMap;

    // Hashmap of key: IP + Port value: client ID
    private final ConcurrentHashMap<String, String> clientKeys;

    // Set of all train client IDs
    private final HashSet<String> allTrains;
    // Set of all unresponsive or "dead" clients
    private final HashSet<String> unresponsiveClients;
    // Set of all train clients waiting to reconnect
    private final HashSet<String> waitingToReconnectTrains;

    private Database() {
        clients = new ConcurrentHashMap<>();
        trainBlockMap = new ConcurrentHashMap<>();

        clientKeys = new ConcurrentHashMap<>();

        allTrains = new HashSet<>();
        unresponsiveClients = new HashSet<>();
        waitingToReconnectTrains = new HashSet<>();
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

    public Map<String, Integer> getTrainBlockMap() {
        return new HashMap<>(trainBlockMap);
    }

    public void clearUnresponsiveClients() {
        unresponsiveClients.clear();
    }

    // Add any client with this method
    public void addClient(String id, Client client, InetAddress clientAddress, String clientPort) {
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

        clientKeys.put(clientAddress + clientPort, id);
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

    public List<Client> getTrains() {
        List<Client> trains = new ArrayList<>();
        for (String id : allTrains) {
            trains.add(clients.get(id));
        }

        return trains;
    }

    public List<Client> getClients() {
        return new ArrayList<>(clients.values());
    }

    public int getTrainCount() {
        return allTrains.size();
    }
}
