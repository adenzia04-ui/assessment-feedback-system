package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gui.components.ParticleBackground;
import gui.components.RoundedButton;
import gui.components.RoundedPanel;
import gui.components.DarkDialog;
import gui.theme.UITheme;
import users.User;
import util.UserManager;
import util.DataManager;
import util.ValidationUtil;
import util.LogManager;
import util.FileManager;
import util.LogManager.LogEntry;
import model.Module;
import model.ClassGroup;
import model.GradingSystem;
import model.GradingSystem.Grade;
import model.LecturerAssignment;

public class AdminFrame extends JFrame {

    private JPanel mainContent;
    private JPanel currentPage;
    private User currentUser;
    private String currentKey = "DASHBOARD";

    private Map<String, JPanel> pages = new HashMap<>();
    private Map<String, RoundedButton> navButtons = new HashMap<>();

    private static final String ASSIGNMENTS_FILE = "assign.txt";

    public AdminFrame(User user) {
        this.currentUser = user;
        setTitle("Admin Panel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        System.setProperty("sun.java2d.opengl", "true");

        ParticleBackground root = new ParticleBackground();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        root.add(buildTopBar(), BorderLayout.NORTH);

        mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(UITheme.SPACE_SM, UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG));

        initializePages();

        currentPage = pages.get("DASHBOARD");
        mainContent.add(currentPage, BorderLayout.CENTER);

        root.add(mainContent, BorderLayout.CENTER);

        updateNavState("DASHBOARD");
    }

    private void initializePages() {
        JPanel dashboardWrapper = new JPanel(new BorderLayout());
        dashboardWrapper.setOpaque(false);
        dashboardWrapper.add(buildProfileHeader(), BorderLayout.NORTH);
        dashboardWrapper.add(new DashboardPanel(), BorderLayout.CENTER);

        pages.put("DASHBOARD", dashboardWrapper);
        pages.put("USERS", new UsersPanel());
        pages.put("MODULES", new ModulesPanel());
        pages.put("GROUPS", new GroupsPanel());
        pages.put("GRADING", new GradingPanel());
        pages.put("ASSIGNMENTS", new LecturerAssignmentsPanel());
        pages.put("REPORTS", new ReportsPanel());
    }

    public void showCard(String key) {
        if (key.equals(currentKey) || !pages.containsKey(key))
            return;

        JPanel nextPage = pages.get(key);

        mainContent.removeAll();
        mainContent.add(nextPage, BorderLayout.CENTER);
        mainContent.revalidate();
        mainContent.repaint();

        currentPage = nextPage;
        currentKey = key;

        updateNavState(key);
    }

    private void updateNavState(String activeKey) {
        for (Map.Entry<String, RoundedButton> entry : navButtons.entrySet()) {
            RoundedButton btn = entry.getValue();
            boolean isActive = entry.getKey().equals(activeKey);

            if (isActive) {
                btn.setBackground(UITheme.NAV_ACTIVE);
                btn.setForeground(UITheme.PRIMARY);

            } else {
                btn.setBackground(UITheme.GLASS_BORDER);
                btn.setForeground(UITheme.TEXT_WHITE);
            }
        }
    }

