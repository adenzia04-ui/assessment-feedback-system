package gui.pages;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

import gui.components.RoundedButton;

import gui.components.DarkDialog;
import gui.theme.UITheme;
import util.DataManager;
import util.LogManager;
import model.ClassGroup;

public class ManageClassGroupsPanel extends JPanel {

    private JPanel gridPanel;
    private DataManager dataManager;
    private ClassGroup selectedGroup = null;
    private JPanel container;

    // Bottom Bar
    private RoundedButton btnEdit, btnDel;

    public ManageClassGroupsPanel() {
        dataManager = new DataManager();
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 5, 20, 5));

        JLabel title = new JLabel("Class Groups");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_WHITE);

        RoundedButton addBtn = new RoundedButton("+ New Group", UITheme.PRIMARY, UITheme.ACCENT_GLOW, 20);
        addBtn.setPreferredSize(new Dimension(130, 38));
        addBtn.addActionListener(e -> openDialog(null));

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Grid
        gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        gridPanel.setOpaque(false);

        container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(gridPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(container);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Bottom Bar
        add(buildBottomBar(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bar.setBackground(UITheme.BAR_BG);
        bar.setPreferredSize(new Dimension(0, 70));

        btnEdit = new RoundedButton("Edit Group", UITheme.BORDER_DARK, null, 20);
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(e -> {
            if (selectedGroup != null)
                openDialog(selectedGroup);
        });

        btnDel = new RoundedButton("Delete", new Color(180, 50, 50), null, 20);
        btnDel.setEnabled(false);
        btnDel.addActionListener(e -> {
            if (selectedGroup != null) {
                int c = JOptionPane.showConfirmDialog(this, "Delete " + selectedGroup.getGroupId() + "?", "Confirm",
                        JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    dataManager.deleteGroup(selectedGroup.getGroupId());
                    LogManager.logGroupDeleted(selectedGroup.getGroupId());
                    selectedGroup = null;
                    updateButtons();
                    refresh();
                }
            }
        });

        bar.add(btnEdit);
        bar.add(btnDel);
        return bar;
    }

    private void updateButtons() {
        boolean sel = (selectedGroup != null);
        btnEdit.setEnabled(sel);
        btnDel.setEnabled(sel);
        Color active = Color.WHITE;
        Color mute = UITheme.TEXT_MUTED;
        btnEdit.setForeground(sel ? active : mute);
        btnDel.setForeground(sel ? active : mute);
    }

    private void refresh() {
        gridPanel.removeAll();
        java.util.List<ClassGroup> groups = dataManager.getAllGroups();
        if (groups.isEmpty()) {
            groups.add(new ClassGroup("C01", "CS101"));
        }

        Color[][] gradients = { UITheme.GRADIENT_BLUE, UITheme.GRADIENT_PURPLE, UITheme.GRADIENT_ORANGE,
                UITheme.GRADIENT_TEAL };
        int i = 0;
        for (ClassGroup g : groups) {
            gridPanel.add(createBlockCard(g, gradients[i % gradients.length]));
            i++;
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createBlockCard(ClassGroup g, Color[] gradient) {
        boolean isSelected = (selectedGroup != null && selectedGroup.getGroupId().equals(g.getGroupId()));

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, gradient[0], getWidth(), getHeight(), gradient[1]);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Geometric Corner Accent (Top Right)
                g2.setColor(new Color(255, 255, 255, 40));
                Path2D p = new Path2D.Double();
                p.moveTo(getWidth() - 60, 0);
                p.lineTo(getWidth(), 0);
                p.lineTo(getWidth(), 60);
                p.curveTo(getWidth() - 30, 60, getWidth() - 60, 30, getWidth() - 60, 0);
                g2.fill(p);

                // Bottom Gradient Overlay for depth
                GradientPaint glow = new GradientPaint(0, getHeight(), new Color(0, 0, 0, 50), 0, getHeight() - 80,
                        new Color(0, 0, 0, 0));
                g2.setPaint(glow);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                if (isSelected) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                } else {
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                }
            }
        };

        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(220, 180)); // Taller/Larger
        card.setBorder(new EmptyBorder(20, 20, 20, 20)); // More padding
        card.setOpaque(false);

        // Status Badge
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.setOpaque(false);
        JLabel badge = new JLabel("ACTIVE");
        badge.setForeground(new Color(255, 255, 255, 200));
        badge.setFont(UITheme.FONT_BOLD.deriveFont(10f));
        top.add(badge);

        // Content - Centered
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JLabel id = new JLabel(g.getGroupId());
        id.setForeground(Color.WHITE);
        id.setFont(UITheme.FONT_TITLE.deriveFont(Font.BOLD, 28f));
        id.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel mod = new JLabel(g.getModuleId());
        mod.setForeground(new Color(255, 255, 255, 200));
        mod.setFont(UITheme.FONT_SUBTITLE);
        mod.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(Box.createVerticalStrut(10));
        body.add(id);
        body.add(Box.createVerticalStrut(5));
        body.add(mod);

        card.add(top, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedGroup = g;
                updateButtons();
                refresh();
            }
        });

        return card;
    }

    private void openDialog(ClassGroup existing) {
        DarkDialog d = new DarkDialog(SwingUtilities.getWindowAncestor(this),
                existing == null ? "New Group" : "Edit Group", 350, 300);
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        d.getContent().setLayout(new BorderLayout());
        d.getContent().add(p);

        JTextField gid = new JTextField(existing != null ? existing.getGroupId() : "");
        JTextField mid = new JTextField(existing != null ? existing.getModuleId() : "");
        if (existing != null)
            gid.setEditable(false);

        DarkDialog.styleField(gid);
        DarkDialog.styleField(mid);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 20, 5, 20);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1;

        JLabel l1 = new JLabel("Group ID");
        DarkDialog.styleLabel(l1);
        p.add(l1, g);
        g.gridy++;
        p.add(gid, g);
        g.gridy++;
        JLabel l2 = new JLabel("Module ID");
        DarkDialog.styleLabel(l2);
        p.add(l2, g);
        g.gridy++;
        p.add(mid, g);

        g.gridy++;
        g.insets = new Insets(20, 20, 10, 20);
        RoundedButton save = new RoundedButton("Save", UITheme.PRIMARY, null, 15);
        save.addActionListener(e -> {
            if (!gid.getText().isEmpty()) {
                boolean isNew = existing == null;
                dataManager.saveGroup(new ClassGroup(gid.getText(), mid.getText()));
                if (isNew) {
                    LogManager.logGroupCreated(gid.getText(), mid.getText());
                }
                d.dispose();
                refresh();
            }
        });
        p.add(save, g);

        d.setVisible(true);
    }
}
