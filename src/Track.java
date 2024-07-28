import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Track implements Paintable{
    private List<TrackSegment> segments;
    private List<Point> points;

    public Track() {
        segments = new ArrayList<>();
        points = new ArrayList<>();
        generateSegments();
        generatePoints();
    }

    private void generateSegments() {
        // Add the first straight segment
        StraightSegment tmpS = new StraightSegment(50, 250, 0.2, 0);
        segments.add(tmpS);
        CurveSegment tmpC = new CurveSegment(tmpS.getEnd(), 0.08, 180, 90);
        segments.add(tmpC);
        segments.add(new StraightSegment(tmpC.getEnd().x, tmpC.getEnd().y, 0.2, 0));
    }

    public void generatePoints() {
        points.clear();
        for(TrackSegment segment : segments) {
            points.addAll(segment.generatePoints());
        }
    }

    @Override
    public void paint(Graphics g) {
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
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
}
