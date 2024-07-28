import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JPanel;

public class Visualiser extends JPanel {
    private static final Logger logger = Logger.getLogger(Visualiser.class.getName());
    public static final double SCALE = 2; // 1 meter = 0.5 pixels
    private static final double FRAME_RATE = 100.0; // 30 frames per second
    private static final double TIME_INTERVAL = 1000.0 / FRAME_RATE; // Time interval per frame in milliseconds
    volatile boolean running = true;
    Thread runThread;
    int w;
    int h;
    Track track;
    List<Train> trains;
    List<Station> stations;

    List<Checkpoint> checkpoints;

    public Visualiser(int w, int h) {
        this.w = w;
        this.h = h;

        track = new Track();
        trains = new ArrayList<>();
        stations = new ArrayList<>();
        checkpoints = new ArrayList<>();

        runThread = new Thread(this::run);
        runThread.start();
    }

    public void stop() {
        try {
            running = false;
            runThread.join();
            System.out.println("Successfully stopped visualiser");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        long time = System.currentTimeMillis();
        long timeInterval = (long) TIME_INTERVAL;
        while (running) {
            if (System.currentTimeMillis() - time >= timeInterval) {
                time = System.currentTimeMillis();
                repaint();
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.black);

        track.paint(g);

        synchronized (trains) {
            for (Train train : trains) {
                train.updatePosition(SCALE / FRAME_RATE);
                train.setPos(track.findPos(train.distance));
                train.paint(g);
            }
        }

        synchronized (stations) {
            for (Station station : stations) {
                Point pos = track.findPos(station.distance);
                station.setPos(pos);
                station.paint(g);
            }
        }

        synchronized (checkpoints) {
            for (Checkpoint checkpoint : checkpoints) {
                Point pos = track.findPos(checkpoint.distance);
                checkpoint.setPos(pos);
                checkpoint.paint(g);
            }
        }
    }

    public void addStation(Station station) {
        stations.add(station);
        // Add a checkpoint slightly before the station
        double checkpointDistance = station.distance - 30; // Adjust this offset as needed
        checkpoints.add(new Checkpoint(checkpointDistance));
    }
}
