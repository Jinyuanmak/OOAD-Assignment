package com.university.parking.view;

import java.awt.Color;
import java.awt.Font;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for Table Styling.
 * 
 * Feature: gui-modernization
 * Property 6: Table Alternating Row Colors
 * Property 7: Table Header Styling
 * Property 8: Table Row Height Adequacy
 * 
 * Validates: Requirements 5.1, 5.2, 5.4
 */
public class StyledTableStylingProperties {

    // ==================== Property 6: Table Alternating Row Colors ====================
    
    /**
     * Property 6: Table Alternating Row Colors
     * For any StyledTable with at least 2 rows, the background color of even-indexed rows 
     * SHALL differ from the background color of odd-indexed rows.
     * 
     * Validates: Requirements 5.1
     */
    @Property(tries = 100)
    void evenAndOddRowColorsAreDifferent(@ForAll @IntRange(min = 2, max = 20) int rowCount) {
        DefaultTableModel model = createTableModel(rowCount, 3);
        StyledTable table = new StyledTable(model);
        
        Color evenRowColor = table.getEvenRowColor();
        Color oddRowColor = table.getOddRowColor();
        
        assert !evenRowColor.equals(oddRowColor) : 
            "Even row color (" + colorToString(evenRowColor) + 
            ") must differ from odd row color (" + colorToString(oddRowColor) + ")";
    }
    
    @Property(tries = 100)
    void evenRowColorIsNotNull(@ForAll @IntRange(min = 1, max = 20) int rowCount) {
        DefaultTableModel model = createTableModel(rowCount, 3);
        StyledTable table = new StyledTable(model);
        
        Color evenRowColor = table.getEvenRowColor();
        
        assert evenRowColor != null : "Even row color must not be null";
    }
    
    @Property(tries = 100)
    void oddRowColorIsNotNull(@ForAll @IntRange(min = 1, max = 20) int rowCount) {
        DefaultTableModel model = createTableModel(rowCount, 3);
        StyledTable table = new StyledTable(model);
        
        Color oddRowColor = table.getOddRowColor();
        
        assert oddRowColor != null : "Odd row color must not be null";
    }
    
    // ==================== Property 7: Table Header Styling ====================
    
    /**
     * Property 7: Table Header Styling
     * For any StyledTable, the table header background color SHALL differ from the row 
     * background colors, and the header font SHALL be bold.
     * 
     * Validates: Requirements 5.2
     */
    @Property(tries = 100)
    void headerBackgroundDiffersFromRowColors(@ForAll @IntRange(min = 1, max = 20) int rowCount) {
        DefaultTableModel model = createTableModel(rowCount, 3);
        StyledTable table = new StyledTable(model);
        
        Color headerColor = table.getHeaderColor();
        Color evenRowColor = table.getEvenRowColor();
        Color oddRowColor = table.getOddRowColor();
        
        assert !headerColor.equals(evenRowColor) : 
            "Header color (" + colorToString(headerColor) + 
            ") must differ from even row color (" + colorToString(evenRowColor) + ")";
        
        assert !headerColor.equals(oddRowColor) : 
            "Header color (" + colorToString(headerColor) + 
            ") must differ from odd row color (" + colorToString(oddRowColor) + ")";
    }
    
    @Property(tries = 100)
    void headerFontIsBold(@ForAll @IntRange(min = 1, max = 20) int rowCount) {
        DefaultTableModel model = createTableModel(rowCount, 3);
        StyledTable table = new StyledTable(model);
        
        JTableHeader header = table.getTableHeader();
        Font headerFont = header.getFont();
        
        assert headerFont.isBold() : 
            "Header font must be bold, got style: " + headerFont.getStyle();
    }
    
    @Property(tries = 100)
    void headerColorIsNotNull(@ForAll @IntRange(min = 1, max = 20) int rowCount) {
        DefaultTableModel model = createTableModel(rowCount, 3);
        StyledTable table = new StyledTable(model);
        
        Color headerColor = table.getHeaderColor();
        
        assert headerColor != null : "Header color must not be null";
    }
    
    // ==================== Property 8: Table Row Height Adequacy ====================
    
    /**
     * Property 8: Table Row Height Adequacy
     * For any StyledTable, the row height SHALL be at least 25 pixels to ensure readability.
     * 
     * Validates: Requirements 5.4
     */
    @Property(tries = 100)
    void rowHeightIsAtLeast25Pixels(@ForAll @IntRange(min = 1, max = 20) int rowCount) {
        DefaultTableModel model = createTableModel(rowCount, 3);
        StyledTable table = new StyledTable(model);
        
        int rowHeight = table.getRowHeight();
        
        assert rowHeight >= StyledTable.MIN_ROW_HEIGHT : 
            "Row height must be at least " + StyledTable.MIN_ROW_HEIGHT + 
            " pixels, got: " + rowHeight;
    }
    
    @Property(tries = 100)
    void rowHeightIsPositive(@ForAll @IntRange(min = 1, max = 20) int rowCount) {
        DefaultTableModel model = createTableModel(rowCount, 3);
        StyledTable table = new StyledTable(model);
        
        int rowHeight = table.getRowHeight();
        
        assert rowHeight > 0 : "Row height must be positive, got: " + rowHeight;
    }
    
    @Property(tries = 100)
    void minRowHeightConstantIsAtLeast25() {
        assert StyledTable.MIN_ROW_HEIGHT >= 25 : 
            "MIN_ROW_HEIGHT constant must be at least 25, got: " + StyledTable.MIN_ROW_HEIGHT;
    }
    
    // ==================== Additional Properties ====================
    
    @Property(tries = 100)
    void hoverColorIsNotNull(@ForAll @IntRange(min = 1, max = 20) int rowCount) {
        DefaultTableModel model = createTableModel(rowCount, 3);
        StyledTable table = new StyledTable(model);
        
        Color hoverColor = table.getHoverColor();
        
        assert hoverColor != null : "Hover color must not be null";
    }
    
    @Property(tries = 100)
    void tableWithEmptyModelHasCorrectStyling() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"A", "B", "C"}, 0);
        StyledTable table = new StyledTable(model);
        
        // Even with no rows, styling should be configured
        assert table.getEvenRowColor() != null : "Even row color must be set";
        assert table.getOddRowColor() != null : "Odd row color must be set";
        assert table.getHeaderColor() != null : "Header color must be set";
        assert table.getRowHeight() >= StyledTable.MIN_ROW_HEIGHT : "Row height must meet minimum";
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Creates a table model with the specified number of rows and columns.
     */
    private DefaultTableModel createTableModel(int rows, int columns) {
        String[] columnNames = new String[columns];
        for (int i = 0; i < columns; i++) {
            columnNames[i] = "Column " + (i + 1);
        }
        
        Object[][] data = new Object[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                data[i][j] = "Cell " + i + "," + j;
            }
        }
        
        return new DefaultTableModel(data, columnNames);
    }
    
    /**
     * Converts a Color to a readable string representation.
     */
    private String colorToString(Color color) {
        return String.format("RGB(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue());
    }
}
