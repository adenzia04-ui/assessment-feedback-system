package gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

import gui.theme.UITheme;

public class DarkDialog extends JDialog {

    private Point initialClick;
    private JPanel contentPanel;
    private ParticleBackground bg;

    public DarkDialog(Window owner, String title, int width, int height) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(width, height);
        setLocationRelativeTo(owner);

        bg = new ParticleBackground() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Shape round = new java.awt.geom.RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), UITheme.RADIUS_XL, UITheme.RADIUS_XL);
                g2.setClip(round);

                super.paintComponent(g);

                g2.setClip(null);

                g2.setColor(UITheme.BORDER_DARK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UITheme.RADIUS_XL, UITheme.RADIUS_XL);
            }
        };
        bg.setLayout(new BorderLayout());

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setBorder(new EmptyBorder(UITheme.SPACE_SM, UITheme.SPACE_LG, UITheme.SPACE_SM, UITheme.SPACE_LG));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_HEADING);
        lblTitle.setForeground(UITheme.TEXT_WHITE);

        JButton closeBtn = new JButton("✕") {
            boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered) {
                    g2.setColor(UITheme.STATUS_ERROR_BG);
                    g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                }
                super.paintComponent(g);
            }
        };
        closeBtn.setFont(UITheme.FONT_BOLD);
        closeBtn.setForeground(UITheme.TEXT_MUTED);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(28, 28));
        closeBtn.addActionListener(e -> dispose());
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                closeBtn.setForeground(UITheme.STATUS_ERROR);
            }

            public void mouseExited(MouseEvent e) {
                closeBtn.setForeground(UITheme.TEXT_MUTED);
            }
        });

        titleBar.add(lblTitle, BorderLayout.WEST);
        titleBar.add(closeBtn, BorderLayout.EAST);

        titleBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });
        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = getLocation().x + e.getX() - initialClick.x;
                int y = getLocation().y + e.getY() - initialClick.y;
                setLocation(x, y);
            }
        });

        bg.add(titleBar, BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG));

        bg.add(contentPanel, BorderLayout.CENTER);

        setContentPane(bg);
    }

    public JPanel getContent() {
        return contentPanel;
    }

    public static void styleField(JTextField f) {
        f.setBackground(UITheme.BG_SURFACE);
        f.setForeground(UITheme.TEXT_WHITE);
        f.setCaretColor(UITheme.TEXT_WHITE);
        f.setSelectionColor(UITheme.SELECTION_HIGHLIGHT);
        f.setSelectedTextColor(UITheme.TEXT_WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK),
                new EmptyBorder(UITheme.SPACE_XS, UITheme.SPACE_SM, UITheme.SPACE_XS, UITheme.SPACE_SM)));
        f.setFont(UITheme.FONT_BODY);
    }

    public static void styleComboBox(JComboBox<?> cb) {
        cb.setBackground(UITheme.BG_SURFACE);
        cb.setForeground(UITheme.TEXT_WHITE);
        cb.setFont(UITheme.FONT_BODY);
        cb.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DARK));

        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? UITheme.SELECTION_HIGHLIGHT : UITheme.BG_SURFACE);
                setForeground(UITheme.TEXT_WHITE);
                setBorder(new EmptyBorder(UITheme.SPACE_XS, UITheme.SPACE_SM, UITheme.SPACE_XS, UITheme.SPACE_SM));
                return this;
            }
        });
    }

    public static void styleLabel(JLabel l) {
        l.setForeground(UITheme.TEXT_SECONDARY);
        l.setFont(UITheme.FONT_BOLD_SM);
    }

    public static JLabel createSectionHeader(String text) {
        JLabel header = new JLabel(text);
        header.setFont(UITheme.FONT_SUBHEADING);
        header.setForeground(UITheme.TEXT_WHITE);
        header.setBorder(new EmptyBorder(UITheme.SPACE_MD, 0, UITheme.SPACE_SM, 0));
        return header;
    }
}
