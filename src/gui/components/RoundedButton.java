package gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import gui.theme.UITheme;

public class RoundedButton extends JButton {
    private final int radius;
    private Color normalFill;
    private Color hoverFill;
    private Color pressedFill;
    private Color borderColor;
    
    private Color currentFill;
    private float glowOpacity = 0f;
    private Timer colorTimer;
    private Timer glowTimer;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public RoundedButton(String text, Color fill, Color border, int radius) {
        super(text);
        this.radius = radius;
        this.normalFill = fill;
        this.borderColor = border;
        
        this.hoverFill = UITheme.brighten(fill, 0.15f);
        this.pressedFill = UITheme.darken(fill, 0.1f);
        this.currentFill = normalFill;

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(UITheme.SPACE_XS, UITheme.SPACE_SM, UITheme.SPACE_XS, UITheme.SPACE_SM));
        setForeground(Color.WHITE);
        setFont(UITheme.FONT_BOLD);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                animateColor(hoverFill);
                animateGlow(0.6f);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                animateColor(normalFill);
                animateGlow(0f);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                currentFill = pressedFill;
                glowOpacity = 0.3f;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                if (contains(e.getPoint())) {
                    animateColor(hoverFill);
                    animateGlow(0.6f);
                } else {
                    animateColor(normalFill);
                    animateGlow(0f);
                }
            }
        });
    }

    private void animateColor(Color target) {
        if (colorTimer != null && colorTimer.isRunning()) colorTimer.stop();
        
        colorTimer = new Timer(12, e -> {
            int r = approach(currentFill.getRed(), target.getRed(), 18);
            int g = approach(currentFill.getGreen(), target.getGreen(), 18);
            int b = approach(currentFill.getBlue(), target.getBlue(), 18);
            int a = approach(currentFill.getAlpha(), target.getAlpha(), 18);
            
            currentFill = new Color(r, g, b, a);
            repaint();
            
            if (colorsClose(currentFill, target)) {
                currentFill = target;
                ((Timer)e.getSource()).stop();
            }
        });
        colorTimer.start();
    }
    
    private void animateGlow(float target) {
        if (glowTimer != null && glowTimer.isRunning()) glowTimer.stop();
        
        glowTimer = new Timer(12, e -> {
            float diff = target - glowOpacity;
            if (Math.abs(diff) < 0.02f) {
                glowOpacity = target;
                ((Timer)e.getSource()).stop();
            } else {
                glowOpacity += diff * 0.2f;
            }
            repaint();
        });
        glowTimer.start();
    }
    
    private int approach(int current, int target, int step) {
        if (current < target) return Math.min(current + step, target);
        if (current > target) return Math.max(current - step, target);
        return current;
    }
    
    private boolean colorsClose(Color a, Color b) {
        return Math.abs(a.getRed() - b.getRed()) <= 2 &&
               Math.abs(a.getGreen() - b.getGreen()) <= 2 &&
               Math.abs(a.getBlue() - b.getBlue()) <= 2 &&
               Math.abs(a.getAlpha() - b.getAlpha()) <= 2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        
        if (glowOpacity > 0.01f && isEnabled()) {
            g2.setColor(new Color(
                currentFill.getRed(), 
                currentFill.getGreen(), 
                currentFill.getBlue(), 
                (int)(40 * glowOpacity)
            ));
            g2.fillRoundRect(-2, 2, w + 4, h, radius + 4, radius + 4);
        }
        
        g2.setColor(isEnabled() ? currentFill : UITheme.darken(currentFill, 0.4f));
        g2.fillRoundRect(0, 0, w, h, radius, radius);
        
        if (isEnabled() && !isPressed) {
            GradientPaint highlight = new GradientPaint(
                0, 0, new Color(255, 255, 255, 15),
                0, h/2, new Color(255, 255, 255, 0)
            );
            g2.setPaint(highlight);
            g2.fillRoundRect(0, 0, w, h/2, radius, radius);
        }

        if (borderColor != null) {
            g2.setColor(isEnabled() ? borderColor : UITheme.darken(borderColor, 0.3f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
    
    @Override
    public void setBackground(Color bg) {
        this.normalFill = bg;
        this.hoverFill = UITheme.brighten(bg, 0.15f);
        this.pressedFill = UITheme.darken(bg, 0.1f);
        if (!isHovered) {
            this.currentFill = bg;
        }
        repaint();
    }
}


