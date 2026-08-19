package gui.pages;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import gui.components.RoundedButton;
import gui.components.RoundedPanel;
import gui.components.DarkDialog;
import gui.theme.UITheme;
import util.UserManager;
import util.ValidationUtil;
import util.LogManager;
import users.User;

public class ManageUsersPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private UserManager userManager;
    private int hoveredRow = -1;

    private RoundedButton btnEdit, btnDel;

    private String currentSortField = "name";
    private boolean sortAscending = true;
    private List<User> allUsers = new ArrayList<>();

    public ManageUsersPanel() {
        userManager = new UserManager();
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, UITheme.SPACE_XXS, UITheme.SPACE_SM, UITheme.SPACE_XXS));

        JLabel title = new JLabel("User Management");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_WHITE);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACE_SM, 0));
        rightPanel.setOpaque(false);

        JButton filterBtn = createFilterButton();
        rightPanel.add(filterBtn);

        RoundedButton addBtn = new RoundedButton("+ New User", UITheme.PRIMARY, null, UITheme.RADIUS_LG);
        addBtn.setPreferredSize(new Dimension(130, 38));
        addBtn.addActionListener(e -> openUserDialog(null));
        rightPanel.add(addBtn);

        header.add(title, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        add(buildTableSection(), BorderLayout.CENTER);

        add(buildBottomBar(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JButton createFilterButton() {
        JButton filterBtn = new JButton("⋮") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2.setColor(UITheme.HOVER_OVERLAY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS_SM, UITheme.RADIUS_SM);
                }
                super.paintComponent(g);
            }
        };
        filterBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        filterBtn.setForeground(UITheme.TEXT_WHITE);
        filterBtn.setContentAreaFilled(false);
        filterBtn.setBorderPainted(false);
        filterBtn.setFocusPainted(false);
        filterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterBtn.setPreferredSize(new Dimension(38, 38));
        filterBtn.setToolTipText("Sort & Filter");

        filterBtn.addActionListener(e -> showFilterMenu(filterBtn));

        return filterBtn;
    }

    private void showFilterMenu(Component anchor) {
        JPopupMenu popup = new JPopupMenu() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS_SM, UITheme.RADIUS_SM);
            }
        };
        popup.setOpaque(false);
        popup.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1));

        JLabel headerLabel = new JLabel("  Sort By");
        headerLabel.setFont(UITheme.FONT_BOLD_SM);
        headerLabel.setForeground(UITheme.TEXT_MUTED);
        headerLabel.setBorder(new EmptyBorder(UITheme.SPACE_XS, UITheme.SPACE_SM, UITheme.SPACE_XS, UITheme.SPACE_SM));
        popup.add(headerLabel);
        popup.addSeparator();

        popup.add(createSortMenuItem("Name", "name"));
        popup.add(createSortMenuItem("Role", "role"));
        popup.add(createSortMenuItem("Join Date", "joinDate"));
        popup.add(createSortMenuItem("Status", "status"));

        popup.addSeparator();

        JLabel orderLabel = new JLabel("  Order");
        orderLabel.setFont(UITheme.FONT_BOLD_SM);
        orderLabel.setForeground(UITheme.TEXT_MUTED);
        orderLabel.setBorder(new EmptyBorder(UITheme.SPACE_XS, UITheme.SPACE_SM, UITheme.SPACE_XS, UITheme.SPACE_SM));
        popup.add(orderLabel);
        popup.addSeparator();

        JMenuItem ascItem = createOrderMenuItem("Ascending ↑", true);
        JMenuItem descItem = createOrderMenuItem("Descending ↓", false);
        popup.add(ascItem);
        popup.add(descItem);

        popup.show(anchor, 0, anchor.getHeight());
    }

    private JMenuItem createSortMenuItem(String label, String field) {
        JMenuItem item = new JMenuItem(label);
        item.setBackground(UITheme.BG_SURFACE);
        item.setForeground(field.equals(currentSortField) ? UITheme.PRIMARY : UITheme.TEXT_WHITE);
        item.setFont(field.equals(currentSortField) ? UITheme.FONT_BOLD : UITheme.FONT_REGULAR);
        item.setBorder(new EmptyBorder(UITheme.SPACE_XS, UITheme.SPACE_MD, UITheme.SPACE_XS, UITheme.SPACE_MD));

        item.addActionListener(e -> {
            currentSortField = field;
            applySorting();
        });

        return item;
    }

    private JMenuItem createOrderMenuItem(String label, boolean ascending) {
        JMenuItem item = new JMenuItem(label);
        item.setBackground(UITheme.BG_SURFACE);
        item.setForeground(sortAscending == ascending ? UITheme.PRIMARY : UITheme.TEXT_WHITE);
        item.setFont(sortAscending == ascending ? UITheme.FONT_BOLD : UITheme.FONT_REGULAR);
        item.setBorder(new EmptyBorder(UITheme.SPACE_XS, UITheme.SPACE_MD, UITheme.SPACE_XS, UITheme.SPACE_MD));

        item.addActionListener(e -> {
            sortAscending = ascending;
            applySorting();
        });

        return item;
    }

    private void applySorting() {
        List<User> sorted = new ArrayList<>(allUsers);

        Comparator<User> comparator;
        switch (currentSortField) {
            case "name":
                comparator = Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "role":
                comparator = Comparator.comparing(User::getRole, String.CASE_INSENSITIVE_ORDER);
                break;
            case "joinDate":
                comparator = Comparator.comparing(User::getJoinDate);
                break;
            case "status":
                comparator = Comparator.comparing(User::getStatus, String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                comparator = Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER);
                break;
        }

        if (!sortAscending) {
            comparator = comparator.reversed();
        }

        sorted.sort(comparator);

        model.setRowCount(0);
        for (User u : sorted) {
            model.addRow(new Object[] { u, u.getRole(), u.getStatus(), u.getJoinDate() });
        }
    }

    private JComponent buildTableSection() {
        RoundedPanel wrapper = new RoundedPanel(20, UITheme.CARD_BG);
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(1, 1, 1, 1));

        // Custom Floating Header
        JPanel floatHeader = new JPanel(new GridLayout(1, 4));
        floatHeader.setOpaque(false);
        floatHeader.setBorder(new EmptyBorder(15, 20, 15, 20));
        floatHeader.setBackground(new Color(0, 0, 0, 0));

        addCol(floatHeader, "USER PROFILE");
        addCol(floatHeader, "ROLE");
        addCol(floatHeader, "STATUS");
        addCol(floatHeader, "JOINED DATE");

        wrapper.add(floatHeader, BorderLayout.NORTH);

        // Actual Table
        String[] cols = { "User Info", "Role", "Status", "Joined" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(model) {
            @Override
            public void paintComponent(Graphics g) {
                // Must create a transparent background effect for rows
                super.paintComponent(g);
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JComponent)
                    ((JComponent) c).setOpaque(false);
                return c;
            }
        };

        // Custom painting via the UI Delegate to get full row selection/hover effects
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        // We will override PaintComponent properly at the ROW level by using a layout
        // trick or
        // simply drawing the row background in paintComponent of table BEFORE cells.

        table = new JTable(model) {
            @Override
            protected void paintComponent(Graphics g) {
                // Paint row backgrounds manually for that "Card" look
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int rowCount = getRowCount();
                int rowHeight = getRowHeight();
                int width = getWidth();

                for (int i = 0; i < rowCount; i++) {
                    int y = i * rowHeight;
                    boolean isSel = isRowSelected(i);
                    boolean isHov = (i == hoveredRow);

                    if (isSel) {
                        g2.setColor(UITheme.SELECTION_HIGHLIGHT); // Active Selection
                        g2.fillRect(0, y, width, rowHeight);
                        // Border
                        g2.setColor(new Color(100, 180, 255));
                        g2.drawRect(0, y, width - 1, rowHeight - 1);
                    } else if (isHov) {
                        g2.setColor(new Color(60, 60, 75, 150)); // Hover
                        g2.fillRect(0, y, width, rowHeight);
                        // Soft inner light
                        GradientPaint gp = new GradientPaint(0, y, new Color(255, 255, 255, 10), 0, y + rowHeight,
                                new Color(0, 0, 0, 0));
                        g2.setPaint(gp);
                        g2.fillRect(0, y, width, rowHeight);
                    } else {
                        // Default Stripe
                        if (i % 2 == 0) {
                            g2.setColor(new Color(25, 25, 30, 40));
                            g2.fillRect(0, y, width, rowHeight);
                        }
                    }
                }

                super.paintComponent(g); // Paint Cells on top
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JComponent)
                    ((JComponent) c).setOpaque(false);
                return c;
            }
        };

        // Remove Default Header
        table.setTableHeader(null);
        table.setOpaque(false);
        table.setFillsViewportHeight(true);

        // Selection Updates
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtons();
                table.repaint(); // Repaint to show selection border
            }
        });

        // Hover Listener
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
            }
        };
        table.addMouseMotionListener(ma);
        table.addMouseListener(ma);

        // Styling
        table.setRowHeight(72);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBorder(null);
        table.setBackground(new Color(0, 0, 0, 0));
        table.setForeground(UITheme.TEXT_WHITE);
        table.setSelectionForeground(UITheme.TEXT_WHITE);
        table.setFont(UITheme.FONT_REGULAR);

        // Renderers - Must be transparent
        table.getColumnModel().getColumn(0).setCellRenderer(new UserInfoRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new SimpleTextRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new SimpleTextRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(0, 0, 0, 0));
        scroll.setOpaque(false);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        scroll.getViewport().setOpaque(false);

        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void addCol(JPanel p, String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_TABLE_HEADER);
        l.setForeground(new Color(100, 100, 120)); // Subtle header
        p.add(l);
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACE_MD, UITheme.SPACE_MD));
        bar.setBackground(UITheme.BAR_BG);
        bar.setBorder(new EmptyBorder(0, UITheme.SPACE_LG, 0, UITheme.SPACE_LG));
        bar.setPreferredSize(new Dimension(0, 70));

        btnEdit = new RoundedButton("Edit Details", UITheme.BORDER_DARK, null, UITheme.RADIUS_LG);
        btnEdit.setPreferredSize(new Dimension(120, 36));
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r != -1)
                openUserDialog((User) model.getValueAt(r, 0));
        });

        btnDel = new RoundedButton("Remove User", UITheme.STATUS_ERROR, null, UITheme.RADIUS_LG);
        btnDel.setPreferredSize(new Dimension(120, 36));
        btnDel.setEnabled(false);
        btnDel.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r != -1) {
                User u = (User) model.getValueAt(r, 0);
                int c = JOptionPane.showConfirmDialog(this, "Delete " + u.getId() + "?", "Confirm",
                        JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    userManager.deleteUser(u.getId());
                    LogManager.logUserDeleted(u.getId(), u.getName());
                    refreshTable();
                }
            }
        });

        RoundedButton btnRefresh = new RoundedButton("Refresh", UITheme.BORDER_DARK, null, UITheme.RADIUS_LG);
        btnRefresh.setPreferredSize(new Dimension(100, 36));
        btnRefresh.addActionListener(e -> refreshTable());

        bar.add(btnRefresh);
        bar.add(btnEdit);
        bar.add(btnDel);
        return bar;
    }

    private void updateButtons() {
        boolean sel = table.getSelectedRow() != -1;
        btnEdit.setEnabled(sel);
        btnDel.setEnabled(sel);

        // Visual feedback for disabled buttons could be improved here, but Swing
        // handles basics
        btnEdit.setForeground(sel ? Color.WHITE : UITheme.TEXT_MUTED);
        btnDel.setForeground(sel ? Color.WHITE : UITheme.TEXT_MUTED);
    }

    // --- Renderers (Same as before but tweaked) ---
    class UserInfoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
            p.setOpaque(false);
            User u = (User) value;
            if (u == null)
                return p;

            // Avatar
            JLabel avatar = new JLabel(u.getName().substring(0, 1).toUpperCase(), SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    GradientPaint gp = new GradientPaint(0, 0, UITheme.GRADIENT_PURPLE[0], getWidth(), getHeight(),
                            UITheme.GRADIENT_PURPLE[1]);
                    g2.setPaint(gp);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    super.paintComponent(g);
                }
            };
            avatar.setPreferredSize(new Dimension(42, 42));
            avatar.setForeground(Color.WHITE);
            avatar.setFont(UITheme.FONT_TITLE.deriveFont(18f));

            JPanel text = new JPanel(new GridLayout(2, 1));
            text.setOpaque(false);
            JLabel name = new JLabel(u.getName());
            name.setFont(UITheme.FONT_BOLD.deriveFont(14f));
            name.setForeground(UITheme.TEXT_WHITE);
            JLabel email = new JLabel(u.getId() + "@apu.edu.my");
            email.setFont(UITheme.FONT_REGULAR.deriveFont(11f));
            email.setForeground(UITheme.TEXT_MUTED);

            text.add(name);
            text.add(email);
            p.add(avatar);
            p.add(text);
            return p;
        }
    }

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 22));
            p.setOpaque(false);
            String status = (String) value;
            Color bg = "Active".equals(status) ? UITheme.STATUS_ACTIVE_BG : UITheme.STATUS_DELETED_BG;
            Color fg = "Active".equals(status) ? UITheme.STATUS_ACTIVE : UITheme.STATUS_DELETED;

            JLabel pill = new JLabel(status, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    g2.setColor(new Color(getForeground().getRed(), getForeground().getGreen(),
                            getForeground().getBlue(), 100));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                    super.paintComponent(g);
                }
            };
            pill.setOpaque(false);
            pill.setBackground(bg);
            pill.setForeground(fg);
            pill.setPreferredSize(new Dimension(70, 24));
            pill.setFont(UITheme.FONT_BOLD.deriveFont(10f));
            p.add(pill);
            return p;
        }
    }

    class SimpleTextRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setForeground(UITheme.TEXT_MUTED);
            c.setFont(UITheme.FONT_REGULAR);
            return c;
        }
    }

    private void refreshTable() {
        model.setRowCount(0);
        allUsers = userManager.getAllUsers();
        for (User u : allUsers) {
            model.addRow(new Object[] { u, u.getRole(), u.getStatus(), u.getJoinDateFormatted() });
        }
    }

    private void openUserDialog(User existing) {
        DarkDialog d = new DarkDialog(SwingUtilities.getWindowAncestor(this),
                existing == null ? "Add User" : "Edit User", 420, 500);
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        d.getContent().setLayout(new BorderLayout()); // Use wrapper
        d.getContent().add(p, BorderLayout.CENTER);

        JTextField idField = new JTextField(existing != null ? existing.getId() : "");
        JTextField nameField = new JTextField(existing != null ? existing.getName() : "");
        JTextField passField = new JTextField(existing != null ? existing.getPassword() : "");
        String[] roles = { "ADMIN", "LECTURER", "STUDENT", "LEADER" };
        JComboBox<String> roleBox = new JComboBox<>(roles);
        if (existing != null)
            roleBox.setSelectedItem(existing.getRole());

        if (existing != null)
            idField.setEditable(false);
        DarkDialog.styleField(idField);
        DarkDialog.styleField(nameField);
        DarkDialog.styleField(passField);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 5, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        addLabel(p, "ID (TP Number)", gbc);
        gbc.gridy++;
        p.add(idField, gbc);
        gbc.gridy++;
        addLabel(p, "Full Name", gbc);
        gbc.gridy++;
        p.add(nameField, gbc);
        gbc.gridy++;
        addLabel(p, "Role", gbc);
        gbc.gridy++;
        p.add(roleBox, gbc);
        gbc.gridy++;
        addLabel(p, "Password", gbc);
        gbc.gridy++;
        p.add(passField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(30, 20, 20, 20);
        RoundedButton save = new RoundedButton("Save Changes", UITheme.PRIMARY, UITheme.ACCENT_GLOW, 20);
        save.setPreferredSize(new Dimension(100, 40));
        save.addActionListener(e -> {
            String i = idField.getText().trim();
            String n = nameField.getText().trim();
            String r = (String) roleBox.getSelectedItem();
            String px = passField.getText().trim();
            if (!ValidationUtil.isValidId(i) || n.isEmpty() || px.isEmpty())
                return;
            boolean isUpdate = existing != null;
            userManager.saveUser(User.create(i, n, px, r), isUpdate);
            if (isUpdate) {
                LogManager.logUserUpdated(i, n);
            } else {
                LogManager.logUserCreated(i, n, r);
            }
            d.dispose();
            refreshTable();
        });
        p.add(save, gbc);
        d.setVisible(true);
    }

    private void addLabel(JPanel p, String text, GridBagConstraints gbc) {
        JLabel l = new JLabel(text);
        DarkDialog.styleLabel(l);
        p.add(l, gbc);
    }
}
