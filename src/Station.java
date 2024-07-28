import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.logging.Logger;

public class Station implements Paintable{
    private static final Logger logger = Logger.getLogger(Station.class.getName());
    int x = 0;
    int y = 0;
    int stationSizeH = 50;
    int stationSizeW = 25;
    double angle;
    enum State {
        OPENDOOR, CLOSEDOOR, OFF
    }
    State stationState = State.OFF;

    public Station(int x, int y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        stationState = State.OFF;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform old = g2d.getTransform();
        int offset = stationSizeW / 2;

        // Position station properly on the track
        g2d.translate(x, y);
        g2d.rotate(angle, stationSizeW / 2.0 - offset - 10, stationSizeH / 2.0);

        // Draw the station
        g2d.drawRect(0, 0, stationSizeW, stationSizeH);
        g2d.drawString(stationState.name(), 0, stationSizeW + 15);

        // Reset the graphics context to its original state
        g2d.setTransform(old);
    }

    public void setPos(Point p) {
        this.x = (int) p.getX();
        this.y = (int) p.getY();
    }
}
