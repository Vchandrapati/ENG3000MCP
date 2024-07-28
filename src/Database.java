import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Database {
    private static final Logger logger = Logger.getLogger(Database.class.getName());
    private Ring ring;
    private List<Train> trains;
    private List<Station> stations;

    public Database(Ring ring) {
        this.ring = ring;
        trains = new ArrayList<>();
        stations = new ArrayList<>();
    }

    public void addTrain(Train t) {
        t.setPos(ring.findPos(t.angle, t.trainSize, t.trainSize));
        trains.add(t);
    }

    public void addStation(Station s) {
        s.setPos(ring.findPos(s.angle, s.stationSizeW, s.stationSizeH));
        stations.add(s);
    }

    public Ring getRing() {
        return ring;
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
