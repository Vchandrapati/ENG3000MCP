import java.awt.Graphics;
import java.util.logging.Logger;
import java.awt.Point;

public class Station implements Paintable{
    private static final Logger logger = Logger.getLogger(Station.class.getName());
    int x = 0;
    int y = 0;
    int stationSizeH = 25;
    int stationSizeW = 50;
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
        g.drawRect(x, y, stationSizeW, stationSizeH);
        g.drawString(stationState.name(), x, y);
    }

    public void setPos(Point p) {
        this.x = (int) p.getX();
        this.y = (int) p.getY();
    }
}
