package client.ui.tabs;

import common.*;
import client.ClientCore;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

public class WebcamTab extends JPanel {
    private ClientCore clientCore;
    private LogManager logger;
    private JLabel videoLabel;
    private JButton startBtn;
    private JButton stopBtn;
    private JLabel statusLabel;
    private Thread streamThread;
    private volatile boolean isStreaming = false;

    public WebcamTab(ClientCore clientCore, LogManager logger) {
        this.clientCore = clientCore;
        this.logger = logger;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Video display
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("📹 Webcam Stream"));
        videoLabel = new JLabel("Click 'Start Webcam' to begin streaming", SwingConstants.CENTER);
        videoLabel.setPreferredSize(new Dimension(640, 480));
        videoLabel.setBackground(Color.BLACK);
        videoLabel.setOpaque(true);
        videoLabel.setForeground(Color.WHITE);
        centerPanel.add(videoLabel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        startBtn = new JButton("▶️ Start Webcam");
        stopBtn = new JButton("⏹️ Stop Webcam");
        statusLabel = new JLabel("Status: Stopped");

        stopBtn.setEnabled(false);

        startBtn.addActionListener(e -> startWebcam());
        stopBtn.addActionListener(e -> stopWebcam());

        controlPanel.add(startBtn);
        controlPanel.add(stopBtn);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(statusLabel);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void startWebcam() {
        try {
            Message request = new Message(MessageType.REQUEST, ActionType.START_WEBCAM, null);
            clientCore.sendRequestAsync(request);

            isStreaming = true;
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            statusLabel.setText("Status: Streaming...");

            // Start receiving frames
            streamThread = new Thread(this::receiveFrames);
            streamThread.start();

            logger.info("Webcam streaming started");
        } catch (Exception e) {
            logger.error("Error starting webcam", e);
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void stopWebcam() {
        try {
            isStreaming = false;
            Message request = new Message(MessageType.REQUEST, ActionType.STOP_WEBCAM, null);
            clientCore.sendRequestAsync(request);

            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            statusLabel.setText("Status: Stopped");
            videoLabel.setIcon(null);
            videoLabel.setText("Webcam stopped");

            logger.info("Webcam streaming stopped");
        } catch (Exception e) {
            logger.error("Error stopping webcam", e);
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void receiveFrames() {
        while (isStreaming && clientCore.isConnected()) {
            try {
                Message frame = NetworkUtils.receiveMessage(clientCore.getSocket());

                if (frame.getType().equals(MessageType.STREAM) &&
                    frame.getAction().equals(ActionType.WEBCAM_FRAME)) {

                    String base64Image = frame.getData().getString("frame");
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                    Image img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                    ImageIcon icon = new ImageIcon(img.getScaledInstance(640, 480, Image.SCALE_FAST));

                    SwingUtilities.invokeLater(() -> {
                        videoLabel.setIcon(icon);
                        videoLabel.setText("");
                    });
                }
            } catch (Exception e) {
                if (isStreaming) {
                    logger.error("Error receiving webcam frame", e);
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
}
