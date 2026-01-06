package com.university.parking.integration;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.university.parking.controller.VehicleEntryController;
import com.university.parking.controller.VehicleExitController;
import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.FineCalculationStrategy;
import com.university.parking.model.FineType;
import com.university.parking.model.FixedFineStrategy;
import com.university.parking.model.Floor;
import com.university.parking.model.HourlyFineStrategy;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.PaymentMethod;
import com.university.parking.model.ProgressiveFineStrategy;
import com.university.parking.model.SpotType;
import com.university.parking.model.VehicleType;

/**
 * Integration tests for admin operations and reporting functionality.
 * Tests fine management, occupancy tracking, and revenue reporting.
 */
public class AdminOperationsIntegrationTest {
    private ParkingLot parkingLot;
    private DatabaseManager dbManager;
    private FineDAO fineDAO;
    private VehicleEntryController entryController;
    private VehicleExitController exitController;

    @BeforeEach
    public void setUp() throws SQLException {
        // Create parking lot with test configuration
        parkingLot = new ParkingLot("Test Parking Lot");
        
        // Create 2 floors with different spot types
        for (int floorNum = 1; floorNum <= 2; floorNum++) {
            Floor floor = new Floor(floorNum);
            floor.createRow(1, 5, new SpotType[]{
                SpotType.COMPACT, SpotType.COMPACT, SpotType.REGULAR, 
                SpotType.REGULAR, SpotType.HANDICAPPED
            });
            parkingLot.addFloor(floor);
        }

        // Initialize database
        String testDbUrl = "jdbc:h2:mem:admin_test_" + System.currentTimeMillis() + ";DB_CLOSE_DELAY=-1";
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
    public void testOccupancyRateCalculation() {
        // Test occupancy rate calculation with various scenarios
        
        // Initially all spots should be available
        int totalSpots = parkingLot.getAllSpots().size();
        int availableSpots = 0;
        for (Floor floor : parkingLot.getFloors()) {
            availableSpots += floor.getAvailableSpots().size();
        }
        assertEquals(totalSpots, availableSpots);
        
        double initialOccupancy = ((totalSpots - availableSpots) * 100.0) / totalSpots;
        assertEquals(0.0, initialOccupancy, 0.01);

        // Park 3 vehicles
        entryController.processEntry("CAR001", VehicleType.CAR, false, "F1-R1-S3");
        entryController.processEntry("CAR002", VehicleType.CAR, false, "F1-R1-S4");
        entryController.processEntry("MOTO001", VehicleType.MOTORCYCLE, false, "F1-R1-S1");

        // Calculate occupancy
        availableSpots = 0;
        for (Floor floor : parkingLot.getFloors()) {
            availableSpots += floor.getAvailableSpots().size();
        }
        int occupied = totalSpots - availableSpots;
        double occupancy = (occupied * 100.0) / totalSpots;
        
        assertEquals(3, occupied);
        assertEquals(30.0, occupancy, 0.01); // 3 out of 10 spots = 30%
    }

    @Test
    public void testRevenueTracking() {
        // Test revenue tracking across multiple transactions
        double initialRevenue = parkingLot.getTotalRevenue();
        assertEquals(0.0, initialRevenue);

        // Process entry and exit for multiple vehicles
        String[] licensePlates = {"REV001", "REV002", "REV003"};
        String[] spotIds = {"F1-R1-S3", "F1-R1-S4", "F2-R1-S3"};
        double totalExpectedRevenue = 0;

        for (int i = 0; i < licensePlates.length; i++) {
            // Entry
            entryController.processEntry(licensePlates[i], VehicleType.CAR, false, spotIds[i]);
            
            // Exit with payment
            List<Fine> fines = exitController.getUnpaidFines(licensePlates[i]);
            VehicleExitController.PaymentSummary summary = exitController.generatePaymentSummary(
                licensePlates[i], fines
            );
            
            exitController.processExit(
                licensePlates[i], summary.getTotalDue(), PaymentMethod.CASH, fines
            );
            
            totalExpectedRevenue += summary.getTotalDue();
        }

        // Verify revenue was tracked
        assertEquals(totalExpectedRevenue, parkingLot.getTotalRevenue(), 0.01);
        assertTrue(parkingLot.getTotalRevenue() > 0);
    }

    @Test
    public void testFineStrategyChange() {
        // Test that fine strategy changes affect future entries only
        
        // Set initial strategy to Fixed
        FineCalculationStrategy fixedStrategy = new FixedFineStrategy();
        parkingLot.changeFineStrategy(fixedStrategy);
        
        assertEquals(fixedStrategy, parkingLot.getFineCalculationContext().getStrategy());

        // Change to Progressive strategy
        FineCalculationStrategy progressiveStrategy = new ProgressiveFineStrategy();
        parkingLot.changeFineStrategy(progressiveStrategy);
        
        assertEquals(progressiveStrategy, parkingLot.getFineCalculationContext().getStrategy());

        // Change to Hourly strategy
        FineCalculationStrategy hourlyStrategy = new HourlyFineStrategy();
        parkingLot.changeFineStrategy(hourlyStrategy);
        
        assertEquals(hourlyStrategy, parkingLot.getFineCalculationContext().getStrategy());
    }

    @Test
    public void testUnpaidFineTracking() throws SQLException {
        // Test tracking of unpaid fines across the system
        
        // Create multiple fines for different vehicles
        String[] licensePlates = {"FINE001", "FINE002", "FINE003"};
        double[] fineAmounts = {50.0, 75.0, 100.0};
        
        for (int i = 0; i < licensePlates.length; i++) {
            Fine fine = new Fine();
            fine.setLicensePlate(licensePlates[i]);
            fine.setType(FineType.OVERSTAY);
            fine.setAmount(fineAmounts[i]);
            fine.setIssuedDate(LocalDateTime.now());
            fine.setPaid(false);
            fineDAO.save(fine);
        }

        // Verify all fines are tracked as unpaid
        List<Fine> allUnpaidFines = fineDAO.findAllUnpaid();
        assertEquals(3, allUnpaidFines.size());

        // Verify fines can be retrieved by license plate
        for (String licensePlate : licensePlates) {
            List<Fine> vehicleFines = exitController.getUnpaidFines(licensePlate);
            assertEquals(1, vehicleFines.size());
            assertEquals(licensePlate, vehicleFines.get(0).getLicensePlate());
        }
    }

    @Test
    public void testCurrentVehicleListing() {
        // Test listing of currently parked vehicles
        
        // Park multiple vehicles
        String[] licensePlates = {"LIST001", "LIST002", "LIST003", "LIST004"};
        String[] spotIds = {"F1-R1-S1", "F1-R1-S3", "F2-R1-S3", "F2-R1-S4"};
        VehicleType[] types = {
            VehicleType.MOTORCYCLE, VehicleType.CAR, 
            VehicleType.CAR, VehicleType.SUV_TRUCK
        };

        for (int i = 0; i < licensePlates.length; i++) {
            entryController.processEntry(licensePlates[i], types[i], false, spotIds[i]);
        }

        // Count currently parked vehicles
        int parkedCount = 0;
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            if (!spot.isAvailable() && spot.getCurrentVehicle() != null) {
                parkedCount++;
            }
        }

        assertEquals(4, parkedCount);

        // Exit one vehicle
        List<Fine> fines = exitController.getUnpaidFines(licensePlates[0]);
        VehicleExitController.PaymentSummary summary = exitController.generatePaymentSummary(
            licensePlates[0], fines
        );
        exitController.processExit(licensePlates[0], summary.getTotalDue(), PaymentMethod.CASH, fines);

        // Verify count decreased
        parkedCount = 0;
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            if (!spot.isAvailable() && spot.getCurrentVehicle() != null) {
                parkedCount++;
            }
        }

