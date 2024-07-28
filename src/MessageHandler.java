import java.util.logging.Logger;

public class MessageHandler {
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
               case "PING":
                   handlePing(client, input, db);
                   break;
               case "train":
                   handleTrainMessage(client, input, db, visServer);
                   break;
               case "station":
                   handleStationMessage(client, input, db, visServer);
                   break;
               default:
                   error();
                   break;
           }
    }
    
    private void error() {
        //TODO
    }

    private void handleTrainInit(Server.Client client) {
        client.sendMessage("ACK trainInit");
        client.clientType = Server.Client.type.TRAIN;
    }

    private void handleStationInit(Server.Client client) {
        client.sendMessage("ACK stationInit");
        client.clientType = Server.Client.type.STATION;
    }

    private void handlePing(Server.Client client, String[] inputArr, Database db) {
        int id = client.id;
        System.out.println(id);
        Train t = db.getTrain(id);
        t.speed = Double.parseDouble(inputArr[1]);
        db.updateTrain(t);
        System.out.println(inputArr[1] + " Given speed of train : Train : " + client.id);
    }

    private void handleTrainMessage(Server.Client client, String[] inputArr, Database db, VisualiserServer visServer) {
        double angle = Double.parseDouble(inputArr[2]);
        double speed = Double.parseDouble(inputArr[3]);
        Train newTrain = new Train(0, 0, speed, angle);
        db.addTrain(newTrain);
        visServer.updateTrains(db.getTrains());
        client.sendMessage("Train confirmed!");
        System.out.println(inputArr[4]);
        client.lastMessage = "Train confirmed";
    }

    private void handleStationMessage(Server.Client client, String[] inputArr, Database db, VisualiserServer visServer) {
        double angle = Double.parseDouble(inputArr[1]);
        Station newStation = new Station(0, 0, angle);
        db.addStation(newStation);
        visServer.updateStations(db.getStations());
        client.sendMessage("Station confirmed!");
        System.out.println(inputArr[2]);
        client.lastMessage = "Station confirmed";
    }
}
