package com.university.parking.util;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;

import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.FineType;
import com.university.parking.model.FixedFineStrategy;
import com.university.parking.model.Vehicle;
import com.university.parking.model.VehicleType;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for fine persistence across sessions.
 * Feature: parking-lot-management, Property 9: Fine Persistence Across Sessions
 * Validates: Requirements 5.6
 */
public class FinePersistenceAcrossSessionsProperties {

    /**
     * Property 9: Fine Persistence Across Sessions
     * For any license plate with unpaid fines, those fines should appear in subsequent
     * parking sessions until paid.
     */
    @Property(tries = 100)
    void unpaidFinesShouldPersistAcrossSessions(
            @ForAll @IntRange(min = 1, max = 10) int numberOfFines,
            @ForAll VehicleType vehicleType
    ) throws SQLException {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase();
        FineDAO fineDAO = new FineDAO(dbManager);
        FineManager fineManager = new FineManager(fineDAO);

        // Use shorter license plate to fit VARCHAR(20) constraint
        String licensePlate = "P" + numberOfFines + "-" + (System.currentTimeMillis() % 100000);

        try {
            // Generate and save multiple unpaid fines for the same license plate
            for (int i = 0; i < numberOfFines; i++) {
                Vehicle vehicle = new Vehicle(licensePlate, vehicleType, false);
                vehicle.setEntryTime(LocalDateTime.now().minusHours(30 + i));
                vehicle.setExitTime(LocalDateTime.now().minusHours(i));

                Fine fine = fineManager.checkAndGenerateOverstayFine(vehicle, new FixedFineStrategy());
                if (fine != null) {
                    fineManager.saveFine(fine);
                }
            }

            // Retrieve unpaid fines in a "new session"
            List<Fine> unpaidFines = fineManager.getUnpaidFines(licensePlate);

            // Assert that all unpaid fines persist
            Assertions.assertFalse(unpaidFines.isEmpty(),
                "Unpaid fines should persist across sessions");
            Assertions.assertTrue(unpaidFines.size() <= numberOfFines,
                String.format("Should have at most %d unpaid fines", numberOfFines));

            // Verify all fines are linked to the correct license plate
            for (Fine fine : unpaidFines) {
                Assertions.assertEquals(licensePlate, fine.getLicensePlate(),
                    "Fine should be linked to the correct license plate");
                Assertions.assertFalse(fine.isPaid(),
                    "Fine should be unpaid");
            }
        } finally {
            // Cleanup: delete test fines
            List<Fine> testFines = fineDAO.findByLicensePlate(licensePlate);
            for (Fine fine : testFines) {
                fineDAO.delete(fine.getId());
            }
        }
    }

    @Property(tries = 100)
    void paidFinesShouldNotAppearInUnpaidList(
            @ForAll @IntRange(min = 1, max = 5) int numberOfFines,
            @ForAll VehicleType vehicleType
    ) throws SQLException {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase();
        FineDAO fineDAO = new FineDAO(dbManager);
        FineManager fineManager = new FineManager(fineDAO);

        // Use shorter license plate to fit VARCHAR(20) constraint
        String licensePlate = "PD" + numberOfFines + "-" + (System.currentTimeMillis() % 100000);

        try {
            // Generate and save fines
            for (int i = 0; i < numberOfFines; i++) {
                Fine fine = new Fine(licensePlate, FineType.OVERSTAY, 50.0);
                Long fineId = fineManager.saveFine(fine);

                // Mark some fines as paid
                if (i % 2 == 0) {
                    fineDAO.markAsPaid(fineId);
                }
            }

            // Retrieve unpaid fines
            List<Fine> unpaidFines = fineManager.getUnpaidFines(licensePlate);

            // Assert that only unpaid fines appear
            for (Fine fine : unpaidFines) {
                Assertions.assertFalse(fine.isPaid(),
                    "Only unpaid fines should appear in unpaid list");
            }
        } finally {
            // Cleanup
            List<Fine> testFines = fineDAO.findByLicensePlate(licensePlate);
            for (Fine fine : testFines) {
                fineDAO.delete(fine.getId());
            }
        }
    }

