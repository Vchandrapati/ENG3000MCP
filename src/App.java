import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class App {
    public static void main(String[] args) throws Exception {
        new Display();
    }

    private static class Display extends JFrame {

        public Display() {
            int w = 1200;
            int h = 800;

            int wMax = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
            int hMax = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    
            setPreferredSize(new Dimension(w, h));
            setLocation(wMax/2 - w/2, hMax/2 - h/2);
            setSize(w, h);

            Visualiser vis = new Visualiser(w, h);
            add(vis);

            Server server = new Server(new VisualiserServer(vis), w, h);

            setVisible(true);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            try {
                                server.stop();
                                Display.this.dispose(); 
                                System.out.println("Successfully closed program");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
    }
}
