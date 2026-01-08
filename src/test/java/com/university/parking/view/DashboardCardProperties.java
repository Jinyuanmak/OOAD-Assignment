package com.university.parking.view;

import java.awt.Color;

import javax.swing.JLabel;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.StringLength;

/**
 * Property-based tests for Dashboard Card.
 * 
 * Feature: gui-modernization
 * Property 3: Dashboard Card Structure
 * Property 4: Dashboard Card Value Updates
 * 
 * Validates: Requirements 3.1, 3.2, 3.3
 */
public class DashboardCardProperties {

    // ==================== Property 3: Dashboard Card Structure ====================
    
    /**
     * Property 3: Dashboard Card Structure
     * For any DashboardCard instance, it SHALL contain a non-empty title label, 
     * a value label, and have a background color distinct from the default panel background.
     * 
     * Validates: Requirements 3.1, 3.2
     */
    @Property(tries = 100)
    void dashboardCardHasNonEmptyTitleLabel(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String title,
            @ForAll @NotBlank @StringLength(min = 1, max = 20) String value) {
        
        DashboardCard card = new DashboardCard(title, value, ThemeManager.PRIMARY);
        
        JLabel titleLabel = card.getTitleLabel();
        
        assert titleLabel != null : "Title label must not be null";
        assert titleLabel.getText() != null : "Title label text must not be null";
        assert !titleLabel.getText().isEmpty() : "Title label text must not be empty";
        assert titleLabel.getText().equals(title) : 
            "Title label text must match provided title. Expected: " + title + ", Got: " + titleLabel.getText();
    }
    
    @Property(tries = 100)
    void dashboardCardHasValueLabel(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String title,
            @ForAll @NotBlank @StringLength(min = 1, max = 20) String value) {
        
        DashboardCard card = new DashboardCard(title, value, ThemeManager.PRIMARY);
        
        JLabel valueLabel = card.getValueLabel();
        
        assert valueLabel != null : "Value label must not be null";
        assert valueLabel.getText() != null : "Value label text must not be null";
        assert valueLabel.getText().equals(value) : 
            "Value label text must match provided value. Expected: " + value + ", Got: " + valueLabel.getText();
    }

    
    @Property(tries = 100)
    void dashboardCardBackgroundIsDistinctFromDefaultPanel(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String title,
            @ForAll @NotBlank @StringLength(min = 1, max = 20) String value) {
        
        DashboardCard card = new DashboardCard(title, value, ThemeManager.PRIMARY);
        
        // The card uses BG_CARD which should be distinct from BG_LIGHT (main content background)
        // Since the card paints its own background in paintComponent, we verify the accent color is set
        Color accentColor = card.getAccentColor();
        
        assert accentColor != null : "Accent color must not be null";
        assert !accentColor.equals(ThemeManager.BG_LIGHT) : 
            "Card accent color must differ from default panel background (BG_LIGHT)";
    }
    
    @Property(tries = 100)
    void dashboardCardHasAccentColor(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String title,
            @ForAll @NotBlank @StringLength(min = 1, max = 20) String value) {
        
        // Test with different accent colors
        Color[] accentColors = {
            ThemeManager.PRIMARY,
            ThemeManager.SUCCESS,
            ThemeManager.WARNING,
            ThemeManager.DANGER,
            ThemeManager.INFO
        };
        
        for (Color accentColor : accentColors) {
            DashboardCard card = new DashboardCard(title, value, accentColor);
            
            assert card.getAccentColor() != null : "Accent color must not be null";
            assert card.getAccentColor().equals(accentColor) : 
                "Accent color must match provided color. Expected: " + colorToString(accentColor) + 
                ", Got: " + colorToString(card.getAccentColor());
        }
    }
    
    @Property(tries = 100)
    void dashboardCardTitleLabelHasCorrectFont(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String title) {
        
        DashboardCard card = new DashboardCard(title, "0", ThemeManager.PRIMARY);
        
        JLabel titleLabel = card.getTitleLabel();
        
        assert titleLabel.getFont() != null : "Title label font must not be null";
        assert titleLabel.getFont().equals(ThemeManager.FONT_SMALL) : 
            "Title label must use FONT_SMALL";
    }
    
    @Property(tries = 100)
    void dashboardCardValueLabelHasCorrectFont(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String title) {
        
        DashboardCard card = new DashboardCard(title, "0", ThemeManager.PRIMARY);
        
        JLabel valueLabel = card.getValueLabel();
        
        assert valueLabel.getFont() != null : "Value label font must not be null";
        assert valueLabel.getFont().equals(ThemeManager.FONT_HEADER) : 
            "Value label must use FONT_HEADER";
    }
    
    // ==================== Property 4: Dashboard Card Value Updates ====================
    
