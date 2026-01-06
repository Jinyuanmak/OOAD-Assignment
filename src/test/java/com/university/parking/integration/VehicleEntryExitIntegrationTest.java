package com.university.parking.integration;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.university.parking.controller.VehicleEntryController;
import com.university.parking.controller.VehicleExitController;
import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.Floor;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.PaymentMethod;
import com.university.parking.model.SpotType;
import com.university.parking.model.VehicleType;

/**
 * Integration tests for complete vehicle entry and exit workflows.
 * Tests the full flow from vehicle entry through payment and exit.
 */
public class VehicleEntryExitIntegrationTest {
    private ParkingLot parkingLot;
    private DatabaseManager dbManager;
    private FineDAO fineDAO;
    private VehicleEntryController entryController;
    private VehicleExitController exitController;

    @BeforeEach
    public void setUp() throws SQLException {
        // Create parking lot with test configuration
        parkingLot = new ParkingLot("Test Parking Lot");
        Floor floor = new Floor(1);
        floor.createRow(1, 3, new SpotType[]{SpotType.COMPACT, SpotType.REGULAR, SpotType.HANDICAPPED});
        parkingLot.addFloor(floor);

        // Initialize database with unique name for each test
        String testDbUrl = "jdbc:h2:mem:test_" + System.currentTimeMillis() + ";DB_CLOSE_DELAY=-1";
        dbManager = new DatabaseManager(testDbUrl);
        dbManager.initializeDatabase();
        fineDAO = new FineDAO(dbManager);

        // Create controllers
        entryController = new VehicleEntryController(parkingLot, dbManager);
        exitController = new VehicleExitController(parkingLot, dbManager, fineDAO);
    }

    @AfterEach
    public void tearDown() {
        if (dbManager != null) {
            dbManager.shutdown();
        }
    }

    @Test
    public void testCompleteVehicleEntryAndExitWorkflow() {
        // Test complete workflow: entry -> exit -> payment
        String licensePlate = "ABC123";
        VehicleType vehicleType = VehicleType.CAR;
        String spotId = "F1-R1-S2"; // Regular spot

        // Step 1: Process vehicle entry
        VehicleEntryController.EntryResult entryResult = entryController.processEntry(
            licensePlate, vehicleType, false, spotId
        );

        assertNotNull(entryResult);
        assertEquals(licensePlate, entryResult.getVehicle().getLicensePlate());
        assertEquals(spotId, entryResult.getSpot().getSpotId());
        assertFalse(entryResult.getSpot().isAvailable());
        assertTrue(entryResult.getTicketNumber().startsWith("T-" + licensePlate));

        // Step 2: Lookup vehicle for exit
        VehicleExitController.VehicleLookupResult lookupResult = exitController.lookupVehicle(licensePlate);
        assertNotNull(lookupResult);
        assertEquals(licensePlate, lookupResult.getVehicle().getLicensePlate());

        // Step 3: Generate payment summary
        List<Fine> unpaidFines = exitController.getUnpaidFines(licensePlate);
        VehicleExitController.PaymentSummary summary = exitController.generatePaymentSummary(
            licensePlate, unpaidFines
        );

        assertNotNull(summary);
        assertTrue(summary.getParkingFee() > 0);
        assertEquals(0, summary.getTotalFines());

        // Step 4: Process exit with payment
        double amountPaid = summary.getTotalDue();
        VehicleExitController.ExitResult exitResult = exitController.processExit(
            licensePlate, amountPaid, PaymentMethod.CASH, unpaidFines
        );

        assertNotNull(exitResult);
        assertTrue(exitResult.isPaymentSufficient());
        assertEquals(0, exitResult.getRemainingBalance());
        assertNotNull(exitResult.getReceipt());

        // Step 5: Verify spot is now available
        ParkingSpot spot = findSpotById(spotId);
        assertTrue(spot.isAvailable());
        assertNull(spot.getCurrentVehicle());
    }

    @Test
    public void testVehicleEntryWithHandicappedVehicle() {
        // Test handicapped vehicle can park in any spot type
        String licensePlate = "HCP001";
        VehicleType vehicleType = VehicleType.HANDICAPPED;
        String spotId = "F1-R1-S2"; // Regular spot

        VehicleEntryController.EntryResult entryResult = entryController.processEntry(
            licensePlate, vehicleType, true, spotId
        );

        assertNotNull(entryResult);
        assertEquals(licensePlate, entryResult.getVehicle().getLicensePlate());
        assertTrue(entryResult.getVehicle().isHandicapped());
        assertFalse(entryResult.getSpot().isAvailable());
    }

