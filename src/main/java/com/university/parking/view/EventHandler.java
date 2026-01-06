package com.university.parking.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

/**
 * Utility class for GUI event handling.
 * Provides common event handling patterns with error handling.
 * 
 * Requirements: 12.3
 */
public class EventHandler {

    // Flag to suppress dialogs during testing
    private static boolean suppressDialogs = false;

    /**
     * Sets whether dialogs should be suppressed (for testing).
     */
    public static void setSuppressDialogs(boolean suppress) {
        suppressDialogs = suppress;
    }

    /**
     * Returns whether dialogs are suppressed.
     */
    public static boolean isSuppressDialogs() {
        return suppressDialogs;
    }

    /**
     * Interface for actions that may throw exceptions.
     */
    @FunctionalInterface
    public interface ThrowingAction {
        void execute() throws Exception;
    }

    /**
     * Interface for actions that return a result and may throw exceptions.
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    /**
     * Creates an ActionListener that executes an action with error handling.
     * Displays error messages in a dialog if the action throws an exception.
     * 
     * @param parent the parent component for error dialogs
     * @param action the action to execute
     * @return an ActionListener that handles errors gracefully
     */
    public static ActionListener createSafeActionListener(JComponent parent, ThrowingAction action) {
        return e -> {
            try {
                action.execute();
            } catch (IllegalArgumentException ex) {
                showError(parent, ex.getMessage());
            } catch (Exception ex) {
                showError(parent, "An unexpected error occurred: " + ex.getMessage());
            }
        };
    }

    /**
     * Creates an ActionListener that executes an action and handles the result.
     * 
     * @param parent the parent component for error dialogs
     * @param action the action to execute
     * @param onSuccess callback for successful execution
     * @param <T> the type of result
     * @return an ActionListener that handles errors gracefully
     */
    public static <T> ActionListener createSafeActionListener(
            JComponent parent, 
            ThrowingSupplier<T> action, 
            Consumer<T> onSuccess) {
        return e -> {
            try {
                T result = action.get();
                onSuccess.accept(result);
            } catch (IllegalArgumentException ex) {
                showError(parent, ex.getMessage());
            } catch (Exception ex) {
                showError(parent, "An unexpected error occurred: " + ex.getMessage());
            }
        };
    }

    /**
     * Executes an action with error handling.
     * 
     * @param parent the parent component for error dialogs
     * @param action the action to execute
     * @return true if the action succeeded, false otherwise
     */
    public static boolean executeWithErrorHandling(JComponent parent, ThrowingAction action) {
        try {
            action.execute();
            return true;
        } catch (IllegalArgumentException ex) {
            showError(parent, ex.getMessage());
            return false;
        } catch (Exception ex) {
            showError(parent, "An unexpected error occurred: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Executes an action with error handling and returns the result.
     * 
     * @param parent the parent component for error dialogs
     * @param action the action to execute
     * @param defaultValue the default value to return on error
     * @param <T> the type of result
     * @return the result of the action, or defaultValue on error
     */
    public static <T> T executeWithErrorHandling(
            JComponent parent, 
            ThrowingSupplier<T> action, 
            T defaultValue) {
        try {
            return action.get();
        } catch (IllegalArgumentException ex) {
            showError(parent, ex.getMessage());
            return defaultValue;
        } catch (Exception ex) {
            showError(parent, "An unexpected error occurred: " + ex.getMessage());
            return defaultValue;
        }
    }

    /**
     * Shows an error message dialog.
     * 
     * @param parent the parent component
     * @param message the error message
     */
    public static void showError(JComponent parent, String message) {
        if (!suppressDialogs) {
            JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Shows an information message dialog.
     * 
     * @param parent the parent component
     * @param message the information message
     */
    public static void showInfo(JComponent parent, String message) {
        if (!suppressDialogs) {
            JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Shows a success message dialog.
     * 
     * @param parent the parent component
     * @param message the success message
     */
    public static void showSuccess(JComponent parent, String message) {
        if (!suppressDialogs) {
            JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Shows a confirmation dialog.
     * 
     * @param parent the parent component
     * @param message the confirmation message
     * @return true if the user confirmed, false otherwise (or true if dialogs suppressed)
     */
    public static boolean showConfirmation(JComponent parent, String message) {
        if (suppressDialogs) {
            return true;
        }
        int result = JOptionPane.showConfirmDialog(
            parent, 
            message, 
            "Confirm", 
            JOptionPane.YES_NO_OPTION
        );
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Validates input and shows error if invalid.
     * 
     * @param parent the parent component for error dialogs
     * @param result the validation result
     * @return true if valid, false otherwise
     */
    public static boolean validateAndShowError(JComponent parent, InputValidator.ValidationResult result) {
        if (!result.isValid()) {
            showError(parent, result.getErrorMessage());
            return false;
        }
        return true;
    }

    /**
     * Triggers an action on a button click.
     * This is a convenience method for testing button click events.
     * 
     * @param button the button to click
     */
    public static void triggerButtonClick(JButton button) {
        for (ActionListener listener : button.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(button, ActionEvent.ACTION_PERFORMED, button.getActionCommand()));
        }
    }

    /**
     * Triggers an action on a combo box selection change.
     * This is a convenience method for testing combo box events.
     * 
     * @param comboBox the combo box
     */
    public static void triggerComboBoxAction(JComboBox<?> comboBox) {
        for (ActionListener listener : comboBox.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(comboBox, ActionEvent.ACTION_PERFORMED, "comboBoxChanged"));
        }
    }
}
