package org.example;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class InfoPanel extends JPanel {
    private final long startupTime;
    private final JLabel currentTimeLabel;
    private final JLabel elapsedTimeLabel;
    private final JLabel connectedBladeRunnersLabel;
    private final JLabel connectedCheckpointsLabel;
    private final JLabel connectedStationsLabel;
    private final JLabel currentState;
    private final JLabel waitingTimer;
    private final JLabel errorClientList;
    private long startTime = -1;
    private final List<JLabel> errorClients;
    private final transient Database db = Database.getInstance();

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
        errorClientList = new JLabel();

        Font font = new Font("Arial", Font.BOLD, 16);
        currentTimeLabel.setFont(font);
        elapsedTimeLabel.setFont(font);
        connectedBladeRunnersLabel.setFont(font);
        connectedCheckpointsLabel.setFont(font);
        connectedStationsLabel.setFont(font);
        currentState.setFont(font);
        waitingTimer.setFont(font);
        errorClientList.setFont(font);

        add(Box.createVerticalStrut(20)); // Add some space at the top
        add(currentTimeLabel);
        add(elapsedTimeLabel);
        add(Box.createVerticalStrut(20)); // Add space between clocks and counts
        add(connectedBladeRunnersLabel);
        add(connectedCheckpointsLabel);
        add(connectedStationsLabel);
        add(currentState);
        add(waitingTimer);
        add(Box.createVerticalStrut(20)); // Add space between clocks and counts
        add(errorClientList);

        startUpdater();

        errorClients = new ArrayList<>();
    }

    private void startUpdater() {
        int delay = 1000; // Update every second
        Timer timer = new Timer(delay, e -> updateInfo());
        timer.start();
    }

    public void updateInfo() {
        // Update time labels
        long currentTimeMillis = updateTime();
        updateCounts();
        updateCurrentStateData(currentTimeMillis);
    }

    private long updateTime() {
        long currentTimeMillis = System.currentTimeMillis();
        String currentTimeStr = formatTime(currentTimeMillis);

        long elapsedTimeMillis = currentTimeMillis - startupTime;
        String elapsedTimeStr = formatElapsedTime(elapsedTimeMillis, false);

        currentTimeLabel.setText("Current Time: " + currentTimeStr);
        elapsedTimeLabel.setText("Time Since Startup: " + elapsedTimeStr);
        return currentTimeMillis;
    }

    private void updateCurrentStateData(long currentTimeMillis) {
        SystemState currState = SystemStateManager.getInstance().getState();
        currentState.setText("Current System State: " + currState);

        // Begin timer for waiting state
        if (currState == SystemState.WAITING) {
            waitingTimer.setVisible(true);
            if (startTime == -1)
                startTime = currentTimeMillis;

            // 10 minutes in millis
            long countdownTimeDuration = TimeUnit.MINUTES.toMillis(10);
            long remainingTime = countdownTimeDuration - (currentTimeMillis - startTime);
            String timer = formatElapsedTime(remainingTime, true);
            waitingTimer.setText("Time remaining for clients to connect: " + timer);

            clearErrorClientLabels();
        } else {
            waitingTimer.setVisible(false);
            if (currState == SystemState.EMERGENCY) {
                errorClientList.setText("Following clients are experiencing an error: ");
                Set<String> clients = db.getAllUnresponsiveClientIDs();
                clearErrorClientLabels();
                createErrorClientLabels(clients);
            } else
                clearErrorClientLabels();
        }
    }

    private void createErrorClientLabels(Set<String> clients) {
        Font font = new Font("Arial", Font.BOLD, 16);

        for (String client : clients) {
            JLabel label = new JLabel(client);
            label.setFont(font);
            errorClients.add(label);
            add(label);
        }
    }

    private void clearErrorClientLabels() {
        for (JLabel label : errorClients) {
            label.setVisible(false);
            remove(label);
        }

        errorClients.clear();
    }

    private void updateCounts() {
        connectedBladeRunnersLabel.setText("Connected BladeRunners: " + db.getBladeRunnerCount());
        connectedCheckpointsLabel.setText("Connected checkpoints: " + db.getCheckpointCount());
        connectedStationsLabel.setText("Connected stations: " + db.getStationCount());
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
