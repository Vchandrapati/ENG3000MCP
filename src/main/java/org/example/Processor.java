package org.example;

import java.util.HashMap;

public class Processor {

    int trainCount = 5;
    int checkPointCount = 10;
    int sensorAmount = 10;
    int STOP = 0;
    Database db = Database.getInstance();
    MessageHandler m = new MessageHandler();
    HashMap<Integer, Integer> stations = new HashMap<>();
    HashMap<TrainClient, Integer> trainZones = new HashMap<>();

    // this needs to get updated every time vikil sends sensor trip


    public void sensorTripped(int sensorTripped){
        fillMap();
        fillStationLocations();
        handleTrainSpeed(sensorTripped);
    }

    public void fillMap() {
        for (int i = 0; i < trainCount; i++) {
            TrainClient t = db.getTrain("BR0" + i);
            trainZones.put(t, t.getZone());
        }
    }

    public void fillStationLocations() { // MASSIVE ASSUMPTION THAT STATIONS ARE EVERY SECOND SENSOR
        int count = 2;
        for (int i = 0; i < checkPointCount; i++) {
            stations.put(i, count);
            count += 2;
        }
    }

    public void handleTrainSpeed(int sensor) {
        for (int i = 0; i < trainCount; i++) {
            TrainClient t = db.getTrain("BR0" + i);
            if (t.getZone() == sensor) {
                // check if it is a sensor station, if it is, stop train+
                if (stations.containsValue(sensor)) {
                    t.sendExecuteMessage(STOP);
                }

                //check if there is a train already in this zone (THIS SHOULD NEVER BE THE CASE)
                trainZones.remove(t); // remove current train otherwise it will always be true
                if (trainZones.containsValue(sensor)) {
                    t.sendExecuteMessage(STOP);
                }
                trainZones.put(t, t.getZone()); // add train back


                // train is in zone in front of train, so stop current train
                if (trainZones.containsValue(t.getZone() + 1))
                    t.sendExecuteMessage(STOP);
            }
             // train continues at same speed
        }
    }

}
