package org.example;

import java.awt.*;
import java.util.logging.Logger;

public class Station implements Paintable{
    private static final Logger logger = Logger.getLogger(Station.class.getName());
    int stationSizeH = 50;
    int stationSizeW = 25;
    int x = 0;
    int y = 0;
    double distance;
    enum State {
        OPENDOOR, CLOSEDOOR, OFF
    }
    State stationState = State.OFF;

    public Station(double distance) {
        this.distance = distance;
        stationState = State.OFF;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawRect(0, 0, stationSizeW, stationSizeH);
        g2d.drawString(stationState.name(), 0, stationSizeW + 15);
    }

    public void setPos(Point p) {
        this.x = (int) p.getX();
        this.y = (int) p.getY();
    }
}
