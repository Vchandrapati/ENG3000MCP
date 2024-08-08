package org.example;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Track implements Constants{
    private final int centerX;
    private final int centerY;
    private final double widthCm;
    private final double heightCm;
    final double totalLength;
    public List<Point> checkpoints;


    public Track(int centerX, int centerY, double widthCm, double heightCm) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.widthCm = widthCm; // a
        this.heightCm = heightCm; // b
        this.totalLength = findLength();
        this.checkpoints = new ArrayList<>();
    }

    private Double findLength() {
        double a = widthCm / 2;
        double b = heightCm / 2;
        double h = Math.pow(a - b, 2) / Math.pow(a + b, 2);

        return Math.PI * (a + b) * (1 + (3 * h) / (10 + Math.sqrt(4 - 3 * h)));
    }

    public Point findPos(double distanceCm) {
        double angle = (2 * Math.PI * distanceCm) / totalLength;
        int x = (int) (centerX + (widthCm * SCALE / 2) * Math.cos(angle));
        int y = (int) (centerY + (heightCm * SCALE / 2) * Math.sin(angle));
        return new Point(x, y);
    }

    public void calculateCheckpoints(int numCheckpoints) {
        checkpoints.clear();
        double distanceBetweenCheckpoints = totalLength / numCheckpoints;

        for (int i = 0; i < numCheckpoints; i++) {
            double distanceCm = i * distanceBetweenCheckpoints;
            checkpoints.add(findPos(distanceCm));
        }
    }

    public List<Point> getCheckpoints() {
        return checkpoints;
    }

    public double getTangentAngle(double distanceCm) {
        return (2 * Math.PI * distanceCm) / totalLength;
    }

    public void paint(Graphics2D g) {
        int pixelWidth = (int) (widthCm * SCALE);
        int pixelHeight = (int) (heightCm * SCALE);
        g.drawOval(centerX - pixelWidth / 2, centerY - pixelHeight / 2, pixelWidth, pixelHeight);
    }
}
