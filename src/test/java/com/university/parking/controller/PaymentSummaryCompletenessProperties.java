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

/**
 * Property-based tests for payment summary completeness.
 * Feature: parking-lot-management, Property 17: Payment Summary Completeness
 * Validates: Requirements 4.6
 * 
 * For any exit process, the payment summary should display hours parked, 
 * parking fee, unpaid fines, and total amount due.
 */
public class PaymentSummaryCompletenessProperties {

    /**
     * Property 17: Payment Summary Completeness
     * For any exit process, the payment summary should display hours parked, 
     * parking fee, unpaid fines, and total amount due.
     */
    @Property(tries = 100)
    void paymentSummaryContainsAllRequiredFields(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double fineAmount) {
        
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
        
        // Create unpaid fines
        List<Fine> unpaidFines = new ArrayList<>();
        if (fineAmount > 0) {
            Fine fine = new Fine(licensePlate.trim().toUpperCase(), FineType.OVERSTAY, fineAmount);
            unpaidFines.add(fine);
        }
        
        // Generate payment summary
        VehicleExitController.PaymentSummary summary = 
            exitController.generatePaymentSummary(licensePlate, unpaidFines);
        
        // Verify hours parked is present and valid
        Assertions.assertTrue(summary.getDurationHours() >= 1,
            "Hours parked should be at least 1 (minimum charge)");
        
        // Verify parking fee is present and valid
        Assertions.assertTrue(summary.getParkingFee() >= 0,
            "Parking fee should be non-negative");
        
        // Verify unpaid fines list is present
        Assertions.assertNotNull(summary.getUnpaidFines(),
            "Unpaid fines list should be present");
        
        // Verify total fines is present and valid
        Assertions.assertTrue(summary.getTotalFines() >= 0,
            "Total fines should be non-negative");
        
        // Verify total due is present and correct
        double expectedTotal = summary.getParkingFee() + summary.getTotalFines();
        Assertions.assertEquals(expectedTotal, summary.getTotalDue(), 0.01,
            "Total due should equal parking fee plus total fines");
    }

    /**
     * Property: Hours parked is calculated correctly (ceiling rounded).
     */
    @Property(tries = 100)
    void hoursParkedIsCalculatedCorrectly(
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
        
        // Generate payment summary
        VehicleExitController.PaymentSummary summary = 
            exitController.generatePaymentSummary(licensePlate, new ArrayList<>());
        
        // Verify hours parked is at least 1 (minimum charge)
        Assertions.assertTrue(summary.getDurationHours() >= 1,
            "Hours parked should be at least 1 for minimum charge");
        
        // Verify hours parked is a whole number (ceiling rounded)
        Assertions.assertEquals((long) summary.getDurationHours(), summary.getDurationHours(),
            "Hours parked should be a whole number (ceiling rounded)");
    }

    /**
     * Property: Parking fee is calculated based on duration and spot rate.
     */
    @Property(tries = 100)
    void parkingFeeIsCalculatedCorrectly(
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
        
        // Generate payment summary
        VehicleExitController.PaymentSummary summary = 
            exitController.generatePaymentSummary(licensePlate, new ArrayList<>());
        
        // Get spot hourly rate
        double hourlyRate = summary.getSpot().getHourlyRate();
        
        // For handicapped vehicles in handicapped spots, rate is RM 2
        if (isHandicapped && summary.getSpot().getType() == SpotType.HANDICAPPED) {
            hourlyRate = 2.0;
        }
        
        // Verify parking fee calculation
        double expectedFee = summary.getDurationHours() * hourlyRate;
        Assertions.assertEquals(expectedFee, summary.getParkingFee(), 0.01,
            "Parking fee should equal duration × hourly rate");
    }

    /**
     * Property: Total due is always non-negative.
     */
    @Property(tries = 100)
    void totalDueIsNonNegative(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll @DoubleRange(min = 0.0, max = 200.0) double fineAmount) {
        
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
        
        // Create fines
        List<Fine> unpaidFines = new ArrayList<>();
        if (fineAmount > 0) {
            Fine fine = new Fine(licensePlate.trim().toUpperCase(), FineType.OVERSTAY, fineAmount);
            unpaidFines.add(fine);
        }
        
        // Generate payment summary
        VehicleExitController.PaymentSummary summary = 
            exitController.generatePaymentSummary(licensePlate, unpaidFines);
        
        // Verify total due is non-negative
        Assertions.assertTrue(summary.getTotalDue() >= 0,
            "Total due should always be non-negative");
    }

    /**
     * Property: Payment summary display text contains all required information.
     */
    @Property(tries = 100)
    void displayTextContainsAllRequiredInfo(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll @DoubleRange(min = 10.0, max = 50.0) double fineAmount) {
        
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
        
        // Create fine
        Fine fine = new Fine(licensePlate.trim().toUpperCase(), FineType.OVERSTAY, fineAmount);
        List<Fine> unpaidFines = new ArrayList<>();
        unpaidFines.add(fine);
        
        // Generate payment summary
        VehicleExitController.PaymentSummary summary = 
            exitController.generatePaymentSummary(licensePlate, unpaidFines);
        
        // Get display text
        String displayText = summary.getDisplayText();
        
        // Verify display text contains required information
        Assertions.assertTrue(displayText.contains("Hours Parked"),
            "Display text should contain hours parked");
        Assertions.assertTrue(displayText.contains("Parking Fee"),
            "Display text should contain parking fee");
        Assertions.assertTrue(displayText.contains("Unpaid Fines") || displayText.contains("Fines"),
            "Display text should contain fines information");
        Assertions.assertTrue(displayText.contains("TOTAL DUE") || displayText.contains("Total"),
            "Display text should contain total due");
    }

    /**
     * Property: Vehicle and spot information is included in summary.
     */
    @Property(tries = 100)
    void vehicleAndSpotInfoIsIncluded(
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
        
        // Generate payment summary
        VehicleExitController.PaymentSummary summary = 
            exitController.generatePaymentSummary(licensePlate, new ArrayList<>());
        
        // Verify vehicle information is present
        Assertions.assertNotNull(summary.getVehicle(),
            "Vehicle should be included in summary");
        Assertions.assertEquals(licensePlate.trim().toUpperCase(), 
            summary.getVehicle().getLicensePlate(),
            "Vehicle license plate should match");
        
        // Verify spot information is present
        Assertions.assertNotNull(summary.getSpot(),
            "Spot should be included in summary");
        Assertions.assertEquals(spotId, summary.getSpot().getSpotId(),
            "Spot ID should match");
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

    private ParkingLot createParkingLotWithAllSpotTypes() {
        ParkingLot parkingLot = new ParkingLot("Test Parking Lot");
        List<ParkingLot.RowConfiguration> rowConfigs = new ArrayList<>();
        SpotType[] spotTypes = SpotType.values();
        rowConfigs.add(new ParkingLot.RowConfiguration(spotTypes.length, spotTypes));
        parkingLot.createFloor(1, rowConfigs);
        return parkingLot;
    }
}
