package com.university.parking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.university.parking.model.Payment;
import com.university.parking.model.PaymentMethod;

/**
 * Data Access Object for Payment entities.
 * Handles CRUD operations for payments in the database.
 */
public class PaymentDAO {
    private final DatabaseManager dbManager;

    public PaymentDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Saves a payment to the database.
     * @param payment the payment to save
     * @return the generated ID
     */
    public Long save(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (license_plate, parking_fee, fine_amount, total_amount, " +
                     "payment_method, payment_date) VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, payment.getLicensePlate());
                stmt.setDouble(2, payment.getParkingFee());
                stmt.setDouble(3, payment.getFineAmount());
                stmt.setDouble(4, payment.getTotalAmount());
                stmt.setString(5, payment.getPaymentMethod().name());
                stmt.setTimestamp(6, Timestamp.valueOf(payment.getPaymentDate()));
                
                stmt.executeUpdate();
                
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long id = rs.getLong(1);
                        payment.setId(id);
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
     * Finds a payment by its ID.
     * @param id the payment ID
     * @return the payment or null if not found
     */
    public Payment findById(Long id) throws SQLException {
        String sql = "SELECT * FROM payments WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToPayment(rs);
                    }
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
        return null;
    }

    /**
     * Finds all payments for a specific license plate.
     * @param licensePlate the license plate
     * @return list of payments
     */
    public List<Payment> findByLicensePlate(String licensePlate) throws SQLException {
        String sql = "SELECT * FROM payments WHERE license_plate = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, licensePlate);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Payment> payments = new ArrayList<>();
                    while (rs.next()) {
                        payments.add(mapResultSetToPayment(rs));
                    }
                    return payments;
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Finds all payments in the database.
     * @return list of all payments
     */
    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT * FROM payments";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                List<Payment> payments = new ArrayList<>();
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
                return payments;
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Calculates total revenue from all payments.
     * @return total revenue
     */
    public double calculateTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(total_amount) as total FROM payments";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
        return 0.0;
    }

    /**
     * Updates a payment in the database.
     * @param id the payment ID
     * @param payment the updated payment data
     */
    public void update(Long id, Payment payment) throws SQLException {
        String sql = "UPDATE payments SET license_plate = ?, parking_fee = ?, fine_amount = ?, " +
                     "total_amount = ?, payment_method = ?, payment_date = ? WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, payment.getLicensePlate());
                stmt.setDouble(2, payment.getParkingFee());
                stmt.setDouble(3, payment.getFineAmount());
                stmt.setDouble(4, payment.getTotalAmount());
                stmt.setString(5, payment.getPaymentMethod().name());
                stmt.setTimestamp(6, Timestamp.valueOf(payment.getPaymentDate()));
                stmt.setLong(7, id);
                
                stmt.executeUpdate();
            }
        } finally {
            dbManager.releaseConnection(conn);
        }
    }

    /**
     * Deletes a payment from the database.
     * @param id the payment ID
     */
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM payments WHERE id = ?";
        
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
     * Maps a ResultSet row to a Payment object.
     */
    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getLong("id"));
        payment.setLicensePlate(rs.getString("license_plate"));
        payment.setParkingFee(rs.getDouble("parking_fee"));
        payment.setFineAmount(rs.getDouble("fine_amount"));
        payment.setTotalAmount(rs.getDouble("total_amount"));
        payment.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
        payment.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
        
        return payment;
    }
}
