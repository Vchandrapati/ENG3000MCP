package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Visualiser extends JPanel implements Constants {
    private static final Logger logger = Logger.getLogger(Visualiser.class.getName());
    private static final double TIME_INTERVAL = SCALE / FRAME_RATE;
    volatile boolean running = true;
    Thread runThread;
    Track track;
    Map<Integer, Train> trains;
    List<Station> stations;
    List<Checkpoint> checkpoints;
    private  final ScheduledExecutorService repaintThread;

    public Visualiser(int width, int height, Track track) {
        trains = new HashMap<>();
        stations = new ArrayList<>();
        checkpoints = new ArrayList<>();
        this.track = track;

        setPreferredSize(new Dimension(width, height));
        repaintThread = Executors.newScheduledThreadPool(1);
        run();
    }

    public void stop() {
        try {
            running = false;
            repaintThread.shutdownNow();
            runThread.join();
            logger.info("Successfully stopped visualiser");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Issue stopping visualiser", e);
        }
    }

    public void run() {
        Runnable repaint = () -> {
            if(running) {
                updatePositions();
                repaint();
            }
        };

        repaintThread.scheduleAtFixedRate(repaint, 0, 1, TimeUnit.SECONDS);
    }

    public void updatePositions() {
        synchronized (trains) {
            trains.values().forEach(train -> train.updatePosition(1 / FRAME_RATE, track));
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Clear the screen
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3.0f));

        track.paint(g2d);

        synchronized (trains) {
            trains.values().forEach(train -> train.paint(g2d));
        }

        synchronized (stations) {
            for (Station station : stations) {
                Point pos = track.findPos(station.distance);
                station.setPos(pos);
                station.paint(g);
            }
        }

        synchronized (checkpoints) {
            checkpoints.forEach(c -> c.paint(g));
        }
    }

    public void updateTrains(Map<Integer, Train> newTrains) {
        synchronized (trains) {
            trains.clear();
            trains.putAll(newTrains);
        }
    }

    public void updateStations(List<Station> newStations) {
        synchronized (stations) {
            stations.clear();
            stations.addAll(newStations);
        }
    }

    public void updateCheckpoints(int numCheckpoints) {
        checkpoints.clear();
        track.calculateCheckpoints(numCheckpoints);
        track.getCheckpoints().forEach(pos -> checkpoints.add(new Checkpoint(pos)));
    }
}
