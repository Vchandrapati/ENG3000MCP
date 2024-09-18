package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

public class VisualiserScreen extends JFrame {
    private VisualiserPanel trackPanel;
    private JTextField commandInput;
    private JTextArea logArea;
    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private CommandHandler commandHandler = new CommandHandler();
    public VisualiserScreen() {
        setTitle("Master Control Protocol");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        trackPanel = new VisualiserPanel();
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());
        userPanel.setPreferredSize(new Dimension(300, 300));

        commandInput = new JTextField();
        Font font = new Font("Arial", Font.PLAIN, 20);
        commandInput.setFont(font);
        commandInput.setPreferredSize(new Dimension(300, 25));
        commandInput.addActionListener(e -> handleCommand());

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroller = new JScrollPane(logArea);
        logScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        userPanel.add(commandInput, BorderLayout.SOUTH);
        userPanel.add(logScroller, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, trackPanel, userPanel);
        splitPane.setDividerLocation(700);
        splitPane.setOneTouchExpandable(true);

        getContentPane().add(splitPane);
        LoggerConfig.setupLogger(logArea);
        startVisualizerUpdater();
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
        trackPanel.updateTrainZones(Database.getInstance().getTrainBlockMap());
    }

    public VisualiserPanel getVisualiserPanel() {
        return trackPanel;
    }

    public JTextField getUserInputField() {
        return commandInput;
    }

    public JTextArea getLogArea() {
        return logArea;
    }
}
