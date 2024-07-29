import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    public static final int W = 1200;
    public static final int H = 800;
    public static void main(String[] args) {
        LoggerConfig.setupLogger();
        SwingUtilities.invokeLater(Display::new);
    }

    private static class Display extends JFrame {
        private static final Logger logger = Logger.getLogger(Display.class.getName());

        public Display() {
            setupWindow();
            Visualiser vis = setupVisualizer();
            Track track = new Track(new Point(300, 500));
            Database db = new Database(track);
            Server server = new Server(new VisualiserServer(vis), db, track);
            setupWindowListener(server);
        }

        public void setupWindow() {
            int wMax = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
            int hMax = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setPreferredSize(new Dimension(W, H));
            setLocation(wMax / 2, hMax / 2 - H / 2);
            setSize(W, H);
            setVisible(true);
        }

        private Visualiser setupVisualizer() {
            Visualiser vis = new Visualiser(W, H);
            add(vis);

            Station station1 = new Station(50);
            Station station2 = new Station(200);
            vis.addStation(station1);
            vis.addStation(station2);

            return vis;
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
