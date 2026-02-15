package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.university.parking.controller.VehicleExitController;
import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.PaymentMethod;
import com.university.parking.util.Receipt;

/**
 * Panel for processing vehicle exits.
 * Handles vehicle lookup, payment processing, and receipt generation.
 * 
 * Requirements: 9.1, 9.3
 */
public class VehicleExitPanel extends BasePanel {
    private JTextField licensePlateField;
    private JTextArea summaryArea;
    private JTextField paymentAmountField;
    private JComboBox<PaymentMethod> paymentMethodCombo;
    private JTextArea receiptArea;
    private JButton lookupButton;
    private JButton processPaymentButton;
    private final VehicleExitController exitController;
    private final DatabaseManager dbManager;
    private final FineDAO fineDAO;
    private VehicleExitController.PaymentSummary currentSummary;
    private Receipt lastReceipt; // Store last receipt for PDF generation

    public VehicleExitPanel(ParkingLot parkingLot) {
        this(parkingLot, null, null);
    }
    
    public VehicleExitPanel(ParkingLot parkingLot, DatabaseManager dbManager, FineDAO fineDAO) {
        super(parkingLot);
        this.dbManager = dbManager;
        this.fineDAO = fineDAO;
        this.exitController = new VehicleExitController(parkingLot, dbManager, fineDAO);
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));

        // Top panel - Vehicle lookup
        add(createLookupPanel(), BorderLayout.NORTH);

        // Center panel - Split between summary and receipt
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createSummaryPanel());
        splitPane.setRightComponent(createReceiptPanel());
        splitPane.setDividerLocation(400);
        add(splitPane, BorderLayout.CENTER);

        // Bottom panel - Payment processing
        add(createPaymentPanel(), BorderLayout.SOUTH);
    }

    private JPanel createLookupPanel() {
        JPanel panel = createTitledPanel("Vehicle Lookup");
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

        panel.add(createLabel("License Plate:"));
        licensePlateField = createTextField(15);
        panel.add(licensePlateField);

        lookupButton = createButton("Lookup Vehicle");
        lookupButton.addActionListener(e -> lookupVehicle());
        panel.add(lookupButton);

        JButton clearButton = createButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        panel.add(clearButton);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = createTitledPanel("Payment Summary");
        panel.setLayout(new BorderLayout());

        summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summaryArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(summaryArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createReceiptPanel() {
        JPanel panel = createTitledPanel("Receipt");
        panel.setLayout(new BorderLayout());

        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPaymentPanel() {
        JPanel panel = createTitledPanel("Payment Processing");
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

        panel.add(createLabel("Payment Amount (RM):"));
        paymentAmountField = createTextField(10);
        panel.add(paymentAmountField);

        panel.add(createLabel("Payment Method:"));
        paymentMethodCombo = new JComboBox<>(PaymentMethod.values());
        paymentMethodCombo.setPreferredSize(new java.awt.Dimension(120, 30)); // Ensure full text is visible
        panel.add(paymentMethodCombo);

        processPaymentButton = createButton("Process Payment");
        processPaymentButton.addActionListener(e -> processPayment());
        processPaymentButton.setEnabled(false);
        panel.add(processPaymentButton);

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
            
            // Only lock if zero payment AND no fines
            if (isZeroPaymentExit && currentSummary.getTotalDue() == 0.0) {
                // Lock payment amount field for grace period or prepaid reservation with no fines
                paymentAmountField.setText("0.00");
                paymentAmountField.setEditable(false);
                paymentAmountField.setEnabled(false);
            } else {
                // Unlock payment amount field for normal exits or exits with fines
                paymentAmountField.setEditable(true);
                paymentAmountField.setEnabled(true);
                paymentAmountField.setText(String.format("%.2f", currentSummary.getTotalDue()));
            }
            
            // Enable payment button
            processPaymentButton.setEnabled(true);
            
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            currentSummary = null;
            processPaymentButton.setEnabled(false);
        } catch (Exception e) {
            showError("Error looking up vehicle: " + e.getMessage());
            currentSummary = null;
            processPaymentButton.setEnabled(false);
        }
    }

    private void processPayment() {
        if (currentSummary == null) {
            showError("Please lookup a vehicle first");
            return;
        }

        PaymentMethod paymentMethod = (PaymentMethod) paymentMethodCombo.getSelectedItem();
        String licensePlate = licensePlateField.getText().trim().toUpperCase();
        
        // Special handling for grace period exits (only if no fines)
        if (currentSummary.isWithinGracePeriod() && currentSummary.getTotalDue() == 0.0) {
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
        
        // Special handling for prepaid reservations with CASH payment (only if no fines)
        if (currentSummary.hasPrepaidReservation() && currentSummary.getTotalDue() == 0.0 && paymentMethod == PaymentMethod.CASH) {
            // CASH payment for prepaid reservation: Must insert cash, then refund
            processPrepaidReservationCashPayment(licensePlate);
            return;
        }
        
        // Normal payment processing (non-grace period, non-prepaid-cash)
        String amountText = paymentAmountField.getText().trim();
        if (!validateNotEmpty(paymentAmountField, "Payment amount")) {
            return;
        }
        
        // Special case: Allow 0.00 for prepaid reservations or grace period with CARD (only if no fines)
        boolean isZeroPaymentAllowed = (currentSummary.hasPrepaidReservation() || currentSummary.isWithinGracePeriod()) 
                                        && currentSummary.getTotalDue() == 0.0;
        
        if (!isZeroPaymentAllowed && !validatePositiveNumber(amountText, "Payment amount")) {
            return;
        }
        
        // Validate it's a valid number (even if 0.00)
        double amountPaid;
        try {
            amountPaid = Double.parseDouble(amountText);
            if (amountPaid < 0) {
                showError("Payment amount cannot be negative");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Payment amount must be a valid number");
            return;
        }

        try {
            List<Fine> unpaidFines = exitController.getUnpaidFines(licensePlate);
            VehicleExitController.ExitResult result = exitController.processExit(
                licensePlate, amountPaid, paymentMethod, unpaidFines
            );

            // Store receipt for PDF generation
            lastReceipt = result.getReceipt();
            
            // Get receipt text
            String receiptText = result.getReceipt().generateReceiptText();
            
            // Display receipt in text area
            receiptArea.setText(receiptText);

            // Show receipt in dialog
            showReceiptDialog(receiptText);
            
            // Ask if user wants to save PDF
            boolean wantsPDF = showConfirm("Would you like to save the receipt as PDF?");
            if (wantsPDF) {
                saveReceiptAsPDF(receiptText, licensePlate);
            }

            // Show success/partial payment message
            if (!result.isPaymentSufficient()) {
                showInfo("PARTIAL PAYMENT PROCESSED!\n\n" +
                    "Remaining balance: RM " + String.format("%.2f", result.getRemainingBalance()) + "\n\n" +
                    "This unpaid balance has been recorded as a fine.\n" +
                    "It will appear when this vehicle (License: " + licensePlate + ") enters again.\n" +
                    "Please pay the full amount on the next visit.");
            } else {
                showSuccess("Payment processed successfully!\n\nVehicle may now exit.");
            }

            // Clear form for next transaction
            clearForm();

        } catch (Exception e) {
            showError("Error processing payment: " + e.getMessage());
            e.printStackTrace();
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

            // Store receipt for PDF generation
            lastReceipt = result.getReceipt();
            
            // Get receipt text
            String receiptText = result.getReceipt().generateReceiptText();
            
            // Display receipt in text area
            receiptArea.setText(receiptText);

            // Show receipt in dialog
            showReceiptDialog(receiptText);
            
            // Ask if user wants to save PDF
            boolean wantsPDF = showConfirm("Would you like to save the receipt as PDF?");
            if (wantsPDF) {
                saveReceiptAsPDF(receiptText, licensePlate);
            }

            showSuccess("Grace Period Exit - No Charge!\n\nVehicle may now exit.");

            // Clear form for next transaction
            clearForm();

        } catch (Exception e) {
            showError("Error processing exit: " + e.getMessage());
            e.printStackTrace();
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

            // Store receipt for PDF generation
            lastReceipt = result.getReceipt();
            
            // Get receipt text with refund notice
            String receiptText = result.getReceipt().generateReceiptText();
            receiptText += "\n========================================\n";
            receiptText += "       GRACE PERIOD - FULL REFUND\n";
            receiptText += "========================================\n";
            receiptText += String.format("Cash Inserted : RM %.2f\n", cashInserted);
            receiptText += String.format("Refund Amount : RM %.2f\n", cashInserted);
            receiptText += "========================================\n";
            
            // Display receipt in text area
            receiptArea.setText(receiptText);

            // Show receipt in dialog
            showReceiptDialog(receiptText);
            
            // Ask if user wants to save PDF
            boolean wantsPDF = showConfirm("Would you like to save the receipt as PDF?");
            if (wantsPDF) {
                saveReceiptAsPDF(receiptText, licensePlate);
            }

            showSuccess("Grace Period Exit - Full Refund!\n\n" +
                "Cash Inserted: RM " + String.format("%.2f", cashInserted) + "\n" +
                "Refund Amount: RM " + String.format("%.2f", cashInserted) + "\n\n" +
                "Vehicle may now exit.");

            // Clear form for next transaction
            clearForm();

        } catch (Exception e) {
            showError("Error processing exit: " + e.getMessage());
            e.printStackTrace();
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

            // Store receipt for PDF generation
            lastReceipt = result.getReceipt();
            
            // Get receipt text with refund notice
            String receiptText = result.getReceipt().generateReceiptText();
            receiptText += "\n========================================\n";
            receiptText += "    PREPAID RESERVATION - FULL REFUND\n";
            receiptText += "========================================\n";
            receiptText += String.format("Cash Inserted : RM %.2f\n", cashInserted);
            receiptText += String.format("Refund Amount : RM %.2f\n", cashInserted);
            receiptText += "========================================\n";
            
            // Display receipt in text area
            receiptArea.setText(receiptText);

            // Show receipt in dialog
            showReceiptDialog(receiptText);
            
            // Ask if user wants to save PDF
            boolean wantsPDF = showConfirm("Would you like to save the receipt as PDF?");
            if (wantsPDF) {
                saveReceiptAsPDF(receiptText, licensePlate);
            }

            showSuccess("Prepaid Reservation Exit - Full Refund!\n\n" +
                "Cash Inserted: RM " + String.format("%.2f", cashInserted) + "\n" +
                "Refund Amount: RM " + String.format("%.2f", cashInserted) + "\n\n" +
                "Vehicle may now exit.");

            // Clear form for next transaction
            clearForm();

        } catch (Exception e) {
            showError("Error processing exit: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shows the receipt in a dialog window.
     */
    private void showReceiptDialog(String receiptText) {
        JTextArea receiptDisplay = new JTextArea(receiptText);
        receiptDisplay.setEditable(false);
        receiptDisplay.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptDisplay.setMargin(new Insets(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(receiptDisplay);
        scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));
        
        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Payment Receipt",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Saves the receipt as a PDF file.
     */
    private void saveReceiptAsPDF(String receiptText, String licensePlate) {
        try {
            // Create receipts directory if it doesn't exist
            java.io.File receiptsDir = new java.io.File("receipts");
            if (!receiptsDir.exists()) {
                receiptsDir.mkdirs();
            }
            
            // Generate filename with timestamp
            String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "receipts/receipt_" + licensePlate + "_" + timestamp + ".pdf";
            
            if (lastReceipt != null) {
                // Generate PDF using PDFBox
                com.university.parking.util.ReceiptPDFGenerator.generatePDF(
                    lastReceipt, 
                    filename
                );
                
                showSuccess("Receipt saved successfully as PDF!\n\nFile: " + filename);
            } else {
                // Fallback to text file if receipt object not available
                String textFilename = "receipts/receipt_" + licensePlate + "_" + timestamp + ".txt";
                try (java.io.FileWriter writer = new java.io.FileWriter(textFilename)) {
                    writer.write(receiptText);
                }
                showSuccess("Receipt saved successfully as text file!\n\nFile: " + textFilename);
            }
            
        } catch (Exception e) {
            showError("Failed to save receipt: " + e.getMessage());
            e.printStackTrace();
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
        lastReceipt = null;
        processPaymentButton.setEnabled(false);
    }

    @Override
    public void refreshData() {
        // Nothing to refresh on this panel
    }

    // Getters for testing
    public JTextField getLicensePlateField() {
        return licensePlateField;
    }

    public JTextArea getSummaryArea() {
        return summaryArea;
    }

    public JTextField getPaymentAmountField() {
        return paymentAmountField;
    }

    public JComboBox<PaymentMethod> getPaymentMethodCombo() {
        return paymentMethodCombo;
    }

    public JTextArea getReceiptArea() {
        return receiptArea;
    }

    public JButton getLookupButton() {
        return lookupButton;
    }

    public JButton getProcessPaymentButton() {
        return processPaymentButton;
    }

    public VehicleExitController getExitController() {
        return exitController;
    }
}
