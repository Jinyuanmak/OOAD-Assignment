package com.university.parking.controller;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;

import com.university.parking.model.Fine;
import com.university.parking.model.FineType;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.SpotType;
import com.university.parking.model.VehicleType;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for fine lookup integration.
 * Feature: parking-lot-management, Property 16: Fine Lookup Integration
 * Validates: Requirements 4.5
 * 
 * For any vehicle exit, the system should check for and display 
 * any unpaid fines associated with the license plate.
 */
public class FineLookupIntegrationProperties {

    /**
     * Property 16: Fine Lookup Integration
     * For any vehicle exit, the system should check for and display 
     * any unpaid fines associated with the license plate.
     */
    @Property(tries = 100)
    void exitProcessIncludesUnpaidFines(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll @DoubleRange(min = 10.0, max = 100.0) double fineAmount) {
        
        // Create parking lot with all spot types
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes();
        VehicleEntryController entryController = new VehicleEntryController(parkingLot);
        VehicleExitController exitController = new VehicleExitController(parkingLot);
        
        // Find an available spot for the vehicle type
        List<ParkingSpot> availableSpots = entryController.findAvailableSpots(vehicleType);
        if (availableSpots.isEmpty()) {
            return; // Skip if no compatible spots
        }
        
        String spotId = availableSpots.get(0).getSpotId();
        boolean isHandicapped = vehicleType == VehicleType.HANDICAPPED;
        
        // Process entry
        entryController.processEntry(licensePlate, vehicleType, isHandicapped, spotId);
        
        // Create unpaid fine for this license plate
        Fine unpaidFine = new Fine(licensePlate.trim().toUpperCase(), FineType.OVERSTAY, fineAmount);
        List<Fine> unpaidFines = new ArrayList<>();
        unpaidFines.add(unpaidFine);
        
        // Generate payment summary with unpaid fines
        VehicleExitController.PaymentSummary summary = 
            exitController.generatePaymentSummary(licensePlate, unpaidFines);
        
        // Verify unpaid fines are included in the summary
        Assertions.assertNotNull(summary.getUnpaidFines(),
            "Payment summary should include unpaid fines list");
        Assertions.assertFalse(summary.getUnpaidFines().isEmpty(),
            "Unpaid fines list should not be empty when fines exist");
        Assertions.assertEquals(1, summary.getUnpaidFines().size(),
            "Should have exactly one unpaid fine");
        
        // Verify fine amount is included in total
        Assertions.assertEquals(fineAmount, summary.getTotalFines(), 0.01,
            "Total fines should match the unpaid fine amount");
        
        // Verify total due includes fine
        double expectedTotal = summary.getParkingFee() + fineAmount;
        Assertions.assertEquals(expectedTotal, summary.getTotalDue(), 0.01,
            "Total due should include parking fee plus fines");
    }

    /**
     * Property: Multiple unpaid fines are all included in payment summary.
     */
    @Property(tries = 100)
    void multipleUnpaidFinesAreIncluded(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll @IntRange(min = 1, max = 5) int numberOfFines,
            @ForAll @DoubleRange(min = 10.0, max = 50.0) double baseFineAmount) {
        
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes();
        VehicleEntryController entryController = new VehicleEntryController(parkingLot);
        VehicleExitController exitController = new VehicleExitController(parkingLot);
        
        List<ParkingSpot> availableSpots = entryController.findAvailableSpots(vehicleType);
        if (availableSpots.isEmpty()) {
            return;
        }
        
        String spotId = availableSpots.get(0).getSpotId();
        boolean isHandicapped = vehicleType == VehicleType.HANDICAPPED;
        
        // Process entry
        entryController.processEntry(licensePlate, vehicleType, isHandicapped, spotId);
        
        // Create multiple unpaid fines
        List<Fine> unpaidFines = new ArrayList<>();
        double totalFineAmount = 0.0;
        String normalizedPlate = licensePlate.trim().toUpperCase();
        
        for (int i = 0; i < numberOfFines; i++) {
            double amount = baseFineAmount + (i * 10);
            Fine fine = new Fine(normalizedPlate, FineType.OVERSTAY, amount);
            unpaidFines.add(fine);
            totalFineAmount += amount;
        }
        
        // Generate payment summary
        VehicleExitController.PaymentSummary summary = 
            exitController.generatePaymentSummary(licensePlate, unpaidFines);
        
        // Verify all fines are included
        Assertions.assertEquals(numberOfFines, summary.getUnpaidFines().size(),
            "All unpaid fines should be included");
        Assertions.assertEquals(totalFineAmount, summary.getTotalFines(), 0.01,
            "Total fines should be sum of all unpaid fines");
    }

