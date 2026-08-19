package gui.components;

import javax.swing.*;
import java.awt.*;
import gui.theme.UITheme;

public class RoundedPanel extends JPanel {
    private final int radius;
    private Color bg;
    private boolean showBorder = false;
    private boolean showShadow = false;

    public RoundedPanel(int radius, Color bg) {
        this.radius = radius;
        this.bg = bg;
        setOpaque(false);
    }
    
    public RoundedPanel(int radius) {
        this(radius, new Color(0, 0, 0, 0));
    }
    
    public RoundedPanel withBorder() {
        this.showBorder = true;
        return this;
    }
    
    public RoundedPanel withShadow() {
        this.showShadow = true;
        return this;
    }

    @Override
    public void setBackground(Color bg) {
        this.bg = bg;
        super.setBackground(bg);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();
        
        if (showShadow) {
            g2.setColor(UITheme.SHADOW_COLOR);
            g2.fillRoundRect(3, 4, w - 3, h - 3, radius, radius);
        }
        
        g2.setColor(bg != null ? bg : getBackground());
        g2.fillRoundRect(0, 0, w, h, radius, radius);
        
        if (showBorder) {
            g2.setColor(UITheme.GLASS_BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
        }
        
        g2.dispose();
        super.paintComponent(g);
    }
}


