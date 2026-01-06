package com.university.parking.view;

/**
 * Utility class for input validation.
 * Provides validation methods for various input types used in the GUI.
 * 
 * Requirements: 9.4, 12.4
 */
public class InputValidator {

    /**
     * Validation result containing success status and error message.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }
    }

    /**
     * Validates that a string is not null or empty.
     * 
     * @param value the value to validate
     * @param fieldName the name of the field for error messages
     * @return validation result
     */
    public static ValidationResult validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.failure(fieldName + " cannot be empty");
        }
        return ValidationResult.success();
    }

    /**
     * Validates a license plate format.
     * License plates must be alphanumeric with optional hyphens, 2-15 characters.
     * 
     * @param licensePlate the license plate to validate
     * @return validation result
     */
    public static ValidationResult validateLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            return ValidationResult.failure("License plate cannot be empty");
        }
        
        String trimmed = licensePlate.trim();
        
        if (!trimmed.matches("[A-Za-z0-9\\-]+")) {
            return ValidationResult.failure("License plate can only contain letters, numbers, and hyphens");
        }
        
        if (trimmed.length() < 2 || trimmed.length() > 15) {
            return ValidationResult.failure("License plate must be between 2 and 15 characters");
        }
        
        return ValidationResult.success();
    }

    /**
     * Validates that a string represents a valid positive number.
     * 
     * @param value the string value to validate
     * @param fieldName the name of the field for error messages
     * @return validation result
     */
    public static ValidationResult validatePositiveNumber(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.failure(fieldName + " cannot be empty");
        }
        
        try {
            double num = Double.parseDouble(value.trim());
            if (num <= 0) {
                return ValidationResult.failure(fieldName + " must be a positive number");
            }
            return ValidationResult.success();
        } catch (NumberFormatException e) {
            return ValidationResult.failure(fieldName + " must be a valid number");
        }
    }

    /**
     * Validates that a string represents a valid non-negative number.
     * 
     * @param value the string value to validate
     * @param fieldName the name of the field for error messages
     * @return validation result
     */
    public static ValidationResult validateNonNegativeNumber(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.failure(fieldName + " cannot be empty");
        }
        
        try {
            double num = Double.parseDouble(value.trim());
            if (num < 0) {
                return ValidationResult.failure(fieldName + " cannot be negative");
            }
            return ValidationResult.success();
        } catch (NumberFormatException e) {
            return ValidationResult.failure(fieldName + " must be a valid number");
        }
    }

    /**
     * Validates that a selection has been made (not null).
     * 
     * @param selection the selected object
     * @param fieldName the name of the field for error messages
     * @return validation result
     */
    public static ValidationResult validateSelection(Object selection, String fieldName) {
        if (selection == null) {
            return ValidationResult.failure("Please select a " + fieldName);
        }
        return ValidationResult.success();
    }

    /**
     * Validates that a table row has been selected.
     * 
     * @param selectedRow the selected row index (-1 if none selected)
     * @param fieldName the name of the field for error messages
     * @return validation result
     */
    public static ValidationResult validateTableSelection(int selectedRow, String fieldName) {
        if (selectedRow < 0) {
            return ValidationResult.failure("Please select a " + fieldName);
        }
        return ValidationResult.success();
    }

    /**
     * Validates that a payment amount is sufficient.
     * 
     * @param amountPaid the amount being paid
     * @param totalDue the total amount due
     * @return validation result (note: insufficient payment is valid but returns a warning)
     */
    public static ValidationResult validatePaymentAmount(double amountPaid, double totalDue) {
        if (amountPaid <= 0) {
            return ValidationResult.failure("Payment amount must be positive");
        }
        if (amountPaid < totalDue) {
            return ValidationResult.failure(String.format(
                "Payment insufficient. Amount due: RM %.2f, Amount paid: RM %.2f", 
                totalDue, amountPaid));
        }
        return ValidationResult.success();
    }

    /**
     * Validates a spot ID format.
     * Spot IDs must follow the format "F{floor}-R{row}-S{spot}".
     * 
     * @param spotId the spot ID to validate
     * @return validation result
     */
    public static ValidationResult validateSpotId(String spotId) {
        if (spotId == null || spotId.trim().isEmpty()) {
            return ValidationResult.failure("Spot ID cannot be empty");
        }
        
        if (!spotId.matches("F\\d+-R\\d+-S\\d+")) {
            return ValidationResult.failure("Invalid spot ID format. Expected: F{floor}-R{row}-S{spot}");
        }
        
        return ValidationResult.success();
    }
}
