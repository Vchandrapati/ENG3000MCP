import java.awt.Graphics;
import java.awt.Point;
import java.util.logging.Logger;

public class Train implements Paintable {
    private static final Logger logger = Logger.getLogger(Train.class.getName());
    int x;
    int y;
    int trainSize = 30;
    double speed;
    double distance;
    enum State {
        OFF, ON, ERROR
    }
    State trainState;

    public Train(double speed, double distance) {
        this.speed = speed;
        this.distance = distance;
        trainState = State.OFF;
    }

    public int getSize() {
        return trainSize;
    }

    public void setPos(Point p) {
        this.x = (int) p.getX();
        this.y = (int) p.getY();
    }

    public void updatePosition(double scalePerFrame, Track track) {
        double distancePerFrame = this.speed * scalePerFrame;
        this.distance += distancePerFrame; // Update the distance traveled in meters
        Point newPos = track.findPos(this.distance); // Find the new position on the track
        setPos(newPos); // Update the train's position
    }

    @Override
    public void paint(Graphics g) {
        g.drawRect(x, y, trainSize, trainSize);
        g.drawString(String.valueOf(this.speed), x, y - 5);
    }
}
