package client.ui.tabs;

import common.*;
import client.ClientCore;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;

public class NetworkMonitorTab extends JPanel {
    private ClientCore clientCore;
    private LogManager logger;
    private JLabel bandwidthLabel;
    private JLabel latencyLabel;
    private JTextArea historyArea;

    public NetworkMonitorTab(ClientCore clientCore, LogManager logger) {
        this.clientCore = clientCore;
        this.logger = logger;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Metrics panel
        JPanel metricsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        metricsPanel.setBorder(BorderFactory.createTitledBorder("📊 Network Metrics"));

        metricsPanel.add(new JLabel("Bandwidth:", SwingConstants.RIGHT));
        bandwidthLabel = new JLabel("-- MB/s", SwingConstants.LEFT);
        bandwidthLabel.setFont(new Font("Arial", Font.BOLD, 16));
        metricsPanel.add(bandwidthLabel);

        metricsPanel.add(new JLabel("Latency:", SwingConstants.RIGHT));
        latencyLabel = new JLabel("-- ms", SwingConstants.LEFT);
        latencyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        metricsPanel.add(latencyLabel);

        add(metricsPanel, BorderLayout.NORTH);

        // History area
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Test History"));
        historyArea = new JTextArea(15, 50);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        historyPanel.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        add(historyPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton measureBandwidthBtn = new JButton("📈 Measure Bandwidth");
        JButton measureLatencyBtn = new JButton("⏱️ Measure Latency");
        JButton clearBtn = new JButton("🗑️ Clear History");

        measureBandwidthBtn.addActionListener(e -> measureBandwidth());
        measureLatencyBtn.addActionListener(e -> measureLatency());
        clearBtn.addActionListener(e -> historyArea.setText(""));

        buttonPanel.add(measureBandwidthBtn);
        buttonPanel.add(measureLatencyBtn);
        buttonPanel.add(clearBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void measureBandwidth() {
        new Thread(() -> {
            try {
                updateHistory("Measuring bandwidth...");

                long startTime = System.currentTimeMillis();
                Message request = new Message(MessageType.REQUEST, ActionType.MEASURE_BANDWIDTH, null);
                Message response = clientCore.sendRequest(request);
                long endTime = System.currentTimeMillis();

                if (response.getData().getBoolean("success")) {
                    double bandwidth = response.getData().getDouble("bandwidth");
                    long dataSize = response.getData().optLong("dataSize", 0);
                    long duration = endTime - startTime;

                    SwingUtilities.invokeLater(() -> {
                        bandwidthLabel.setText(String.format("%.2f MB/s", bandwidth));
                    });

                    updateHistory(String.format("✅ Bandwidth: %.2f MB/s (transferred %d bytes in %d ms)",
                        bandwidth, dataSize, duration));
                    logger.info("Bandwidth measured: " + bandwidth + " MB/s");
                }
            } catch (Exception e) {
                logger.error("Error measuring bandwidth", e);
                updateHistory("❌ Error: " + e.getMessage());
            }
        }).start();
    }

    private void measureLatency() {
        new Thread(() -> {
            try {
                updateHistory("Measuring latency (ping)...");

                long startTime = System.nanoTime();
                Message request = new Message(MessageType.REQUEST, ActionType.MEASURE_LATENCY, null);
                Message response = clientCore.sendRequest(request);
                long endTime = System.nanoTime();

                double latency = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds

                if (response.getData().getBoolean("success")) {
                    SwingUtilities.invokeLater(() -> {
                        latencyLabel.setText(String.format("%.2f ms", latency));
                    });

                    updateHistory(String.format("✅ Latency: %.2f ms (round-trip time)", latency));
                    logger.info("Latency measured: " + latency + " ms");
                }
            } catch (Exception e) {
                logger.error("Error measuring latency", e);
                updateHistory("❌ Error: " + e.getMessage());
            }
        }).start();
    }

    private void updateHistory(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            historyArea.append(String.format("[%s] %s\n", timestamp, message));
            historyArea.setCaretPosition(historyArea.getDocument().getLength());
        });
    }
}
