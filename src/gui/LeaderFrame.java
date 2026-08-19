package gui;

import users.AcademicLeader;
import util.FileManager;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

public final class LeaderFrame extends JFrame {

    private final AcademicLeader leader;
    private final Runnable onLogout;

    // Navigation
    private final FluidTransitionPanel contentPanel = new FluidTransitionPanel();

    private static final String CARD_HOME = "HOME";
    private static final String CARD_MANAGE = "MANAGE";
    private static final String CARD_ASSIGN = "ASSIGN";
    private static final String CARD_REPORT = "REPORT";
    private static final String CARD_NOTIFICATIONS = "NOTIFICATIONS";
    private static final String CARD_SETTINGS = "SETTINGS";
    private static final String CARD_EDIT_PROFILE = "EDIT_PROFILE";
    private static final String CARD_WHATS_NEW = "WHATS_NEW";

    // Theme (mutable so the UI can switch between light/dark)
    private enum ThemeMode {
        DARK, LIGHT
    }

    private static ThemeMode currentTheme = ThemeMode.LIGHT;
    private static final String THEME_FILE = "theme.txt";

    private static Color BG_DARK = new Color(11, 14, 17);
    private static Color CARD_BG = new Color(24, 25, 27);
    private static Color ACCENT_BLUE = new Color(30, 129, 255);
    private static Color TEXT_MUTED = new Color(160, 160, 165);
    private static Color INPUT_BG = new Color(16, 18, 21);
    private static Color TEXT_WHITE = new Color(245, 245, 245);
    private static Color DIALOG_BORDER = new Color(40, 43, 48);
    private static Color ERROR_RED = new Color(220, 80, 80);
    private static Color NOTIF_CARD = new Color(30, 31, 33);
    private static Color NOTIF_CARD_BORDER = new Color(45, 48, 55);

    private static Color SETTINGS_CARD = new Color(40, 41, 43);
    private static Color SETTINGS_CARD_BORDER = new Color(58, 60, 62);
    private static Color MENU_ROW_BG = new Color(52, 54, 56);
    private static Color MENU_ROW_BG_HOVER = new Color(62, 64, 68);
    private static Color MENU_ROW_BORDER = new Color(68, 70, 72);
    private static Color MENU_ROW_BORDER_HOVER = new Color(92, 94, 98);
    private static Color MENU_TEXT_HOVER = new Color(220, 230, 255);
    private static Color HEADER_ICON_HOVER = new Color(210, 225, 255);
    private static Color TILE_HOVER = new Color(45, 150, 255);
    private static Color TILE_TEXT = Color.WHITE;
    private static Color TILE_TEXT_HOVER = new Color(235, 245, 255);
    private static Color AVATAR_BG = new Color(60, 64, 70);
    private static Color PILL_BORDER = new Color(120, 120, 120);
    private static Color STATUS_PILL_BG = new Color(170, 170, 170);
    private static Color STATUS_PILL_TEXT = new Color(30, 30, 30);
    private static Color SUBCARD_BG = new Color(26, 28, 31);
    private static Color SUBCARD_BORDER = new Color(40, 42, 45);
    private static Color TABLE_BG = new Color(16, 18, 21);
    private static Color TABLE_ALT_BG = new Color(22, 25, 28);
    private static Color TABLE_HEADER_BG = new Color(30, 32, 36);
    private static Color TABLE_HEADER_BORDER = new Color(45, 51, 59);
    private static final int HOVER_LIFT_PX = 2;
    private static Color DASH_CARD_BG = new Color(20, 22, 25);
    private static Color DASH_CARD_BORDER = new Color(36, 38, 41);
    private static final int DASH_CARD_RADIUS = 24;
    private static final int DASH_CARD_WIDTH = 880;
    private static final int QUICK_TILE_GAP = 16;
    private static final int QUICK_TILE_HEIGHT = 104;
    private static Color WHATS_NEW_BG = new Color(8, 9, 11);
    private static Color WHATS_NEW_BAR = new Color(22, 23, 25);
    private static Color WHATS_NEW_CARD = new Color(34, 36, 38);
    private static Color WHATS_NEW_CARD_BORDER = new Color(48, 50, 52);
    private static Color WHATS_NEW_SUBTEXT = new Color(165, 165, 170);
    private static final int WHATS_NEW_COLUMN_WIDTH = 880;
    private static final int WHATS_NEW_ICON_LARGE = 78;
    private static final int WHATS_NEW_ICON_SMALL = 64;

    private static void applyTheme(ThemeMode mode) {
        if (mode == ThemeMode.DARK) {
            BG_DARK = new Color(11, 14, 17);
            CARD_BG = new Color(24, 25, 27);
            ACCENT_BLUE = new Color(30, 129, 255);
            TEXT_MUTED = new Color(160, 160, 165);
            INPUT_BG = new Color(16, 18, 21);
            TEXT_WHITE = new Color(245, 245, 245);
            DIALOG_BORDER = new Color(40, 43, 48);
            ERROR_RED = new Color(220, 80, 80);
            NOTIF_CARD = new Color(30, 31, 33);
            NOTIF_CARD_BORDER = new Color(45, 48, 55);

            SETTINGS_CARD = new Color(40, 41, 43);
            SETTINGS_CARD_BORDER = new Color(58, 60, 62);
            MENU_ROW_BG = new Color(52, 54, 56);
            MENU_ROW_BG_HOVER = new Color(62, 64, 68);
            MENU_ROW_BORDER = new Color(68, 70, 72);
            MENU_ROW_BORDER_HOVER = new Color(92, 94, 98);
            MENU_TEXT_HOVER = new Color(220, 230, 255);
            HEADER_ICON_HOVER = new Color(210, 225, 255);
            TILE_HOVER = new Color(45, 150, 255);
            TILE_TEXT = Color.WHITE;
            TILE_TEXT_HOVER = new Color(235, 245, 255);
            AVATAR_BG = new Color(60, 64, 70);
            PILL_BORDER = new Color(120, 120, 120);
            STATUS_PILL_BG = new Color(170, 170, 170);
            STATUS_PILL_TEXT = new Color(30, 30, 30);
            SUBCARD_BG = new Color(26, 28, 31);
            SUBCARD_BORDER = new Color(40, 42, 45);
            TABLE_BG = new Color(16, 18, 21);
            TABLE_ALT_BG = new Color(22, 25, 28);
            TABLE_HEADER_BG = new Color(30, 32, 36);
            TABLE_HEADER_BORDER = new Color(45, 51, 59);
            DASH_CARD_BG = new Color(20, 22, 25);
            DASH_CARD_BORDER = new Color(36, 38, 41);
            WHATS_NEW_BG = new Color(8, 9, 11);
            WHATS_NEW_BAR = new Color(22, 23, 25);
            WHATS_NEW_CARD = new Color(34, 36, 38);
            WHATS_NEW_CARD_BORDER = new Color(48, 50, 52);
            WHATS_NEW_SUBTEXT = new Color(165, 165, 170);
        } else {
            BG_DARK = new Color(245, 247, 251);
            CARD_BG = new Color(255, 255, 255);
            ACCENT_BLUE = new Color(47, 124, 255);
            TEXT_MUTED = new Color(110, 115, 125);
            INPUT_BG = new Color(246, 248, 252);
            TEXT_WHITE = new Color(33, 37, 41);
            DIALOG_BORDER = new Color(224, 228, 235);
            ERROR_RED = new Color(220, 80, 80);
            NOTIF_CARD = new Color(255, 255, 255);
            NOTIF_CARD_BORDER = new Color(226, 230, 238);

            SETTINGS_CARD = new Color(255, 255, 255);
            SETTINGS_CARD_BORDER = new Color(228, 232, 238);
            MENU_ROW_BG = new Color(245, 247, 251);
            MENU_ROW_BG_HOVER = new Color(235, 240, 247);
            MENU_ROW_BORDER = new Color(228, 232, 238);
            MENU_ROW_BORDER_HOVER = new Color(210, 216, 226);
            MENU_TEXT_HOVER = new Color(30, 94, 210);
            HEADER_ICON_HOVER = new Color(30, 94, 210);
            TILE_HOVER = new Color(25, 105, 220);
            TILE_TEXT = Color.WHITE;
            TILE_TEXT_HOVER = new Color(235, 245, 255);
            AVATAR_BG = new Color(225, 230, 238);
            PILL_BORDER = new Color(180, 185, 195);
            STATUS_PILL_BG = new Color(230, 234, 242);
            STATUS_PILL_TEXT = new Color(90, 95, 105);
            SUBCARD_BG = new Color(248, 250, 253);
            SUBCARD_BORDER = new Color(230, 234, 241);
            TABLE_BG = new Color(248, 250, 253);
            TABLE_ALT_BG = new Color(241, 245, 250);
            TABLE_HEADER_BG = new Color(242, 245, 249);
            TABLE_HEADER_BORDER = new Color(226, 230, 238);
            DASH_CARD_BG = new Color(255, 255, 255);
            DASH_CARD_BORDER = new Color(226, 230, 238);
            WHATS_NEW_BG = new Color(245, 247, 251);
            WHATS_NEW_BAR = new Color(255, 255, 255);
            WHATS_NEW_CARD = new Color(255, 255, 255);
            WHATS_NEW_CARD_BORDER = new Color(230, 234, 241);
            WHATS_NEW_SUBTEXT = new Color(125, 130, 140);
        }
    }

    // Busy state
    private boolean busy = false;

    // Header icons
    private JButton docBtn;
    private JButton gearBtn;
    private JButton bellBtn;
    private JButton logoutBtn;
    private JLabel nameLabel;
    private JLabel idLabel;
    private JLabel covidStatusPillLabel;
    private BufferedImage leaderAvatarImage;
    private BufferedImage whatsNewHighlightImage;
    private BufferedImage whatsNewHideImage;
    private BufferedImage whatsNewThemeImage;
    private BufferedImage whatsNewDashboardImage;
    private BufferedImage whatsNewFindClassroomImage;
    private BufferedImage whatsNewFavouritesImage;
    private BufferedImage quickModuleImage;
    private BufferedImage quickAssignmentImage;
    private BufferedImage quickReportImage;
    private BufferedImage noItemsImage;

    // Home tiles
    private TileButton manageTile;
    private TileButton assignTile;
    private TileButton reportsTile;

    // Manage screen
    private final JTextField createModuleIdField = new JTextField(14);
    private final JTextField createModuleNameField = new JTextField(22);
    private final JTextField updateModuleIdField = new JTextField(14);
    private final JTextField updateModuleNameField = new JTextField(22);
    private final JButton createBtn = new JButton("Create Module");
    private final JButton updateBtn = new JButton("Update Module");
    private final JButton deleteBtn = new JButton("Delete Module");
    private final List<JButton> manageTabCreateBtns = new ArrayList<>();
    private final List<JButton> manageTabUpdateBtns = new ArrayList<>();
    private final CardLayout manageCardLayout = new CardLayout();
    private final JPanel manageCardHost = new JPanel(manageCardLayout);
    private static final String MANAGE_CREATE = "MANAGE_CREATE";
    private static final String MANAGE_UPDATE = "MANAGE_UPDATE";

    // Assign screen
    private final JTextField assignModuleIdField = new JTextField(14);
    private final JTextField lecturerIdField = new JTextField(14);
    private final JButton assignBtn = new JButton("Assign Lecturer");

    // Reports screen
    private final JTextField reportModuleIdField = new JTextField(14);
    private final JButton viewBtn = new JButton("View Reports");
    private final JLabel feedbackCountLabel = new JLabel("Feedback: 0");

    // Settings screen (edit profile)
    private final JTextField profileUserIdField = new JTextField(14);
    private final JTextField profileRoleField = new JTextField(14);
    private final JTextField profileNameField = new JTextField(22);
    private final JPasswordField profilePasswordField = new JPasswordField(22);
    private final JButton saveProfileBtn = createDialogPrimaryButton("Save Changes");
    private final JButton editProfileMenuBtn = new JButton("Edit Profile");
    private final JButton themeMenuBtn = new JButton("Theme");
    private final JButton logoutMenuBtn = new JButton("Logout");

