package com.university.parking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.university.parking.model.ParkingSpot;
import com.university.parking.model.SpotStatus;
import com.university.parking.model.SpotType;

/**
 * Data Access Object for ParkingSpot entities.
 * Handles CRUD operations for parking spots in the database.
 */
public class ParkingSpotDAO {
    private final DatabaseManager dbManager;

    public ParkingSpotDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves a parking spot to the database.
     * @param floorId the floor ID this spot belongs to
     * @param spot the parking spot to save
     * @return the generated ID
     */
    public Long save(Long floorId, ParkingSpot spot) throws SQLException {
        String sql = "INSERT INTO parking_spots (floor_id, spot_id, spot_type, hourly_rate, status, current_vehicle_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, floorId);
                stmt.setString(2, spot.getSpotId());
                stmt.setString(3, spot.getType().name());
                stmt.setDouble(4, spot.getHourlyRate());
                stmt.setString(5, spot.getStatus().name());
                stmt.setObject(6, null);
                
                stmt.executeUpdate();
                
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Finds a parking spot by its ID.
     * @param id the spot database ID
     * @return the parking spot or null if not found
     */
    public ParkingSpot findById(Long id) throws SQLException {
        String sql = "SELECT * FROM parking_spots WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToSpot(rs);
                    }
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Finds a parking spot by its spot ID (e.g., "F1-R2-S3").
     * @param spotId the spot identifier
     * @return the parking spot or null if not found
     */
    public ParkingSpot findBySpotId(String spotId) throws SQLException {
        String sql = "SELECT * FROM parking_spots WHERE spot_id = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, spotId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToSpot(rs);
                    }
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Finds all available parking spots.
     * @return list of available spots
     */
    public List<ParkingSpot> findAvailable() throws SQLException {
        String sql = "SELECT * FROM parking_spots WHERE status = 'AVAILABLE'";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                List<ParkingSpot> spots = new ArrayList<>();
                while (rs.next()) {
                    spots.add(mapResultSetToSpot(rs));
                }
                return spots;
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Finds all parking spots for a specific floor.
     * @param floorId the floor ID
     * @return list of spots on the floor
     */
    public List<ParkingSpot> findByFloorId(Long floorId) throws SQLException {
        String sql = "SELECT * FROM parking_spots WHERE floor_id = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, floorId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    List<ParkingSpot> spots = new ArrayList<>();
                    while (rs.next()) {
                        spots.add(mapResultSetToSpot(rs));
                    }
                    return spots;
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Updates a parking spot in the database.
     * @param spotId the spot identifier
     * @param spot the updated spot data
     */
    public void update(String spotId, ParkingSpot spot) throws SQLException {
        String sql = "UPDATE parking_spots SET spot_type = ?, hourly_rate = ?, status = ?, current_vehicle_id = ? " +
                     "WHERE spot_id = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, spot.getType().name());
                stmt.setDouble(2, spot.getHourlyRate());
                stmt.setString(3, spot.getStatus().name());
                stmt.setObject(4, null);
                stmt.setString(5, spotId);
                
                stmt.executeUpdate();
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Updates the status of a parking spot.
     * @param spotId the spot identifier
     * @param status the new status
     */
    public void updateStatus(String spotId, SpotStatus status) throws SQLException {
        String sql = "UPDATE parking_spots SET status = ? WHERE spot_id = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status.name());
                stmt.setString(2, spotId);
                
                stmt.executeUpdate();
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Deletes a parking spot from the database.
     * @param id the spot database ID
     */
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM parking_spots WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Finds all parking spots in the database.
     * @return list of all spots
     */
    public List<ParkingSpot> findAll() throws SQLException {
        String sql = "SELECT * FROM parking_spots";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                List<ParkingSpot> spots = new ArrayList<>();
                while (rs.next()) {
                    spots.add(mapResultSetToSpot(rs));
                }
                return spots;
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Maps a ResultSet row to a ParkingSpot object.
     */
    private ParkingSpot mapResultSetToSpot(ResultSet rs) throws SQLException {
        ParkingSpot spot = new ParkingSpot();
        spot.setSpotId(rs.getString("spot_id"));
        spot.setType(SpotType.valueOf(rs.getString("spot_type")));
        spot.setHourlyRate(rs.getDouble("hourly_rate"));
        spot.setStatus(SpotStatus.valueOf(rs.getString("status")));
        return spot;
    }
}
