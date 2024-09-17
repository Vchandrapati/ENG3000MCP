package org.example;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton instance that maintains a lookup table for clients.
 * The table maps client connection details (IP and port) to corresponding component information.
 */
public class ClientTable implements Constants {
    private final Map<String, String> lookupTable = new HashMap<>();

    private ClientTable() {
        createLEDS();
        createBladerunners();
        createStations();
    }

    /**
     * Holder class for implementing the Singleton pattern.
     */
    private static class Holder {
        private static final ClientTable INSTANCE = new ClientTable();
    }

    /**
     * Returns the singleton instance of the Server.
     *
     * @return the singleton Server instance
     */
    public static ClientTable getInstance() {
        return Holder.INSTANCE;
    }

    private void createLEDS() {
        // LEDs
        // for (int i = 1; i <= 20; i++) {
        //     String ip = String.format(DOMAIN, 50 + i);
        //     int port = 5000 + i;
        //     String component = String.format("LED%d", i);
        //     lookupTable.put(String.format("%s %d",ip, port), component);
        // }
    }

    private void createBladerunners() {
        for (int i = 1; i <= 20; i++) {
            String ip = String.format(DOMAIN, 50 + i);
            int port = 5000 + i;
            String component = String.format("BR%d", i);
            lookupTable.put(String.format("%s %d",ip, port), component);
        }
    }

    private void createStations() {
        for (int i = 1; i <= 8; i++) {
            String ip = String.format(DOMAIN, 200 + i);
            int port = 4000 + i;
            String component = String.format("ST%d", i);
            lookupTable.put(String.format("%s %d", ip, port), component);
        }
    }

    public String getComponent(String ip, int port) {
        return lookupTable.get(String.format("%s %d",ip, port));
    }
}