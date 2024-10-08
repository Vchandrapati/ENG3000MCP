package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class ClientsPanel extends JPanel {
    private final DefaultTableModel tableModel;

    public ClientsPanel() {
        setLayout(new BorderLayout());

        String[] columnNames = {"Client ID", "Type", "Status", "Last Exec Message Sent", "last Message Received", "Sequence Count", "Missed Stat Count"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable clientsTable = new JTable(tableModel);

        // Add table to panel
        JScrollPane scrollPane = new JScrollPane(clientsTable);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
        setPreferredSize(new Dimension(800, 400));
    }

    public void updateClientsData() {
        List<Client> updatedClients = Database.getInstance().getClients();
        updatedClients.sort(Comparator.comparing(Client::getId));

        // Refresh data
        tableModel.setRowCount(0);

        // Populate the table with updated client data
        for (Client client : updatedClients) {
            Object[] rowData = {client.getId(), client.getClass().getSimpleName(),
                    client.getStatus(), client.getLastExecMessageSent(), client.getLastResponse(), client.getSequenceCount(), client.getMissedStatCount()};
            tableModel.addRow(rowData);
        }

        // Refresh the table view
        tableModel.fireTableDataChanged();
    }
}
