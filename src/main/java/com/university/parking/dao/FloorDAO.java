package com.university.parking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.university.parking.model.Floor;

/**
 * Data Access Object for Floor entities.
 * Handles CRUD operations for floors in the database.
 */
public class FloorDAO {
    private final DatabaseManager dbManager;

    public FloorDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves a floor to the database.
     * @param parkingLotId the parking lot ID this floor belongs to
     * @param floor the floor to save
     * @return the generated ID
     */
    public Long save(Long parkingLotId, Floor floor) throws SQLException {
        String sql = "INSERT INTO floors (parking_lot_id, floor_number, total_spots) VALUES (?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, parkingLotId);
                stmt.setInt(2, floor.getFloorNumber());
                stmt.setInt(3, floor.getAllSpots().size());
                
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
     * Finds all floors for a specific parking lot.
     * @param parkingLotId the parking lot ID
     * @return list of floors
     */
    public List<Floor> findByParkingLotId(Long parkingLotId) throws SQLException {
        String sql = "SELECT * FROM floors WHERE parking_lot_id = ? ORDER BY floor_number";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, parkingLotId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Floor> floors = new ArrayList<>();
                    while (rs.next()) {
                        Floor floor = new Floor(rs.getInt("floor_number"));
                        floors.add(floor);
                    }
                    return floors;
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Gets the database ID for a floor by parking lot ID and floor number.
     * @param parkingLotId the parking lot ID
     * @param floorNumber the floor number
     * @return the floor database ID or null if not found
     */
    public Long getFloorId(Long parkingLotId, int floorNumber) throws SQLException {
        String sql = "SELECT id FROM floors WHERE parking_lot_id = ? AND floor_number = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, parkingLotId);
                stmt.setInt(2, floorNumber);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("id");
                    }
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Finds a floor by its database ID.
     * @param id the floor database ID
     * @return the floor or null if not found
     */
    public Floor findById(Long id) throws SQLException {
        String sql = "SELECT * FROM floors WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Floor(rs.getInt("floor_number"));
                    }
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Deletes a floor from the database.
     * @param id the floor database ID
     */
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM floors WHERE id = ?";
        
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
}