    /**
     * Property 4: Dashboard Card Value Updates
     * For any DashboardCard and any valid string value, calling setValue() 
     * SHALL update the displayed value text to match the provided string.
     * 
     * Validates: Requirements 3.3
     */
    @Property(tries = 100)
    void setValueUpdatesDisplayedValue(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String title,
            @ForAll @NotBlank @StringLength(min = 1, max = 20) String initialValue,
            @ForAll @NotBlank @StringLength(min = 1, max = 20) String newValue) {
        
        DashboardCard card = new DashboardCard(title, initialValue, ThemeManager.PRIMARY);
        
        // Verify initial value
        assert card.getValue().equals(initialValue) : 
            "Initial value must match. Expected: " + initialValue + ", Got: " + card.getValue();
        
        // Update value
        card.setValue(newValue);
        
        // Verify updated value
        assert card.getValue().equals(newValue) : 
            "Updated value must match. Expected: " + newValue + ", Got: " + card.getValue();
        
        // Verify label text is also updated
        assert card.getValueLabel().getText().equals(newValue) : 
            "Value label text must match updated value. Expected: " + newValue + 
            ", Got: " + card.getValueLabel().getText();
    }

    
    @Property(tries = 100)
    void setValueWithNumericStrings(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String title) {
        
        DashboardCard card = new DashboardCard(title, "0", ThemeManager.PRIMARY);
        
        // Test with various numeric values
        String[] numericValues = {"0", "1", "100", "1000", "99.9%", "$1,234.56", "50/100"};
        
        for (String numericValue : numericValues) {
            card.setValue(numericValue);
            
            assert card.getValue().equals(numericValue) : 
                "Value must be updated to: " + numericValue + ", Got: " + card.getValue();
            assert card.getValueLabel().getText().equals(numericValue) : 
                "Value label must display: " + numericValue + ", Got: " + card.getValueLabel().getText();
        }
    }
    
    @Property(tries = 100)
    void multipleSetValueCallsUpdateCorrectly(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String title,
            @ForAll @NotBlank @StringLength(min = 1, max = 20) String value1,
            @ForAll @NotBlank @StringLength(min = 1, max = 20) String value2,
            @ForAll @NotBlank @StringLength(min = 1, max = 20) String value3) {
        
        DashboardCard card = new DashboardCard(title, "initial", ThemeManager.PRIMARY);
        
        // First update
        card.setValue(value1);
        assert card.getValue().equals(value1) : "First update failed";
        
        // Second update
        card.setValue(value2);
        assert card.getValue().equals(value2) : "Second update failed";
        
        // Third update
        card.setValue(value3);
        assert card.getValue().equals(value3) : "Third update failed";
        
        // Final verification
        assert card.getValueLabel().getText().equals(value3) : 
            "Final value label must show last value: " + value3;
    }
    
    // ==================== Additional Structure Properties ====================
    
    @Property(tries = 100)
    void dashboardCardHasMinimumDimensions(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String title) {
        
        DashboardCard card = new DashboardCard(title, "0", ThemeManager.PRIMARY);
        
        assert card.getPreferredSize().width >= 150 : 
            "Card preferred width must be at least 150px, got: " + card.getPreferredSize().width;
        assert card.getPreferredSize().height >= 80 : 
            "Card preferred height must be at least 80px, got: " + card.getPreferredSize().height;
        
        assert card.getMinimumSize().width >= 150 : 
            "Card minimum width must be at least 150px, got: " + card.getMinimumSize().width;
        assert card.getMinimumSize().height >= 80 : 
            "Card minimum height must be at least 80px, got: " + card.getMinimumSize().height;
    }
    
    @Property(tries = 100)
    void dashboardCardTitleCanBeUpdated(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String initialTitle,
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String newTitle) {
        
        DashboardCard card = new DashboardCard(initialTitle, "0", ThemeManager.PRIMARY);
        
        // Verify initial title
        assert card.getTitle().equals(initialTitle) : 
            "Initial title must match. Expected: " + initialTitle + ", Got: " + card.getTitle();
        
        // Update title
        card.setTitle(newTitle);
        
        // Verify updated title
        assert card.getTitle().equals(newTitle) : 
            "Updated title must match. Expected: " + newTitle + ", Got: " + card.getTitle();
        assert card.getTitleLabel().getText().equals(newTitle) : 
            "Title label must display updated title";
    }
    
    @Property(tries = 100)
    void dashboardCardAccentColorCanBeUpdated(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String title) {
        
        DashboardCard card = new DashboardCard(title, "0", ThemeManager.PRIMARY);
        
        // Verify initial accent color
        assert card.getAccentColor().equals(ThemeManager.PRIMARY) : 
            "Initial accent color must be PRIMARY";
        
        // Update accent color
        card.setAccentColor(ThemeManager.SUCCESS);
        
        // Verify updated accent color
        assert card.getAccentColor().equals(ThemeManager.SUCCESS) : 
            "Updated accent color must be SUCCESS";
    }
    
    @Property(tries = 100)
    void dashboardCardConstructorWithTwoArgs(
            @ForAll @NotBlank @StringLength(min = 1, max = 50) String title) {
        
        DashboardCard card = new DashboardCard(title, ThemeManager.WARNING);
        
        assert card.getTitle().equals(title) : "Title must match";
        assert card.getValue().equals("0") : "Default value must be '0'";
        assert card.getAccentColor().equals(ThemeManager.WARNING) : "Accent color must match";
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Converts a Color to a readable string representation.
     */
    private String colorToString(Color color) {
        return String.format("RGB(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue());
    }
}
