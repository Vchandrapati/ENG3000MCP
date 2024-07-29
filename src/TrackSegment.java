import java.awt.*;
import java.util.ArrayList;

abstract class TrackSegment implements Constants{
    protected double length = 0;

    public double getLength() {
        return length;
    }

    public abstract ArrayList<Point> generatePoints();

    public abstract Point findPos(double distance);

    public abstract double getTangentAngle(double v);
}
