package org.example;

import org.example.client.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("rawtypes")
public class Database {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // All client objects
    private final ConcurrentHashMap<String, AbstractClient> clients;
    private final ConcurrentHashMap<String, Integer> bladeRunnerBlockMap;

    // Set of all BladeRunner client IDs
    private final HashSet<String> allBladeRunners;

    // Set of all unresponsive or "dead" clients
    private final HashSet<String> unresponsiveClients;

    private final AtomicInteger numberOfCheckpoints;
    private final AtomicInteger numberOfStations;

    private final ConcurrentHashMap<Integer, String> isStationMap;

    public Database () {
        clients = new ConcurrentHashMap<>();
        bladeRunnerBlockMap = new ConcurrentHashMap<>();

        allBladeRunners = new HashSet<>();
        unresponsiveClients = new HashSet<>();

        numberOfCheckpoints = new AtomicInteger(0);
        numberOfStations = new AtomicInteger(0);

        isStationMap = new ConcurrentHashMap<>();
    }

    /**
     * Returns the singleton instance of the Server.
     *
     * @return the singleton Server instance
     */
    public static Database getInstance () {
        return Holder.INSTANCE;
    }

    public Optional<StationClient> getStationIfExist (int zone) {
        String sc = isStationMap.get(zone);

        if (sc == null) {
            return Optional.empty();
        }

        return Optional.of((StationClient) clients.get(sc));
    }

    private void addStationsToMap (int zone, String id) {
        isStationMap.put(zone, id);
    }

    // Add any client with this method
    public void addClient (String id, AbstractClient client) {
        // Will attempt to add a client
        // If absent it will happen, however, if it is present the previous client will
        // be handed over
        AbstractClient prevValue = clients.putIfAbsent(id, client);
        clients.putIfAbsent(id, client);
        logger.log(Level.INFO, "Added {0} to database", client.getId());

        // If there was a previous client log the error
        if (prevValue != null) {
            String message = "Attempted to add duplicate client with id: " + id;
            logger.log(Level.WARNING, message);
            throw new IllegalArgumentException(message);
        }

        // If there was no previous client then add the client to the correct lists
        if (client instanceof BladeRunnerClient) {
            allBladeRunners.add(id);
        }

        if (client instanceof CheckpointClient) {
            numberOfCheckpoints.getAndIncrement();
        }

        if (client instanceof StationClient sc) {
            numberOfStations.getAndIncrement();
            addStationsToMap(sc.getLocation(), id);
        }
    }

    // Get any client with this method
    public <T extends AbstractClient> Optional<T> getClient (String id, Class<T> type) {
        AbstractClient c = clients.get(id);

        if (c == null)
            return Optional.empty();


        if (type.isInstance(c)) {
            return Optional.of(type.cast(c));
        } else {
            logger.log(Level.WARNING, "Client with ID: {0} is not of type: {1}", new Object[] {id, type.getName()});
        }

        return Optional.empty();
    }

    public Set<String> getAllUnresponsiveClientIDs () {
        return unresponsiveClients;
    }

    public boolean addUnresponsiveClient (String id, ReasonEnum newReason) {
        Optional<AbstractClient> cOptional = getClient(id, AbstractClient.class);
        AbstractClient c;

        if (cOptional.isPresent()) {
            c = cOptional.get();
        } else {
            logger.log(Level.WARNING, "Attempted to get non-existent client {0}", id);
            return false;
        }

        c.addReason(newReason);
        unresponsiveClients.add(id);
        return true;
    }

    public void fullPurge (String id) {
        allBladeRunners.remove(id);
        unresponsiveClients.remove(id);
        clients.remove(id);
    }

    public void ultraPurge () {
        clients.clear();
        bladeRunnerBlockMap.clear();

        allBladeRunners.clear();
        unresponsiveClients.clear();

        numberOfCheckpoints.set(0);
        numberOfStations.set(0);

        isStationMap.clear();
    }

    public void removeReason (String id, ReasonEnum reason) {
        if (!isClientUnresponsive(id)) {
            logger.log(Level.WARNING, "{0} is not an unresponsive client", id);
            return;
        }

        Optional<AbstractClient> cOptional = getClient(id, AbstractClient.class);
        AbstractClient c;

        if (cOptional.isPresent()) {
            c = cOptional.get();
        } else {
            logger.log(Level.WARNING, "Attempted to get non-existent client {0}", id);
            return;
        }

        c.removeReason(reason);

        if (c.isReasonsEmpty()) {
            unresponsiveClients.remove(id);
        }
    }

    public Set getClientReasons (String id) {
        if (!isClientUnresponsive(id)) {
            logger.log(Level.WARNING, "{0} is not an unresponsive client", id);
            return new HashSet<>();
        }

        Optional<AbstractClient> cOptional = getClient(id, AbstractClient.class);
        AbstractClient c;

        if (cOptional.isPresent()) {
            c = cOptional.get();
            return c.getUnresponsiveReasons();
        } else {
            logger.log(Level.WARNING, "Attempted to get non-existent client {0}t", id);
        }

        return new HashSet<>();
    }

    public boolean isClientUnresponsive (String id) {
        return unresponsiveClients.contains(id);
    }

    public boolean isUnresponsiveEmpty () {
        return unresponsiveClients.isEmpty();
    }

    public void updateBladeRunnerBlock (String bladeRunnerId, int newBlock) {
        bladeRunnerBlockMap.put(bladeRunnerId, newBlock);
    }

    public boolean isBlockOccupied (int blockId) {
        return bladeRunnerBlockMap.containsValue(blockId);
    }

    public String getLastBladeRunnerInBlock (int blockId) {
        return bladeRunnerBlockMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(blockId)).map(Map.Entry::getKey)
                .reduce((first, second) -> second).orElse(null);
    }

    public List<BladeRunnerClient> getBladeRunnerClients () {
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

    public List<AbstractClient> getClients () {
        return new ArrayList<>(clients.values());
    }

    public int getBladeRunnerCount () {
        return allBladeRunners.size();
    }

    public int getCheckpointCount () {
        return numberOfCheckpoints.get();
    }

    public int getStationCount () {
        return numberOfStations.get();
    }

    public int getBlockCount () {
        return getCheckpointCount() + getStationCount();
    }

    /**
     * Holder class for implementing the Singleton pattern.
     */
    private static class Holder {
        private static final Database INSTANCE = new Database();
    }
}
