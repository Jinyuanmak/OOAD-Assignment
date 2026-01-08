package com.university.parking.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Custom text field with rounded borders and focus effects.
 * Provides modern styling with configurable border radius and colors.
 * 
 * Requirements: 4.1, 4.4
 */
public class StyledTextField extends JTextField {
    
    private Color borderColor;
    private Color focusBorderColor;
    private int borderRadius;
    private boolean hasFocus = false;
    
    // Default padding values
    private static final int PADDING_TOP = 8;
    private static final int PADDING_LEFT = 12;
    private static final int PADDING_BOTTOM = 8;
    private static final int PADDING_RIGHT = 12;
    
    /**
     * Creates a styled text field with default settings.
     */
    public StyledTextField() {
        this(15);
    }
    
    /**
     * Creates a styled text field with the specified number of columns.
     * 
     * @param columns the number of columns
     */
    public StyledTextField(int columns) {
        super(columns);
        this.borderColor = ThemeManager.TEXT_SECONDARY;
        this.focusBorderColor = ThemeManager.PRIMARY;
        this.borderRadius = ThemeManager.BORDER_RADIUS;
        
        initializeTextField();
    }
    
    private void initializeTextField() {
        setOpaque(false);
        setFont(ThemeManager.FONT_BODY);
        setForeground(ThemeManager.TEXT_PRIMARY);
        setCaretColor(ThemeManager.PRIMARY);
        
        // Set padding using EmptyBorder
        setBorder(new EmptyBorder(PADDING_TOP, PADDING_LEFT, PADDING_BOTTOM, PADDING_RIGHT));
        
        // Add focus listener for border color change
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                hasFocus = true;
                repaint();
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                hasFocus = false;
                repaint();
            }
        });
    }
    
    /**
     * Gets the border radius.
     * 
     * @return the border radius in pixels
     */
    public int getBorderRadius() {
        return borderRadius;
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
     * Gets the normal border color.
     * 
     * @return the border color
     */
    public Color getBorderColor() {
        return borderColor;
    }
    
    /**
     * Sets the normal border color.
     * 
     * @param color the border color
     */
    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }
    
    /**
     * Gets the focus border color.
     * 
     * @return the focus border color
     */
    public Color getFocusBorderColor() {
        return focusBorderColor;
    }
    
    /**
     * Sets the border color when focused.
     * 
     * @param color the focus border color
     */
    public void setFocusBorderColor(Color color) {
        this.focusBorderColor = color;
        repaint();
    }
    
    /**
     * Gets the padding insets.
     * 
     * @return the padding insets
     */
    public Insets getPaddingInsets() {
        return new Insets(PADDING_TOP, PADDING_LEFT, PADDING_BOTTOM, PADDING_RIGHT);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw white background with rounded corners
        g2d.setColor(ThemeManager.BG_WHITE);
        g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, borderRadius, borderRadius));
        
        g2d.dispose();
        
        super.paintComponent(g);
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Choose border color based on focus state
        Color currentBorderColor = hasFocus ? focusBorderColor : borderColor;
        
        // Draw thicker border when focused
        int strokeWidth = hasFocus ? 2 : 1;
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.setColor(currentBorderColor);
        g2d.draw(new RoundRectangle2D.Float(
            strokeWidth / 2f, 
            strokeWidth / 2f, 
            getWidth() - strokeWidth, 
            getHeight() - strokeWidth, 
            borderRadius, 
            borderRadius
        ));
        
        g2d.dispose();
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        // Ensure minimum height for better appearance
        return new Dimension(size.width, Math.max(size.height, 36));
    }
}
