package com.university.parking.view;

import java.awt.Color;

import net.jqwik.api.Property;

/**
 * Property-based tests for Header Background Distinction.
 * 
 * Feature: gui-modernization, Property 9: Header Background Distinction
 * Validates: Requirements 6.2
 * 
 * For any HeaderPanel and its parent container, the header background color 
 * SHALL differ from the main content panel background color.
 */
public class HeaderBackgroundDistinctionProperties {

    /**
     * Property 9: Header Background Distinction
     * 
     * For any HeaderPanel, its background color SHALL differ from the 
     * standard content panel background color (BG_LIGHT).
     */
    @Property(tries = 100)
    void headerBackgroundDiffersFromContentBackground() {
        HeaderPanel headerPanel = new HeaderPanel();
        
        Color headerBackground = headerPanel.getHeaderBackground();
        Color contentBackground = ThemeManager.BG_LIGHT;
        
        assert headerBackground != null : "Header background must not be null";
        assert !headerBackground.equals(contentBackground) : 
            "Header background (" + colorToString(headerBackground) + 
            ") must differ from content background (" + colorToString(contentBackground) + ")";
        
        // Clean up timer
        headerPanel.stopTimer();
    }
    
    /**
     * Property: Header background differs from white background
     * 
     * The header should also be distinct from pure white panels.
     */
    @Property(tries = 100)
    void headerBackgroundDiffersFromWhiteBackground() {
        HeaderPanel headerPanel = new HeaderPanel();
        
        Color headerBackground = headerPanel.getHeaderBackground();
        Color whiteBackground = ThemeManager.BG_WHITE;
        
        assert headerBackground != null : "Header background must not be null";
        assert !headerBackground.equals(whiteBackground) : 
            "Header background must differ from white background";
        
        headerPanel.stopTimer();
    }
    
    /**
     * Property: Header uses a dark/primary color scheme
     * 
     * The header should use a darker color to create visual hierarchy.
     */
    @Property(tries = 100)
    void headerBackgroundIsDarkerThanContentBackground() {
        HeaderPanel headerPanel = new HeaderPanel();
        
        Color headerBackground = headerPanel.getHeaderBackground();
        Color contentBackground = ThemeManager.BG_LIGHT;
        
        // Calculate luminance (perceived brightness)
        double headerLuminance = calculateLuminance(headerBackground);
        double contentLuminance = calculateLuminance(contentBackground);
        
        assert headerLuminance < contentLuminance : 
            "Header background should be darker than content background for visual hierarchy. " +
            "Header luminance: " + headerLuminance + ", Content luminance: " + contentLuminance;
        
        headerPanel.stopTimer();
    }
    
    /**
     * Property: Header has sufficient contrast with content area
     * 
     * There should be enough visual distinction between header and content.
     */
    @Property(tries = 100)
    void headerHasSufficientContrastWithContent() {
        HeaderPanel headerPanel = new HeaderPanel();
        
        Color headerBackground = headerPanel.getHeaderBackground();
        Color contentBackground = ThemeManager.BG_LIGHT;
        
        double contrastRatio = calculateContrastRatio(headerBackground, contentBackground);
        
        // Minimum contrast ratio for visual distinction (WCAG recommends 3:1 for large text)
        double minimumContrast = 2.0;
        
        assert contrastRatio >= minimumContrast : 
            "Header should have sufficient contrast with content. " +
            "Contrast ratio: " + contrastRatio + ", Minimum required: " + minimumContrast;
        
        headerPanel.stopTimer();
    }
    
    /**
     * Property: Header displays title text
     * 
     * The header should contain a non-empty title.
     */
    @Property(tries = 100)
    void headerDisplaysTitle() {
        HeaderPanel headerPanel = new HeaderPanel();
        
        String title = headerPanel.getTitleText();
        
        assert title != null : "Header title must not be null";
        assert !title.trim().isEmpty() : "Header title must not be empty";
        
        headerPanel.stopTimer();
    }
    
    /**
     * Property: Header displays date/time
     * 
     * The header should display current date/time information.
     */
    @Property(tries = 100)
    void headerDisplaysDateTime() {
        HeaderPanel headerPanel = new HeaderPanel();
        
        String dateTime = headerPanel.getDateTimeText();
        
        assert dateTime != null : "Header date/time must not be null";
        assert !dateTime.trim().isEmpty() : "Header date/time must not be empty";
        
        headerPanel.stopTimer();
    }
    
    /**
     * Property: Header has correct preferred height
     * 
     * The header should use the theme-defined height.
     */
    @Property(tries = 100)
    void headerHasCorrectPreferredHeight() {
        HeaderPanel headerPanel = new HeaderPanel();
        
        int preferredHeight = headerPanel.getPreferredSize().height;
        int expectedHeight = ThemeManager.HEADER_HEIGHT;
        
        assert preferredHeight == expectedHeight : 
            "Header preferred height should be " + expectedHeight + 
            " but was " + preferredHeight;
        
        headerPanel.stopTimer();
    }
    
    /**
     * Calculates the relative luminance of a color.
     * Based on WCAG 2.0 formula.
     */
    private double calculateLuminance(Color color) {
        double r = color.getRed() / 255.0;
        double g = color.getGreen() / 255.0;
        double b = color.getBlue() / 255.0;
        
        r = r <= 0.03928 ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = g <= 0.03928 ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = b <= 0.03928 ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);
        
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }
    
    /**
     * Calculates the contrast ratio between two colors.
     * Based on WCAG 2.0 formula.
     */
    private double calculateContrastRatio(Color color1, Color color2) {
        double lum1 = calculateLuminance(color1);
        double lum2 = calculateLuminance(color2);
        
        double lighter = Math.max(lum1, lum2);
        double darker = Math.min(lum1, lum2);
        
        return (lighter + 0.05) / (darker + 0.05);
    }
    
    /**
     * Helper method to convert color to readable string.
     */
    private String colorToString(Color color) {
        return String.format("RGB(%d, %d, %d)", 
            color.getRed(), color.getGreen(), color.getBlue());
    }
}
