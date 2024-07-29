import java.awt.*;
import java.util.ArrayList;

class CurveSegment extends TrackSegment {
    private Point center;
    private double radius;
    private double startAngle;
    private double sweepAngle;

    /**
     * Constructor to create a curve segment.
     * @param radius The radius of the curve.
     * @param startAngle The starting angle of the curve, in degrees.
     * @param sweepAngle The sweep angle of the curve, in degrees. Positive for counterclockwise, negative for clockwise.
     */
    public CurveSegment(Point startPoint, double radiusMeters, double startAngleDegrees, double sweepAngleDegrees) {
        this.radius = radiusMeters * SCALE;
        this.startAngle = Math.toRadians(startAngleDegrees);
        this.sweepAngle = Math.toRadians(sweepAngleDegrees);
        this.length = radius * Math.abs(this.sweepAngle);
        this.center = calculateCurveCenter(startPoint, this.radius, startAngleDegrees);
    }

    private Point calculateCurveCenter(Point startPoint, double radius, double startAngleDegrees) {
        double angleRad = Math.toRadians(startAngleDegrees);
        int centerX = (int) (startPoint.x - radius * Math.cos(angleRad));
        int centerY = (int) (startPoint.y - radius * Math.sin(angleRad));
        return new Point(centerX, centerY);
    }

    public Point getEnd() {
        double endAngle = startAngle + sweepAngle;
        int x = (int) (center.x + radius * Math.cos(endAngle));
        int y = (int) (center.y + radius * Math.sin(endAngle));
        return new Point(x, y);
    }

    @Override
    public ArrayList<Point> generatePoints() {
        ArrayList<Point> points = new ArrayList<>();
        int numPoints = (int) length;
        for (int i = 0; i <= numPoints; i++) {
            double angle = startAngle + (sweepAngle * i / numPoints);
            int x = (int) (center.x + radius * Math.cos(angle));
            int y = (int) (center.y + radius * Math.sin(angle));
            points.add(new Point(x, y));
        }
        return points;
    }

    @Override
    public Point findPos(double distance) {
        double angle = startAngle + (distance / radius) * Math.signum(sweepAngle); // Use signum to determine direction
        int x = (int) (center.x + radius * Math.cos(angle));
        int y = (int) (center.y + radius * Math.sin(angle));
        return new Point(x, y);
    }

    @Override
    public double getTangentAngle(double distance) {
        // Calculate the angle of the tangent to the curve at the given distance
        double angle = startAngle + (distance / radius) * Math.signum(sweepAngle);
        return angle + (sweepAngle > 0 ? Math.PI / 2 : -Math.PI / 2); // Perpendicular to the radius
    }

}
