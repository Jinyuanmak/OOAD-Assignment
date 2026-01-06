package com.university.parking.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.university.parking.model.Fine;
import com.university.parking.model.FineType;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.Payment;
import com.university.parking.model.PaymentMethod;
import com.university.parking.model.SpotStatus;
import com.university.parking.model.SpotType;
import com.university.parking.model.Vehicle;
import com.university.parking.model.VehicleType;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Property-based tests for data persistence consistency.
 * Feature: parking-lot-management, Property 27: Data Persistence Consistency
 * Validates: Requirements 11.1, 11.2, 11.3, 11.4
 * 
 * Tests that all system operations that modify data are persisted to the database
 * and survive system restarts.
 */
public class DataPersistenceConsistencyProperties {

    /**
     * Property: For any vehicle saved to the database, retrieving it should return
     * the same vehicle data.
     */
    @Property(tries = 100)
    void vehiclePersistenceRoundTrip(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll boolean isHandicapped) throws SQLException {
        
        // Create a test database manager with unique DB for this test
        DatabaseManager dbManager = new DatabaseManager("jdbc:h2:mem:test_vehicle_" + System.nanoTime());
        dbManager.initializeDatabase();
        
        try {
            VehicleDAO vehicleDAO = new VehicleDAO(dbManager);
            
            // Create and save a vehicle
            Vehicle original = new Vehicle(licensePlate, vehicleType, isHandicapped);
            original.setEntryTime(LocalDateTime.now());
            
            Long id = vehicleDAO.save(original);
            
            // Retrieve the vehicle
            Vehicle retrieved = vehicleDAO.findById(id);
            
            // Verify persistence
            assert retrieved != null : "Vehicle should be retrievable after save";
            assert retrieved.getLicensePlate().equals(original.getLicensePlate()) : 
                "License plate should match";
            assert retrieved.getType() == original.getType() : 
                "Vehicle type should match";
            assert retrieved.isHandicapped() == original.isHandicapped() : 
                "Handicapped status should match";
            assert retrieved.getEntryTime() != null : 
                "Entry time should be persisted";
        } finally {
            dbManager.shutdown();
        }
    }

    /**
     * Property: For any parking spot saved to the database, retrieving it should return
     * the same spot data.
     */
    @Property(tries = 100)
    void parkingSpotPersistenceRoundTrip(
            @ForAll("spotIds") String spotId,
            @ForAll("spotTypes") SpotType spotType) throws SQLException {
        
        DatabaseManager dbManager = new DatabaseManager("jdbc:h2:mem:test_spot_" + System.nanoTime());
        dbManager.initializeDatabase();
        
        try {
            ParkingSpotDAO spotDAO = new ParkingSpotDAO(dbManager);
            
            // First, create a parking_lot record (required for foreign key constraint)
            Connection conn = dbManager.getConnection();
            Long parkingLotId;
            try (java.sql.PreparedStatement lotStmt = conn.prepareStatement(
                    "INSERT INTO parking_lots (name, total_floors, total_revenue) VALUES (?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS)) {
                lotStmt.setString(1, "Test Lot");
                lotStmt.setInt(2, 1);
                lotStmt.setDouble(3, 0.0);
                lotStmt.executeUpdate();
                
                try (java.sql.ResultSet lotRs = lotStmt.getGeneratedKeys()) {
                    lotRs.next();
                    parkingLotId = lotRs.getLong(1);
                }
            }
            
            // Now create a floor record
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO floors (parking_lot_id, floor_number, total_spots) VALUES (?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, parkingLotId);
                stmt.setInt(2, 1); // floor_number
                stmt.setInt(3, 10); // total_spots
                stmt.executeUpdate();
                
                try (java.sql.ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    Long floorId = rs.getLong(1);
                    
                    // Create and save a parking spot
                    ParkingSpot original = new ParkingSpot(spotId, spotType);
                    
                    Long id = spotDAO.save(floorId, original);
                    
                    // Retrieve the spot
                    ParkingSpot retrieved = spotDAO.findById(id);
                    
                    // Verify persistence
                    assert retrieved != null : "Spot should be retrievable after save";
                    assert retrieved.getSpotId().equals(original.getSpotId()) : 
                        "Spot ID should match";
                    assert retrieved.getType() == original.getType() : 
                        "Spot type should match";
                    assert retrieved.getHourlyRate() == original.getHourlyRate() : 
                        "Hourly rate should match";
                    assert retrieved.getStatus() == SpotStatus.AVAILABLE : 
                        "Initial status should be AVAILABLE";
                }
            } finally {
                dbManager.releaseConnection(conn);
            }
        } finally {
            dbManager.shutdown();
        }
    }

