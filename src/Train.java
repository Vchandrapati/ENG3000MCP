import java.awt.Graphics;
import java.awt.Point;
import java.util.logging.Logger;

public class Train implements Paintable {
    private static final Logger logger = Logger.getLogger(Train.class.getName());
    int x = 0;
    int y = 0;
    int trainSize = 30;
    double speed;
    double angle;
    enum State {
        OFF, ON, ERROR
    }
    State trainState;

    public Train(int x, int y, double speed, double angle) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.angle = angle;
        trainState = State.OFF;
    }

    public int getSize() {
        return trainSize;
    }

    public void setPos(Point p) {
        this.x = (int) p.getX();
        this.y = (int) p.getY();
    }

    public void updatePosition(double scalePerFrame) {
        // Update the angle based on speed (converted to pixels per frame)
        this.angle += this.speed * scalePerFrame;
    }

    @Override
    public void paint(Graphics g) {
        g.drawRect(x, y, trainSize, trainSize);
    }
}
