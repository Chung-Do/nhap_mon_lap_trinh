package client.ui.tabs;

import common.*;
import client.ClientCore;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ProcessTab extends JPanel {
    private ClientCore clientCore;
    private LogManager logger;
    private JTable processTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public ProcessTab(ClientCore clientCore, LogManager logger) {
        this.clientCore = clientCore;
        this.logger = logger;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel: Search
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        topPanel.add(searchField);
        JButton searchBtn = new JButton("🔍 Filter");
        searchBtn.addActionListener(e -> filterProcesses());
        topPanel.add(searchBtn);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Process Name", "PID", "Memory (MB)", "CPU (%)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        processTable = new JTable(tableModel);
        processTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(processTable), BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("🔄 Refresh List");
        JButton killBtn = new JButton("⛔ Kill Process");

        refreshBtn.addActionListener(e -> refreshProcessList());
        killBtn.addActionListener(e -> killProcess());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(killBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshProcessList() {
        try {
            Message request = new Message(MessageType.REQUEST, ActionType.LIST_PROCESSES, null);
            Message response = clientCore.sendRequest(request);

            if (response.getData().getBoolean("success")) {
                JSONArray processes = response.getData().getJSONArray("processes");
                tableModel.setRowCount(0);
                for (int i = 0; i < processes.length(); i++) {
                    JSONObject proc = processes.getJSONObject(i);
                    tableModel.addRow(new Object[]{
                        proc.optString("Name", ""),
                        proc.optInt("Id", 0),
                        String.format("%.1f", proc.optDouble("WorkingSet", 0) / (1024*1024)),
                        String.format("%.1f", proc.optDouble("CPU", 0))
                    });
                }
                logger.info("Loaded " + processes.length() + " processes");
            }
        } catch (Exception e) {
            logger.error("Error refreshing process list", e);
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void filterProcesses() {
        String searchTerm = searchField.getText().toLowerCase();
        // Simple filter implementation - in production, filter on server side
        refreshProcessList();
    }

    private void killProcess() {
        int row = processTable.getSelectedRow();
        if (row >= 0) {
            String processName = (String) tableModel.getValueAt(row, 0);
            int pid = (int) tableModel.getValueAt(row, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                "Kill process '" + processName + "' (PID: " + pid + ")?",
                "Confirm Kill",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    JSONObject data = new JSONObject();
                    data.put("pid", pid);
                    Message request = new Message(MessageType.REQUEST, ActionType.KILL_PROCESS, data);
                    Message response = clientCore.sendRequest(request);

                    if (response.getData().getBoolean("success")) {
                        JOptionPane.showMessageDialog(this, "Process killed!");
                        refreshProcessList();
                    }
                } catch (Exception e) {
                    logger.error("Error killing process", e);
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a process first!");
        }
    }
}
