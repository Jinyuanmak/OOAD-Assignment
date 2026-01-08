package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Application header panel with logo, title, and date/time display.
 * Provides a professional header with distinct background color.
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4
 */
public class HeaderPanel extends JPanel {
    
    private JLabel logoLabel;
    private JLabel titleLabel;
    private JLabel dateTimeLabel;
    private Timer dateTimeTimer;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE, MMMM d, yyyy  HH:mm:ss");
    
    /**
     * Creates a new HeaderPanel with logo, title, and date/time display.
     */
    public HeaderPanel() {
        initializeComponents();
        setupLayout();
        startDateTimeTimer();
    }
    
    private void initializeComponents() {
        // Logo/Icon label - empty, no icon needed
        logoLabel = new JLabel("");
        logoLabel.setFont(ThemeManager.FONT_TITLE.deriveFont(32f));
        logoLabel.setForeground(ThemeManager.TEXT_LIGHT);
        
        // Application title
        titleLabel = new JLabel("University Parking Management System");
        titleLabel.setFont(ThemeManager.FONT_TITLE);
        titleLabel.setForeground(ThemeManager.TEXT_LIGHT);
        
        // Date/time display
        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(ThemeManager.FONT_BODY);
        dateTimeLabel.setForeground(ThemeManager.TEXT_LIGHT);
        updateDateTime();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.PRIMARY_DARK);
        setPreferredSize(new Dimension(0, ThemeManager.HEADER_HEIGHT));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // Left panel with logo
        JPanel leftPanel = createTransparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.add(logoLabel);
        
        // Center panel with title
        JPanel centerPanel = createTransparentPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        centerPanel.add(titleLabel);
        
        // Right panel with date/time
        JPanel rightPanel = createTransparentPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightPanel.add(dateTimeLabel);
        
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }
    
    private JPanel createTransparentPanel(FlowLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }
    
    private void startDateTimeTimer() {
        // Update date/time every second
        dateTimeTimer = new Timer(1000, e -> updateDateTime());
        dateTimeTimer.start();
    }
    
    private void updateDateTime() {
        dateTimeLabel.setText(DATE_FORMAT.format(new Date()));
    }
    
    /**
     * Stops the date/time update timer.
     * Should be called when the panel is no longer needed.
     */
    public void stopTimer() {
        if (dateTimeTimer != null && dateTimeTimer.isRunning()) {
            dateTimeTimer.stop();
        }
    }
    
    /**
     * Gets the background color of this header panel.
     * Used for testing property verification.
     * 
     * @return the background color
     */
    public Color getHeaderBackground() {
        return getBackground();
    }
    
    /**
     * Gets the title text displayed in the header.
     * 
     * @return the title text
     */
    public String getTitleText() {
        return titleLabel.getText();
    }
    
    /**
     * Gets the current date/time text displayed.
     * 
     * @return the date/time text
     */
    public String getDateTimeText() {
        return dateTimeLabel.getText();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Paint background
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Add subtle bottom border/shadow
        g2d.setColor(ThemeManager.PRIMARY.darker());
        g2d.fillRect(0, getHeight() - 2, getWidth(), 2);
        
        g2d.dispose();
    }
}
