public class MessageHandler {
    public void handleMessage(Server.Client client, String message, Database db, VisualiserServer visServer) {
           String[] input = message.split(",");

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
                   handleStationMessage(client, input);
                   break;
           }
    }

    private void handleTrainInit(Server.Client client) {
        client.sendMessage("ACK trainInit");
        client.clientType = Server.Client.type.TRAIN;
    }

    private void handleStationInit(Server.Client client) {
        client.sendMessage("ACK trainInit");
        client.clientType = Server.Client.type.TRAIN;
    }

    private void handlePing(Server.Client client, String[] inputArr, Database db) {
        int id = client.id;
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

    private void handleStationMessage(Server.Client client, String[] inputArr) {
        //TODO
    }
}
