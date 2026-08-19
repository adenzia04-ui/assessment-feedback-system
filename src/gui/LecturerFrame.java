package gui;

import users.Lecturer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LecturerFrame extends JFrame {

    private final Lecturer lecturer;
    private final Color BG_COLOR = new Color(18, 18, 18); // Dark background
    private final Color ACCENT_COLOR = new Color(59, 130, 246); // Bright Blue
    private final Color TEXT_COLOR = new Color(240, 240, 240);

    private enum IconType {
        ASSESSMENT, MARKS, FEEDBACK, STATS
    }

    public LecturerFrame(Lecturer lecturer) {
        this.lecturer = lecturer;

        setTitle("AFS - Lecturer Dashboard");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(BG_COLOR);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);

        // --- Header ---
        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        // --- Body (Quick Access) ---
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG_COLOR);
        body.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel sectionTitle = new JLabel("Quick Access");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        sectionTitle.setForeground(ACCENT_COLOR);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(sectionTitle);
        body.add(Box.createVerticalStrut(20));

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setBackground(BG_COLOR);
        cardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsPanel.setMaximumSize(new Dimension(2000, 200)); // Limit height

        // Create Cards
        cardsPanel.add(createActionCard("Create Assessment", "Manage new assessments",
                e -> createAssessmentDialog(), IconType.ASSESSMENT));
        cardsPanel.add(createActionCard("Enter Marks", "Grade student submissions",
                e -> enterMarksDialog(), IconType.MARKS));
        cardsPanel.add(createActionCard("Feedback", "Provide or view feedback",
                e -> openFeedbackMenu(), IconType.FEEDBACK));
        cardsPanel.add(createActionCard("Statistics", "View assessment data",
                e -> viewStatsDialog(), IconType.STATS));

        body.add(cardsPanel);

        // Spacer
        body.add(Box.createVerticalGlue());

        // Lower Section (Placeholder for Schedule/Other)
        JLabel scheduleTitle = new JLabel("My Schedule");
        scheduleTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        scheduleTitle.setForeground(ACCENT_COLOR);
        scheduleTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        // body.add(scheduleTitle); // Uncomment to add more sections later

        mainPanel.add(body, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_COLOR);
        header.setBorder(new EmptyBorder(20, 40, 20, 40));

        // Profile Area
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        profilePanel.setOpaque(false);

        // Avatar Placeholder (Circle)
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                // Image could go here
            }
        };
        avatar.setPreferredSize(new Dimension(50, 50));
        avatar.setOpaque(false);

        // Text Info
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(lecturer.getUserId()); // Using ID as name proxy
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLabel.setForeground(TEXT_COLOR);
        JLabel subLabel = new JLabel("Lecturer");
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLabel.setForeground(Color.GRAY);

        infoPanel.add(nameLabel);
        infoPanel.add(subLabel);

        profilePanel.add(avatar);
        profilePanel.add(infoPanel);

        // Right Side Icons (Placeholder + Exit)
        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        iconsPanel.setOpaque(false);

        JButton profileBtn = new JButton("Profile");
        styleGhostButton(profileBtn);
        profileBtn.addActionListener(e -> openProfileDialog());

        JButton logoutBtn = new JButton("Logout");
        styleGhostButton(logoutBtn);
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        iconsPanel.add(profileBtn);
        iconsPanel.add(logoutBtn);

        header.add(profilePanel, BorderLayout.WEST);
        header.add(iconsPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createActionCard(String title, String subtitle, java.awt.event.ActionListener action,
            IconType iconType) {
        JPanel card = new JPanel() {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        action.actionPerformed(null);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient Background
                Color startColor = hovered ? ACCENT_COLOR.brighter() : ACCENT_COLOR;
                Color endColor = hovered ? new Color(37, 99, 235) : new Color(29, 78, 216); // Darker blue
                GradientPaint gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                // Draw Icon on Right
                drawIcon(g2, getWidth() - 70, getHeight() / 2 - 20, iconType);
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(new EmptyBorder(25, 25, 25, 80)); // Right padding for icon space

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 19));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("<html>" + subtitle + "</html>");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(220, 230, 255));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private void drawIcon(Graphics2D g2, int x, int y, IconType type) {
        g2.setColor(new Color(255, 255, 255, 50)); // Faint white backing for icon
        g2.fillOval(x - 10, y - 10, 60, 60);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (type) {
            case ASSESSMENT:
                // Document shape
                g2.drawRect(x + 10, y + 5, 20, 30);
                // Lines inside
                g2.drawLine(x + 15, y + 12, x + 25, y + 12);
                g2.drawLine(x + 15, y + 18, x + 25, y + 18);
                // Plus badge
                g2.fillOval(x + 22, y + 25, 14, 14);
                g2.setColor(ACCENT_COLOR);
                g2.drawLine(x + 29, y + 29, x + 29, y + 35);
                g2.drawLine(x + 26, y + 32, x + 32, y + 32);
                break;
            case MARKS:
                // Clipboard/Check
                g2.drawRect(x + 8, y + 5, 24, 30);
                g2.drawLine(x + 15, y + 5, x + 25, y + 5); // Clip
                // Checks
                g2.drawLine(x + 12, y + 15, x + 16, y + 15);
                g2.drawLine(x + 20, y + 15, x + 28, y + 15);
                g2.drawLine(x + 12, y + 22, x + 16, y + 22);
                g2.drawLine(x + 20, y + 22, x + 28, y + 22);
                break;
            case FEEDBACK:
                // Speech Bubble
                g2.drawRoundRect(x + 5, y + 8, 30, 22, 10, 10);
                int[] px = { x + 10, x + 10, x + 18 };
                int[] py = { y + 30, y + 38, y + 28 };
                g2.fillPolygon(px, py, 3);
                // Dots
                g2.fillOval(x + 12, y + 17, 3, 3);
                g2.fillOval(x + 19, y + 17, 3, 3);
                g2.fillOval(x + 26, y + 17, 3, 3);
                break;
            case STATS:
                // Bar Chart
                g2.drawRect(x + 5, y + 5, 30, 30);
                g2.fillRect(x + 10, y + 20, 6, 10);
                g2.fillRect(x + 18, y + 12, 6, 18);
                g2.fillRect(x + 26, y + 16, 6, 14);
                break;
        }
    }

    private void styleGhostButton(JButton btn) {
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(Color.LIGHT_GRAY);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
    }

    // --- Original Logic Preserved ---

    // --- Inner Dialog Classes ---

    private abstract class BaseDialog extends JDialog {
        protected final Color BG_COLOR = new Color(30, 30, 30);
        protected final Color INPUT_BG = new Color(45, 45, 45);
        protected final Color ACCENT_COLOR = new Color(59, 130, 246);
        protected JPanel mainPanel;

        public BaseDialog(JFrame parent, String title, int height) {
            super(parent, title, true);
            setUndecorated(true);
            setSize(500, height);
            setLocationRelativeTo(parent);
            setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

            mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBackground(BG_COLOR);
            mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

            // Title
            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            titlePanel.setBackground(BG_COLOR);
            titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
            lblTitle.setForeground(ACCENT_COLOR);
            titlePanel.add(lblTitle);

            mainPanel.add(titlePanel);
            mainPanel.add(Box.createVerticalStrut(30));
        }

        protected JTextField addField(String labelText) {
            JLabel label = new JLabel(labelText);
            label.setFont(new Font("SansSerif", Font.PLAIN, 12));
            label.setForeground(Color.GRAY);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);

            JTextField field = new JTextField() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(INPUT_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    super.paintComponent(g);
                }
            };
            field.setOpaque(false);
            field.setForeground(Color.WHITE);
            field.setCaretColor(Color.WHITE);
            field.setBorder(new EmptyBorder(8, 10, 8, 10));
            field.setMaximumSize(new Dimension(1000, 40));
            field.setAlignmentX(Component.LEFT_ALIGNMENT);

            mainPanel.add(label);
            mainPanel.add(Box.createVerticalStrut(5));
            mainPanel.add(field);
            mainPanel.add(Box.createVerticalStrut(15));
            return field;
        }

        protected JPasswordField addPasswordField(String labelText) {
            JLabel label = new JLabel(labelText);
            label.setFont(new Font("SansSerif", Font.PLAIN, 12));
            label.setForeground(Color.GRAY);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPasswordField field = new JPasswordField() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(INPUT_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    super.paintComponent(g);
                }
            };
            field.setOpaque(false);
            field.setForeground(Color.WHITE);
            field.setCaretColor(Color.WHITE);
            field.setBorder(new EmptyBorder(8, 10, 8, 10));
            field.setMaximumSize(new Dimension(1000, 40));
            field.setAlignmentX(Component.LEFT_ALIGNMENT);

            mainPanel.add(label);
            mainPanel.add(Box.createVerticalStrut(5));
            mainPanel.add(field);
            mainPanel.add(Box.createVerticalStrut(15));
            return field;
        }

        protected void addButtons(String submitText, java.awt.event.ActionListener submitAction) {
            mainPanel.add(Box.createVerticalStrut(15));
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            buttonPanel.setOpaque(false);
            buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton btnCancel = new JButton("Cancel");
            styleGhostButton(btnCancel);
            btnCancel.addActionListener(e -> dispose());

            JButton btnSubmit = new JButton(submitText);
            stylePrimaryButton(btnSubmit);
            btnSubmit.addActionListener(submitAction);

            buttonPanel.add(btnCancel);
            buttonPanel.add(btnSubmit);
            mainPanel.add(buttonPanel);

            add(mainPanel);
        }

        private void stylePrimaryButton(JButton btn) {
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("SansSerif", Font.BOLD, 14));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ACCENT_COLOR);
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
                    super.paint(g, c);
                }
            });
        }

        private void styleGhostButton(JButton btn) {
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setForeground(new Color(255, 100, 100)); // Reddish for cancel
            btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    private class AssessmentDialog extends BaseDialog {
        private JTextField txtId, txtModule, txtType, txtMarks;

        public AssessmentDialog() {
            super(LecturerFrame.this, "Create Assessment", 420);
            txtId = addField("Assessment ID");
            txtModule = addField("Module ID");
            txtType = addField("Type (e.g., Quiz)");
            txtMarks = addField("Max Marks");
            addButtons("Create", e -> createAssessment());
        }

        private void createAssessment() {
            try {
                int maxMarks = Integer.parseInt(txtMarks.getText().trim());
                lecturer.createAssessment(txtId.getText().trim(), txtModule.getText().trim(),
                        txtType.getText().trim(), maxMarks);
                JOptionPane.showMessageDialog(this, "Assessment created successfully!");
                dispose();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Max Marks must be a number.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class MarksDialog extends BaseDialog {
        private JTextField txtStudentId, txtAssessmentId, txtMarks;

        public MarksDialog() {
            super(LecturerFrame.this, "Enter Marks", 380);
            txtStudentId = addField("Student ID (e.g., TP123456)");
            txtAssessmentId = addField("Assessment ID (e.g., A01)");
            txtMarks = addField("Marks");
            addButtons("Submit", e -> submitMarks());
        }

        private void submitMarks() {
            try {
                int marks = Integer.parseInt(txtMarks.getText().trim());
                lecturer.enterMarks(txtStudentId.getText().trim(), txtAssessmentId.getText().trim(), marks);
                JOptionPane.showMessageDialog(this, "Marks saved successfully!");
                dispose();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Marks must be a number.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class FeedbackDialog extends BaseDialog {
        private JTextField txtStudentId, txtAssessmentId, txtFeedback;

        public FeedbackDialog() {
            super(LecturerFrame.this, "Give Feedback", 380);
            txtStudentId = addField("Student ID");
            txtAssessmentId = addField("Assessment ID");
            txtFeedback = addField("Feedback");
            addButtons("Submit", e -> submitFeedback());
        }

        private void submitFeedback() {
            try {
                lecturer.giveFeedback(txtStudentId.getText().trim(), txtAssessmentId.getText().trim(),
                        txtFeedback.getText().trim());
                JOptionPane.showMessageDialog(this, "Feedback saved successfully!");
                dispose();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ProfileDialog extends BaseDialog {
        private JPasswordField txtNewPass;
        private JPasswordField txtConfirmPass;

        public ProfileDialog() {
            super(LecturerFrame.this, "Update Password", 300);
            txtNewPass = addPasswordField("New Password");
            txtConfirmPass = addPasswordField("Confirm Password");
            addButtons("Update", e -> updatePassword());
        }

        private void updatePassword() {
            String p1 = new String(txtNewPass.getPassword()).trim();
            String p2 = new String(txtConfirmPass.getPassword()).trim();

            if (p1.isEmpty() || p2.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password fields cannot be empty.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!p1.equals(p2)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                lecturer.editProfile(lecturer.getFullName(), p1);
                JOptionPane.showMessageDialog(this, "Password updated successfully!");
                dispose();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Actions ---

    private void createAssessmentDialog() {
        new AssessmentDialog().setVisible(true);
    }

    private void enterMarksDialog() {
        new MarksDialog().setVisible(true);
    }

    private void feedbackDialog() {
        new FeedbackDialog().setVisible(true);
    }

    private void openFeedbackMenu() {
        Object[] options = { "Give Feedback", "View Student Feedback" };
        int choice = JOptionPane.showOptionDialog(
                this,
                "Choose a feedback action",
                "Feedback",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (choice == 0) {
            feedbackDialog();
        } else if (choice == 1) {
            viewCommentsDialog();
        }
    }

    private void viewCommentsDialog() {
        new CommentsDialog().setVisible(true);
    }

    private void viewStatsDialog() {
        new StatsDialog().setVisible(true);
    }

    private class CommentsDialog extends BaseDialog {
        public CommentsDialog() {
            super(LecturerFrame.this, "Student Feedback", 480);

            JLabel subtitle = new JLabel("Feedback submitted by your students");
            subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
            subtitle.setForeground(new Color(160, 160, 170));
            subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainPanel.add(subtitle);
            mainPanel.add(Box.createVerticalStrut(16));

            JPanel listPanel = new JPanel();
            listPanel.setOpaque(false);
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

            java.util.List<String[]> comments = java.util.Collections.emptyList();
            try {
                comments = lecturer.viewStudentComments();
            } catch (Exception e) {
                comments = java.util.Collections.emptyList();
            }

            if (comments.isEmpty()) {
                JPanel empty = new JPanel();
                empty.setOpaque(false);
                empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
                empty.setBorder(new EmptyBorder(25, 20, 25, 20));

                JLabel title = new JLabel("No feedback yet");
                title.setFont(new Font("SansSerif", Font.BOLD, 16));
                title.setForeground(new Color(210, 210, 220));
                title.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel hint = new JLabel("When students comment, you'll see it here.");
                hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
                hint.setForeground(new Color(140, 140, 150));
                hint.setAlignmentX(Component.CENTER_ALIGNMENT);

                empty.add(title);
                empty.add(Box.createVerticalStrut(6));
                empty.add(hint);
                listPanel.add(empty);
            } else {
                int i = 0;
                for (String[] c : comments) {
                    listPanel.add(createCommentCard(c[0], c[1], i));
                    listPanel.add(Box.createVerticalStrut(12));
                    i++;
                }
            }

            JScrollPane sp = new JScrollPane(listPanel);
            sp.setBorder(new EmptyBorder(0, 0, 0, 0));
            sp.setOpaque(false);
            sp.getViewport().setOpaque(false);
            sp.getVerticalScrollBar().setUnitIncrement(14);
            sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    this.thumbColor = new Color(70, 80, 95);
                    this.trackColor = BG_COLOR;
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
                    b.setMinimumSize(new Dimension(0, 0));
                    b.setMaximumSize(new Dimension(0, 0));
                    b.setOpaque(false);
                    return b;
                }
            });
            sp.setMaximumSize(new Dimension(1000, 260));

            mainPanel.add(sp);
            mainPanel.add(Box.createVerticalStrut(10));

            addButtons("Close", e -> dispose());
        }

        private JPanel createCommentCard(String studentId, String comment, int index) {
            Color accent = (index % 2 == 0) ? new Color(72, 129, 255) : new Color(111, 88, 255);

            JPanel card = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(36, 36, 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    g2.setColor(new Color(55, 58, 64));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                    g2.setColor(accent);
                    g2.fillRoundRect(0, 0, 6, getHeight(), 18, 18);
                    g2.dispose();
                }
            };
            card.setOpaque(false);
            card.setLayout(new BorderLayout(0, 8));
            card.setBorder(new EmptyBorder(12, 16, 12, 16));

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);

            JLabel label = new JLabel("Student");
            label.setFont(new Font("SansSerif", Font.PLAIN, 11));
            label.setForeground(new Color(150, 150, 160));

            JLabel pill = new JLabel(studentId);
            pill.setOpaque(true);
            pill.setBackground(new Color(44, 50, 62));
            pill.setForeground(new Color(200, 215, 255));
            pill.setBorder(new EmptyBorder(3, 8, 3, 8));
            pill.setFont(new Font("SansSerif", Font.BOLD, 12));

            JPanel pillWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            pillWrap.setOpaque(false);
            pillWrap.add(pill);

            top.add(label, BorderLayout.WEST);
            top.add(pillWrap, BorderLayout.EAST);

            JTextArea body = new JTextArea(comment);
            body.setEditable(false);
            body.setLineWrap(true);
            body.setWrapStyleWord(true);
            body.setOpaque(false);
            body.setForeground(new Color(230, 230, 235));
            body.setFont(new Font("SansSerif", Font.PLAIN, 13));
            body.setBorder(null);

            card.add(top, BorderLayout.NORTH);
            card.add(body, BorderLayout.CENTER);
            return card;
        }
    }

    private void openProfileDialog() {
        new ProfileDialog().setVisible(true);
    }

    private class StatsDialog extends BaseDialog {
        private JTextField txtAssessmentId;

        public StatsDialog() {
            super(LecturerFrame.this, "Assessment Statistics", 300);
            txtAssessmentId = addField("Assessment ID (e.g., A01)");
            addButtons("Show Stats", e -> showStats());
        }

        private void showStats() {
            String id = txtAssessmentId.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an Assessment ID.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                String stats = lecturer.getAssessmentStats(id);
                JOptionPane.showMessageDialog(this, stats, "Statistics for " + id,
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
