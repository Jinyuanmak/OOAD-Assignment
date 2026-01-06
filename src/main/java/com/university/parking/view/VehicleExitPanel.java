package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
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

            // Display receipt
            receiptArea.setText(result.getReceipt().generateReceiptText());

            if (!result.isPaymentSufficient()) {
                showInfo("PARTIAL PAYMENT PROCESSED!\n\n" +
                    "Remaining balance: RM " + String.format("%.2f", result.getRemainingBalance()) + "\n\n" +
                    "This unpaid balance has been recorded as a fine.\n" +
                    "It will appear when this vehicle (License: " + licensePlate + ") enters again.\n" +
                    "Please pay the full amount on the next visit.");
            } else {
                showSuccess("Payment processed successfully!");
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

    private void clearForm() {
        licensePlateField.setText("");
        summaryArea.setText("");
        paymentAmountField.setText("");
        receiptArea.setText("");
        currentSummary = null;
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
