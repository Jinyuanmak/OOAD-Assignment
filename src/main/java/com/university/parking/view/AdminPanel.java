package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.FineCalculationStrategy;
import com.university.parking.model.FixedFineStrategy;
import com.university.parking.model.Floor;
import com.university.parking.model.HourlyFineStrategy;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.ProgressiveFineStrategy;
import com.university.parking.model.Vehicle;

/**
 * Admin panel for system oversight and management.
 * Displays floor status, occupancy rates, revenue, and fine configuration.
 * 
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6
 */
public class AdminPanel extends BasePanel {
    private JTable floorTable;
    private DefaultTableModel floorTableModel;
    private JLabel occupancyLabel;
    private JLabel revenueLabel;
    private JComboBox<String> fineStrategyCombo;
    private JTable vehicleTable;
    private DefaultTableModel vehicleTableModel;
    private JTable fineTable;
    private DefaultTableModel fineTableModel;
    private final DatabaseManager dbManager;
    private final FineDAO fineDAO;

    public AdminPanel(ParkingLot parkingLot) {
        this(parkingLot, null, null);
    }
    
    public AdminPanel(ParkingLot parkingLot, DatabaseManager dbManager, FineDAO fineDAO) {
        super(parkingLot);
        this.dbManager = dbManager;
        this.fineDAO = fineDAO;
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));

        // Top panel - Statistics
        add(createStatisticsPanel(), BorderLayout.NORTH);

        // Center panel - Split between floors and vehicles/fines
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(createFloorPanel());
        splitPane.setBottomComponent(createVehiclesAndFinesPanel());
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);

        // Bottom panel - Fine configuration
        add(createFineConfigPanel(), BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = createTitledPanel("Statistics");
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));

        occupancyLabel = createLabel("Occupancy Rate: 0%");
        occupancyLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(occupancyLabel);

        revenueLabel = createLabel("Total Revenue: RM 0.00");
        revenueLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(revenueLabel);

        JButton refreshButton = createButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        panel.add(refreshButton);

        return panel;
    }

    private JPanel createFloorPanel() {
        JPanel panel = createTitledPanel("Floor Status");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Floor", "Total Spots", "Available", "Occupied", "Occupancy %"};
        floorTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        floorTable = new JTable(floorTableModel);
        floorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(floorTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createVehiclesAndFinesPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));

        // Current vehicles panel
        JPanel vehiclesPanel = createTitledPanel("Currently Parked Vehicles");
        vehiclesPanel.setLayout(new BorderLayout());
        String[] vehicleColumns = {"License Plate", "Type", "Spot", "Entry Time"};
        vehicleTableModel = new DefaultTableModel(vehicleColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        vehicleTable = new JTable(vehicleTableModel);
        vehiclesPanel.add(new JScrollPane(vehicleTable), BorderLayout.CENTER);
        panel.add(vehiclesPanel);

        // Unpaid fines panel
        JPanel finesPanel = createTitledPanel("Unpaid Fines");
        finesPanel.setLayout(new BorderLayout());
        String[] fineColumns = {"License Plate", "Type", "Amount (RM)", "Date"};
        fineTableModel = new DefaultTableModel(fineColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        fineTable = new JTable(fineTableModel);
        finesPanel.add(new JScrollPane(fineTable), BorderLayout.CENTER);
        panel.add(finesPanel);

        return panel;
    }

    private JPanel createFineConfigPanel() {
        JPanel panel = createTitledPanel("Fine Configuration");
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

        panel.add(createLabel("Fine Calculation Strategy:"));

        fineStrategyCombo = new JComboBox<>(new String[]{
            "Fixed (RM 50)",
            "Progressive (RM 50 + escalating)",
            "Hourly (RM 20/hour)"
        });
        panel.add(fineStrategyCombo);

        JButton applyButton = createButton("Apply Strategy");
        applyButton.addActionListener(e -> applyFineStrategy());
        panel.add(applyButton);

        return panel;
    }

    private void applyFineStrategy() {
        int selectedIndex = fineStrategyCombo.getSelectedIndex();
        FineCalculationStrategy strategy;
        
        switch (selectedIndex) {
            case 0:
                strategy = new FixedFineStrategy();
                break;
            case 1:
                strategy = new ProgressiveFineStrategy();
                break;
            case 2:
                strategy = new HourlyFineStrategy();
                break;
            default:
                strategy = new FixedFineStrategy();
        }
        
        parkingLot.changeFineStrategy(strategy);
        showSuccess("Fine strategy updated successfully. New strategy will apply to future entries only.");
    }

    @Override
    public void refreshData() {
        refreshFloorTable();
        refreshStatistics();
        refreshVehicleTable();
        refreshFineTable();
    }

    private void refreshFloorTable() {
        floorTableModel.setRowCount(0);
        
        for (Floor floor : parkingLot.getFloors()) {
            int total = floor.getAllSpots().size();
            int available = floor.getAvailableSpots().size();
            int occupied = total - available;
            double occupancy = total > 0 ? (occupied * 100.0 / total) : 0;
            
            floorTableModel.addRow(new Object[]{
                "Floor " + floor.getFloorNumber(),
                total,
                available,
                occupied,
                String.format("%.1f%%", occupancy)
            });
        }
    }

    private void refreshStatistics() {
        int totalSpots = 0;
        int occupiedSpots = 0;
        
        for (Floor floor : parkingLot.getFloors()) {
            totalSpots += floor.getAllSpots().size();
            occupiedSpots += floor.getAllSpots().size() - floor.getAvailableSpots().size();
        }
        
        double occupancyRate = totalSpots > 0 ? (occupiedSpots * 100.0 / totalSpots) : 0;
        occupancyLabel.setText(String.format("Occupancy Rate: %.1f%%", occupancyRate));
        revenueLabel.setText(String.format("Total Revenue: RM %.2f", parkingLot.getTotalRevenue()));
    }

    private void refreshVehicleTable() {
        vehicleTableModel.setRowCount(0);
        
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getAllSpots()) {
                if (!spot.isAvailable() && spot.getCurrentVehicle() != null) {
                    Vehicle vehicle = spot.getCurrentVehicle();
                    vehicleTableModel.addRow(new Object[]{
                        vehicle.getLicensePlate(),
                        vehicle.getType(),
                        spot.getSpotId(),
                        vehicle.getEntryTime()
                    });
                }
            }
        }
    }

    private void refreshFineTable() {
        fineTableModel.setRowCount(0);
        
        if (fineDAO == null) {
            return; // Database not available
        }
        
        try {
            List<Fine> unpaidFines = fineDAO.findAllUnpaid();
            for (Fine fine : unpaidFines) {
                fineTableModel.addRow(new Object[]{
                    fine.getLicensePlate(),
                    fine.getType(),
                    String.format("%.2f", fine.getAmount()),
                    fine.getIssuedDate()
                });
            }
        } catch (Exception e) {
            // Log error but don't crash the UI
            System.err.println("Error loading unpaid fines: " + e.getMessage());
        }
    }

    // Getters for testing
    public JTable getFloorTable() {
        return floorTable;
    }

    public JLabel getOccupancyLabel() {
        return occupancyLabel;
    }

    public JLabel getRevenueLabel() {
        return revenueLabel;
    }

    public JComboBox<String> getFineStrategyCombo() {
        return fineStrategyCombo;
    }

    public JTable getVehicleTable() {
        return vehicleTable;
    }

    public JTable getFineTable() {
        return fineTable;
    }
}
