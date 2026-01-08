package com.university.parking.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/**
 * Custom combo box with theme-consistent styling.
 * Applies theme colors to the combo box and dropdown list.
 * 
 * Requirements: 4.3
 * 
 * @param <E> the type of elements in this combo box
 */
public class StyledComboBox<E> extends JComboBox<E> {
    
    /**
     * Creates a styled combo box with an empty model.
     */
    public StyledComboBox() {
        super();
        initializeComboBox();
    }
    
    /**
     * Creates a styled combo box with the specified items.
     * 
     * @param items the items to populate the combo box
     */
    public StyledComboBox(E[] items) {
        super(items);
        initializeComboBox();
    }
    
    /**
     * Creates a styled combo box with the specified model.
     * 
     * @param model the combo box model
     */
    public StyledComboBox(ComboBoxModel<E> model) {
        super(model);
        initializeComboBox();
    }
    
    private void initializeComboBox() {
        setFont(ThemeManager.FONT_BODY);
        setBackground(ThemeManager.BG_WHITE);
        setForeground(ThemeManager.TEXT_PRIMARY);
        setBorder(new EmptyBorder(4, 8, 4, 8));
        
        // Set custom UI for styling
        setUI(new StyledComboBoxUI());
        
        // Style the renderer for dropdown items
        setRenderer(new StyledListCellRenderer<>());
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(size.width, Math.max(size.height, 36));
    }
    
    /**
     * Custom UI for the combo box with styled arrow button.
     */
    private class StyledComboBoxUI extends BasicComboBoxUI {
        
        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton();
            button.setText("▼");
            button.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            button.setBackground(ThemeManager.PRIMARY);
            button.setForeground(ThemeManager.TEXT_LIGHT);
            button.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return button;
        }
        
        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox) {
                @Override
                protected JScrollPane createScroller() {
                    JScrollPane scroller = super.createScroller();
                    scroller.setBorder(BorderFactory.createLineBorder(ThemeManager.PRIMARY, 1));
                    return scroller;
                }
            };
            popup.getAccessibleContext().setAccessibleParent(comboBox);
            return popup;
        }
        
        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(ThemeManager.BG_WHITE);
            g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            
            // Draw border
            Color borderColor = hasFocus ? ThemeManager.PRIMARY : ThemeManager.TEXT_SECONDARY;
            g2d.setColor(borderColor);
            g2d.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
            
            g2d.dispose();
        }
    }
    
    /**
     * Custom list cell renderer for dropdown items.
     */
    private static class StyledListCellRenderer<E> extends DefaultListCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            setFont(ThemeManager.FONT_BODY);
            setBorder(new EmptyBorder(6, 10, 6, 10));
            
            if (isSelected) {
                setBackground(ThemeManager.PRIMARY);
                setForeground(ThemeManager.TEXT_LIGHT);
            } else {
                setBackground(ThemeManager.BG_WHITE);
                setForeground(ThemeManager.TEXT_PRIMARY);
            }
            
            return this;
        }
    }
}
