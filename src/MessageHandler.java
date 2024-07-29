import java.util.logging.Logger;

public class MessageHandler {
    int ID = 0;
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());
    public void handleMessage(Server.Client client, String message, Database db, VisualiserServer visServer) {
           String[] input = message.split(",");
           message = input[0];

           switch (message) {
               case "trainInit":
                   handleTrainInit(client);
                   break;
               case "stationInit":
                   handleStationInit(client);
                   break;
               case "train":
                   handleTrainMessage(client, input, db, visServer);
                   break;
               case "station":
                   handleStationMessage(client, input, db, visServer);
                   break;
               case "STATUS":
                   handleStatusMessage(client, input, db);
                   break;
               default:
                   defaultResponse(client);
                   break;
           }
    }

    private void handleStatusMessage(Server.Client client, String[] input, Database db) {
        int id = client.id;
        Train t = db.getTrain(id);
        t.speed = Double.parseDouble(input[1]);
        db.updateTrain(t);
        System.out.println(input[1] + " Given speed of train : Train : " + client.id);
    }

    private void defaultResponse(Server.Client client) {
        client.sendMessage("OK");
    }

    private void handleTrainInit(Server.Client client) {
        client.sendMessage("ACK trainInit");
        client.clientType = Server.Client.type.TRAIN;
    }

    private void handleStationInit(Server.Client client) {
        client.sendMessage("ACK stationInit");
        client.clientType = Server.Client.type.STATION;
    }

    private void handleTrainMessage(Server.Client client, String[] inputArr, Database db, VisualiserServer visServer) {
        double startDistance = Double.parseDouble(inputArr[1]);
        double speed = Double.parseDouble(inputArr[2]);
        Train newTrain = new Train(client.id, speed, startDistance);
        db.addTrain(client.id, newTrain);
        visServer.updateTrains(db.getTrains());
        client.sendMessage("ID " + client.id);
        client.lastMessage = "ID " + client.id;
    }

    private void handleStationMessage(Server.Client client, String[] inputArr, Database db, VisualiserServer visServer) {
        double angle = Double.parseDouble(inputArr[1]);
        Station newStation = new Station(angle);
        db.addStation(newStation);
        visServer.updateStations(db.getStations());
        client.sendMessage("Station confirmed!");
        client.lastMessage = "Station confirmed";
    }
}
