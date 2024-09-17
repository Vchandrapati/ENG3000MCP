package org.example;

import javax.swing.*;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class ScreenLogHandler extends Handler {
    private JTextArea textArea;
    private Formatter formatter;

    public ScreenLogHandler(JTextArea textArea) {
        this.textArea = textArea;
        this.formatter = new SimpleFormatter();
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record))
            return;

        String message = formatter.format(record);

        SwingUtilities.invokeLater(() -> {
            textArea.append(message);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    @Override
    public void flush() {
        // Not needed for JTextArea
    }

    @Override
    public void close() throws SecurityException {
        // Not needed for JTextArea
    }
}
