package com.university.parking.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;

/**
 * Custom button with hover effects and modern styling.
 * Implements rounded corners and configurable colors.
 * 
 * Requirements: 4.2
 */
public class StyledButton extends JButton {
    
    private Color normalColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor;
    private int borderRadius;
    private boolean isHovered = false;
    private boolean isPressed = false;
    
    /**
     * Creates a styled button with the specified text and default primary color.
     * 
     * @param text the button text
     */
    public StyledButton(String text) {
        this(text, ThemeManager.PRIMARY);
    }
    
    /**
     * Creates a styled button with the specified text and color.
     * 
     * @param text the button text
     * @param color the base color for the button
     */
    public StyledButton(String text, Color color) {
        super(text);
        this.normalColor = color;
        this.hoverColor = darkenColor(color, 0.1f);
        this.pressedColor = darkenColor(color, 0.2f);
        this.textColor = Color.WHITE; // Use pure white for better contrast
        this.borderRadius = ThemeManager.BORDER_RADIUS;
        
        initializeButton();
    }
    
    private void initializeButton() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setFont(ThemeManager.FONT_BODY);
        setForeground(textColor);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add padding
        setMargin(new Insets(8, 16, 8, 16));
        
        // Add mouse listeners for hover and press effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }
    
    /**
     * Sets the border radius for rounded corners.
     * 
     * @param radius the border radius in pixels
     */
    public void setBorderRadius(int radius) {
        this.borderRadius = radius;
        repaint();
    }
    
    /**
     * Gets the current border radius.
     * 
     * @return the border radius in pixels
     */
    public int getBorderRadius() {
        return borderRadius;
    }
    
    /**
     * Sets the normal (default) background color.
     * 
     * @param color the normal color
     */
    public void setNormalColor(Color color) {
        this.normalColor = color;
        this.hoverColor = darkenColor(color, 0.1f);
        this.pressedColor = darkenColor(color, 0.2f);
        repaint();
    }
    
    /**
     * Gets the normal background color.
     * 
     * @return the normal color
     */
    public Color getNormalColor() {
        return normalColor;
    }
    
    /**
     * Sets the text color.
     * 
     * @param color the text color
     */
    public void setTextColor(Color color) {
        this.textColor = color;
        setForeground(color);
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Determine current color based on state
        Color currentColor;
        if (!isEnabled()) {
            // Disabled state - use lighter background
            currentColor = new Color(180, 180, 180);
            // Set text color to dark for disabled state
            setForeground(new Color(100, 100, 100));
        } else {
            // Restore normal text color when enabled
            setForeground(textColor);
            if (isPressed) {
                currentColor = pressedColor;
            } else if (isHovered) {
                currentColor = hoverColor;
            } else {
                currentColor = normalColor;
            }
        }
        
        // Draw rounded rectangle background
        g2d.setColor(currentColor);
        g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), borderRadius, borderRadius));
        
        g2d.dispose();
        
        // Paint the text
        super.paintComponent(g);
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        // Ensure minimum size for better appearance
        return new Dimension(Math.max(size.width, 80), Math.max(size.height, 32));
    }
    
    /**
     * Darkens a color by the specified factor.
     * 
     * @param color the original color
     * @param factor the darkening factor (0.0 to 1.0)
     * @return the darkened color
     */
    private Color darkenColor(Color color, float factor) {
        int r = Math.max(0, (int) (color.getRed() * (1 - factor)));
        int g = Math.max(0, (int) (color.getGreen() * (1 - factor)));
        int b = Math.max(0, (int) (color.getBlue() * (1 - factor)));
        return new Color(r, g, b);
    }
}
