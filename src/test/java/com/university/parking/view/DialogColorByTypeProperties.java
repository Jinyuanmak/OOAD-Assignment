package com.university.parking.view;

import java.awt.Color;

import net.jqwik.api.Property;

/**
 * Property-based tests for Dialog Color By Type.
 * 
 * Feature: gui-modernization, Property 13: Dialog Color By Type
 * Validates: Requirements 9.1, 9.2
 * 
 * For any StyledDialog of type SUCCESS, the accent color SHALL be ThemeManager.SUCCESS (green).
 * For any StyledDialog of type ERROR, the accent color SHALL be ThemeManager.DANGER (red).
 */
public class DialogColorByTypeProperties {

    @Property(tries = 100)
    void successDialogHasSuccessColor() {
        StyledDialog dialog = StyledDialog.createForType(StyledDialog.DialogType.SUCCESS);
        Color accentColor = dialog.getAccentColor();
        
        assert accentColor != null : "SUCCESS dialog accent color must not be null";
        assert accentColor.equals(ThemeManager.SUCCESS) : 
            "SUCCESS dialog accent color must be ThemeManager.SUCCESS (green), got: " + accentColor;
        
        dialog.dispose();
    }
    
    @Property(tries = 100)
    void errorDialogHasDangerColor() {
        StyledDialog dialog = StyledDialog.createForType(StyledDialog.DialogType.ERROR);
        Color accentColor = dialog.getAccentColor();
        
        assert accentColor != null : "ERROR dialog accent color must not be null";
        assert accentColor.equals(ThemeManager.DANGER) : 
            "ERROR dialog accent color must be ThemeManager.DANGER (red), got: " + accentColor;
        
        dialog.dispose();
    }
    
    @Property(tries = 100)
    void warningDialogHasWarningColor() {
        StyledDialog dialog = StyledDialog.createForType(StyledDialog.DialogType.WARNING);
        Color accentColor = dialog.getAccentColor();
        
        assert accentColor != null : "WARNING dialog accent color must not be null";
        assert accentColor.equals(ThemeManager.WARNING) : 
            "WARNING dialog accent color must be ThemeManager.WARNING (orange), got: " + accentColor;
        
        dialog.dispose();
    }
    
    @Property(tries = 100)
    void infoDialogHasInfoColor() {
        StyledDialog dialog = StyledDialog.createForType(StyledDialog.DialogType.INFO);
        Color accentColor = dialog.getAccentColor();
        
        assert accentColor != null : "INFO dialog accent color must not be null";
        assert accentColor.equals(ThemeManager.INFO) : 
            "INFO dialog accent color must be ThemeManager.INFO (blue), got: " + accentColor;
        
        dialog.dispose();
    }
    
    @Property(tries = 100)
    void dialogTypeEnumHasCorrectAccentColors() {
        // Verify each DialogType enum value has the correct accent color
        assert StyledDialog.DialogType.SUCCESS.getAccentColor().equals(ThemeManager.SUCCESS) :
            "DialogType.SUCCESS must have SUCCESS accent color";
        assert StyledDialog.DialogType.ERROR.getAccentColor().equals(ThemeManager.DANGER) :
            "DialogType.ERROR must have DANGER accent color";
        assert StyledDialog.DialogType.WARNING.getAccentColor().equals(ThemeManager.WARNING) :
            "DialogType.WARNING must have WARNING accent color";
        assert StyledDialog.DialogType.INFO.getAccentColor().equals(ThemeManager.INFO) :
            "DialogType.INFO must have INFO accent color";
    }
    
    @Property(tries = 100)
    void allDialogTypesHaveNonNullAccentColors() {
        for (StyledDialog.DialogType type : StyledDialog.DialogType.values()) {
            Color accentColor = type.getAccentColor();
            assert accentColor != null : 
                "DialogType " + type.name() + " must have a non-null accent color";
        }
    }
    
    @Property(tries = 100)
    void successColorIsGreen() {
        Color successColor = ThemeManager.SUCCESS;
        // Green color should have higher green component than red and blue
        assert successColor.getGreen() > successColor.getRed() : 
            "SUCCESS color should have green > red";
        assert successColor.getGreen() > successColor.getBlue() : 
            "SUCCESS color should have green > blue";
    }
    
    @Property(tries = 100)
    void dangerColorIsRed() {
        Color dangerColor = ThemeManager.DANGER;
        // Red color should have higher red component than green and blue
        assert dangerColor.getRed() > dangerColor.getGreen() : 
            "DANGER color should have red > green";
        assert dangerColor.getRed() > dangerColor.getBlue() : 
            "DANGER color should have red > blue";
    }
}
