package client.ui.tabs;

import common.*;
import client.ClientCore;
import javax.swing.*;
import java.awt.*;

public class SystemControlTab extends JPanel {
    private ClientCore clientCore;
    private LogManager logger;

    public SystemControlTab(ClientCore clientCore, LogManager logger) {
        this.clientCore = clientCore;
        this.logger = logger;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("⚙️ System Power Control"));

        JTextArea infoText = new JTextArea(
            "Control the remote server's power state:\n\n" +
            "  • SHUTDOWN: Turns off the remote computer\n" +
            "  • RESTART: Reboots the remote computer\n\n" +
            "⚠️ WARNING: These actions will immediately affect the remote system.\n" +
            "Any unsaved work will be lost!"
        );
        infoText.setEditable(false);
        infoText.setFont(new Font("Arial", Font.PLAIN, 13));
        infoText.setBackground(new Color(255, 230, 230));
        infoText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(infoText, BorderLayout.CENTER);

        add(infoPanel, BorderLayout.NORTH);

        // Control panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        // Shutdown button
        JButton shutdownBtn = new JButton("⏻ SHUTDOWN");
        shutdownBtn.setFont(new Font("Arial", Font.BOLD, 20));
        shutdownBtn.setBackground(new Color(200, 50, 50));
        shutdownBtn.setForeground(Color.WHITE);
        shutdownBtn.setFocusPainted(false);
        shutdownBtn.setPreferredSize(new Dimension(350, 80));
        shutdownBtn.setMaximumSize(new Dimension(350, 80));
        shutdownBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        shutdownBtn.addActionListener(e -> shutdownSystem());
        centerPanel.add(shutdownBtn);

        centerPanel.add(Box.createVerticalStrut(30));

        // Restart button
        JButton restartBtn = new JButton("🔄 RESTART");
        restartBtn.setFont(new Font("Arial", Font.BOLD, 20));
        restartBtn.setBackground(new Color(50, 120, 200));
        restartBtn.setForeground(Color.WHITE);
        restartBtn.setFocusPainted(false);
        restartBtn.setPreferredSize(new Dimension(350, 80));
        restartBtn.setMaximumSize(new Dimension(350, 80));
        restartBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        restartBtn.addActionListener(e -> restartSystem());
        centerPanel.add(restartBtn);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void shutdownSystem() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "⚠️ Are you sure you want to SHUTDOWN the remote server?\n\n" +
            "This will immediately turn off the computer.\n" +
            "All unsaved work will be lost!",
            "Confirm Shutdown",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Message request = new Message(MessageType.REQUEST, ActionType.SHUTDOWN, null);
                clientCore.sendRequestAsync(request); // Fire and forget

                logger.info("Shutdown command sent");
                JOptionPane.showMessageDialog(this,
                    "Shutdown command sent successfully!\n" +
                    "The remote server is shutting down...",
                    "Shutdown",
                    JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                logger.error("Error sending shutdown command", e);
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void restartSystem() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "⚠️ Are you sure you want to RESTART the remote server?\n\n" +
            "This will immediately reboot the computer.\n" +
            "All unsaved work will be lost!",
            "Confirm Restart",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Message request = new Message(MessageType.REQUEST, ActionType.RESTART, null);
                clientCore.sendRequestAsync(request); // Fire and forget

                logger.info("Restart command sent");
                JOptionPane.showMessageDialog(this,
                    "Restart command sent successfully!\n" +
                    "The remote server is restarting...",
                    "Restart",
                    JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                logger.error("Error sending restart command", e);
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}
