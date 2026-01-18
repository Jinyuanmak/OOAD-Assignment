package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.university.parking.controller.VehicleEntryController;
import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.Vehicle;
import com.university.parking.model.VehicleType;

/**
 * Panel for processing vehicle entries.
 * Handles vehicle type selection, spot selection, and ticket generation.
 * 
 * Requirements: 9.1, 9.2
 */
public class VehicleEntryPanel extends BasePanel {
    private JTextField licensePlateField;
    private JComboBox<VehicleType> vehicleTypeCombo;
    private JCheckBox handicappedCheckbox;
    private JTable spotTable;
    private DefaultTableModel spotTableModel;
    private JTextArea ticketArea;
    private JButton processEntryButton;
    private final VehicleEntryController entryController;
    private final DatabaseManager dbManager;
    private final FineDAO fineDAO;

    public VehicleEntryPanel(ParkingLot parkingLot) {
        this(parkingLot, null, null);
    }
    
    public VehicleEntryPanel(ParkingLot parkingLot, DatabaseManager dbManager, FineDAO fineDAO) {
        super(parkingLot);
        this.dbManager = dbManager;
        this.fineDAO = fineDAO;
        this.entryController = new VehicleEntryController(parkingLot, dbManager);
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));

        // Left panel - Input form
        add(createInputPanel(), BorderLayout.WEST);

        // Center panel - Available spots
        add(createSpotSelectionPanel(), BorderLayout.CENTER);

        // Right panel - Ticket display
        add(createTicketPanel(), BorderLayout.EAST);

        refreshData();
    }

    private JPanel createInputPanel() {
        JPanel panel = createTitledPanel("Vehicle Information");
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(250, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // License plate
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createLabel("License Plate:"), gbc);
        gbc.gridx = 1;
        licensePlateField = createTextField(15);
        panel.add(licensePlateField, gbc);

        // Vehicle type
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(createLabel("Vehicle Type:"), gbc);
        gbc.gridx = 1;
        vehicleTypeCombo = new JComboBox<>(VehicleType.values());
        vehicleTypeCombo.addActionListener(e -> refreshAvailableSpots());
        panel.add(vehicleTypeCombo, gbc);

        // Handicapped checkbox
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        handicappedCheckbox = new JCheckBox("Handicapped Vehicle");
        handicappedCheckbox.addActionListener(e -> refreshAvailableSpots());
        panel.add(handicappedCheckbox, gbc);

        // Search button
        gbc.gridy = 3;
        JButton searchButton = createButton("Find Available Spots");
        searchButton.addActionListener(e -> refreshAvailableSpots());
        panel.add(searchButton, gbc);

        // Process entry button
        gbc.gridy = 4;
        processEntryButton = createButton("Process Entry");
        processEntryButton.addActionListener(e -> processEntry());
        panel.add(processEntryButton, gbc);

        // Clear button
        gbc.gridy = 5;
        JButton clearButton = createButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        panel.add(clearButton, gbc);

        return panel;
    }

    private JPanel createSpotSelectionPanel() {
        JPanel panel = createTitledPanel("Available Spots");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Spot ID", "Floor", "Type", "Rate (RM/hr)"};
        spotTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        spotTable = new JTable(spotTableModel);
        spotTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(spotTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel instructionLabel = createLabel("Select a spot from the list above");
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(instructionLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTicketPanel() {
        JPanel panel = createTitledPanel("Parking Ticket");
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 0));

        ticketArea = new JTextArea();
        ticketArea.setEditable(false);
        ticketArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ticketArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(ticketArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshAvailableSpots() {
        spotTableModel.setRowCount(0);
        
        VehicleType selectedType = (VehicleType) vehicleTypeCombo.getSelectedItem();
        boolean isHandicapped = handicappedCheckbox.isSelected();
        
        if (selectedType == null) return;

        // Create a temporary vehicle to check compatibility
        Vehicle tempVehicle = new Vehicle("TEMP", selectedType, isHandicapped);
        
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            if (spot.isAvailable() && tempVehicle.canParkInSpot(spot.getType())) {
                String floorInfo = spot.getSpotId().split("-")[0]; // Extract floor from spot ID
                spotTableModel.addRow(new Object[]{
                    spot.getSpotId(),
                    floorInfo,
                    spot.getType(),
                    String.format("%.2f", spot.getHourlyRate())
                });
            }
        }
    }

    private void processEntry() {
        // Validate inputs
        if (!validateLicensePlate(licensePlateField.getText())) {
            return;
        }
        if (!validateSelection(vehicleTypeCombo, "vehicle type")) {
            return;
        }

        int selectedRow = spotTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Please select a parking spot");
            return;
        }

        String spotId = (String) spotTableModel.getValueAt(selectedRow, 0);
        String licensePlate = licensePlateField.getText().trim().toUpperCase();
        VehicleType vehicleType = (VehicleType) vehicleTypeCombo.getSelectedItem();
        boolean isHandicapped = handicappedCheckbox.isSelected();

        try {
            VehicleEntryController.EntryResult result = entryController.processEntry(
                licensePlate, vehicleType, isHandicapped, spotId
            );

            // Display ticket
            ticketArea.setText(result.getTicketDisplay());

            showSuccess("Vehicle entry processed successfully!");
            
            // Clear input fields for next entry (keep ticket visible)
            licensePlateField.setText("");
            vehicleTypeCombo.setSelectedIndex(0); // Reset to MOTORCYCLE
            handicappedCheckbox.setSelected(false);
            spotTable.clearSelection();
            
            // Refresh available spots
            refreshAvailableSpots();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void clearForm() {
        licensePlateField.setText("");
        vehicleTypeCombo.setSelectedIndex(0);
        handicappedCheckbox.setSelected(false);
        ticketArea.setText("");
        spotTable.clearSelection();
        refreshAvailableSpots();
    }

    @Override
    public void refreshData() {
        refreshAvailableSpots();
    }

    // Getters for testing
    public JTextField getLicensePlateField() {
        return licensePlateField;
    }

    public JComboBox<VehicleType> getVehicleTypeCombo() {
        return vehicleTypeCombo;
    }

    public JCheckBox getHandicappedCheckbox() {
        return handicappedCheckbox;
    }

    public JTable getSpotTable() {
        return spotTable;
    }

    public JTextArea getTicketArea() {
        return ticketArea;
    }

    public JButton getProcessEntryButton() {
        return processEntryButton;
    }

    public VehicleEntryController getEntryController() {
        return entryController;
    }
}