    @Property(tries = 100)
    void totalUnpaidFinesShouldSumCorrectly(
            @ForAll @IntRange(min = 1, max = 5) int numberOfFines,
            @ForAll @DoubleRange(min = 10.0, max = 200.0) double fineAmount,
            @ForAll VehicleType vehicleType
    ) throws SQLException {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase();
        FineDAO fineDAO = new FineDAO(dbManager);
        FineManager fineManager = new FineManager(fineDAO);

        // Use shorter license plate to fit VARCHAR(20) constraint
        String licensePlate = "SM" + numberOfFines + "-" + (System.currentTimeMillis() % 100000);

        try {
            // Generate and save fines with the same amount
            for (int i = 0; i < numberOfFines; i++) {
                Fine fine = new Fine(licensePlate, FineType.OVERSTAY, fineAmount);
                fineManager.saveFine(fine);
            }

            // Calculate total unpaid fines
            double totalUnpaid = fineManager.calculateTotalUnpaidFines(licensePlate);

            // Expected total
            double expectedTotal = numberOfFines * fineAmount;

            // Assert the total is correct
            Assertions.assertEquals(expectedTotal, totalUnpaid, 0.01,
                String.format("Total unpaid fines should be %.2f but got %.2f", expectedTotal, totalUnpaid));
        } finally {
            // Cleanup
            List<Fine> testFines = fineDAO.findByLicensePlate(licensePlate);
            for (Fine fine : testFines) {
                fineDAO.delete(fine.getId());
            }
        }
    }

    @Property(tries = 100)
    void finesForDifferentLicensePlatesShouldBeIndependent(
            @ForAll @IntRange(min = 1, max = 3) int finesForPlate1,
            @ForAll @IntRange(min = 1, max = 3) int finesForPlate2,
            @ForAll VehicleType vehicleType
    ) throws SQLException {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase();
        FineDAO fineDAO = new FineDAO(dbManager);
        FineManager fineManager = new FineManager(fineDAO);

        // Use shorter license plates to fit VARCHAR(20) constraint
        long timestamp = System.currentTimeMillis() % 100000;
        String licensePlate1 = "PL1-" + timestamp;
        String licensePlate2 = "PL2-" + timestamp;

        try {
            // Generate fines for first license plate
            for (int i = 0; i < finesForPlate1; i++) {
                Fine fine = new Fine(licensePlate1, FineType.OVERSTAY, 50.0);
                fineManager.saveFine(fine);
            }

            // Generate fines for second license plate
            for (int i = 0; i < finesForPlate2; i++) {
                Fine fine = new Fine(licensePlate2, FineType.OVERSTAY, 50.0);
                fineManager.saveFine(fine);
            }

            // Retrieve unpaid fines for each plate
            List<Fine> finesPlate1 = fineManager.getUnpaidFines(licensePlate1);
            List<Fine> finesPlate2 = fineManager.getUnpaidFines(licensePlate2);

            // Assert independence
            Assertions.assertEquals(finesForPlate1, finesPlate1.size(),
                "First license plate should have correct number of fines");
            Assertions.assertEquals(finesForPlate2, finesPlate2.size(),
                "Second license plate should have correct number of fines");

            // Verify no cross-contamination
            for (Fine fine : finesPlate1) {
                Assertions.assertEquals(licensePlate1, fine.getLicensePlate());
            }
            for (Fine fine : finesPlate2) {
                Assertions.assertEquals(licensePlate2, fine.getLicensePlate());
            }
        } finally {
            // Cleanup
            List<Fine> testFines1 = fineDAO.findByLicensePlate(licensePlate1);
            for (Fine fine : testFines1) {
                fineDAO.delete(fine.getId());
            }
            List<Fine> testFines2 = fineDAO.findByLicensePlate(licensePlate2);
            for (Fine fine : testFines2) {
                fineDAO.delete(fine.getId());
            }
        }
    }
}
