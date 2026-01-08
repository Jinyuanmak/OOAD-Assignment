package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
 * Modern admin panel with dashboard cards and styled components.
 * Displays floor status, occupancy rates, revenue, and fine configuration.
 * 
 * Requirements: 3.4, 8.1
 */
public class ModernAdminPanel extends BasePanel {
    
    // Dashboard cards
    private DashboardCard occupancyCard;
    private DashboardCard revenueCard;
    private DashboardCard availableSpotsCard;
    private DashboardCard parkedVehiclesCard;
    
    // Styled tables
    private StyledTable floorTable;
    private DefaultTableModel floorTableModel;
    private StyledTable vehicleTable;
    private DefaultTableModel vehicleTableModel;
    private StyledTable fineTable;
    private DefaultTableModel fineTableModel;
    
    // Fine configuration
    private StyledComboBox<String> fineStrategyCombo;
    
    // Database access
    @SuppressWarnings("unused")
    private final DatabaseManager dbManager;
    private final FineDAO fineDAO;

    public ModernAdminPanel(ParkingLot parkingLot) {
        this(parkingLot, null, null);
    }
    
    public ModernAdminPanel(ParkingLot parkingLot, DatabaseManager dbManager, FineDAO fineDAO) {
        super(parkingLot);
        this.dbManager = dbManager;
        this.fineDAO = fineDAO;
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(ThemeManager.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top panel - Dashboard cards
        add(createDashboardPanel(), BorderLayout.NORTH);

        // Center panel - Split between floors and vehicles/fines
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(createFloorPanel());
        splitPane.setBottomComponent(createVehiclesAndFinesPanel());
        splitPane.setDividerLocation(250);
        splitPane.setBorder(null);
        splitPane.setBackground(ThemeManager.BG_LIGHT);
        add(splitPane, BorderLayout.CENTER);

        // Bottom panel - Fine configuration
        add(createFineConfigPanel(), BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Create dashboard cards
        occupancyCard = new DashboardCard("Occupancy Rate", "0%", ThemeManager.PRIMARY);
        revenueCard = new DashboardCard("Total Revenue", "RM 0.00", ThemeManager.SUCCESS);
        availableSpotsCard = new DashboardCard("Available Spots", "0", ThemeManager.INFO);
        parkedVehiclesCard = new DashboardCard("Parked Vehicles", "0", ThemeManager.WARNING);
        
        // Add cards with spacing
        panel.add(occupancyCard);
        panel.add(Box.createRigidArea(new Dimension(15, 0)));
        panel.add(revenueCard);
        panel.add(Box.createRigidArea(new Dimension(15, 0)));
        panel.add(availableSpotsCard);
        panel.add(Box.createRigidArea(new Dimension(15, 0)));
        panel.add(parkedVehiclesCard);
        panel.add(Box.createHorizontalGlue());
        
        // Add refresh button
        StyledButton refreshButton = new StyledButton("Refresh", ThemeManager.PRIMARY);
        refreshButton.addActionListener(e -> refreshData());
        refreshButton.setPreferredSize(new Dimension(100, 40));
        refreshButton.setMaximumSize(new Dimension(100, 40));
        panel.add(refreshButton);
        
        return panel;
    }


    private JPanel createFloorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_CARD, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Floor Status");
        titleLabel.setFont(ThemeManager.FONT_SUBHEADER);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Create styled table
        String[] columns = {"Floor", "Total Spots", "Available", "Occupied", "Occupancy %"};
        floorTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        floorTable = new StyledTable(floorTableModel);
        
        JScrollPane scrollPane = new JScrollPane(floorTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeManager.BG_WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createVehiclesAndFinesPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setOpaque(false);

        // Current vehicles panel
        panel.add(createVehiclesPanel());

        // Unpaid fines panel
        panel.add(createFinesPanel());

        return panel;
    }

    private JPanel createVehiclesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_CARD, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Currently Parked Vehicles");
        titleLabel.setFont(ThemeManager.FONT_SUBHEADER);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Create styled table
        String[] vehicleColumns = {"License Plate", "Type", "Spot", "Entry Time"};
        vehicleTableModel = new DefaultTableModel(vehicleColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        vehicleTable = new StyledTable(vehicleTableModel);
        
        JScrollPane scrollPane = new JScrollPane(vehicleTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeManager.BG_WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }


    private JPanel createFinesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_CARD, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Unpaid Fines");
        titleLabel.setFont(ThemeManager.FONT_SUBHEADER);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Create styled table
        String[] fineColumns = {"License Plate", "Type", "Amount (RM)", "Date"};
        fineTableModel = new DefaultTableModel(fineColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        fineTable = new StyledTable(fineTableModel);
        
        JScrollPane scrollPane = new JScrollPane(fineTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeManager.BG_WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createFineConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_CARD, 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Fine Configuration");
        titleLabel.setFont(ThemeManager.FONT_SUBHEADER);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        
        // Controls panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        controlsPanel.setOpaque(false);
        
        JLabel strategyLabel = new JLabel("Fine Calculation Strategy:");
        strategyLabel.setFont(ThemeManager.FONT_BODY);
        strategyLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        controlsPanel.add(strategyLabel);

        fineStrategyCombo = new StyledComboBox<>(new String[]{
            "Fixed (RM 50)",
            "Progressive (RM 50 + escalating)",
            "Hourly (RM 20/hour)"
        });
        fineStrategyCombo.setPreferredSize(new Dimension(220, 36));
        controlsPanel.add(fineStrategyCombo);

        StyledButton applyButton = new StyledButton("Apply Strategy", ThemeManager.SUCCESS);
        applyButton.addActionListener(e -> applyFineStrategy());
        controlsPanel.add(applyButton);
        
        // Layout
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        contentPanel.setOpaque(false);
        contentPanel.add(titleLabel);
        
        panel.add(contentPanel, BorderLayout.NORTH);
        panel.add(controlsPanel, BorderLayout.CENTER);

        return panel;
    }


    /**
     * Refreshes all dashboard data from the parking lot.
     */
    public void refreshData() {
        if (parkingLot == null) {
            return;
        }
        
        // Update dashboard cards
        updateDashboardCards();
        
        // Update floor table
        updateFloorTable();
        
        // Update vehicles table
        updateVehicleTable();
        
        // Update fines table
        updateFineTable();
    }
    
    private void updateDashboardCards() {
        List<ParkingSpot> allSpots = parkingLot.getAllSpots();
        int totalSpots = allSpots.size();
        int availableSpots = 0;
        for (ParkingSpot spot : allSpots) {
            if (spot.isAvailable()) {
                availableSpots++;
            }
        }
        int parkedVehicles = totalSpots - availableSpots;
        
        double occupancyRate = totalSpots > 0 
            ? ((double) parkedVehicles / totalSpots) * 100 
            : 0;
        
        occupancyCard.setValue(String.format("%.1f%%", occupancyRate));
        availableSpotsCard.setValue(String.valueOf(availableSpots));
        parkedVehiclesCard.setValue(String.valueOf(parkedVehicles));
        
        // Get revenue from parking lot
        revenueCard.setValue(String.format("RM %.2f", parkingLot.getTotalRevenue()));
    }
    
    private void updateFloorTable() {
        floorTableModel.setRowCount(0);
        
        for (Floor floor : parkingLot.getFloors()) {
            int totalSpots = floor.getTotalSpots();
            int availableSpots = floor.getAvailableSpots().size();
            int occupiedSpots = totalSpots - availableSpots;
            double occupancyPercent = totalSpots > 0 
                ? ((double) occupiedSpots / totalSpots) * 100 
                : 0;
            
            floorTableModel.addRow(new Object[]{
                "Floor " + floor.getFloorNumber(),
                totalSpots,
                availableSpots,
                occupiedSpots,
                String.format("%.1f%%", occupancyPercent)
            });
        }
    }

    
    private void updateVehicleTable() {
        vehicleTableModel.setRowCount(0);
        
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getAllSpots()) {
                Vehicle vehicle = spot.getCurrentVehicle();
                if (vehicle != null) {
                    vehicleTableModel.addRow(new Object[]{
                        vehicle.getLicensePlate(),
                        vehicle.getType().toString(),
                        spot.getSpotId(),
                        vehicle.getEntryTime() != null 
                            ? vehicle.getEntryTime().toString() 
                            : "N/A"
                    });
                }
            }
        }
    }
    
    private void updateFineTable() {
        fineTableModel.setRowCount(0);
        
        if (fineDAO != null) {
            try {
                List<Fine> unpaidFines = fineDAO.findAllUnpaid();
                for (Fine fine : unpaidFines) {
                    fineTableModel.addRow(new Object[]{
                        fine.getLicensePlate(),
                        fine.getType().toString(),
                        String.format("%.2f", fine.getAmount()),
                        fine.getIssuedDate() != null 
                            ? fine.getIssuedDate().toString() 
                            : "N/A"
                    });
                }
            } catch (Exception e) {
                // Log error but don't crash
                System.err.println("Error loading fines: " + e.getMessage());
            }
        }
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
        
        // Apply strategy to parking lot's fine calculation context
        if (parkingLot != null) {
            parkingLot.changeFineStrategy(strategy);
            StyledDialog.showSuccess(this, "Fine strategy updated to: " + 
                fineStrategyCombo.getSelectedItem());
        }
    }

    
    // Getters for testing
    public DashboardCard getOccupancyCard() {
        return occupancyCard;
    }
    
    public DashboardCard getRevenueCard() {
        return revenueCard;
    }
    
    public DashboardCard getAvailableSpotsCard() {
        return availableSpotsCard;
    }
    
    public DashboardCard getParkedVehiclesCard() {
        return parkedVehiclesCard;
    }
    
    public StyledTable getFloorTable() {
        return floorTable;
    }
    
    public StyledTable getVehicleTable() {
        return vehicleTable;
    }
    
    public StyledTable getFineTable() {
        return fineTable;
    }
}
