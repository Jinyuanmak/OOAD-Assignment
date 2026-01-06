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
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for available spot filtering.
 * Feature: parking-lot-management, Property 14: Available Spot Filtering
 * Validates: Requirements 3.1
 */
public class AvailableSpotFilteringProperties {

    /**
     * Property 14: Available Spot Filtering
     * For any vehicle entry request, the displayed available spots should only 
     * include spots that are available and compatible with the vehicle type.
     */
    @Property(tries = 100)
    void availableSpotFilteringReturnsOnlyCompatibleAndAvailableSpots(
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll @IntRange(min = 1, max = 3) int numberOfFloors,
            @ForAll @IntRange(min = 1, max = 3) int rowsPerFloor,
            @ForAll @IntRange(min = 1, max = 5) int spotsPerRow) {
        
        // Create a parking lot with mixed spot types
        ParkingLot parkingLot = createMixedParkingLot(numberOfFloors, rowsPerFloor, spotsPerRow);
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        // Get available spots for the vehicle type
        List<ParkingSpot> availableSpots = controller.findAvailableSpots(vehicleType);
        
        // Create a test vehicle to check compatibility
        Vehicle testVehicle = new Vehicle("TEST123", vehicleType, vehicleType == VehicleType.HANDICAPPED);
        
        // Verify all returned spots are available
        for (ParkingSpot spot : availableSpots) {
            Assertions.assertTrue(spot.isAvailable(), 
                "All returned spots should be available: " + spot.getSpotId());
        }
        
        // Verify all returned spots are compatible with the vehicle type
        for (ParkingSpot spot : availableSpots) {
            Assertions.assertTrue(testVehicle.canParkInSpot(spot.getType()),
                "All returned spots should be compatible with vehicle type " + vehicleType + 
                ": spot " + spot.getSpotId() + " has type " + spot.getType());
        }
    }

    /**
     * Property: No incompatible spots should be returned.
     */
    @Property(tries = 100)
    void availableSpotFilteringExcludesIncompatibleSpots(
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll @IntRange(min = 1, max = 3) int numberOfFloors) {
        
        // Create a parking lot with all spot types
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes(numberOfFloors);
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        // Get available spots for the vehicle type
        List<ParkingSpot> availableSpots = controller.findAvailableSpots(vehicleType);
        
        // Create a test vehicle to check compatibility
        Vehicle testVehicle = new Vehicle("TEST123", vehicleType, vehicleType == VehicleType.HANDICAPPED);
        
        // Verify no incompatible spots are returned
        for (ParkingSpot spot : availableSpots) {
            boolean isCompatible = testVehicle.canParkInSpot(spot.getType());
            Assertions.assertTrue(isCompatible,
                "Incompatible spot should not be returned: vehicle type " + vehicleType + 
                " cannot park in spot type " + spot.getType());
        }
    }

    /**
     * Property: Occupied spots should not be returned.
     */
    @Property(tries = 100)
    void availableSpotFilteringExcludesOccupiedSpots(
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll @IntRange(min = 1, max = 3) int numberOfFloors,
            @ForAll @IntRange(min = 0, max = 5) int spotsToOccupy) {
        
        // Create a parking lot
        ParkingLot parkingLot = createMixedParkingLot(numberOfFloors, 2, 4);
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        // Occupy some spots
        List<ParkingSpot> allSpots = parkingLot.getAllSpots();
        int occupiedCount = Math.min(spotsToOccupy, allSpots.size());
        for (int i = 0; i < occupiedCount; i++) {
            Vehicle occupyingVehicle = new Vehicle("OCC" + i, VehicleType.CAR, false);
            allSpots.get(i).occupySpot(occupyingVehicle);
        }
        
        // Get available spots
        List<ParkingSpot> availableSpots = controller.findAvailableSpots(vehicleType);
        
        // Verify no occupied spots are returned
        for (ParkingSpot spot : availableSpots) {
            Assertions.assertTrue(spot.isAvailable(),
                "Occupied spot should not be returned: " + spot.getSpotId());
        }
    }

    /**
     * Property: All compatible and available spots should be returned.
     */
    @Property(tries = 100)
    void availableSpotFilteringReturnsAllCompatibleAvailableSpots(
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll @IntRange(min = 1, max = 2) int numberOfFloors) {
        
        // Create a parking lot with all spot types
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes(numberOfFloors);
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        // Create a test vehicle
        Vehicle testVehicle = new Vehicle("TEST123", vehicleType, vehicleType == VehicleType.HANDICAPPED);
        
        // Count expected compatible and available spots
        int expectedCount = 0;
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            if (spot.isAvailable() && testVehicle.canParkInSpot(spot.getType())) {
                expectedCount++;
            }
        }
        
        // Get available spots
        List<ParkingSpot> availableSpots = controller.findAvailableSpots(vehicleType);
        
        // Verify count matches
        Assertions.assertEquals(expectedCount, availableSpots.size(),
            "Should return all compatible and available spots for vehicle type " + vehicleType);
    }

    @Provide
    Arbitrary<VehicleType> vehicleTypes() {
        return Arbitraries.of(VehicleType.values());
    }

    /**
     * Creates a parking lot with mixed spot types.
     */
    private ParkingLot createMixedParkingLot(int floors, int rowsPerFloor, int spotsPerRow) {
        ParkingLot parkingLot = new ParkingLot("Test Parking Lot");
        SpotType[] spotTypes = SpotType.values();
        
        for (int f = 1; f <= floors; f++) {
            List<ParkingLot.RowConfiguration> rowConfigs = new ArrayList<>();
            for (int r = 0; r < rowsPerFloor; r++) {
                SpotType[] rowSpotTypes = new SpotType[spotsPerRow];
                for (int s = 0; s < spotsPerRow; s++) {
                    // Cycle through spot types
                    rowSpotTypes[s] = spotTypes[(r + s) % spotTypes.length];
                }
                rowConfigs.add(new ParkingLot.RowConfiguration(spotsPerRow, rowSpotTypes));
            }
            parkingLot.createFloor(f, rowConfigs);
        }
        
        return parkingLot;
    }

    /**
     * Creates a parking lot with all spot types represented.
     */
    private ParkingLot createParkingLotWithAllSpotTypes(int floors) {
        ParkingLot parkingLot = new ParkingLot("Test Parking Lot");
        SpotType[] spotTypes = SpotType.values();
        
        for (int f = 1; f <= floors; f++) {
            List<ParkingLot.RowConfiguration> rowConfigs = new ArrayList<>();
            // Create one row with all spot types
            SpotType[] rowSpotTypes = new SpotType[spotTypes.length];
            System.arraycopy(spotTypes, 0, rowSpotTypes, 0, spotTypes.length);
            rowConfigs.add(new ParkingLot.RowConfiguration(spotTypes.length, rowSpotTypes));
            parkingLot.createFloor(f, rowConfigs);
        }
        
        return parkingLot;
    }
}
