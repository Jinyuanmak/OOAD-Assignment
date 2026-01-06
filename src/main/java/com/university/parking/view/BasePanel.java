package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.university.parking.model.ParkingLot;

/**
 * Base panel class providing common functionality for all panels.
 * Implements common UI patterns and validation utilities.
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
     * Creates a standard button with consistent styling.
     */
    protected JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return button;
    }

    /**
     * Creates a standard label.
     */
    protected JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return label;
    }

    /**
     * Creates a standard text field.
     */
    protected JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return field;
    }

    /**
     * Shows an error message dialog.
     * Requirement 12.4: Display clear error messages
     */
    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows an information message dialog.
     */
    protected void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows a success message dialog.
     */
    protected void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
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
