import java.util.ArrayList;
import java.util.List;

public class Database {
    //can be an actual database idk, this is placeholder
    private Ring ring;
    private List<Train> trains;
    private List<Station> stations;

    public Database(Ring ring) {
        this.ring = ring;
        trains = new ArrayList<>();
        stations = new ArrayList<>();
    }

    public void addTrain(Train t) {
        trains.add(t);
    }

    public void addStation() {
        
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

    public void updateTrain(Train t) {

    }
    
}
