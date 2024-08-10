package org.example;

import java.awt.*;

public class Checkpoint {
    private final Point pos;

    public Checkpoint(Point pos) {
        this.pos = pos;
    }

    public void paint(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(pos.x - 5, pos.y - 5, 10, 10);
    }
}
