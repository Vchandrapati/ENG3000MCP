package org.example.messages;

import org.example.*;
import org.example.client.*;
import org.example.state.SystemState;
import org.example.state.SystemStateManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientFactory {
    private Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private HashMap<String, Integer> locations = new HashMap<>();
    private Database db = Database.getInstance();

    private static class Holder {
        private static final ClientFactory INSTANCE = new ClientFactory();
    }

    public static ClientFactory getInstance() {
        return Holder.INSTANCE;
    }

    public void handleInitialise(ReceiveMessage receiveMessage, InetAddress address, int port) {
        try {
            MessageGenerator messageGenerator = new MessageGenerator();
            MessageSender messageSender =
                    new MessageSender(Server.getInstance(), address, port, receiveMessage.clientID);
            AbstractClient<?, ?> client = null;
            switch (receiveMessage.clientType) {
                case "CCP":
                    client = new BladeRunnerClient(receiveMessage.clientID, messageGenerator,
                            messageSender, receiveMessage.sequenceNumber);
                    break;
                case "CPC": {
                    Integer zone = locations.get(address.toString() + port);
                    System.out.println(address.toString() + " Look");
                    client = new CheckpointClient(receiveMessage.clientID, messageGenerator,
                            messageSender, zone, receiveMessage.sequenceNumber);
                    break;
                }
                case "STC": {
                    Integer zone = locations.get(address.toString() + port);
                    client = new StationClient(receiveMessage.clientID, messageGenerator,
                            messageSender, zone, receiveMessage.sequenceNumber);
                    break;
                }
                default:
                    logger.log(Level.WARNING, "Unknown client type: {0}",
                            receiveMessage.clientType);
                    break;
            }

            if (client != null) {
                db.addClient(receiveMessage.clientID, client);
                client.sendAcknowledgeMessage(MessageEnums.AKType.AKIN);
                logger.log(Level.INFO, "Initialised new client: {0}", receiveMessage.clientID);
                // if a client joins while not in waiting state, goes to emergency mode
                SystemStateManager.getInstance().addUnresponsiveClient(receiveMessage.clientID,
                        ReasonEnum.INVALCONNECT);

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
                if (splitStr.length == 2) {
                    locations.put(splitStr[0], Integer.parseInt(splitStr[1]));
                    System.out.println(splitStr[0] + "there");
                }
            }

            s.close();
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "File: {0} not found", fileLocation);
            e.printStackTrace();
        }
    }
}