    private JComponent buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(UITheme.SPACE_MD, UITheme.SPACE_LG, UITheme.SPACE_MD, UITheme.SPACE_LG));

        JLabel brand = new JLabel("Admin Panel");
        brand.setForeground(UITheme.TEXT_WHITE);
        brand.setFont(UITheme.FONT_TITLE_SM);
        top.add(brand, BorderLayout.WEST);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACE_SM, 0));
        navPanel.setOpaque(false);

        navPanel.add(createNavPill("Dashboard", "DASHBOARD"));
        navPanel.add(createNavPill("Users", "USERS"));
        navPanel.add(createNavPill("Modules", "MODULES"));
        navPanel.add(createNavPill("Groups", "GROUPS"));
        navPanel.add(createNavPill("Grading", "GRADING"));
        navPanel.add(createNavPill("Lecturers", "ASSIGNMENTS"));
        navPanel.add(createNavPill("Reports", "REPORTS"));

        RoundedButton logout = new RoundedButton("Logout", UITheme.STATUS_ERROR, null, UITheme.RADIUS_LG);
        logout.setForeground(Color.WHITE);
        logout.setPreferredSize(new Dimension(85, 36));
        logout.setFont(UITheme.FONT_BOLD);
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        navPanel.add(Box.createHorizontalStrut(UITheme.SPACE_MD));
        navPanel.add(logout);

        top.add(navPanel, BorderLayout.EAST);
        return top;
    }

    private RoundedButton createNavPill(String text, String key) {
        RoundedButton btn = new RoundedButton(text, UITheme.GLASS_BORDER, null, UITheme.RADIUS_LG);
        btn.setForeground(UITheme.TEXT_WHITE);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setPreferredSize(new Dimension(100, 36));
        btn.addActionListener(e -> showCard(key));

        navButtons.put(key, btn);
        return btn;
    }

    private List<LecturerAssignment> getAllLecturerAssignments() {
        List<LecturerAssignment> assignments = new ArrayList<>();
        try {
            for (String line : FileManager.readAllLines(ASSIGNMENTS_FILE)) {
                if (!ValidationUtil.hasExactFieldCount(line, 6)) {
                    continue;
                }
                LecturerAssignment assignment = LecturerAssignment.fromCsvRecord(line);
                if (assignment != null) {
                    assignments.add(assignment);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load lecturer assignments: " + e.getMessage());
        }
        return assignments;
    }

    private boolean saveLecturerAssignment(LecturerAssignment assignment) {
        try {
            List<String> lines = FileManager.readAllLines(ASSIGNMENTS_FILE);
            List<String> updated = new ArrayList<>();
            boolean replaced = false;

            for (String line : lines) {
                if (!ValidationUtil.hasExactFieldCount(line, 6)) {
                    updated.add(line);
                    continue;
                }

                LecturerAssignment existing = LecturerAssignment.fromCsvRecord(line);
                if (existing != null && existing.getLecturerId().equalsIgnoreCase(assignment.getLecturerId())) {
                    if (!replaced) {
                        updated.add(assignment.toCsvRecord());
                        replaced = true;
                    }
                } else {
                    updated.add(line);
                }
            }

            if (!replaced) {
                updated.add(assignment.toCsvRecord());
            }

            FileManager.writeAllLines(ASSIGNMENTS_FILE, updated);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save lecturer assignment: " + e.getMessage());
            return false;
        }
    }

    private boolean deleteLecturerAssignment(String lecturerId) {
        try {
            List<String> lines = FileManager.readAllLines(ASSIGNMENTS_FILE);
            List<String> updated = new ArrayList<>();

            for (String line : lines) {
                if (!ValidationUtil.hasExactFieldCount(line, 6)) {
                    updated.add(line);
                    continue;
                }

                LecturerAssignment existing = LecturerAssignment.fromCsvRecord(line);
                if (existing == null || !existing.getLecturerId().equalsIgnoreCase(lecturerId)) {
                    updated.add(line);
                }
            }

            FileManager.writeAllLines(ASSIGNMENTS_FILE, updated);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to delete lecturer assignment: " + e.getMessage());
            return false;
        }
    }

    private JComponent buildProfileHeader() {
        RoundedPanel header = new RoundedPanel(UITheme.RADIUS_LG, UITheme.CARD_BG);
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACE_MD, 0));
        left.setOpaque(false);

        JLabel avatar = new JLabel(currentUser.getName().substring(0, 1).toUpperCase(), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(52, 52));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(UITheme.FONT_TITLE_SM);

        JPanel texts = new JPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setOpaque(false);

        JLabel name = new JLabel("Welcome back, " + currentUser.getName());
        name.setForeground(UITheme.TEXT_WHITE);
        name.setFont(UITheme.FONT_HEADING);

        JLabel role = new JLabel(currentUser.getRole() + " • " + currentUser.getId());
        role.setForeground(UITheme.TEXT_MUTED);
        role.setFont(UITheme.FONT_REGULAR);

        texts.add(name);
        texts.add(Box.createVerticalStrut(UITheme.SPACE_XXS));
        texts.add(role);

        left.add(avatar);
        left.add(texts);

        header.add(left, BorderLayout.WEST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(header, BorderLayout.CENTER);
        wrapper.add(Box.createVerticalStrut(UITheme.SPACE_LG), BorderLayout.SOUTH);

        return wrapper;
    }

    // ==================== INNER CLASS: DashboardPanel ====================
    class DashboardPanel extends JPanel {
        DashboardPanel() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(buildQuickAccess());
            add(Box.createVerticalStrut(20));
            add(buildActivityPlaceholder());
        }

        private JComponent buildQuickAccess() {
            JPanel container = new JPanel(new BorderLayout());
            container.setOpaque(false);
            JLabel title = new JLabel("Quick Access");
            title.setForeground(UITheme.PRIMARY);
            title.setFont(UITheme.FONT_TITLE.deriveFont(20f));
            container.add(title, BorderLayout.NORTH);

            JPanel grid = new JPanel(new GridLayout(1, 4, 20, 0));
            grid.setOpaque(false);
            grid.setBorder(new EmptyBorder(15, 0, 15, 0));

            grid.add(quickCard("Manage Users", "Add/Edit Staff & Students", "USERS", new Color(0, 200, 255)));
            grid.add(quickCard("Modules", "Course Configuration", "MODULES", new Color(200, 100, 255)));
            grid.add(quickCard("Class Groups", "Allocations", "GROUPS", new Color(255, 180, 50)));
            grid.add(quickCard("Reports", "System Analytics", "REPORTS", new Color(100, 255, 150)));

            container.add(grid, BorderLayout.CENTER);
            container.setMaximumSize(new Dimension(2000, 250));
            return container;
        }

        private JComponent quickCard(String heading, String sub, String navKey, Color iconColor) {
            JPanel card = new JPanel() {
                boolean hover = false;
                {
                    addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) {
                            hover = true;
                            repaint();
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        }

                        public void mouseExited(MouseEvent e) {
                            hover = false;
                            repaint();
                            setCursor(Cursor.getDefaultCursor());
                        }

                        public void mouseClicked(MouseEvent e) {
                            showCard(navKey);
                        }
                    });
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(hover ? new Color(60, 60, 75, 200) : UITheme.CARD_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                    g2.setColor(hover ? iconColor : UITheme.GLASS_BORDER);
                    g2.setStroke(new BasicStroke(hover ? 2 : 1));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
                }
            };
            card.setOpaque(false);
            card.setLayout(new BorderLayout());
            card.setBorder(new EmptyBorder(20, 20, 20, 20));

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

            JLabel icon = new JLabel("•");
            icon.setForeground(iconColor);
            icon.setFont(new Font("Segoe UI", Font.PLAIN, 40));
            card.add(text, BorderLayout.CENTER);
            card.add(icon, BorderLayout.NORTH);
            return card;
        }

        private JComponent buildActivityPlaceholder() {
            RoundedPanel panel = new RoundedPanel(20, UITheme.CARD_BG);
            panel.setLayout(new BorderLayout());
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));
            JLabel t = new JLabel("Recent Activity Log");
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

    // ==================== INNER CLASS: ReportsPanel ====================
    class ReportsPanel extends JPanel {
        private JPanel logsContainer;

        ReportsPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);

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

            JScrollPane scrollPane = new JScrollPane(logsContainer);
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

            JLabel detailsLabel = new JLabel("<html><body style='width: 600px'>" + log.details + "</body></html>");
            detailsLabel.setFont(UITheme.FONT_REGULAR);
            detailsLabel.setForeground(UITheme.TEXT_WHITE);

            JLabel adminLabel = new JLabel("By: " + log.adminInfo);
            adminLabel.setFont(UITheme.FONT_SMALL);
            adminLabel.setForeground(UITheme.TEXT_MUTED);

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
                case "LECTURER_ASSIGNED":
                    return "Lecturer Assigned";
                case "LECTURER_UNASSIGNED":
                    return "Lecturer Unassigned";
                case "GRADE_CALCULATED":
                    return "Grade Calculated";
                case "LOGIN":
                    return "User Login";
                case "LOGOUT":
                    return "User Logout";
                default:
                    return action;
            }
        }

        private Color getActionColor(String action) {
            if (action.contains("DELETED") || action.contains("UNASSIGNED"))
                return UITheme.STATUS_ERROR;
            if (action.contains("CREATED") || action.contains("ASSIGNED"))
                return UITheme.STATUS_ACTIVE;
            if (action.contains("UPDATED") || action.contains("CALCULATED"))
                return UITheme.STATUS_WARNING;
            if (action.contains("LOGIN") || action.contains("LOGOUT"))
                return UITheme.PRIMARY;
            return UITheme.TEXT_WHITE;
        }
    }

    // ==================== INNER CLASS: GroupsPanel ====================
    class GroupsPanel extends JPanel {
        private JPanel gridPanel;
        private DataManager dataManager;
        private ClassGroup selectedGroup = null;
        private RoundedButton btnEdit, btnDel;

        GroupsPanel() {
            dataManager = new DataManager();
            setLayout(new BorderLayout());
            setOpaque(false);

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

            gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
            gridPanel.setOpaque(false);

            JPanel container = new JPanel(new BorderLayout());
            container.setOpaque(false);
            container.add(gridPanel, BorderLayout.NORTH);

            JScrollPane scroll = new JScrollPane(container);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            add(scroll, BorderLayout.CENTER);

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
            btnEdit.setForeground(sel ? Color.WHITE : UITheme.TEXT_MUTED);
            btnDel.setForeground(sel ? Color.WHITE : UITheme.TEXT_MUTED);
        }

        private void refresh() {
            gridPanel.removeAll();
            List<ClassGroup> groups = dataManager.getAllGroups();
            if (groups.isEmpty())
                groups.add(new ClassGroup("C01", "CS101"));

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
                    g2.setColor(new Color(255, 255, 255, 40));
                    Path2D p = new Path2D.Double();
                    p.moveTo(getWidth() - 60, 0);
                    p.lineTo(getWidth(), 0);
                    p.lineTo(getWidth(), 60);
                    p.curveTo(getWidth() - 30, 60, getWidth() - 60, 30, getWidth() - 60, 0);
                    g2.fill(p);
                    if (isSelected) {
                        g2.setColor(Color.WHITE);
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                    }
                }
            };

            card.setLayout(new BorderLayout());
            card.setPreferredSize(new Dimension(220, 180));
            card.setBorder(new EmptyBorder(20, 20, 20, 20));
            card.setOpaque(false);

            JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            top.setOpaque(false);
            JLabel badge = new JLabel("ACTIVE");
            badge.setForeground(new Color(255, 255, 255, 200));
            badge.setFont(UITheme.FONT_BOLD.deriveFont(10f));
            top.add(badge);

            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setOpaque(false);

            JLabel id = new JLabel(g.getGroupId());
            id.setForeground(Color.WHITE);
            id.setFont(UITheme.FONT_TITLE.deriveFont(Font.BOLD, 28f));

            JLabel mod = new JLabel(g.getModuleId());
            mod.setForeground(new Color(255, 255, 255, 200));
            mod.setFont(UITheme.FONT_SUBTITLE);

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

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 20, 5, 20);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;

            JLabel l1 = new JLabel("Group ID");
            DarkDialog.styleLabel(l1);
            p.add(l1, gbc);
            gbc.gridy++;
            p.add(gid, gbc);
            gbc.gridy++;
            JLabel l2 = new JLabel("Module ID");
            DarkDialog.styleLabel(l2);
            p.add(l2, gbc);
            gbc.gridy++;
            p.add(mid, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(20, 20, 10, 20);
            RoundedButton save = new RoundedButton("Save", UITheme.PRIMARY, null, 15);
            save.addActionListener(e -> {
                if (!gid.getText().isEmpty()) {
                    boolean isNew = existing == null;
                    dataManager.saveGroup(new ClassGroup(gid.getText(), mid.getText()));
                    if (isNew)
                        LogManager.logGroupCreated(gid.getText(), mid.getText());
                    d.dispose();
                    refresh();
                }
            });
            p.add(save, gbc);

            d.setVisible(true);
        }
    }

    // ==================== INNER CLASS: ModulesPanel ====================
    class ModulesPanel extends JPanel {
        private JPanel gridPanel;
        private DataManager dataManager;
        private Module selectedModule = null;
        private RoundedButton btnEdit, btnFiles, btnDel;

        ModulesPanel() {
            dataManager = new DataManager();
            setLayout(new BorderLayout());
            setOpaque(false);

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

            gridPanel = new JPanel(new GridLayout(0, 3, UITheme.SPACE_LG, UITheme.SPACE_LG));
            gridPanel.setOpaque(false);

            JPanel modulesContainer = new JPanel(new BorderLayout());
            modulesContainer.setOpaque(false);
            modulesContainer.add(gridPanel, BorderLayout.NORTH);

            JScrollPane scroll = new JScrollPane(modulesContainer);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            add(scroll, BorderLayout.CENTER);

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
                    int c = JOptionPane.showConfirmDialog(this, "Delete " + selectedModule.getModuleId() + "?",
                            "Confirm", JOptionPane.YES_NO_OPTION);
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
            btnFiles.setForeground(sel ? Color.WHITE : UITheme.TEXT_MUTED);
            btnEdit.setForeground(sel ? Color.WHITE : UITheme.TEXT_MUTED);
            btnDel.setForeground(sel ? Color.WHITE : UITheme.TEXT_MUTED);
        }

        private void refresh() {
            gridPanel.removeAll();
            List<Module> modules = dataManager.getAllModules();
            if (modules.isEmpty())
                modules.add(new Module("CS101", "Intro to Computing", "TP000001"));

            Color[] palette = { UITheme.PRIMARY, new Color(255, 80, 80), new Color(40, 200, 80) };
            int i = 0;
            for (Module m : modules) {
                gridPanel.add(createModuleCard(m, palette[i % palette.length]));
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
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                    GradientPaint gp = new GradientPaint(0, 0, new Color(255, 255, 255, 10), 0, getHeight(),
                            new Color(0, 0, 0, 20));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
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
            card.setPreferredSize(new Dimension(240, 300));
            card.setBorder(new EmptyBorder(25, 25, 25, 25));
            card.setOpaque(false);

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

            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBorder(new EmptyBorder(20, 0, 20, 0));

            JLabel code = new JLabel(m.getModuleId());
            code.setFont(UITheme.FONT_BOLD.deriveFont(14f));
            code.setForeground(accent);
            code.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel titleLbl = new JLabel("<html><center>" + m.getModuleName() + "</center></html>");
            titleLbl.setFont(UITheme.FONT_TITLE.deriveFont(18f));
            titleLbl.setForeground(UITheme.TEXT_WHITE);
            titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lead = new JLabel("Leader: " + m.getLeaderId());
            lead.setFont(UITheme.FONT_REGULAR);
            lead.setForeground(UITheme.TEXT_MUTED);
            lead.setAlignmentX(Component.CENTER_ALIGNMENT);

            body.add(code);
            body.add(Box.createVerticalStrut(10));
            body.add(titleLbl);
            body.add(Box.createVerticalStrut(15));
            body.add(lead);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
            footer.setOpaque(false);
            int fCount = dataManager.getModuleFiles(m.getModuleId()).size();
            JLabel files = new JLabel(fCount + " Files Attached");
            files.setForeground(new Color(255, 255, 255, 150));
            files.setFont(UITheme.FONT_REGULAR.deriveFont(11f));
            footer.add(files);

            card.add(top, BorderLayout.NORTH);
            card.add(body, BorderLayout.CENTER);
            card.add(footer, BorderLayout.SOUTH);

            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    selectedModule = m;
                    updateButtons();
                    refresh();
                }

                public void mouseEntered(MouseEvent e) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                public void mouseExited(MouseEvent e) {
                    setCursor(Cursor.getDefaultCursor());
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

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 20, 5, 20);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;

            JLabel l1 = new JLabel("Module Code");
            DarkDialog.styleLabel(l1);
            p.add(l1, gbc);
            gbc.gridy++;
            p.add(c, gbc);
            gbc.gridy++;
            JLabel l2 = new JLabel("Module Name");
            DarkDialog.styleLabel(l2);
            p.add(l2, gbc);
            gbc.gridy++;
            p.add(n, gbc);
            gbc.gridy++;
            JLabel l3 = new JLabel("Leader ID");
            DarkDialog.styleLabel(l3);
            p.add(l3, gbc);
            gbc.gridy++;
            p.add(l, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(30, 20, 20, 20);
            RoundedButton save = new RoundedButton("Save Changes", UITheme.PRIMARY, null, 20);
            save.setPreferredSize(new Dimension(0, 40));
            save.addActionListener(e -> {
                if (!c.getText().isEmpty()) {
                    boolean isUpdate = existing != null;
                    dataManager.saveModule(new Module(c.getText(), n.getText(), l.getText()), isUpdate);
                    if (isUpdate)
                        LogManager.logModuleUpdated(c.getText(), n.getText());
                    else
                        LogManager.logModuleCreated(c.getText(), n.getText());
                    d.dispose();
                    refresh();
                }
            });
            p.add(save, gbc);
            d.setVisible(true);
        }

        private void openFilesDialog(Module m) {
            DarkDialog d = new DarkDialog(SwingUtilities.getWindowAncestor(this), "Manage Files: " + m.getModuleId(),
                    500, 450);
            JPanel root = new JPanel(new BorderLayout());
            root.setOpaque(false);
            root.setBorder(new EmptyBorder(20, 20, 20, 20));
            d.getContent().setLayout(new BorderLayout());
            d.getContent().add(root);

            DefaultTableModel fm = new DefaultTableModel(new String[] { "File Name", "Type" }, 0);
            JTable ft = new JTable(fm);
            ft.setOpaque(false);
            ft.setBackground(new Color(0, 0, 0, 0));
            ft.setForeground(Color.WHITE);
            ft.setShowGrid(false);
            ft.setRowHeight(45);

            for (String[] f : dataManager.getModuleFiles(m.getModuleId()))
                fm.addRow(new Object[] { f[0], "Text Document" });

            JScrollPane sp = new JScrollPane(ft);
            sp.getViewport().setOpaque(false);
            sp.setOpaque(false);
            sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DARK));
            root.add(sp, BorderLayout.CENTER);

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

    // ==================== INNER CLASS: UsersPanel ====================
    class UsersPanel extends JPanel {
        private JTable table;
        private DefaultTableModel model;
        private UserManager userManager;
        private int hoveredRow = -1;
        private RoundedButton btnEdit, btnDel;
        private String currentSortField = "name";
        private boolean sortAscending = true;
        private List<User> allUsers = new ArrayList<>();

        UsersPanel() {
            userManager = new UserManager();
            setLayout(new BorderLayout());
            setOpaque(false);

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
            JPopupMenu popup = new JPopupMenu();
            popup.setBackground(UITheme.BG_SURFACE);
            popup.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1));

            popup.add(createSortMenuItem("Name", "name"));
            popup.add(createSortMenuItem("Role", "role"));
            popup.add(createSortMenuItem("Join Date", "joinDate"));
            popup.add(createSortMenuItem("Status", "status"));
            popup.addSeparator();
            popup.add(createOrderMenuItem("Ascending ↑", true));
            popup.add(createOrderMenuItem("Descending ↓", false));

            popup.show(anchor, 0, anchor.getHeight());
        }

        private JMenuItem createSortMenuItem(String label, String field) {
            JMenuItem item = new JMenuItem(label);
            item.setBackground(UITheme.BG_SURFACE);
            item.setForeground(field.equals(currentSortField) ? UITheme.PRIMARY : UITheme.TEXT_WHITE);
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
            if (!sortAscending)
                comparator = comparator.reversed();
            sorted.sort(comparator);
            model.setRowCount(0);
            for (User u : sorted)
                model.addRow(new Object[] { u, u.getRole(), u.getStatus(), u.getJoinDate() });
        }

        private JComponent buildTableSection() {
            RoundedPanel wrapper = new RoundedPanel(20, UITheme.CARD_BG);
            wrapper.setLayout(new BorderLayout());
            wrapper.setBorder(new EmptyBorder(1, 1, 1, 1));

            JPanel floatHeader = new JPanel(new GridLayout(1, 4));
            floatHeader.setOpaque(false);
            floatHeader.setBorder(new EmptyBorder(15, 20, 15, 20));
            addCol(floatHeader, "USER PROFILE");
            addCol(floatHeader, "ROLE");
            addCol(floatHeader, "STATUS");
            addCol(floatHeader, "JOINED DATE");
            wrapper.add(floatHeader, BorderLayout.NORTH);

            String[] cols = { "User Info", "Role", "Status", "Joined" };
            model = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };

            table = new JTable(model) {
                @Override
                protected void paintComponent(Graphics g) {
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
                            g2.setColor(UITheme.SELECTION_HIGHLIGHT);
                            g2.fillRect(0, y, width, rowHeight);
                            g2.setColor(new Color(100, 180, 255));
                            g2.drawRect(0, y, width - 1, rowHeight - 1);
                        } else if (isHov) {
                            g2.setColor(new Color(60, 60, 75, 150));
                            g2.fillRect(0, y, width, rowHeight);
                        } else if (i % 2 == 0) {
                            g2.setColor(new Color(25, 25, 30, 40));
                            g2.fillRect(0, y, width, rowHeight);
                        }
                    }
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

            table.setTableHeader(null);
            table.setOpaque(false);
            table.setFillsViewportHeight(true);
            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    updateButtons();
                    table.repaint();
                }
            });

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

            table.setRowHeight(72);
            table.setShowGrid(false);
            table.setIntercellSpacing(new Dimension(0, 0));
            table.setBorder(null);
            table.setBackground(new Color(0, 0, 0, 0));
            table.setForeground(UITheme.TEXT_WHITE);
            table.setFont(UITheme.FONT_REGULAR);

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
            l.setForeground(new Color(100, 100, 120));
            p.add(l);
        }

        private JPanel buildBottomBar() {
            JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACE_MD, UITheme.SPACE_MD));
            bar.setBackground(UITheme.BAR_BG);
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
            btnEdit.setForeground(sel ? Color.WHITE : UITheme.TEXT_MUTED);
            btnDel.setForeground(sel ? Color.WHITE : UITheme.TEXT_MUTED);
        }

        class UserInfoRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
                p.setOpaque(false);
                User u = (User) value;
                if (u == null)
                    return p;

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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 22));
                p.setOpaque(false);
                String status = (String) value;
                Color bg = "Active".equals(status) ? UITheme.STATUS_ACTIVE_BG : UITheme.STATUS_ERROR_BG;
                Color fg = "Active".equals(status) ? UITheme.STATUS_ACTIVE : UITheme.STATUS_ERROR;

                JLabel pill = new JLabel(status, SwingConstants.CENTER) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getBackground());
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(UITheme.TEXT_MUTED);
                c.setFont(UITheme.FONT_REGULAR);
                return c;
            }
        }

        private void refreshTable() {
            model.setRowCount(0);
            allUsers = userManager.getAllUsers();
            for (User u : allUsers)
                model.addRow(new Object[] { u, u.getRole(), u.getStatus(), u.getJoinDateFormatted() });
        }

        private void openUserDialog(User existing) {
            DarkDialog d = new DarkDialog(SwingUtilities.getWindowAncestor(this),
                    existing == null ? "Add User" : "Edit User", 420, 500);
            JPanel p = new JPanel(new GridBagLayout());
            p.setOpaque(false);
            d.getContent().setLayout(new BorderLayout());
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
                if (isUpdate)
                    LogManager.logUserUpdated(i, n);
                else
                    LogManager.logUserCreated(i, n, r);
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

    // ==================== INNER CLASS: GradingPanel ====================
    class GradingPanel extends JPanel {

        GradingPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(0, UITheme.SPACE_XXS, UITheme.SPACE_LG, UITheme.SPACE_XXS));

            JLabel title = new JLabel("APU Grading System");
            title.setFont(UITheme.FONT_TITLE);
            title.setForeground(UITheme.TEXT_WHITE);

            JLabel subtitle = new JLabel("University Grade Definitions & GPA Scale");
            subtitle.setFont(UITheme.FONT_SUBTITLE);
            subtitle.setForeground(UITheme.TEXT_MUTED);

            JPanel titlePanel = new JPanel();
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
            titlePanel.setOpaque(false);
            titlePanel.add(title);
            titlePanel.add(Box.createVerticalStrut(5));
            titlePanel.add(subtitle);

            header.add(titlePanel, BorderLayout.WEST);
            add(header, BorderLayout.NORTH);

            RoundedPanel tableWrapper = new RoundedPanel(UITheme.RADIUS_LG, UITheme.CARD_BG);
            tableWrapper.setLayout(new BorderLayout());
            tableWrapper
                    .setBorder(new EmptyBorder(UITheme.SPACE_MD, UITheme.SPACE_MD, UITheme.SPACE_MD, UITheme.SPACE_MD));

            String[] cols = { "Grade", "Percentage Scale %", "Interpretation", "GPA" };
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };

            Map<String, Grade> grades = GradingSystem.getAllGrades();
            for (Grade g : grades.values()) {
                model.addRow(new Object[] {
                        g.getGrade(),
                        g.getPercentageRange(),
                        g.getInterpretation(),
                        g.getGpaString()
                });
            }

            JTable table = new JTable(model);
            table.setOpaque(false);
            table.setBackground(new Color(0, 0, 0, 0));
            table.setForeground(UITheme.TEXT_WHITE);
            table.setFont(UITheme.FONT_REGULAR.deriveFont(14f));
            table.setRowHeight(50);
            table.setShowGrid(false);
            table.setIntercellSpacing(new Dimension(0, 0));
            table.getTableHeader().setBackground(UITheme.BG_SURFACE);
            table.getTableHeader().setForeground(UITheme.PRIMARY);
            table.getTableHeader().setFont(UITheme.FONT_BOLD);

            table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                            row, column);
                    String grade = (String) value;
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setFont(UITheme.FONT_BOLD.deriveFont(16f));

                    Color color = Color.decode(GradingSystem.getGradeColorHex(grade));
                    label.setForeground(color);
                    label.setBackground(new Color(0, 0, 0, 0));
                    label.setOpaque(false);
                    return label;
                }
            });

            for (int i = 1; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                            boolean isSelected, boolean hasFocus, int row, int column) {
                        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                                row, column);
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                        label.setForeground(UITheme.TEXT_WHITE);
                        label.setOpaque(false);
                        return label;
                    }
                });
            }

            JScrollPane scroll = new JScrollPane(table);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);

            tableWrapper.add(scroll, BorderLayout.CENTER);
            add(tableWrapper, BorderLayout.CENTER);

            JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            infoPanel.setOpaque(false);
            infoPanel.setBorder(new EmptyBorder(UITheme.SPACE_MD, 0, 0, 0));

            JLabel passLabel = new JLabel("* Passing Grade: D (60%) or above");
            passLabel.setFont(UITheme.FONT_REGULAR);
            passLabel.setForeground(UITheme.STATUS_ACTIVE);

            JLabel failLabel = new JLabel("   * Fail Grade: F (below 60%)");
            failLabel.setFont(UITheme.FONT_REGULAR);
            failLabel.setForeground(UITheme.STATUS_ERROR);

            infoPanel.add(passLabel);
            infoPanel.add(failLabel);
            add(infoPanel, BorderLayout.SOUTH);
        }
    }

    // ==================== INNER CLASS: LecturerAssignmentsPanel
    // ====================
    class LecturerAssignmentsPanel extends JPanel {
        private JPanel assignmentsGrid;
        private UserManager userManager;
        private LecturerAssignment selectedAssignment = null;
        private RoundedButton btnEdit, btnDel;

        LecturerAssignmentsPanel() {
            userManager = new UserManager();
            setLayout(new BorderLayout());
            setOpaque(false);

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(0, UITheme.SPACE_XXS, UITheme.SPACE_LG, UITheme.SPACE_XXS));

            JPanel titlePanel = new JPanel();
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
            titlePanel.setOpaque(false);

            JLabel title = new JLabel("Lecturer Assignments");
            title.setFont(UITheme.FONT_TITLE);
            title.setForeground(UITheme.TEXT_WHITE);

            JLabel subtitle = new JLabel("Assign Lecturers to Academic Leaders");
            subtitle.setFont(UITheme.FONT_SUBTITLE);
            subtitle.setForeground(UITheme.TEXT_MUTED);

            titlePanel.add(title);
            titlePanel.add(Box.createVerticalStrut(5));
            titlePanel.add(subtitle);

            RoundedButton addBtn = new RoundedButton("+ New Assignment", UITheme.PRIMARY, null, UITheme.RADIUS_LG);
            addBtn.setPreferredSize(new Dimension(160, 38));
            addBtn.addActionListener(e -> openAssignmentDialog(null));

            header.add(titlePanel, BorderLayout.WEST);
            header.add(addBtn, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            assignmentsGrid = new JPanel(new GridLayout(0, 3, UITheme.SPACE_LG, UITheme.SPACE_LG));
            assignmentsGrid.setOpaque(false);

            JPanel container = new JPanel(new BorderLayout());
            container.setOpaque(false);
            container.add(assignmentsGrid, BorderLayout.NORTH);

            JScrollPane scroll = new JScrollPane(container);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            add(scroll, BorderLayout.CENTER);

            add(buildBottomBar(), BorderLayout.SOUTH);
            refresh();
        }

        private JPanel buildBottomBar() {
            JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACE_MD, UITheme.SPACE_MD));
            bar.setBackground(UITheme.BAR_BG);
            bar.setPreferredSize(new Dimension(0, 70));

            btnEdit = new RoundedButton("Edit Assignment", UITheme.BORDER_DARK, null, UITheme.RADIUS_LG);
            btnEdit.setPreferredSize(new Dimension(140, 36));
            btnEdit.setEnabled(false);
            btnEdit.addActionListener(e -> {
                if (selectedAssignment != null) {
                    openAssignmentDialog(selectedAssignment);
                }
            });

            btnDel = new RoundedButton("Remove", UITheme.STATUS_ERROR, null, UITheme.RADIUS_LG);
            btnDel.setPreferredSize(new Dimension(100, 36));
            btnDel.setEnabled(false);
            btnDel.addActionListener(e -> {
                if (selectedAssignment != null) {
                    int c = JOptionPane.showConfirmDialog(this,
                            "Remove assignment for " + selectedAssignment.getLecturerName() + "?",
                            "Confirm", JOptionPane.YES_NO_OPTION);
                    if (c == JOptionPane.YES_OPTION) {
                        if (deleteLecturerAssignment(selectedAssignment.getLecturerId())) {
                            LogManager.log("LECTURER_UNASSIGNED", String.format(
                                    "Unassigned lecturer: %s (%s)",
                                    selectedAssignment.getLecturerName(),
                                    selectedAssignment.getLecturerId()));
                        }
                        selectedAssignment = null;
                        updateButtons();
                        refresh();
                    }
                }
            });

            RoundedButton btnRefresh = new RoundedButton("Refresh", UITheme.BORDER_DARK, null, UITheme.RADIUS_LG);
            btnRefresh.setPreferredSize(new Dimension(100, 36));
            btnRefresh.addActionListener(e -> refresh());

            bar.add(btnRefresh);
            bar.add(btnEdit);
            bar.add(btnDel);
            return bar;
        }

        private void updateButtons() {
            boolean sel = selectedAssignment != null;
            btnEdit.setEnabled(sel);
            btnDel.setEnabled(sel);
            btnEdit.setForeground(sel ? Color.WHITE : UITheme.TEXT_MUTED);
            btnDel.setForeground(sel ? Color.WHITE : UITheme.TEXT_MUTED);
        }

        private void refresh() {
            assignmentsGrid.removeAll();
            List<LecturerAssignment> assignments = getAllLecturerAssignments();

            Color[][] gradients = { UITheme.GRADIENT_PURPLE, UITheme.GRADIENT_BLUE, UITheme.GRADIENT_TEAL,
                    UITheme.GRADIENT_ORANGE };
            int i = 0;
            for (LecturerAssignment a : assignments) {
                assignmentsGrid.add(createAssignmentCard(a, gradients[i % gradients.length]));
                i++;
            }

            if (assignments.isEmpty()) {
                JPanel emptyPanel = new JPanel(new GridBagLayout());
                emptyPanel.setOpaque(false);
                JLabel emptyLabel = new JLabel("No lecturer assignments yet. Click '+ New Assignment' to create one.");
                emptyLabel.setFont(UITheme.FONT_SUBTITLE);
                emptyLabel.setForeground(UITheme.TEXT_MUTED);
                emptyPanel.add(emptyLabel);
                assignmentsGrid.add(emptyPanel);
            }

            assignmentsGrid.revalidate();
            assignmentsGrid.repaint();
        }

        private JPanel createAssignmentCard(LecturerAssignment a, Color[] gradient) {
            boolean isSelected = selectedAssignment != null &&
                    selectedAssignment.getLecturerId().equals(a.getLecturerId());

            JPanel card = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    GradientPaint gp = new GradientPaint(0, 0, gradient[0], getWidth(), getHeight(), gradient[1]);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                    g2.setColor(new Color(255, 255, 255, 40));
                    Path2D p = new Path2D.Double();
                    p.moveTo(getWidth() - 50, 0);
                    p.lineTo(getWidth(), 0);
                    p.lineTo(getWidth(), 50);
                    p.curveTo(getWidth() - 25, 50, getWidth() - 50, 25, getWidth() - 50, 0);
                    g2.fill(p);

                    if (isSelected) {
                        g2.setColor(Color.WHITE);
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                    }
                }
            };

            card.setLayout(new BorderLayout());
            card.setPreferredSize(new Dimension(280, 160));
            card.setBorder(new EmptyBorder(20, 20, 20, 20));
            card.setOpaque(false);

            JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            top.setOpaque(false);
            JLabel badge = new JLabel(a.getStatus().toUpperCase());
            badge.setForeground(a.isActive() ? new Color(100, 255, 150) : new Color(255, 150, 100));
            badge.setFont(UITheme.FONT_BOLD.deriveFont(10f));
            top.add(badge);

            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setOpaque(false);

            JLabel lecturerName = new JLabel(a.getLecturerName());
            lecturerName.setForeground(Color.WHITE);
            lecturerName.setFont(UITheme.FONT_BOLD.deriveFont(16f));

            JLabel lecturerId = new JLabel(a.getLecturerId());
            lecturerId.setForeground(new Color(255, 255, 255, 180));
            lecturerId.setFont(UITheme.FONT_REGULAR.deriveFont(12f));

            JLabel arrow = new JLabel("Reports to");
            arrow.setForeground(new Color(255, 255, 255, 150));
            arrow.setFont(UITheme.FONT_REGULAR.deriveFont(11f));

            JLabel leaderName = new JLabel(a.getAcademicLeaderName());
            leaderName.setForeground(new Color(255, 220, 100));
            leaderName.setFont(UITheme.FONT_BOLD.deriveFont(14f));

            body.add(lecturerName);
            body.add(Box.createVerticalStrut(2));
            body.add(lecturerId);
            body.add(Box.createVerticalStrut(10));
            body.add(arrow);
            body.add(Box.createVerticalStrut(4));
            body.add(leaderName);

            card.add(top, BorderLayout.NORTH);
            card.add(body, BorderLayout.CENTER);

            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedAssignment = a;
                    updateButtons();
                    refresh();
                }
            });

            return card;
        }

        private void openAssignmentDialog(LecturerAssignment existing) {
            DarkDialog d = new DarkDialog(SwingUtilities.getWindowAncestor(this),
                    existing == null ? "New Assignment" : "Edit Assignment", 450, 450);
            JPanel p = new JPanel(new GridBagLayout());
            p.setOpaque(false);
            d.getContent().setLayout(new BorderLayout());
            d.getContent().add(p, BorderLayout.CENTER);

            List<User> allUsers = userManager.getAllUsers();
            List<User> lecturers = new ArrayList<>();
            List<User> leaders = new ArrayList<>();
            for (User user : allUsers) {
                String role = user.getRole() == null ? "" : user.getRole().trim();
                if ("LECTURER".equalsIgnoreCase(role)) {
                    lecturers.add(user);
                } else if ("LEADER".equalsIgnoreCase(role) || "ACADEMICLEADER".equalsIgnoreCase(role)) {
                    leaders.add(user);
                }
            }

            JComboBox<String> lecturerBox = new JComboBox<>();
            for (User u : lecturers) {
                lecturerBox.addItem(u.getId() + " - " + u.getName());
            }

            JComboBox<String> leaderBox = new JComboBox<>();
            for (User u : leaders) {
                leaderBox.addItem(u.getId() + " - " + u.getName());
            }

            if (lecturerBox.getItemCount() == 0 || leaderBox.getItemCount() == 0) {
                JOptionPane.showMessageDialog(d,
                        "At least one Lecturer and one Leader user are required.",
                        "Missing Users",
                        JOptionPane.WARNING_MESSAGE);
                d.dispose();
                return;
            }

            String[] statuses = { "Active", "Inactive" };
            JComboBox<String> statusBox = new JComboBox<>(statuses);

            if (existing != null) {
                for (int i = 0; i < lecturerBox.getItemCount(); i++) {
                    if (lecturerBox.getItemAt(i).startsWith(existing.getLecturerId())) {
                        lecturerBox.setSelectedIndex(i);
                        break;
                    }
                }
                for (int i = 0; i < leaderBox.getItemCount(); i++) {
                    if (leaderBox.getItemAt(i).startsWith(existing.getAcademicLeaderId())) {
                        leaderBox.setSelectedIndex(i);
                        break;
                    }
                }
                statusBox.setSelectedItem(existing.getStatus());
                lecturerBox.setEnabled(false);
            }

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 20, 5, 20);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;

            JLabel l1 = new JLabel("Lecturer");
            DarkDialog.styleLabel(l1);
            p.add(l1, gbc);
            gbc.gridy++;
            p.add(lecturerBox, gbc);
            gbc.gridy++;
            JLabel l2 = new JLabel("Academic Leader");
            DarkDialog.styleLabel(l2);
            p.add(l2, gbc);
            gbc.gridy++;
            p.add(leaderBox, gbc);
            gbc.gridy++;
            JLabel l3 = new JLabel("Status");
            DarkDialog.styleLabel(l3);
            p.add(l3, gbc);
            gbc.gridy++;
            p.add(statusBox, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(30, 20, 20, 20);
            RoundedButton save = new RoundedButton("Save Assignment", UITheme.PRIMARY, null, UITheme.RADIUS_LG);
            save.setPreferredSize(new Dimension(0, 40));
            save.addActionListener(e -> {
                String lecturerSelection = (String) lecturerBox.getSelectedItem();
                String leaderSelection = (String) leaderBox.getSelectedItem();

                if (lecturerSelection == null || leaderSelection == null) {
                    JOptionPane.showMessageDialog(d, "Please select both a lecturer and an academic leader.");
                    return;
                }

                String[] lecturerParts = lecturerSelection.split(" - ", 2);
                String[] leaderParts = leaderSelection.split(" - ", 2);
                if (lecturerParts.length < 2 || leaderParts.length < 2) {
                    JOptionPane.showMessageDialog(d, "Invalid selection format.");
                    return;
                }

                String lecturerId = lecturerParts[0];
                String lecturerName = lecturerParts[1];
                String leaderId = leaderParts[0];
                String leaderName = leaderParts[1];
                String status = (String) statusBox.getSelectedItem();

                LecturerAssignment assignment;
                if (existing != null) {
                    assignment = new LecturerAssignment(
                            lecturerId, lecturerName, leaderId, leaderName,
                            existing.getAssignmentDate(), status);
                } else {
                    assignment = new LecturerAssignment(lecturerId, lecturerName, leaderId, leaderName);
                    assignment.setStatus(status);
                }

                if (!saveLecturerAssignment(assignment)) {
                    JOptionPane.showMessageDialog(d,
                            "Unable to save assignment.",
                            "Data Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                LogManager.log("LECTURER_ASSIGNED", String.format(
                        "Assigned lecturer: %s (%s) to Academic Leader: %s (%s)",
                        lecturerName, lecturerId, leaderName, leaderId));

                d.dispose();
                refresh();
            });
            p.add(save, gbc);

            d.setVisible(true);
        }
    }
}
