package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Custom styled dialog for messages and confirmations.
 * Provides modern appearance with appropriate accent colors for each dialog type.
 * 
 * Requirements: 9.1, 9.2, 9.3
 */
public class StyledDialog extends JDialog {
    
    /**
     * Dialog types with associated accent colors.
     */
    public enum DialogType {
        SUCCESS(ThemeManager.SUCCESS),
        ERROR(ThemeManager.DANGER),
        WARNING(ThemeManager.WARNING),
        INFO(ThemeManager.INFO);
        
        private final Color accentColor;
        
        DialogType(Color accentColor) {
            this.accentColor = accentColor;
        }
        
        public Color getAccentColor() {
            return accentColor;
        }
    }
    
    private final DialogType dialogType;
    private final Color accentColor;
    private boolean confirmed = false;

    /**
     * Creates a styled dialog with the specified type and message.
     * 
     * @param parent the parent component
     * @param title the dialog title
     * @param message the message to display
     * @param type the dialog type (SUCCESS, ERROR, WARNING, INFO)
     */
    public StyledDialog(Component parent, String title, String message, DialogType type) {
        super(getParentFrame(parent), title, true);
        this.dialogType = type;
        this.accentColor = type.getAccentColor();
        
        initializeDialog(message, false);
    }
    
    /**
     * Creates a styled confirmation dialog.
     * 
     * @param parent the parent component
     * @param title the dialog title
     * @param message the message to display
     * @param type the dialog type
     * @param isConfirmation true if this is a confirmation dialog
     */
    private StyledDialog(Component parent, String title, String message, DialogType type, boolean isConfirmation) {
        super(getParentFrame(parent), title, true);
        this.dialogType = type;
        this.accentColor = type.getAccentColor();
        
        initializeDialog(message, isConfirmation);
    }
    
    private void initializeDialog(String message, boolean isConfirmation) {
        setUndecorated(false);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(ThemeManager.BG_WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        // Accent bar at top
        JPanel accentBar = createAccentBar();
        mainPanel.add(accentBar, BorderLayout.NORTH);
        
        // Content panel with message
        JPanel contentPanel = createContentPanel(message);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel(isConfirmation);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        pack();
        setMinimumSize(new Dimension(300, 150));
        setLocationRelativeTo(getParent());
    }
    
    private JPanel createAccentBar() {
        JPanel accentBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(accentColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        accentBar.setPreferredSize(new Dimension(0, 5));
        return accentBar;
    }

    private JPanel createContentPanel(String message) {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(ThemeManager.BG_WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));
        
        // Icon label based on type
        JLabel iconLabel = new JLabel(getIconText());
        iconLabel.setFont(ThemeManager.FONT_TITLE);
        iconLabel.setForeground(accentColor);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        contentPanel.add(iconLabel, BorderLayout.NORTH);
        
        // Message label
        JLabel messageLabel = new JLabel("<html><center>" + message + "</center></html>");
        messageLabel.setFont(ThemeManager.FONT_BODY);
        messageLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(messageLabel, BorderLayout.CENTER);
        
        return contentPanel;
    }
    
    private String getIconText() {
        // Return empty string - no icons needed
        return "";
    }
    
    private JPanel createButtonPanel(boolean isConfirmation) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(ThemeManager.BG_WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        if (isConfirmation) {
            StyledButton yesButton = new StyledButton("Yes", accentColor);
            yesButton.addActionListener(e -> {
                confirmed = true;
                dispose();
            });
            
            StyledButton noButton = new StyledButton("No", ThemeManager.TEXT_SECONDARY);
            noButton.addActionListener(e -> {
                confirmed = false;
                dispose();
            });
            
            buttonPanel.add(yesButton);
            buttonPanel.add(noButton);
        } else {
            StyledButton okButton = new StyledButton("OK", accentColor);
            okButton.addActionListener(e -> dispose());
            buttonPanel.add(okButton);
        }
        
        return buttonPanel;
    }
    
    /**
     * Gets the accent color for this dialog.
     * 
     * @return the accent color
     */
    public Color getAccentColor() {
        return accentColor;
    }
    
    /**
     * Gets the dialog type.
     * 
     * @return the dialog type
     */
    public DialogType getDialogType() {
        return dialogType;
    }
    
    /**
     * Returns whether the user confirmed the dialog (for confirmation dialogs).
     * 
     * @return true if confirmed, false otherwise
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    // ==================== Static Helper Methods ====================
    
    /**
     * Shows a success message dialog.
     * 
     * @param parent the parent component
     * @param message the message to display
     */
    public static void showSuccess(Component parent, String message) {
        StyledDialog dialog = new StyledDialog(parent, "Success", message, DialogType.SUCCESS);
        dialog.setVisible(true);
    }
    
    /**
     * Shows an error message dialog.
     * 
     * @param parent the parent component
     * @param message the message to display
     */
    public static void showError(Component parent, String message) {
        StyledDialog dialog = new StyledDialog(parent, "Error", message, DialogType.ERROR);
        dialog.setVisible(true);
    }
    
    /**
     * Shows a warning message dialog.
     * 
     * @param parent the parent component
     * @param message the message to display
     */
    public static void showWarning(Component parent, String message) {
        StyledDialog dialog = new StyledDialog(parent, "Warning", message, DialogType.WARNING);
        dialog.setVisible(true);
    }
    
    /**
     * Shows an information message dialog.
     * 
     * @param parent the parent component
     * @param message the message to display
     */
    public static void showInfo(Component parent, String message) {
        StyledDialog dialog = new StyledDialog(parent, "Information", message, DialogType.INFO);
        dialog.setVisible(true);
    }
    
    /**
     * Shows a confirmation dialog and returns the user's choice.
     * 
     * @param parent the parent component
     * @param message the message to display
     * @return true if the user confirmed, false otherwise
     */
    public static boolean showConfirm(Component parent, String message) {
        StyledDialog dialog = new StyledDialog(parent, "Confirm", message, DialogType.WARNING, true);
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }
    
    /**
     * Creates a StyledDialog instance for testing purposes without showing it.
     * 
     * @param type the dialog type
     * @return a new StyledDialog instance
     */
    public static StyledDialog createForType(DialogType type) {
        return new StyledDialog(null, "Test", "Test message", type);
    }
    
    // Helper method to get parent frame
    private static Frame getParentFrame(Component component) {
        if (component == null) {
            return null;
        }
        Window window = SwingUtilities.getWindowAncestor(component);
        if (window instanceof Frame) {
            return (Frame) window;
        }
        return null;
    }
}
