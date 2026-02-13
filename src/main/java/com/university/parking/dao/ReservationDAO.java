package com.university.parking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.university.parking.model.Reservation;

/**
 * Data Access Object for Reservation entity.
 * Handles CRUD operations for parking spot reservations.
 */
public class ReservationDAO {
    private final DatabaseManager dbManager;

    public ReservationDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves a new reservation to the database.
     * @param reservation the reservation to save
     * @return true if successful
     * @throws SQLException if database operation fails
     */
    public boolean save(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservations (license_plate, spot_id, start_time, end_time, is_active, created_at, prepaid_amount) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = dbManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, reservation.getLicensePlate());
            stmt.setString(2, reservation.getSpotId());
            stmt.setTimestamp(3, Timestamp.valueOf(reservation.getStartTime()));
            stmt.setTimestamp(4, Timestamp.valueOf(reservation.getEndTime()));
            stmt.setBoolean(5, reservation.isActive());
            stmt.setTimestamp(6, Timestamp.valueOf(reservation.getCreatedAt()));
            stmt.setDouble(7, reservation.getPrepaidAmount());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reservation.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
            return false;
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Finds a valid reservation for a license plate and spot at the given time.
     * @param licensePlate the vehicle's license plate
     * @param spotId the spot ID
     * @param checkTime the time to check
     * @return the reservation if found and valid, null otherwise
     * @throws SQLException if database operation fails
     */
    public Reservation findValidReservation(String licensePlate, String spotId, LocalDateTime checkTime) throws SQLException {
        String sql = "SELECT * FROM reservations " +
                     "WHERE license_plate = ? AND spot_id = ? AND is_active = TRUE " +
                     "AND ? BETWEEN start_time AND end_time";
        
        Connection conn = dbManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, licensePlate);
            stmt.setString(2, spotId);
            stmt.setTimestamp(3, Timestamp.valueOf(checkTime));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                }
            }
            return null;
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Finds all active reservations for a license plate.
     * @param licensePlate the vehicle's license plate
     * @return list of active reservations
     * @throws SQLException if database operation fails
     */
    public List<Reservation> findByLicensePlate(String licensePlate) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE license_plate = ? AND is_active = TRUE ORDER BY start_time DESC";
        
        List<Reservation> reservations = new ArrayList<>();
        Connection conn = dbManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, licensePlate);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
            return reservations;
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Finds all active reservations for a spot.
     * @param spotId the spot ID
     * @return list of active reservations
     * @throws SQLException if database operation fails
     */
    public List<Reservation> findBySpotId(String spotId) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE spot_id = ? AND is_active = TRUE ORDER BY start_time";
        
        List<Reservation> reservations = new ArrayList<>();
        Connection conn = dbManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, spotId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
            return reservations;
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Checks if a spot has any active reservations during the given time period.
     * @param spotId the spot ID
     * @param startTime the start time
     * @param endTime the end time
     * @return true if spot is reserved during this period
     * @throws SQLException if database operation fails
     */
    public boolean isSpotReserved(String spotId, LocalDateTime startTime, LocalDateTime endTime) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations " +
                     "WHERE spot_id = ? AND is_active = TRUE " +
                     "AND ((start_time BETWEEN ? AND ?) OR (end_time BETWEEN ? AND ?) " +
                     "OR (start_time <= ? AND end_time >= ?))";
        
        Connection conn = dbManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, spotId);
            stmt.setTimestamp(2, Timestamp.valueOf(startTime));
            stmt.setTimestamp(3, Timestamp.valueOf(endTime));
            stmt.setTimestamp(4, Timestamp.valueOf(startTime));
            stmt.setTimestamp(5, Timestamp.valueOf(endTime));
            stmt.setTimestamp(6, Timestamp.valueOf(startTime));
            stmt.setTimestamp(7, Timestamp.valueOf(endTime));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Cancels a reservation (marks as inactive).
     * @param id the reservation ID
     * @return true if successful
     * @throws SQLException if database operation fails
     */
    public boolean cancel(Long id) throws SQLException {
        String sql = "UPDATE reservations SET is_active = FALSE WHERE id = ?";
        
        Connection conn = dbManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Finds all reservations.
     * @return list of all reservations
     * @throws SQLException if database operation fails
     */
    public List<Reservation> findAll() throws SQLException {
        String sql = "SELECT * FROM reservations ORDER BY created_at DESC";
        
        List<Reservation> reservations = new ArrayList<>();
        Connection conn = dbManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
            return reservations;
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Maps a ResultSet row to a Reservation object.
     */
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getLong("id"));
        reservation.setLicensePlate(rs.getString("license_plate"));
        reservation.setSpotId(rs.getString("spot_id"));
        reservation.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        reservation.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        reservation.setActive(rs.getBoolean("is_active"));
        reservation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        reservation.setPrepaidAmount(rs.getDouble("prepaid_amount"));
        return reservation;
    }
}
