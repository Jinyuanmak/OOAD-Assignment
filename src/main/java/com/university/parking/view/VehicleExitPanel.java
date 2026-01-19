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
            
            // Pre-fill payment amount
            paymentAmountField.setText(String.format("%.2f", currentSummary.getTotalDue()));
            
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

        String amountText = paymentAmountField.getText().trim();
        if (!validateNotEmpty(paymentAmountField, "Payment amount")) {
            return;
        }
        if (!validatePositiveNumber(amountText, "Payment amount")) {
            return;
        }

        double amountPaid = Double.parseDouble(amountText);
        PaymentMethod paymentMethod = (PaymentMethod) paymentMethodCombo.getSelectedItem();
        String licensePlate = licensePlateField.getText().trim().toUpperCase();

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

            // Show success/partial payment message
            if (!result.isPaymentSufficient()) {
                showInfo("PARTIAL PAYMENT PROCESSED!\n\n" +
                    "Remaining balance: RM " + String.format("%.2f", result.getRemainingBalance()) + "\n\n" +
                    "This unpaid balance has been recorded as a fine.\n" +
                    "It will appear when this vehicle (License: " + licensePlate + ") enters again.\n" +
                    "Please pay the full amount on the next visit.");
            } else {
                showSuccess("Payment processed successfully!");
            }
            
            // Show receipt in dialog
            showReceiptDialog(receiptText);
            
            // Clear the receipt field after showing dialog
            receiptArea.setText("");
            
            // Ask if user wants to save PDF
            boolean wantsPDF = showConfirm("Would you like to save the receipt as PDF?");
            if (wantsPDF) {
                saveReceiptAsPDF(receiptText, licensePlate);
            }

            // Clear form for next transaction
            clearForm();

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Error processing payment: " + e.getMessage());
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