    @Test
    public void testVehicleExitWithUnpaidFines() throws SQLException {
        // Test exit workflow when vehicle has unpaid fines
        String licensePlate = "FINE123";
        VehicleType vehicleType = VehicleType.CAR;
        String spotId = "F1-R1-S2";

        // Step 1: Process entry
        entryController.processEntry(licensePlate, vehicleType, false, spotId);

        // Step 2: Add unpaid fine to database
        Fine fine = new Fine();
        fine.setLicensePlate(licensePlate);
        fine.setType(com.university.parking.model.FineType.OVERSTAY);
        fine.setAmount(50.0);
        fine.setIssuedDate(LocalDateTime.now());
        fine.setPaid(false);
        fineDAO.save(fine);

        // Step 3: Lookup vehicle and generate summary
        List<Fine> unpaidFines = exitController.getUnpaidFines(licensePlate);
        assertEquals(1, unpaidFines.size());

        VehicleExitController.PaymentSummary summary = exitController.generatePaymentSummary(
            licensePlate, unpaidFines
        );

        assertTrue(summary.getTotalFines() > 0);
        assertTrue(summary.getTotalDue() > summary.getParkingFee());

        // Step 4: Process exit with full payment
        double amountPaid = summary.getTotalDue();
        VehicleExitController.ExitResult exitResult = exitController.processExit(
            licensePlate, amountPaid, PaymentMethod.CARD, unpaidFines
        );

        assertTrue(exitResult.isPaymentSufficient());
        assertEquals(0, exitResult.getRemainingBalance());
    }

    @Test
    public void testVehicleExitWithInsufficientPayment() {
        // Test exit workflow with insufficient payment
        String licensePlate = "PARTIAL123";
        VehicleType vehicleType = VehicleType.CAR;
        String spotId = "F1-R1-S2";

        // Step 1: Process entry
        entryController.processEntry(licensePlate, vehicleType, false, spotId);

        // Step 2: Generate payment summary
        List<Fine> unpaidFines = exitController.getUnpaidFines(licensePlate);
        VehicleExitController.PaymentSummary summary = exitController.generatePaymentSummary(
            licensePlate, unpaidFines
        );

        // Step 3: Process exit with partial payment
        double partialPayment = summary.getTotalDue() / 2;
        VehicleExitController.ExitResult exitResult = exitController.processExit(
            licensePlate, partialPayment, PaymentMethod.CASH, unpaidFines
        );

        assertFalse(exitResult.isPaymentSufficient());
        assertTrue(exitResult.getRemainingBalance() > 0);
        
        // Spot should still be vacated even with partial payment
        ParkingSpot spot = findSpotById(spotId);
        assertTrue(spot.isAvailable());
    }

    @Test
    public void testMultipleVehiclesEntryAndExit() {
        // Test multiple vehicles can enter and exit independently
        // Spots: F1-R1-S1=COMPACT, F1-R1-S2=REGULAR, F1-R1-S3=HANDICAPPED
        String[] licensePlates = {"MOTO001", "CAR001", "CAR002"};
        String[] spotIds = {"F1-R1-S1", "F1-R1-S2", "F1-R1-S2"}; // Compact, Regular, Regular (reuse after exit)
        VehicleType[] types = {VehicleType.MOTORCYCLE, VehicleType.CAR, VehicleType.CAR};

        // Enter first two vehicles
        for (int i = 0; i < 2; i++) {
            VehicleEntryController.EntryResult result = entryController.processEntry(
                licensePlates[i], types[i], false, spotIds[i]
            );
            assertNotNull(result);
            assertFalse(result.getSpot().isAvailable());
        }

        // Exit first vehicle
        List<Fine> fines = exitController.getUnpaidFines(licensePlates[0]);
        VehicleExitController.PaymentSummary summary = exitController.generatePaymentSummary(
            licensePlates[0], fines
        );
        VehicleExitController.ExitResult result = exitController.processExit(
            licensePlates[0], summary.getTotalDue(), PaymentMethod.CASH, fines
        );
        assertTrue(result.isPaymentSufficient());

        // Enter third vehicle in now-available spot
        VehicleEntryController.EntryResult entryResult = entryController.processEntry(
            licensePlates[2], types[2], false, "F1-R1-S1"
        );
        assertNotNull(entryResult);

        // Exit remaining vehicles
        for (int i = 1; i < licensePlates.length; i++) {
            fines = exitController.getUnpaidFines(licensePlates[i]);
            summary = exitController.generatePaymentSummary(licensePlates[i], fines);
            result = exitController.processExit(
                licensePlates[i], summary.getTotalDue(), PaymentMethod.CASH, fines
            );
            assertTrue(result.isPaymentSufficient());
        }

        // Verify all spots are available
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            assertTrue(spot.isAvailable());
        }
    }

    @Test
    public void testInvalidVehicleEntry() {
        // Test that invalid entries are rejected
        String licensePlate = "INVALID";
        VehicleType vehicleType = VehicleType.MOTORCYCLE;
        String spotId = "F1-R1-S2"; // Regular spot - motorcycles can't park here

        assertThrows(IllegalArgumentException.class, () -> {
            entryController.processEntry(licensePlate, vehicleType, false, spotId);
        });
    }

    @Test
    public void testVehicleExitNotFound() {
        // Test exit for non-existent vehicle
        String licensePlate = "NOTFOUND";
        
        VehicleExitController.VehicleLookupResult result = exitController.lookupVehicle(licensePlate);
        assertNull(result);
    }

    private ParkingSpot findSpotById(String spotId) {
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            if (spot.getSpotId().equals(spotId)) {
                return spot;
            }
        }
        return null;
    }
}
