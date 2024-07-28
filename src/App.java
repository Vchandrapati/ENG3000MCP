import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    public static void main(String[] args) {
        LoggerConfig.setupLogger();
        new Display();
    }

    private static class Display extends JFrame {
        private static final Logger logger = Logger.getLogger(Display.class.getName());

        public Display() {
            int w = 1200;
            int h = 800;

            int wMax = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
            int hMax = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setPreferredSize(new Dimension(w, h));
            setLocation(wMax / 2 - w / 2, hMax / 2 - h / 2);
            setSize(w, h);

            Visualiser vis = new Visualiser(w, h);
            add(vis);

            // Example of adding stations
            Station station1 = new Station(50);
            Station station2 = new Station(200);

            vis.addStation(station1);
            vis.addStation(station2);

            Server server = new Server(new VisualiserServer(vis), w, h);

            setVisible(true);

            SwingUtilities.invokeLater(() -> addWindowListener(new WindowAdapter() {
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
            }));
        }
    }
}
