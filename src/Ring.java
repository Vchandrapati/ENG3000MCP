import java.awt.Graphics;
import java.awt.Point;
import java.util.logging.Logger;

public class Ring implements Paintable{
    private static final Logger logger = Logger.getLogger(Ring.class.getName());
    private int width;
    private int height;
    private int offsetX = 300;
    private int offsetY = 180;
    private int ovalWidth;
    private int ovalHeight;
    private int x;
    private int y;

    public Ring(int w, int h) {
        setScreenSize(w, h);
    }

    public void paint(Graphics g) {
        g.drawOval(x, y, ovalWidth, ovalHeight);
    }

    public void setScreenSize(int w, int h) {
        ovalWidth = w - offsetX;
        ovalHeight = h - offsetY;
        x = offsetX / 2;
        y = offsetY / 2;
        width = w;
        height = h;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public Point findPos(double angle, int objectSize) {
        int x = (int) (width / 2 + (ovalWidth / 2) * Math.cos(angle)) - objectW / 2;
        int y = (int) (height / 2 + (ovalHeight / 2) * Math.sin(angle)) - objectH / 2;
        return new Point(x, y);
    }
}
