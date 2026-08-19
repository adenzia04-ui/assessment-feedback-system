package gui.pages;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import gui.components.RoundedPanel;
import gui.AdminFrame;
import gui.theme.UITheme;

public class AdminDashboardPanel extends JPanel {

    private AdminFrame parentFrame;

    public AdminDashboardPanel(AdminFrame parent) {
        this.parentFrame = parent;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(0, 0, 0, 0));

        add(buildQuickAccess());
        add(Box.createVerticalStrut(20));
        add(buildSectionPlaceholder("Recent Activity Log"));
    }

    private JComponent buildQuickAccess() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        JLabel title = new JLabel("Quick Access");
        title.setForeground(UITheme.PRIMARY);
        title.setFont(UITheme.FONT_TITLE.deriveFont(20f));
        container.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 4, 20, 0)); // 1 row, 4 cols
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(15, 0, 15, 0));

        grid.add(glassCard("Manage Users", "Add/Edit Staff & Students", "USERS", new Color(0, 200, 255)));
        grid.add(glassCard("Modules", "Course Configuration", "MODULES", new Color(200, 100, 255)));
        grid.add(glassCard("Class Groups", "Allocations", "GROUPS", new Color(255, 180, 50)));
        grid.add(glassCard("Reports", "System Analytics", "REPORTS", new Color(100, 255, 150)));

        container.add(grid, BorderLayout.CENTER);
        
        // Wrap in a panel to constrain height if needed, but BoxLayout handles it.
        container.setMaximumSize(new Dimension(2000, 250));
        return container;
    }

    private JComponent glassCard(String heading, String sub, String navKey, Color iconColor) {
        // We use a custom RoundedPanel that supports hover
        JPanel card = new JPanel() {
            private boolean hover = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Animated Hover Color
                g2.setColor(hover ? new Color(60, 60, 75, 200) : UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                // Border
                g2.setColor(hover ? iconColor : UITheme.GLASS_BORDER);
                g2.setStroke(new BasicStroke(hover ? 2 : 1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);
            }
            
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true; repaint(); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
                    public void mouseExited(MouseEvent e) { hover = false; repaint(); setCursor(Cursor.getDefaultCursor()); }
                    public void mouseClicked(MouseEvent e) { if(parentFrame!=null) parentFrame.showCard(navKey); }
                });
            }
        };
        
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Content
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel h = new JLabel(heading);
        h.setForeground(UITheme.TEXT_WHITE);
        h.setFont(UITheme.FONT_BOLD.deriveFont(16f));

        JLabel s = new JLabel("<html>" + sub + "</html>");
        s.setForeground(UITheme.TEXT_MUTED);
        s.setFont(UITheme.FONT_REGULAR.deriveFont(12f));

        text.add(h);
        text.add(Box.createVerticalStrut(8));
        text.add(s);

        // Icon/Color Strip
        JPanel strip = new JPanel();
        strip.setOpaque(false);
        strip.setPreferredSize(new Dimension(4, 30));
        strip.setBackground(iconColor); // Just a placeholder for "Icon"
        
        // Let's draw a circle instead
        JLabel icon = new JLabel("•");
        icon.setForeground(iconColor);
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        
        card.add(text, BorderLayout.CENTER);
        card.add(icon, BorderLayout.NORTH);

        return card;
    }

    private JComponent buildSectionPlaceholder(String title) {
        RoundedPanel panel = new RoundedPanel(20, UITheme.CARD_BG);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel t = new JLabel(title);
        t.setForeground(UITheme.TEXT_WHITE);
        t.setFont(UITheme.FONT_TITLE.deriveFont(18f));
        panel.add(t, BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setOpaque(false);
        area.setForeground(UITheme.TEXT_MUTED);
        area.setFont(UITheme.FONT_REGULAR);
        area.setText("\nNo recent activity to display.\nSystem is running smoothly.");
        
        panel.add(area, BorderLayout.CENTER);
        return panel;
    }
}

