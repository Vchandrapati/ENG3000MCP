package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Database {
    private static final Logger logger = Logger.getLogger(Database.class.getName());
    private Track track;
    private Map<Integer, Train> trains;
    private List<Station> stations;
    private static Database instance;

    public Database(Track track) {
        this.track = track;
        trains = new HashMap<>();
        stations = new ArrayList<>();
    }

    public static synchronized Database getInstance(Track track) {
        if(instance == null)
            instance = new Database(track);

        return instance;
    }

    public void addTrain(int ID, Train t) {
        t.setPos(track.findPos(t.distance));
        trains.put(ID, t);
    }

    public void addStation(Station s) {
        s.setPos(track.findPos(s.distance));
        stations.add(s);
    }

    public Track getTrack() {
        return track;
    }

    public Map<Integer, Train> getTrains() {
        return trains;
    }

    public Train getTrain(int id) {
        return trains.get(id);
    }

    public List<Station> getStations() {
        return stations;
    }

    public void updateTrain(Train t) {
        //TODO
    }
    
}
