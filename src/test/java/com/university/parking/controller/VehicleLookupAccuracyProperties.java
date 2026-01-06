package com.university.parking.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;

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

/**
 * Property-based tests for vehicle lookup accuracy.
 * Feature: parking-lot-management, Property 15: Vehicle Lookup Accuracy
 * Validates: Requirements 4.2
 * 
 * For any license plate entered during exit, the system should retrieve 
 * the correct vehicle record with accurate entry time and spot assignment.
 */
public class VehicleLookupAccuracyProperties {

    /**
     * Property 15: Vehicle Lookup Accuracy
     * For any license plate entered during exit, the system should retrieve 
     * the correct vehicle record with accurate entry time and spot assignment.
     */
    @Property(tries = 100)
    void vehicleLookupReturnsCorrectVehicleRecord(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType) {
        
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
        VehicleEntryController.EntryResult entryResult = entryController.processEntry(
            licensePlate, vehicleType, isHandicapped, spotId);
        
        LocalDateTime originalEntryTime = entryResult.getVehicle().getEntryTime();
        
        // Lookup vehicle by license plate
        VehicleExitController.VehicleLookupResult lookupResult = 
            exitController.lookupVehicle(licensePlate);
        
        // Verify lookup returns a result
        Assertions.assertNotNull(lookupResult,
            "Lookup should find the parked vehicle");
        
        // Verify correct license plate
        Assertions.assertEquals(licensePlate.trim().toUpperCase(), 
            lookupResult.getVehicle().getLicensePlate(),
            "Lookup should return vehicle with correct license plate");
        
        // Verify correct vehicle type
        Assertions.assertEquals(vehicleType, lookupResult.getVehicle().getType(),
            "Lookup should return vehicle with correct type");
        
        // Verify correct entry time
        Assertions.assertEquals(originalEntryTime, lookupResult.getVehicle().getEntryTime(),
            "Lookup should return vehicle with accurate entry time");
        
        // Verify correct spot assignment
        Assertions.assertEquals(spotId, lookupResult.getSpot().getSpotId(),
            "Lookup should return correct spot assignment");
    }

    /**
     * Property: Lookup with non-existent license plate returns null.
     */
    @Property(tries = 100)
    void lookupNonExistentVehicleReturnsNull(
            @ForAll("licensePlates") String licensePlate) {
        
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes();
        VehicleExitController exitController = new VehicleExitController(parkingLot);
        
        // Lookup vehicle that was never parked
        VehicleExitController.VehicleLookupResult lookupResult = 
            exitController.lookupVehicle(licensePlate);
        
        // Verify lookup returns null for non-existent vehicle
        Assertions.assertNull(lookupResult,
            "Lookup should return null for non-existent vehicle");
    }

    /**
     * Property: Lookup is case-insensitive for license plates.
     */
    @Property(tries = 100)
    void lookupIsCaseInsensitive(
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
        
        // Process entry with uppercase license plate
        entryController.processEntry(licensePlate.toUpperCase(), vehicleType, isHandicapped, spotId);
        
        // Lookup with lowercase license plate
        VehicleExitController.VehicleLookupResult lookupResult = 
            exitController.lookupVehicle(licensePlate.toLowerCase());
        
        // Verify lookup finds the vehicle regardless of case
        Assertions.assertNotNull(lookupResult,
            "Lookup should be case-insensitive");
    }

    /**
     * Property: Lookup handles whitespace in license plate.
     */
    @Property(tries = 100)
    void lookupHandlesWhitespace(
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
        
        // Lookup with whitespace around license plate
        VehicleExitController.VehicleLookupResult lookupResult = 
            exitController.lookupVehicle("  " + licensePlate + "  ");
        
        // Verify lookup handles whitespace
        Assertions.assertNotNull(lookupResult,
            "Lookup should handle whitespace in license plate");
    }

    /**
     * Property: Lookup returns correct spot with vehicle information.
     */
    @Property(tries = 100)
    void lookupReturnsSpotWithVehicleInfo(
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
        
        // Lookup vehicle
        VehicleExitController.VehicleLookupResult lookupResult = 
            exitController.lookupVehicle(licensePlate);
        
        // Verify spot contains the vehicle
        Assertions.assertNotNull(lookupResult.getSpot().getCurrentVehicle(),
            "Spot should contain the parked vehicle");
        Assertions.assertEquals(lookupResult.getVehicle(), 
            lookupResult.getSpot().getCurrentVehicle(),
            "Spot's current vehicle should match lookup result");
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
