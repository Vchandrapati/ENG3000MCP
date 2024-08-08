package org.example;

import java.awt.*;

public class Checkpoint implements Constants {
    static double distance;
    private Point pos;

    public Checkpoint(double distance) {
        this.distance = distance;
    }

    public Checkpoint(Point pos) {
        this.pos = pos;
    }

    public void setPos(Point pos) {
        this.pos = pos;
    }

    public void paint(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(pos.x - 5, pos.y - 5, 10, 10);
    }
}
