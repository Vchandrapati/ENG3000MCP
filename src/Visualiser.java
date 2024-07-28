import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JPanel;

public class Visualiser extends JPanel {
    private static final Logger logger = Logger.getLogger(Visualiser.class.getName());
    volatile boolean running = true;
    Thread runThread;
    int w;
    int h;
    Ring ring;
    List<Train> trains;
    List<Station> stations;

    public Visualiser(int w, int h) {
        this.w = w;
        this.h = h;

        ring = null;
        trains = new ArrayList<>();
        stations = new ArrayList<>();

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
        long timeInterval = 1000 / 30; // 30 fps
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

        if(ring == null) return;
        ring.setScreenSize(width, height);
        ring.paint(g);

        synchronized (trains) {
            for (Train train : trains) {
                train.angle += train.speed;
                train.setPos(ring.findPos(train.angle, train.trainSize));
                train.paint(g);
            }
        }

        synchronized (stations) {
            for (Station station : stations) {
                station.paint(g);
            }
        }
    }
}
