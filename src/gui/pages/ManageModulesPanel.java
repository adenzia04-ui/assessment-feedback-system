package gui.pages;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import gui.components.RoundedButton;
import gui.components.DarkDialog;
import gui.theme.UITheme;
import util.DataManager;
import util.LogManager;
import model.Module;

public class ManageModulesPanel extends JPanel {

    private JPanel gridPanel;
    private DataManager dataManager;
    private Module selectedModule = null;
    private JPanel modulesContainer;

    // Bottom Bar
    private RoundedButton btnEdit, btnFiles, btnDel;

    public ManageModulesPanel() {
        dataManager = new DataManager();
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, UITheme.SPACE_XXS, UITheme.SPACE_LG, UITheme.SPACE_XXS));

        JLabel title = new JLabel("Module Management");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_WHITE);

        RoundedButton addBtn = new RoundedButton("+ New Module", UITheme.PRIMARY, null, UITheme.RADIUS_LG);
        addBtn.setPreferredSize(new Dimension(140, 38));
        addBtn.addActionListener(e -> openEditDialog(null));

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Grid Content
        gridPanel = new JPanel(new GridLayout(0, 3, UITheme.SPACE_LG, UITheme.SPACE_LG));
        gridPanel.setOpaque(false);

        modulesContainer = new JPanel(new BorderLayout());
        modulesContainer.setOpaque(false);
        modulesContainer.add(gridPanel, BorderLayout.NORTH); // Align top

        JScrollPane scroll = new JScrollPane(modulesContainer);
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

        btnFiles = new RoundedButton("Manage Files", UITheme.BORDER_DARK, null, 20);
        btnFiles.setEnabled(false);
        btnFiles.addActionListener(e -> {
            if (selectedModule != null)
                openFilesDialog(selectedModule);
        });

        btnEdit = new RoundedButton("Edit Module", UITheme.BORDER_DARK, null, 20);
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(e -> {
            if (selectedModule != null)
                openEditDialog(selectedModule);
        });

        btnDel = new RoundedButton("Delete", new Color(180, 50, 50), null, 20);
        btnDel.setEnabled(false);
        btnDel.addActionListener(e -> {
            if (selectedModule != null) {
                int c = JOptionPane.showConfirmDialog(this, "Delete " + selectedModule.getModuleId() + "?", "Confirm",
                        JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    dataManager.deleteModule(selectedModule.getModuleId());
                    LogManager.logModuleDeleted(selectedModule.getModuleId());
                    selectedModule = null;
                    updateButtons();
                    refresh();
                }
            }
        });

        bar.add(btnFiles);
        bar.add(btnEdit);
        bar.add(btnDel);
        return bar;
    }

    private void updateButtons() {
        boolean sel = (selectedModule != null);
        btnFiles.setEnabled(sel);
        btnEdit.setEnabled(sel);
        btnDel.setEnabled(sel);

        Color active = Color.WHITE;
        Color mute = UITheme.TEXT_MUTED;
        btnFiles.setForeground(sel ? active : mute);
        btnEdit.setForeground(sel ? active : mute);
        btnDel.setForeground(sel ? active : mute);
    }

    private void refresh() {
        gridPanel.removeAll();
        java.util.List<Module> modules = dataManager.getAllModules();
        if (modules.isEmpty()) {
            modules.add(new Module("CS101", "Intro to Computing", "TP000001"));
        }

        Color[] pallatte = { UITheme.PRIMARY, new Color(255, 80, 80), new Color(40, 200, 80) };
        int i = 0;
        for (Module m : modules) {
            gridPanel.add(createModuleCard(m, pallatte[i % pallatte.length]));
            i++;
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createModuleCard(Module m, Color accent) {
        boolean isSelected = (selectedModule != null && selectedModule.getModuleId().equals(m.getModuleId()));
        Color bg = isSelected ? new Color(60, 60, 80, 200) : new Color(30, 30, 40, 150);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card BG
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // Subtle Gradient Overlay
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 255, 255, 10), 0, getHeight(),
                        new Color(0, 0, 0, 20));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // Border - Glow if selected
                if (isSelected) {
                    g2.setColor(new Color(100, 180, 255));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 25, 25);
                } else {
                    g2.setColor(UITheme.BORDER_DARK);
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 25, 25);
                }
            }
        };

        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(240, 300)); // Tall card
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        card.setOpaque(false);

        // --- Icon & Top ---
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel icon = new JLabel(m.getModuleName().substring(0, 1).toUpperCase(), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        icon.setPreferredSize(new Dimension(50, 50));
        icon.setForeground(Color.WHITE);
        icon.setFont(UITheme.FONT_TITLE.deriveFont(22f));
        top.add(icon, BorderLayout.CENTER);

        // --- Body ---
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel code = new JLabel(m.getModuleId());
        code.setFont(UITheme.FONT_BOLD.deriveFont(14f));
        code.setForeground(accent);
        code.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("<html><center>" + m.getModuleName() + "</center></html>");
        title.setFont(UITheme.FONT_TITLE.deriveFont(18f));
        title.setForeground(UITheme.TEXT_WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lead = new JLabel("Leader: " + m.getLeaderId());
        lead.setFont(UITheme.FONT_REGULAR);
        lead.setForeground(UITheme.TEXT_MUTED);
        lead.setAlignmentX(Component.CENTER_ALIGNMENT);

        body.add(code);
        body.add(Box.createVerticalStrut(10));
        body.add(title);
        body.add(Box.createVerticalStrut(15));
        body.add(lead);

        // --- Footer (Stats) ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        int fCount = dataManager.getModuleFiles(m.getModuleId()).size();

        JLabel files = new JLabel(fCount + " Files Attached");
        files.setForeground(new Color(255, 255, 255, 150));
        files.setFont(UITheme.FONT_REGULAR.deriveFont(11f));
        files.setIcon(new Icon() { // Simple dot icon
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(fCount > 0 ? accent : Color.GRAY);
                g.fillOval(x, y + 3, 6, 6);
            }

            public int getIconWidth() {
                return 10;
            }

            public int getIconHeight() {
                return 10;
            }
        });
        footer.add(files);

        card.add(top, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!isSelected)
                    card.setBackground(new Color(50, 50, 60, 200));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.repaint();
            }

            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
                card.repaint();
            }

            public void mouseClicked(MouseEvent e) {
                selectedModule = m;
                updateButtons();
                refresh();
            }
        });

        return card;
    }

    private void openEditDialog(Module existing) {
        DarkDialog d = new DarkDialog(SwingUtilities.getWindowAncestor(this),
                existing == null ? "New Module" : "Edit Module", 420, 450);
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        d.getContent().setLayout(new BorderLayout());
        d.getContent().add(p);

        JTextField c = new JTextField(existing != null ? existing.getModuleId() : "");
        JTextField n = new JTextField(existing != null ? existing.getModuleName() : "");
        JTextField l = new JTextField(existing != null ? existing.getLeaderId() : "");
        if (existing != null)
            c.setEditable(false);

        DarkDialog.styleField(c);
        DarkDialog.styleField(n);
        DarkDialog.styleField(l);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 20, 5, 20);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1;

        JLabel l1 = new JLabel("Module Code");
        DarkDialog.styleLabel(l1);
        p.add(l1, g);
        g.gridy++;
        p.add(c, g);
        g.gridy++;
        JLabel l2 = new JLabel("Module Name");
        DarkDialog.styleLabel(l2);
        p.add(l2, g);
        g.gridy++;
        p.add(n, g);
        g.gridy++;
        JLabel l3 = new JLabel("Leader ID");
        DarkDialog.styleLabel(l3);
        p.add(l3, g);
        g.gridy++;
        p.add(l, g);

        g.gridy++;
        g.insets = new Insets(30, 20, 20, 20);
        RoundedButton save = new RoundedButton("Save Changes", UITheme.PRIMARY, null, 20);
        save.setPreferredSize(new Dimension(0, 40));
        save.addActionListener(e -> {
            if (!c.getText().isEmpty()) {
                boolean isUpdate = existing != null;
                dataManager.saveModule(new Module(c.getText(), n.getText(), l.getText()), isUpdate);
                if (isUpdate) {
                    LogManager.logModuleUpdated(c.getText(), n.getText());
                } else {
                    LogManager.logModuleCreated(c.getText(), n.getText());
                }
                d.dispose();
                refresh();
            }
        });
        p.add(save, g);

        d.setVisible(true);
    }

    private void openFilesDialog(Module m) {
        DarkDialog d = new DarkDialog(SwingUtilities.getWindowAncestor(this), "Manage Files: " + m.getModuleId(), 500,
                450);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        d.getContent().setLayout(new BorderLayout());
        d.getContent().add(root);

        // Table
        DefaultTableModel fm = new DefaultTableModel(new String[] { "File Name", "Type" }, 0);
        JTable ft = new JTable(fm);
        ft.setOpaque(false);
        ft.setBackground(new Color(0, 0, 0, 0));
        ft.setForeground(Color.WHITE);
        ft.setShowGrid(false);
        ft.setRowHeight(45);
        ft.setIntercellSpacing(new Dimension(0, 0));

        // Transparent Renderer
        ft.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r,
                    int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
                comp.setBackground(r % 2 == 0 ? new Color(255, 255, 255, 10) : new Color(0, 0, 0, 0));
                comp.setForeground(Color.WHITE);
                if (comp instanceof JComponent)
                    ((JComponent) comp).setOpaque(true);
                if (isSel)
                    comp.setBackground(UITheme.SELECTION_HIGHLIGHT);
                return comp;
            }
        });

        // Header
        ft.getTableHeader().setBackground(new Color(0, 0, 0, 0));
        ft.getTableHeader().setForeground(UITheme.TEXT_MUTED);
        ft.getTableHeader().setFont(UITheme.FONT_BOLD);
        ft.getTableHeader().setOpaque(false);
        ((javax.swing.table.DefaultTableCellRenderer) ft.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(JLabel.LEFT);

        // Data
        for (String[] f : dataManager.getModuleFiles(m.getModuleId()))
            fm.addRow(new Object[] { f[0], "Text Document" });

        JScrollPane sp = new JScrollPane(ft);
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DARK));

        root.add(sp, BorderLayout.CENTER);

        // Action
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(15, 0, 0, 0));

        RoundedButton addF = new RoundedButton("+ Attach File", UITheme.PRIMARY, null, 15);
        addF.setPreferredSize(new Dimension(120, 35));
        addF.addActionListener(ev -> {
            String fname = JOptionPane.showInputDialog("Filename:");
            if (fname != null && !fname.trim().isEmpty()) {
                dataManager.saveModuleFile(m.getModuleId(), fname, "Content...");
                fm.addRow(new Object[] { fname, "Text Document" });
                refresh();
            }
        });
        bottom.add(addF);

        root.add(bottom, BorderLayout.SOUTH);
        d.setVisible(true);
    }
}
