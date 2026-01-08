package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Card component for displaying statistics with icon and value.
 * Used in dashboard views to show key metrics like occupancy, revenue, etc.
 * 
 * Requirements: 3.1, 3.2, 3.3
 */
public class DashboardCard extends JPanel {
    
    private String title;
    private String value;
    private Color accentColor;
    private Icon icon;
    
    private JLabel titleLabel;
    private JLabel valueLabel;
    private JLabel iconLabel;
    
    private static final int BORDER_RADIUS = ThemeManager.BORDER_RADIUS;
    private static final int ACCENT_BAR_WIDTH = 4;
    
    /**
     * Creates a dashboard card with the specified title, value, and accent color.
     * 
     * @param title the card title
     * @param value the initial value to display
     * @param accentColor the accent color for the left border
     */
    public DashboardCard(String title, String value, Color accentColor) {
        this.title = title;
        this.value = value;
        this.accentColor = accentColor;
        
        initializeCard();
    }

    /**
     * Creates a dashboard card with the specified title and accent color.
     * Value is initialized to "0".
     * 
     * @param title the card title
     * @param accentColor the accent color for the left border
     */
    public DashboardCard(String title, Color accentColor) {
        this(title, "0", accentColor);
    }
    
    private void initializeCard() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 5));
        setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.CARD_PADDING, 
            ThemeManager.CARD_PADDING + ACCENT_BAR_WIDTH, 
            ThemeManager.CARD_PADDING, 
            ThemeManager.CARD_PADDING
        ));
        
        // Create content panel
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.setOpaque(false);
        
        // Create title label
        titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeManager.FONT_SMALL);
        titleLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        
        // Create value label
        valueLabel = new JLabel(value);
        valueLabel.setFont(ThemeManager.FONT_HEADER);
        valueLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        
        // Create icon label (on the right side)
        iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // Add labels to content panel
        JPanel textPanel = new JPanel(new BorderLayout(0, 2));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(valueLabel, BorderLayout.CENTER);
        
        contentPanel.add(textPanel, BorderLayout.CENTER);
        contentPanel.add(iconLabel, BorderLayout.EAST);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Set preferred size
        setPreferredSize(new Dimension(200, 100));
    }
    
    /**
     * Sets the displayed value.
     * 
     * @param value the new value to display
     */
    public void setValue(String value) {
        this.value = value;
        valueLabel.setText(value);
        repaint();
    }
    
    /**
     * Gets the current displayed value.
     * 
     * @return the current value
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Sets the card icon.
     * 
     * @param icon the icon to display
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
        iconLabel.setIcon(icon);
        repaint();
    }
    
    /**
     * Gets the current icon.
     * 
     * @return the current icon
     */
    public Icon getIcon() {
        return icon;
    }
    
    /**
     * Gets the card title.
     * 
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the card title.
     * 
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
        titleLabel.setText(title);
        repaint();
    }
    
    /**
     * Gets the accent color.
     * 
     * @return the accent color
     */
    public Color getAccentColor() {
        return accentColor;
    }
    
    /**
     * Sets the accent color for the left border.
     * 
     * @param accentColor the new accent color
     */
    public void setAccentColor(Color accentColor) {
        this.accentColor = accentColor;
        repaint();
    }
    
    /**
     * Gets the title label component.
     * 
     * @return the title label
     */
    public JLabel getTitleLabel() {
        return titleLabel;
    }
    
    /**
     * Gets the value label component.
     * 
     * @return the value label
     */
    public JLabel getValueLabel() {
        return valueLabel;
    }

    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw card background with rounded corners
        g2d.setColor(ThemeManager.BG_CARD);
        g2d.fill(new RoundRectangle2D.Float(0, 0, width, height, BORDER_RADIUS, BORDER_RADIUS));
        
        // Draw subtle border
        g2d.setColor(new Color(220, 220, 220));
        g2d.draw(new RoundRectangle2D.Float(0, 0, width - 1, height - 1, BORDER_RADIUS, BORDER_RADIUS));
        
        // Draw accent color bar on the left
        g2d.setColor(accentColor);
        g2d.fillRoundRect(0, 0, ACCENT_BAR_WIDTH + BORDER_RADIUS / 2, height, BORDER_RADIUS, BORDER_RADIUS);
        // Fill the right part of the accent bar to make it straight
        g2d.fillRect(ACCENT_BAR_WIDTH, 0, BORDER_RADIUS / 2, height);
        
        g2d.dispose();
        
        super.paintComponent(g);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 100);
    }
    
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(150, 80);
    }
}