    private final DefaultTableModel reportModel = new DefaultTableModel(
            new Object[] { "Assessment ID", "Type", "Max Marks", "Average", "No. of Submissions" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable reportTable = new JTable(reportModel);
    private TableRowSorter<DefaultTableModel> reportSorter;
    private String filterType = "ALL";
    private String filterPerformance = "ALL";
    private boolean filterHasSubmissions = false;

    public LeaderFrame(AcademicLeader leader, Runnable onLogout) {
        super("APSpace - Leader");
        this.leader = Objects.requireNonNull(leader, "leader cannot be null");
        this.onLogout = (onLogout == null) ? () -> {
        } : onLogout;
        currentTheme = readThemePreferenceForUser(this.leader.getUserId());
        applyTheme(currentTheme);

        // UI Setup
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 850);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        leaderAvatarImage = loadImage("resources/leader_emoji1.png", "leader avatar");
        whatsNewHighlightImage = loadImage("resources/whats_new_highlight.png", "whats new highlight");
        whatsNewHideImage = loadImage("resources/whats_new_hide.png", "whats new hide");
        whatsNewThemeImage = loadImage("resources/whats_new_theme.png", "whats new theme");
        whatsNewDashboardImage = loadImage("resources/whats_new_dashboard.png", "whats new dashboard");
        whatsNewFindClassroomImage = loadImage("resources/whats_new_find_classroom.png", "whats new classroom");
        whatsNewFavouritesImage = loadImage("resources/whats_new_favourites.png", "whats new favourites");
        quickModuleImage = loadImage("resources/quick_module.png", "quick access module");
        quickAssignmentImage = loadImage("resources/quick_assignment.png", "quick access assignment");
        quickReportImage = loadImage("resources/quick_report.png", "quick access report");
        noItemsImage = loadImage("resources/noitems.png", "no items");
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        setContentPane(root);

        // Header
        root.add(buildHeader(), BorderLayout.NORTH);

        // Content Area
        // Content Area
        contentPanel.setOpaque(false);
        contentPanel.addCard(CARD_HOME, buildHomeDashboard());
        contentPanel.addCard(CARD_MANAGE, buildManageScreen());
        contentPanel.addCard(CARD_ASSIGN, buildAssignScreen());
        contentPanel.addCard(CARD_REPORT, buildReportsScreen());
        contentPanel.addCard(CARD_NOTIFICATIONS, buildNotificationsScreen());
        contentPanel.addCard(CARD_SETTINGS, buildSettingsScreen());
        contentPanel.addCard(CARD_EDIT_PROFILE, buildEditProfileScreen());
        contentPanel.addCard(CARD_WHATS_NEW, buildWhatsNewScreen());

        root.add(contentPanel, BorderLayout.CENTER);

        // Footer status (subtle, like an internal bar)
        // root.add(buildFooter(), BorderLayout.SOUTH); // Removed for Toast

        bindEvents();

        // Setup GlassPane for Toasts
        setGlassPane(new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                // Pass-through
            }
        });
        getGlassPane().setVisible(true); // Active for painting toasts

