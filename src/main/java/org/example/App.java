package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    public static final int W = 1500;
    public static final int H = 1000;
    public static void main(String[] args) {
        LoggerConfig.setupLogger();
        SwingUtilities.invokeLater(Display::new);
    }

    private static class Display extends JFrame {
        private static final Logger logger = Logger.getLogger(Display.class.getName());

        public Display() {
            setupWindow();
            Track track = new Track(W / 2, H / 2, 200, 80);
            Database db = Database.getInstance();

            Visualiser visualiser = new Visualiser(W, H, track);
            visualiser.updateCheckpoints(10);
            add(visualiser);
            Server server = new Server();

            Processor processor = Processor.getInstance(db, server);
            processor.start();
            setupWindowListener(server);

            db.addTrain(1, new Train(1, 10.0, 0.0));
            visualiser.updateTrains(db.getTrains());
        }

        public void setupWindow() {
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setPreferredSize(new Dimension(W, H));
            setSize(W, H);
            setVisible(true);
        }

        private void setupWindowListener(Server server) {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        server.stop();
                        Display.this.dispose();
                        logger.info("Successfully closed program");
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE,"Program failed to shutdown", ex);
                    }
                }
            });
        }
    }
}
