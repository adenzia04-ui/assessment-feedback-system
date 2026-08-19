package gui.theme;

import java.awt.Color;
import java.awt.Font;

public class UITheme {
    
    public static final int SPACE_XXS = 4;
    public static final int SPACE_XS = 8;
    public static final int SPACE_SM = 12;
    public static final int SPACE_MD = 16;
    public static final int SPACE_LG = 24;
    public static final int SPACE_XL = 32;
    public static final int SPACE_XXL = 48;
    
    public static final int RADIUS_SM = 8;
    public static final int RADIUS_MD = 12;
    public static final int RADIUS_LG = 20;
    public static final int RADIUS_XL = 25;
    public static final int RADIUS_PILL = 50;
    
    public static final Color BG_DARK = new Color(10, 10, 15);
    public static final Color BG_ELEVATED = new Color(18, 18, 24);
    public static final Color BG_SURFACE = new Color(25, 25, 32);
    public static final Color CARD_BG = new Color(30, 30, 40, 200);
    public static final Color CARD_BG_SOLID = new Color(28, 28, 36);
    public static final Color GLASS_BG = new Color(255, 255, 255, 8);
    
    public static final Color PRIMARY = new Color(0, 122, 255);
    public static final Color PRIMARY_HOVER = new Color(30, 140, 255);
    public static final Color PRIMARY_PRESSED = new Color(0, 100, 220);
    public static final Color ACCENT_GLOW = new Color(0, 150, 255, 80);
    public static final Color ACCENT_SUBTLE = new Color(0, 122, 255, 20);
    
    public static final Color TEXT_WHITE = new Color(248, 248, 252);
    public static final Color TEXT_PRIMARY = new Color(245, 245, 250);
    public static final Color TEXT_SECONDARY = new Color(180, 180, 195);
    public static final Color TEXT_MUTED = new Color(130, 130, 150);
    public static final Color TEXT_DISABLED = new Color(80, 80, 95);
    
    public static final Color BORDER_DARK = new Color(55, 55, 65);
    public static final Color BORDER_SUBTLE = new Color(45, 45, 55);
    public static final Color BORDER_LIGHT = new Color(70, 70, 85);
    public static final Color GLASS_BORDER = new Color(255, 255, 255, 25);
    public static final Color DIVIDER = new Color(50, 50, 60, 150);
    
    public static final Color STATUS_ACTIVE = new Color(52, 199, 89);
    public static final Color STATUS_ACTIVE_BG = new Color(52, 199, 89, 25);
    public static final Color STATUS_WARNING = new Color(255, 204, 0);
    public static final Color STATUS_WARNING_BG = new Color(255, 204, 0, 25);
    public static final Color STATUS_ERROR = new Color(255, 69, 58);
    public static final Color STATUS_ERROR_BG = new Color(255, 69, 58, 25);
    public static final Color STATUS_DELETED = STATUS_ERROR;
    public static final Color STATUS_DELETED_BG = STATUS_ERROR_BG;
    public static final Color STATUS_WARN = STATUS_WARNING;
    
    public static final Color HOVER_OVERLAY = new Color(255, 255, 255, 8);
    public static final Color PRESS_OVERLAY = new Color(0, 0, 0, 15);
    public static final Color SELECTION_HIGHLIGHT = new Color(0, 122, 255, 35);
    public static final Color SELECTION_BORDER = new Color(0, 122, 255, 120);
    public static final Color FOCUS_RING = new Color(0, 122, 255, 100);
    
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 60);
    public static final Color SHADOW_HEAVY = new Color(0, 0, 0, 100);
    public static final Color GLOW_PRIMARY = new Color(0, 150, 255, 40);
    
    public static final Color TABLE_HEADER_BG = new Color(22, 22, 30, 250);
    public static final Color TABLE_ROW_ALT = new Color(255, 255, 255, 3);
    public static final Color BAR_BG = new Color(18, 18, 22, 250);
    public static final Color NAV_ACTIVE = new Color(0, 122, 255, 30);
    
    public static final Color[] GRADIENT_BLUE = { new Color(0, 100, 255), new Color(0, 200, 255) };
    public static final Color[] GRADIENT_PURPLE = { new Color(120, 50, 255), new Color(180, 100, 255) };
    public static final Color[] GRADIENT_ORANGE = { new Color(255, 100, 50), new Color(255, 180, 80) };
    public static final Color[] GRADIENT_TEAL = { new Color(0, 180, 150), new Color(80, 220, 180) };
    public static final Color[] GRADIENT_PINK = { new Color(255, 45, 85), new Color(255, 100, 130) };
    
    private static final String FONT_FAMILY = "Segoe UI";
    
    public static final Font FONT_DISPLAY = new Font(FONT_FAMILY, Font.BOLD, 36);
    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 28);
    public static final Font FONT_TITLE_SM = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_SUBHEADING = new Font(FONT_FAMILY, Font.BOLD, 16);
    
    public static final Font FONT_SUBTITLE = new Font(FONT_FAMILY, Font.PLAIN, 15);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_REGULAR = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_CAPTION = new Font(FONT_FAMILY, Font.PLAIN, 11);
    
    public static final Font FONT_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_BOLD_SM = new Font(FONT_FAMILY, Font.BOLD, 12);
    public static final Font FONT_TABLE_HEADER = new Font(FONT_FAMILY, Font.BOLD, 11);
    
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);
    
    public static final int ANIM_INSTANT = 100;
    public static final int ANIM_FAST = 150;
    public static final int ANIM_NORMAL = 200;
    public static final int ANIM_SLOW = 300;
    
    public static Color brighten(Color c, float factor) {
        int r = Math.min(255, (int)(c.getRed() + (255 - c.getRed()) * factor));
        int g = Math.min(255, (int)(c.getGreen() + (255 - c.getGreen()) * factor));
        int b = Math.min(255, (int)(c.getBlue() + (255 - c.getBlue()) * factor));
        return new Color(r, g, b, c.getAlpha());
    }
    
    public static Color darken(Color c, float factor) {
        int r = Math.max(0, (int)(c.getRed() * (1 - factor)));
        int g = Math.max(0, (int)(c.getGreen() * (1 - factor)));
        int b = Math.max(0, (int)(c.getBlue() * (1 - factor)));
        return new Color(r, g, b, c.getAlpha());
    }
    
    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha)));
    }
}


