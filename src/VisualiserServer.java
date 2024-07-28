import java.util.List;
import java.util.logging.Logger;

public class VisualiserServer {
    private static final Logger logger = Logger.getLogger(VisualiserServer.class.getName());
    Visualiser vis;
    public VisualiserServer(Visualiser vis) {
        this.vis = vis;
    }

    public void updateTrains(List<Train> list) {
        //will be blocked if locked by thread, however only a 1/30s max lock
        vis.trains = list;
    }

    public void updateRing(Ring ring) {
        //will be blocked if locked by thread, however only a 1/30s max lock
        vis.ring = ring;
    }

    public void updateStations(List<Station> list) {
        //will be blocked if locked by thread, however only a 1/30s max lock
        vis.stations = list;
    }

    public void stop() {
        vis.stop();
    }
}
