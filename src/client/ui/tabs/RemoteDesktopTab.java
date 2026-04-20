package client.ui.tabs;

import common.*;
import client.ClientCore;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

public class RemoteDesktopTab extends JPanel {
    private ClientCore clientCore;
    private LogManager logger;
    private JLabel desktopLabel;
    private JButton startBtn;
    private JButton stopBtn;
    private JLabel statusLabel;
    private Thread streamThread;
    private volatile boolean isStreaming = false;

    public RemoteDesktopTab(ClientCore clientCore, LogManager logger) {
        this.clientCore = clientCore;
        this.logger = logger;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Desktop display
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("🖥️ Remote Desktop View"));
        desktopLabel = new JLabel("Click 'Start Remote Desktop' to begin", SwingConstants.CENTER);
        desktopLabel.setPreferredSize(new Dimension(800, 600));
        desktopLabel.setBackground(Color.BLACK);
        desktopLabel.setOpaque(true);
        desktopLabel.setForeground(Color.WHITE);

        // Add mouse and keyboard listeners for remote control
        desktopLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isStreaming) {
                    sendMouseEvent("click", e.getX(), e.getY(), e.getButton());
                }
            }
        });

        desktopLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (isStreaming) {
                    sendMouseEvent("move", e.getX(), e.getY(), 0);
                }
            }
        });

        centerPanel.add(new JScrollPane(desktopLabel), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        startBtn = new JButton("▶️ Start Remote Desktop");
        stopBtn = new JButton("⏹️ Stop Remote Desktop");
        statusLabel = new JLabel("Status: Stopped");

        stopBtn.setEnabled(false);

        startBtn.addActionListener(e -> startRemoteDesktop());
        stopBtn.addActionListener(e -> stopRemoteDesktop());

        controlPanel.add(startBtn);
        controlPanel.add(stopBtn);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(statusLabel);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(new JLabel("⚠️ Mouse control enabled when streaming"));

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void startRemoteDesktop() {
        try {
            Message request = new Message(MessageType.REQUEST, ActionType.START_REMOTE_DESKTOP, null);
            clientCore.sendRequestAsync(request);

            isStreaming = true;
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            statusLabel.setText("Status: Streaming...");

            // Start receiving frames
            streamThread = new Thread(this::receiveFrames);
            streamThread.start();

            logger.info("Remote desktop streaming started");
        } catch (Exception e) {
            logger.error("Error starting remote desktop", e);
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void stopRemoteDesktop() {
        try {
            isStreaming = false;
            Message request = new Message(MessageType.REQUEST, ActionType.STOP_REMOTE_DESKTOP, null);
            clientCore.sendRequestAsync(request);

            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            statusLabel.setText("Status: Stopped");
            desktopLabel.setIcon(null);
            desktopLabel.setText("Remote desktop stopped");

            logger.info("Remote desktop streaming stopped");
        } catch (Exception e) {
            logger.error("Error stopping remote desktop", e);
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void receiveFrames() {
        while (isStreaming && clientCore.isConnected()) {
            try {
                Message frame = NetworkUtils.receiveMessage(clientCore.getSocket());

                if (frame.getType().equals(MessageType.STREAM) &&
                    frame.getAction().equals(ActionType.DESKTOP_FRAME)) {

                    String base64Image = frame.getData().getString("frame");
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                    Image img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                    ImageIcon icon = new ImageIcon(img.getScaledInstance(800, 600, Image.SCALE_FAST));

                    SwingUtilities.invokeLater(() -> {
                        desktopLabel.setIcon(icon);
                        desktopLabel.setText("");
                    });
                }
            } catch (Exception e) {
                if (isStreaming) {
                    logger.error("Error receiving desktop frame", e);
                    isStreaming = false;
                    SwingUtilities.invokeLater(() -> {
                        startBtn.setEnabled(true);
                        stopBtn.setEnabled(false);
                        statusLabel.setText("Status: Error - " + e.getMessage());
                    });
                }
                break;
            }
        }
    }

    private void sendMouseEvent(String type, int x, int y, int button) {
        try {
            JSONObject data = new JSONObject();
            data.put("type", type);
            data.put("x", x);
            data.put("y", y);
            data.put("button", button);

            Message request = new Message(MessageType.REQUEST, ActionType.MOUSE_EVENT, data);
            clientCore.sendRequestAsync(request);
        } catch (Exception e) {
            logger.error("Error sending mouse event", e);
        }
    }
}
