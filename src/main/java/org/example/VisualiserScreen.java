package org.example;

import javax.swing.*;
import java.awt.*;

public class VisualiserScreen extends JFrame {
    private final VisualiserPanel trackPanel;
    private final JTextField commandInput;
    private final transient CommandHandler commandHandler;

    public VisualiserScreen() {
        setTitle("Master Control Protocol");
        setSize(1900, 800);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        long startupTime = System.currentTimeMillis();

        trackPanel = new VisualiserPanel();
        InfoPanel infoPanel = new InfoPanel(startupTime);

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());
        userPanel.setPreferredSize(new Dimension(800, 300));

        commandInput = new JTextField();
        Font font = new Font("Arial", Font.PLAIN, 20);
        commandInput.setFont(font);
        commandInput.setPreferredSize(new Dimension(600, 25));
        commandInput.addActionListener(e -> handleCommand());

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroller = new JScrollPane(logArea);
        logScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        userPanel.add(commandInput, BorderLayout.SOUTH);
        userPanel.add(logScroller, BorderLayout.CENTER);

        // Create a horizontal split pane for the bottom half
        JSplitPane sideSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, trackPanel, userPanel);
        sideSplitPane.setDividerLocation(800); // Adjust as needed
        sideSplitPane.setOneTouchExpandable(true);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, infoPanel, sideSplitPane);
        mainSplitPane.setDividerLocation(380); // Adjust as needed
        mainSplitPane.setOneTouchExpandable(true);

        getContentPane().add(mainSplitPane);
        LoggerConfig.setupLogger(logArea);
        startVisualizerUpdater();
        commandHandler = new CommandHandler();
    }

    // Method to handle user input from the text field
    private void handleCommand() {
        String input = commandInput.getText();
        commandInput.setText(""); // Clear the input field
        commandHandler.processInput(input);
    }

    private void startVisualizerUpdater() {
        int delay = 1000; // milliseconds
        Timer timer = new Timer(delay, e -> updateVisualizer());
        timer.start();
    }

    private void updateVisualizer() {
        trackPanel.updateBladeRunnerZones(Database.getInstance().getBladeRunnerClients());
    }
}
