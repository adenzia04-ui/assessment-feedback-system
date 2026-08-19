package gui;

import model.GradingSystem;
import users.Student;
import util.AnimationUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

public final class StudentFrame extends JFrame {

    private final Student student;

    // Navigation
    private final FluidTransitionPanel contentPanel = new FluidTransitionPanel();

    private static final String CARD_HOME = "HOME";
    private static final String CARD_RESULTS = "RESULTS";
    private static final String CARD_REGISTER = "REGISTER";
    private static final String CARD_COMMENTS = "COMMENTS";
    private static final String CARD_SETTINGS = "SETTINGS";
    private static final String CARD_EDIT_PROFILE = "EDIT_PROFILE";
    private static final String CARD_NOTIFICATIONS = "NOTIFICATIONS";
    private static final String CARD_LECTURER_FEEDBACK = "LECTURER_FEEDBACK";

    // Colors (shared with LeaderFrame schema)
    private static final Color BG_DARK = new Color(11, 14, 17);
    private static final Color ACCENT_BLUE = new Color(30, 129, 255);
    private static final Color TEXT_MUTED = new Color(160, 160, 165);
    private static final Color INPUT_BG = new Color(16, 18, 21);

    private static final Color TILE_HOVER = new Color(45, 150, 255);
    private static final Color DASH_CARD_BG = new Color(20, 22, 25);
    private static final Color DASH_CARD_BORDER = new Color(36, 38, 41);

    private static final int DASH_CARD_RADIUS = 24;
    private static final int DASH_CARD_WIDTH = 880;
    private static final int QUICK_TILE_GAP = 16;
    private static final int QUICK_TILE_HEIGHT = 104;

    // Busy state
    private boolean busy = false;

    // Header icons
    private JButton gearBtn;
    private JButton logoutBtn;
    private JLabel nameLabel;
    private JLabel idLabel;
    private BufferedImage studentAvatarImage;
    private BufferedImage userIconImage;
    private BufferedImage quickModuleImage;
    private BufferedImage quickResultsImage;
    private BufferedImage quickFeedbackImage;

    private BufferedImage quickRegisterImage;

    // Header Images
    private BufferedImage headerSettingsImage;
    private BufferedImage headerNotificationImage;
    private BufferedImage headerLogoutImage;

    // Schedule Image
    private BufferedImage noItemsImage;

    // Home tiles
    private TileButton resultsTile;

    private TileButton registerTile;
    private TileButton commentsTile;
    private TileButton feedbackTile;

    // Screens
    private final JPanel classListPanel = new JPanel(); // New list container
    private java.util.List<users.Student.ClassInfo> allClassesCache = new java.util.ArrayList<>();
    private java.util.List<String> lecturerCache = new java.util.ArrayList<>();

    private final JTextField commentLecturerIdField = new JTextField(14);
    private final JTextArea commentArea = new JTextArea(5, 20);
    private JButton submitCommentBtn = new JButton("Submit Comment");

    // Profile
    private final JTextField profileNameField = new JTextField(22);
    private final JPasswordField profilePasswordField = new JPasswordField(22);
    private final JButton saveProfileBtn = createDialogPrimaryButton("Save Changes");
    private final JButton editProfileMenuBtn = new JButton("Edit Profile");
    private final JButton logoutMenuBtn = new JButton("Logout");

    // Tables
    // Results UI
    private final JPanel resultsContentPanel = new JPanel();
    private final JLabel resultsPassedLabel = new JLabel("Modules Passed: 0");
    private final JLabel resultsGpaLabel = new JLabel("GPA: 0.00");

    private final DefaultTableModel lecturerFeedbackModel = new DefaultTableModel(
            new Object[] { "Module Name", "Assessment Type", "Lecturer", "Feedback" }, 0);
    private final JTable lecturerFeedbackTable = new JTable(lecturerFeedbackModel);

    public StudentFrame(Student student) {
        super("APSpace - Student");
        this.student = Objects.requireNonNull(student, "student cannot be null");

        // UI Setup
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 850);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Load resources
        studentAvatarImage = loadImage("resources/profile_placeholder.png", "student avatar");
        userIconImage = loadImage("resources/User.jpeg", "user icon");
        quickModuleImage = loadImage("resources/Rate.jpeg", "modules");
        quickResultsImage = loadImage("resources/quick_report.png", "results");
        quickFeedbackImage = loadImage("resources/read_feedback.png", "feedback");
        quickRegisterImage = loadImage("resources/whats_new_find_classroom.png", "register");

        headerSettingsImage = loadImage("resources/set.jpeg", "settings icon");
        headerNotificationImage = loadImage("resources/noti.jpeg", "notification icon");
        headerLogoutImage = loadImage("resources/logoff.jpeg", "logout icon");
        noItemsImage = loadImage("resources/noitems.png", "no items");

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        setContentPane(root);

        // Header
        root.add(buildHeader(), BorderLayout.NORTH);

        // Content Area
        contentPanel.setOpaque(true);
        contentPanel.setBackground(BG_DARK);
        contentPanel.addCard(CARD_HOME, buildHomeDashboard());
        contentPanel.addCard(CARD_RESULTS, buildResultsScreen());
        contentPanel.addCard(CARD_REGISTER, buildRegisterScreen());
        contentPanel.addCard(CARD_COMMENTS, buildCommentsScreen());
        contentPanel.addCard(CARD_SETTINGS, buildSettingsScreen());
        contentPanel.addCard(CARD_EDIT_PROFILE, buildEditProfileScreen());
        contentPanel.addCard(CARD_NOTIFICATIONS, buildNotificationScreen());
        contentPanel.addCard(CARD_LECTURER_FEEDBACK, buildLecturerFeedbackScreen());

        loadLecturers();

        root.add(contentPanel, BorderLayout.CENTER);

        bindEvents();

