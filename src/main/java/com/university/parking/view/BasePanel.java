package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import com.university.parking.model.ParkingLot;

/**
 * Base panel class providing common functionality for all panels.
 * Implements common UI patterns and validation utilities.
 * Updated to use modern styled components.
 * 
 * Requirements: 4.1, 4.2, 9.1, 9.2, 9.3
 */
public abstract class BasePanel extends JPanel {
    protected ParkingLot parkingLot;

    public BasePanel(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    /**
     * Refreshes the panel data. Override in subclasses.
     */
    public abstract void refreshData();

    /**
     * Creates a titled panel with a border.
     */
    protected JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    /**
     * Creates a styled button with modern appearance.
     * Uses StyledButton with hover effects and rounded corners.
     * 
     * Requirements: 4.2
     * 
     * @param text the button text
     * @return a StyledButton instance
     */
    protected StyledButton createButton(String text) {
        return new StyledButton(text);
    }

    /**
     * Creates a styled button with custom color.
     * Uses StyledButton with hover effects and rounded corners.
     * 
     * Requirements: 4.2
     * 
     * @param text the button text
     * @param color the button background color
     * @return a StyledButton instance
     */
    protected StyledButton createButton(String text, Color color) {
        return new StyledButton(text, color);
    }

    /**
     * Creates a standard label with theme styling.
     * 
     * @param text the label text
     * @return a JLabel instance with theme font
     */
    protected JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ThemeManager.FONT_BODY);
        label.setForeground(ThemeManager.TEXT_PRIMARY);
        return label;
    }

    /**
     * Creates a styled text field with modern appearance.
     * Uses StyledTextField with rounded borders and focus effects.
     * 
     * Requirements: 4.1
     * 
     * @param columns the number of columns
     * @return a StyledTextField instance
     */
    protected StyledTextField createTextField(int columns) {
        return new StyledTextField(columns);
    }

    /**
     * Creates a styled table with modern appearance.
     * Uses StyledTable with alternating row colors and hover effects.
     * 
     * Requirements: 5.1, 5.2, 5.3, 5.4
     * 
     * @param model the table model
     * @return a StyledTable instance
     */
    protected StyledTable createStyledTable(TableModel model) {
        return new StyledTable(model);
    }

    /**
     * Creates a styled table with default model.
     * Uses StyledTable with alternating row colors and hover effects.
     * 
     * Requirements: 5.1, 5.2, 5.3, 5.4
     * 
     * @return a StyledTable instance
     */
    protected StyledTable createStyledTable() {
        return new StyledTable();
    }

    /**
     * Shows an error message dialog with modern styling.
     * Uses StyledDialog with red accent color.
     * 
     * Requirements: 9.2
     * 
     * @param message the error message to display
     */
    protected void showError(String message) {
        StyledDialog.showError(this, message);
    }

    /**
     * Shows an information message dialog with modern styling.
     * Uses StyledDialog with blue accent color.
     * 
     * Requirements: 9.3
     * 
     * @param message the information message to display
     */
    protected void showInfo(String message) {
        StyledDialog.showInfo(this, message);
    }

    /**
     * Shows a success message dialog with modern styling.
     * Uses StyledDialog with green accent color.
     * 
     * Requirements: 9.1
     * 
     * @param message the success message to display
     */
    protected void showSuccess(String message) {
        StyledDialog.showSuccess(this, message);
    }

    /**
     * Shows a warning message dialog with modern styling.
     * Uses StyledDialog with orange accent color.
     * 
     * @param message the warning message to display
     */
    protected void showWarning(String message) {
        StyledDialog.showWarning(this, message);
    }

    /**
     * Shows a confirmation dialog with modern styling.
     * Uses StyledDialog with Yes/No buttons.
     * 
     * @param message the confirmation message to display
     * @return true if user confirmed, false otherwise
     */
    protected boolean showConfirm(String message) {
        return StyledDialog.showConfirm(this, message);
    }

    /**
     * Validates that a text field is not empty.
     * Requirement 9.4, 12.4: Validate all user inputs
     * 
     * @param field the text field to validate
     * @param fieldName the name of the field for error messages
     * @return true if valid, false otherwise
     */
    protected boolean validateNotEmpty(JTextField field, String fieldName) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            showError(fieldName + " cannot be empty");
            field.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Validates that a combo box has a selection.
     * 
     * @param comboBox the combo box to validate
     * @param fieldName the name of the field for error messages
     * @return true if valid, false otherwise
     */
    protected boolean validateSelection(JComboBox<?> comboBox, String fieldName) {
        if (comboBox.getSelectedItem() == null) {
            showError("Please select a " + fieldName);
            comboBox.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Validates that a string represents a valid positive number.
     * 
     * @param value the string value to validate
     * @param fieldName the name of the field for error messages
     * @return true if valid, false otherwise
     */
    protected boolean validatePositiveNumber(String value, String fieldName) {
        try {
            double num = Double.parseDouble(value);
            if (num <= 0) {
                showError(fieldName + " must be a positive number");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showError(fieldName + " must be a valid number");
            return false;
        }
    }

    /**
     * Validates license plate format.
     * 
     * @param licensePlate the license plate to validate
     * @return true if valid, false otherwise
     */
    protected boolean validateLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            showError("License plate cannot be empty");
            return false;
        }
        // Basic validation - alphanumeric with possible hyphens
        String trimmed = licensePlate.trim();
        if (!trimmed.matches("[A-Za-z0-9\\-]+")) {
            showError("License plate can only contain letters, numbers, and hyphens");
            return false;
        }
        if (trimmed.length() < 2 || trimmed.length() > 15) {
            showError("License plate must be between 2 and 15 characters");
            return false;
        }
        return true;
    }
}
