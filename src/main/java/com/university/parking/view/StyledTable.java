package com.university.parking.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Enhanced table with alternating row colors, styled header, and hover effects.
 * Provides modern styling consistent with the application theme.
 * 
 * Requirements: 5.1, 5.2, 5.3, 5.4
 */
public class StyledTable extends JTable {
    
    private Color evenRowColor;
    private Color oddRowColor;
    private Color hoverColor;
    private Color headerColor;
    private int hoveredRow = -1;
    
    /** Minimum row height for readability */
    public static final int MIN_ROW_HEIGHT = 25;
    
    /**
     * Creates a styled table with the specified model.
     * 
     * @param model the table model
     */
    public StyledTable(TableModel model) {
        super(model);
        initializeTable();
    }
    
    /**
     * Creates a styled table with a default model.
     */
    public StyledTable() {
        super();
        initializeTable();
    }
    
    private void initializeTable() {
        // Set colors
        this.evenRowColor = ThemeManager.BG_WHITE;
        this.oddRowColor = ThemeManager.BG_CARD;
        this.hoverColor = new Color(ThemeManager.PRIMARY.getRed(), 
                                    ThemeManager.PRIMARY.getGreen(), 
                                    ThemeManager.PRIMARY.getBlue(), 40);
        this.headerColor = ThemeManager.PRIMARY;
        
        // Configure table appearance
        setFont(ThemeManager.FONT_BODY);
        setForeground(ThemeManager.TEXT_PRIMARY);
        setSelectionBackground(ThemeManager.PRIMARY_LIGHT);
        setSelectionForeground(ThemeManager.TEXT_LIGHT);
        setGridColor(new Color(220, 220, 220));
        setShowGrid(true);
        setShowHorizontalLines(true);
        setShowVerticalLines(false);
        
        // Set row height (minimum 25px for readability)
        setRowHeight(Math.max(MIN_ROW_HEIGHT, 30));
        
        // Configure header
        configureHeader();
        
        // Add hover effect listeners
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    repaint();
                }
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                repaint();
            }
        });
    }
    
    private void configureHeader() {
        JTableHeader header = getTableHeader();
        header.setBackground(headerColor);
        header.setForeground(ThemeManager.TEXT_LIGHT);
        header.setFont(ThemeManager.FONT_SUBHEADER);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 35));
        header.setReorderingAllowed(false);
        
        // Custom header renderer
        header.setDefaultRenderer(new StyledHeaderRenderer());
    }
    
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component comp = super.prepareRenderer(renderer, row, column);
        
        if (!isRowSelected(row)) {
            // Apply hover color if this is the hovered row
            if (row == hoveredRow) {
                comp.setBackground(hoverColor);
            } else {
                // Apply alternating row colors
                comp.setBackground(row % 2 == 0 ? evenRowColor : oddRowColor);
            }
            comp.setForeground(ThemeManager.TEXT_PRIMARY);
        }
        
        // Add padding to cells
        if (comp instanceof JComponent) {
            ((JComponent) comp).setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        }
        
        return comp;
    }
    
    /**
     * Gets the even row background color.
     * 
     * @return the even row color
     */
    public Color getEvenRowColor() {
        return evenRowColor;
    }
    
    /**
     * Sets the even row background color.
     * 
     * @param color the even row color
     */
    public void setEvenRowColor(Color color) {
        this.evenRowColor = color;
        repaint();
    }
    
    /**
     * Gets the odd row background color.
     * 
     * @return the odd row color
     */
    public Color getOddRowColor() {
        return oddRowColor;
    }
    
    /**
     * Sets the odd row background color.
     * 
     * @param color the odd row color
     */
    public void setOddRowColor(Color color) {
        this.oddRowColor = color;
        repaint();
    }
    
    /**
     * Gets the hover highlight color.
     * 
     * @return the hover color
     */
    public Color getHoverColor() {
        return hoverColor;
    }
    
    /**
     * Sets the hover highlight color.
     * 
     * @param color the hover color
     */
    public void setHoverColor(Color color) {
        this.hoverColor = color;
        repaint();
    }
    
    /**
     * Gets the header background color.
     * 
     * @return the header color
     */
    public Color getHeaderColor() {
        return headerColor;
    }
    
    /**
     * Sets the header background color.
     * 
     * @param color the header color
     */
    public void setHeaderColor(Color color) {
        this.headerColor = color;
        if (getTableHeader() != null) {
            getTableHeader().setBackground(color);
        }
        repaint();
    }
    
    /**
     * Custom header renderer with styled appearance.
     */
    private class StyledHeaderRenderer extends DefaultTableCellRenderer {
        
        public StyledHeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            setBackground(headerColor);
            setForeground(ThemeManager.TEXT_LIGHT);
            setFont(ThemeManager.FONT_SUBHEADER);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, ThemeManager.PRIMARY_DARK),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            
            return this;
        }
    }
}
