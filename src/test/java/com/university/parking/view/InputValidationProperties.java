package com.university.parking.view;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

/**
 * Property-based tests for Input Validation and Error Handling.
 * 
 * Feature: parking-lot-management, Property 24: Input Validation and Error Handling
 * Validates: Requirements 9.4, 12.4
 */
public class InputValidationProperties {

    @Property(tries = 5)
    void emptyStringFailsNotEmptyValidation() {
        InputValidator.ValidationResult result = InputValidator.validateNotEmpty("", "Field");
        assert !result.isValid() : "Empty string should fail validation";
        assert result.getErrorMessage() != null : "Should have error message";
    }

    @Property(tries = 5)
    void nonEmptyStringPassesNotEmptyValidation(
            @ForAll @AlphaChars @StringLength(min = 1, max = 20) String text) {
        InputValidator.ValidationResult result = InputValidator.validateNotEmpty(text, "Field");
        assert result.isValid() : "Non-empty string should pass validation";
    }

    @Property(tries = 5)
    void validLicensePlatePassesValidation(
            @ForAll @AlphaChars @StringLength(min = 2, max = 10) String plate) {
        InputValidator.ValidationResult result = InputValidator.validateLicensePlate(plate);
        assert result.isValid() : "Valid license plate should pass: " + plate;
    }

    @Property(tries = 5)
    void tooShortLicensePlateFailsValidation() {
        InputValidator.ValidationResult result = InputValidator.validateLicensePlate("A");
        assert !result.isValid() : "Too short plate should fail";
    }

    @Property(tries = 5)
    void positiveNumberPassesValidation(
            @ForAll @IntRange(min = 1, max = 1000) int num) {
        InputValidator.ValidationResult result = 
            InputValidator.validatePositiveNumber(String.valueOf(num), "Amount");
        assert result.isValid() : "Positive number should pass";
    }

    @Property(tries = 5)
    void zeroFailsPositiveNumberValidation() {
        InputValidator.ValidationResult result = 
            InputValidator.validatePositiveNumber("0", "Amount");
        assert !result.isValid() : "Zero should fail positive validation";
    }

    @Property(tries = 5)
    void negativeFailsPositiveNumberValidation(
            @ForAll @IntRange(min = -1000, max = -1) int num) {
        InputValidator.ValidationResult result = 
            InputValidator.validatePositiveNumber(String.valueOf(num), "Amount");
        assert !result.isValid() : "Negative should fail positive validation";
    }

    @Property(tries = 5)
    void nonNegativePassesNonNegativeValidation(
            @ForAll @IntRange(min = 0, max = 1000) int num) {
        InputValidator.ValidationResult result = 
            InputValidator.validateNonNegativeNumber(String.valueOf(num), "Amount");
        assert result.isValid() : "Non-negative should pass";
    }

    @Property(tries = 5)
    void validSpotIdPassesValidation(
            @ForAll @IntRange(min = 1, max = 5) int floor,
            @ForAll @IntRange(min = 1, max = 5) int row,
            @ForAll @IntRange(min = 1, max = 10) int spot) {
        String spotId = "F" + floor + "-R" + row + "-S" + spot;
        InputValidator.ValidationResult result = InputValidator.validateSpotId(spotId);
        assert result.isValid() : "Valid spot ID should pass: " + spotId;
    }

    @Property(tries = 5)
    void invalidSpotIdFailsValidation(
            @ForAll @AlphaChars @StringLength(min = 1, max = 10) String invalid) {
        InputValidator.ValidationResult result = InputValidator.validateSpotId(invalid);
        assert !result.isValid() : "Invalid spot ID should fail: " + invalid;
    }

    @Property(tries = 5)
    void nullSelectionFailsValidation() {
        InputValidator.ValidationResult result = 
            InputValidator.validateSelection(null, "item");
        assert !result.isValid() : "Null selection should fail";
    }

    @Property(tries = 5)
    void validSelectionPassesValidation(
            @ForAll @AlphaChars @StringLength(min = 1, max = 10) String selection) {
        InputValidator.ValidationResult result = 
            InputValidator.validateSelection(selection, "item");
        assert result.isValid() : "Valid selection should pass";
    }

    @Property(tries = 5)
    void negativeRowFailsTableSelection() {
        InputValidator.ValidationResult result = 
            InputValidator.validateTableSelection(-1, "row");
        assert !result.isValid() : "Negative row should fail";
    }

    @Property(tries = 5)
    void validRowPassesTableSelection(
            @ForAll @IntRange(min = 0, max = 100) int row) {
        InputValidator.ValidationResult result = 
            InputValidator.validateTableSelection(row, "row");
        assert result.isValid() : "Valid row should pass";
    }
}
