package com.university.parking;

import java.sql.SQLException;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.dao.ParkingLotDAO;
import com.university.parking.model.Floor;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.SpotType;
import com.university.parking.view.ModernMainFrame;

/**
 * Main application entry point for the University Parking Lot Management System.
 * Initializes the parking lot with default configuration and launches the GUI.
 */
public class ParkingApplication {

    public static void main(String[] args) {
        // Initialize database
        DatabaseManager dbManager = null;
        FineDAO fineDAO = null;
        
        try {
            dbManager = new DatabaseManager();
            dbManager.initializeDatabase();
            fineDAO = new FineDAO(dbManager);
            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Warning: Database initialization failed. Running without persistence.");
            System.err.println("Error: " + e.getMessage());
            // Continue without database - application will work in memory-only mode
        }
        
        // Load parking lot from database or create default
        ParkingLot parkingLot = null;
        try {
            parkingLot = loadOrCreateParkingLot(dbManager);
        } catch (SQLException e) {
            System.err.println("Error loading parking lot from database: " + e.getMessage());
            System.err.println("Creating default parking lot in memory...");
            parkingLot = createDefaultParkingLot();
        }

        // Make database manager and fineDAO final for lambda
        final DatabaseManager finalDbManager = dbManager;
        final FineDAO finalFineDAO = fineDAO;
        final ParkingLot finalParkingLot = parkingLot;
        
        // Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                ModernMainFrame mainFrame = new ModernMainFrame(finalParkingLot, finalDbManager, finalFineDAO);
                mainFrame.setVisible(true);
                
                // Add shutdown hook to close database connections and cleanup
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("Shutting down application...");
                    mainFrame.cleanup();
                    if (finalDbManager != null) {
                        System.out.println("Closing database connections...");
                        finalDbManager.shutdown();
                    }
                }));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, 
                    "Failed to start application: " + e.getMessage(),
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    /**
     * Loads parking lot from database or creates default if none exists.
     * @param dbManager the database manager
     * @return the parking lot (loaded or newly created)
     * @throws SQLException if database operation fails
     */
    private static ParkingLot loadOrCreateParkingLot(DatabaseManager dbManager) throws SQLException {
        if (dbManager == null) {
            System.out.println("No database connection. Creating default parking lot in memory...");
            return createDefaultParkingLot();
        }
        
        ParkingLotDAO parkingLotDAO = new ParkingLotDAO(dbManager);
        
        // Try to load existing parking lot
        if (parkingLotDAO.parkingLotExists()) {
            System.out.println("Loading existing parking lot from database...");
            ParkingLot parkingLot = parkingLotDAO.loadParkingLot();
            System.out.println("Loaded parking lot: " + parkingLot.getName() + 
                             " with " + parkingLot.getFloors().size() + " floors");
            
            // Count active vehicles
            int activeVehicles = 0;
            for (Floor floor : parkingLot.getFloors()) {
                for (ParkingSpot spot : floor.getAllSpots()) {
                    if (spot.getCurrentVehicle() != null) {
                        activeVehicles++;
                    }
                }
            }
            System.out.println("Restored " + activeVehicles + " active parking sessions");
            
            return parkingLot;
        }
        
        // First run - create default and save
        System.out.println("First run detected. Creating default parking lot...");
        ParkingLot parkingLot = createDefaultParkingLot();
        parkingLotDAO.saveParkingLot(parkingLot);
        System.out.println("Default parking lot saved to database");
        return parkingLot;
    }

    /**
     * Creates a default parking lot with 5 floors and various spot types.
     * Requirement 1.1: Support multiple floors (minimum 5 floors)
     */
    public static ParkingLot createDefaultParkingLot() {
        ParkingLot parkingLot = new ParkingLot("University Parking Lot");

        // Create 5 floors with different configurations
        for (int floorNum = 1; floorNum <= 5; floorNum++) {
            Floor floor = new Floor(floorNum);
            
            // Each floor has 3 rows with different spot types
            // Row 1: Compact spots
            floor.createRow(1, 5, createSpotTypes(5, SpotType.COMPACT));
            
            // Row 2: Regular spots
            floor.createRow(2, 5, createSpotTypes(5, SpotType.REGULAR));
            
            // Row 3: Mix of handicapped and reserved
            SpotType[] mixedTypes = new SpotType[]{
                SpotType.HANDICAPPED, SpotType.HANDICAPPED,
                SpotType.RESERVED, SpotType.RESERVED, SpotType.REGULAR
            };
            floor.createRow(3, 5, mixedTypes);
            
            parkingLot.addFloor(floor);
        }

        return parkingLot;
    }

    /**
     * Creates an array of spot types with the specified type.
     */
    private static SpotType[] createSpotTypes(int count, SpotType type) {
        SpotType[] types = new SpotType[count];
        Arrays.fill(types, type);
        return types;
    }
}