        assertEquals(3, parkedCount);
    }

    @Test
    public void testFloorOccupancyBreakdown() {
        // Test occupancy tracking per floor
        
        // Park vehicles on different floors
        entryController.processEntry("F1CAR1", VehicleType.CAR, false, "F1-R1-S3");
        entryController.processEntry("F1CAR2", VehicleType.CAR, false, "F1-R1-S4");
        entryController.processEntry("F2CAR1", VehicleType.CAR, false, "F2-R1-S3");

        // Check floor 1 occupancy
        Floor floor1 = parkingLot.getFloors().get(0);
        int floor1Total = floor1.getAllSpots().size();
        int floor1Available = floor1.getAvailableSpots().size();
        int floor1Occupied = floor1Total - floor1Available;
        
        assertEquals(2, floor1Occupied);

        // Check floor 2 occupancy
        Floor floor2 = parkingLot.getFloors().get(1);
        int floor2Total = floor2.getAllSpots().size();
        int floor2Available = floor2.getAvailableSpots().size();
        int floor2Occupied = floor2Total - floor2Available;
        
        assertEquals(1, floor2Occupied);
    }

    @Test
    public void testFinePaymentIntegration() throws SQLException {
        // Test complete fine payment workflow
        String licensePlate = "FINEPAY001";
        
        // Park vehicle
        entryController.processEntry(licensePlate, VehicleType.CAR, false, "F1-R1-S3");

        // Add fine
        Fine fine = new Fine();
        fine.setLicensePlate(licensePlate);
        fine.setType(FineType.UNAUTHORIZED_RESERVED);
        fine.setAmount(100.0);
        fine.setIssuedDate(LocalDateTime.now());
        fine.setPaid(false);
        Long fineId = fineDAO.save(fine);
        assertNotNull(fineId);

        // Verify fine is unpaid
        List<Fine> unpaidFines = exitController.getUnpaidFines(licensePlate);
        assertEquals(1, unpaidFines.size());
        assertFalse(unpaidFines.get(0).isPaid());

        // Process exit with payment
        VehicleExitController.PaymentSummary summary = exitController.generatePaymentSummary(
            licensePlate, unpaidFines
        );
        
        exitController.processExit(
            licensePlate, summary.getTotalDue(), PaymentMethod.CARD, unpaidFines
        );

        // Verify fine is marked as paid in database
        Fine updatedFine = fineDAO.findById(fineId);
        assertTrue(updatedFine.isPaid());
    }

    @Test
    public void testSpotTypeOccupancy() {
        // Test occupancy tracking by spot type
        
        // Park vehicles in different spot types
        entryController.processEntry("COMPACT1", VehicleType.MOTORCYCLE, false, "F1-R1-S1");
        entryController.processEntry("COMPACT2", VehicleType.MOTORCYCLE, false, "F1-R1-S2");
        entryController.processEntry("REGULAR1", VehicleType.CAR, false, "F1-R1-S3");
        entryController.processEntry("HANDICAP1", VehicleType.HANDICAPPED, true, "F1-R1-S5");

        // Count occupancy by spot type
        int compactOccupied = 0;
        int regularOccupied = 0;
        int handicappedOccupied = 0;

        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            if (!spot.isAvailable()) {
                switch (spot.getType()) {
                    case COMPACT:
                        compactOccupied++;
                        break;
                    case REGULAR:
                        regularOccupied++;
                        break;
                    case HANDICAPPED:
                        handicappedOccupied++;
                        break;
                    default:
                        break;
                }
            }
        }

        assertEquals(2, compactOccupied);
        assertEquals(1, regularOccupied);
        assertEquals(1, handicappedOccupied);
    }
}
