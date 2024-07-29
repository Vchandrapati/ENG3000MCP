import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Database {
    private static final Logger logger = Logger.getLogger(Database.class.getName());
    private Track track;
    private List<Train> trains;
    private List<Station> stations;

    public Database(Track track) {
        this.track = track;
        trains = new ArrayList<>();
        stations = new ArrayList<>();
    }

    public void addTrain(Train t) {
        t.setPos(track.findPos(t.distance));
        trains.add(t);
    }

    public void addStation(Station s) {
        s.setPos(track.findPos(s.distance));
        stations.add(s);
    }

    public Track getTrack() {
        return track;
    }

    public List<Train> getTrains() {
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
