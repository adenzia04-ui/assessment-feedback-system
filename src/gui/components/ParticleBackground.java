package gui.components;

import gui.theme.UITheme;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleBackground extends JPanel {

    private final List<Particle> particles = new ArrayList<>();
    private final Timer timer;
    private final Random random = new Random();
    
    private int particleCount = 45;
    private int connectionDistance = 100;
    private int timerInterval = 25;
    
    private BufferedImage gradientCache;
    private int lastWidth, lastHeight;
    
    private boolean highQuality = true;

    public ParticleBackground() {
        setOpaque(true);
        setBackground(UITheme.BG_DARK);
        setDoubleBuffered(true);
        
        timer = new Timer(timerInterval, e -> {
            updateParticles();
            repaint();
        });
    }
    
    public void setQualityMode(boolean high) {
        this.highQuality = high;
        if (high) {
            particleCount = 50;
            connectionDistance = 110;
            timerInterval = 20;
        } else {
            particleCount = 30;
            connectionDistance = 80;
            timerInterval = 33;
        }
        timer.setDelay(timerInterval);
        
        particles.clear();
        spawnParticles(getWidth(), getHeight());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (particles.isEmpty()) {
            spawnParticles(getWidth(), getHeight());
        }
        timer.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        timer.stop();
    }
    
    private void spawnParticles(int w, int h) {
        if (w <= 0) w = 800;
        if (h <= 0) h = 600;
        
        particles.clear();
        for (int i = 0; i < particleCount; i++) {
            particles.add(new Particle(random.nextInt(w), random.nextInt(h)));
        }
    }

    private void updateParticles() {
        int w = getWidth();
        int h = getHeight();
        for (Particle p : particles) {
            p.update(w, h);
        }
    }
    
    private void ensureGradientCache(int w, int h) {
        if (gradientCache == null || lastWidth != w || lastHeight != h) {
            gradientCache = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = gradientCache.createGraphics();
            
            GradientPaint gp = new GradientPaint(
                0, 0, UITheme.BG_DARK, 
                0, h, new Color(15, 15, 25)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
            g2.dispose();
            
            lastWidth = w;
            lastHeight = h;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;
        
        Graphics2D g2 = (Graphics2D) g;
        
        ensureGradientCache(w, h);
        g2.drawImage(gradientCache, 0, 0, null);
        
        if (highQuality) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        
        int size = particles.size();
        for (int i = 0; i < size; i++) {
            Particle p1 = particles.get(i);
            
            int checkLimit = Math.min(i + 15, size);
            for (int j = i + 1; j < checkLimit; j++) {
                Particle p2 = particles.get(j);
                
                double dx = p1.x - p2.x;
                double dy = p1.y - p2.y;
                double distSq = dx * dx + dy * dy;
                int maxDistSq = connectionDistance * connectionDistance;
                
                if (distSq < maxDistSq) {
                    double dist = Math.sqrt(distSq);
                    int alpha = (int) (30 * (1.0 - (dist / connectionDistance)));
                    if (alpha > 0) {
                        g2.setColor(new Color(100, 180, 255, alpha));
                        g2.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
                    }
                }
            }
        }
        
        g2.setColor(new Color(200, 220, 255, 50));
        for (Particle p : particles) {
            g2.fillOval((int)p.x - 1, (int)p.y - 1, 3, 3);
        }
    }
    
    private class Particle {
        double x, y, vx, vy;

        Particle(int startX, int startY) {
            x = startX;
            y = startY;
            vx = (random.nextDouble() - 0.5) * 0.6;
            vy = (random.nextDouble() - 0.5) * 0.6;
        }

        void update(int w, int h) {
            x += vx;
            y += vy;

            if (x < 0 || x > w) vx = -vx;
            if (y < 0 || y > h) vy = -vy;
            
            x = Math.max(0, Math.min(w, x));
            y = Math.max(0, Math.min(h, y));
        }
    }
}