        // Setup GlassPane for Toasts
        // Setup GlassPane for Toasts
        JComponent glass = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
            }
        };
        glass.setLayout(null); // Ensure null layout for setBounds to work
        setGlassPane(glass);
        getGlassPane().setVisible(true);

        SwingUtilities.invokeLater(() -> contentPanel.show(CARD_HOME));
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(10, 0, 8, 0));
        header.setMaximumSize(new Dimension(DASH_CARD_WIDTH, 140)); // Increased height
        header.setPreferredSize(new Dimension(DASH_CARD_WIDTH, 140));

        // Profile Section
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Draw user image inside a circular mask
                BufferedImage avatarImg = userIconImage != null ? userIconImage : studentAvatarImage;
                if (avatarImg != null) {
                    Shape oldClip = g2.getClip();
                    Shape clip = new java.awt.geom.Ellipse2D.Double(2, 2, 56, 56);
                    g2.setClip(clip);
                    g2.drawImage(avatarImg, 2, 2, 56, 56, null);
                    g2.setClip(oldClip);
                } else {
                    // Fallback
                    g2.setColor(new Color(60, 64, 70));
                    g2.fillOval(0, 0, 60, 60);
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

        String fullName = student.getFullName();
        nameLabel = new JLabel(fullName == null ? "" : fullName.toUpperCase());

        // Modern Font & Letter Spacing
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.TRACKING, 0.05);
        attributes.put(TextAttribute.FAMILY, "Segoe UI");
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        attributes.put(TextAttribute.SIZE, 26f);
        nameLabel.setFont(new Font(attributes));

        nameLabel.setForeground(ACCENT_BLUE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String uid = student.getUserId();
        idLabel = new JLabel((uid == null ? "" : uid) + " | STUDENT");
        idLabel.setForeground(TEXT_MUTED);
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(nameLabel);
        text.add(Box.createVerticalStrut(4));
        text.add(idLabel);
        text.add(Box.createVerticalStrut(10)); // Gap before buttons

        // New Action Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Emergency Button
        JButton emergencyBtn = new JButton("Emergency Hotline") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(2.0f));
                // Inset slightly so 2px stroke doesn't clip
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);

                // Shield Icon
                int iy = 8;
                int ix = 12;
                g2.drawLine(ix, iy, ix + 10, iy);
                g2.drawLine(ix, iy, ix, iy + 6);
                g2.drawLine(ix + 10, iy, ix + 10, iy + 6);
                g2.drawLine(ix, iy + 6, ix + 5, iy + 11);
                g2.drawLine(ix + 10, iy + 6, ix + 5, iy + 11);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        emergencyBtn.setHorizontalAlignment(SwingConstants.LEFT);
        emergencyBtn.setBorder(new EmptyBorder(0, 35, 0, 0)); // Space for icon
        emergencyBtn.setForeground(new Color(200, 200, 200));
        emergencyBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        emergencyBtn.setContentAreaFilled(false);
        emergencyBtn.setBorderPainted(false); // We paint it ourselves
        emergencyBtn.setFocusPainted(false);
        emergencyBtn.setPreferredSize(new Dimension(170, 28)); // Slightly wider
        emergencyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        emergencyBtn.addActionListener(e -> showEmergencyDialog());

        // No Status Button
        JButton statusBtn = new JButton("No Status") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(110, 110, 110)); // Gray fill
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                // Virus/Gear Icon
                g2.setColor(Color.BLACK);
                g2.drawOval(10, 8, 10, 10);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        statusBtn.setHorizontalAlignment(SwingConstants.LEFT);
        statusBtn.setBorder(new EmptyBorder(0, 30, 0, 0));
        statusBtn.setForeground(Color.BLACK);
        statusBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusBtn.setContentAreaFilled(false);
        statusBtn.setBorderPainted(false);
        statusBtn.setFocusPainted(false);
        statusBtn.setPreferredSize(new Dimension(110, 28));
        statusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        statusBtn.addActionListener(e -> showCovidFormDialog());

        actions.add(emergencyBtn);
        actions.add(statusBtn);
        text.add(actions);

        left.add(avatar);
        left.add(text);

        // Icon Section with GlowButtons
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); // Increased gap
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(5, 0, 0, 0));

        gearBtn = new GlowIconButton(headerSettingsImage, false);
        gearBtn.setToolTipText("Settings");
        gearBtn.addActionListener(e -> showCard(CARD_SETTINGS));

        // Notification button with badge
        // Notification button (Icon now has static badge, so we disable code-drawn one)
        JButton notificationBtn = new GlowIconButton(headerNotificationImage, false);
        // request
        notificationBtn.setToolTipText("Notifications");
        notificationBtn.addActionListener(e -> contentPanel.show(CARD_NOTIFICATIONS));

        logoutBtn = new GlowIconButton(headerLogoutImage, false);
        logoutBtn.setToolTipText("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        right.add(gearBtn);
        right.add(notificationBtn);
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

    private JComponent buildHomeDashboard() {
        JPanel home = new JPanel(new GridBagLayout());
        home.setOpaque(true);
        home.setBackground(BG_DARK);
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

        resultsTile = new TileButton("My Results", quickResultsImage);

        registerTile = new TileButton("Enroll in a class", quickRegisterImage);
        commentsTile = new TileButton("Give Feedback", quickModuleImage);
        feedbackTile = new TileButton("Lecturer Feedback", quickFeedbackImage);

        // Grid Actions
        resultsTile.addActionListener(e -> contentPanel.show(CARD_RESULTS));
        registerTile.addActionListener(e -> contentPanel.show(CARD_REGISTER));
        commentsTile.addActionListener(e -> contentPanel.show(CARD_COMMENTS));
        feedbackTile.addActionListener(e -> {
            loadLecturerFeedbackData();
            contentPanel.show(CARD_LECTURER_FEEDBACK);
        });

        grid.add(resultsTile);
        grid.add(feedbackTile);
        grid.add(registerTile);
        grid.add(commentsTile);

        quickAccessCard.add(title);
        quickAccessCard.add(Box.createVerticalStrut(14));
        quickAccessCard.add(grid);

        column.add(quickAccessCard);
        column.add(Box.createVerticalStrut(22));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        home.add(column, gbc);

        // Add Schedule Card below
        column.add(buildScheduleCard());
        column.add(Box.createVerticalStrut(22));

        return home;
    }

    private JComponent buildScheduleCard() {
        DashboardCard card = new DashboardCard(DASH_CARD_BG, DASH_CARD_BORDER, DASH_CARD_RADIUS);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 20, 20));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(DASH_CARD_WIDTH, 480));
        card.setPreferredSize(new Dimension(DASH_CARD_WIDTH, 460));

        // Title
        JLabel title = new JLabel("My Schedule");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(ACCENT_BLUE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tabs Container (GridBag for equal 50% width)
        JPanel tabs = new JPanel(new GridBagLayout());
        tabs.setOpaque(false);
        tabs.setAlignmentX(Component.CENTER_ALIGNMENT);
        tabs.setMaximumSize(new Dimension(DASH_CARD_WIDTH, 40));

        // Define active/inactive styles
        Font activeFont = new Font("SansSerif", Font.BOLD, 14);
        Color activeColor = ACCENT_BLUE;
        Border activeBorder = BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_BLUE);

        Font inactiveFont = new Font("SansSerif", Font.BOLD, 14);
        Color inactiveColor = TEXT_MUTED;
        Border inactiveBorder = BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(40, 40, 40)); // Subtle gray
                                                                                                    // border for
                                                                                                    // inactive

        // Tab Labels
        JLabel todayTab = new JLabel("TODAY", SwingConstants.CENTER);
        todayTab.setFont(activeFont);
        todayTab.setForeground(activeColor);
        todayTab.setBorder(activeBorder);
        todayTab.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel upcomingTab = new JLabel("UPCOMING", SwingConstants.CENTER);
        upcomingTab.setFont(inactiveFont);
        upcomingTab.setForeground(inactiveColor);
        upcomingTab.setBorder(inactiveBorder);
        upcomingTab.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Click Logic
        todayTab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                todayTab.setForeground(activeColor);
                todayTab.setBorder(activeBorder);
                upcomingTab.setForeground(inactiveColor);
                upcomingTab.setBorder(inactiveBorder);
            }
        });

        upcomingTab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                upcomingTab.setForeground(activeColor);
                upcomingTab.setBorder(activeBorder);
                todayTab.setForeground(inactiveColor);
                todayTab.setBorder(inactiveBorder);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5; // EXACTLY 50% width
        gbc.gridy = 0;

        gbc.gridx = 0;
        tabs.add(todayTab, gbc);

        gbc.gridx = 1;
        tabs.add(upcomingTab, gbc);

        // Separator line for tabs (optional, but good for design)
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(DASH_CARD_WIDTH, 1));
        sep.setForeground(new Color(60, 60, 60));
        sep.setBackground(new Color(60, 60, 60));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Empty State Content
        JPanel contentData = new JPanel();
        contentData.setOpaque(false);
        contentData.setLayout(new BoxLayout(contentData, BoxLayout.Y_AXIS));
        contentData.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Image
        JLabel imgLabel = new JLabel();
        if (noItemsImage != null) {
            int maxW = 220;
            int maxH = 180;
            int imgW = noItemsImage.getWidth();
            int imgH = noItemsImage.getHeight();
            float scale = Math.min((float) maxW / imgW, (float) maxH / imgH);
            scale = Math.min(scale, 1.0f);
            int drawW = Math.round(imgW * scale);
            int drawH = Math.round(imgH * scale);
            Image scaled = noItemsImage.getScaledInstance(drawW, drawH, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(scaled));
        }
        imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptyTitle = new JLabel("The list is empty!");
        emptyTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        emptyTitle.setForeground(ACCENT_BLUE);
        emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptySub = new JLabel("Either no items to show for today, or the list has finished");
        emptySub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        emptySub.setForeground(TEXT_MUTED);
        emptySub.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentData.add(Box.createVerticalStrut(20));
        contentData.add(imgLabel);
        contentData.add(Box.createVerticalStrut(15));
        contentData.add(emptyTitle);
        contentData.add(Box.createVerticalStrut(5));
        contentData.add(emptySub);

        card.add(title);
        card.add(Box.createVerticalStrut(20));
        card.add(tabs);
        // card.add(sep); // Optional separator visual
        card.add(Box.createVerticalStrut(20));
        card.add(contentData);

        return card;
    }

    private JComponent buildNotificationScreen() {
        JPanel main = new JPanel(new BorderLayout(0, 20));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(20, 50, 20, 50));

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);

        JButton backBtn = new JButton("<html><b style='color:#1e81ff'>\u2190 Back</b></html>");
        backBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> contentPanel.show(CARD_HOME));

        JLabel title = new JLabel("Notifications");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        header.add(backBtn);
        header.add(title);

        main.add(header, BorderLayout.NORTH);

        // Content
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        List<Student.NotificationData> notifs = student.getNotifications();
        for (Student.NotificationData n : notifs) {
            listPanel.add(new NotificationCard(n));
            listPanel.add(Box.createVerticalStrut(15));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(scroll.getVerticalScrollBar());

        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    private JComponent buildResultsScreen() {
        JPanel main = new JPanel(new BorderLayout(0, 20));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(20, 50, 20, 50));

        // Header Section
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);

        // Top Row: Back Button + Title
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topRow.setOpaque(false);

        JButton backBtn = new JButton("<html><b style='color:#1e81ff'>\u2190 Back</b></html>");
        backBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> contentPanel.show(CARD_HOME));

        JLabel title = new JLabel("Academic Results");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        topRow.add(backBtn);
        topRow.add(title);
        header.add(topRow, BorderLayout.NORTH);

        // Hint removed as per request

        // Stats Row (Semester 1 | Passed | GPA)
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setOpaque(false);

        JLabel semLabel = new JLabel("Semester 1");
        semLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        semLabel.setForeground(Color.WHITE);

        JPanel rightStats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 0));
        rightStats.setOpaque(false);

        resultsPassedLabel.setForeground(TEXT_MUTED);
        resultsGpaLabel.setForeground(TEXT_MUTED);
        resultsPassedLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        resultsGpaLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        rightStats.add(resultsPassedLabel);
        rightStats.add(resultsGpaLabel);

        statsPanel.add(semLabel, BorderLayout.WEST);
        statsPanel.add(rightStats, BorderLayout.EAST);

        header.add(statsPanel, BorderLayout.SOUTH);
        main.add(header, BorderLayout.NORTH);

        // Refresh Button (Optional, maybe in header? keeping simpler for now)
        // Let's verify data on show.

        // List Content
        resultsContentPanel.setLayout(new BoxLayout(resultsContentPanel, BoxLayout.Y_AXIS));
        resultsContentPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(resultsContentPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(scroll.getVerticalScrollBar());

        main.add(scroll, BorderLayout.CENTER);

        return main;
    }

    private JComponent buildLecturerFeedbackScreen() {
        return buildTableScreen("Lecturer Feedback", lecturerFeedbackTable, this::loadLecturerFeedbackData);
    }

    private void loadLecturerFeedbackData() {
        lecturerFeedbackModel.setRowCount(0);
        try {
            // 1. Build Module ID -> Module Name map
            Map<String, String> moduleNames = new HashMap<>();
            List<String> moduleLines = util.FileManager.readAllLines("modules.txt");
            for (String line : moduleLines) {
                // Format: ModuleID, ModuleName, LecturerID
                String[] parts = line.split(",", -1);
                if (parts.length >= 2) {
                    moduleNames.put(parts[0].trim(), parts[1].trim());
                }
            }

            // 2. Build Assessment ID -> {ModuleID, AssessmentType}
            class AssessmentInfo {
                String moduleId;
                String type;

                AssessmentInfo(String m, String t) {
                    moduleId = m;
                    type = t;
                }
            }
            Map<String, AssessmentInfo> assessmentInfoMap = new HashMap<>();
            List<String> assessmentLines = util.FileManager.readAllLines("assessments.txt");
            for (String line : assessmentLines) {
                // Format: AssessmentID, ModuleID, Type, Weight
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    assessmentInfoMap.put(parts[0].trim(), new AssessmentInfo(parts[1].trim(), parts[2].trim()));
                }
            }

            // 3. Build Lecturer ID -> Lecturer Name map
            Map<String, String> lecturerNames = new HashMap<>();
            List<String> userLines = util.FileManager.readAllLines("users.txt");
            for (String line : userLines) {
                // Format: ID, Name, Password, Role
                String[] parts = line.split(",", -1);
                if (parts.length >= 4 && "LECTURER".equalsIgnoreCase(parts[3].trim())) {
                    lecturerNames.put(parts[0].trim(), parts[1].trim());
                }
            }

            // 4. Read Feedback
            List<String> lines = util.FileManager.readAllLines("feedback.txt");
            String myId = student.getUserId();
            for (String line : lines) {
                // Format: StudentID, AssessmentID, LecturerID, Feedback
                String[] parts = line.split(",", 4);
                if (parts.length >= 4) {
                    if (parts[0].trim().equalsIgnoreCase(myId)) {
                        String assessmentId = parts[1].trim();
                        String lecturerId = parts[2].trim();
                        String feedback = parts[3].trim();

                        // Resolve details
                        AssessmentInfo info = assessmentInfoMap.get(assessmentId);
                        String moduleName = "Unknown Module";
                        String assessmentType = "Unknown Type";
                        String lecturerName = lecturerNames.getOrDefault(lecturerId, "Unknown Lecturer");
                        String lecturerDisplay = lecturerName + " (" + lecturerId + ")";

                        if (info != null) {
                            assessmentType = info.type;
                            moduleName = moduleNames.getOrDefault(info.moduleId, info.moduleId);
                        }

                        lecturerFeedbackModel.addRow(new Object[] {
                                moduleName,
                                assessmentType,
                                lecturerDisplay,
                                feedback
                        });
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading feedback: " + e.getMessage());
        }
    }

    private JComponent buildTableScreen(String title, JTable table, Runnable refreshAction) {
        JPanel main = new JPanel(new GridBagLayout());
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(20, 50, 20, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        main.add(buildBackRow(title), gbc);

        JPanel card = new ElegantPanel(new Color(28, 31, 38));
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Wrap button in a panel to avoid full-width stretching
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnPanel.setOpaque(false);

        JButton loadBtn = new JButton("Refresh Data") {
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        loadBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        loadBtn.setForeground(Color.WHITE);
        loadBtn.setContentAreaFilled(false);
        loadBtn.setFocusPainted(false);
        loadBtn.setBorderPainted(false);
        loadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loadBtn.setBorder(new EmptyBorder(8, 20, 8, 20)); // Padding

        loadBtn.addActionListener(e -> refreshAction.run());

        btnPanel.add(loadBtn);
        card.add(btnPanel, BorderLayout.NORTH);

        styleDarkTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(16, 18, 21));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 0, 0, 0);
        main.add(card, gbc);

        return main;
    }

    private JComponent buildRegisterScreen() {
        JPanel main = new JPanel(new BorderLayout(0, 20)); // Gap between header and list
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(20, 50, 20, 50));

        // Header Container
        JPanel headerContainer = new JPanel(new BorderLayout(0, 10));
        headerContainer.setOpaque(false);

        // Top Row: Back + Title + Refresh
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        // Left: Back Button + Title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);

        JButton backBtn = new JButton("<html><b style='color:#1e81ff'>\u2190 Back</b></html>");
        backBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> contentPanel.show(CARD_HOME));

        JLabel title = new JLabel("Enroll in Class");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        left.add(backBtn);
        left.add(title);

        topRow.add(left, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setContentAreaFilled(false);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setForeground(TEXT_MUTED);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadAvailableClasses());
        topRow.add(refreshBtn, BorderLayout.EAST);

        headerContainer.add(topRow, BorderLayout.NORTH);

        // Search Bar
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 40));
        styleManageField(searchField); // Reuse style
        addPlaceholder(searchField, "Search Class ID or Name...");

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterClasses(searchField.getText());
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterClasses(searchField.getText());
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterClasses(searchField.getText());
            }
        });

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.add(searchField, BorderLayout.CENTER);
        // Limit width
        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setOpaque(false);
        searchWrapper.add(searchPanel, BorderLayout.WEST);
        searchWrapper.setPreferredSize(new Dimension(400, 45));

        headerContainer.add(searchWrapper, BorderLayout.SOUTH);

        main.add(headerContainer, BorderLayout.NORTH);

        // List
        classListPanel.setLayout(new BoxLayout(classListPanel, BoxLayout.Y_AXIS));
        classListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(classListPanel);
        styleScrollBar(scroll.getVerticalScrollBar());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    private JComponent buildCommentsScreen() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(20, 50, 0, 50);
        main.add(buildBackRow("Feedback"), gbc);

        // Main Card Container
        JPanel card = new ElegantPanel(new Color(20, 22, 26));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(60, 100, 60, 100));
        card.setPreferredSize(new Dimension(900, 650));

        // 1. Lecturer ID Group
        styleManageField(commentLecturerIdField);
        addPlaceholder(commentLecturerIdField, "TP number or lecturer name");
        JPanel idGroup = createInputGroup("Lecturer ID", commentLecturerIdField);
        idGroup.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the group itself
        idGroup.setMaximumSize(new Dimension(600, 120)); // Limit width for better centering

        // Add Auto-Complete
        // Add Auto-Complete
        setupAutoComplete(commentLecturerIdField);

        card.add(idGroup);

        card.add(Box.createVerticalStrut(30));

        // 2. Comments Group
        commentArea.setBackground(INPUT_BG);
        commentArea.setForeground(Color.WHITE);
        commentArea.setCaretColor(Color.WHITE);
        commentArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setBorder(null);
        addPlaceholder(commentArea, "Enter your comments here...");

        JScrollPane scrollArea = new JScrollPane(commentArea);
        scrollArea.setBorder(null);
        scrollArea.getViewport().setOpaque(false);
        scrollArea.setOpaque(false);
        scrollArea.getVerticalScrollBar().setBackground(INPUT_BG);
        scrollArea.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        styleScrollBar(scrollArea.getVerticalScrollBar());
        scrollArea.setPreferredSize(new Dimension(100, 120));

        JPanel commentGroup = createInputGroup("Comments", scrollArea);
        commentGroup.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the group itself
        commentGroup.setMaximumSize(new Dimension(600, 200)); // Limit width
        card.add(commentGroup);

        card.add(Box.createVerticalStrut(50));

        // 3. Submit Button (Custom Pill/Rounded Rectangle)
        submitCommentBtn = new JButton("Submit Feedback") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw Pill Background
                if (getModel().isRollover()) {
                    g2.setColor(new Color(45, 150, 255)); // Lighter Blue Hover
                } else {
                    g2.setColor(ACCENT_BLUE);
                }
                // Radius = Height for fully rounded sides (Pill)
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

                // Draw Centered Text manually to ensure contrast
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                // Adjust Y slightly (ascent offset seems a bit high often, center baseline is
                // better)
                // But standard drawString baseline: y is baseline.
                // Centering vertically: y = (height - textHeight) / 2 + ascent
                g2.drawString(getText(), x, y - 4); // -4 manual tweak for visual vertical center

                g2.dispose();
            }
        };
        submitCommentBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitCommentBtn.setForeground(Color.WHITE);
        submitCommentBtn.setContentAreaFilled(false);
        submitCommentBtn.setFocusPainted(false);
        submitCommentBtn.setBorderPainted(false);
        submitCommentBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitCommentBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitCommentBtn.setPreferredSize(new Dimension(250, 50)); // Slightly taller for pill shape looks
        submitCommentBtn.setMaximumSize(new Dimension(250, 50));

        // Submit Logic
        submitCommentBtn.addActionListener(e -> onSubmitComment());

        card.add(submitCommentBtn);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        main.add(card, gbc);
        return main;
    }

    // --- New UI Helpers for "Glow" Look ---

    private JPanel createInputGroup(String labelText, JComponent inputBuffer) {
        // Container with rounded border and slight glow effect styling
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dark background
                g2.setColor(new Color(28, 32, 38));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Blueish Glow Border
                g2.setColor(new Color(40, 60, 90));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(new Color(30, 129, 255, 50)); // Inner glow hint
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);

                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200)); // Cap height if needed, usually BoxLayout handles
                                                                     // width

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(12));

        // Wrap input in a container if it needs specific sizing or borders inside the
        // group
        // The reference shows the input text just sitting nicely inside.
        // We need to ensure the input component itself doesn't have ugly borders.
        if (inputBuffer instanceof JScrollPane) {
            ((JScrollPane) inputBuffer).setBorder(BorderFactory.createEmptyBorder());
            ((JScrollPane) inputBuffer).getViewport().setBackground(new Color(28, 32, 38)); // Match group bg
            JComponent view = (JComponent) ((JScrollPane) inputBuffer).getViewport().getView();
            view.setBackground(new Color(28, 32, 38));
        } else if (inputBuffer instanceof JTextField) {
            inputBuffer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            inputBuffer.setBackground(new Color(24, 26, 30)); // Slightly distinct input box
            inputBuffer.setBorder(BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(new Color(45, 50, 60), 1, true),
                    new EmptyBorder(8, 10, 8, 10)));
        }

        inputBuffer.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(inputBuffer);

        return panel;
    }

    private void addPlaceholder(JComponent comp, String placeholder) {
        if (comp instanceof javax.swing.text.JTextComponent) {
            javax.swing.text.JTextComponent textComp = (javax.swing.text.JTextComponent) comp;
            // Simple placeholder logic
            textComp.putClientProperty("placeholder", placeholder);

            // We need a custom UI or paint hook to draw the placeholder.
            // Instead of full UI replacement, let's use a FocusListener approach (setting
            // text)
            // OR a simple paint hook if we can.
            // Setting text is risky if we want to read it properly.
            // Let's use a standard "FocusListener" approach that manages a "ghost" text.

            // Clean slate first
            if (textComp.getText().isEmpty()) {
                textComp.setText(placeholder);
                textComp.setForeground(Color.GRAY);
            }

            textComp.addFocusListener(new java.awt.event.FocusListener() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (textComp.getText().equals(placeholder)) {
                        textComp.setText("");
                        textComp.setForeground(Color.WHITE);
                    }
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (textComp.getText().isEmpty()) {
                        textComp.setText(placeholder);
                        textComp.setForeground(Color.GRAY);
                    }
                }
            });
        }
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(60, 65, 75);
                this.trackColor = INPUT_BG;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    private JComponent buildSettingsScreen() {
        return new SettingsCard();
    }

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

    private void bindEvents() {
        resultsTile.addActionListener(e -> {
            showCard(CARD_RESULTS);
            loadResults();
        });

        registerTile.addActionListener(e -> {
            showCard(CARD_REGISTER);
            loadAvailableClasses();
        });
        commentsTile.addActionListener(e -> showCard(CARD_COMMENTS));

        gearBtn.addActionListener(e -> showCard(CARD_SETTINGS));
        editProfileMenuBtn.addActionListener(e -> showCard(CARD_EDIT_PROFILE));
        logoutMenuBtn.addActionListener(e -> dispose());
        saveProfileBtn.addActionListener(e -> onSaveProfile());
    }

    private void loadResults() {
        setStatus("Loading results...");
        resultsContentPanel.removeAll();

        runAsync(
                () -> student.viewResults(),
                (List<Student.ResultData> list) -> {

                    if (list.isEmpty()) {
                        JLabel empty = new JLabel("No results available.");
                        empty.setForeground(TEXT_MUTED);
                        resultsContentPanel.add(empty);
                    } else {
                        double totalGpa = 0;
                        int gpaCount = 0;
                        int passedCount = 0;

                        for (Student.ResultData data : list) {
                            resultsContentPanel.add(new ResultCard(data));
                            resultsContentPanel.add(Box.createVerticalStrut(15));

                            if (GradingSystem.isPassing(data.grade)) {
                                passedCount++;
                            }
                            if (data.gpa >= 0) {
                                totalGpa += data.gpa;
                                gpaCount++;
                            }
                        }

                        double avgGpa = gpaCount == 0 ? -1.0 : totalGpa / gpaCount;

                        // Update Header Stats
                        resultsPassedLabel.setText("Modules Passed: " + passedCount);
                        resultsGpaLabel.setText(avgGpa < 0 ? "GPA: N/A" : String.format("GPA: %.2f", avgGpa));
                    }

                    resultsContentPanel.revalidate();
                    resultsContentPanel.repaint();
                    setStatus("Results loaded");
                },
                this::error);
    }

    private class ResultCard extends JPanel {
        public ResultCard(Student.ResultData data) {
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                    new javax.swing.border.MatteBorder(0, 0, 1, 0, new Color(40, 42, 45)), // Bottom Border only
                    new EmptyBorder(15, 0, 15, 0)));

            // Left: Name, Result Date
            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

            JLabel name = new JLabel(data.moduleName);
            name.setFont(new Font("SansSerif", Font.BOLD, 16));
            name.setForeground(new Color(30, 144, 255)); // Bright Blue

            JLabel date = new JLabel("Internal Release Date: " + data.date);
            date.setFont(new Font("SansSerif", Font.PLAIN, 12));
            date.setForeground(Color.WHITE);

            left.add(name);
            left.add(Box.createVerticalStrut(8));
            left.add(date);
            left.add(Box.createVerticalStrut(5));

            JLabel resLbl = new JLabel("Result: ");
            resLbl.setForeground(Color.WHITE);
            resLbl.setFont(new Font("SansSerif", Font.BOLD, 13));

            // Keep report colors in sync with the grading table.
            Color gradeColor = Color.decode(GradingSystem.getGradeColorHex(data.grade));

            JLabel gradeVal = new JLabel(data.grade);
            gradeVal.setForeground(gradeColor);
            gradeVal.setFont(new Font("SansSerif", Font.BOLD, 13));

            JPanel resPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            resPanel.setOpaque(false);
            resPanel.add(resLbl);
            resPanel.add(gradeVal);

            left.add(resPanel);
            left.add(Box.createVerticalStrut(3));

            // Interpretation removed as per request

            // Right: Grade Points, GPA
            JPanel right = new JPanel();
            right.setOpaque(false);
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.setAlignmentX(Component.RIGHT_ALIGNMENT);

            add(left, BorderLayout.WEST);

            // Assessment Details Panel (Collapsible)
            JPanel detailsPanel = new JPanel();
            detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
            detailsPanel.setOpaque(false);
            detailsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
            detailsPanel.setVisible(false); // Hidden by default

            if (data.assessmentDetails != null && !data.assessmentDetails.isEmpty()) {
                for (String detail : data.assessmentDetails) {
                    JLabel d = new JLabel(detail); // e.g., "Assignment: 35/40"
                    d.setFont(new Font("SansSerif", Font.PLAIN, 13));
                    d.setForeground(new Color(200, 200, 200));
                    detailsPanel.add(d);
                    detailsPanel.add(Box.createVerticalStrut(3));
                }
            } else {
                JLabel d = new JLabel("No assessment details available.");
                d.setFont(new Font("SansSerif", Font.ITALIC, 13));
                d.setForeground(Color.GRAY);
                detailsPanel.add(d);
            }

            add(detailsPanel, BorderLayout.SOUTH);

            // Toggle Button
            JButton toggleBtn = new JButton("Show Details \u25BC");
            toggleBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
            toggleBtn.setForeground(new Color(30, 144, 255));
            toggleBtn.setContentAreaFilled(false);
            toggleBtn.setBorderPainted(false);
            toggleBtn.setFocusPainted(false);
            toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            toggleBtn.addActionListener(e -> {
                boolean visible = !detailsPanel.isVisible();
                detailsPanel.setVisible(visible);
                toggleBtn.setText(visible ? "Hide Details \u25B2" : "Show Details \u25BC");
                revalidate();
                repaint();
            });

            // Right side modifications
            // Score removed as per request

            JLabel gpaLbl = new JLabel("GPA: ");
            gpaLbl.setForeground(Color.WHITE);
            gpaLbl.setFont(new Font("SansSerif", Font.BOLD, 13));

            JLabel gpaVal = new JLabel(data.gpa < 0 ? "N/A" : String.format("%.2f", data.gpa));
            gpaVal.setForeground(data.gpa < 0 ? TEXT_MUTED : new Color(0, 255, 255)); // Cyan
            gpaVal.setFont(new Font("SansSerif", Font.BOLD, 13));

            JPanel gpaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            gpaPanel.setOpaque(false);
            gpaPanel.add(gpaLbl);
            gpaPanel.add(gpaVal);

            // right.add(scoreLbl);
            right.add(Box.createVerticalStrut(5));
            right.add(gpaPanel);
            right.add(Box.createVerticalStrut(5));
            right.add(toggleBtn); // Add toggle button to right side

            add(right, BorderLayout.EAST);
        }
    }

    private class NotificationCard extends JPanel {
        public NotificationCard(Student.NotificationData data) {
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                    new javax.swing.border.MatteBorder(0, 0, 1, 0, new Color(40, 42, 45)),
                    new EmptyBorder(20, 0, 20, 0)));

            // Left: Title, Date, Category
            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("<html><body style='width: 600px'>" + data.title + "</body></html>");
            title.setFont(new Font("SansSerif", Font.PLAIN, 16));
            title.setForeground(Color.WHITE);

            JLabel date = new JLabel(data.date);
            date.setFont(new Font("SansSerif", Font.PLAIN, 14));
            date.setForeground(TEXT_MUTED);

            // Category "Pill"
            JPanel pillWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            pillWrapper.setOpaque(false);

            JLabel category = new JLabel(data.category);
            category.setFont(new Font("SansSerif", Font.BOLD, 12));
            category.setForeground(Color.WHITE);
            category.setBorder(new EmptyBorder(4, 12, 4, 12));

            JPanel pill = new JPanel(new BorderLayout());
            pill.setBackground(new Color(160, 32, 240)); // Purple/Magenta
            // Rounding would require custom paint, let's just color bg for now or use my
            // existing RoundedSettingsPanel logic?
            // Let's make a simple inline rounded panel
            pill = new JPanel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(147, 51, 234)); // Purple
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                }
            };
            pill.setOpaque(false);
            pill.add(category);
            pillWrapper.add(pill);

            content.add(title);
            content.add(Box.createVerticalStrut(8));
            content.add(date);
            content.add(Box.createVerticalStrut(12));
            content.add(pillWrapper);

            content.add(pillWrapper);

            // Right: Arrow removed as per user request

            add(content, BorderLayout.CENTER);

            // Mouse Interaction
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    setBackground(new Color(255, 255, 255, 10));
                    setOpaque(true);
                    repaint();
                }

                public void mouseExited(java.awt.event.MouseEvent e) {
                    setOpaque(false);
                    repaint();
                }
            });
        }
    }

    private void loadAvailableClasses() {
        classListPanel.removeAll();
        classListPanel.revalidate();
        classListPanel.repaint();
        setStatus("Loading classes...");

        runAsync(() -> {
            return student.getAvailableClasses();
        }, classes -> {
            setStatus("Classes loaded");
            allClassesCache = classes;
            filterClasses(""); // Initial show all
        }, this::error);
    }

    private void loadLecturers() {
        runAsync(() -> {
            try {
                return student.getLecturerList();
            } catch (Exception e) {
                return new java.util.ArrayList<String>();
            }
        }, list -> {
            lecturerCache = list;
        }, null);
    }

    private void setupAutoComplete(JTextField field) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(new Color(35, 40, 45)); // Dark background
        menu.setBorder(BorderFactory.createLineBorder(new Color(60, 65, 75)));

        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Ignore navigation keys
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP
                        || e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    return;
                }

                String content = field.getText().trim();
                menu.setVisible(false);
                menu.removeAll();

                if (content.isEmpty() || lecturerCache == null || lecturerCache.isEmpty()) {
                    return;
                }

                int count = 0;
                for (String item : lecturerCache) {
                    // Item format: "ID - Name"
                    // Check if content matches ID or Name
                    if (item.toLowerCase().contains(content.toLowerCase())) {
                        JMenuItem mi = new JMenuItem(item) {
                            @Override
                            protected void paintComponent(Graphics g) {
                                Graphics2D g2 = (Graphics2D) g.create();
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                                // Background
                                if (isArmed() || isSelected()) {
                                    g2.setColor(new Color(45, 150, 255)); // Hover Blue
                                } else {
                                    g2.setColor(new Color(35, 40, 45)); // Default Dark
                                }
                                g2.fillRect(0, 0, getWidth(), getHeight());

                                // Text
                                g2.setColor(Color.WHITE);
                                g2.setFont(getFont());
                                FontMetrics fm = g2.getFontMetrics();
                                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                                g2.drawString(getText(), 10, y); // 10px padding

                                g2.dispose();
                            }
                        };
                        mi.setFont(new Font("SansSerif", Font.PLAIN, 14));
                        mi.setPreferredSize(new Dimension(field.getWidth(), 35)); // Match field width roughly
                        mi.setBorder(null);

                        // Validation: If user selects, we fill ID?
                        // Let's fill the ID part for consistency
                        mi.addActionListener(ev -> {
                            String id = item.split(" - ")[0];
                            field.setText(id);
                            menu.setVisible(false);
                        });
                        menu.add(mi);
                        count++;
                        if (count >= 5)
                            break;
                    }
                }

                if (count > 0) {
                    menu.setPopupSize(field.getWidth(), count * 35); // Force width
                    menu.show(field, 0, field.getHeight());
                    field.requestFocus();
                }
            }
        });
    }

    private void filterClasses(String query) {
        classListPanel.removeAll();
        String q = query.toLowerCase().trim();

        boolean found = false;
        if (allClassesCache != null) {
            for (users.Student.ClassInfo c : allClassesCache) {
                // Search by Module ID or Name
                if (c.moduleId.toLowerCase().contains(q) || c.moduleName.toLowerCase().contains(q)) {
                    addEnrollmentRow(c);
                    classListPanel.add(Box.createVerticalStrut(10));
                    found = true;
                }
            }
        }

        if (!found) {
            JLabel l = new JLabel("No matching classes found.");
            l.setForeground(TEXT_MUTED);
            l.setFont(new Font("SansSerif", Font.ITALIC, 14));
            classListPanel.add(l);
        }

        classListPanel.revalidate();
        classListPanel.repaint();
    }

    private void addEnrollmentRow(users.Student.ClassInfo info) {
        // We use info.classId for logic, but display info.moduleId

        // Custom rounded panel for the row
        JPanel row = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(45, 50, 58));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(20, 25, 20, 25)); // Internal padding
        row.setMaximumSize(new Dimension(DASH_CARD_WIDTH, 80)); // Slightly taller default

        // Label using Module ID
        JLabel lbl = new JLabel("<html><b style='font-size:14px; color:#ffffff'>" + info.moduleId
                + "</b> <span style='color:#aaaaaa'> - " + info.moduleName + "</span></html>");
        row.add(lbl, BorderLayout.CENTER);

        // Button with custom rounded paint
        JButton actionBtn = new JButton("Join") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(getBackground().darker());
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Text
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        actionBtn.setBackground(ACCENT_BLUE);
        actionBtn.setForeground(Color.WHITE);
        actionBtn.setFocusPainted(false);
        actionBtn.setBorderPainted(false);
        actionBtn.setContentAreaFilled(false); // Important for custom paint
        actionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionBtn.setPreferredSize(new Dimension(100, 35));

        // Check if enrolled (Synchronous check or Async? Student.isEnrolled is IO.
        // We should probably fetch enrollment status with the list or check it here
        // async.
        // For simplicity, we check async.

        row.add(actionBtn, BorderLayout.EAST);

        classListPanel.add(row);

        // Async check enrollment state
        // Async check enrollment state
        runAsync(() -> student.isEnrolled(info.classId), enrolled -> {
            if (enrolled) {
                actionBtn.setText("Drop");
                actionBtn.setBackground(new Color(220, 53, 69)); // Red indicating Drop

                // Remove previous listeners
                for (java.awt.event.ActionListener al : actionBtn.getActionListeners()) {
                    actionBtn.removeActionListener(al);
                }
                actionBtn.addActionListener(e -> dropClass(info.classId, info.moduleId, actionBtn));
            } else {
                actionBtn.setText("Join");
                actionBtn.setBackground(ACCENT_BLUE);

                // Remove previous listeners
                for (java.awt.event.ActionListener al : actionBtn.getActionListeners()) {
                    actionBtn.removeActionListener(al);
                }
                actionBtn.addActionListener(e -> enrollInClass(info.classId, info.moduleId, actionBtn));
            }
        }, err -> {
        });
    }

    private void dropClass(String classId, String moduleId, JButton btn) {
        showCustomConfirm("Confirm Drop", "Are you sure you want to drop " + moduleId + "?", () -> {
            btn.setEnabled(false);
            runAsync(() -> {
                student.unregisterClass(classId);
                return null;
            }, v -> {
                GlassToast.show(this, "Dropped " + moduleId);
                // Update button state back to Join
                btn.setText("Join");
                btn.setBackground(ACCENT_BLUE);
                btn.setEnabled(true);

                // Re-bind to enroll action
                for (java.awt.event.ActionListener al : btn.getActionListeners()) {
                    btn.removeActionListener(al);
                }
                btn.addActionListener(e -> enrollInClass(classId, moduleId, btn));

            }, err -> {
                btn.setEnabled(true);
                error(err);
            });
        });
    }

    private void enrollInClass(String classId, String moduleId, JButton btn) {
        showCustomConfirm("Confirm Join", "Are you sure you want to join " + moduleId + "?", () -> {
            btn.setEnabled(false);
            runAsync(() -> {
                student.registerClass(classId);
                return null;
            }, v -> {
                GlassToast.show(this, "Joined " + moduleId + "!");
                // Update button state to Drop
                btn.setText("Drop");
                btn.setBackground(new Color(220, 53, 69));
                btn.setEnabled(true);

                // Re-bind to drop action
                for (java.awt.event.ActionListener al : btn.getActionListeners()) {
                    btn.removeActionListener(al);
                }
                btn.addActionListener(e -> dropClass(classId, moduleId, btn));

            }, err -> {
                btn.setEnabled(true);
                error(err);
            });
        });
    }

    private void onSubmitComment() {
        String lid = commentLecturerIdField.getText().trim();
        String c = commentArea.getText().trim();

        // Remove placeholders if present
        if (lid.equals("TP number or lecturer name"))
            lid = "";
        if (c.equals("Enter your comments here..."))
            c = "";

        if (lid.isEmpty() || c.isEmpty()) {
            error(new IllegalArgumentException("Lecturer ID and Comment required"));
            return;
        }

        // Validate and Resolve Lecturer
        String resolvedId = null;
        if (lecturerCache != null) {
            for (String entry : lecturerCache) {
                // Format: "ID - Name"
                String[] parts = entry.split(" - ");
                String id = parts[0];
                String name = parts.length > 1 ? parts[1] : "";

                if (id.equalsIgnoreCase(lid)) {
                    resolvedId = id;
                    break;
                }
                if (name.equalsIgnoreCase(lid)) {
                    resolvedId = id; // Resolve Name to ID
                    break;
                }
            }
        }

        if (resolvedId == null) {
            // If cache not loaded or not found
            // Maybe allow bypass if cache failed? No, explicit requirement to match
            // users.txt
            if (lecturerCache == null || lecturerCache.isEmpty()) {
                // Try reload or just proceed with raw ID (User might be offline or something,
                // but context implies local file)
                // Let's enforce validation.
                error(new IllegalArgumentException("Validation data not loaded. Please wait or restart."));
                return;
            }
            error(new IllegalArgumentException("Lecturer not found. Please enter a valid ID or Name."));
            return;
        }

        final String lectureId = resolvedId;
        final String comment = c;

        runAsync(
                () -> {
                    student.commentLecturer(lectureId, comment);
                    return null;
                },
                v -> {
                    // Success Popup as requested
                    showCustomMessage("Feedback Submitted", "Your feedback has been submitted successfully.", false);

                    // Reset fields with placeholders
                    commentLecturerIdField.setText("TP number or lecturer name");
                    commentLecturerIdField.setForeground(Color.GRAY);
                    commentArea.setText("Enter your comments here...");
                    commentArea.setForeground(Color.GRAY);
                },
                this::error);
    }

    // --- FluidTransitionPanel Implementation ---
    private static class FluidTransitionPanel extends JPanel {
        private final CardLayout cardLayout = new CardLayout();
        private final Map<String, JComponent> screens = new HashMap<>();

        public FluidTransitionPanel() {
            setLayout(cardLayout);
            setOpaque(false);
        }

        public void addCard(String name, JComponent comp) {
            screens.put(name, comp);
            add(comp, name);
        }

        public void show(String name) {
            // Simple switch for now to restore functionality
            cardLayout.show(this, name);
        }
    }

    // Proxy methods to fix visibility/naming issues
    private void updateStudentProfile(String newName, String newPass) {
        System.out.println("DEBUG: updateStudentProfile called with " + newName);
        try {
            // Student.editProfile is method to use
            student.editProfile(newName, newPass);
            // Update labels locally as editProfile works on file
            // Reflection or public setter needed if field update required immediately on
            // object
            // But since Student.editProfile calls setFullName/setPassword internally (which
            // are protected in User),
            // and Student is in same package as User (users package), it works there.
            // StudentFrame is in 'gui', so it can't call setFullName directly on student
            // (User).
            // But validation logic is in editProfile anyway.
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void onSaveProfile() {
        String newName = profileNameField.getText().trim();
        String newPass = new String(profilePasswordField.getPassword()).trim();

        if (newName.isEmpty() && newPass.isEmpty()) {
            GlassToast.show(this, "Nothing to update");
            return;
        }

        runAsync(
                () -> {
                    updateStudentProfile(newName, newPass);
                    return null;
                },
                v -> {
                    setStatus("Profile updated. Some changes may need relogin.");
                    nameLabel.setText(student.getFullName().toUpperCase());
                    showCard(CARD_HOME);
                },
                this::error);
    }

    // --- Core Helpers ---

    private void showCard(String name) {
        contentPanel.show(name);
    }

    private BufferedImage loadImage(String path, String desc) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Could not load " + desc + ": " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unused")
    private String safe(String s) {
        return s == null ? "?" : s;
    }

    private void setStatus(String msg) {
        if (busy)
            return;
        GlassToast.show(this, msg);
    }

    private void error(Exception e) {
        e.printStackTrace();

        Throwable cause = e;
        // Unwrap wrapper exceptions
        while ((cause instanceof java.util.concurrent.ExecutionException ||
                cause instanceof java.lang.reflect.InvocationTargetException) && cause.getCause() != null) {
            cause = cause.getCause();
        }

        String msg = cause.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = cause.getClass().getSimpleName();
        }

        // Don't show "Error: " prefix if it's a validation message
        if (cause instanceof IllegalArgumentException || cause instanceof IllegalStateException) {
            GlassToast.show(this, msg);
        } else {
            GlassToast.show(this, "Error: " + msg);
        }
    }

    private <T> void runAsync(Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onError) {
        busy = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new javax.swing.SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                busy = false;
                setCursor(Cursor.getDefaultCursor());
                try {
                    onSuccess.accept(get());
                } catch (Exception e) {
                    onError.accept(e);
                }
            }
        }.execute();
    }

    // --- Helper Components ---

    private JPanel buildBackRow(String title) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        row.setOpaque(false);

        JButton back = new JButton("← Back");
        back.setFont(new Font("SansSerif", Font.BOLD, 14));
        back.setForeground(ACCENT_BLUE);
        back.setContentAreaFilled(false);
        back.setBorderPainted(false);
        back.setFocusPainted(false);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> showCard(CARD_HOME));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 24));
        t.setForeground(Color.WHITE);

        row.add(back);
        row.add(t);
        return row;
    }

    private JLabel createManageLabel(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(c);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void styleManageField(JTextField f) {
        f.setBackground(INPUT_BG);
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(45, 51, 59), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }

    private JButton createDialogPrimaryButton(String text) {
        JButton b = new JButton(text);
        styleManagePrimaryButton(b, ACCENT_BLUE);
        return b;
    }

    private void styleManagePrimaryButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.putClientProperty("bgColor", bg);

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(bg.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(bg);
            }
        });
    }

    private void styleDarkTable(JTable table) {
        table.setBackground(new Color(24, 25, 27));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(45, 48, 55));
        table.setRowHeight(40);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        table.getTableHeader().setBackground(new Color(20, 22, 25)); // Darker header
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 63, 70)));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45)); // Taller header

        // Custom Header Renderer for Padding and Caps
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                l.setBackground(new Color(20, 22, 25));
                l.setForeground(new Color(220, 220, 220)); // Soft white
                l.setFont(new Font("SansSerif", Font.BOLD, 13));
                l.setBorder(new EmptyBorder(0, 15, 0, 0)); // Padding
                if (value != null) {
                    l.setText(value.toString().toUpperCase()); // CAPS for elegance
                }
                return l;
            }
        });
    }

    // UI Classes

    private class TileButton extends JButton {
        private float hoverAlpha = 0f;
        private final BufferedImage icon;
        private Color customBg = ACCENT_BLUE; // Default

        public TileButton(String text, BufferedImage icon) {
            this.icon = icon;
            setText(text);
            setFont(new Font("SansSerif", Font.BOLD, 16));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setBorder(new EmptyBorder(20, 15, 20, 25)); // Restore vertical padding, keep left reduced
            setHorizontalAlignment(SwingConstants.LEFT);
            setVerticalAlignment(SwingConstants.TOP); // Restore original alignment
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    startAnim(1f);
                }

                public void mouseExited(java.awt.event.MouseEvent e) {
                    startAnim(0f);
                }
            });
        }

        private void startAnim(float target) {
            AnimationUtil.animateValue(hoverAlpha, target, 200, (val, complete) -> {
                hoverAlpha = val;
                repaint();
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape shape = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 18, 18);
            g2.setClip(shape);

            // Using lerpColor for blending
            g2.setColor(AnimationUtil.lerpColor(customBg, TILE_HOVER, hoverAlpha));

            float eased = AnimationUtil.easeOutCubic(hoverAlpha);
            // Translate only the content inside (if we want movement)
            // But if we translate context, we might shift the clip?
            // "g2.translate(0, -2 * eased);" shifts everything.
            // Let's keep the background static or ensure updated shape.
            // Original code shifted the fillRoundRect.

            // To be consistent with old style but clipped:
            // We should draw the background on the shape.
            g2.fill(shape);

            g2.setColor(new Color(255, 255, 255, (int) (22 + (18 * eased))));
            int glow = getHeight() + 30;
            g2.fillOval(getWidth() - glow / 2, -glow / 3, glow, glow);

            if (icon != null) {
                // "Give Feedback" style: Contain image on the right side
                // Determine available space
                int maxH = getHeight() - 10;
                int maxW = (int) (getWidth() * 0.55); // Allow up to 55% width

                // Calculate scaling to fit in the box
                double scaleH = (double) maxH / icon.getHeight();
                double scaleW = (double) maxW / icon.getWidth();
                double baseScale = Math.min(scaleH, scaleW);

                // Add hover zoom effect
                double finalScale = baseScale * (0.95 + (0.05 * eased));

                int dw = (int) (icon.getWidth() * finalScale);
                int dh = (int) (icon.getHeight() * finalScale);

                // Position on right
                int x = getWidth() - dw - 15;
                int y = (getHeight() - dh) / 2;

                g2.drawImage(icon, x, y, dw, dh, null);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class GlowIconButton extends JButton {
        private final BufferedImage icon;
        private final boolean showBadge;
        private float hoverAlpha = 0f;

        public GlowIconButton(BufferedImage icon, boolean showBadge) {
            this.icon = icon;
            this.showBadge = showBadge;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(50, 50));
            setMaximumSize(new Dimension(50, 50));

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    AnimationUtil.animateValue(hoverAlpha, 1f, 200, (val, complete) -> {
                        hoverAlpha = val;
                        repaint();
                    });
                }

                public void mouseExited(java.awt.event.MouseEvent e) {
                    AnimationUtil.animateValue(hoverAlpha, 0f, 200, (val, complete) -> {
                        hoverAlpha = val;
                        repaint();
                    });
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // High quality resizing for elegant icons
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            // Container Shape
            int w = getWidth();
            int h = getHeight();
            Shape shape = new RoundRectangle2D.Double(2, 2, w - 4, h - 4, 16, 16);

            // 1. Dark Glassy Background
            g2.setColor(new Color(30, 32, 36, 230));
            g2.fill(shape);

            // 2. Glow / Border
            // Lerp between subtle gray and active blue
            Color normalBorder = new Color(255, 255, 255, 30);
            Color activeBorder = new Color(60, 130, 255, 180);
            Color borderColor = AnimationUtil.lerpColor(normalBorder, activeBorder, hoverAlpha);

            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(borderColor);
            g2.draw(shape);

            // 3. Icon
            if (icon != null) {
                // Increased size for better "fit" (34x34)
                int iw = 34;
                int ih = 34;
                int ix = (w - iw) / 2;
                int iy = (h - ih) / 2;
                g2.drawImage(icon, ix, iy, iw, ih, null);
            }

            // 4. Notification Badge
            if (showBadge) {
                g2.setColor(new Color(255, 59, 48)); // Red
                int bw = 10;
                int bh = 10;
                // Position at top-right inside the box
                g2.fillOval(w - 18, 12, bw, bh);
            }

            g2.dispose();
        }
    }

    private static class DashboardCard extends JPanel {
        Color bg, border;
        int r;

        DashboardCard(Color bg, Color b, int r) {
            this.bg = bg;
            this.border = b;
            this.r = r;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, r, r);
            g2.dispose();
        }
    }

    private static class GlassToast {
        public static void show(JFrame frame, String msg) {
            JComponent glass = (JComponent) frame.getGlassPane();
            glass.setVisible(true);

            // Use GridBagLayout for reliable centering
            JPanel toast = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(20, 20, 20, 230));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.dispose();
                }
            };
            toast.setOpaque(false);

            JLabel l = new JLabel(msg);
            l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Bold for better visibility
            toast.add(l);

            // Centered at bottom (Dynamic width up to 800)
            FontMetrics fm = toast.getFontMetrics(l.getFont());
            int textWidth = fm.stringWidth(msg);
            int w = Math.min(800, Math.max(400, textWidth + 60)); // Min 400, Max 800, Padding 60
            int h = 50;
            toast.setBounds((frame.getWidth() - w) / 2, frame.getHeight() - 100, w, h);

            glass.removeAll();
            glass.add(toast);

            // Force layout update
            toast.revalidate();
            toast.repaint();
            glass.repaint();

            javax.swing.Timer t = new javax.swing.Timer(2500, e -> {
                glass.remove(toast);
                glass.repaint();
            });
            t.setRepeats(false);
            t.start();
        }
    }

    private static class ElegantPanel extends JPanel {
        Color bg;

        ElegantPanel(Color bg) {
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // --- Sub-panels for Tabs ---

    private class SettingsCard extends JPanel {

        // State for Bus Locations
        private String busLoc1 = "Parkhill";
        private String busLoc2 = "APU";
        private final String[] BUS_LOCATIONS = {
                "Parkhill", "LRT - Bukit Jalil", "Mosque", "APU", "M Vertica",
                "City of Green", "Bloomsvale", "Fortune Park", "Kuchai Sentral",
                "Kingston Hotel", "Harmony", "Maple", "D'IVO"
        };

        // State for Theme
        private String currentAccent = "Clear Blue (Default)";
        private final String[] ACCENT_OPTIONS = {
                "Clear Blue (Default)"
        };

        // State for Others
        private boolean isMenuViewCards = true;
        private boolean hideProfilePicture = false;
        private boolean useLocalTimezone = false;
        private boolean is12HourFormat = true;
        private boolean disableShakeFeedback = false;

        public SettingsCard() {
            setOpaque(false);
            setLayout(new BorderLayout());

            // Header Panel (Fixed at top)
            JPanel headerObj = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
            headerObj.setOpaque(false);

            JLabel backLbl = new JLabel("\u2190 Back"); // Left arrow
            backLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
            backLbl.setForeground(ACCENT_BLUE);
            backLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            backLbl.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    contentPanel.show(CARD_HOME);
                }
            });
            headerObj.add(backLbl);

            JLabel title = new JLabel("Settings");
            title.setFont(new Font("SansSerif", Font.BOLD, 24));
            title.setForeground(Color.WHITE);
            headerObj.add(title);

            add(headerObj, BorderLayout.NORTH);

            // Scrollable Content
            JPanel content = new JPanel(new GridBagLayout());
            content.setOpaque(false);
            content.setBorder(new EmptyBorder(0, 20, 40, 20)); // Bottom padding for scroll

            JScrollPane scroll = new JScrollPane(content);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(20);
            add(scroll, BorderLayout.CENTER);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 0.5;
            gbc.anchor = GridBagConstraints.NORTH;

            // Note: We use weighty=0 for content rows so they don't stretch excessively
            // and force the scrollbar to appear when they overflow.

            // --- LEFT COLUMN ---
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weighty = 0;
            content.add(createThemesPanel(), gbc);

            gbc.gridy = 1;
            content.add(createOthersPanel(), gbc);

            // --- RIGHT COLUMN ---
            gbc.gridx = 1;
            gbc.gridy = 0;
            content.add(createBusPanel(), gbc);

            gbc.gridy = 1;
            content.add(createTimetablePanel(), gbc);

            gbc.gridy = 2;
            content.add(createSecurityPanel(), gbc);

            // --- FILLER ---
            // Push everything up
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.weighty = 1.0;
            content.add(Box.createVerticalGlue(), gbc);
        }

        private JPanel createThemesPanel() {
            RoundedSettingsPanel p = new RoundedSettingsPanel("Themes", new Color(60, 100, 200));
            p.addSectionHeader("Appearance");
            p.addText("Choose your preferred Theme Mode");
            ButtonGroup themeGroup = new ButtonGroup();
            p.addInteractiveRadio("Auto (Default)", false, themeGroup);

            JRadioButton lightBtn = p.addInteractiveRadio("Light", false, themeGroup);
            JRadioButton darkBtn = p.addInteractiveRadio("Dark", true, themeGroup);

            lightBtn.addActionListener(e -> {
                GlassToast.show(StudentFrame.this, "Light mode currently not available for the students!");
                darkBtn.setSelected(true);
            });
            p.addSpace(10);
            p.addSpace(10);
            p.addInteractiveDropdown("Accent Color", currentAccent, ACCENT_OPTIONS, val -> {
                currentAccent = val;
                return true;
            });
            return p;
        }

        private JPanel createOthersPanel() {
            RoundedSettingsPanel p = new RoundedSettingsPanel("Others", new Color(100, 100, 100));
            p.addSectionHeader("Menu UI");
            p.addText("Choose your preferred More Page View");

            ButtonGroup menuGroup = new ButtonGroup();
            JRadioButton cardsBtn = p.addInteractiveRadio("Cards", isMenuViewCards, menuGroup);
            JRadioButton listBtn = p.addInteractiveRadio("List", !isMenuViewCards, menuGroup);

            cardsBtn.addActionListener(e -> isMenuViewCards = true);
            listBtn.addActionListener(e -> isMenuViewCards = false);

            p.addSeparator();
            p.addActionRow("Active Dashboard Sections", "No Active Sections");

            p.addInteractiveToggle("Hide Profile Picture", hideProfilePicture, val -> hideProfilePicture = val);
            p.addInteractiveToggle("Use Local Timezone", useLocalTimezone, val -> useLocalTimezone = val);

            p.addSpace(10);
            p.addSectionHeader("Time Format");
            p.addText("Select your preferred clock display.");

            ButtonGroup timeGroup = new ButtonGroup();
            JRadioButton h12 = p.addInteractiveRadio("12 Hours", is12HourFormat, timeGroup);
            JRadioButton h24 = p.addInteractiveRadio("24 Hours", !is12HourFormat, timeGroup);

            h12.addActionListener(e -> is12HourFormat = true);
            h24.addActionListener(e -> is12HourFormat = false);

            p.addInteractiveToggle("Disable Shake Feedback", disableShakeFeedback, val -> disableShakeFeedback = val,
                    "This feature available only on mobile app");
            return p;
        }

        private JPanel createBusPanel() {
            RoundedSettingsPanel p = new RoundedSettingsPanel("Bus Shuttle Service", new Color(60, 200, 100));

            // First Location
            p.addInteractiveDropdown("First Location", busLoc1, BUS_LOCATIONS, val -> {
                if (val.equals(busLoc2)) {
                    GlassToast.show(StudentFrame.this, "Locations cannot be the same!");
                    return false; // Reject
                }
                busLoc1 = val;
                return true; // Accept
            });

            // Second Location
            p.addInteractiveDropdown("Second Location", busLoc2, BUS_LOCATIONS, val -> {
                if (val.equals(busLoc1)) {
                    GlassToast.show(StudentFrame.this, "Locations cannot be the same!");
                    return false; // Reject
                }
                busLoc2 = val;
                return true; // Accept
            });
            return p;
        }

        private JPanel createTimetablePanel() {
            RoundedSettingsPanel p = new RoundedSettingsPanel("Student Timetable", new Color(200, 100, 60));
            JLabel status = new JLabel("No Modules Blacklisted");
            status.setForeground(new Color(255, 80, 80));
            status.setFont(new Font("SansSerif", Font.BOLD, 12));
            status.setBorder(new EmptyBorder(0, 0, 10, 0));
            p.getContentContainer().add(status);

            p.addActionRow("Add Hidden Modules", "");
            p.addActionRow("Manage Hidden Modules", "Select the kinds of modules you would like to hide.");
            return p;
        }

        private JPanel createSecurityPanel() {
            RoundedSettingsPanel p = new RoundedSettingsPanel("Security & Privacy", new Color(150, 60, 200));
            JButton btn = p.addActionRow("Change APKey Password", "");
            btn.addActionListener(e -> handleChangePassword());
            return p;
        }

        private void handleChangePassword() {
            showCustomInput("Change Password", (newPass) -> {
                if (newPass.length() < 6) {
                    GlassToast.show(StudentFrame.this, "Password too short!");
                    return;
                }

                runAsync(() -> {
                    // Assuming we just update password, keeping name same
                    // But student.editProfile updates both.
                    // We should probably fetch current name or use student.getFullName()
                    student.editProfile(student.getFullName(), newPass);
                    return null;
                }, v -> {
                    GlassToast.show(StudentFrame.this, "Password changed successfully!");
                }, StudentFrame.this::error);
            });
        }
    }

    private static class RoundedSettingsPanel extends JPanel {
        private final Color accentColor;
        private final JPanel contentContainer;

        public RoundedSettingsPanel(String title, Color accent) {
            this.accentColor = accent;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(5, 5, 5, 5));

            contentContainer = new JPanel();
            contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
            contentContainer.setOpaque(false);
            contentContainer.setBorder(new EmptyBorder(25, 30, 25, 30));

            // Header with Icon placeholder
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            header.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            titleLabel.setForeground(Color.WHITE);
            header.add(titleLabel, BorderLayout.WEST);

            // Icon on right (visual placeholder)
            JLabel icon = new JLabel("<html><span style='font-size:16px'>\u2699</span></html>"); // Gear unicode
            icon.setForeground(new Color(100, 100, 100));
            header.add(icon, BorderLayout.EAST);

            contentContainer.add(header);
            contentContainer.add(Box.createVerticalStrut(20));

            add(contentContainer, BorderLayout.CENTER);
        }

        public JPanel getContentContainer() {
            return contentContainer;
        }

        @SuppressWarnings("unused")
        public void addContent(Component comp) {
            if (comp instanceof JComponent) {
                ((JComponent) comp).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            contentContainer.add(comp);
        }

        public void addSectionHeader(String text) {
            JLabel l = new JLabel(text);
            l.setFont(new Font("Segoe UI", Font.BOLD, 14));
            l.setForeground(new Color(200, 200, 200));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentContainer.add(l);
            contentContainer.add(Box.createVerticalStrut(5));
        }

        public void addText(String text) {
            JLabel l = new JLabel(text);
            l.setFont(new Font("SansSerif", Font.PLAIN, 12));
            l.setForeground(new Color(100, 150, 255)); // Blueish description
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentContainer.add(l);
            contentContainer.add(Box.createVerticalStrut(10));
        }

        public void addInteractiveDropdown(String label, String initialValue, String[] options,
                java.util.function.Function<String, Boolean> onSelect) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            p.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel l = new JLabel(label);
            l.setForeground(Color.LIGHT_GRAY);
            p.add(l, BorderLayout.NORTH);

            JButton btn = new JButton(initialValue + " \u25BC");
            btn.setFont(new Font("SansSerif", Font.BOLD, 13));
            btn.setForeground(Color.WHITE);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btn.addActionListener(e -> {
                showStyledSelectionDialog(label, options, initialValue, (selection) -> {
                    if (selection != null && !selection.equals(initialValue)) {
                        if (onSelect.apply(selection)) {
                            btn.setText(selection + " \u25BC");
                        }
                    }
                });
            });

            p.add(btn, BorderLayout.SOUTH);

            contentContainer.add(p);
            contentContainer.add(Box.createVerticalStrut(15));
        }

        @SuppressWarnings("unused")
        public void addRadioOption(String text, boolean selected) {
            addInteractiveRadio(text, selected, null).setEnabled(false); // Visual only
        }

        public JRadioButton addInteractiveRadio(String text, boolean selected, ButtonGroup group) {
            JRadioButton rb = new JRadioButton(text);
            rb.setOpaque(false);
            rb.setForeground(selected ? Color.WHITE : new Color(150, 150, 150));
            rb.setFont(new Font("SansSerif", Font.PLAIN, 13));
            rb.setSelected(selected);
            rb.setFocusPainted(false);

            rb.addItemListener(e -> {
                rb.setForeground(rb.isSelected() ? Color.WHITE : new Color(150, 150, 150));
            });

            if (group != null) {
                group.add(rb);
            }
            rb.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentContainer.add(rb);
            contentContainer.add(Box.createVerticalStrut(5));
            return rb;
        }

        @SuppressWarnings("unused")
        public void addToggleRow(String text, boolean isOn, String... subtext) {
            addInteractiveToggle(text, isOn, null, subtext).setEnabled(false); // Visual only fallback
        }

        public JComponent addInteractiveToggle(String text, boolean initial, Consumer<Boolean> onToggle,
                String... subtext) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
            p.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel l = new JLabel(text);
            l.setForeground(Color.LIGHT_GRAY);
            p.add(l, BorderLayout.WEST);

            // Interactive Toggle Visual
            // We use a final container to update the icon
            JLabel toggle = new JLabel(initial ? "\u25C9" : "\u25CB"); // Bullseye or Circle
            toggle.setForeground(initial ? ACCENT_BLUE : Color.GRAY);
            toggle.setFont(new Font("SansSerif", Font.BOLD, 18));
            p.add(toggle, BorderLayout.EAST);

            // Toggle state holder
            final boolean[] state = { initial };

            class ToggleHandler extends java.awt.event.MouseAdapter {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    state[0] = !state[0];
                    // Update visual
                    toggle.setText(state[0] ? "\u25C9" : "\u25CB");
                    toggle.setForeground(state[0] ? ACCENT_BLUE : Color.GRAY);

                    if (onToggle != null) {
                        onToggle.accept(state[0]);
                    }
                    p.repaint();
                }
            }

            if (onToggle != null) {
                p.addMouseListener(new ToggleHandler());
                toggle.addMouseListener(new ToggleHandler()); // Also on icon
            }

            contentContainer.add(p);
            if (subtext.length > 0) {
                JLabel sub = new JLabel(subtext[0]);
                sub.setFont(new Font("SansSerif", Font.PLAIN, 10));
                sub.setForeground(new Color(80, 80, 80));
                sub.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentContainer.add(sub);
            }
            contentContainer.add(Box.createVerticalStrut(10));
            return p;
        }

        public JButton addActionRow(String text, String subtext) {
            JButton btn = new JButton();
            btn.setLayout(new BorderLayout());
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Left: Icon + Text
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            left.setOpaque(false);
            // Could add icon here
            JLabel l = new JLabel(text);
            l.setForeground(Color.WHITE);
            l.setFont(new Font("SansSerif", Font.PLAIN, 13));
            left.add(l);

            btn.add(left, BorderLayout.WEST);

            JLabel chev = new JLabel(">");
            chev.setForeground(Color.GRAY);
            btn.add(chev, BorderLayout.EAST);

            contentContainer.add(btn);
            if (subtext != null && !subtext.isEmpty()) {
                JLabel sub = new JLabel("  " + subtext);
                sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
                sub.setForeground(Color.GRAY);
                sub.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentContainer.add(sub);
            }

            // Border bottom
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(60, 60, 60));
            sep.setBackground(new Color(60, 60, 60));
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            sep.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentContainer.add(Box.createVerticalStrut(5));
            contentContainer.add(sep);
            contentContainer.add(Box.createVerticalStrut(5));

            return btn;
        }

        private void showStyledSelectionDialog(String title, String[] options, String current,
                java.util.function.Consumer<String> onConfirm) {
            JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);

            JList<String> list = new JList<>(options);
            list.setSelectedValue(current, true);
            list.setFont(new Font("SansSerif", Font.PLAIN, 14));
            list.setBackground(new Color(35, 39, 45));
            list.setForeground(Color.WHITE);
            list.setSelectionBackground(new Color(30, 136, 229));
            list.setSelectionForeground(Color.WHITE);
            list.setFixedCellHeight(35);
            list.setVisibleRowCount(6); // Show exactly 6 rows
            list.setBorder(new EmptyBorder(5, 5, 5, 5));

            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(null);
            // Removed fixed preferred size to let visibleRowCount control height
            scroll.getViewport().setBackground(new Color(35, 39, 45));
            // Hide scrollbars for cleaner look if content fits or minimal style
            scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
            scroll.getVerticalScrollBar().setBackground(new Color(35, 39, 45));

            ElegantDialog d = new ElegantDialog(owner, "Select Location", title, ElegantDialog.TYPE_CONFIRM, scroll);

            d.setConfirmAction(() -> {
                onConfirm.accept(list.getSelectedValue());
            });
            d.setVisible(true);
        }

        public void addSeparator() {
            contentContainer.add(Box.createVerticalStrut(5));
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(60, 60, 60));
            sep.setBackground(new Color(60, 60, 60));
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            contentContainer.add(sep);
            contentContainer.add(Box.createVerticalStrut(10));
        }

        public void addSpace(int h) {
            contentContainer.add(Box.createVerticalStrut(h));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - 10;
            int h = getHeight() - 10;
            int x = 5;
            int y = 5;
            int arc = 20;

            // Background
            g2.setColor(new Color(28, 31, 38));
            g2.fillRoundRect(x, y, w, h, arc, arc);

            // Side Rectangle (Accent Strip)
            g2.setColor(accentColor);
            Area base = new Area(new RoundRectangle2D.Double(x, y, w, h, arc, arc));
            Area strip = new Area(new Rectangle2D.Double(x, y, 12, h)); // 12px wide strip
            base.intersect(strip); // Clip to rounded corner
            g2.fill(base);

            // Subtle border
            g2.setColor(new Color(45, 48, 55));
            g2.drawRoundRect(x, y, w - 1, h - 1, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class EditProfileCard extends JPanel {
        public EditProfileCard() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JLabel l = new JLabel("Update Your Profile");
            l.setFont(new Font("SansSerif", Font.BOLD, 18));
            l.setForeground(ACCENT_BLUE);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(l);
            add(Box.createVerticalStrut(20));

            add(createManageLabel("Full Name", Color.GRAY));
            add(Box.createVerticalStrut(5));
            styleManageField(profileNameField);
            add(profileNameField);

            add(Box.createVerticalStrut(15));
            add(createManageLabel("New Password", Color.GRAY));
            add(Box.createVerticalStrut(5));
            styleManageField(profilePasswordField);
            add(profilePasswordField);

            add(Box.createVerticalStrut(30));
            saveProfileBtn.setText("Save Changes");
            add(saveProfileBtn);
        }

    }

    private void showEmergencyDialog() {
        JDialog d = new JDialog(this, true); // Modal
        d.setUndecorated(true);
        d.setBackground(new Color(0, 0, 0, 0)); // Transparent for custom painting

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(24, 24, 26));
        content.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Emergency Hotline");
        title.setFont(new Font("SansSerif", Font.PLAIN, 16));
        title.setForeground(new Color(200, 200, 200));

        JLabel close = new JLabel("x");
        close.setText("\u2715"); // Unicode Cross
        close.setFont(new Font("SansSerif", Font.BOLD, 16));
        close.setForeground(Color.GRAY);
        close.setCursor(new Cursor(Cursor.HAND_CURSOR));

        final JLabel finalClose = close; // Final reference for listener
        close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                d.dispose();
            }

            public void mouseEntered(java.awt.event.MouseEvent e) {
                finalClose.setForeground(Color.WHITE);
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                finalClose.setForeground(Color.GRAY);
            }
        });

        header.add(title, BorderLayout.WEST);
        header.add(close, BorderLayout.EAST);

        // Body
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(10, 20, 30, 20));

        // Block 1
        JLabel l1 = new JLabel("APU Security/Emergency Hotline");
        l1.setFont(new Font("SansSerif", Font.BOLD, 14));
        l1.setForeground(Color.WHITE);
        l1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel p1 = new JLabel("\uD83D\uDCDE 017-238 1300"); // Phone icon
        p1.setFont(new Font("SansSerif", Font.PLAIN, 14));
        p1.setForeground(Color.WHITE);
        p1.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(400, 1));
        sep.setForeground(new Color(50, 50, 50));
        sep.setBackground(new Color(50, 50, 50));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Block 2
        JLabel l2 = new JLabel("Weekends, Public Holidays Only");
        l2.setFont(new Font("SansSerif", Font.BOLD, 14));
        l2.setForeground(Color.WHITE);
        l2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel p2 = new JLabel("\uD83D\uDCDE 017-379 1700");
        p2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        p2.setForeground(Color.WHITE);
        p2.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(l1);
        body.add(Box.createVerticalStrut(5));
        body.add(p1);
        body.add(Box.createVerticalStrut(20));
        body.add(sep);
        body.add(Box.createVerticalStrut(20));
        body.add(l2);
        body.add(Box.createVerticalStrut(5));
        body.add(p2);

        content.add(header, BorderLayout.NORTH);
        content.add(body, BorderLayout.CENTER);

        d.setContentPane(content);
        d.setSize(420, 280);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void showCovidFormDialog() {
        JDialog d = new JDialog(this, true); // Modal
        d.setUndecorated(true);
        d.setBackground(new Color(0, 0, 0, 0)); // Transparent

        // Main Container (Black Background)
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(new Color(10, 10, 10)); // Near black
        container.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Form Card (Dark Gray Round)
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 20, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Dialog Border
                g2.setColor(new Color(40, 40, 40));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 40, 30, 40));
        card.setPreferredSize(new Dimension(800, 500));

        // 1. Header Title
        JLabel title = new JLabel("COVID-19 Information Form");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel required = new JLabel("<html><font color='red'>*</font> Required</html>");
        required.setFont(new Font("SansSerif", Font.PLAIN, 12));
        required.setForeground(new Color(150, 50, 50)); // Dark red
        required.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 2. Info Block
        JLabel infoTitle = new JLabel("Why do we collect vaccination data?");
        infoTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        infoTitle.setForeground(Color.WHITE);
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel link = new JLabel(
                "<html>Read our article: <font color='#3b82f6'>COVID-19 Updates & Advisory</font></html>");
        link.setFont(new Font("SansSerif", Font.PLAIN, 12));
        link.setForeground(Color.WHITE);
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 3. Name Field
        JLabel nameLabel = new JLabel("<html>Full Name <font color='red'>*</font></html>");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameLabel.setForeground(new Color(200, 200, 200));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameValue = new JLabel(student.getFullName() != null ? student.getFullName().toUpperCase() : "");
        nameValue.setFont(new Font("SansSerif", Font.PLAIN, 16));
        nameValue.setForeground(Color.WHITE);
        nameValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameValue.setBorder(new EmptyBorder(5, 0, 10, 0));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(800, 1));
        sep.setForeground(new Color(60, 60, 60));
        sep.setBackground(new Color(60, 60, 60));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 4. Vaccination Info
        JLabel vaxHeader = new JLabel("Vaccination Information");
        vaxHeader.setFont(new Font("SansSerif", Font.PLAIN, 20));
        vaxHeader.setForeground(Color.WHITE);
        vaxHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel statusLabel = new JLabel("<html>Vaccination Status <font color='red'>*</font></html>");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(new Color(200, 200, 200));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Custom Dark Combo Box
        String[] statuses = { "Fully Vaccinated", "Partially Vaccinated", "Not Vaccinated" };
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        statusCombo.setMaximumSize(new Dimension(800, 40));
        statusCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusCombo.setBackground(new Color(30, 30, 30));
        statusCombo.setForeground(Color.WHITE);
        statusCombo.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));

        statusCombo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("\u25BC"); // Down arrow
                btn.setFont(new Font("SansSerif", Font.PLAIN, 10));
                btn.setForeground(Color.WHITE);
                btn.setBackground(new Color(30, 30, 30));
                btn.setContentAreaFilled(false);
                btn.setBorder(new EmptyBorder(0, 10, 0, 10));
                btn.setFocusPainted(false);
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(new Color(30, 30, 30));
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            @Override
            protected void installDefaults() {
                super.installDefaults();
                comboBox.setBackground(new Color(30, 30, 30));
                comboBox.setForeground(Color.WHITE);
            }
        });

        statusCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(new Color(60, 60, 60));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(new Color(30, 30, 30));
                    setForeground(Color.WHITE);
                }
                setBorder(new EmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
        statusCombo.setOpaque(false);

        // Load saved status
        try {
            String saved = student.getVaccinationStatus();
            if (saved != null) {
                statusCombo.setSelectedItem(saved);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Footer Warning
        JLabel footer = new JLabel(
                "<html><font color='#d9534f'>By clicking the submit button, I hereby declare that the above is accurate and complete information. I understand that any misleading or falsified information can lead to action to be taken against me.</font></html>");
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add components to card
        card.add(title);
        card.add(Box.createVerticalStrut(5));
        card.add(required);
        card.add(Box.createVerticalStrut(20));

        card.add(infoTitle);
        card.add(Box.createVerticalStrut(2));
        card.add(link);
        card.add(Box.createVerticalStrut(25));

        card.add(nameLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(nameValue);
        card.add(Box.createVerticalStrut(10));

        card.add(sep);
        card.add(Box.createVerticalStrut(20));

        card.add(vaxHeader);
        card.add(Box.createVerticalStrut(20));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(statusCombo);

        card.add(Box.createVerticalStrut(30));
        card.add(footer);
        card.add(Box.createVerticalStrut(20));

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPanel.setMaximumSize(new Dimension(800, 60));
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton backBtn = new JButton("Back") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(80, 80, 80) : new Color(60, 60, 60));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        backBtn.setPreferredSize(new Dimension(100, 38));
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> d.dispose());

        JButton submitBtn = new JButton("Submit") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = new Color(220, 53, 69); // Red
                g2.setColor(getModel().isRollover() ? base.brighter() : base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        submitBtn.setPreferredSize(new Dimension(120, 38));
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setContentAreaFilled(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> {
            try {
                student.updateVaccinationStatus((String) statusCombo.getSelectedItem());
                JOptionPane.showMessageDialog(d, "Status saved successfully.");
                d.dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(d, "Error saving status: " + ex.getMessage());
            }
        });

        btnPanel.add(backBtn);
        btnPanel.add(submitBtn);
        card.add(btnPanel);

        // Close on click outside (Optional, but good for modal)
        container.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!card.getBounds().contains(e.getPoint())) {
                    d.dispose();
                }
            }
        });

        container.add(card);
        d.setContentPane(container);
        d.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        d.setLocationRelativeTo(null);
        d.setVisible(true);
    }
    // --- Custom Dialog System ---

    private void showCustomMessage(String title, String message, boolean isError) {
        new ElegantDialog(this, title, message, isError ? ElegantDialog.TYPE_ERROR : ElegantDialog.TYPE_INFO, null)
                .setVisible(true);
    }

    private void showCustomConfirm(String title, String message, Runnable onConfirm) {
        ElegantDialog d = new ElegantDialog(this, title, message, ElegantDialog.TYPE_CONFIRM, null);
        d.setConfirmAction(onConfirm);
        d.setVisible(true);
    }

    private void showCustomInput(String title, RunnableWithInput onConfirm) {
        // Specifically for password change mostly, but generic enough
        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        inputPanel.setOpaque(false);
        JPasswordField passField = new JPasswordField();
        styleManageField(passField);
        inputPanel.add(createManageLabel("New Password (min 6 chars):", Color.GRAY));
        inputPanel.add(passField);

        ElegantDialog d = new ElegantDialog(this, title, "", ElegantDialog.TYPE_INPUT, inputPanel);
        d.setConfirmAction(() -> {
            onConfirm.run(new String(passField.getPassword()).trim());
        });
        d.setVisible(true);
    }

    @FunctionalInterface
    interface RunnableWithInput {
        void run(String input);
    }

    private static class ElegantDialog extends JDialog {
        public static final int TYPE_INFO = 0;
        public static final int TYPE_ERROR = 1;
        public static final int TYPE_CONFIRM = 2;
        public static final int TYPE_INPUT = 3;

        private Runnable confirmAction;

        public ElegantDialog(JFrame owner, String title, String message, int type, Component customContent) {
            super(owner, true);
            setUndecorated(true);
            setBackground(new Color(0, 0, 0, 0));

            JPanel contentData = new JPanel();
            contentData.setLayout(new BoxLayout(contentData, BoxLayout.Y_AXIS));
            contentData.setOpaque(false);
            contentData.setBorder(new EmptyBorder(30, 40, 30, 40));

            // Icon
            JLabel icon = new JLabel();
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);
            // Use simple colored circle with symbol if no image
            // Or simple text icon
            // Let's rely on Title color/content

            // Title
            JLabel t = new JLabel(title);
            t.setFont(new Font("SansSerif", Font.BOLD, 22));
            t.setForeground(type == TYPE_ERROR ? new Color(255, 80, 80) : Color.WHITE);
            t.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentData.add(t);
            contentData.add(Box.createVerticalStrut(20));

            // Message
            if (message != null && !message.isEmpty()) {
                JTextArea msg = new JTextArea(message);
                msg.setFont(new Font("SansSerif", Font.PLAIN, 15));
                msg.setForeground(new Color(200, 200, 200));
                msg.setLineWrap(true);
                msg.setWrapStyleWord(true);
                msg.setOpaque(false);
                msg.setEditable(false);
                msg.setAlignmentX(Component.CENTER_ALIGNMENT);
                // Center text hack
                // Let's use JLabel with HTML for centering
                JLabel msgLbl = new JLabel(
                        "<html><div style='text-align: center; width: 300px;'>" + message + "</div></html>");
                msgLbl.setFont(new Font("SansSerif", Font.PLAIN, 15));
                msgLbl.setForeground(new Color(200, 200, 200));
                msgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentData.add(msgLbl);
                contentData.add(Box.createVerticalStrut(20));
            }

            // Custom Content (Input)
            if (customContent != null) {
                customContent.setMaximumSize(new Dimension(300, 100)); // Limit size
                contentData.add(customContent);
                contentData.add(Box.createVerticalStrut(20));
            }

            // Buttons
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            btns.setOpaque(false);
            btns.setAlignmentX(Component.CENTER_ALIGNMENT);

            if (type == TYPE_CONFIRM || type == TYPE_INPUT) { // Yes/No or OK/Cancel
                JButton cancel = createButton("Cancel", new Color(60, 65, 75));
                cancel.addActionListener(e -> dispose());
                btns.add(cancel);

                JButton ok = createButton(type == TYPE_INPUT ? "Submit" : "Confirm",
                        type == TYPE_ERROR ? new Color(255, 80, 80) : new Color(30, 136, 229)); // Red or Blue
                ok.addActionListener(e -> {
                    if (confirmAction != null)
                        confirmAction.run();
                    dispose();
                });
                btns.add(ok);
            } else { // OK only
                JButton ok = createButton("OK", new Color(30, 136, 229));
                ok.addActionListener(e -> dispose());
                btns.add(ok);
            }

            contentData.add(btns);

            // Main Container with rounded bg
            JPanel container = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Shadow/Glow (Simulated by borders usually, but let's keep simple: Dark Box)
                    g2.setColor(new Color(32, 36, 42));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                    // Border
                    g2.setColor(new Color(60, 65, 75));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);

                    g2.dispose();
                }
            };
            container.setOpaque(false);
            container.add(contentData, BorderLayout.CENTER);

            setContentPane(container);
            pack();
            setLocationRelativeTo(owner);
        }

        public void setConfirmAction(Runnable r) {
            this.confirmAction = r;
        }

        private JButton createButton(String text, Color bg) {
            JButton b = new JButton(text) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Color c = bg;
                    if (getModel().isRollover())
                        c = c.brighter();
                    g2.setColor(c);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight()); // Pill

                    g2.setColor(Color.WHITE);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(getText())) / 2;
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(getText(), x, y - 1);
                    g2.dispose();
                }
            };
            b.setFont(new Font("SansSerif", Font.BOLD, 14));
            b.setPreferredSize(new Dimension(100, 40));
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setContentAreaFilled(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }
    }
}
