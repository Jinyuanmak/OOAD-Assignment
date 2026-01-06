package com.university.parking.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for spot identifier generation.
 * Feature: parking-lot-management, Property 1: Unique Spot Identifier Generation
 * Validates: Requirements 1.2, 1.4
 */
public class SpotIdentifierProperties {

    /**
     * Property 1: Unique Spot Identifier Generation
     * For any floor with rows and spots, all generated spot IDs should be unique 
     * and follow the format "F{floor}-R{row}-S{spot}"
     */
    @Property(tries = 100)
    void uniqueSpotIdentifierGeneration(
            @ForAll @IntRange(min = 1, max = 10) int floorNumber,
            @ForAll @IntRange(min = 1, max = 5) int numberOfRows,
            @ForAll @IntRange(min = 1, max = 10) int spotsPerRow) {
        
        // Create a parking lot
        ParkingLot parkingLot = new ParkingLot("Test Parking Lot");
        
        // Create row configurations
        List<ParkingLot.RowConfiguration> rowConfigs = new ArrayList<>();
        for (int i = 0; i < numberOfRows; i++) {
            SpotType[] spotTypes = new SpotType[spotsPerRow];
            for (int j = 0; j < spotsPerRow; j++) {
                spotTypes[j] = SpotType.REGULAR; // Use regular spots for simplicity
            }
            rowConfigs.add(new ParkingLot.RowConfiguration(spotsPerRow, spotTypes));
        }
        
        // Create the floor
        Floor floor = parkingLot.createFloor(floorNumber, rowConfigs);
        
        // Verify all spot IDs are unique
        boolean uniqueIds = parkingLot.validateUniqueSpotIds();
        Assertions.assertTrue(uniqueIds, "All spot IDs should be unique");
        
        // Verify all spot IDs follow the correct format
        boolean correctFormat = parkingLot.validateSpotIdFormat();
        Assertions.assertTrue(correctFormat, "All spot IDs should follow correct format");
        
        // Verify the expected number of spots were created
        int expectedSpots = numberOfRows * spotsPerRow;
        int actualSpots = floor.getAllSpots().size();
        Assertions.assertEquals(expectedSpots, actualSpots, "Expected number of spots should match actual");
        
        // Verify each spot ID contains the correct floor number
        for (ParkingSpot spot : floor.getAllSpots()) {
            String spotId = spot.getSpotId();
            Assertions.assertTrue(spotId.startsWith("F" + floorNumber + "-"), 
                "Spot ID should start with correct floor number: " + spotId);
        }
    }

    /**
     * Property test for spot ID format validation across multiple floors.
     */
    @Property(tries = 100)
    void multiFloorUniqueSpotIdentifiers(
            @ForAll @IntRange(min = 2, max = 5) int numberOfFloors,
            @ForAll @IntRange(min = 1, max = 3) int rowsPerFloor,
            @ForAll @IntRange(min = 1, max = 5) int spotsPerRow) {
        
        ParkingLot parkingLot = new ParkingLot("Multi-Floor Test Lot");
        
        // Create multiple floors
        for (int floorNum = 1; floorNum <= numberOfFloors; floorNum++) {
            List<ParkingLot.RowConfiguration> rowConfigs = new ArrayList<>();
            for (int i = 0; i < rowsPerFloor; i++) {
                SpotType[] spotTypes = new SpotType[spotsPerRow];
                for (int j = 0; j < spotsPerRow; j++) {
                    spotTypes[j] = SpotType.COMPACT; // Use compact spots for variety
                }
                rowConfigs.add(new ParkingLot.RowConfiguration(spotsPerRow, spotTypes));
            }
            parkingLot.createFloor(floorNum, rowConfigs);
        }
        
        // Verify all spot IDs are unique across all floors
        boolean uniqueIds = parkingLot.validateUniqueSpotIds();
        Assertions.assertTrue(uniqueIds, "All spot IDs should be unique across floors");
        
        // Verify all spot IDs follow the correct format
        boolean correctFormat = parkingLot.validateSpotIdFormat();
        Assertions.assertTrue(correctFormat, "All spot IDs should follow correct format");
        
        // Verify total number of spots
        int expectedTotalSpots = numberOfFloors * rowsPerFloor * spotsPerRow;
        int actualTotalSpots = parkingLot.getAllSpots().size();
        Assertions.assertEquals(expectedTotalSpots, actualTotalSpots, "Total spots should match expected count");
    }
}