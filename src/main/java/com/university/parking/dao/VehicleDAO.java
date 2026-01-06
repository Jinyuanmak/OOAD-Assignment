package com.university.parking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.university.parking.model.Vehicle;
import com.university.parking.model.VehicleType;

/**
 * Data Access Object for Vehicle entities.
 * Handles CRUD operations for vehicles in the database.
 */
public class VehicleDAO {
    private final DatabaseManager dbManager;

    public VehicleDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves a vehicle to the database.
     * @param vehicle the vehicle to save
     * @return the generated ID
     */
    public Long save(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, exit_time, assigned_spot_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, vehicle.getLicensePlate());
                stmt.setString(2, vehicle.getType().name());
                stmt.setBoolean(3, vehicle.isHandicapped());
                stmt.setTimestamp(4, vehicle.getEntryTime() != null ? Timestamp.valueOf(vehicle.getEntryTime()) : null);
                stmt.setTimestamp(5, vehicle.getExitTime() != null ? Timestamp.valueOf(vehicle.getExitTime()) : null);
                stmt.setString(6, null); // assigned_spot_id will be set separately
                
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
     * Finds a vehicle by its ID.
     * @param id the vehicle ID
     * @return the vehicle or null if not found
     */
    public Vehicle findById(Long id) throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToVehicle(rs);
                    }
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Finds a vehicle by its license plate.
     * @param licensePlate the license plate
     * @return the vehicle or null if not found
     */
    public Vehicle findByLicensePlate(String licensePlate) throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE license_plate = ? ORDER BY id DESC LIMIT 1";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, licensePlate);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToVehicle(rs);
                    }
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Finds all vehicles currently parked (no exit time).
     * @return list of currently parked vehicles
     */
    public List<Vehicle> findCurrentlyParked() throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE exit_time IS NULL";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                List<Vehicle> vehicles = new ArrayList<>();
                while (rs.next()) {
                    vehicles.add(mapResultSetToVehicle(rs));
                }
                return vehicles;
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Updates a vehicle in the database.
     * @param id the vehicle ID
     * @param vehicle the updated vehicle data
     */
    public void update(Long id, Vehicle vehicle) throws SQLException {
        String sql = "UPDATE vehicles SET license_plate = ?, vehicle_type = ?, is_handicapped = ?, " +
                     "entry_time = ?, exit_time = ?, assigned_spot_id = ? WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, vehicle.getLicensePlate());
                stmt.setString(2, vehicle.getType().name());
                stmt.setBoolean(3, vehicle.isHandicapped());
                stmt.setTimestamp(4, vehicle.getEntryTime() != null ? Timestamp.valueOf(vehicle.getEntryTime()) : null);
                stmt.setTimestamp(5, vehicle.getExitTime() != null ? Timestamp.valueOf(vehicle.getExitTime()) : null);
                stmt.setString(6, null);
                stmt.setLong(7, id);
                
                stmt.executeUpdate();
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Deletes a vehicle from the database.
     * @param id the vehicle ID
     */
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        
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
     * Finds all vehicles in the database.
     * @return list of all vehicles
     */
    public List<Vehicle> findAll() throws SQLException {
        String sql = "SELECT * FROM vehicles";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                List<Vehicle> vehicles = new ArrayList<>();
                while (rs.next()) {
                    vehicles.add(mapResultSetToVehicle(rs));
                }
                return vehicles;
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Maps a ResultSet row to a Vehicle object.
     */
    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(rs.getString("license_plate"));
        vehicle.setType(VehicleType.valueOf(rs.getString("vehicle_type")));
        vehicle.setHandicapped(rs.getBoolean("is_handicapped"));
        
        Timestamp entryTime = rs.getTimestamp("entry_time");
        if (entryTime != null) {
            vehicle.setEntryTime(entryTime.toLocalDateTime());
        }
        
        Timestamp exitTime = rs.getTimestamp("exit_time");
        if (exitTime != null) {
            vehicle.setExitTime(exitTime.toLocalDateTime());
        }
        
        return vehicle;
    }
}
