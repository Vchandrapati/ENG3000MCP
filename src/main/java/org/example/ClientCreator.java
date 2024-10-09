package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientCreator {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final HashMap<String, Integer> locations = new HashMap<>();

    public static ClientCreator getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final ClientCreator INSTANCE = new ClientCreator();
    }

    // I dont think anything needs to change here except location
    public void handleInitialise(ReceiveMessage receiveMessage, InetAddress address, int port) {
        try {
            Client<?, ?> client = null;
            switch (receiveMessage.clientType) {
                case "CCP":
                    client = new BladeRunnerClient(address, port, receiveMessage.clientID,
                            receiveMessage.sequenceNumber);
                    break;
                case "CPC": {
                    Integer zone = locations.get(address.toString() + port);
                    client = new CheckpointClient(address, port, receiveMessage.clientID,
                            receiveMessage.sequenceNumber, zone);
                    break;
                }
                case "STC": {
                    Integer zone = locations.get(address.toString() + port);
                    client = new StationClient(address, port, receiveMessage.clientID,
                            receiveMessage.sequenceNumber, zone);
                    break;
                }
                default:
                    logger.log(Level.WARNING, "Unknown client type: {0}",
                            receiveMessage.clientType);
                    break;
            }

            if (client != null) {
                client.registerClient();
                client.sendAcknowledgeMessage(MessageEnums.AKType.AKIN);
                logger.log(Level.INFO, "Initialised new client: {0}", receiveMessage.clientID);
                // if a client joins while not in waiting state, goes to emergency mode
                if (SystemStateManager.getInstance().getState() != SystemState.WAITING) {
                    SystemStateManager.getInstance().addUnresponsiveClient(receiveMessage.clientID,
                            ReasonEnum.INVALCONNECT);
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to handle message");
            logger.log(Level.SEVERE, "Exception: ", e);
        }
    }

    public void readFromFile(String fileLocation) {

        try {
            File file = new File(fileLocation);
            Scanner s = new Scanner(file);

            while (s.hasNextLine()) {
                String str = s.nextLine();
                String[] splitStr = str.split("_");

                locations.put(splitStr[0], Integer.parseInt(splitStr[1]));
                logger.log(Level.FINER, "Key: {0} Value: {1}",
                        new Object[] {splitStr[0], splitStr[1]});
            }

            s.close();
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "File: {0} not found", fileLocation);
            e.printStackTrace();
        }
    }
}
