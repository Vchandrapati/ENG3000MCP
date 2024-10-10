package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VisualiserPanel extends JPanel {
    private static final Database db = Database.getInstance();
    private static final double SEGMENT_DRAW_LENGTH = 10;
    private transient List<BladeRunnerClient> bladeRunnerZones = new ArrayList<>();

    private static void drawSegments(double angleIncrement, int centerX, double radiusX,
            int centerY, double radiusY, Graphics2D g2d) {
        for (int i = 1; i <= db.getBlockCount(); i++) {
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

            // // Check if the current checkpoint has a station
            // Optional<StationClient> stationClientOpt = db.getClient("ST" + (i < 10 ? "0" + i :
            // i), StationClient.class);
            // if (stationClientOpt.isPresent()) {
            // StationClient stationClient = stationClientOpt.get();

            // // Calculate the position of the station text box
            // double stationLabelX = centerX + (radiusX - 50) * Math.cos(labelAngle);
            // double stationLabelY = centerY + (radiusY - 50) * Math.sin(labelAngle);

            // // Get station ID and status
            // String stationId = stationClient.getId();
            // String stationStatus = stationClient.getStatus().toString();

            // // Draw the station ID and status
            // g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            // String stationInfo = stationId + " (" + stationStatus + ")";
            // int stationLabelWidth = fm.stringWidth(stationInfo);
            // g2d.setColor(Color.BLUE);
            // g2d.drawString(stationInfo, (float) stationLabelX - (float) stationLabelWidth / 2,
            // (float) stationLabelY);
            // }
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
        double angleIncrement = 2 * Math.PI / db.getBlockCount();

        // Radii for the ellipse
        double radiusX = ellipseWidth / 2.0;
        double radiusY = ellipseHeight / 2.0;

        drawSegments(angleIncrement, centerX, radiusX, centerY, radiusY, g2d);
        drawBladeRunnerLocations(angleIncrement, centerX, radiusX, centerY, radiusY, g2d);
    }

    private void drawBladeRunnerLocations(double angleIncrement, int centerX, double radiusX,
            int centerY, double radiusY, Graphics2D g2d) {
        for (BladeRunnerClient bladeRunner : bladeRunnerZones) {
            if (bladeRunner.isUnmapped())
                continue;

            String status = bladeRunner.getStatus().toString();
            int zone = bladeRunner.getZone();

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
            int rectWidth = 70;
            int rectHeight = 30;

            // Create the rectangle centered at (0, 0)
            Rectangle2D rect = new Rectangle2D.Double(-rectWidth / 2.0, -rectHeight / 2.0,
                    rectWidth, rectHeight);

            // Create a transform to position and rotate the rectangle
            AffineTransform transform = new AffineTransform();
            transform.translate(bladeRunnerX, bladeRunnerY);
            transform.rotate(angleOfTangent);

            Shape rotatedRect = transform.createTransformedShape(rect);

            if (bladeRunner.currentStatus != MessageEnums.CCPStatus.ERR)
                g2d.setColor(Color.GREEN);
            else
                g2d.setColor(Color.RED);

            g2d.fill(rotatedRect);

            // Draw the BladeRunner ID near the BladeRunner
            g2d.setColor(Color.BLACK);
            FontMetrics fm = g2d.getFontMetrics();
            int idWidth = fm.stringWidth(bladeRunner.getId());

            double idOffsetX = -idWidth / 2.0;
            double idOffsetY = -rectHeight / 2.0 - 15; // Move the ID further away from the
                                                       // rectangle

            AffineTransform idTransform = new AffineTransform();
            idTransform.translate(bladeRunnerX, bladeRunnerY);
            idTransform.rotate(angleOfTangent);

            Point2D idPoint = idTransform.transform(new Point2D.Double(idOffsetX, idOffsetY), null);

            g2d.drawString(bladeRunner.getId(), (float) idPoint.getX(), (float) idPoint.getY());
            g2d.drawString(status, (float) idPoint.getX(), (float) idPoint.getY() - 20);
        }
    }
}