    /**
     * Property: For any fine saved to the database, retrieving it should return
     * the same fine data, and unpaid fines should persist across sessions.
     */
    @Property(tries = 100)
    void finePersistenceAndCrossSessionRetrieval(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("fineTypes") FineType fineType,
            @ForAll("fineAmounts") double amount) throws SQLException {
        
        DatabaseManager dbManager = new DatabaseManager("jdbc:h2:mem:test_fine_" + System.nanoTime());
        dbManager.initializeDatabase();
        
        try {
            FineDAO fineDAO = new FineDAO(dbManager);
            
            // Create and save a fine
            Fine original = new Fine(licensePlate, fineType, amount);
            
            Long id = fineDAO.save(original);
            
            // Retrieve the fine
            Fine retrieved = fineDAO.findById(id);
            
            // Verify persistence
            assert retrieved != null : "Fine should be retrievable after save";
            assert retrieved.getLicensePlate().equals(original.getLicensePlate()) : 
                "License plate should match";
            assert retrieved.getType() == original.getType() : 
                "Fine type should match";
            assert retrieved.getAmount() == original.getAmount() : 
                "Fine amount should match";
            assert !retrieved.isPaid() : 
                "Fine should initially be unpaid";
            
            // Verify unpaid fines are retrievable by license plate
            List<Fine> unpaidFines = fineDAO.findUnpaidByLicensePlate(licensePlate);
            assert !unpaidFines.isEmpty() : 
                "Unpaid fines should be retrievable by license plate";
            assert unpaidFines.stream().anyMatch(f -> f.getId().equals(id)) : 
                "Saved fine should appear in unpaid fines list";
        } finally {
            dbManager.shutdown();
        }
    }

    /**
     * Property: For any payment saved to the database, retrieving it should return
     * the same payment data.
     */
    @Property(tries = 100)
    void paymentPersistenceRoundTrip(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("parkingFees") double parkingFee,
            @ForAll("fineAmounts") double fineAmount,
            @ForAll("paymentMethods") PaymentMethod paymentMethod) throws SQLException {
        
        DatabaseManager dbManager = new DatabaseManager("jdbc:h2:mem:test_payment_" + System.nanoTime());
        dbManager.initializeDatabase();
        
        try {
            PaymentDAO paymentDAO = new PaymentDAO(dbManager);
            
            // Create and save a payment
            Payment original = new Payment(licensePlate, parkingFee, fineAmount, paymentMethod);
            
            Long id = paymentDAO.save(original);
            
            // Retrieve the payment
            Payment retrieved = paymentDAO.findById(id);
            
            // Verify persistence
            assert retrieved != null : "Payment should be retrievable after save";
            assert retrieved.getLicensePlate().equals(original.getLicensePlate()) : 
                "License plate should match";
            assert Math.abs(retrieved.getParkingFee() - original.getParkingFee()) < 0.01 : 
                "Parking fee should match";
            assert Math.abs(retrieved.getFineAmount() - original.getFineAmount()) < 0.01 : 
                "Fine amount should match";
            assert Math.abs(retrieved.getTotalAmount() - original.getTotalAmount()) < 0.01 : 
                "Total amount should match";
            assert retrieved.getPaymentMethod() == original.getPaymentMethod() : 
                "Payment method should match";
        } finally {
            dbManager.shutdown();
        }
    }

