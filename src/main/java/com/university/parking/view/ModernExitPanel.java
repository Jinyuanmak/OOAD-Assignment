package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.university.parking.controller.VehicleExitController;
import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.PaymentMethod;

/**
 * Modern styled panel for processing vehicle exits.
 * Uses styled components for a professional appearance.
 * 
 * Requirements: 4.1, 4.2, 10.2, 10.4
 */
public class ModernExitPanel extends BasePanel {
    
    private StyledTextField licensePlateField;
    private JTextArea summaryArea;
    private JPanel summaryDisplayPanel;
    private StyledTextField paymentAmountField;
    private StyledComboBox<PaymentMethod> paymentMethodCombo;
    private JTextArea receiptArea;
    private JPanel receiptDisplayPanel;
    private StyledButton lookupButton;
    private StyledButton processPaymentButton;
    private final VehicleExitController exitController;
    private VehicleExitController.PaymentSummary currentSummary;
    
    @SuppressWarnings("unused")
    private final DatabaseManager dbManager;
    @SuppressWarnings("unused")
    private final FineDAO fineDAO;

    public ModernExitPanel(ParkingLot parkingLot) {
        this(parkingLot, null, null);
    }
    
    public ModernExitPanel(ParkingLot parkingLot, DatabaseManager dbManager, FineDAO fineDAO) {
        super(parkingLot);
        this.dbManager = dbManager;
        this.fineDAO = fineDAO;
        this.exitController = new VehicleExitController(parkingLot, dbManager, fineDAO);
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ThemeManager.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Left panel - Vehicle lookup and payment
        add(createInputPanel(), BorderLayout.WEST);

        // Center panel - Payment summary
        add(createSummaryPanel(), BorderLayout.CENTER);

        // Right panel - Receipt display
        add(createReceiptPanel(), BorderLayout.EAST);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_WHITE);
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_CARD, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Vehicle Exit");
        titleLabel.setFont(ThemeManager.FONT_SUBHEADER);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ThemeManager.BG_WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Vehicle Lookup Section
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lookupSectionLabel = createSectionLabel("Vehicle Lookup");
        formPanel.add(lookupSectionLabel, gbc);
        
        // Separator
        gbc.gridy = 1;
        formPanel.add(createSeparator(), gbc);

        // License plate
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        formPanel.add(createStyledLabel("License Plate:"), gbc);
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        licensePlateField = new StyledTextField(15);
        formPanel.add(licensePlateField, gbc);

