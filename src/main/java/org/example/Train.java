package org.example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.logging.Logger;

public class Train implements Paintable, Constants {
    private static final Logger logger = Logger.getLogger(Train.class.getName());
    int x, y;
    int trainW = 50;
    int trainH = 30;
    double speed;
    double distance;
    double angle;
    int id;
    enum State {
        OFF, ON, ERROR
    }
    State trainState;

    public Train(int id, double speed, double distance) {
        this.speed = speed;
        this.distance = distance;
        this.id = id;
        trainState = State.OFF;
    }

    public void setPos(Point p) {
        this.x = (int) p.getX();
        this.y = (int) p.getY();
    }

    public void updatePosition(double scalePerFrame, Track track) {
        double distancePerFrame = this.speed * scalePerFrame;
        this.distance += distancePerFrame; // Update the distance traveled in meters

        double totalTrackLength = track.totalLength;

        if (this.distance > totalTrackLength)
            this.distance = this.distance % totalTrackLength;

        Point newPos = track.findPos(this.distance); // Find the new position on the track
        setPos(newPos); // Update the train's position

        this.angle = track.getTangentAngle(this.distance);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform old = g2d.getTransform();

        g2d.translate(x, y);
        g2d.rotate(angle);

        // Draw the train centered at the origin
        g2d.setColor(Color.GREEN);
        g2d.fillRect(-trainW / 2, -trainH / 2, trainW, trainH);

        BigDecimal roundedSpeed = BigDecimal.valueOf(speed);
        roundedSpeed = roundedSpeed.round(new MathContext(3));

        if(angle < 4 && angle > 2) {
            g2d.rotate(Math.PI);
            g2d.drawString("ID: " + id + " Speed: " + roundedSpeed, -trainW / 2, trainH / 2 + 15);
        }
        else
            g2d.drawString("ID: " + id + " Speed: " + roundedSpeed, -trainW / 2, -trainH / 2 - 5);

        g2d.setTransform(old); // Restore the original transform
    }
}
