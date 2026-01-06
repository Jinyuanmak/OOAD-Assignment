package com.university.parking.model;

import org.junit.jupiter.api.Assertions;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for spot state consistency.
 * Feature: parking-lot-management, Property 3: Spot State Consistency
 * Validates: Requirements 1.5, 1.6, 3.2, 4.7
 */
public class SpotStateConsistencyProperties {

    /**
     * Property 3: Spot State Consistency
     * For any parking spot, when a vehicle is assigned to it, the spot status should be occupied 
     * and the current vehicle should be recorded; when vacated, the spot should be available 
     * with no current vehicle
     */
    @Property(tries = 100)
    void spotStateConsistency(
            @ForAll @IntRange(min = 1, max = 10) int floorNumber,
            @ForAll @IntRange(min = 1, max = 10) int rowNumber,
            @ForAll @IntRange(min = 1, max = 10) int spotNumber,
            @ForAll SpotType spotType,
            @ForAll VehicleType vehicleType) {
        
        // Generate a unique spot ID
        String spotId = Floor.generateSpotId(floorNumber, rowNumber, spotNumber);
        
        // Create a parking spot
        ParkingSpot spot = new ParkingSpot(spotId, spotType);
        
        // Create a vehicle
        String licensePlate = "TEST" + floorNumber + rowNumber + spotNumber;
        Vehicle vehicle = new Vehicle(licensePlate, vehicleType, vehicleType == VehicleType.HANDICAPPED);
        
        // Initially, the spot should be available with no current vehicle
        Assertions.assertTrue(spot.isAvailable(), "Spot should initially be available");
        Assertions.assertEquals(SpotStatus.AVAILABLE, spot.getStatus(), "Spot status should be AVAILABLE");
        Assertions.assertNull(spot.getCurrentVehicle(), "Current vehicle should be null initially");
        
        // Occupy the spot with the vehicle
        spot.occupySpot(vehicle);
        
        // After occupation, spot should be occupied and have the current vehicle recorded
        Assertions.assertFalse(spot.isAvailable(), "Spot should not be available after occupation");
        Assertions.assertEquals(SpotStatus.OCCUPIED, spot.getStatus(), "Spot status should be OCCUPIED");
        Assertions.assertNotNull(spot.getCurrentVehicle(), "Current vehicle should not be null after occupation");
        Assertions.assertEquals(vehicle, spot.getCurrentVehicle(), "Current vehicle should match the assigned vehicle");
        Assertions.assertEquals(licensePlate, spot.getCurrentVehicle().getLicensePlate(), 
            "License plate should match the assigned vehicle");
        
        // Vacate the spot
        spot.vacateSpot();
        
        // After vacation, spot should be available with no current vehicle
        Assertions.assertTrue(spot.isAvailable(), "Spot should be available after vacation");
        Assertions.assertEquals(SpotStatus.AVAILABLE, spot.getStatus(), "Spot status should be AVAILABLE after vacation");
        Assertions.assertNull(spot.getCurrentVehicle(), "Current vehicle should be null after vacation");
    }

    /**
     * Property test for multiple occupy/vacate cycles to ensure consistency is maintained.
     */
    @Property(tries = 100)
    void multipleOccupyVacateCycles(
            @ForAll @IntRange(min = 1, max = 5) int cycles,
            @ForAll SpotType spotType) {
        
        // Create a parking spot
        String spotId = Floor.generateSpotId(1, 1, 1);
        ParkingSpot spot = new ParkingSpot(spotId, spotType);
        
        for (int i = 0; i < cycles; i++) {
            // Create a different vehicle for each cycle
            String licensePlate = "CYCLE" + i;
            Vehicle vehicle = new Vehicle(licensePlate, VehicleType.CAR, false);
            
            // Initially available
            Assertions.assertTrue(spot.isAvailable(), "Spot should be available at start of cycle " + i);
            Assertions.assertNull(spot.getCurrentVehicle(), "No current vehicle at start of cycle " + i);
            
            // Occupy
            spot.occupySpot(vehicle);
            Assertions.assertFalse(spot.isAvailable(), "Spot should be occupied in cycle " + i);
            Assertions.assertEquals(vehicle, spot.getCurrentVehicle(), "Vehicle should be assigned in cycle " + i);
            
            // Vacate
            spot.vacateSpot();
            Assertions.assertTrue(spot.isAvailable(), "Spot should be available after vacation in cycle " + i);
            Assertions.assertNull(spot.getCurrentVehicle(), "No current vehicle after vacation in cycle " + i);
        }
    }

    /**
     * Property test to ensure that occupying an already occupied spot doesn't change the state.
     */
    @Property(tries = 100)
    void occupyAlreadyOccupiedSpot(
            @ForAll SpotType spotType,
            @ForAll VehicleType vehicleType1,
            @ForAll VehicleType vehicleType2) {
        
        // Create a parking spot
        String spotId = Floor.generateSpotId(1, 1, 1);
        ParkingSpot spot = new ParkingSpot(spotId, spotType);
        
        // Create two different vehicles
        Vehicle vehicle1 = new Vehicle("FIRST", vehicleType1, vehicleType1 == VehicleType.HANDICAPPED);
        Vehicle vehicle2 = new Vehicle("SECOND", vehicleType2, vehicleType2 == VehicleType.HANDICAPPED);
        
        // Occupy with first vehicle
        spot.occupySpot(vehicle1);
        Assertions.assertEquals(vehicle1, spot.getCurrentVehicle(), "First vehicle should be assigned");
        Assertions.assertFalse(spot.isAvailable(), "Spot should be occupied");
        
        // Try to occupy with second vehicle (should not change state)
        spot.occupySpot(vehicle2);
        Assertions.assertEquals(vehicle1, spot.getCurrentVehicle(), "First vehicle should still be assigned");
        Assertions.assertFalse(spot.isAvailable(), "Spot should still be occupied");
        Assertions.assertEquals(SpotStatus.OCCUPIED, spot.getStatus(), "Status should remain OCCUPIED");
    }
}