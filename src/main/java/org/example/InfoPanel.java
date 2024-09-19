package org.example;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InfoPanel extends JPanel {
    private final long startupTime;
    private final JLabel currentTimeLabel;
    private final JLabel elapsedTimeLabel;
    private final JLabel connectedTrainsLabel;
    private final JLabel connectedCheckpointsLabel;
    private final JLabel connectedStationsLabel;
    private final JLabel systemStateLabel;

    public InfoPanel(long startupTime) {
        this.startupTime = startupTime;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        currentTimeLabel = new JLabel();
        systemStateLabel = new JLabel();
        elapsedTimeLabel = new JLabel();
        connectedTrainsLabel = new JLabel();
        connectedCheckpointsLabel = new JLabel();
        connectedStationsLabel = new JLabel();

        Font font = new Font("Arial", Font.BOLD, 16);
        currentTimeLabel.setFont(font);
        elapsedTimeLabel.setFont(font);
        connectedTrainsLabel.setFont(font);
        connectedCheckpointsLabel.setFont(font);
        connectedStationsLabel.setFont(font);
        systemStateLabel.setFont(font);

        add(Box.createVerticalStrut(20)); // Add some space at the top
        add(currentTimeLabel);
        add(elapsedTimeLabel);
        add(Box.createVerticalStrut(20)); // Add space between clocks and counts
        add(connectedTrainsLabel);
        add(connectedCheckpointsLabel);
        add(connectedStationsLabel);
        add(systemStateLabel);

        startUpdater();
    }

    private void startUpdater() {
        int delay = 1000; // Update every second
        Timer timer = new Timer(delay, e -> updateInfo());
        timer.start();
    }

    public void updateInfo() {
        // Update time labels
        long currentTimeMillis = System.currentTimeMillis();
        String currentTimeStr = formatTime(currentTimeMillis);

        long elapsedTimeMillis = currentTimeMillis - startupTime;
        String elapsedTimeStr = formatElapsedTime(elapsedTimeMillis);

        currentTimeLabel.setText("Current Time: " + currentTimeStr);
        elapsedTimeLabel.setText("Time Since Startup: " + elapsedTimeStr);

        systemStateLabel.setText("Current system state: " + SystemStateManager.getInstance().getState());

        // Update counts
        Database db = Database.getInstance();
        connectedTrainsLabel.setText("Connected trains: " + db.getTrainCount());
        connectedCheckpointsLabel.setText("Connected checkpoints: " + db.getCheckpointCount());
        connectedStationsLabel.setText("Connected stations: " + db.getStationCount());
    }

    private String formatTime(long timeMillis) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        return timeFormat.format(new Date(timeMillis));
    }

    private String formatElapsedTime(long elapsedMillis) {
        long seconds = elapsedMillis / 1000 % 60;
        long minutes = elapsedMillis / (1000 * 60) % 60;
        long hours = elapsedMillis / (1000 * 60 * 60);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