        // Lookup button
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 5, 5, 5);
        lookupButton = new StyledButton("Lookup Vehicle", ThemeManager.INFO);
        lookupButton.addActionListener(e -> lookupVehicle());
        formPanel.add(lookupButton, gbc);

        // Clear button
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 5, 15, 5);
        StyledButton clearButton = new StyledButton("Clear", ThemeManager.TEXT_SECONDARY);
        clearButton.addActionListener(e -> clearForm());
        formPanel.add(clearButton, gbc);

        // Payment Section
        gbc.gridy = 6;
        gbc.insets = new Insets(8, 5, 8, 5);
        JLabel paymentSectionLabel = createSectionLabel("Payment Processing");
        formPanel.add(paymentSectionLabel, gbc);
        
        // Separator
        gbc.gridy = 7;
        formPanel.add(createSeparator(), gbc);

        // Payment amount
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        formPanel.add(createStyledLabel("Amount (RM):"), gbc);
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        paymentAmountField = new StyledTextField(10);
        formPanel.add(paymentAmountField, gbc);

        // Payment method
        gbc.gridy = 10;
        gbc.gridwidth = 1;
        formPanel.add(createStyledLabel("Payment Method:"), gbc);
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        paymentMethodCombo = new StyledComboBox<>(PaymentMethod.values());
        formPanel.add(paymentMethodCombo, gbc);

        // Process payment button
        gbc.gridy = 12;
        gbc.insets = new Insets(15, 5, 5, 5);
        processPaymentButton = new StyledButton("Process Payment", ThemeManager.SUCCESS);
        processPaymentButton.addActionListener(e -> processPayment());
        processPaymentButton.setEnabled(false);
        formPanel.add(processPaymentButton, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ThemeManager.FONT_BODY);
        label.setForeground(ThemeManager.TEXT_PRIMARY);
        return label;
    }
    
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ThemeManager.FONT_SUBHEADER);
        label.setForeground(ThemeManager.PRIMARY);
        return label;
    }
    
    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(ThemeManager.BG_CARD);
        return separator;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_CARD, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Payment Summary");
        titleLabel.setFont(ThemeManager.FONT_SUBHEADER);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Summary display panel with distinct background
        summaryDisplayPanel = new JPanel(new BorderLayout());
        summaryDisplayPanel.setBackground(ThemeManager.BG_CARD);
        summaryDisplayPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.PRIMARY_LIGHT, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summaryArea.setBackground(ThemeManager.BG_CARD);
        summaryArea.setForeground(ThemeManager.TEXT_PRIMARY);
        summaryArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(summaryArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeManager.BG_CARD);
        summaryDisplayPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(summaryDisplayPanel, BorderLayout.CENTER);

        // Instruction label
        JLabel instructionLabel = new JLabel("Lookup a vehicle to see payment details");
        instructionLabel.setFont(ThemeManager.FONT_SMALL);
        instructionLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(instructionLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReceiptPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_WHITE);
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_CARD, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Receipt");
        titleLabel.setFont(ThemeManager.FONT_SUBHEADER);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Receipt display panel with distinct background
        receiptDisplayPanel = new JPanel(new BorderLayout());
        receiptDisplayPanel.setBackground(ThemeManager.BG_CARD);
        receiptDisplayPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.SUCCESS, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setBackground(ThemeManager.BG_CARD);
        receiptArea.setForeground(ThemeManager.TEXT_PRIMARY);
        receiptArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeManager.BG_CARD);
        receiptDisplayPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(receiptDisplayPanel, BorderLayout.CENTER);

        // Instruction label
        JLabel instructionLabel = new JLabel("Receipt will appear after payment");
        instructionLabel.setFont(ThemeManager.FONT_SMALL);
        instructionLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(instructionLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void lookupVehicle() {
        if (!validateLicensePlate(licensePlateField.getText())) {
            return;
        }

        String licensePlate = licensePlateField.getText().trim().toUpperCase();

        try {
            // Get unpaid fines for this license plate from database
            List<Fine> unpaidFines = exitController.getUnpaidFines(licensePlate);
            
            // Generate payment summary
            currentSummary = exitController.generatePaymentSummary(licensePlate, unpaidFines);
            
            // Display summary
            summaryArea.setText(currentSummary.getDisplayText());
            
            // Check if this is grace period or prepaid reservation
            boolean isZeroPaymentExit = currentSummary.isWithinGracePeriod() || currentSummary.hasPrepaidReservation();
            
            if (isZeroPaymentExit) {
                // Lock payment amount field for grace period or prepaid reservation
                paymentAmountField.setText("0.00");
                paymentAmountField.setEditable(false);
                paymentAmountField.setEnabled(false);
            } else {
                // Unlock payment amount field for normal exits
                paymentAmountField.setEditable(true);
                paymentAmountField.setEnabled(true);
                paymentAmountField.setText(String.format("%.2f", currentSummary.getTotalDue()));
            }
            
            // Enable payment button
            processPaymentButton.setEnabled(true);
            
        } catch (IllegalArgumentException e) {
            StyledDialog.showError(this, e.getMessage());
            currentSummary = null;
            processPaymentButton.setEnabled(false);
        } catch (Exception e) {
            StyledDialog.showError(this, "Error looking up vehicle: " + e.getMessage());
            currentSummary = null;
            processPaymentButton.setEnabled(false);
        }
    }

    private void processPayment() {
        if (currentSummary == null) {
            StyledDialog.showError(this, "Please lookup a vehicle first");
            return;
        }

        PaymentMethod paymentMethod = (PaymentMethod) paymentMethodCombo.getSelectedItem();
        String licensePlate = licensePlateField.getText().trim().toUpperCase();
        
        // Special handling for grace period exits
        if (currentSummary.isWithinGracePeriod()) {
            if (paymentMethod == PaymentMethod.CARD) {
                // CARD payment: No amount needed, can exit with RM 0.00
                processGracePeriodExit(licensePlate, 0.0, paymentMethod);
                return;
            } else {
                // CASH payment: Must insert cash, then refund
                processGracePeriodCashPayment(licensePlate);
                return;
            }
        }
        
        // Special handling for prepaid reservations with CASH payment
        if (currentSummary.hasPrepaidReservation() && paymentMethod == PaymentMethod.CASH) {
            // CASH payment for prepaid reservation: Must insert cash, then refund
            processPrepaidReservationCashPayment(licensePlate);
            return;
        }
        
        // Normal payment processing (non-grace period, non-prepaid-cash)
        String amountText = paymentAmountField.getText().trim();
        if (!validateNotEmpty(paymentAmountField, "Payment amount")) {
            return;
        }
        
        // Special case: Allow 0.00 for prepaid reservations with CARD
        boolean isZeroPaymentAllowed = currentSummary.hasPrepaidReservation();
        
        if (!isZeroPaymentAllowed && !validatePositiveNumber(amountText, "Payment amount")) {
            return;
        }
        
        // Validate it's a valid number (even if 0.00)
        double amountPaid;
        try {
            amountPaid = Double.parseDouble(amountText);
            if (amountPaid < 0) {
                StyledDialog.showError(this, "Payment amount cannot be negative");
                return;
            }
        } catch (NumberFormatException e) {
            StyledDialog.showError(this, "Payment amount must be a valid number");
            return;
        }

        try {
            List<Fine> unpaidFines = exitController.getUnpaidFines(licensePlate);
            VehicleExitController.ExitResult result = exitController.processExit(
                licensePlate, amountPaid, paymentMethod, unpaidFines
            );

            // Display receipt
            receiptArea.setText(result.getReceipt().generateReceiptText());

            if (!result.isPaymentSufficient()) {
                StyledDialog.showInfo(this, "PARTIAL PAYMENT PROCESSED!\n\n" +
                    "Remaining balance: RM " + String.format("%.2f", result.getRemainingBalance()) + "\n\n" +
                    "This unpaid balance has been recorded as a fine.\n" +
                    "It will appear when this vehicle (License: " + licensePlate + ") enters again.\n" +
                    "Please pay the full amount on the next visit.");
            } else {
                StyledDialog.showSuccess(this, "Payment processed successfully!");
            }

            // Clear form for next transaction
            clearForm();

        } catch (IllegalArgumentException e) {
            StyledDialog.showError(this, e.getMessage());
        } catch (Exception e) {
            StyledDialog.showError(this, "Error processing payment: " + e.getMessage());
        }
    }
    
    /**
     * Handles grace period exit with CARD payment (no cash needed).
     */
    private void processGracePeriodExit(String licensePlate, double amountPaid, PaymentMethod paymentMethod) {
        try {
            List<Fine> unpaidFines = exitController.getUnpaidFines(licensePlate);
            VehicleExitController.ExitResult result = exitController.processExit(
                licensePlate, amountPaid, paymentMethod, unpaidFines
            );

            // Display receipt
            receiptArea.setText(result.getReceipt().generateReceiptText());

            StyledDialog.showSuccess(this, "Grace Period Exit - No Charge!\n\nVehicle may now exit.");

            // Clear form for next transaction
            clearForm();

        } catch (Exception e) {
            StyledDialog.showError(this, "Error processing exit: " + e.getMessage());
        }
    }
    
    /**
     * Handles grace period exit with CASH payment (requires cash insertion, then refund).
     */
    private void processGracePeriodCashPayment(String licensePlate) {
        // Show cash denomination selection dialog
        String[] cashOptions = {"RM 1", "RM 5", "RM 10", "RM 50", "RM 100"};
        String selectedCash = (String) JOptionPane.showInputDialog(
            this,
            "Grace Period Exit - CASH Payment\n\n" +
            "Please insert cash (will be fully refunded):",
            "Insert Cash",
            JOptionPane.QUESTION_MESSAGE,
            null,
            cashOptions,
            cashOptions[2] // Default to RM 10
        );
        
        if (selectedCash == null) {
            return; // User cancelled
        }
        
        // Extract amount from selection
        double cashInserted = 0.0;
        switch (selectedCash) {
            case "RM 1": cashInserted = 1.0; break;
            case "RM 5": cashInserted = 5.0; break;
            case "RM 10": cashInserted = 10.0; break;
            case "RM 50": cashInserted = 50.0; break;
            case "RM 100": cashInserted = 100.0; break;
        }
        
        try {
            List<Fine> unpaidFines = exitController.getUnpaidFines(licensePlate);
            VehicleExitController.ExitResult result = exitController.processExit(
                licensePlate, 0.0, PaymentMethod.CASH, unpaidFines
            );

            // Get receipt text with refund notice
            String receiptText = result.getReceipt().generateReceiptText();
            receiptText += "\n========================================\n";
            receiptText += "       GRACE PERIOD - FULL REFUND\n";
            receiptText += "========================================\n";
            receiptText += String.format("Cash Inserted : RM %.2f\n", cashInserted);
            receiptText += String.format("Refund Amount : RM %.2f\n", cashInserted);
            receiptText += "========================================\n";
            
            // Display receipt
            receiptArea.setText(receiptText);

            StyledDialog.showSuccess(this, "Grace Period Exit - Full Refund!\n\n" +
                "Cash Inserted: RM " + String.format("%.2f", cashInserted) + "\n" +
                "Refund Amount: RM " + String.format("%.2f", cashInserted) + "\n\n" +
                "Vehicle may now exit.");

            // Clear form for next transaction
            clearForm();

        } catch (Exception e) {
            StyledDialog.showError(this, "Error processing exit: " + e.getMessage());
        }
    }
    
    /**
     * Handles prepaid reservation exit with CASH payment (requires cash insertion, then refund).
     */
    private void processPrepaidReservationCashPayment(String licensePlate) {
        // Show cash denomination selection dialog
        String[] cashOptions = {"RM 1", "RM 5", "RM 10", "RM 50", "RM 100"};
        String selectedCash = (String) JOptionPane.showInputDialog(
            this,
            "Prepaid Reservation Exit - CASH Payment\n\n" +
            "Please insert cash (will be fully refunded):",
            "Insert Cash",
            JOptionPane.QUESTION_MESSAGE,
            null,
            cashOptions,
            cashOptions[2] // Default to RM 10
        );
        
        if (selectedCash == null) {
            return; // User cancelled
        }
        
        // Extract amount from selection
        double cashInserted = 0.0;
        switch (selectedCash) {
            case "RM 1": cashInserted = 1.0; break;
            case "RM 5": cashInserted = 5.0; break;
            case "RM 10": cashInserted = 10.0; break;
            case "RM 50": cashInserted = 50.0; break;
            case "RM 100": cashInserted = 100.0; break;
        }
        
        try {
            List<Fine> unpaidFines = exitController.getUnpaidFines(licensePlate);
            VehicleExitController.ExitResult result = exitController.processExit(
                licensePlate, 0.0, PaymentMethod.CASH, unpaidFines
            );

            // Get receipt text with refund notice
            String receiptText = result.getReceipt().generateReceiptText();
            receiptText += "\n========================================\n";
            receiptText += "    PREPAID RESERVATION - FULL REFUND\n";
            receiptText += "========================================\n";
            receiptText += String.format("Cash Inserted : RM %.2f\n", cashInserted);
            receiptText += String.format("Refund Amount : RM %.2f\n", cashInserted);
            receiptText += "========================================\n";
            
            // Display receipt
            receiptArea.setText(receiptText);

            StyledDialog.showSuccess(this, "Prepaid Reservation Exit - Full Refund!\n\n" +
                "Cash Inserted: RM " + String.format("%.2f", cashInserted) + "\n" +
                "Refund Amount: RM " + String.format("%.2f", cashInserted) + "\n\n" +
                "Vehicle may now exit.");

            // Clear form for next transaction
            clearForm();

        } catch (Exception e) {
            StyledDialog.showError(this, "Error processing exit: " + e.getMessage());
        }
    }

    private void clearForm() {
        licensePlateField.setText("");
        summaryArea.setText("");
        paymentAmountField.setText("");
        paymentAmountField.setEditable(true);
        paymentAmountField.setEnabled(true);
        receiptArea.setText("");
        currentSummary = null;
        processPaymentButton.setEnabled(false);
    }
    
    /**
     * Override showError to use StyledDialog.
     */
    @Override
    protected void showError(String message) {
        StyledDialog.showError(this, message);
    }
    
    /**
     * Override showSuccess to use StyledDialog.
     */
    @Override
    protected void showSuccess(String message) {
        StyledDialog.showSuccess(this, message);
    }
    
    /**
     * Override showInfo to use StyledDialog.
     */
    @Override
    protected void showInfo(String message) {
        StyledDialog.showInfo(this, message);
    }

    @Override
    public void refreshData() {
        // Nothing to refresh on this panel
    }

    // Getters for testing
    public StyledTextField getLicensePlateField() {
        return licensePlateField;
    }

    public JTextArea getSummaryArea() {
        return summaryArea;
    }
    
    /**
     * Gets the summary display panel for testing background color distinction.
     * 
     * @return the summary display panel
     */
    public JPanel getSummaryDisplayPanel() {
        return summaryDisplayPanel;
    }
    
    /**
     * Gets the background color of the summary display panel.
     * 
     * @return the summary display background color
     */
    public Color getSummaryDisplayBackground() {
        return summaryDisplayPanel.getBackground();
    }

    public StyledTextField getPaymentAmountField() {
        return paymentAmountField;
    }

    public StyledComboBox<PaymentMethod> getPaymentMethodCombo() {
        return paymentMethodCombo;
    }

    public JTextArea getReceiptArea() {
        return receiptArea;
    }
    
    /**
     * Gets the receipt display panel for testing background color distinction.
     * 
     * @return the receipt display panel
     */
    public JPanel getReceiptDisplayPanel() {
        return receiptDisplayPanel;
    }
    
    /**
     * Gets the background color of the receipt display panel.
     * 
     * @return the receipt display background color
     */
    public Color getReceiptDisplayBackground() {
        return receiptDisplayPanel.getBackground();
    }

    public StyledButton getLookupButton() {
        return lookupButton;
    }

    public StyledButton getProcessPaymentButton() {
        return processPaymentButton;
    }

    public VehicleExitController getExitController() {
        return exitController;
    }
}
