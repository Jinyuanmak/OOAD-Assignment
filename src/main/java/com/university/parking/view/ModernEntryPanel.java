package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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
 * Modern styled panel for processing vehicle entries.
 * Uses styled components for a professional appearance.
 * 
 * Requirements: 4.1, 4.2, 4.3, 10.1, 10.3
 */
public class ModernEntryPanel extends BasePanel {
    
    private StyledTextField licensePlateField;
    private StyledComboBox<VehicleType> vehicleTypeCombo;
    private JCheckBox handicappedCheckbox;
    private StyledTable spotTable;
    private DefaultTableModel spotTableModel;
    private JTextArea ticketArea;
    private JPanel ticketDisplayPanel;
    private StyledButton processEntryButton;
    private final VehicleEntryController entryController;
    
    @SuppressWarnings("unused")
    private final DatabaseManager dbManager;
    @SuppressWarnings("unused")
    private final FineDAO fineDAO;

    public ModernEntryPanel(ParkingLot parkingLot) {
        this(parkingLot, null, null);
    }
    
    public ModernEntryPanel(ParkingLot parkingLot, DatabaseManager dbManager, FineDAO fineDAO) {
        super(parkingLot);
        this.dbManager = dbManager;
        this.fineDAO = fineDAO;
        this.entryController = new VehicleEntryController(parkingLot, dbManager);
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ThemeManager.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Left panel - Input form
        add(createInputPanel(), BorderLayout.WEST);

        // Center panel - Available spots
        add(createSpotSelectionPanel(), BorderLayout.CENTER);

        // Right panel - Ticket display
        add(createTicketPanel(), BorderLayout.EAST);

        refreshData();
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_WHITE);
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_CARD, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Vehicle Information");
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

        // License plate
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 1;
        formPanel.add(createStyledLabel("License Plate:"), gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        licensePlateField = new StyledTextField(15);
        formPanel.add(licensePlateField, gbc);

        // Vehicle type
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 1;
        formPanel.add(createStyledLabel("Vehicle Type:"), gbc);
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        vehicleTypeCombo = new StyledComboBox<>(VehicleType.values());
        vehicleTypeCombo.addActionListener(e -> refreshAvailableSpots());
        formPanel.add(vehicleTypeCombo, gbc);

        // Handicapped checkbox
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        handicappedCheckbox = new JCheckBox("Handicapped Vehicle");
        handicappedCheckbox.setFont(ThemeManager.FONT_BODY);
        handicappedCheckbox.setForeground(ThemeManager.TEXT_PRIMARY);
        handicappedCheckbox.setBackground(ThemeManager.BG_WHITE);
        handicappedCheckbox.addActionListener(e -> refreshAvailableSpots());
        formPanel.add(handicappedCheckbox, gbc);

        // Search button
        gbc.gridy = 5;
        gbc.insets = new Insets(15, 5, 5, 5);
        StyledButton searchButton = new StyledButton("Find Available Spots", ThemeManager.INFO);
        searchButton.addActionListener(e -> refreshAvailableSpots());
        formPanel.add(searchButton, gbc);

        // Process entry button
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 5, 5, 5);
        processEntryButton = new StyledButton("Process Entry", ThemeManager.SUCCESS);
        processEntryButton.addActionListener(e -> processEntry());
        formPanel.add(processEntryButton, gbc);

        // Clear button
        gbc.gridy = 7;
        StyledButton clearButton = new StyledButton("Clear", ThemeManager.TEXT_SECONDARY);
        clearButton.addActionListener(e -> clearForm());
        formPanel.add(clearButton, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ThemeManager.FONT_BODY);
        label.setForeground(ThemeManager.TEXT_PRIMARY);
        return label;
    }

    private JPanel createSpotSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_CARD, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Available Spots");
        titleLabel.setFont(ThemeManager.FONT_SUBHEADER);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Create styled table
        String[] columns = {"Spot ID", "Floor", "Type", "Rate (RM/hr)"};
        spotTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        spotTable = new StyledTable(spotTableModel);
        spotTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(spotTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeManager.BG_WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Instruction label
        JLabel instructionLabel = new JLabel("Select a spot from the list above");
        instructionLabel.setFont(ThemeManager.FONT_SMALL);
        instructionLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(instructionLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTicketPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_WHITE);
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_CARD, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Parking Ticket");
        titleLabel.setFont(ThemeManager.FONT_SUBHEADER);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Ticket display panel with distinct background
        ticketDisplayPanel = new JPanel(new BorderLayout());
        ticketDisplayPanel.setBackground(ThemeManager.BG_CARD);
        ticketDisplayPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.PRIMARY_LIGHT, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        ticketArea = new JTextArea();
        ticketArea.setEditable(false);
        ticketArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ticketArea.setBackground(ThemeManager.BG_CARD);
        ticketArea.setForeground(ThemeManager.TEXT_PRIMARY);
        ticketArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(ticketArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeManager.BG_CARD);
        ticketDisplayPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(ticketDisplayPanel, BorderLayout.CENTER);

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
            StyledDialog.showError(this, "Please select a parking spot");
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

            // Refresh available spots
            refreshAvailableSpots();

            StyledDialog.showSuccess(this, "Vehicle entry processed successfully!");
        } catch (IllegalArgumentException e) {
            StyledDialog.showError(this, e.getMessage());
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

    @Override
    public void refreshData() {
        refreshAvailableSpots();
    }

    // Getters for testing
    public StyledTextField getLicensePlateField() {
        return licensePlateField;
    }

    public StyledComboBox<VehicleType> getVehicleTypeCombo() {
        return vehicleTypeCombo;
    }

    public JCheckBox getHandicappedCheckbox() {
        return handicappedCheckbox;
    }

    public StyledTable getSpotTable() {
        return spotTable;
    }

    public JTextArea getTicketArea() {
        return ticketArea;
    }
    
    /**
     * Gets the ticket display panel for testing background color distinction.
     * 
     * @return the ticket display panel
     */
    public JPanel getTicketDisplayPanel() {
        return ticketDisplayPanel;
    }
    
    /**
     * Gets the background color of the ticket display panel.
     * 
     * @return the ticket display background color
     */
    public Color getTicketDisplayBackground() {
        return ticketDisplayPanel.getBackground();
    }

    public StyledButton getProcessEntryButton() {
        return processEntryButton;
    }

    public VehicleEntryController getEntryController() {
        return entryController;
    }
}
