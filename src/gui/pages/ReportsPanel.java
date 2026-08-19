package gui.pages;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

import gui.components.RoundedButton;
import gui.components.RoundedPanel;
import gui.theme.UITheme;
import util.LogManager;
import util.LogManager.LogEntry;

public class ReportsPanel extends JPanel {

    private JPanel logsContainer;
    private JScrollPane scrollPane;

    public ReportsPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, UITheme.SPACE_XXS, UITheme.SPACE_SM, UITheme.SPACE_XXS));

        JLabel title = new JLabel("Admin Activity Log");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_WHITE);

        RoundedButton refreshBtn = new RoundedButton("Refresh", UITheme.BORDER_DARK, null, UITheme.RADIUS_LG);
        refreshBtn.setPreferredSize(new Dimension(100, 38));
        refreshBtn.addActionListener(e -> refreshLogs());

        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        RoundedPanel wrapper = new RoundedPanel(UITheme.RADIUS_LG, UITheme.CARD_BG);
        wrapper.setLayout(new BorderLayout());

        logsContainer = new JPanel();
        logsContainer.setLayout(new BoxLayout(logsContainer, BoxLayout.Y_AXIS));
        logsContainer.setOpaque(false);

        scrollPane = new JScrollPane(logsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        wrapper.add(scrollPane, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        refreshLogs();
    }

    private void refreshLogs() {
        logsContainer.removeAll();

        List<LogEntry> logs = LogManager.getAllLogs();

        if (logs.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);

            JLabel emptyLabel = new JLabel("No activity logs yet");
            emptyLabel.setFont(UITheme.FONT_SUBTITLE);
            emptyLabel.setForeground(UITheme.TEXT_MUTED);
            emptyPanel.add(emptyLabel);

            logsContainer.add(emptyPanel);
        } else {
            for (LogEntry log : logs) {
                logsContainer.add(createLogCard(log));
                logsContainer.add(Box.createVerticalStrut(UITheme.SPACE_XS));
            }
        }

        logsContainer.revalidate();
        logsContainer.repaint();
    }

    private JPanel createLogCard(LogEntry log) {
        JPanel card = new JPanel(new BorderLayout(UITheme.SPACE_MD, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS_SM, UITheme.RADIUS_SM);
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = Math.max(d.height, 90);
                return d;
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(UITheme.SPACE_MD, UITheme.SPACE_LG, UITheme.SPACE_MD, UITheme.SPACE_LG));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel actionLabel = new JLabel(formatAction(log.action));
        actionLabel.setFont(UITheme.FONT_BOLD);
        actionLabel.setForeground(getActionColor(log.action));
        actionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel detailsLabel = new JLabel("<html><body style='width: 600px'>" + log.details + "</body></html>");
        detailsLabel.setFont(UITheme.FONT_REGULAR);
        detailsLabel.setForeground(UITheme.TEXT_WHITE);
        detailsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel adminLabel = new JLabel("By: " + log.adminInfo);
        adminLabel.setFont(UITheme.FONT_SMALL);
        adminLabel.setForeground(UITheme.TEXT_MUTED);
        adminLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(actionLabel);
        leftPanel.add(Box.createVerticalStrut(UITheme.SPACE_XXS));
        leftPanel.add(detailsLabel);
        leftPanel.add(Box.createVerticalStrut(UITheme.SPACE_XXS));
        leftPanel.add(adminLabel);

        JLabel timeLabel = new JLabel(log.timestamp);
        timeLabel.setFont(UITheme.FONT_MONO);
        timeLabel.setForeground(UITheme.TEXT_MUTED);
        timeLabel.setVerticalAlignment(SwingConstants.TOP);

        card.add(leftPanel, BorderLayout.CENTER);
        card.add(timeLabel, BorderLayout.EAST);

        return card;
    }

    private String formatAction(String action) {
        switch (action) {
            case "USER_CREATED":
                return "User Created";
            case "USER_UPDATED":
                return "User Updated";
            case "USER_DELETED":
                return "User Deleted";
            case "MODULE_CREATED":
                return "Module Created";
            case "MODULE_UPDATED":
                return "Module Updated";
            case "MODULE_DELETED":
                return "Module Deleted";
            case "GROUP_CREATED":
                return "Group Created";
            case "GROUP_DELETED":
                return "Group Deleted";
            case "LOGIN":
                return "Login";
            case "LOGOUT":
                return "Logout";
            default:
                return action;
        }
    }

    private Color getActionColor(String action) {
        if (action.contains("DELETED"))
            return UITheme.STATUS_ERROR;
        if (action.contains("CREATED"))
            return UITheme.STATUS_ACTIVE;
        if (action.contains("UPDATED"))
            return UITheme.STATUS_WARNING;
        if (action.equals("LOGIN") || action.equals("LOGOUT"))
            return UITheme.PRIMARY;
        return UITheme.TEXT_WHITE;
    }
}
