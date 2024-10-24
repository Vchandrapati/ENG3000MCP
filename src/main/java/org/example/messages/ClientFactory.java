package org.example.messages;

import org.example.Database;
import org.example.client.*;
import org.example.events.ClientErrorEvent;
import org.example.events.ClientIntialiseEvent;
import org.example.events.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientFactory {
    private final EventBus eventBus;
    private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final HashMap<String, Integer> locations = new HashMap<>();
    private final Database db = Database.getInstance();

    public ClientFactory (EventBus eventBus) {
        this.eventBus = eventBus;

        eventBus.subscribe(ClientIntialiseEvent.class, this::handleInitialise);
    }

    public void handleInitialise (ClientIntialiseEvent event) {
        InetAddress address = event.address();
        int port = event.port();
        ReceiveMessage receiveMessage = event.receiveMessage();

        try {
            MessageGenerator messageGenerator = new MessageGenerator();
            MessageSender messageSender =
                    new MessageSender(address, port, receiveMessage.clientID, eventBus);
            AbstractClient<?, ?> client = null;
            switch (receiveMessage.clientType) {
                case "CCP":
                    client = new BladeRunnerClient(receiveMessage.clientID, messageGenerator,
                            messageSender, receiveMessage.sequenceNumber);
                    break;
                case "CPC": {
                    Integer zone = locations.get(address.toString() + port);
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
                eventBus.publish(
                        new ClientErrorEvent(receiveMessage.clientID, ReasonEnum.INVALCONNECT));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to handle message: {0}", e.getMessage());
        }
    }

    public void readFromFile (String fileLocation) {
        try {
            File file = new File(fileLocation);
            Scanner s = new Scanner(file);

            while (s.hasNextLine()) {
                String str = s.nextLine();
                String[] splitStr = str.split("_");
                if (splitStr.length == 2)
                    locations.put(splitStr[0], Integer.parseInt(splitStr[1]));
            }

            s.close();
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "File: {0} not found", fileLocation);
        }
    }
}
