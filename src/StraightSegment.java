import java.awt.*;
import java.util.ArrayList;

public class StraightSegment extends TrackSegment {
    private final Point start;
    private final Point end;

    public StraightSegment(double startX, double startY, double lengthMeters, double angleDegrees) {
        double lengthPixels = lengthMeters * SCALE;
        double angleRadians = Math.toRadians(angleDegrees);
        this.start = new Point((int)startX, (int)startY);

        this.end = new Point(
                (int)(start.x + lengthPixels * Math.cos(angleRadians)),
                (int)(start.y + lengthPixels * Math.sin(angleRadians))
        );

        this.length = Math.abs(lengthMeters); // Ensure that distance is positive
    }

    public Point getEnd() {
        return end;
    }

    @Override
    public ArrayList<Point> generatePoints() {
        ArrayList<Point> points = new ArrayList<>();
        int numPoints = (int) (length * SCALE);
        for (int i = 0; i <= numPoints; i++) {
            double ratio = (double) i / numPoints;
            int x = (int) (start.x + ratio * (end.x - start.x));
            int y = (int) (start.y + ratio * (end.y - start.y));
            points.add(new Point(x, y));
        }
        return points;
    }

    @Override
    public Point findPos(double distance) {
        double ratio = (distance * SCALE)  / (length * SCALE);
        int x = (int) (start.x + ratio * (end.x - start.x));
        int y = (int) (start.y + ratio * (end.y - start.y));
        return new Point(x, y);
    }

    @Override
    public double getTangentAngle(double distance) {
        // Calculate the angle of the segment in radians
        return Math.atan2(end.y - start.y, end.x - start.x);
    }

}
