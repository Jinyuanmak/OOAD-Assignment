package com.university.parking.view;

import java.awt.Color;
import java.awt.Font;

/**
 * Centralized theme configuration for the application.
 * Provides consistent colors, fonts, and dimensions across all GUI components.
 * 
 * Requirements: 1.1, 1.2, 1.4
 */
public final class ThemeManager {
    
    // Private constructor to prevent instantiation
    private ThemeManager() {
        throw new UnsupportedOperationException("ThemeManager is a utility class and cannot be instantiated");
    }
    
    // ==================== Primary Colors ====================
    /** Primary blue color for main UI elements */
    public static final Color PRIMARY = new Color(41, 128, 185);
    
    /** Darker blue for hover states and emphasis */
    public static final Color PRIMARY_DARK = new Color(31, 97, 141);
    
    /** Lighter blue for highlights and accents */
    public static final Color PRIMARY_LIGHT = new Color(52, 152, 219);
    
    // ==================== Accent Colors ====================
    /** Green color for success states and confirmations */
    public static final Color SUCCESS = new Color(39, 174, 96);
    
    /** Orange color for warnings and cautions */
    public static final Color WARNING = new Color(243, 156, 18);
    
    /** Red color for errors and danger states */
    public static final Color DANGER = new Color(231, 76, 60);
    
    /** Light blue for informational messages */
    public static final Color INFO = new Color(52, 152, 219);

    
    // ==================== Background Colors ====================
    /** Dark background for sidebar and navigation */
    public static final Color BG_DARK = new Color(44, 62, 80);
    
    /** Light gray background for main content areas */
    public static final Color BG_LIGHT = new Color(236, 240, 241);
    
    /** Pure white background for panels and cards */
    public static final Color BG_WHITE = new Color(255, 255, 255);
    
    /** Slightly off-white background for card components */
    public static final Color BG_CARD = new Color(248, 249, 250);
    
    // ==================== Text Colors ====================
    /** Primary dark text color for main content */
    public static final Color TEXT_PRIMARY = new Color(44, 62, 80);
    
    /** Secondary gray text color for less important content */
    public static final Color TEXT_SECONDARY = new Color(127, 140, 141);
    
    /** Light/white text color for dark backgrounds */
    public static final Color TEXT_LIGHT = new Color(255, 255, 255);
    
    // ==================== Fonts ====================
    /** Large bold font for main titles */
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    
    /** Bold font for section headers */
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);
    
    /** Bold font for sub-section headers */
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    
    /** Regular font for body text */
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    
    /** Small font for captions and minor text */
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    
    // ==================== Dimensions ====================
    /** Width of the sidebar navigation panel in pixels */
    public static final int SIDEBAR_WIDTH = 220;
    
    /** Height of the header panel in pixels */
    public static final int HEADER_HEIGHT = 60;
    
    /** Height of the status bar in pixels */
    public static final int STATUS_BAR_HEIGHT = 30;
    
    /** Standard padding for card components in pixels */
    public static final int CARD_PADDING = 15;
    
    /** Standard border radius for rounded components in pixels */
    public static final int BORDER_RADIUS = 8;
}
