package org.example;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InfoPanel extends JPanel {
    private final long startupTime;
    private final JLabel currentTimeLabel;
    private final JLabel elapsedTimeLabel;
    private final JLabel connectedBladeRunnersLabel;
    private final JLabel connectedCheckpointsLabel;
    private final JLabel connectedStationsLabel;
    private final JLabel currentState;
    private final JLabel waitingTimer;
    private long startTime = -1;

    public InfoPanel(long startupTime) {
        this.startupTime = startupTime;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        currentTimeLabel = new JLabel();
        elapsedTimeLabel = new JLabel();
        connectedBladeRunnersLabel = new JLabel();
        connectedCheckpointsLabel = new JLabel();
        connectedStationsLabel = new JLabel();
        currentState = new JLabel();
        waitingTimer = new JLabel();

        Font font = new Font("Arial", Font.BOLD, 16);
        currentTimeLabel.setFont(font);
        elapsedTimeLabel.setFont(font);
        connectedBladeRunnersLabel.setFont(font);
        connectedCheckpointsLabel.setFont(font);
        connectedStationsLabel.setFont(font);
        currentState.setFont(font);
        waitingTimer.setFont(font);

        add(Box.createVerticalStrut(20)); // Add some space at the top
        add(currentTimeLabel);
        add(elapsedTimeLabel);
        add(Box.createVerticalStrut(20)); // Add space between clocks and counts
        add(connectedBladeRunnersLabel);
        add(connectedCheckpointsLabel);
        add(connectedStationsLabel);
        add(currentState);
        add(waitingTimer);

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
        String elapsedTimeStr = formatElapsedTime(elapsedTimeMillis, false);

        currentTimeLabel.setText("Current Time: " + currentTimeStr);
        elapsedTimeLabel.setText("Time Since Startup: " + elapsedTimeStr);

        // Update counts
        Database db = Database.getInstance();
        connectedBladeRunnersLabel.setText("Connected BladeRunners: " + db.getBladeRunnerCount());
        connectedCheckpointsLabel.setText("Connected checkpoints: " + db.getCheckpointCount());
        connectedStationsLabel.setText("Connected stations: " + db.getStationCount());

        SystemState currState = SystemStateManager.getInstance().getState();
        currentState.setText("Current System State: " + currState);

        // Begin timer for waiting state
        if (currState == SystemState.WAITING) {
            waitingTimer.setVisible(true);
            if (startTime == -1)
                startTime = currentTimeMillis;

            // 10 minutes in millis
            long countdownTimeDuration = 10 * 60 * 1000L;
            long remainingTime = countdownTimeDuration - (currentTimeMillis - startTime);
            String timer = formatElapsedTime(remainingTime, true);
            waitingTimer.setText("Time remaining for clients to connect: " + timer);
        } else
            waitingTimer.setVisible(false);
    }

    private String formatTime(long timeMillis) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        return timeFormat.format(new Date(timeMillis));
    }

    private String formatElapsedTime(long elapsedMillis, boolean timer) {
        long seconds = elapsedMillis / 1000 % 60;
        long minutes = elapsedMillis / (1000 * 60) % 60;
        long hours = elapsedMillis / (1000 * 60 * 60);

        return timer ? String.format("%02d:%02d", minutes, seconds) : String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