        // Initial show
        SwingUtilities.invokeLater(() -> contentPanel.show(CARD_HOME));
    }

    // =========================
    // Header
    // =========================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(10, 0, 8, 0));
        header.setMaximumSize(new Dimension(DASH_CARD_WIDTH, 120));
        header.setPreferredSize(new Dimension(DASH_CARD_WIDTH, 120));

        // Profile Section
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AVATAR_BG);
                g2.fill(new Ellipse2D.Double(0, 0, 60, 60));
                if (leaderAvatarImage != null) {
                    Shape oldClip = g2.getClip();
                    Shape clip = new Ellipse2D.Double(2, 2, 56, 56);
                    g2.setClip(clip);
                    g2.drawImage(leaderAvatarImage, 2, 2, 56, 56, null);
                    g2.setClip(oldClip);
                }
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(60, 60);
            }
        };

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        nameLabel = new JLabel(safe(leader.getFullName()).toUpperCase());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        nameLabel.setForeground(ACCENT_BLUE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        idLabel = new JLabel(safe(leader.getUserId()) + " | LEADER");
        idLabel.setForeground(TEXT_MUTED);
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tags = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        tags.setOpaque(false);
        tags.setAlignmentX(Component.LEFT_ALIGNMENT);
        JComponent hotlinePill = createHeaderPill("Emergency Hotline", ERROR_RED, new Color(0, 0, 0, 0));
        attachPillAction(hotlinePill, this::showHotlineDialog);
        tags.add(hotlinePill);
        tags.add(buildStatusPill());

        text.add(nameLabel);
        text.add(Box.createVerticalStrut(4));
        text.add(idLabel);
        text.add(tags);

        left.add(avatar);
        left.add(text);

        // Icon Section (placeholders)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 2));
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(8, 0, 0, 0));

        docBtn = createIcon("📰");
        gearBtn = createIcon("⚙️");
        bellBtn = createIcon("🔔");

        logoutBtn = createIcon("↩");
        logoutBtn.addActionListener(e -> doLogout());

        right.add(docBtn);
        right.add(gearBtn);
        right.add(bellBtn);
        right.add(logoutBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(8, 0, 6, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.NORTH;
        wrapper.add(header, c);
        return wrapper;
    }

    private BufferedImage loadImage(String path, String label) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Failed to load " + label + ": " + e.getMessage());
            return null;
        }
    }

    private JComponent buildStatusPill() {
        JComponent statusPill = createHeaderPill(readVaccinationStatusLabel(), STATUS_PILL_TEXT, STATUS_PILL_BG);
        if (statusPill.getComponentCount() > 0 && statusPill.getComponent(0) instanceof JLabel) {
            covidStatusPillLabel = (JLabel) statusPill.getComponent(0);
        }
        attachPillAction(statusPill, this::showCovidFormDialog);
        return statusPill;
    }

    private String readVaccinationStatusLabel() {
        try {
            String status = leader.getVaccinationStatus();
            if (status == null || status.trim().isEmpty()) {
                return "No Status";
            }
            return status.trim();
        } catch (IOException e) {
            System.err.println("Failed to read vaccination status: " + e.getMessage());
            return "No Status";
        }
    }

    private void refreshStatusPillText() {
        if (covidStatusPillLabel != null) {
            covidStatusPillLabel.setText(readVaccinationStatusLabel());
            covidStatusPillLabel.revalidate();
            covidStatusPillLabel.repaint();
        }
    }

    private JComponent createHeaderPill(String text, Color textColor, Color fillColor) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(textColor);
        label.setBorder(new EmptyBorder(4, 10, 4, 10));

        JPanel pill = new JPanel(new BorderLayout());
        pill.setOpaque(false);
        pill.add(label, BorderLayout.CENTER);
        pill.setBorder(new EmptyBorder(0, 0, 0, 0));

        pill = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (fillColor.getAlpha() > 0) {
                    g2.setColor(fillColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                }
                boolean isError = textColor.equals(ERROR_RED);
                g2.setColor(isError ? ERROR_RED : PILL_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setOpaque(false);
        pill.add(label, BorderLayout.CENTER);
        return pill;
    }

    private void attachPillAction(JComponent pill, Runnable action) {
        if (pill == null || action == null) {
            return;
        }
        Cursor hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        java.awt.event.MouseAdapter adapter = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
            }
        };
        pill.setCursor(hand);
        pill.addMouseListener(adapter);
        for (Component child : pill.getComponents()) {
            child.setCursor(hand);
            child.addMouseListener(adapter);
        }
    }

    private void showCovidFormDialog() {
        JDialog dialog = new JDialog(this, "COVID-19 Information Form", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        overlay.setOpaque(false);
        overlay.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card = new JPanel(new BorderLayout(0, 14)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(DIALOG_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.setPreferredSize(new Dimension(560, 360));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("COVID-19 Information Form");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(TEXT_WHITE);
        JButton close = createDialogTextButton("X");
        close.addActionListener(e -> dialog.dispose());
        header.add(title, BorderLayout.WEST);
        header.add(close, BorderLayout.EAST);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel note = new JLabel("Update your vaccination status.");
        note.setFont(new Font("SansSerif", Font.PLAIN, 13));
        note.setForeground(TEXT_MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel fullNameLabel = new JLabel("Full Name");
        fullNameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        fullNameLabel.setForeground(TEXT_MUTED);
        fullNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel fullNameValue = new JLabel(safe(leader.getFullName()).toUpperCase());
        fullNameValue.setFont(new Font("SansSerif", Font.BOLD, 15));
        fullNameValue.setForeground(TEXT_WHITE);
        fullNameValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel statusLabel = new JLabel("<html>Vaccination Status <font color='#ff6b6b'>*</font></html>");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(TEXT_MUTED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] statuses = { "Fully Vaccinated", "Partially Vaccinated", "Not Vaccinated" };
        JComboBox<String> statusCombo = createStyledComboBoxDark(statuses, INPUT_BG, DIALOG_BORDER, ACCENT_BLUE);
        statusCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        try {
            String saved = leader.getVaccinationStatus();
            if (saved != null && !saved.trim().isEmpty()) {
                statusCombo.setSelectedItem(saved.trim());
            }
        } catch (IOException e) {
            System.err.println("Failed to load vaccination status: " + e.getMessage());
        }

        JLabel footer = new JLabel(
                "<html><body style='width:460px'>By submitting, you confirm this information is accurate and up to date.</body></html>");
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.setForeground(TEXT_MUTED);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(note);
        form.add(Box.createVerticalStrut(16));
        form.add(fullNameLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(fullNameValue);
        form.add(Box.createVerticalStrut(14));
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(statusCombo);
        form.add(Box.createVerticalStrut(14));
        form.add(footer);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton backBtn = createDialogTextButton("Back");
        backBtn.addActionListener(e -> dialog.dispose());

        JButton submitBtn = createDialogPrimaryButton("Submit");
        submitBtn.addActionListener(e -> {
            String selected = (String) statusCombo.getSelectedItem();
            if (selected == null || selected.trim().isEmpty()) {
                showErrorDialog("Please select a vaccination status.");
                return;
            }
            try {
                leader.updateVaccinationStatus(selected.trim());
                refreshStatusPillText();
                dialog.dispose();
                info("Vaccination status updated.");
            } catch (IOException ex) {
                showErrorDialog("Error saving status: " + safe(ex.getMessage()));
            }
        });
        actions.add(backBtn);
        actions.add(submitBtn);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        overlay.add(card, gbc);

        dialog.setContentPane(overlay);
        dialog.setSize(getSize());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showHotlineDialog() {
        JDialog dialog = new JDialog(this, "Emergency Hotline", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        overlay.setOpaque(false);
        overlay.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(DIALOG_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.setPreferredSize(new Dimension(520, 360));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Emergency Hotline");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(TEXT_WHITE);
        JButton close = createDialogTextButton("X");
        close.addActionListener(e -> dialog.dispose());
        header.add(title, BorderLayout.WEST);
        header.add(close, BorderLayout.EAST);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(buildHotlineItem("APU Security/Emergency Hotline", "017-238 1300", true));
        list.add(buildHotlineItem("Weekends, Public Holidays Only", "017-379 1700", false));

        card.add(header, BorderLayout.NORTH);
        card.add(list, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        overlay.add(card, gbc);

        dialog.setContentPane(overlay);
        dialog.setSize(getSize());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JComponent buildHotlineItem(String titleText, String phone, boolean showDivider) {
        JPanel item = new JPanel();
        item.setOpaque(false);
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        if (showDivider) {
            item.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, DIALOG_BORDER),
                    new EmptyBorder(12, 0, 12, 0)));
        } else {
            item.setBorder(new EmptyBorder(12, 0, 12, 0));
        }

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel phoneRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        phoneRow.setOpaque(false);
        JLabel icon = new JLabel(new PhoneIcon(14, TEXT_MUTED));
        JLabel phoneLabel = new JLabel(phone);
        phoneLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        phoneLabel.setForeground(TEXT_WHITE);
        phoneRow.add(icon);
        phoneRow.add(phoneLabel);
        phoneRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        item.add(title);
        item.add(Box.createVerticalStrut(6));
        item.add(phoneRow);
        return item;
    }

    private static class PhoneIcon implements Icon {
        private final int size;
        private final Color color;

        private PhoneIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int s = size - 1;
            g2.drawArc(x + 1, y + 1, s - 3, s - 3, 225, 90);
            g2.drawArc(x + 2, y + 2, s - 5, s - 5, 45, 90);
            g2.drawLine(x + size / 2 - 1, y + size / 2 - 1, x + size / 2 + 1, y + size / 2 + 1);
            g2.dispose();
        }
    }

    // =========================
    // Elegant Helpers
    // =========================

    private static class ElegantPanel extends JPanel {
        private final Color bgColor;

        ElegantPanel(Color bgColor) {
            this.bgColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

            // Subtle border
            Color border = isLightTheme() ? DIALOG_BORDER : new Color(255, 255, 255, 15);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class DashboardCard extends JPanel {
        private final Color bg;
        private final Color border;
        private final int radius;

        DashboardCard(Color bg, Color border, int radius) {
            this.bg = bg;
            this.border = border;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =========================
    // Home Dashboard
    // =========================

    private JComponent buildHomeDashboard() {
        JPanel home = new JPanel(new GridBagLayout());
        home.setOpaque(false);
        home.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setAlignmentX(Component.CENTER_ALIGNMENT);

        DashboardCard quickAccessCard = new DashboardCard(DASH_CARD_BG, DASH_CARD_BORDER, DASH_CARD_RADIUS);
        quickAccessCard.setLayout(new BoxLayout(quickAccessCard, BoxLayout.Y_AXIS));
        quickAccessCard.setBorder(new EmptyBorder(18, 20, 20, 20));
        quickAccessCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        quickAccessCard.setMaximumSize(new Dimension(DASH_CARD_WIDTH, 320));
        quickAccessCard.setPreferredSize(new Dimension(DASH_CARD_WIDTH, 300));

        JLabel title = new JLabel("Quick Access");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(ACCENT_BLUE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        int contentWidth = DASH_CARD_WIDTH - 40;
        int gridHeight = (QUICK_TILE_HEIGHT * 2) + QUICK_TILE_GAP;
        JPanel grid = new JPanel(new GridLayout(2, 2, QUICK_TILE_GAP, QUICK_TILE_GAP));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setPreferredSize(new Dimension(contentWidth, gridHeight));
        grid.setMaximumSize(new Dimension(contentWidth, gridHeight));

        int tileWidth = (contentWidth - QUICK_TILE_GAP) / 2;
        Dimension tileSize = new Dimension(tileWidth, QUICK_TILE_HEIGHT);

        manageTile = new TileButton("Manage Modules", quickModuleImage);
        assignTile = new TileButton("Assign Lecturer", quickAssignmentImage);
        reportsTile = new TileButton("Reports", quickReportImage);

        manageTile.setPreferredSize(tileSize);
        manageTile.setMaximumSize(tileSize);
        assignTile.setPreferredSize(tileSize);
        assignTile.setMaximumSize(tileSize);
        reportsTile.setPreferredSize(tileSize);
        reportsTile.setMaximumSize(tileSize);

        grid.add(manageTile);
        grid.add(assignTile);
        grid.add(reportsTile);
        grid.add(new JPanel() {
            {
                setOpaque(false);
            }
        });

        quickAccessCard.add(title);
        quickAccessCard.add(Box.createVerticalStrut(14));
        quickAccessCard.add(grid);

        column.add(quickAccessCard);
        column.add(Box.createVerticalStrut(22));
        column.add(new SchedulePanel());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        home.add(column, gbc);

        return home;
    }

    // =========================
    // What's New Screen
    // =========================

    private JComponent buildWhatsNewScreen() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(true);
        main.setBackground(WHATS_NEW_BG);
        main.add(buildWhatsNewAppBar(), BorderLayout.NORTH);

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.setMaximumSize(new Dimension(WHATS_NEW_COLUMN_WIDTH, Integer.MAX_VALUE));
        column.setBorder(new EmptyBorder(24, 0, 60, 0));

        JLabel highlightTitle = whatsNewHeading("Highlight");
        column.add(highlightTitle);
        column.add(Box.createVerticalStrut(12));

        column.add(new WhatsNewCard("Hide Modules", "Learn how to hide modules", whatsNewHighlightImage,
                WHATS_NEW_ICON_LARGE, WHATS_NEW_COLUMN_WIDTH, 170, true));

        column.add(Box.createVerticalStrut(26));

        JLabel tipsTitle = whatsNewHeading("Tips");
        column.add(tipsTitle);
        column.add(Box.createVerticalStrut(12));

        int tipWidth = (WHATS_NEW_COLUMN_WIDTH - 22) / 2;
        JPanel tipsGrid = new JPanel(new GridLayout(0, 2, 22, 22));
        tipsGrid.setOpaque(false);
        tipsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        tipsGrid.setMaximumSize(new Dimension(WHATS_NEW_COLUMN_WIDTH, 1000));

        tipsGrid.add(new WhatsNewCard(
                "Theme & Accent Color",
                "Learn how to change your theme or accent color on APSpace",
                whatsNewThemeImage,
                WHATS_NEW_ICON_SMALL, tipWidth, 140, false));
        tipsGrid.add(new WhatsNewCard(
                "Show or Hide Dashboard Cards",
                "Learn how to show or hide cards on APSpace's Dashboard page.",
                whatsNewDashboardImage,
                WHATS_NEW_ICON_SMALL, tipWidth, 140, false));
        tipsGrid.add(new WhatsNewCard(
                "Hide Modules",
                "Learn how to hide modules",
                whatsNewHideImage,
                WHATS_NEW_ICON_SMALL, tipWidth, 140, false));
        tipsGrid.add(new WhatsNewCard(
                "Find Available Classroom",
                "Learn how to find available classrooms on APSpace's Classroom finder.",
                whatsNewFindClassroomImage,
                WHATS_NEW_ICON_SMALL, tipWidth, 140, false));
        tipsGrid.add(new WhatsNewCard(
                "Optimise Favourites",
                "Learn how to prioritise your favourites on APSpace's More tab.",
                whatsNewFavouritesImage,
                WHATS_NEW_ICON_SMALL, tipWidth, 140, false));
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        tipsGrid.add(filler);

        column.add(tipsGrid);
        column.add(Box.createVerticalStrut(30));

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        GridBagConstraints wc = new GridBagConstraints();
        wc.gridx = 0;
        wc.gridy = 0;
        wc.weightx = 1.0;
        wc.anchor = GridBagConstraints.NORTH;
        wrapper.add(column, wc);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    private JComponent buildWhatsNewAppBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(true);
        bar.setBackground(WHATS_NEW_BAR);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DIALOG_BORDER));
        bar.setPreferredSize(new Dimension(0, 52));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        left.setOpaque(false);

        JButton back = new JButton("←");
        back.setFont(new Font("SansSerif", Font.BOLD, 18));
        back.setForeground(TEXT_WHITE);
        back.setContentAreaFilled(false);
        back.setBorderPainted(false);
        back.setFocusPainted(false);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> showCard(CARD_HOME));

        JLabel title = new JLabel("What's New");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);

        left.add(back);
        left.add(title);
        bar.add(left, BorderLayout.WEST);
        return bar;
    }

    private JLabel whatsNewHeading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        label.setForeground(TEXT_WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static class WhatsNewCard extends JPanel {
        WhatsNewCard(String title, String subtitle, BufferedImage icon, int iconSize, int width, int height,
                boolean large) {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(16, 18, 16, 18));
            setPreferredSize(new Dimension(width, height));
            setMaximumSize(new Dimension(width, height));

            JLabel iconLabel = new JLabel();
            if (icon != null) {
                Image scaled = icon.getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(scaled));
            }
            iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel t = new JLabel(title);
            t.setFont(new Font("SansSerif", Font.BOLD, large ? 18 : 16));
            t.setForeground(TEXT_WHITE);
            t.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel s = new JLabel("<html><body style='width:" + (large ? 520 : 320) + "px'>"
                    + htmlEscape(subtitle) + "</body></html>");
            s.setFont(new Font("SansSerif", Font.PLAIN, 12));
            s.setForeground(WHATS_NEW_SUBTEXT);
            s.setAlignmentX(Component.LEFT_ALIGNMENT);

            add(iconLabel);
            add(Box.createVerticalStrut(10));
            add(t);
            add(Box.createVerticalStrut(4));
            add(s);
            add(Box.createVerticalGlue());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(WHATS_NEW_CARD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.setColor(WHATS_NEW_CARD_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =========================
    // Manage Screen
    // =========================

    private JComponent buildManageScreen() {
        Color hexBg = BG_DARK;
        Color cardColor = CARD_BG;
        Color accentBlue = ACCENT_BLUE;
        Color fieldBg = INPUT_BG;
        Color borderColor = DIALOG_BORDER;

        JPanel main = new JPanel(new GridBagLayout());
        main.setOpaque(true);
        main.setBackground(hexBg);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 50, 0, 50); // Consistent margin
        main.add(buildBackRow("Manage Module"), gbc);

        JPanel createCard = buildManageCreateCard(cardColor, accentBlue, fieldBg, borderColor);
        JPanel updateCard = buildManageUpdateCard(cardColor, accentBlue, fieldBg, borderColor);

        manageCardHost.setOpaque(false);
        manageCardHost.add(createCard, MANAGE_CREATE);
        manageCardHost.add(updateCard, MANAGE_UPDATE);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        main.add(manageCardHost, gbc);

        showManageTab(MANAGE_CREATE);

        return main;
    }

    // =========================
    // Assign Screen
    // =========================

    private JComponent buildAssignScreen() {
        Color hexBg = BG_DARK;
        Color cardColor = CARD_BG;
        Color accentBlue = ACCENT_BLUE;
        Color fieldBg = INPUT_BG;
        Color borderColor = DIALOG_BORDER;

        JPanel main = new JPanel(new GridBagLayout());
        main.setOpaque(true);
        main.setBackground(hexBg);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 50, 0, 50); // Consistent margin
        main.add(buildBackRow("Assign Lecturer"), gbc);

        JPanel card = new ElegantPanel(cardColor);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(50, 60, 50, 60));
        card.setPreferredSize(new Dimension(800, 550));

        JLabel cardTitle = new JLabel("Assignment Details");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        cardTitle.setForeground(accentBlue);
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(cardTitle);
        card.add(Box.createVerticalStrut(40));

        card.add(createManageLabel("Module ID", TEXT_MUTED));
        card.add(Box.createVerticalStrut(8));

        styleManageField(assignModuleIdField, fieldBg, TEXT_WHITE, borderColor);
        assignModuleIdField.setPreferredSize(new Dimension(800, 50));
        assignModuleIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        card.add(assignModuleIdField);

        card.add(Box.createVerticalStrut(25));

        card.add(createManageLabel("Lecturer ID", TEXT_MUTED));
        card.add(Box.createVerticalStrut(8));

        styleManageField(lecturerIdField, fieldBg, TEXT_WHITE, borderColor);
        lecturerIdField.setPreferredSize(new Dimension(800, 50));
        lecturerIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        card.add(lecturerIdField);

        card.add(Box.createVerticalStrut(50));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleManagePrimaryButton(assignBtn, accentBlue);
        assignBtn.setPreferredSize(new Dimension(180, 45));

        btnRow.add(assignBtn);
        card.add(btnRow);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        main.add(card, gbc);

        return main;
    }

    private JComponent buildReportsScreen() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(20, 50, 20, 50));

        // Header (Back + Title)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        main.add(buildBackRow("Reports"), gbc);

        // Elegant Card for Content
        JPanel card = new ElegantPanel(CARD_BG);
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- Top Control Bar ---
        JPanel controls = new JPanel(new BorderLayout(15, 0));
        controls.setOpaque(false);

        // Search Box
        JPanel searchBox = new JPanel(new BorderLayout(10, 0));
        searchBox.setOpaque(false);
        JLabel lblId = createManageLabel("Module ID:", TEXT_MUTED);
        searchBox.add(lblId, BorderLayout.WEST);

        styleManageField(reportModuleIdField, INPUT_BG, TEXT_WHITE, DIALOG_BORDER);
        reportModuleIdField.setPreferredSize(new Dimension(200, 40));
        searchBox.add(reportModuleIdField, BorderLayout.CENTER);

        // Buttons
        JPanel acts = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        acts.setOpaque(false);

        styleManagePrimaryButton(viewBtn, ACCENT_BLUE);
        viewBtn.setPreferredSize(new Dimension(100, 40));

        JButton filterBtn = new JButton("Filter");
        styleManageOutlinedButton(filterBtn, ACCENT_BLUE);
        filterBtn.setPreferredSize(new Dimension(100, 40));
        filterBtn.addActionListener(e -> showFilterDialog());

        acts.add(viewBtn);
        acts.add(filterBtn);

        feedbackCountLabel.setForeground(TEXT_MUTED);
        feedbackCountLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        controls.add(searchBox, BorderLayout.WEST);
        controls.add(acts, BorderLayout.CENTER);
        controls.add(feedbackCountLabel, BorderLayout.EAST); // Feedback count on right

        card.add(controls, BorderLayout.NORTH);

        // --- Table ---
        reportSorter = new TableRowSorter<>(reportModel);
        reportTable.setRowSorter(reportSorter);
        styleReportTable(reportTable);

        JScrollPane scroll = new JScrollPane(reportTable);
        scroll.getViewport().setBackground(TABLE_BG);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);

        // Custom Scrollbar visual could be added here, but default is okay for now

        card.add(scroll, BorderLayout.CENTER);

        // Add Card to Main
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 0, 0, 0); // Gap below header
        main.add(card, gbc);

        return main;
    }

    private void styleReportTable(JTable table) {
        table.setBackground(TABLE_BG);
        table.setForeground(TEXT_WHITE);
        table.setSelectionBackground(accentWithAlpha(isLightTheme() ? 60 : 100));
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(40); // Taller rows
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(ACCENT_BLUE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, TABLE_HEADER_BORDER));
        header.setPreferredSize(new Dimension(0, 45));

        // Remove focus border
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r,
                    int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSel, false, r, c);
                if (r % 2 == 1 && !isSel) {
                    comp.setBackground(TABLE_ALT_BG); // Alternating row color
                } else if (!isSel) {
                    comp.setBackground(TABLE_BG);
                }

                setBorder(new EmptyBorder(0, 15, 0, 15)); // Cell padding
                return comp;
            }
        });
    }

    // =========================
    // Notifications Screen
    // =========================

    private JComponent buildNotificationsScreen() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setOpaque(false);
        // Remove direct border, let insets handle it or keep border if content needs
        // it?
        // Other screens use insets in GBC for header.
        // Let's stick to the pattern:
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(20, 50, 0, 50);
        main.add(buildBackRow("Notifications"), gbc);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(new EmptyBorder(0, 36, 20, 36)); // Padding for content

        // Static List matching screenshot

        // Static List matching screenshot
        list.add(new NotificationCard(
                "Student Representative Council (SRC) 2026-2027 Nominations Now Open! - For The Stude...",
                "January 19, 2026, 09:51 AM",
                "Announcements", new Color(155, 59, 104))); // Pinkish

        list.add(Box.createVerticalStrut(15));

        list.add(new NotificationCard(
                "Scheduled Power Shutdown - On Campus Residences (J1, J2, K1 and K2) on 3rd January...",
                "December 31, 2025, 11:48 PM",
                "Announcements", new Color(155, 59, 104)));

        list.add(Box.createVerticalStrut(15));

        list.add(new NotificationCard(
                "Notice: Change of APU Zone B Parking Entry and Exit Flow",
                "December 31, 2025, 04:51 PM",
                "Announcements", new Color(155, 59, 104)));

        list.add(Box.createVerticalStrut(15));

        list.add(new NotificationCard(
                "Updated version: Moodle and Results access restriction for unsettled overdue fees - effectiv...",
                "December 27, 2025, 04:18 PM",
                "Financials", new Color(150, 40, 70))); // Darker Red

        list.add(Box.createVerticalStrut(15));

        list.add(new NotificationCard(
                "Important: Your Smart Meter Balance is Running Low",
                "December 27, 2025, 01:11 PM",
                "Reminders", new Color(155, 59, 104)));

        list.add(Box.createVerticalStrut(15));

        list.add(new NotificationCard(
                "Moodle and Results access restriction for outstanding fees",
                "December 24, 2025, 05:43 PM",
                "Announcements", new Color(155, 59, 104)));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(list, BorderLayout.NORTH);

        // Wrap in scroll pane if list gets long
        JScrollPane scroll = new JScrollPane(center);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 0, 0, 0); // Gap
        main.add(scroll, gbc);

        return main;
    }

    // =========================
    // Settings Screen
    // =========================

    private JComponent buildSettingsScreen() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(20, 50, 0, 50);
        main.add(buildBackRow("Settings"), gbc);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(new EmptyBorder(20, 0, 0, 0));
        list.setAlignmentX(Component.CENTER_ALIGNMENT);

        SettingsCard card = new SettingsCard();
        list.add(card);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(list, BorderLayout.NORTH);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH; // Center content area
        gbc.insets = new Insets(20, 0, 0, 0);
        main.add(center, gbc);

        return main;
    }

    // =========================
    // Edit Profile Screen
    // =========================

    private JComponent buildEditProfileScreen() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(20, 50, 0, 50);
        main.add(buildBackRow("Edit Profile"), gbc);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(new EmptyBorder(20, 0, 0, 0));
        list.setAlignmentX(Component.CENTER_ALIGNMENT);

        EditProfileCard card = new EditProfileCard();
        list.add(card);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(list, BorderLayout.NORTH);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 0, 0, 0);
        main.add(center, gbc);

        return main;
    }

    // =========================
    // Placeholders + Events
    // =========================

    private void bindEvents() {
        // Home tiles
        manageTile.addActionListener(e -> showCard(CARD_MANAGE));
        assignTile.addActionListener(e -> showCard(CARD_ASSIGN));
        reportsTile.addActionListener(e -> showCard(CARD_REPORT));

        // Placeholder icons
        docBtn.addActionListener(e -> showCard(CARD_WHATS_NEW));
        gearBtn.addActionListener(e -> showCard(CARD_SETTINGS));
        bellBtn.addActionListener(e -> showCard(CARD_NOTIFICATIONS));

        // Manage actions
        createBtn.addActionListener(e -> onCreateModule());
        updateBtn.addActionListener(e -> onUpdateModule());
        deleteBtn.addActionListener(e -> onDeleteModule());

        // Assign action
        assignBtn.addActionListener(e -> onAssignLecturer());

        // Report action
        viewBtn.addActionListener(e -> onViewReports());
        saveProfileBtn.addActionListener(e -> onSaveProfile());
        editProfileMenuBtn.addActionListener(e -> showCard(CARD_EDIT_PROFILE));
        themeMenuBtn.addActionListener(e -> showThemeDialog());
        logoutMenuBtn.addActionListener(e -> doLogout());

        // Enter convenience
        createModuleIdField.addActionListener(e -> onCreateModule());
        createModuleNameField.addActionListener(e -> onCreateModule());
        updateModuleIdField.addActionListener(e -> onUpdateModule());
        updateModuleNameField.addActionListener(e -> onUpdateModule());
        assignModuleIdField.addActionListener(e -> onAssignLecturer());
        lecturerIdField.addActionListener(e -> onAssignLecturer());
        reportModuleIdField.addActionListener(e -> onViewReports());
        profileNameField.addActionListener(e -> onSaveProfile());
        profilePasswordField.addActionListener(e -> onSaveProfile());
    }

    private void onCreateModule() {
        final String mId = norm(createModuleIdField.getText());
        final String mName = createModuleNameField.getText() == null ? "" : createModuleNameField.getText().trim();

        try {
            requireNotEmpty(mId, "Module ID");
            requireNotEmpty(mName, "Module Name");
            ValidationUtil.enforceCsvSafety(mId, "moduleId");
            ValidationUtil.enforceCsvSafety(mName, "moduleName");
        } catch (Exception ex) {
            error(ex);
            return;
        }

        runAsync(
                () -> {
                    leader.createModule(mId, mName);
                    return null;
                },
                v -> setStatus("Module created: " + mId),
                this::error);
    }

    private void onUpdateModule() {
        final String mId = norm(updateModuleIdField.getText());
        final String mName = updateModuleNameField.getText() == null ? "" : updateModuleNameField.getText().trim();

        try {
            requireNotEmpty(mId, "Module ID");
            requireNotEmpty(mName, "Module Name");
            ValidationUtil.enforceCsvSafety(mId, "moduleId");
            ValidationUtil.enforceCsvSafety(mName, "moduleName");
        } catch (Exception ex) {
            error(ex);
            return;
        }

        runAsync(
                () -> {
                    leader.updateModule(mId, mName);
                    return null;
                },
                v -> setStatus("Module updated: " + mId),
                this::error);
    }

    private void onDeleteModule() {
        final String mId = norm(updateModuleIdField.getText());

        try {
            requireNotEmpty(mId, "Module ID");
            ValidationUtil.enforceCsvSafety(mId, "moduleId");
        } catch (Exception ex) {
            error(ex);
            return;
        }

        boolean confirm = showElegantConfirmDialog(
                "Delete Module?",
                "Are you sure you want to delete <b>" + mId
                        + "</b>?<br><br>This will permanently remove all associated assessments, marks, and feedback.",
                "Delete Module");

        if (!confirm)
            return;

        runAsync(
                () -> {
                    leader.deleteModule(mId);
                    return null;
                },
                v -> setStatus("Module deleted: " + mId),
                this::error);
    }

    private void onAssignLecturer() {
        final String mId = norm(assignModuleIdField.getText());
        final String lecId = norm(lecturerIdField.getText());

        try {
            requireNotEmpty(mId, "Module ID");
            requireNotEmpty(lecId, "Lecturer User ID");
            ValidationUtil.enforceCsvSafety(mId, "moduleId");
            ValidationUtil.enforceCsvSafety(lecId, "lecturerUserId");
        } catch (Exception ex) {
            error(ex);
            return;
        }

        runAsync(
                () -> {
                    leader.assignLecturer(mId, lecId);
                    return null;
                },
                v -> setStatus("Lecturer validated: " + lecId + " -> " + mId),
                this::error);
    }

    private void onViewReports() {
        final String mId = norm(reportModuleIdField.getText());

        try {
            requireNotEmpty(mId, "Module ID");
            ValidationUtil.enforceCsvSafety(mId, "moduleId");
        } catch (Exception ex) {
            error(ex);
            return;
        }

        runAsync(
                () -> leader.viewReports(mId),
                this::renderReportFlexible,
                this::error);
    }

    private void onSaveProfile() {
        final String newName = profileNameField.getText() == null ? "" : profileNameField.getText().trim();
        final String newPassword = new String(profilePasswordField.getPassword());

        try {
            requireNotEmpty(newName, "Name");
            ValidationUtil.enforceCsvSafety(newName, "name");
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (!ValidationUtil.isValidPassword(newPassword)) {
                    throw new IllegalArgumentException("Password must be at least 6 characters");
                }
                ValidationUtil.enforceCsvSafety(newPassword, "password");
            }
        } catch (Exception ex) {
            error(ex);
            return;
        }

        runAsync(
                () -> {
                    leader.editProfile(newName, newPassword);
                    return null;
                },
                v -> {
                    nameLabel.setText(newName.toUpperCase());
                    profilePasswordField.setText("");
                    setStatus("Profile updated");
                },
                this::error);
    }

    // =========================
    // Report Rendering (Flexible)
    // =========================

    private void showFilterDialog() {
        JDialog filterDialog = new JDialog(this, "Filter Settings", true);
        filterDialog.setUndecorated(true);
        filterDialog.setBackground(new Color(0, 0, 0, 0));

        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
                g2.setColor(DIALOG_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 26, 26);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 22, 16, 22));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel title = new JLabel("Filter Settings");
        title.setForeground(ACCENT_BLUE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        contentPanel.add(title);
        contentPanel.add(Box.createVerticalStrut(12));

        JLabel lblType = new JLabel("Assessment Type");
        lblType.setForeground(TEXT_MUTED);
        lblType.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblType.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblType);
        contentPanel.add(Box.createVerticalStrut(6));

        ButtonGroup typeGroup = new ButtonGroup();
        JRadioButton typeAll = createFilterRadio("All Types", "ALL".equalsIgnoreCase(filterType));
        JRadioButton typeAssignment = createFilterRadio("Assignment", "ASSIGNMENT".equalsIgnoreCase(filterType));
        JRadioButton typeFinal = createFilterRadio("Final Exam", "FINALEXAM".equalsIgnoreCase(filterType));
        JRadioButton typeQuiz = createFilterRadio("Quiz", "QUIZ".equalsIgnoreCase(filterType));
        JRadioButton typeProject = createFilterRadio("Project", "PROJECT".equalsIgnoreCase(filterType));

        typeGroup.add(typeAll);
        typeGroup.add(typeAssignment);
        typeGroup.add(typeFinal);
        typeGroup.add(typeQuiz);
        typeGroup.add(typeProject);

        contentPanel.add(buildFilterRow("All Types", typeAll));
        contentPanel.add(buildFilterRow("Assignment", typeAssignment));
        contentPanel.add(buildFilterRow("Final Exam", typeFinal));
        contentPanel.add(buildFilterRow("Quiz", typeQuiz));
        contentPanel.add(buildFilterRow("Project", typeProject));

        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createSeparator());
        contentPanel.add(Box.createVerticalStrut(10));

        JLabel lblPerf = new JLabel("Performance");
        lblPerf.setForeground(TEXT_MUTED);
        lblPerf.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPerf.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblPerf);
        contentPanel.add(Box.createVerticalStrut(6));

        ButtonGroup perfGroup = new ButtonGroup();
        JRadioButton perfHigh = createFilterRadio("Top Performers (>70%)", "HIGH".equalsIgnoreCase(filterPerformance));
        JRadioButton perfLow = createFilterRadio("Needs Review (<40%)", "LOW".equalsIgnoreCase(filterPerformance));
        JRadioButton perfAll = createFilterRadio("All Scores", "ALL".equalsIgnoreCase(filterPerformance));

        perfGroup.add(perfHigh);
        perfGroup.add(perfLow);
        perfGroup.add(perfAll);

        contentPanel.add(buildFilterRow("Top Performers (>70%)", perfHigh));
        contentPanel.add(buildFilterRow("Needs Review (<40%)", perfLow));
        contentPanel.add(buildFilterRow("All Scores", perfAll));

        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createSeparator());
        contentPanel.add(Box.createVerticalStrut(10));

        JCheckBox chkSub = new JCheckBox();
        chkSub.setOpaque(false);
        chkSub.setSelected(filterHasSubmissions);
        chkSub.setForeground(TEXT_WHITE);

        contentPanel.add(buildCheckboxRow("Submissions > 0", chkSub));

        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(6, 0, 0, 0));

        JButton btnClear = new JButton("Clear Filters");
        btnClear.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnClear.setForeground(ERROR_RED);
        btnClear.setContentAreaFilled(false);
        btnClear.setBorderPainted(false);
        btnClear.setFocusPainted(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> {
            filterType = "ALL";
            filterPerformance = "ALL";
            filterHasSubmissions = false;
            applyReportFilter();
            filterDialog.dispose();
        });

        JButton btnApply = new JButton("Apply Filters");
        stylePrimaryFilledButton(btnApply);
        btnApply.addActionListener(e -> {
            if (typeAssignment.isSelected())
                filterType = "Assignment";
            else if (typeFinal.isSelected())
                filterType = "FinalExam";
            else if (typeQuiz.isSelected())
                filterType = "Quiz";
            else if (typeProject.isSelected())
                filterType = "Project";
            else
                filterType = "ALL";

            if (perfHigh.isSelected())
                filterPerformance = "HIGH";
            else if (perfLow.isSelected())
                filterPerformance = "LOW";
            else
                filterPerformance = "ALL";

            filterHasSubmissions = chkSub.isSelected();
            applyReportFilter();
            filterDialog.dispose();
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnApply);

        btnPanel.add(btnClear, BorderLayout.WEST);
        btnPanel.add(right, BorderLayout.EAST);

        card.add(contentPanel, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.SOUTH);

        filterDialog.setContentPane(card);
        filterDialog.pack();
        filterDialog.setSize(520, 420);
        filterDialog.setLocationRelativeTo(this);
        filterDialog.setVisible(true);
    }

    private JRadioButton createFilterRadio(String text, boolean selected) {
        JRadioButton rb = new JRadioButton();
        rb.setSelected(selected);
        rb.setForeground(TEXT_WHITE);
        rb.setOpaque(false);
        return rb;
    }

    private JComponent buildFilterRow(String text, JRadioButton radio) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel label = new JLabel(text);
        label.setForeground(TEXT_WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));

        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(label, BorderLayout.WEST);
        row.add(radio, BorderLayout.EAST);
        return row;
    }

    private JComponent buildCheckboxRow(String text, JCheckBox box) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel label = new JLabel(text);
        label.setForeground(TEXT_WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));

        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(label, BorderLayout.WEST);
        row.add(box, BorderLayout.EAST);
        return row;
    }

    private JComponent createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(DIALOG_BORDER);
        sep.setOpaque(false);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private void applyReportFilter() {
        if (reportSorter == null)
            return;

        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();

        if (!"ALL".equalsIgnoreCase(filterType)) {
            filters.add(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String type = String.valueOf(entry.getValue(1));
                    return filterType.equalsIgnoreCase(type);
                }
            });
        }

        if ("HIGH".equalsIgnoreCase(filterPerformance)) {
            filters.add(new RowFilter<DefaultTableModel, Integer>() {

                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    double avg = parseDouble(entry.getValue(3));
                    return avg > 70.0;
                }
            });
        } else if ("LOW".equalsIgnoreCase(filterPerformance)) {
            filters.add(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    double avg = parseDouble(entry.getValue(3));
                    return avg < 40.0;
                }
            });
        }

        if (filterHasSubmissions)

        {
            filters.add(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    int subs = parseInt(entry.getValue(4));
                    return subs > 0;
                }
            });
        }

        if (filters.isEmpty()) {
            reportSorter.setRowFilter(null);
        } else {
            reportSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private static double parseDouble(Object v) {
        if (v instanceof Number)
            return ((Number) v).doubleValue();
        if (v == null)
            return 0.0;
        try {
            return Double.parseDouble(String.valueOf(v).trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static int parseInt(Object v) {
        if (v instanceof Number)
            return ((Number) v).intValue();
        if (v == null)
            return 0;
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void renderReportFlexible(Object reportObj) {
        reportModel.setRowCount(0);
        feedbackCountLabel.setText("Feedback: 0");

        if (reportObj == null) {
            setStatus("No report returned.");
            return;
        }

        if (reportObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> report = (Map<String, Object>) reportObj;

            Object rowsObj = report.get("rows");
            if (rowsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object[]> rows = (List<Object[]>) rowsObj;
                for (Object[] r : rows)
                    reportModel.addRow(r);
            }

            int fb = asInt(report.get("feedbackCount"));
            feedbackCountLabel.setText("Feedback: " + fb);

            setStatus("Report loaded for " + report.get("moduleId") + " - " + report.get("moduleName"));
            applyReportFilter();
            return;
        }

        try {
            String moduleId = readStringFieldOrGetter(reportObj, "moduleId");
            String moduleName = readStringFieldOrGetter(reportObj, "moduleName");
            int fb = readIntFieldOrGetter(reportObj, "feedbackCount");

            Object assessmentsObj = readFieldOrGetter(reportObj, "assessments");
            if (assessmentsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> assessments = (List<Object>) assessmentsObj;
                for (Object s : assessments) {
                    String aid = readStringFieldOrGetter(s, "assessmentId");
                    String type = readStringFieldOrGetter(s, "type");
                    int max = readIntFieldOrGetter(s, "maxMarks");
                    double avg = readDoubleFieldOrGetter(s, "avg");
                    int subs = readIntFieldOrGetter(s, "submissions");

                    reportModel.addRow(new Object[] {
                            aid, type, max, String.format("%.2f", avg), subs
                    });
                }
            }

            feedbackCountLabel.setText("Feedback: " + fb);
            setStatus("Report loaded for " + moduleId + " - " + moduleName);
            applyReportFilter();
        } catch (Exception ex) {
            error(new IllegalStateException(
                    "Unsupported report type returned by viewReports(). " +
                            "Return a Map{rows,feedbackCount,moduleId,moduleName} OR a LeaderReport DTO.",
                    ex));
        }
    }

    private static Object readFieldOrGetter(Object obj, String name) throws Exception {
        String g1 = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        try {
            Method m = obj.getClass().getMethod(g1);
            return m.invoke(obj);
        } catch (NoSuchMethodException ignored) {
        }

        Field f = obj.getClass().getField(name);
        return f.get(obj);
    }

    private static String readStringFieldOrGetter(Object obj, String name) throws Exception {
        Object v = readFieldOrGetter(obj, name);
        return v == null ? "" : String.valueOf(v);
    }

    private static int readIntFieldOrGetter(Object obj, String name) throws Exception {
        Object v = readFieldOrGetter(obj, name);
        return asInt(v);
    }

    private static double readDoubleFieldOrGetter(Object obj, String name) throws Exception {
        Object v = readFieldOrGetter(obj, name);
        if (v instanceof Number)
            return ((Number) v).doubleValue();
        if (v == null)
            return 0.0;
        try {
            return Double.parseDouble(String.valueOf(v).trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static int asInt(Object v) {
        if (v instanceof Number)
            return ((Number) v).intValue();
        if (v == null)
            return 0;
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    // =========================
    // Navigation
    // =========================

    private JComponent buildBackRow(String title) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JButton back = new JButton("   Back   ");
        styleManageOutlinedButton(back, TEXT_MUTED);
        // Custom override for Back button specific look if needed, or keep generic
        // styled
        back.setBorder(new javax.swing.border.LineBorder(TEXT_MUTED, 1, true));
        back.setFont(new Font("SansSerif", Font.BOLD, 12));
        back.setPreferredSize(new Dimension(80, 32));

        back.addActionListener(e -> showCard(CARD_HOME));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 22));
        t.setForeground(ACCENT_BLUE);
        t.setBorder(new EmptyBorder(0, 20, 0, 0));

        row.add(back, BorderLayout.WEST);
        row.add(t, BorderLayout.CENTER);
        return row;
    }

    private void showCard(String key) {
        contentPanel.show(key);
        if (CARD_MANAGE.equals(key)) {
            showManageTab(MANAGE_CREATE);
        }
    }

    private void doLogout() {
        if (busy)
            return;
        dispose();
        onLogout.run(); // aligns with main.Main: new LeaderFrame(leader, () -> System.exit(0))
    }

    // =========================
    // Async + Busy State
    // =========================

    private <T> void runAsync(Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onError) {
        setBusy(true);
        new SwingWorker<T, Void>() {
            private Exception failure;
            private T result;

            @Override
            protected T doInBackground() {
                try {
                    result = task.call();
                    return result;
                } catch (Exception ex) {
                    failure = ex;
                    return null;
                }
            }

            @Override
            protected void done() {
                setBusy(false);
                if (failure != null)
                    onError.accept(failure);
                else
                    onSuccess.accept(result);
            }
        }.execute();
    }

    private void setBusy(boolean b) {
        busy = b;

        createBtn.setEnabled(!b);
        updateBtn.setEnabled(!b);
        assignBtn.setEnabled(!b);
        viewBtn.setEnabled(!b);
        saveProfileBtn.setEnabled(!b);

        if (manageTile != null)
            manageTile.setEnabled(!b);
        if (assignTile != null)
            assignTile.setEnabled(!b);
        if (reportsTile != null)
            reportsTile.setEnabled(!b);

        if (docBtn != null)
            docBtn.setEnabled(!b);
        if (gearBtn != null)
            gearBtn.setEnabled(!b);
        if (bellBtn != null)
            bellBtn.setEnabled(!b);
        if (logoutBtn != null)
            logoutBtn.setEnabled(!b);

        setStatus(b ? "Working..." : "Ready");
    }

    // =========================
    // Styling Helpers
    // =========================

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_MUTED);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        return l;
    }

    private JLabel createManageLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styleManageField(JTextField f, Color bg, Color fg, Color border) {
        f.setBackground(bg);
        f.setForeground(fg);
        f.setCaretColor(fg);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(border, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setPreferredSize(new Dimension(320, 40));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private void styleManagePrimaryButton(JButton b, Color accent) {
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(false);
        b.setUI(new AnimatedButtonUI(accent));
    }

    private JButton createManageTabButton(String text, boolean isCreateTab) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(TEXT_MUTED);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(false);
        b.putClientProperty("tabActive", Boolean.FALSE);
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton ab = (AbstractButton) c;
                Boolean active = (Boolean) ab.getClientProperty("tabActive");
                boolean isActive = active != null && active;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isActive) {
                    g2.setColor(ACCENT_BLUE);
                    g2.fillRoundRect(0, 0, ab.getWidth(), ab.getHeight(), 10, 10);
                }
                g2.dispose();
                super.paint(g, c);
            }
        });
        b.addChangeListener(e -> b.repaint());
        if (isCreateTab)
            manageTabCreateBtns.add(b);
        else
            manageTabUpdateBtns.add(b);
        return b;
    }

    private void showManageTab(String key) {
        manageCardLayout.show(manageCardHost, key);
        boolean createActive = MANAGE_CREATE.equals(key);
        for (JButton b : manageTabCreateBtns) {
            b.putClientProperty("tabActive", createActive);
            b.setForeground(createActive ? Color.WHITE : TEXT_MUTED);
            b.repaint();
        }
        for (JButton b : manageTabUpdateBtns) {
            b.putClientProperty("tabActive", !createActive);
            b.setForeground(!createActive ? Color.WHITE : TEXT_MUTED);
            b.repaint();
        }
    }

    private JPanel buildManageCreateCard(Color cardColor, Color accentBlue, Color fieldBg, Color borderColor) {
        JPanel card = new ElegantPanel(cardColor);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(50, 60, 50, 60));
        card.setPreferredSize(new Dimension(800, 550));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JPanel titleRow = new JPanel(new BorderLayout(10, 0));
        titleRow.setOpaque(false);

        JLabel cardTitle = new JLabel("Create Module");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        cardTitle.setForeground(accentBlue);
        titleRow.add(cardTitle, BorderLayout.WEST);

        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(titleRow, gbc);

        // Row 2: Label + Tabs
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);
        row2.add(createManageLabel("Module ID", TEXT_MUTED), BorderLayout.WEST);

        JPanel tabRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        tabRow.setOpaque(false);
        JButton createTab = createManageTabButton("Create", true);
        JButton updateTab = createManageTabButton("Update", false);
        createTab.addActionListener(e -> showManageTab(MANAGE_CREATE));
        updateTab.addActionListener(e -> showManageTab(MANAGE_UPDATE));
        tabRow.add(createTab);
        tabRow.add(updateTab);

        row2.add(tabRow, BorderLayout.EAST);

        gbc.gridy = 1;
        gbc.insets = new Insets(25, 10, 5, 10); // More space before input area
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(row2, gbc);

        styleManageField(createModuleIdField, fieldBg, TEXT_WHITE, borderColor);
        createModuleIdField.setPreferredSize(new Dimension(400, 50)); // Taller input

        gbc.gridy = 2;
        gbc.insets = new Insets(5, 10, 20, 10);
        gbc.anchor = GridBagConstraints.WEST;
        card.add(createModuleIdField, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        card.add(createManageLabel("Module Name", TEXT_MUTED), gbc);

        styleManageField(createModuleNameField, fieldBg, TEXT_WHITE, borderColor);
        createModuleNameField.setPreferredSize(new Dimension(400, 50)); // Taller input

        gbc.gridy = 4;
        gbc.insets = new Insets(5, 10, 30, 10);
        gbc.anchor = GridBagConstraints.WEST;
        card.add(createModuleNameField, gbc);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);
        styleManagePrimaryButton(createBtn, accentBlue);
        createBtn.setPreferredSize(new Dimension(160, 45)); // Bigger button
        footer.add(createBtn);

        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.fill = GridBagConstraints.NONE;
        card.add(footer, gbc);

        return card;
    }

    private JPanel buildManageUpdateCard(Color cardColor, Color accentBlue, Color fieldBg, Color borderColor) {
        JPanel card = new ElegantPanel(cardColor);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(50, 60, 50, 60));
        card.setPreferredSize(new Dimension(800, 550));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JPanel titleRow = new JPanel(new BorderLayout(10, 0));
        titleRow.setOpaque(false);

        JLabel cardTitle = new JLabel("Update Module");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        cardTitle.setForeground(accentBlue);
        titleRow.add(cardTitle, BorderLayout.WEST);

        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(titleRow, gbc);

        // Row 2: Label + Tabs
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);
        row2.add(createManageLabel("Module ID", TEXT_MUTED), BorderLayout.WEST);

        JPanel tabRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        tabRow.setOpaque(false);
        JButton createTab = createManageTabButton("Create", true);
        JButton updateTab = createManageTabButton("Update", false);
        createTab.addActionListener(e -> showManageTab(MANAGE_CREATE));
        updateTab.addActionListener(e -> showManageTab(MANAGE_UPDATE));
        tabRow.add(createTab);
        tabRow.add(updateTab);

        row2.add(tabRow, BorderLayout.EAST);

        gbc.gridy = 1;
        gbc.insets = new Insets(25, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(row2, gbc);

        styleManageField(updateModuleIdField, fieldBg, TEXT_WHITE, borderColor);
        updateModuleIdField.setPreferredSize(new Dimension(400, 50)); // Taller input

        gbc.gridy = 2;
        gbc.insets = new Insets(5, 10, 20, 10);
        gbc.anchor = GridBagConstraints.WEST;
        card.add(updateModuleIdField, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        card.add(createManageLabel("Module Name", TEXT_MUTED), gbc);

        styleManageField(updateModuleNameField, fieldBg, TEXT_WHITE, borderColor);
        updateModuleNameField.setPreferredSize(new Dimension(400, 50)); // Taller input

        gbc.gridy = 4;
        gbc.insets = new Insets(5, 10, 30, 10);
        gbc.anchor = GridBagConstraints.WEST;
        card.add(updateModuleNameField, gbc);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);

        styleManageOutlinedButton(deleteBtn, ERROR_RED);
        deleteBtn.setPreferredSize(new Dimension(140, 45));

        styleManagePrimaryButton(updateBtn, accentBlue);
        updateBtn.setPreferredSize(new Dimension(160, 45));

        footer.add(deleteBtn);
        footer.add(updateBtn);

        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.fill = GridBagConstraints.NONE;
        card.add(footer, gbc);

        return card;
    }

    private void styleManageOutlinedButton(JButton b, Color accent) {
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(accent);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(false);
        b.setUI(new AnimatedButtonUI(null, accent)); // Null bg = outlined
    }

    private JComboBox<String> createStyledComboBoxDark(String[] items, Color bg, Color border, Color accent) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setBackground(bg);
        combo.setForeground(TEXT_WHITE);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        combo.setPreferredSize(new Dimension(320, 40));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setBorder(new javax.swing.border.LineBorder(border, 1, true));
        combo.setFocusable(true);
        combo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setOpaque(true);
                l.setForeground(TEXT_WHITE);
                l.setBackground(isSelected ? TABLE_ALT_BG : bg);
                l.setBorder(new EmptyBorder(6, 10, 6, 10));
                return l;
            }
        });
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton("v");
                b.setFont(new Font("SansSerif", Font.PLAIN, 12));
                b.setForeground(Color.WHITE);
                b.setBackground(bg);
                b.setBorder(new EmptyBorder(0, 6, 0, 6));
                b.setFocusPainted(false);
                b.setContentAreaFilled(false);
                b.setOpaque(false);
                return b;
            }
        });
        combo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                combo.setBorder(new javax.swing.border.LineBorder(accent, 1, true));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                combo.setBorder(new javax.swing.border.LineBorder(border, 1, true));
            }
        });
        return combo;
    }

    private JTextField styleField(JTextField f) {
        f.setBackground(INPUT_BG);
        f.setForeground(TEXT_WHITE);
        f.setCaretColor(TEXT_WHITE);
        f.setBorder(new EmptyBorder(10, 12, 10, 12));
        f.setOpaque(true);
        return f;
    }

    private JTextField styleReadOnlyField(JTextField f) {
        styleField(f);
        f.setEditable(false);
        f.setFocusable(false);
        f.setForeground(TEXT_MUTED);
        return f;
    }

    private void stylePrimaryFilledButton(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(false);
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.addChangeListener(e -> b.repaint());

        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton ab = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT_BLUE);
                g2.fillRoundRect(0, 0, ab.getWidth(), ab.getHeight(), 10, 10);
                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    private static JButton createIcon(String icon) {
        JButton b = new HoverIconButton(icon);
        b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        b.setForeground(TEXT_MUTED);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(2, 6, 2, 6));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static class HoverIconButton extends JButton {
        private float hoverAlpha = 0.0f;
        private float hoverTarget = 0.0f;
        private final javax.swing.Timer hoverTimer;

        HoverIconButton(String text) {
            super(text);
            setOpaque(false);
            java.awt.event.MouseAdapter hoverHandler = new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    setHoverTarget(1.0f);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!isPointerInside()) {
                        setHoverTarget(0.0f);
                    }
                }
            };
            addMouseListener(hoverHandler);
            hoverTimer = new javax.swing.Timer(16, e -> {
                if (hoverAlpha < hoverTarget) {
                    hoverAlpha = Math.min(hoverTarget, hoverAlpha + 0.12f);
                } else if (hoverAlpha > hoverTarget) {
                    hoverAlpha = Math.max(hoverTarget, hoverAlpha - 0.12f);
                }
                setForeground(AnimationUtils.blend(TEXT_MUTED, HEADER_ICON_HOVER, hoverAlpha));
                repaint();
                if (hoverAlpha == hoverTarget) {
                    ((javax.swing.Timer) e.getSource()).stop();
                }
            });
            hoverTimer.setRepeats(true);
        }

        private boolean isPointerInside() {
            Point p = getMousePosition();
            return p != null && contains(p);
        }

        private void setHoverTarget(float target) {
            hoverTarget = target;
            if (!hoverTimer.isRunning()) {
                hoverTimer.start();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    private class TileButton extends JButton {
        private float hoverAlpha = 0.0f;
        private float hoverTarget = 0.0f;
        private final javax.swing.Timer hoverTimer;
        private final BufferedImage icon;

        public TileButton(String text, BufferedImage icon) {
            this.icon = icon;
            setText(text);
            setFont(new Font("SansSerif", Font.BOLD, 16));
            setForeground(TILE_TEXT);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setBorder(new EmptyBorder(20, 25, 20, 25));
            setHorizontalAlignment(SwingConstants.LEFT);
            setVerticalAlignment(SwingConstants.TOP);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            java.awt.event.MouseAdapter hoverHandler = new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    setHoverTarget(1.0f);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!isPointerInside()) {
                        setHoverTarget(0.0f);
                    }
                }
            };
            addMouseListener(hoverHandler);

            hoverTimer = new javax.swing.Timer(16, e -> {
                if (hoverAlpha < hoverTarget) {
                    hoverAlpha = Math.min(hoverTarget, hoverAlpha + 0.08f);
                } else if (hoverAlpha > hoverTarget) {
                    hoverAlpha = Math.max(hoverTarget, hoverAlpha - 0.08f);
                }
                setForeground(AnimationUtils.blend(TILE_TEXT, TILE_TEXT_HOVER, hoverAlpha));
                repaint();
                if (hoverAlpha == hoverTarget) {
                    ((javax.swing.Timer) e.getSource()).stop();
                }
            });
            hoverTimer.setRepeats(true);
        }

        private boolean isPointerInside() {
            Point p = getMousePosition();
            return p != null && contains(p);
        }

        private void setHoverTarget(float target) {
            hoverTarget = target;
            if (!hoverTimer.isRunning()) {
                hoverTimer.start();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            float eased = AnimationUtils.easeOutCubic(hoverAlpha);
            Color fill = AnimationUtils.blend(ACCENT_BLUE, TILE_HOVER, eased);
            int lift = Math.round(HOVER_LIFT_PX * eased);
            g2.translate(0, -lift);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.setColor(new Color(255, 255, 255, (int) (22 + (18 * eased))));
            int glowSize = getHeight() + 30;
            g2.fillOval(getWidth() - (glowSize / 2), -glowSize / 3, glowSize, glowSize);
            if (icon != null) {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                float scale = 0.72f + (0.06f * eased); // mimic 80% -> 85% hover grow
                int maxH = Math.round(getHeight() * scale);
                int maxW = Math.round(getWidth() * scale);
                int imgW = icon.getWidth();
                int imgH = icon.getHeight();
                if (imgW > 0 && imgH > 0) {
                    float ratio = Math.min((float) maxW / imgW, (float) maxH / imgH);
                    int drawW = Math.round(imgW * ratio);
                    int drawH = Math.round(imgH * ratio);
                    int x = getWidth() - drawW - 20;
                    int y = (getHeight() - drawH) / 2;
                    g2.drawImage(icon, x, y, drawW, drawH, null);
                }
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class NotificationCard extends JPanel {

        NotificationCard(String title, String time, String tag, Color tagColor) {

            setOpaque(false);
            setLayout(new BorderLayout(15, 10));
            setBorder(new EmptyBorder(15, 20, 15, 20));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setMaximumSize(new Dimension(800, 110));
            setPreferredSize(new Dimension(800, 110));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            titleLabel.setForeground(TEXT_WHITE);

            JLabel timeLabel = new JLabel(time);
            timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            timeLabel.setForeground(TEXT_MUTED);

            JLabel tagLabel = new JLabel(tag);
            tagLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            tagLabel.setForeground(Color.WHITE);

            // Custom pill painting
            JPanel pillContainer = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(tagColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pillContainer.setOpaque(false);
            pillContainer.setLayout(new BorderLayout());
            pillContainer.add(tagLabel);
            pillContainer.setBorder(new EmptyBorder(2, 8, 2, 8));

            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.add(titleLabel);
            content.add(Box.createVerticalStrut(6));
            content.add(timeLabel);
            content.add(Box.createVerticalStrut(10));

            JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            tagRow.setOpaque(false);
            tagRow.add(pillContainer);
            content.add(tagRow);

            JLabel chevron = new JLabel(">");
            chevron.setForeground(TEXT_MUTED);
            chevron.setFont(new Font("SansSerif", Font.BOLD, 18));

            add(content, BorderLayout.CENTER);
            add(chevron, BorderLayout.EAST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Card BG
            g2.setColor(NOTIF_CARD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            // Let's draw a nice strip with rounded left corners
            g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, 6, getHeight(), 16, 16));
            g2.fillRect(0, 0, 6, getHeight());
            g2.setClip(null);

            // Outer Border
            g2.setColor(NOTIF_CARD_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class SettingsCard extends JPanel {
        SettingsCard() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(18, 22, 18, 22));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setMaximumSize(new Dimension(760, 220));
            setPreferredSize(new Dimension(760, 220));

            JLabel title = new JLabel("Menu");
            title.setFont(new Font("SansSerif", Font.BOLD, 18));
            title.setForeground(ACCENT_BLUE);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            title.setHorizontalAlignment(SwingConstants.CENTER);
            add(title);
            add(Box.createVerticalStrut(14));

            add(new SettingsMenuRow(editProfileMenuBtn));
            add(Box.createVerticalStrut(12));
            add(new SettingsMenuRow(themeMenuBtn));
            add(Box.createVerticalStrut(12));
            add(new SettingsMenuRow(logoutMenuBtn));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(SETTINGS_CARD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.setColor(SETTINGS_CARD_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }

    }

    private class EditProfileCard extends JPanel {
        EditProfileCard() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(16, 18, 16, 18));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setMaximumSize(new Dimension(720, 380));
            setPreferredSize(new Dimension(720, 380));

            JLabel title = new JLabel("Edit Profile");
            title.setFont(new Font("SansSerif", Font.BOLD, 16));
            title.setForeground(TEXT_WHITE);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            title.setHorizontalAlignment(SwingConstants.CENTER);
            add(title);
            add(Box.createVerticalStrut(12));

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            GridBagConstraints c = gbc();
            c.insets = new Insets(8, 10, 8, 10);
            form.setAlignmentX(Component.CENTER_ALIGNMENT);

            form.add(label("User ID"), c0(c, 0, 0));
            profileUserIdField.setText(safe(leader.getUserId()));
            form.add(styleReadOnlyField(profileUserIdField), c1(c, 1, 0));

            form.add(label("Role"), c0(c, 0, 1));
            profileRoleField.setText("LEADER");
            form.add(styleReadOnlyField(profileRoleField), c1(c, 1, 1));

            form.add(label("Full Name"), c0(c, 0, 2));
            profileNameField.setText(safe(leader.getFullName()));
            form.add(styleField(profileNameField), c1(c, 1, 2));

            form.add(label("New Password (optional)"), c0(c, 0, 3));
            form.add(styleField(profilePasswordField), c1(c, 1, 3));

            add(form);
            add(Box.createVerticalStrut(14));

            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);
            JLabel note = new JLabel("*Academic leader can only change full name and password");
            note.setFont(new Font("SansSerif", Font.PLAIN, 11));
            note.setForeground(ERROR_RED);
            footer.add(note, BorderLayout.WEST);

            stylePrimaryFilledButton(saveProfileBtn);
            JButton cancelBtn = createDialogTextButton("Cancel");
            cancelBtn.setForeground(ERROR_RED);
            cancelBtn.addActionListener(e -> showCard(CARD_SETTINGS));

            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
            actionPanel.setOpaque(false);
            actionPanel.add(saveProfileBtn);
            actionPanel.add(cancelBtn);
            footer.add(actionPanel, BorderLayout.EAST);

            add(footer);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(SETTINGS_CARD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }

    }

    private class SettingsMenuRow extends JPanel {
        private final JButton button;
        private final JLabel chevron;
        private float hoverAlpha = 0.0f;
        private float hoverTarget = 0.0f;
        private final javax.swing.Timer hoverTimer;

        SettingsMenuRow(JButton button) {
            this.button = button;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(10, 16, 10, 16));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setMaximumSize(new Dimension(680, 52));
            setPreferredSize(new Dimension(680, 52));

            button.setFont(new Font("SansSerif", Font.BOLD, 13));
            button.setForeground(TEXT_WHITE);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMargin(new Insets(0, 0, 0, 0));

            chevron = new JLabel(">");
            chevron.setForeground(TEXT_MUTED);
            chevron.setFont(new Font("SansSerif", Font.BOLD, 16));
            chevron.setHorizontalAlignment(SwingConstants.CENTER);
            chevron.setPreferredSize(new Dimension(24, 24));

            add(button, BorderLayout.CENTER);
            add(chevron, BorderLayout.EAST);

            java.awt.event.MouseAdapter clickForwarder = new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (button.isEnabled())
                        button.doClick();
                }
            };
            addMouseListener(clickForwarder);
            chevron.addMouseListener(clickForwarder);

            java.awt.event.MouseAdapter hoverHandler = new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    setHoverTarget(1.0f);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!isPointerInside()) {
                        setHoverTarget(0.0f);
                    }
                }
            };
            addMouseListener(hoverHandler);
            button.addMouseListener(hoverHandler);
            chevron.addMouseListener(hoverHandler);

            hoverTimer = new javax.swing.Timer(16, e -> {
                if (hoverAlpha < hoverTarget) {
                    hoverAlpha = Math.min(hoverTarget, hoverAlpha + 0.12f);
                } else if (hoverAlpha > hoverTarget) {
                    hoverAlpha = Math.max(hoverTarget, hoverAlpha - 0.12f);
                }
                updateHoverColors();
                repaint();
                if (hoverAlpha == hoverTarget) {
                    ((javax.swing.Timer) e.getSource()).stop();
                }
            });
            hoverTimer.setRepeats(true);
        }

        private boolean isPointerInside() {
            Point p = getMousePosition();
            return p != null && contains(p);
        }

        private void setHoverTarget(float target) {
            hoverTarget = target;
            if (!hoverTimer.isRunning()) {
                hoverTimer.start();
            }
        }

        private void updateHoverColors() {
            button.setForeground(AnimationUtils.blend(TEXT_WHITE, MENU_TEXT_HOVER, hoverAlpha));
            chevron.setForeground(AnimationUtils.blend(TEXT_MUTED, TEXT_WHITE, hoverAlpha));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = AnimationUtils.blend(MENU_ROW_BG, MENU_ROW_BG_HOVER, hoverAlpha);
            Color border = AnimationUtils.blend(MENU_ROW_BORDER, MENU_ROW_BORDER_HOVER, hoverAlpha);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class SchedulePanel extends JPanel {
        public SchedulePanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(DASH_CARD_WIDTH, 420));
            setMaximumSize(new Dimension(DASH_CARD_WIDTH, 420));
            setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(DASH_CARD_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), DASH_CARD_RADIUS, DASH_CARD_RADIUS);
            g2.setColor(DASH_CARD_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, DASH_CARD_RADIUS, DASH_CARD_RADIUS);

            int left = 24;
            int top = 24;

            g2.setColor(ACCENT_BLUE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            g2.drawString("My Schedule", left, top + 18);

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(TEXT_MUTED);
            g2.drawString("TODAY", left, top + 46);
            g2.setColor(ACCENT_BLUE);
            g2.fillRoundRect(left, top + 52, 70, 3, 3, 3);

            int contentTop = top + 68;
            int contentHeight = getHeight() - contentTop - 24;
            int contentWidth = getWidth() - (left * 2);
            g2.setColor(SUBCARD_BG);
            g2.fillRoundRect(left, contentTop, contentWidth, contentHeight, 16, 16);
            g2.setColor(SUBCARD_BORDER);
            g2.drawRoundRect(left, contentTop, contentWidth - 1, contentHeight - 1, 16, 16);

            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            String msg = "The list is empty!";
            FontMetrics fm = g2.getFontMetrics();
            int textHeight = fm.getAscent();

            if (noItemsImage != null) {
                int maxImgWidth = Math.min(220, contentWidth - 80);
                int maxImgHeight = 180;
                int imgW = noItemsImage.getWidth();
                int imgH = noItemsImage.getHeight();
                float scale = Math.min((float) maxImgWidth / imgW, (float) maxImgHeight / imgH);
                scale = Math.min(scale, 1.0f);
                int drawW = Math.round(imgW * scale);
                int drawH = Math.round(imgH * scale);
                int gap = 12;
                int blockHeight = drawH + gap + textHeight;
                int blockTop = contentTop + (contentHeight - blockHeight) / 2;
                int imgX = left + (contentWidth - drawW) / 2;
                int imgY = blockTop;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(noItemsImage, imgX, imgY, drawW, drawH, null);
                int msgX = left + (contentWidth - fm.stringWidth(msg)) / 2;
                int msgY = imgY + drawH + gap + textHeight;
                g2.drawString(msg, msgX, msgY);
            } else {
                int msgX = left + (contentWidth - fm.stringWidth(msg)) / 2;
                int msgY = contentTop + (contentHeight / 2);
                g2.drawString(msg, msgX, msgY);
            }
            g2.dispose();
        }
    }

    private static GridBagConstraints gbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        return c;
    }

    private static GridBagConstraints c0(GridBagConstraints base, int x, int y) {
        GridBagConstraints c = (GridBagConstraints) base.clone();
        c.gridx = x;
        c.gridy = y;
        c.weightx = 0.0;
        return c;
    }

    private static GridBagConstraints c1(GridBagConstraints base, int x, int y) {
        GridBagConstraints c = (GridBagConstraints) base.clone();
        c.gridx = x;
        c.gridy = y;
        c.weightx = 1.0;
        return c;
    }

    private void setStatus(String msg) {
        if (msg == null || msg.equals("Ready"))
            return; // Don't show "Ready" toasts
        GlassToast.show(this, msg);
    }

    private void info(String msg) {
        GlassToast.show(this, msg);
    }

    // ========================================================================================================
    // FLUIDITY & ANIMATION ENGINE
    // ========================================================================================================

    /**
     * Replaces CardLayout with a transition engine.
     * Supports: Fade-in, Slide-up, and Layered transitions.
     */
    private static class FluidTransitionPanel extends JLayeredPane {
        private final Map<String, Component> cards = new HashMap<>();
        private Component currentCard = null;

        // Animation State
        private boolean isAnimating = false;
        private java.awt.image.BufferedImage imgPrev = null;
        private java.awt.image.BufferedImage imgNext = null;
        private float animProgress = 0f;

        public FluidTransitionPanel() {
            setLayout(null); // Absolute positioning for transitions
        }

        public void addCard(String key, Component card) {
            cards.put(key, card);
            card.setVisible(false);
            add(card, JLayeredPane.DEFAULT_LAYER);
        }

        public void show(String key) {
            Component next = cards.get(key);
            if (next == null || next == currentCard || isAnimating)
                return;

            // First run?
            if (currentCard == null) {
                currentCard = next;
                currentCard.setVisible(true);
                doLayout();
                revalidate();
                repaint();
                return;
            }

            // Capture frames for transition
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0)
                return; // Cannot animate zero size

            Component prev = currentCard;

            // 1. Capture Previous State
            imgPrev = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D gP = imgPrev.createGraphics();
            prev.paint(gP);
            gP.dispose();

            // 2. Prepare Next State (Layout & Capture)
            next.setVisible(true);
            next.setBounds(0, 0, w, h);
            next.validate(); // Ensure layout is clean

            imgNext = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D gN = imgNext.createGraphics();
            next.paint(gN);
            gN.dispose();

            // 3. Setup Animation Mode
            isAnimating = true;
            animProgress = 0f;

            // Hide real components during painting
            prev.setVisible(false);
            next.setVisible(false);

            currentCard = next; // Logic switch

            // 4. Run Animation
            AnimationUtils.animate(progress -> {
                animProgress = AnimationUtils.easeOutCubic(progress);
                repaint();
            }, 350, () -> {
                isAnimating = false;
                imgPrev = null;
                imgNext = null;
                next.setVisible(true);
                repaint();
            });
        }

        @Override
        public void doLayout() {
            // Keep active card showing full size
            if (!isAnimating && currentCard != null) {
                currentCard.setBounds(0, 0, getWidth(), getHeight());
            }
        }

        @Override
        protected void paintChildren(Graphics g) {
            if (isAnimating && imgPrev != null && imgNext != null) {
                Graphics2D g2 = (Graphics2D) g.create();

                // Draw Previous (Fade Out due to Next covering it, or explicitly fade)
                // We'll keep Prev static opacity 1, or fade it 1->0
                // Let's Fade Prev 1->0
                g2.setComposite(java.awt.AlphaComposite.SrcOver.derive(1.0f - animProgress));
                g2.drawImage(imgPrev, 0, 0, null);

                // Draw Next (Slide Up + Fade In)
                g2.setComposite(java.awt.AlphaComposite.SrcOver.derive(animProgress));
                int slideY = (int) (40 * (1.0f - animProgress)); // slide from 40px down
                g2.drawImage(imgNext, 0, slideY, null);

                g2.dispose();
            } else {
                super.paintChildren(g);
            }
        }
    }

    /**
     * Sophisticated button animation: Color morphing + Scale on Click.
     */
    private static class AnimatedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final Color baseColor;
        private final Color targetColor;
        private final boolean isOutlined;

        // Animation State
        private float hoverProgress = 0f; // 0.0 (base) -> 1.0 (hover)
        private float pressScale = 1.0f; // 1.0 (normal) -> 0.95 (pressed)
        private javax.swing.Timer hoverTimer;

        public AnimatedButtonUI(Color base) {
            this(base, base);
        }

        public AnimatedButtonUI(Color base, Color outlineColor) {
            if (base != null) {
                this.baseColor = base;
                this.targetColor = ACCENT_BLUE.brighter(); // Brighter accent
                this.isOutlined = false;
            } else {
                // Outlined Mode
                this.baseColor = outlineColor;
                this.targetColor = outlineColor.darker(); // Darker on hover
                this.isOutlined = true;
            }
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            AbstractButton b = (AbstractButton) c;
            b.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    startHover(true, b);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    startHover(false, b);
                }

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    pressScale = 0.95f;
                    b.repaint();
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    pressScale = 1.0f;
                    b.repaint();
                }
            });
        }

        private void startHover(boolean entering, Component c) {
            if (hoverTimer != null && hoverTimer.isRunning())
                hoverTimer.stop();

            float target = entering ? 1.0f : 0.0f;
            // 200ms transition
            hoverTimer = new javax.swing.Timer(15, new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (Math.abs(target - hoverProgress) < 0.05f) {
                        hoverProgress = target;
                        ((javax.swing.Timer) e.getSource()).stop();
                    } else {
                        hoverProgress += (target - hoverProgress) * 0.2f; // Smooth Lerp
                    }
                    c.repaint();
                }
            });
            hoverTimer.start();
        }

        @Override
        public void paint(Graphics g, JComponent c) {

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = c.getWidth();
            int h = c.getHeight();

            // Scale transform centered
            g2.translate(w / 2, h / 2);
            g2.scale(pressScale, pressScale);
            g2.translate(-w / 2, -h / 2);

            // Interpolate Color
            Color drawColor;
            if (isOutlined) {
                // Outlined: Text changes color?? No, keep text static, animate border/bg?
                // Let's animate Border color
                drawColor = AnimationUtils.blend(baseColor, ACCENT_BLUE.brighter(), hoverProgress);
                g2.setColor(drawColor);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
            } else {
                drawColor = AnimationUtils.blend(baseColor, targetColor, hoverProgress);
                g2.setColor(drawColor);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
            }

            // Restore for super (text/icon) painting?
            // BasicButtonUI paints text. We need to set color?
            // Usually we set component foreground.
            // But we can just paint text manually or let super handle it.
            // Super will paint text at default pos (unscaled).
            // We want text scaled too.

            super.paint(g2, c);
            g2.dispose();
        }
    }

    /**
     * Floating Toast Notification (GlassPane Overlay)
     */
    private static class GlassToast {
        public static void show(JFrame frame, String msg) {
            JComponent glass = (JComponent) frame.getGlassPane();
            glass.setVisible(true);

            // Add a temporary toast component
            JPanel toast = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(20, 20, 20, 230));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(new Color(60, 60, 60));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    g2.dispose();
                }
            };
            toast.setOpaque(false);

            JLabel lbl = new JLabel(msg);
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
            toast.add(lbl);

            // Layout manually: Bottom Center
            int w = 300;
            int h = 40;
            int x = (frame.getWidth() - w) / 2;
            int startY = frame.getHeight();
            int endY = frame.getHeight() - 100;

            toast.setBounds(x, endY, w, h); // Start pos
            glass.add(toast);
            glass.revalidate();
            glass.repaint();

            // Slide Up + Fade In?
            // GlassPane layout is null usually.

            // Animate In
            AnimationUtils.animate(p -> {
                float ease = AnimationUtils.easeOutCubic(p);
                int curY = (int) (startY - (startY - endY) * ease);
                toast.setBounds(x, curY, w, h);
            }, 300, () -> {
                // Wait 2s then Fade Out
                javax.swing.Timer t = new javax.swing.Timer(2000, e -> {
                    glass.remove(toast);
                    glass.revalidate();
                    glass.repaint();
                });
                t.setRepeats(false);
                t.start();
            });
        }
    }

    private static class AnimationUtils {
        public static void animate(Consumer<Float> consumer, int durationMs, Runnable onComplete) {
            long startTime = System.nanoTime();
            javax.swing.Timer timer = new javax.swing.Timer(16, null); // ~60fps
            timer.addActionListener(e -> {
                long now = System.nanoTime();
                float progress = (float) (now - startTime) / (durationMs * 1_000_000f);
                if (progress >= 1.0f) {
                    consumer.accept(1.0f);
                    timer.stop();
                    if (onComplete != null)
                        onComplete.run();
                } else {
                    consumer.accept(progress);
                }
            });
            timer.start();
        }

        public static float easeOutCubic(float t) {
            return 1 - (float) Math.pow(1 - t, 3);
        }

        public static Color blend(Color c1, Color c2, float ratio) {
            float r = (float) ratio;
            float ir = 1.0f - r;
            float[] rgb1 = c1.getRGBComponents(null);
            float[] rgb2 = c2.getRGBComponents(null);
            return new Color(
                    rgb1[0] * ir + rgb2[0] * r,
                    rgb1[1] * ir + rgb2[1] * r,
                    rgb1[2] * ir + rgb2[2] * r,
                    rgb1[3] * ir + rgb2[3] * r);
        }
    }

    private void error(Exception ex) {
        String message = (ex == null || ex.getMessage() == null) ? "Something went wrong." : ex.getMessage();
        showErrorDialog(message);
        System.err.println("[LeaderFrame] " + ex);
        setStatus("Error: " + message);
    }

    private static void requireNotEmpty(String v, String name) {
        if (v == null || v.trim().isEmpty())
            throw new IllegalArgumentException(name + " cannot be empty");
    }

    private static String norm(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private void showErrorDialog(String msg) {
        JDialog dialog = new JDialog(this, "Error", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel card = new JPanel(new BorderLayout(0, 14)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(DIALOG_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Error");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(ERROR_RED);
        JButton close = createDialogTextButton("X");
        close.addActionListener(e -> dialog.dispose());
        header.add(title, BorderLayout.WEST);
        header.add(close, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(12, 0));
        body.setOpaque(false);
        JLabel icon = new JLabel(UIManager.getIcon("OptionPane.errorIcon"));
        body.add(icon, BorderLayout.WEST);

        String safeMsg = safe(msg);
        JLabel message = new JLabel("<html><body style='width:320px'>" + htmlEscape(safeMsg) + "</body></html>");
        message.setForeground(TEXT_WHITE);
        message.setFont(new Font("SansSerif", Font.PLAIN, 14));
        body.add(message, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        JButton ok = createDialogPrimaryButton("OK");
        ok.addActionListener(e -> dialog.dispose());
        actions.add(ok);

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(card);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showThemeDialog() {
        JDialog dialog = new JDialog(this, "Theme", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel card = new JPanel(new BorderLayout(0, 14)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(DIALOG_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Theme");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);
        JButton close = createDialogTextButton("X");
        close.addActionListener(e -> dialog.dispose());
        header.add(title, BorderLayout.WEST);
        header.add(close, BorderLayout.EAST);

        String currentLabel = (currentTheme == ThemeMode.LIGHT) ? "Light" : "Dark";
        JLabel message = new JLabel("<html><body style='width:320px'>Current theme: " + currentLabel
                + ". Choose a theme.</body></html>");
        message.setForeground(TEXT_MUTED);
        message.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(message, BorderLayout.CENTER);

        JButton lightBtn = createDialogPrimaryButton("Light Mode");
        JButton darkBtn = createDialogPrimaryButton("Dark Mode");
        lightBtn.setEnabled(currentTheme != ThemeMode.LIGHT);
        darkBtn.setEnabled(currentTheme != ThemeMode.DARK);
        lightBtn.addActionListener(e -> {
            dialog.dispose();
            switchTheme(ThemeMode.LIGHT);
        });
        darkBtn.addActionListener(e -> {
            dialog.dispose();
            switchTheme(ThemeMode.DARK);
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(lightBtn);
        actions.add(darkBtn);

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(card);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void switchTheme(ThemeMode mode) {
        if (currentTheme == mode) {
            return;
        }
        currentTheme = mode;
        writeThemePreferenceForUser(leader.getUserId(), mode);
        applyTheme(mode);
        LeaderFrame fresh = new LeaderFrame(leader, onLogout);
        fresh.setVisible(true);
        dispose();
    }

    private ThemeMode readThemePreferenceForUser(String userId) {
        String targetUserId = norm(userId);
        if (targetUserId.isEmpty()) {
            return ThemeMode.LIGHT;
        }

        try {
            for (String line : FileManager.readAllLines(THEME_FILE)) {
                if (!ValidationUtil.hasExactFieldCount(line, 2)) {
                    continue;
                }
                String[] f = line.split(",", -1);
                if (!norm(f[0]).equalsIgnoreCase(targetUserId)) {
                    continue;
                }

                String saved = norm(f[1]);
                if ("DARK".equals(saved)) {
                    return ThemeMode.DARK;
                }
                if ("LIGHT".equals(saved)) {
                    return ThemeMode.LIGHT;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read theme preference: " + e.getMessage());
        }

        return ThemeMode.LIGHT;
    }

    private void writeThemePreferenceForUser(String userId, ThemeMode mode) {
        String targetUserId = norm(userId);
        if (targetUserId.isEmpty() || mode == null) {
            return;
        }

        try {
            List<String> lines = FileManager.readAllLines(THEME_FILE);
            List<String> updated = new ArrayList<>();
            boolean replaced = false;
            String record = targetUserId + "," + mode.name();

            for (String line : lines) {
                if (!ValidationUtil.hasExactFieldCount(line, 2)) {
                    updated.add(line);
                    continue;
                }

                String[] f = line.split(",", -1);
                if (norm(f[0]).equalsIgnoreCase(targetUserId)) {
                    if (!replaced) {
                        updated.add(record);
                        replaced = true;
                    }
                } else {
                    updated.add(line);
                }
            }

            if (!replaced) {
                updated.add(record);
            }

            FileManager.writeAllLines(THEME_FILE, updated);
        } catch (IOException e) {
            System.err.println("Failed to save theme preference: " + e.getMessage());
        }
    }

    private static boolean isLightTheme() {
        return currentTheme == ThemeMode.LIGHT;
    }

    private static Color accentWithAlpha(int alpha) {
        int a = Math.max(0, Math.min(255, alpha));
        return new Color(ACCENT_BLUE.getRed(), ACCENT_BLUE.getGreen(), ACCENT_BLUE.getBlue(), a);
    }

    private static JButton createDialogTextButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(TEXT_MUTED);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton createDialogPrimaryButton(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(ACCENT_BLUE.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(ACCENT_BLUE.brighter());
                } else {
                    g2.setColor(ACCENT_BLUE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(6, 20, 6, 20));
        b.setMaximumSize(new Dimension(150, 32));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                b.repaint();
            }
        });
        return b;
    }

    private boolean showElegantConfirmDialog(String title, String msg, String confirmText) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        java.util.concurrent.atomic.AtomicBoolean confirmed = new java.util.concurrent.atomic.AtomicBoolean(false);

        JPanel card = new JPanel(new BorderLayout(0, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(DIALOG_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitle.setForeground(TEXT_WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        JLabel lblMsg = new JLabel("<html><body style='width:350px; color:#cccccc'>" + msg + "</body></html>");
        lblMsg.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actions.setOpaque(false);

        JButton btnCancel = createDialogTextButton("Cancel");
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnConfirm = new JButton(confirmText);
        btnConfirm.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setContentAreaFilled(false);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorder(new EmptyBorder(10, 20, 10, 20));
        btnConfirm.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirm.setUI(new AnimatedButtonUI(ERROR_RED));
        btnConfirm.addActionListener(e -> {
            confirmed.set(true);
            dialog.dispose();
        });

        actions.add(btnCancel);
        actions.add(btnConfirm);

        card.add(header, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(card);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return confirmed.get();
    }

    private static String htmlEscape(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
