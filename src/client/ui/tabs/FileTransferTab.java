package client.ui.tabs;

import common.*;
import client.ClientCore;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Base64;

public class FileTransferTab extends JPanel {
    private ClientCore clientCore;
    private LogManager logger;
    private JTextArea statusArea;
    private JProgressBar progressBar;
    private JTextField remotePathField;
    private JTextField localPathField;

    public FileTransferTab(ClientCore clientCore, LogManager logger) {
        this.clientCore = clientCore;
        this.logger = logger;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Upload section
        JPanel uploadPanel = new JPanel(new BorderLayout(5, 5));
        uploadPanel.setBorder(BorderFactory.createTitledBorder("📤 Upload File (Client → Server)"));

        JPanel uploadFields = new JPanel(new GridLayout(2, 2, 5, 5));
        uploadFields.add(new JLabel("Local File:"));
        localPathField = new JTextField();
        JButton browseLocalBtn = new JButton("Browse");
        browseLocalBtn.addActionListener(e -> browseLocalFile());
        JPanel localPanel = new JPanel(new BorderLayout());
        localPanel.add(localPathField, BorderLayout.CENTER);
        localPanel.add(browseLocalBtn, BorderLayout.EAST);
        uploadFields.add(localPanel);

        uploadFields.add(new JLabel("Remote Path:"));
        JTextField uploadRemoteField = new JTextField("C:\\uploaded_file.txt");
        uploadFields.add(uploadRemoteField);

        uploadPanel.add(uploadFields, BorderLayout.CENTER);
        JButton uploadBtn = new JButton("⬆️ Upload");
        uploadBtn.addActionListener(e -> uploadFile(localPathField.getText(), uploadRemoteField.getText()));
        uploadPanel.add(uploadBtn, BorderLayout.SOUTH);

        centerPanel.add(uploadPanel);
        centerPanel.add(Box.createVerticalStrut(20));

        // Download section
        JPanel downloadPanel = new JPanel(new BorderLayout(5, 5));
        downloadPanel.setBorder(BorderFactory.createTitledBorder("📥 Download File (Server → Client)"));

        JPanel downloadFields = new JPanel(new GridLayout(2, 2, 5, 5));
        downloadFields.add(new JLabel("Remote Path:"));
        remotePathField = new JTextField("C:\\file.txt");
        downloadFields.add(remotePathField);

        downloadFields.add(new JLabel("Save As:"));
        JTextField saveAsField = new JTextField("downloaded_file.txt");
        downloadFields.add(saveAsField);

        downloadPanel.add(downloadFields, BorderLayout.CENTER);
        JButton downloadBtn = new JButton("⬇️ Download");
        downloadBtn.addActionListener(e -> downloadFile(remotePathField.getText(), saveAsField.getText()));
        downloadPanel.add(downloadBtn, BorderLayout.SOUTH);

        centerPanel.add(downloadPanel);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(progressBar);

        add(centerPanel, BorderLayout.CENTER);

        // Status area
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Transfer Status"));
        statusArea = new JTextArea(8, 40);
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        bottomPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void browseLocalFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            localPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void uploadFile(String localPath, String remotePath) {
        new Thread(() -> {
            try {
                File file = new File(localPath);
                if (!file.exists()) {
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "File not found: " + localPath));
                    return;
                }

                updateStatus("Uploading: " + file.getName() + " (" + file.length() + " bytes)");

                // Read file
                byte[] fileData = new byte[(int) file.length()];
                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.read(fileData);
                }

                // Send request
                JSONObject data = new JSONObject();
                data.put("path", remotePath);
                data.put("data", Base64.getEncoder().encodeToString(fileData));
                data.put("size", file.length());

                Message request = new Message(MessageType.REQUEST, ActionType.UPLOAD_FILE, data);
                Message response = clientCore.sendRequest(request);

                if (response.getData().getBoolean("success")) {
                    updateStatus("✅ Upload complete: " + remotePath);
                    SwingUtilities.invokeLater(() -> progressBar.setValue(100));
                } else {
                    updateStatus("❌ Upload failed!");
                }

            } catch (Exception e) {
                logger.error("Upload error", e);
                updateStatus("❌ Error: " + e.getMessage());
            }
        }).start();
    }

    private void downloadFile(String remotePath, String savePath) {
        new Thread(() -> {
            try {
                updateStatus("Requesting: " + remotePath);

                JSONObject data = new JSONObject();
                data.put("path", remotePath);

                Message request = new Message(MessageType.REQUEST, ActionType.DOWNLOAD_FILE, data);
                Message response = clientCore.sendRequest(request);

                if (response.getData().getBoolean("success")) {
                    String base64Data = response.getData().getString("data");
                    byte[] fileData = Base64.getDecoder().decode(base64Data);

                    // Save to file
                    File saveFile = new File(savePath);
                    try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                        fos.write(fileData);
                    }

                    updateStatus("✅ Downloaded: " + saveFile.getAbsolutePath() + " (" + fileData.length + " bytes)");
                    SwingUtilities.invokeLater(() -> progressBar.setValue(100));
                } else {
                    updateStatus("❌ Download failed!");
                }

            } catch (Exception e) {
                logger.error("Download error", e);
                updateStatus("❌ Error: " + e.getMessage());
            }
        }).start();
    }

    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusArea.append(message + "\n");
            statusArea.setCaretPosition(statusArea.getDocument().getLength());
        });
    }
}
