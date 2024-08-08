package org.example;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton instance that maintains a lookup table for clients.
 * The table maps client connection details (IP and port) to corresponding component information.
 * Uses the {@link Pair} class to store and manage client connection details and component information.
 */
public class ClientTable implements Constants {
    // Pair of IP, Port and pair of IP and Port as well as Component ID all predefined
    private final Map<Pair<String, Integer>, String> lookupTable = new HashMap<>();
    private static ClientTable instance;

    private ClientTable() {
        initializeLookupTable();
    }

    // IP and Port ranges are predefined for every component
    private void initializeLookupTable() {
        // LEDs
        for (int i = 1; i <= 20; i++) {
            String ip = String.format(DOMAIN, 50 + i);
            int port = 5000 + i;
            String component = String.format("LED %d", i);
            lookupTable.put(new Pair<>(ip, port), component);
        }

        // Bladerunners
        for (int i = 1; i <= 32; i++) {
            String ip = String.format(DOMAIN, 100 + i);
            int port = 3000 + i;
            String component = String.format("BR %d", i);
            lookupTable.put(new Pair<>(ip, port), component);
        }

        // Stations
        for (int i = 1; i <= 8; i++) {
            String ip = String.format(DOMAIN, 200 + i);
            int port = 4000 + i;
            String component = String.format("ST %d", i);
            lookupTable.put(new Pair<>(ip, port), component);
        }
    }

    public static synchronized ClientTable getInstance() {
        if(instance == null)
            instance = new ClientTable();

        return instance;
    }

    public String getComponent(String ip, int port) {
        return lookupTable.get(new Pair<>(ip, port));
    }
}
