package client.ui.tabs;

import common.*;
import client.ClientCore;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;

public class ScreenshotTab extends JPanel {
    private ClientCore clientCore;
    private LogManager logger;
    private JLabel imageLabel;
    private BufferedImage currentImage; // Store original image for saving

    public ScreenshotTab(ClientCore clientCore, LogManager logger) {
        this.clientCore = clientCore;
        this.logger = logger;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        imageLabel = new JLabel("No screenshot", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(800, 600));
        add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton captureBtn = new JButton("📷 Capture Screenshot");
        JButton saveBtn = new JButton("💾 Save");

        captureBtn.addActionListener(e -> captureScreenshot());
        saveBtn.addActionListener(e -> saveScreenshot());

        buttonPanel.add(captureBtn);
        buttonPanel.add(saveBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void captureScreenshot() {
        try {
            logger.info("Requesting screenshot...");
            Message request = new Message(MessageType.REQUEST, ActionType.SCREENSHOT, null);
            Message response = clientCore.sendRequest(request);

            if (response.getData().getBoolean("success")) {
                String base64Image = response.getData().getString("image");
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                // Store original image for saving
                currentImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

                // Display scaled version
                ImageIcon icon = new ImageIcon(currentImage.getScaledInstance(800, 600, Image.SCALE_SMOOTH));
                imageLabel.setIcon(icon);
                imageLabel.setText("");

                logger.info("Screenshot received: " + imageBytes.length + " bytes");
            }
        } catch (Exception e) {
            logger.error("Error capturing screenshot", e);
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void saveScreenshot() {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this,
                "No screenshot to save! Capture a screenshot first.",
                "No Image",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Show file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Screenshot");
            fileChooser.setSelectedFile(new File("screenshot_" + System.currentTimeMillis() + ".png"));

            // Add file filters
            fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "PNG Images (*.png)", "png"));
            fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "JPEG Images (*.jpg, *.jpeg)", "jpg", "jpeg"));
            fileChooser.setAcceptAllFileFilterUsed(false);

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File saveFile = fileChooser.getSelectedFile();

                // Determine format from filter or extension
                String format = "png"; // default
                javax.swing.filechooser.FileFilter filter = fileChooser.getFileFilter();
                if (filter.getDescription().contains("JPEG")) {
                    format = "jpg";
                } else if (!saveFile.getName().toLowerCase().endsWith(".png")) {
                    // Add .png if no extension
                    saveFile = new File(saveFile.getAbsolutePath() + ".png");
                }

                // Save image
                ImageIO.write(currentImage, format, saveFile);

                logger.info("Screenshot saved to: " + saveFile.getAbsolutePath());
                JOptionPane.showMessageDialog(this,
                    "Screenshot saved successfully!\n" + saveFile.getAbsolutePath(),
                    "Save Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            logger.error("Error saving screenshot", e);
            JOptionPane.showMessageDialog(this,
                "Error saving screenshot: " + e.getMessage(),
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
