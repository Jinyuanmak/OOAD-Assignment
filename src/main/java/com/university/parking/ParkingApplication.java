package com.university.parking;

import java.sql.SQLException;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.model.Floor;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.SpotType;
import com.university.parking.view.MainFrame;

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
        
        // Initialize parking lot with default configuration
        ParkingLot parkingLot = createDefaultParkingLot();

        // Make database manager and fineDAO final for lambda
        final DatabaseManager finalDbManager = dbManager;
        final FineDAO finalFineDAO = fineDAO;
        
        // Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame mainFrame = new MainFrame(parkingLot, finalDbManager, finalFineDAO);
                mainFrame.setVisible(true);
                
                // Add shutdown hook to close database connections
                if (finalDbManager != null) {
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.println("Shutting down database connections...");
                        finalDbManager.shutdown();
                    }));
                }
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