    /**
     * Property: No fines results in zero fine amount.
     */
    @Property(tries = 100)
    void noFinesResultsInZeroFineAmount(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType) {
        
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes();
        VehicleEntryController entryController = new VehicleEntryController(parkingLot);
        VehicleExitController exitController = new VehicleExitController(parkingLot);
        
        List<ParkingSpot> availableSpots = entryController.findAvailableSpots(vehicleType);
        if (availableSpots.isEmpty()) {
            return;
        }
        
        String spotId = availableSpots.get(0).getSpotId();
        boolean isHandicapped = vehicleType == VehicleType.HANDICAPPED;
        
        // Process entry
        entryController.processEntry(licensePlate, vehicleType, isHandicapped, spotId);
        
        // Generate payment summary with no fines
        VehicleExitController.PaymentSummary summary = 
            exitController.generatePaymentSummary(licensePlate, new ArrayList<>());
        
        // Verify no fines
        Assertions.assertTrue(summary.getUnpaidFines().isEmpty(),
            "Unpaid fines list should be empty");
        Assertions.assertEquals(0.0, summary.getTotalFines(), 0.01,
            "Total fines should be zero when no fines exist");
        
        // Verify total due equals parking fee only
        Assertions.assertEquals(summary.getParkingFee(), summary.getTotalDue(), 0.01,
            "Total due should equal parking fee when no fines");
    }

    /**
     * Property: Null fines list is handled gracefully.
     */
    @Property(tries = 100)
    void nullFinesListIsHandledGracefully(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType) {
        
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes();
        VehicleEntryController entryController = new VehicleEntryController(parkingLot);
        VehicleExitController exitController = new VehicleExitController(parkingLot);
        
        List<ParkingSpot> availableSpots = entryController.findAvailableSpots(vehicleType);
        if (availableSpots.isEmpty()) {
            return;
        }
        
        String spotId = availableSpots.get(0).getSpotId();
        boolean isHandicapped = vehicleType == VehicleType.HANDICAPPED;
        
        // Process entry
        entryController.processEntry(licensePlate, vehicleType, isHandicapped, spotId);
        
        // Generate payment summary with null fines list
        VehicleExitController.PaymentSummary summary = 
            exitController.generatePaymentSummary(licensePlate, null);
        
        // Verify graceful handling
        Assertions.assertNotNull(summary.getUnpaidFines(),
            "Unpaid fines list should not be null");
        Assertions.assertEquals(0.0, summary.getTotalFines(), 0.01,
            "Total fines should be zero when fines list is null");
    }

    /**
     * Property: Fine types are preserved in payment summary.
     */
    @Property(tries = 100)
    void fineTypesArePreserved(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll("fineTypes") FineType fineType,
            @ForAll @DoubleRange(min = 10.0, max = 100.0) double fineAmount) {
        
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes();
        VehicleEntryController entryController = new VehicleEntryController(parkingLot);
        VehicleExitController exitController = new VehicleExitController(parkingLot);
        
        List<ParkingSpot> availableSpots = entryController.findAvailableSpots(vehicleType);
        if (availableSpots.isEmpty()) {
            return;
        }
        
        String spotId = availableSpots.get(0).getSpotId();
        boolean isHandicapped = vehicleType == VehicleType.HANDICAPPED;
        
        // Process entry
        entryController.processEntry(licensePlate, vehicleType, isHandicapped, spotId);
        
        // Create fine with specific type
        Fine fine = new Fine(licensePlate.trim().toUpperCase(), fineType, fineAmount);
        List<Fine> unpaidFines = new ArrayList<>();
        unpaidFines.add(fine);
        
        // Generate payment summary
        VehicleExitController.PaymentSummary summary = 
            exitController.generatePaymentSummary(licensePlate, unpaidFines);
        
        // Verify fine type is preserved
        Assertions.assertEquals(fineType, summary.getUnpaidFines().get(0).getType(),
            "Fine type should be preserved in payment summary");
    }

    @Provide
    Arbitrary<String> licensePlates() {
        Arbitrary<String> letters = Arbitraries.strings()
            .withCharRange('A', 'Z')
            .ofMinLength(1)
            .ofMaxLength(3);
        Arbitrary<String> numbers = Arbitraries.strings()
            .withCharRange('0', '9')
            .ofMinLength(1)
            .ofMaxLength(4);
        
        return Combinators.combine(letters, numbers)
            .as((l, n) -> l + n);
    }

    @Provide
    Arbitrary<VehicleType> vehicleTypes() {
        return Arbitraries.of(VehicleType.values());
    }

    @Provide
    Arbitrary<FineType> fineTypes() {
        return Arbitraries.of(FineType.values());
    }

    private ParkingLot createParkingLotWithAllSpotTypes() {
        ParkingLot parkingLot = new ParkingLot("Test Parking Lot");
        List<ParkingLot.RowConfiguration> rowConfigs = new ArrayList<>();
        SpotType[] spotTypes = SpotType.values();
        rowConfigs.add(new ParkingLot.RowConfiguration(spotTypes.length, spotTypes));
        parkingLot.createFloor(1, rowConfigs);
        return parkingLot;
    }
}
