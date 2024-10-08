package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class VisualiserPanel extends JPanel {
    private static final int SEGMENTS = 10;
    private static final double SEGMENT_DRAW_LENGTH = 10;
    private transient List<BladeRunnerClient> bladeRunnerZones = new ArrayList<>();

    private static void drawSegments(double angleIncrement, int centerX, double radiusX,
            int centerY, double radiusY, Graphics2D g2d) {
        for (int i = 1; i <= SEGMENTS; i++) {
            // Current angle for this segment
            double angle = i * angleIncrement;

            // Calculate the start point of the segment line on the edge of the ellipse
            double startX = centerX + radiusX * Math.cos(angle);
            double startY = centerY + radiusY * Math.sin(angle);

            // Calculate the end point of the segment line (inward)
            double endX = centerX + (radiusX - SEGMENT_DRAW_LENGTH) * Math.cos(angle);
            double endY = centerY + (radiusY - SEGMENT_DRAW_LENGTH) * Math.sin(angle);

            // Draw the segment line
            g2d.draw(new Line2D.Double(startX, startY, endX, endY));

            double labelAngle = angle + angleIncrement / 2;
            double labelRadiusX = radiusX - 30; // Adjust distance from center
            double labelRadiusY = radiusY - 30;
            double labelX = centerX + labelRadiusX * Math.cos(labelAngle);
            double labelY = centerY + labelRadiusY * Math.sin(labelAngle);

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            String zoneNumber = String.valueOf(i);

            // Center the label
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(zoneNumber);
            int labelHeight = fm.getAscent();
            g2d.drawString(zoneNumber, (float) labelX - (float) labelWidth / 2,
                    (float) labelY + (float) labelHeight / 2);
        }
    }

    public void updateBladeRunnerZones(List<BladeRunnerClient> newBladeRunnerZones) {
        bladeRunnerZones = newBladeRunnerZones;
        repaint(); // Request the panel to be repainted
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Center point of the panel
        int centerX = width / 2;
        int centerY = height / 2;

        int ellipseWidth = (int) (width * 0.8);
        int ellipseHeight = (int) (height * 0.8);
        int ellipseX = centerX - ellipseWidth / 2;
        int ellipseY = centerY - ellipseHeight / 2;

        // Draw the ellipse
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(Color.BLACK);
        Ellipse2D.Double outerEllipse =
                new Ellipse2D.Double(ellipseX, ellipseY, ellipseWidth, ellipseHeight);
        g2d.draw(outerEllipse);

        // Calculate where to draw lines
        double angleIncrement = 2 * Math.PI / SEGMENTS;

        // Radii for the ellipse
        double radiusX = ellipseWidth / 2.0;
        double radiusY = ellipseHeight / 2.0;

        drawSegments(angleIncrement, centerX, radiusX, centerY, radiusY, g2d);
        drawBladeRunnerLocations(angleIncrement, centerX, radiusX, centerY, radiusY, g2d);
    }

    private void drawBladeRunnerLocations(double angleIncrement, int centerX, double radiusX,
            int centerY, double radiusY, Graphics2D g2d) {
        for (BladeRunnerClient BladeRunner : bladeRunnerZones) {
            if (BladeRunner.isUnmapped())
                continue;

            String status = BladeRunner.getStatus().toString();
            int zone = BladeRunner.getZone();

            // Calculate the angle at the center of the zone
            double theta = zone * angleIncrement + angleIncrement / 2;

            // Calculate the position on the ellipse for the BladeRunner
            double bladeRunnerX = centerX + radiusX * Math.cos(theta);
            double bladeRunnerY = centerY + radiusY * Math.sin(theta);

            // Calculate the angle of the track at this point
            double dx = -radiusX * Math.sin(theta);
            double dy = radiusY * Math.cos(theta);
            double angleOfTangent = Math.atan2(dy, dx);

            // Draw the BladeRunner rotated to face the track direction
            int rectWidth = 20;
            int rectHeight = 10;

            // Create the rectangle centered at (0, 0)
            Rectangle2D rect = new Rectangle2D.Double(-rectWidth / 2.0, -rectHeight / 2.0,
                    rectWidth, rectHeight);

            // Create a transform to position and rotate the rectangle
            AffineTransform transform = new AffineTransform();
            transform.translate(bladeRunnerX, bladeRunnerY);
            transform.rotate(angleOfTangent);

            Shape rotatedRect = transform.createTransformedShape(rect);

            g2d.setColor(Color.GREEN);
            g2d.fill(rotatedRect);

            // Draw the BladeRunner ID near the BladeRunner
            g2d.setColor(Color.BLACK);
            FontMetrics fm = g2d.getFontMetrics();
            int idWidth = fm.stringWidth(BladeRunner.getId());

            double idOffsetX = -idWidth / 2.0;
            double idOffsetY = -rectHeight / 2.0 - 5;

            AffineTransform idTransform = new AffineTransform();
            idTransform.translate(bladeRunnerX, bladeRunnerY);
            idTransform.rotate(angleOfTangent);
            Point2D idPoint = idTransform.transform(new Point2D.Double(idOffsetX, idOffsetY), null);

            g2d.drawString(BladeRunner.getId(), (float) idPoint.getX(), (float) idPoint.getY());
            g2d.drawString(status, (float) idPoint.getX(), (float) idPoint.getY() - 10);
        }
    }
}
