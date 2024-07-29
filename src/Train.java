import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.logging.Logger;

public class Train implements Paintable, Constants {
    private static final Logger logger = Logger.getLogger(Train.class.getName());
    int x, y;
    int trainW = 50;
    int trainH = 30;
    double speed;
    double distance;
    double angle;
    enum State {
        OFF, ON, ERROR
    }
    State trainState;

    public Train(double speed, double distance) {
        this.speed = speed;
        this.distance = distance;
        trainState = State.OFF;
    }

    public void setPos(Point p) {
        this.x = (int) p.getX();
        this.y = (int) p.getY();
    }

    public void updatePosition(double scalePerFrame, Track track) {
        double distancePerFrame = this.speed * SCALE * scalePerFrame;
        this.distance += distancePerFrame; // Update the distance traveled in meters

        double totalTrackLength = track.totalLength;

        if(this.distance > totalTrackLength)
            this.distance -= totalTrackLength;

        Point newPos = track.findPos(this.distance); // Find the new position on the track
        setPos(newPos); // Update the train's position

        this.angle = track.getTangentAngle(this.distance);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform old = g2d.getTransform();

        g2d.translate(x, y);
        g2d.rotate(angle);

        // Draw the train centered at the origin
        g2d.drawRect(-trainW / 2, -trainH/ 2, trainW, trainH);

        if(angle < 4 && angle > 2) {
            g2d.rotate(Math.PI);
            g2d.drawString(String.valueOf(this.speed), -trainW / 2, trainH / 2 + 15);
        }
        else
            g2d.drawString(String.valueOf(this.speed), -trainW / 2, -trainH / 2 - 5);

        g2d.setTransform(old); // Restore the original transform
    }
}
