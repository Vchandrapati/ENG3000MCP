import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Track implements Paintable {
    private List<TrackSegment> segments;
    private List<Point> points;
    Point startPoint;

    double totalLength = 0;

    public Track(Point startPoint) {
        this.startPoint = startPoint;
        segments = new ArrayList<>();
        points = new ArrayList<>();
        generateSegments();
        generatePoints();
    }

    private void generateSegments() {
        // Add the first straight segment
        CurveSegment tmpC = new CurveSegment(startPoint, 0.20, 90, 180);
        segments.add(tmpC);
        StraightSegment tmpS = new StraightSegment(tmpC.getEnd().x, tmpC.getEnd().y, 0.50, 0);
        segments.add(tmpS);
        tmpC = new CurveSegment(tmpS.getEnd(), 0.20, -90, 180);
        segments.add(tmpC);
        tmpS = new StraightSegment(tmpC.getEnd().x, tmpC.getEnd().y, -0.50, 0);
        segments.add(tmpS);
    }

    public void generatePoints() {
        points.clear();
        totalLength = 0;
        for (TrackSegment segment : segments) {
            totalLength += segment.getLength();
            points.addAll(segment.generatePoints());
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;  // Cast Graphics to Graphics2D
        g2d.setColor(Color.BLACK);  // Set the color for the lines

        // Set the stroke to a thicker line
        float thickness = 3.0f;  // Adjust the thickness as needed
        g2d.setStroke(new BasicStroke(thickness));

        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }


    public Point findPos(double distance) {
        double accumulatedDistance = 0.0;
        for (TrackSegment segment : segments) {
            double segmentLength = segment.getLength();
            if (accumulatedDistance + segmentLength >= distance) {
                return segment.findPos(distance - accumulatedDistance);
            }
            accumulatedDistance += segmentLength;
        }
        // If distance is out of range, return last point
        return points.get(points.size() - 1);
    }


    public double getTangentAngle(double distance) {
        double accumulatedDistance = 0.0;
        for (TrackSegment segment : segments) {
            double segmentLength = segment.getLength();
            if (accumulatedDistance + segmentLength >= distance) {
                return segment.getTangentAngle(distance - accumulatedDistance);
            }
            accumulatedDistance += segmentLength;
        }
        return 0.0; // Default angle if distance is out of range
    }
}
