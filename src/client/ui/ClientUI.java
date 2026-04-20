package client.ui;

import common.*;
import client.ClientCore;
import client.ui.tabs.*;
import javax.swing.*;
import java.awt.*;

/**
 * Main UI cho Client - tabbed interface với 10 tabs cho từng tính năng.
 */
public class ClientUI extends JFrame {
    private LogManager logger;
    private ClientCore clientCore;
    private JTabbedPane tabbedPane;
    private JTextArea logArea;
    private JLabel statusLabel;

    // Connection info
    private JTextField hostField;
    private JTextField portField;
    private JButton connectButton;

    public ClientUI(LogManager logger) {
        this.logger = logger;
        this.clientCore = new ClientCore(logger);

        initComponents();
        logger.addListener(this::appendLog);
    }

    private void initComponents() {
        setTitle("Remote Control Client");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // === TOP PANEL: Connection ===
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(new Color(52, 73, 94));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title row
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleRow.setBackground(new Color(52, 73, 94));

        JLabel titleLabel = new JLabel("🖥️ REMOTE CONTROL CLIENT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleRow.add(titleLabel);

        titleRow.add(Box.createHorizontalStrut(20));

        // Display Client IP
        JLabel clientIpLabel = new JLabel("Client IP: " + getLocalIPAddress());
        clientIpLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        clientIpLabel.setForeground(new Color(255, 223, 186));
        titleRow.add(clientIpLabel);

        topPanel.add(titleRow);
        topPanel.add(Box.createVerticalStrut(5));

        // Connection controls row
        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlRow.setBackground(new Color(52, 73, 94));

        JLabel hostLabel = new JLabel("Server:");
        hostLabel.setForeground(Color.WHITE);
        controlRow.add(hostLabel);

        hostField = new JTextField("localhost", 12);
        controlRow.add(hostField);

        JLabel portLabel = new JLabel("Port:");
        portLabel.setForeground(Color.WHITE);
        controlRow.add(portLabel);

        portField = new JTextField(String.valueOf(Constants.DEFAULT_PORT), 6);
        controlRow.add(portField);

        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> toggleConnection());
        controlRow.add(connectButton);

        statusLabel = new JLabel("● Disconnected");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        controlRow.add(statusLabel);

        topPanel.add(controlRow);
        add(topPanel, BorderLayout.NORTH);

        // === CENTER: Tabbed Pane ===
        tabbedPane = new JTabbedPane();

        // Add all feature tabs
        tabbedPane.addTab("Applications", new ApplicationTab(clientCore, logger));
        tabbedPane.addTab("Processes", new ProcessTab(clientCore, logger));
        tabbedPane.addTab("Screenshot", new ScreenshotTab(clientCore, logger));
        tabbedPane.addTab("Keylogger", new KeyloggerTab(clientCore, logger));
        tabbedPane.addTab("File Transfer", new FileTransferTab(clientCore, logger));
        tabbedPane.addTab("System Control", new SystemControlTab(clientCore, logger));
        tabbedPane.addTab("Webcam", new WebcamTab(clientCore, logger));
        tabbedPane.addTab("Network Monitor", new NetworkMonitorTab(clientCore, logger));
        tabbedPane.addTab("Remote Desktop", new RemoteDesktopTab(clientCore, logger));
        tabbedPane.addTab("System Lock", new SystemLockTab(clientCore, logger));

        add(tabbedPane, BorderLayout.CENTER);

        // === BOTTOM: Log Panel ===
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Logs"));
        bottomPanel.setPreferredSize(new Dimension(getWidth(), 200));

        logArea = new JTextArea(10, 80);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(200, 200, 200));

        JScrollPane logScroll = new JScrollPane(logArea);
        bottomPanel.add(logScroll, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Toggle connection to server.
     */
    private void toggleConnection() {
        if (clientCore.isConnected()) {
            clientCore.disconnect();
            connectButton.setText("Connect");
            statusLabel.setText("● Disconnected");
            statusLabel.setForeground(Color.RED);
        } else {
            String host = hostField.getText();
            int port;
            try {
                port = Integer.parseInt(portField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid port number!");
                return;
            }

            if (clientCore.connect(host, port)) {
                connectButton.setText("Disconnect");
                statusLabel.setText("● Connected");
                statusLabel.setForeground(Color.GREEN);
            } else {
                JOptionPane.showMessageDialog(this, "Connection failed!");
            }
        }
    }

    /**
     * Append log to log area.
     */
    public void appendLog(String level, String message, String timestamp) {
        SwingUtilities.invokeLater(() -> {
            String line = String.format("[%s] [%s] %s\n", timestamp, level, message);
            logArea.append(line);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Get local IP address of this machine.
     * Shows the primary IPv4 address (useful for LAN connections).
     *
     * @return IP address string
     */
    private String getLocalIPAddress() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces =
                java.net.NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();

                // Skip loopback and down interfaces
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress addr = addresses.nextElement();

                    // Return first IPv4 address
                    if (addr instanceof java.net.Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            return "Unable to detect";
        }

        return "No network found";
    }
}
