import java.awt.*;

public class Checkpoint implements Paintable {
    int x = 0;
    int y = 0;
    int size = 10;
    double distance;

    public Checkpoint(double distance) {
        this.distance = distance;
    }

    public void setPos(Point p) {
        this.x = (int) p.getX();
        this.y = (int) p.getY();
    }

    @Override
    public void paint(Graphics g) {
        g.drawRect(x, y, size, size);
    }
}
