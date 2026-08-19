package gui;

import users.*;
import util.ValidationUtil;
import util.LogManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class LoginFrame extends JFrame {

    // Components
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JButton loginButton; // The arrow button
    private TriangleButton loginToggleBtn;
    private JLabel loginTitleLabel;
    private JLabel loginSubLabel;
    private JPanel loginDropdown;
    private boolean loginVisible = false;
    private Timer animationTimer;
    private float currentAlpha = 0.0f;
    private JButton aboutTopBtn;
    private JButton homeTopBtn;
    private final CardLayout centerLayout = new CardLayout();
    private final JPanel centerCards = new JPanel(centerLayout);

    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_ABOUT = "ABOUT";
    private static final String ABOUT_TEXT = "The Assessment & Feedback System is a centralised academic platform designed to support the full "
            +
            "assessment lifecycle. It enables the management of assessments, recording of marks, and delivery of " +
            "structured feedback in a consistent and transparent manner. The system supports students, lecturers, " +
            "academic leaders, and administrators by providing clear oversight of assessment activities, improving " +
            "feedback quality, and encouraging continuous academic improvement.";

    // Assets
    private BufferedImage bgImage;
    private BufferedImage campusImage;
    private BufferedImage logoImage;
    private JPanel bgPanel;
    private boolean showCampusBg = false;

    // Colors
    private static final Color TEXT_DARK = new Color(33, 37, 41);
    private static final Color TEXT_MUTED = new Color(100, 100, 100);
    private static final Color ACCENT_BLUE = new Color(0, 122, 255);
    private static final Color PLACEHOLDER_COLOR = new Color(255, 255, 255, 160);
    private static final int PILL_HEIGHT = 90;
    private static final int LOGIN_PILL_WIDTH = 480;
    private static final int LOGIN_DROPDOWN_HEIGHT = 250;
    private static final String ID_PLACEHOLDER = "Enter ID";
    private static final String PASSWORD_PLACEHOLDER = "Password";

    public LoginFrame() {
        setTitle("APU Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);

        loadResources();

        JLayeredPane layeredPane = new JLayeredPane();
        setContentPane(layeredPane);
        layeredPane.setLayout(new OverlayLayout(layeredPane));

        // 1. Background
        bgPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage img = showCampusBg && campusImage != null ? campusImage : bgImage;
                if (img != null) {
                    // Draw image covering the whole screen, maintaining aspect ratio or stretch?
                    // "Stretch" usually looks best for abstract BGs
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                } else {
                    g.setColor(new Color(10, 20, 50));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }

                // Dark overlay for text readability
                g.setColor(new Color(0, 0, 0, 80));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // 2. Content
        JPanel glassPane = new JPanel(new BorderLayout());
        glassPane.setOpaque(false);
        glassPane.setBorder(new EmptyBorder(40, 60, 40, 60));

        // --- Top Bar (Logo) ---
        // "Fix the ratio and scaling... APU logo is very big"
        JLabel logoLbl = new JLabel();
        if (logoImage != null) {
            // Smaller scale: ~150px width
            int targetWidth = 160;
            int targetHeight = (int) ((double) logoImage.getHeight() / logoImage.getWidth() * targetWidth);
            Image scaled = logoImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            logoLbl.setIcon(new ImageIcon(scaled));
        } else {
            logoLbl.setText("APU LOGO");
            logoLbl.setForeground(Color.WHITE);
            logoLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        }
        logoLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoLbl.setToolTipText("Visit APU website");
        logoLbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openApuWebsite();
            }
        });

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(logoLbl, BorderLayout.WEST);

        aboutTopBtn = new JButton("ABOUT");
        styleTopBarButton(aboutTopBtn);
        aboutTopBtn.addActionListener(e -> showAboutCard());

        JButton exitBtn = new JButton("EXIT");
        styleTopBarButton(exitBtn);
        exitBtn.addActionListener(e -> System.exit(0));

        homeTopBtn = new JButton("HOME");
        styleTopBarButton(homeTopBtn);
        homeTopBtn.addActionListener(e -> showLoginCard());
        homeTopBtn.setVisible(false);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        topActions.setOpaque(false);
        topActions.add(homeTopBtn);
        topActions.add(aboutTopBtn);
        topActions.add(exitBtn);
        topBar.add(topActions, BorderLayout.EAST);

        glassPane.add(topBar, BorderLayout.NORTH);

        centerCards.setOpaque(false);
        centerCards.add(buildLoginCard(), CARD_LOGIN);
        centerCards.add(buildAboutCard(), CARD_ABOUT);
        glassPane.add(centerCards, BorderLayout.CENTER);

        // --- Dropdowns (Matches Pill Widths) ---
        loginDropdown = createLoginDropdown();
        loginDropdown.setVisible(false);
        loginDropdown.setSize(LOGIN_PILL_WIDTH, LOGIN_DROPDOWN_HEIGHT);
        getLayeredPane().add(loginDropdown, JLayeredPane.POPUP_LAYER);

        layeredPane.add(glassPane, Integer.valueOf(2));
        layeredPane.add(bgPanel, Integer.valueOf(1));
    }

    private JComponent buildLoginCard() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel contentBlock = new JPanel();
        contentBlock.setOpaque(false);
        contentBlock.setLayout(new BoxLayout(contentBlock, BoxLayout.Y_AXIS));
        contentBlock.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hero1 = new JLabel("Transforming Feedback into");
        hero1.setFont(new Font("Segoe UI", Font.BOLD, 68));
        hero1.setForeground(Color.WHITE);
        hero1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hero2 = new JLabel("Future Improvement");
        hero2.setFont(new Font("Segoe UI", Font.BOLD, 68));
        hero2.setForeground(Color.WHITE);
        hero2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("A structured assessment and feedback system for academic excellence");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        sub.setForeground(new Color(230, 230, 255));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pillRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pillRow.setOpaque(false);
        pillRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        pillRow.add(buildPill("Login", "Access your assessments and feedback."));

        contentBlock.add(hero1);
        contentBlock.add(hero2);
        contentBlock.add(Box.createVerticalStrut(15));
        contentBlock.add(sub);
        contentBlock.add(Box.createVerticalStrut(60));
        contentBlock.add(pillRow);

        center.add(contentBlock);
        return center;
    }

    private JComponent buildAboutCard() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("About the System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 54));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel body = new JLabel("<html><body style='width:900px'>" + ABOUT_TEXT + "</body></html>");
        body.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        body.setForeground(new Color(235, 235, 235));
        body.setHorizontalAlignment(SwingConstants.LEFT);
        body.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(body);

        center.add(content);

        return center;
    }

    private JPanel buildPill(String title, String sub) {
        JPanel pill = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int r = (int) (255 - (235 * currentAlpha));
                int gr = (int) (255 - (230 * currentAlpha));
                int b = (int) (255 - (220 * currentAlpha));
                int a = (int) (255 - (45 * currentAlpha));

                g2.setColor(new Color(r, gr, b, a));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

                if (currentAlpha > 0.1f) {
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.setColor(new Color(0, 122, 255, (int) (200 * currentAlpha)));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 40, 40);
                }
                g2.dispose();
            }
        };
        pill.setOpaque(false);
        Dimension pillSize = new Dimension(LOGIN_PILL_WIDTH, PILL_HEIGHT);
        pill.setPreferredSize(pillSize);
        pill.setMinimumSize(pillSize);
        pill.setMaximumSize(pillSize);
        pill.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        JPanel textCont = new JPanel();
        textCont.setLayout(new BoxLayout(textCont, BoxLayout.Y_AXIS));
        textCont.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 16));
        t.setForeground(TEXT_DARK);

        JLabel s = new JLabel(sub);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        s.setForeground(TEXT_MUTED);

        textCont.add(Box.createVerticalGlue());
        textCont.add(t);
        textCont.add(s);
        textCont.add(Box.createVerticalGlue());

        pill.add(textCont, BorderLayout.CENTER);

        TriangleButton btn = createTriangleButton(false);
        btn.addActionListener(e -> toggleDropdown(pill, btn));
        pill.add(btn, BorderLayout.EAST);

        loginTitleLabel = t;
        loginSubLabel = s;
        loginToggleBtn = btn;
        return pill;
    }

    private void toggleDropdown(JPanel parentPill, TriangleButton btn) {
        if (animationTimer != null && animationTimer.isRunning()) {
            return;
        }

        final Point loc = SwingUtilities.convertPoint(parentPill.getParent(), parentPill.getLocation(),
                getLayeredPane());

        if (!loginVisible) {
            loginDropdown.setVisible(true);
            loginVisible = true;
            btn.setExpanded(true);

            animationTimer = new Timer(10, new ActionListener() {
                int currentH = 0;

                public void actionPerformed(ActionEvent e) {
                    currentH += 15;
                    currentAlpha += 0.06f;
                    if (currentH >= LOGIN_DROPDOWN_HEIGHT) {
                        currentH = LOGIN_DROPDOWN_HEIGHT;
                        currentAlpha = 1.0f;
                        animationTimer.stop();
                    }
                    loginDropdown.setBounds(loc.x, loc.y + parentPill.getHeight() + 5, parentPill.getWidth(),
                            currentH);
                    ((GlassPanel) loginDropdown).setTransitionAlpha(currentAlpha);
                    updatePillTextColors();
                    parentPill.repaint();
                }
            });
        } else {
            animationTimer = new Timer(10, new ActionListener() {
                int currentH = loginDropdown.getHeight();

                public void actionPerformed(ActionEvent e) {
                    currentH -= 15;
                    currentAlpha -= 0.06f;
                    if (currentH <= 0) {
                        currentH = 0;
                        currentAlpha = 0.0f;
                        loginDropdown.setVisible(false);
                        loginVisible = false;
                        btn.setExpanded(false);
                        animationTimer.stop();
                    }
                    loginDropdown.setBounds(loc.x, loc.y + parentPill.getHeight() + 5, parentPill.getWidth(),
                            currentH);
                    ((GlassPanel) loginDropdown).setTransitionAlpha(currentAlpha);
                    updatePillTextColors();
                    parentPill.repaint();
                }
            });
        }
        animationTimer.start();
    }

    private void updatePillTextColors() {
        if (loginTitleLabel == null || loginSubLabel == null) {
            return;
        }
        float t = Math.min(1.0f, Math.max(0.0f, currentAlpha));
        loginTitleLabel.setForeground(blendColor(TEXT_DARK, Color.WHITE, t));
        loginSubLabel.setForeground(blendColor(TEXT_MUTED, Color.WHITE, t));
    }

    private Color blendColor(Color from, Color to, float t) {
        int r = (int) (from.getRed() + (to.getRed() - from.getRed()) * t);
        int g = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * t);
        int b = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * t);
        int a = (int) (from.getAlpha() + (to.getAlpha() - from.getAlpha()) * t);
        return new Color(r, g, b, a);
    }

    private JPanel createLoginDropdown() {
        GlassPanel p = new GlassPanel(40);
        p.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 25));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        userIdField = new PlaceholderTextField(22, ID_PLACEHOLDER);
        styleInput(userIdField, ID_PLACEHOLDER);

        passwordField = new PlaceholderPasswordField(22, PASSWORD_PLACEHOLDER);
        styleInput(passwordField, PASSWORD_PLACEHOLDER);

        loginButton = new JButton("Login");
        loginButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (b.getModel().isPressed()) {
                    g2.setColor(ACCENT_BLUE.darker());
                } else if (b.getModel().isRollover()) {
                    g2.setColor(new Color(60, 150, 255));
                } else {
                    g2.setColor(ACCENT_BLUE);
                }
                g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 10, 10);
                g2.dispose();
                super.paint(g, c);
            }
        });
        loginButton.setPreferredSize(new Dimension(320, 45));
        loginButton.setBackground(ACCENT_BLUE);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setContentAreaFilled(false);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(new EmptyBorder(8, 16, 8, 16));

        p.add(userIdField);
        p.add(passwordField);
        p.add(loginButton);

        loginButton.addActionListener(e -> attemptLogin());
        return p;
    }

    private void styleTopBarButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(new Color(255, 255, 255, 180));
        button.setContentAreaFilled(false);
        button.setBorder(null);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void openApuWebsite() {
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                JOptionPane.showMessageDialog(this, "Opening links is not supported on this system.", "APU",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Desktop.getDesktop().browse(new URI("https://www.apu.edu.my/"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to open the APU website.", "APU",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void styleInput(JTextField f, String ph) {
        f.setPreferredSize(new Dimension(380, 45));
        f.setBackground(new Color(20, 25, 35));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setOpaque(false);
        f.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(255, 255, 255, 70), 1, 12),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        f.setToolTipText(ph);
    }

    private void loadResources() {
        try {
            bgImage = ImageIO.read(new File("resources/login_bg.png"));
            campusImage = ImageIO.read(new File("resources/campus.jpg"));
            logoImage = ImageIO.read(new File("resources/apu_logo.png"));
        } catch (IOException e) {
            System.err.println("Failed to load images: " + e.getMessage());
        }
    }

    private TriangleButton createTriangleButton(boolean expanded) {
        TriangleButton btn = new TriangleButton(ACCENT_BLUE, expanded);
        btn.setPreferredSize(new Dimension(24, 24));
        btn.setMinimumSize(new Dimension(24, 24));
        btn.setMaximumSize(new Dimension(24, 24));
        return btn;
    }

    private void attemptLogin() {
        String uid = userIdField.getText().trim();
        String pwd = new String(passwordField.getPassword());

        if (ValidationUtil.isEmpty(uid) || ValidationUtil.isEmpty(pwd)) {
            showErrorDialog("Please enter ID and Password.");
            return;
        }

        loginButton.setEnabled(false); // simple busy indication

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                Thread.sleep(600);
                return util.LoginManager.authenticate(uid, pwd);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                try {
                    User user = get();
                    routeUser(user);
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                    handleLoginFailure(e.getCause() == null ? "Login failed." : e.getCause().getMessage());
                }
            }
        }.execute();
    }

    private void handleLoginFailure(String message) {
        showErrorDialog(message);
        if (passwordField != null) {
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }

    private void showAboutCard() {
        hideLoginDropdown();
        centerLayout.show(centerCards, CARD_ABOUT);
        showCampusBg = true;
        if (bgPanel != null) {
            bgPanel.repaint();
        }
        if (aboutTopBtn != null) {
            aboutTopBtn.setVisible(false);
        }
        if (homeTopBtn != null) {
            homeTopBtn.setVisible(true);
        }
    }

    private void showLoginCard() {
        centerLayout.show(centerCards, CARD_LOGIN);
        showCampusBg = false;
        if (bgPanel != null) {
            bgPanel.repaint();
        }
        if (homeTopBtn != null) {
            homeTopBtn.setVisible(false);
        }
        if (aboutTopBtn != null) {
            aboutTopBtn.setVisible(true);
        }
    }

    private void hideLoginDropdown() {
        if (loginDropdown != null) {
            loginDropdown.setVisible(false);
        }
        loginVisible = false;
        currentAlpha = 0.0f;
        updatePillTextColors();
        if (loginToggleBtn != null) {
            loginToggleBtn.setExpanded(false);
        }
    }

    private void showErrorDialog(String msg) {
        JDialog dialog = new JDialog(this, "Login Failed", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 25, 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(40, 43, 48));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 14, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Login Failed");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(new Color(220, 80, 80));
        JButton close = createDialogTextButton("X");
        close.addActionListener(e -> dialog.dispose());
        header.add(title, BorderLayout.WEST);
        header.add(close, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(10, 0));
        body.setOpaque(false);
        JLabel icon = new JLabel(UIManager.getIcon("OptionPane.errorIcon"));
        body.add(icon, BorderLayout.WEST);

        JLabel message = new JLabel("<html><body style='width:260px'>" + safe(msg) + "</body></html>");
        message.setForeground(new Color(235, 235, 235));
        message.setFont(new Font("SansSerif", Font.PLAIN, 13));
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

    private static JButton createDialogTextButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(new Color(160, 160, 165));
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
                    g2.setColor(new Color(60, 150, 255));
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
        b.setBorder(new EmptyBorder(6, 18, 6, 18));
        return b;
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void routeUser(User user) {
        dispose();
        Runnable onLogout = () -> new LoginFrame().setVisible(true);

        if (user instanceof AdminStaff) {
            LogManager.setCurrentAdmin(user.getId(), user.getName());
            LogManager.logLogin(user.getId(), user.getName());
            new AdminFrame(user).setVisible(true);
        } else if (user instanceof AcademicLeader) {
            new LeaderFrame((AcademicLeader) user, onLogout).setVisible(true);
        } else if (user instanceof Lecturer) {
            new LecturerFrame((Lecturer) user).setVisible(true);
        } else if (user instanceof Student) {
            new StudentFrame((Student) user).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, "Role not implemented yet.");
            onLogout.run();
        }
    }

    private static final class TriangleButton extends JButton {
        private final Color color;
        private boolean expanded;

        private TriangleButton(Color color, boolean expanded) {
            this.color = color;
            this.expanded = expanded;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        private void setExpanded(boolean expanded) {
            this.expanded = expanded;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);

            int w = getWidth();
            int h = getHeight();
            int size = Math.min(w, h) / 2;
            int cx = w / 2;
            int cy = h / 2;

            Polygon triangle = new Polygon();
            if (expanded) {
                triangle.addPoint(cx - size / 2, cy - size / 4);
                triangle.addPoint(cx + size / 2, cy - size / 4);
                triangle.addPoint(cx, cy + size / 2);
            } else {
                triangle.addPoint(cx - size / 4, cy - size / 2);
                triangle.addPoint(cx - size / 4, cy + size / 2);
                triangle.addPoint(cx + size / 2, cy);
            }

            g2.fillPolygon(triangle);
            g2.dispose();
        }
    }

    private static final class GlassPanel extends JPanel {
        private final int radius;
        private float transitionAlpha = 0.0f;

        private GlassPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        public void setTransitionAlpha(float alpha) {
            this.transitionAlpha = Math.min(1.0f, Math.max(0.0f, alpha));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(new Color(20, 25, 35, (int) (210 * transitionAlpha)));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(new Color(0, 122, 255, (int) (200 * transitionAlpha)));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, radius, radius);
            g2.dispose();
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape clip = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setClip(clip);
            super.paintChildren(g2);
            g2.dispose();
        }
    }

    private static final class PlaceholderTextField extends JTextField {
        private final String placeholder;

        private PlaceholderTextField(int columns, String placeholder) {
            super(columns);
            this.placeholder = placeholder;
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaintContainer();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    select(0, 0);
                    repaintContainer();
                }
            });
            getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    repaintContainer();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    repaintContainer();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    repaintContainer();
                }
            });
        }

        private void repaintContainer() {
            Container parent = getParent();
            if (parent != null) {
                parent.repaint();
            } else {
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!isFocusOwner() && getText().isEmpty() && placeholder != null && !placeholder.isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(PLACEHOLDER_COLOR);
                g2.setFont(getFont());
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int x = insets.left;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, x, y);
                g2.dispose();
            }
        }
    }

    private static final class PlaceholderPasswordField extends JPasswordField {
        private final String placeholder;

        private PlaceholderPasswordField(int columns, String placeholder) {
            super(columns);
            this.placeholder = placeholder;
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaintContainer();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    select(0, 0);
                    repaintContainer();
                }
            });
            getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    repaintContainer();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    repaintContainer();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    repaintContainer();
                }
            });
        }

        private void repaintContainer() {
            Container parent = getParent();
            if (parent != null) {
                parent.repaint();
            } else {
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!isFocusOwner() && getPassword().length == 0 && placeholder != null && !placeholder.isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(PLACEHOLDER_COLOR);
                g2.setFont(getFont());
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int x = insets.left;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, x, y);
                g2.dispose();
            }
        }
    }

    private static final class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;

        private RoundedBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = thickness;
            return insets;
        }
    }

}
