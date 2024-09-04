package org.example;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton instance that maintains a lookup table for clients.
 * The table maps client connection details (IP and port) to corresponding component information.
 */
public class ClientTable implements Constants {
    private final Map<String, String> lookupTable = new HashMap<>();
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
            String component = String.format("LED%d", i);
            lookupTable.put(String.format("%s %d",ip, port), component);
        }

        // Bladerunners
        for (int i = 1; i <= 32; i++) {
            String ip = String.format(DOMAIN, 100 + i);
            int port = 3000 + i;
            String component = String.format("BR%d", i);
            lookupTable.put(String.format("%s %d",ip, port), component);
        }

        lookupTable.put(String.format("127.0.0.1 %d", 2000), "BR69"); // Test Client
        lookupTable.put(String.format("127.0.0.1 %d", 3000), "BR79"); // Test Client

        createTestClients();

        // Stations
        for (int i = 1; i <= 8; i++) {
            String ip = String.format(DOMAIN, 200 + i);
            int port = 4000 + i;
            String component = String.format("ST%d", i);
            lookupTable.put(String.format("%s %d",ip, port), component);
        }
    }

    private void createTestClients() {
        for(int i = 1; i <= 5; i++) {
            //blade runner
            lookupTable.put(String.format("127.0.0.1 %d", 3000 + i), "br"+i); // Test Client
            //station
            lookupTable.put(String.format("127.0.0.1 %d", 4000 + i), "st"+i); // Test Client
        }
        //checkpoints
        for(int i = 1; i <= 10; i++) {
            lookupTable.put(String.format("127.0.0.1 %d", 5000 + i), "c"+i); // Test Client
        }
    }

    public static synchronized ClientTable getInstance() {
        if(instance == null)
            instance = new ClientTable();

        return instance;
    }

    public String getComponent(String ip, int port) {
        return lookupTable.get(String.format("%s %d",ip, port));
    }
}