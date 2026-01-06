package com.university.parking.controller;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;

import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.SpotType;
import com.university.parking.model.Vehicle;
import com.university.parking.model.VehicleType;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Property-based tests for vehicle data recording.
 * Feature: parking-lot-management, Property 13: Vehicle Data Recording
 * Validates: Requirements 2.6, 3.3
 */
public class VehicleDataRecordingProperties {

    /**
     * Property 13: Vehicle Data Recording
     * For any vehicle entry, the system should record license plate, vehicle type, 
     * entry time, and assigned spot; exit time should be recorded upon exit.
     */
    @Property(tries = 100)
    void vehicleEntryRecordsAllRequiredData(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType) {
        
        // Create parking lot with all spot types
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes();
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        // Find an available spot for the vehicle type
        List<ParkingSpot> availableSpots = controller.findAvailableSpots(vehicleType);
        if (availableSpots.isEmpty()) {
            return; // Skip if no compatible spots
        }
        
        String spotId = availableSpots.get(0).getSpotId();
        boolean isHandicapped = vehicleType == VehicleType.HANDICAPPED;
        
        // Process entry
        VehicleEntryController.EntryResult result = controller.processEntry(
            licensePlate, vehicleType, isHandicapped, spotId);
        
        Vehicle recordedVehicle = result.getVehicle();
        
        // Verify license plate is recorded
        Assertions.assertNotNull(recordedVehicle.getLicensePlate(),
            "License plate should be recorded");
        Assertions.assertEquals(licensePlate.trim().toUpperCase(), 
            recordedVehicle.getLicensePlate(),
            "License plate should match input (normalized to uppercase)");
        
        // Verify vehicle type is recorded
        Assertions.assertNotNull(recordedVehicle.getType(),
            "Vehicle type should be recorded");
        Assertions.assertEquals(vehicleType, recordedVehicle.getType(),
            "Vehicle type should match input");
        
        // Verify entry time is recorded
        Assertions.assertNotNull(recordedVehicle.getEntryTime(),
            "Entry time should be recorded");
        
        // Verify assigned spot is recorded
        Assertions.assertNotNull(result.getSpot(),
            "Assigned spot should be recorded");
        Assertions.assertEquals(spotId, result.getSpot().getSpotId(),
            "Assigned spot ID should match selected spot");
    }

    /**
     * Property: License plate is normalized to uppercase.
     */
    @Property(tries = 100)
    void licensePlateIsNormalizedToUppercase(
            @ForAll("mixedCaseLicensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType) {
        
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes();
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        List<ParkingSpot> availableSpots = controller.findAvailableSpots(vehicleType);
        if (availableSpots.isEmpty()) {
            return;
        }
        
        String spotId = availableSpots.get(0).getSpotId();
        boolean isHandicapped = vehicleType == VehicleType.HANDICAPPED;
        
        VehicleEntryController.EntryResult result = controller.processEntry(
            licensePlate, vehicleType, isHandicapped, spotId);
        
        // Verify license plate is uppercase
        String recordedPlate = result.getVehicle().getLicensePlate();
        Assertions.assertEquals(recordedPlate.toUpperCase(), recordedPlate,
            "License plate should be stored in uppercase");
    }

    /**
     * Property: Handicapped status is correctly recorded.
     */
    @Property(tries = 100)
    void handicappedStatusIsCorrectlyRecorded(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll boolean isHandicapped) {
        
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes();
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        // For handicapped vehicle type, always set isHandicapped to true
        boolean effectiveHandicapped = vehicleType == VehicleType.HANDICAPPED || isHandicapped;
        
        List<ParkingSpot> availableSpots = controller.findAvailableSpots(vehicleType);
        if (availableSpots.isEmpty()) {
            return;
        }
        
        String spotId = availableSpots.get(0).getSpotId();
        
        VehicleEntryController.EntryResult result = controller.processEntry(
            licensePlate, vehicleType, effectiveHandicapped, spotId);
        
        // Verify handicapped status is recorded
        Assertions.assertEquals(effectiveHandicapped, result.getVehicle().isHandicapped(),
            "Handicapped status should be correctly recorded");
    }

    /**
     * Property: Entry time is recorded at the time of entry.
     */
    @Property(tries = 100)
    void entryTimeIsRecordedAtTimeOfEntry(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType) {
        
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes();
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        List<ParkingSpot> availableSpots = controller.findAvailableSpots(vehicleType);
        if (availableSpots.isEmpty()) {
            return;
        }
        
        String spotId = availableSpots.get(0).getSpotId();
        boolean isHandicapped = vehicleType == VehicleType.HANDICAPPED;
        
        // Record time before entry
        java.time.LocalDateTime beforeEntry = java.time.LocalDateTime.now();
        
        VehicleEntryController.EntryResult result = controller.processEntry(
            licensePlate, vehicleType, isHandicapped, spotId);
        
        // Record time after entry
        java.time.LocalDateTime afterEntry = java.time.LocalDateTime.now();
        
        java.time.LocalDateTime entryTime = result.getVehicle().getEntryTime();
        
        // Verify entry time is within the expected range
        Assertions.assertFalse(entryTime.isBefore(beforeEntry),
            "Entry time should not be before the operation started");
        Assertions.assertFalse(entryTime.isAfter(afterEntry),
            "Entry time should not be after the operation completed");
    }

    /**
     * Property: Exit time is null upon entry (not yet exited).
     */
    @Property(tries = 100)
    void exitTimeIsNullUponEntry(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType) {
        
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes();
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        List<ParkingSpot> availableSpots = controller.findAvailableSpots(vehicleType);
        if (availableSpots.isEmpty()) {
            return;
        }
        
        String spotId = availableSpots.get(0).getSpotId();
        boolean isHandicapped = vehicleType == VehicleType.HANDICAPPED;
        
        VehicleEntryController.EntryResult result = controller.processEntry(
            licensePlate, vehicleType, isHandicapped, spotId);
        
        // Verify exit time is null (vehicle hasn't exited yet)
        Assertions.assertNull(result.getVehicle().getExitTime(),
            "Exit time should be null upon entry");
    }

    /**
     * Property: Session contains correct spot assignment.
     */
    @Property(tries = 100)
    void sessionContainsCorrectSpotAssignment(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType) {
        
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes();
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        List<ParkingSpot> availableSpots = controller.findAvailableSpots(vehicleType);
        if (availableSpots.isEmpty()) {
            return;
        }
        
        String spotId = availableSpots.get(0).getSpotId();
        boolean isHandicapped = vehicleType == VehicleType.HANDICAPPED;
        
        VehicleEntryController.EntryResult result = controller.processEntry(
            licensePlate, vehicleType, isHandicapped, spotId);
        
        // Verify session contains correct spot ID
        Assertions.assertEquals(spotId, result.getSession().getSpotId(),
            "Session should contain correct spot ID");
        
        // Verify session entry time matches vehicle entry time
        Assertions.assertEquals(result.getVehicle().getEntryTime(), 
            result.getSession().getEntryTime(),
            "Session entry time should match vehicle entry time");
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
    Arbitrary<String> mixedCaseLicensePlates() {
        Arbitrary<String> letters = Arbitraries.strings()
            .withCharRange('a', 'z')
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
