package client.ui.tabs;

import common.*;
import client.ClientCore;
import javax.swing.*;
import java.awt.*;

public class SystemLockTab extends JPanel {
    private ClientCore clientCore;
    private LogManager logger;
    private JButton lockBtn;
    private JButton unlockBtn;
    private JLabel statusLabel;
    private boolean isLocked = false;

    public SystemLockTab(ClientCore clientCore, LogManager logger) {
        this.clientCore = clientCore;
        this.logger = logger;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("🔒 System Lock Control"));

        JTextArea infoText = new JTextArea(
            "This feature allows you to lock the remote server's keyboard and mouse.\n\n" +
            "When locked:\n" +
            "  • User cannot use keyboard\n" +
            "  • User cannot use mouse\n" +
            "  • System is frozen until unlocked from this client\n\n" +
            "⚠️ WARNING: Use responsibly! The remote user will be unable to control their computer."
        );
        infoText.setEditable(false);
        infoText.setFont(new Font("Arial", Font.PLAIN, 13));
        infoText.setBackground(new Color(255, 255, 200));
        infoText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(infoText, BorderLayout.CENTER);

        add(infoPanel, BorderLayout.NORTH);

        // Control panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Status
        statusLabel = new JLabel("🔓 Status: Unlocked", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 24));
        statusLabel.setForeground(new Color(0, 150, 0));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(statusLabel);

        centerPanel.add(Box.createVerticalStrut(30));

        // Lock button
        lockBtn = new JButton("🔒 LOCK SYSTEM");
        lockBtn.setFont(new Font("Arial", Font.BOLD, 18));
        lockBtn.setBackground(new Color(220, 50, 50));
        lockBtn.setForeground(Color.WHITE);
        lockBtn.setFocusPainted(false);
        lockBtn.setPreferredSize(new Dimension(300, 60));
        lockBtn.setMaximumSize(new Dimension(300, 60));
        lockBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        lockBtn.addActionListener(e -> lockSystem());
        centerPanel.add(lockBtn);

        centerPanel.add(Box.createVerticalStrut(20));

        // Unlock button
        unlockBtn = new JButton("🔓 UNLOCK SYSTEM");
        unlockBtn.setFont(new Font("Arial", Font.BOLD, 18));
        unlockBtn.setBackground(new Color(50, 150, 50));
        unlockBtn.setForeground(Color.WHITE);
        unlockBtn.setFocusPainted(false);
        unlockBtn.setPreferredSize(new Dimension(300, 60));
        unlockBtn.setMaximumSize(new Dimension(300, 60));
        unlockBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        unlockBtn.setEnabled(false);
        unlockBtn.addActionListener(e -> unlockSystem());
        centerPanel.add(unlockBtn);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void lockSystem() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to LOCK the remote system?\n" +
            "The user will be unable to use keyboard and mouse!",
            "Confirm System Lock",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Message request = new Message(MessageType.REQUEST, ActionType.LOCK_SYSTEM, null);
                Message response = clientCore.sendRequest(request);

                if (response.getData().getBoolean("success")) {
                    isLocked = true;
                    lockBtn.setEnabled(false);
                    unlockBtn.setEnabled(true);
                    statusLabel.setText("🔒 Status: LOCKED");
                    statusLabel.setForeground(new Color(200, 0, 0));
                    logger.info("System locked");
                    JOptionPane.showMessageDialog(this, "System is now LOCKED!");
                }
            } catch (Exception e) {
                logger.error("Error locking system", e);
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void unlockSystem() {
        try {
            Message request = new Message(MessageType.REQUEST, ActionType.UNLOCK_SYSTEM, null);
            Message response = clientCore.sendRequest(request);

            if (response.getData().getBoolean("success")) {
                isLocked = false;
                lockBtn.setEnabled(true);
                unlockBtn.setEnabled(false);
                statusLabel.setText("🔓 Status: Unlocked");
                statusLabel.setForeground(new Color(0, 150, 0));
                logger.info("System unlocked");
                JOptionPane.showMessageDialog(this, "System is now UNLOCKED!");
            }
        } catch (Exception e) {
            logger.error("Error unlocking system", e);
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
