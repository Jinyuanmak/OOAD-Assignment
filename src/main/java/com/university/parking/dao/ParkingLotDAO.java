package com.university.parking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.university.parking.model.Floor;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.SpotType;
import com.university.parking.model.Vehicle;

/**
 * Data Access Object for ParkingLot entities.
 * Handles loading and saving complete parking lot structure including floors, spots, and active vehicles.
 */
public class ParkingLotDAO {
    private final DatabaseManager dbManager;
    private final FloorDAO floorDAO;
    private final ParkingSpotDAO spotDAO;
    private final VehicleDAO vehicleDAO;

    public ParkingLotDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.floorDAO = new FloorDAO(dbManager);
        this.spotDAO = new ParkingSpotDAO(dbManager);
        this.vehicleDAO = new VehicleDAO(dbManager);
    }

    /**
     * Checks if a parking lot exists in the database.
     * @return true if at least one parking lot record exists
     */
    public boolean parkingLotExists() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM parking_lots";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
        return false;
    }

    /**
     * Loads the complete parking lot structure from the database.
     * Includes all floors, spots, and active vehicles.
     * @return fully populated ParkingLot object or null if no parking lot exists
     */
    public ParkingLot loadParkingLot() throws SQLException {
        // Get the parking lot record
        String sql = "SELECT * FROM parking_lots LIMIT 1";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (!rs.next()) {
                    return null; // No parking lot found
                }
                
                Long parkingLotId = rs.getLong("id");
                String name = rs.getString("name");
                double totalRevenue = rs.getDouble("total_revenue");
                
                // Create parking lot object
                ParkingLot parkingLot = new ParkingLot(name);
                parkingLot.setTotalRevenue(totalRevenue);
                
                // Load all floors with their spots
                List<Floor> floors = loadFloors(parkingLotId);
                for (Floor floor : floors) {
                    parkingLot.addFloor(floor);
                }
                
                // Load active vehicles and assign them to their spots
                loadActiveVehicles(parkingLot);
                
                return parkingLot;
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Saves a parking lot structure to the database.
     * Saves the parking lot, all floors, and all spots.
     * @param parkingLot the parking lot to save
     * @return the generated parking lot ID
     */
    public Long saveParkingLot(ParkingLot parkingLot) throws SQLException {
        // Save parking lot record
        String sql = "INSERT INTO parking_lots (name, total_floors, total_revenue) VALUES (?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            Long parkingLotId;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, parkingLot.getName());
                stmt.setInt(2, parkingLot.getFloors().size());
                stmt.setDouble(3, parkingLot.getTotalRevenue());
                
                stmt.executeUpdate();
                
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        parkingLotId = rs.getLong(1);
                    } else {
                        throw new SQLException("Failed to get parking lot ID");
                    }
                }
            }
            
            // Save all floors and their spots
            for (Floor floor : parkingLot.getFloors()) {
                Long floorId = floorDAO.save(parkingLotId, floor);
                
                // Save all spots for this floor
                for (ParkingSpot spot : floor.getAllSpots()) {
                    spotDAO.save(floorId, spot);
                }
            }
            
            return parkingLotId;
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Loads all floors for a parking lot, including their spots.
     * @param parkingLotId the parking lot ID
     * @return list of floors with spots
     */
    private List<Floor> loadFloors(Long parkingLotId) throws SQLException {
        List<Floor> floors = floorDAO.findByParkingLotId(parkingLotId);
        
        // For each floor, load its spots
        for (Floor floor : floors) {
            Long floorId = floorDAO.getFloorId(parkingLotId, floor.getFloorNumber());
            if (floorId != null) {
                List<ParkingSpot> spots = spotDAO.findByFloorId(floorId);
                
                // Organize spots by row
                Map<Integer, List<ParkingSpot>> spotsByRow = new HashMap<>();
                for (ParkingSpot spot : spots) {
                    // Extract row number from spot ID (format: F1-R2-S3)
                    String spotId = spot.getSpotId();
                    String[] parts = spotId.split("-");
                    if (parts.length >= 2) {
                        int rowNumber = Integer.parseInt(parts[1].substring(1)); // Remove 'R' prefix
                        spotsByRow.computeIfAbsent(rowNumber, k -> new ArrayList<>()).add(spot);
                    }
                }
                
                // Recreate rows in the floor
                for (Map.Entry<Integer, List<ParkingSpot>> entry : spotsByRow.entrySet()) {
                    int rowNumber = entry.getKey();
                    List<ParkingSpot> rowSpots = entry.getValue();
                    
                    // Sort spots by spot number
                    rowSpots.sort((s1, s2) -> {
                        String[] parts1 = s1.getSpotId().split("-");
                        String[] parts2 = s2.getSpotId().split("-");
                        int spot1 = Integer.parseInt(parts1[2].substring(1));
                        int spot2 = Integer.parseInt(parts2[2].substring(1));
                        return Integer.compare(spot1, spot2);
                    });
                    
                    // Extract spot types
                    SpotType[] spotTypes = new SpotType[rowSpots.size()];
                    for (int i = 0; i < rowSpots.size(); i++) {
                        spotTypes[i] = rowSpots.get(i).getType();
                    }
                    
                    // Create the row in the floor
                    floor.createRow(rowNumber, rowSpots.size(), spotTypes);
                    
                    // Update the spots in the floor with the loaded data (status, etc.)
                    for (int i = 0; i < rowSpots.size(); i++) {
                        ParkingSpot loadedSpot = rowSpots.get(i);
                        ParkingSpot floorSpot = floor.findSpotById(loadedSpot.getSpotId());
                        if (floorSpot != null) {
                            floorSpot.setStatus(loadedSpot.getStatus());
                            floorSpot.setHourlyRate(loadedSpot.getHourlyRate());
                        }
                    }
                }
            }
        }
        
        return floors;
    }

    /**
     * Loads active vehicles (those without exit times) and assigns them to their spots.
     * @param parkingLot the parking lot to populate with vehicles
     */
    private void loadActiveVehicles(ParkingLot parkingLot) throws SQLException {
        List<Vehicle> activeVehicles = vehicleDAO.findActiveVehicles();
        
        for (Vehicle vehicle : activeVehicles) {
            String assignedSpotId = vehicle.getAssignedSpotId();
            if (assignedSpotId != null) {
                // Find the spot in the parking lot
                ParkingSpot spot = findSpotById(parkingLot, assignedSpotId);
                if (spot != null) {
                    // Assign vehicle to spot
                    spot.assignVehicle(vehicle);
                }
            }
        }
    }

    /**
     * Finds a parking spot by its ID within the parking lot.
     * @param parkingLot the parking lot to search
     * @param spotId the spot ID to find
     * @return the parking spot or null if not found
     */
    private ParkingSpot findSpotById(ParkingLot parkingLot, String spotId) {
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getAllSpots()) {
                if (spot.getSpotId().equals(spotId)) {
                    return spot;
                }
            }
        }
        return null;
    }
}
