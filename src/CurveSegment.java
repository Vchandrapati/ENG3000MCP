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
        this.radius = radiusMeters; // Radius stored in meters
        this.startAngle = Math.toRadians(startAngleDegrees);
        this.sweepAngle = Math.toRadians(sweepAngleDegrees);

        double radiusPixels = radiusMeters * Constants.SCALE; // Convert radius to pixels
        this.length = (radiusPixels * Math.abs(this.sweepAngle)) / Constants.SCALE; // Length in meters

        this.center = calculateCurveCenter(startPoint, radiusPixels, startAngleDegrees);
    }

    private Point calculateCurveCenter(Point startPoint, double radiusPixels, double startAngleDegrees) {
        double angleRad = Math.toRadians(startAngleDegrees);
        int centerX = (int) (startPoint.x - radiusPixels * Math.cos(angleRad));
        int centerY = (int) (startPoint.y - radiusPixels * Math.sin(angleRad));
        return new Point(centerX, centerY);
    }

    public Point getEnd() {
        double endAngle = startAngle + sweepAngle;
        double radiusPixels = radius * Constants.SCALE;
        int x = (int) (center.x + radiusPixels * Math.cos(endAngle));
        int y = (int) (center.y + radiusPixels * Math.sin(endAngle));
        return new Point(x, y);
    }

    @Override
    public ArrayList<Point> generatePoints() {
        ArrayList<Point> points = new ArrayList<>();
        int numPoints = (int) (length * SCALE);
        double radiusPixels = radius * Constants.SCALE;
        for (int i = 0; i <= numPoints; i++) {
            double angle = startAngle + (sweepAngle * i / numPoints);
            int x = (int) (center.x + radiusPixels * Math.cos(angle));
            int y = (int) (center.y + radiusPixels * Math.sin(angle));
            points.add(new Point(x, y));
        }
        return points;
    }

    @Override
    public Point findPos(double distance) {
        double distancePixels = distance * Constants.SCALE; // Convert distance to pixels
        double angle = startAngle + (distancePixels / (radius * Constants.SCALE)) * Math.signum(sweepAngle);
        double radiusPixels = radius * Constants.SCALE; // Radius in pixels
        int x = (int) (center.x + radiusPixels * Math.cos(angle));
        int y = (int) (center.y + radiusPixels * Math.sin(angle));
        return new Point(x, y);
    }

    @Override
    public double getTangentAngle(double distance) {
        // Calculate the angle of the tangent to the curve at the given distance
        double distancePixels = distance * Constants.SCALE; // Convert distance to pixels
        double angle = startAngle + (distancePixels / (radius * SCALE)) * Math.signum(sweepAngle);
        return angle + (sweepAngle > 0 ? Math.PI / 2 : -Math.PI / 2); // Perpendicular to the radius
    }

}
