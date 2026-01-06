package com.university.parking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.university.parking.model.Fine;
import com.university.parking.model.FineType;

/**
 * Data Access Object for Fine entities.
 * Handles CRUD operations for fines in the database.
 */
public class FineDAO {
    private final DatabaseManager dbManager;

    public FineDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves a fine to the database.
     * @param fine the fine to save
     * @return the generated ID
     */
    public Long save(Fine fine) throws SQLException {
        String sql = "INSERT INTO fines (license_plate, fine_type, amount, issued_date, is_paid, parking_session_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, fine.getLicensePlate());
                stmt.setString(2, fine.getType().name());
                stmt.setDouble(3, fine.getAmount());
                stmt.setTimestamp(4, Timestamp.valueOf(fine.getIssuedDate()));
                stmt.setBoolean(5, fine.isPaid());
                stmt.setObject(6, null); // parking_session_id
                
                stmt.executeUpdate();
                
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long id = rs.getLong(1);
                        fine.setId(id);
                        return id;
                    }
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Finds a fine by its ID.
     * @param id the fine ID
     * @return the fine or null if not found
     */
    public Fine findById(Long id) throws SQLException {
        String sql = "SELECT * FROM fines WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToFine(rs);
                    }
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Finds all unpaid fines for a specific license plate.
     * @param licensePlate the license plate
     * @return list of unpaid fines
     */
    public List<Fine> findUnpaidByLicensePlate(String licensePlate) throws SQLException {
        String sql = "SELECT * FROM fines WHERE license_plate = ? AND is_paid = FALSE";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, licensePlate);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Fine> fines = new ArrayList<>();
                    while (rs.next()) {
                        fines.add(mapResultSetToFine(rs));
                    }
                    return fines;
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Finds all fines for a specific license plate.
     * @param licensePlate the license plate
     * @return list of all fines
     */
    public List<Fine> findByLicensePlate(String licensePlate) throws SQLException {
        String sql = "SELECT * FROM fines WHERE license_plate = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, licensePlate);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Fine> fines = new ArrayList<>();
                    while (rs.next()) {
                        fines.add(mapResultSetToFine(rs));
                    }
                    return fines;
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Finds all unpaid fines in the system.
     * @return list of all unpaid fines
     */
    public List<Fine> findAllUnpaid() throws SQLException {
        String sql = "SELECT * FROM fines WHERE is_paid = FALSE";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                List<Fine> fines = new ArrayList<>();
                while (rs.next()) {
                    fines.add(mapResultSetToFine(rs));
                }
                return fines;
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Updates a fine in the database.
     * @param id the fine ID
     * @param fine the updated fine data
     */
    public void update(Long id, Fine fine) throws SQLException {
        String sql = "UPDATE fines SET license_plate = ?, fine_type = ?, amount = ?, " +
                     "issued_date = ?, is_paid = ? WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, fine.getLicensePlate());
                stmt.setString(2, fine.getType().name());
                stmt.setDouble(3, fine.getAmount());
                stmt.setTimestamp(4, Timestamp.valueOf(fine.getIssuedDate()));
                stmt.setBoolean(5, fine.isPaid());
                stmt.setLong(6, id);
                
                stmt.executeUpdate();
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Marks a fine as paid.
     * @param id the fine ID
     */
    public void markAsPaid(Long id) throws SQLException {
        String sql = "UPDATE fines SET is_paid = TRUE WHERE id = ?";
        
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
     * Deletes a fine from the database.
     * @param id the fine ID
     */
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM fines WHERE id = ?";
        
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
     * Finds all fines in the database.
     * @return list of all fines
     */
    public List<Fine> findAll() throws SQLException {
        String sql = "SELECT * FROM fines";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                List<Fine> fines = new ArrayList<>();
                while (rs.next()) {
                    fines.add(mapResultSetToFine(rs));
                }
                return fines;
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Maps a ResultSet row to a Fine object.
     */
    private Fine mapResultSetToFine(ResultSet rs) throws SQLException {
        Fine fine = new Fine();
        fine.setId(rs.getLong("id"));
        fine.setLicensePlate(rs.getString("license_plate"));
        fine.setType(FineType.valueOf(rs.getString("fine_type")));
        fine.setAmount(rs.getDouble("amount"));
        fine.setIssuedDate(rs.getTimestamp("issued_date").toLocalDateTime());
        fine.setPaid(rs.getBoolean("is_paid"));
        return fine;
    }
}
