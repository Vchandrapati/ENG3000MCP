import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JPanel;

public class Visualiser extends JPanel implements Constants{
    private static final Logger logger = Logger.getLogger(Visualiser.class.getName());
    private static final double TIME_INTERVAL = SCALE / FRAME_RATE;
    volatile boolean running = true;
    Thread runThread;
    int w;
    int h;
    Track track;
    Map<Integer, Train> trains;
    List<Station> stations;

    List<Checkpoint> checkpoints;

    public Visualiser(int w, int h, Track track) {
        this.w = w;
        this.h = h;

        trains = new HashMap<>();
        stations = new ArrayList<>();
        checkpoints = new ArrayList<>();
        this.track = track;

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
                updatePositions();
                repaint();
            }
        }
    }

    public void updatePositions() {
        synchronized (trains) {
            trains.forEach((k, v) -> v.updatePosition(1 / FRAME_RATE, track));
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Clear the screen
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);

        float thickness = 3.0f;
        g2d.setStroke(new BasicStroke(thickness));

        track.paint(g2d);

        synchronized (trains) {
            trains.forEach((k, v) -> v.paint(g2d));
        }

        synchronized (stations) {
            for (Station station : stations) {
                Point pos = track.findPos(station.distance * SCALE);
                station.setPos(pos);
                station.paint(g);
            }
        }

        synchronized (checkpoints) {
            for (Checkpoint checkpoint : checkpoints) {
                Point pos = track.findPos(checkpoint.distance * SCALE);
                checkpoint.setPos(pos);
                checkpoint.paint(g);
            }
        }
    }

    public void addStation(Station station) {
        stations.add(station);
        // Add a checkpoint slightly before the station
        double checkpointDistance = station.distance; // Adjust this offset as needed
        checkpoints.add(new Checkpoint(checkpointDistance));
    }
}