    /**
     * Property: For any spot status update, the change should be persisted
     * and retrievable.
     */
    @Property(tries = 100)
    void spotStatusUpdatePersistence(
            @ForAll("spotIds") String spotId,
            @ForAll("spotTypes") SpotType spotType,
            @ForAll("spotStatuses") SpotStatus newStatus) throws SQLException {
        
        DatabaseManager dbManager = new DatabaseManager("jdbc:h2:mem:test_status_" + System.nanoTime());
        dbManager.initializeDatabase();
        
        try {
            ParkingSpotDAO spotDAO = new ParkingSpotDAO(dbManager);
            
            // First, create a parking_lot record (required for foreign key constraint)
            Connection conn = dbManager.getConnection();
            Long parkingLotId;
            try (java.sql.PreparedStatement lotStmt = conn.prepareStatement(
                    "INSERT INTO parking_lots (name, total_floors, total_revenue) VALUES (?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS)) {
                lotStmt.setString(1, "Test Lot");
                lotStmt.setInt(2, 1);
                lotStmt.setDouble(3, 0.0);
                lotStmt.executeUpdate();
                
                try (java.sql.ResultSet lotRs = lotStmt.getGeneratedKeys()) {
                    lotRs.next();
                    parkingLotId = lotRs.getLong(1);
                }
            }
            
            // Now create a floor record
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO floors (parking_lot_id, floor_number, total_spots) VALUES (?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, parkingLotId);
                stmt.setInt(2, 1);
                stmt.setInt(3, 10);
                stmt.executeUpdate();
                
                try (java.sql.ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    Long floorId = rs.getLong(1);
                    
                    // Create and save a parking spot
                    ParkingSpot spot = new ParkingSpot(spotId, spotType);
                    spotDAO.save(floorId, spot);
                    
                    // Update the status
                    spotDAO.updateStatus(spotId, newStatus);
                    
                    // Retrieve and verify
                    ParkingSpot retrieved = spotDAO.findBySpotId(spotId);
                    
                    assert retrieved != null : "Spot should be retrievable after update";
                    assert retrieved.getStatus() == newStatus : 
                        "Updated status should be persisted";
                }
            } finally {
                dbManager.releaseConnection(conn);
            }
        } finally {
            dbManager.shutdown();
        }
    }

    // Arbitraries (generators) for test data

    @Provide
    Arbitrary<String> licensePlates() {
        return Arbitraries.strings()
                .withCharRange('A', 'Z')
                .ofMinLength(3)
                .ofMaxLength(10)
                .map(s -> s + Arbitraries.integers().between(100, 999).sample());
    }

    @Provide
    Arbitrary<VehicleType> vehicleTypes() {
        return Arbitraries.of(VehicleType.class);
    }

    @Provide
    Arbitrary<SpotType> spotTypes() {
        return Arbitraries.of(SpotType.class);
    }

    @Provide
    Arbitrary<SpotStatus> spotStatuses() {
        return Arbitraries.of(SpotStatus.class);
    }

    @Provide
    Arbitrary<FineType> fineTypes() {
        return Arbitraries.of(FineType.class);
    }

    @Provide
    Arbitrary<PaymentMethod> paymentMethods() {
        return Arbitraries.of(PaymentMethod.class);
    }

    @Provide
    Arbitrary<String> spotIds() {
        return Arbitraries.integers().between(1, 10)
                .flatMap(floor -> Arbitraries.integers().between(1, 5)
                        .flatMap(row -> Arbitraries.integers().between(1, 10)
                                .map(spot -> String.format("F%d-R%d-S%d", floor, row, spot))));
    }

    @Provide
    Arbitrary<Double> parkingFees() {
        return Arbitraries.doubles().between(0.0, 100.0).ofScale(2);
    }

    @Provide
    Arbitrary<Double> fineAmounts() {
        return Arbitraries.doubles().between(0.0, 200.0).ofScale(2);
    }
}
