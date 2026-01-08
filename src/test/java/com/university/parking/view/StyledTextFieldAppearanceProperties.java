package com.university.parking.view;

import java.awt.Insets;

import javax.swing.JTextField;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for Styled Text Field Appearance.
 * 
 * Feature: gui-modernization, Property 5: Styled Text Field Appearance
 * Validates: Requirements 4.1
 * 
 * For any StyledTextField instance, it SHALL have a border radius greater than 0 
 * and insets (padding) greater than the default JTextField insets.
 */
public class StyledTextFieldAppearanceProperties {

    @Property(tries = 100)
    void styledTextFieldHasPositiveBorderRadius(@ForAll @IntRange(min = 1, max = 50) int columns) {
        StyledTextField styledTextField = new StyledTextField(columns);
        
        int borderRadius = styledTextField.getBorderRadius();
        
        assert borderRadius > 0 : 
            "StyledTextField border radius must be greater than 0, got: " + borderRadius;
    }
    
    @Property(tries = 100)
    void styledTextFieldHasGreaterPaddingThanDefault(@ForAll @IntRange(min = 1, max = 50) int columns) {
        StyledTextField styledTextField = new StyledTextField(columns);
        JTextField defaultTextField = new JTextField(columns);
        
        Insets styledInsets = styledTextField.getPaddingInsets();
        Insets defaultInsets = defaultTextField.getInsets();
        
        // StyledTextField should have greater padding than default JTextField
        int styledTotalPadding = styledInsets.top + styledInsets.bottom + styledInsets.left + styledInsets.right;
        int defaultTotalPadding = defaultInsets.top + defaultInsets.bottom + defaultInsets.left + defaultInsets.right;
        
        assert styledTotalPadding > defaultTotalPadding : 
            "StyledTextField total padding (" + styledTotalPadding + 
            ") must be greater than default JTextField padding (" + defaultTotalPadding + ")";
    }
    
    @Property(tries = 100)
    void styledTextFieldHasNonZeroTopPadding(@ForAll @IntRange(min = 1, max = 50) int columns) {
        StyledTextField styledTextField = new StyledTextField(columns);
        
        Insets insets = styledTextField.getPaddingInsets();
        
        assert insets.top > 0 : 
            "StyledTextField top padding must be greater than 0, got: " + insets.top;
    }
    
    @Property(tries = 100)
    void styledTextFieldHasNonZeroLeftPadding(@ForAll @IntRange(min = 1, max = 50) int columns) {
        StyledTextField styledTextField = new StyledTextField(columns);
        
        Insets insets = styledTextField.getPaddingInsets();
        
        assert insets.left > 0 : 
            "StyledTextField left padding must be greater than 0, got: " + insets.left;
    }
    
    @Property(tries = 100)
    void styledTextFieldHasNonZeroBottomPadding(@ForAll @IntRange(min = 1, max = 50) int columns) {
        StyledTextField styledTextField = new StyledTextField(columns);
        
        Insets insets = styledTextField.getPaddingInsets();
        
        assert insets.bottom > 0 : 
            "StyledTextField bottom padding must be greater than 0, got: " + insets.bottom;
    }
    
    @Property(tries = 100)
    void styledTextFieldHasNonZeroRightPadding(@ForAll @IntRange(min = 1, max = 50) int columns) {
        StyledTextField styledTextField = new StyledTextField(columns);
        
        Insets insets = styledTextField.getPaddingInsets();
        
        assert insets.right > 0 : 
            "StyledTextField right padding must be greater than 0, got: " + insets.right;
    }
    
    @Property(tries = 100)
    void borderRadiusCanBeConfigured(@ForAll @IntRange(min = 1, max = 30) int newRadius) {
        StyledTextField styledTextField = new StyledTextField();
        
        styledTextField.setBorderRadius(newRadius);
        
        assert styledTextField.getBorderRadius() == newRadius : 
            "Border radius should be configurable, expected: " + newRadius + 
            ", got: " + styledTextField.getBorderRadius();
    }
    
    @Property(tries = 100)
    void defaultBorderRadiusMatchesThemeManager() {
        StyledTextField styledTextField = new StyledTextField();
        
        int expectedRadius = ThemeManager.BORDER_RADIUS;
        int actualRadius = styledTextField.getBorderRadius();
        
        assert actualRadius == expectedRadius : 
            "Default border radius should match ThemeManager.BORDER_RADIUS (" + expectedRadius + 
            "), got: " + actualRadius;
    }
}
