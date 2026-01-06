package com.university.parking.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic unit test for spot identifier generation to verify the implementation works.
 * This serves as a fallback when property-based testing framework is not available.
 */
public class SpotIdentifierTest {

    public static void main(String[] args) {
        SpotIdentifierTest test = new SpotIdentifierTest();
        
        System.out.println("Running Spot Identifier Tests...");
        
        try {
            test.testUniqueSpotIdentifierGeneration();
            test.testMultiFloorUniqueSpotIdentifiers();
            test.testSpotIdFormat();
            
            System.out.println("All tests passed!");
        } catch (AssertionError e) {
            System.err.println("Test failed: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Test error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Test unique spot identifier generation for a single floor.
     */
    public void testUniqueSpotIdentifierGeneration() {
        System.out.println("Testing unique spot identifier generation...");
        
        // Test case 1: Single floor with multiple rows
        ParkingLot parkingLot = new ParkingLot("Test Parking Lot");
        
        List<ParkingLot.RowConfiguration> rowConfigs = new ArrayList<>();
        // Create 3 rows with 5 spots each
        for (int i = 0; i < 3; i++) {
            SpotType[] spotTypes = new SpotType[5];
            for (int j = 0; j < 5; j++) {
                spotTypes[j] = SpotType.REGULAR;
            }
            rowConfigs.add(new ParkingLot.RowConfiguration(5, spotTypes));
        }
        
        Floor floor = parkingLot.createFloor(1, rowConfigs);
        
        // Verify all spot IDs are unique
        assertTrue(parkingLot.validateUniqueSpotIds(), "All spot IDs should be unique");
        
        // Verify all spot IDs follow the correct format
        assertTrue(parkingLot.validateSpotIdFormat(), "All spot IDs should follow correct format");
        
        // Verify the expected number of spots were created
        assertEquals(15, floor.getAllSpots().size(), "Should have 15 spots total");
        
        // Verify each spot ID contains the correct floor number
        for (ParkingSpot spot : floor.getAllSpots()) {
            String spotId = spot.getSpotId();
            assertTrue(spotId.startsWith("F1-"), "Spot ID should start with F1-: " + spotId);
        }
        
        System.out.println("✓ Single floor test passed");
    }

    /**
     * Test unique spot identifiers across multiple floors.
     */
    public void testMultiFloorUniqueSpotIdentifiers() {
        System.out.println("Testing multi-floor unique spot identifiers...");
        
        ParkingLot parkingLot = new ParkingLot("Multi-Floor Test Lot");
        
        // Create 3 floors with 2 rows each, 4 spots per row
        for (int floorNum = 1; floorNum <= 3; floorNum++) {
            List<ParkingLot.RowConfiguration> rowConfigs = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                SpotType[] spotTypes = new SpotType[4];
                for (int j = 0; j < 4; j++) {
                    spotTypes[j] = SpotType.COMPACT;
                }
                rowConfigs.add(new ParkingLot.RowConfiguration(4, spotTypes));
            }
            parkingLot.createFloor(floorNum, rowConfigs);
        }
        
        // Verify all spot IDs are unique across all floors
        assertTrue(parkingLot.validateUniqueSpotIds(), "All spot IDs should be unique across floors");
        
        // Verify all spot IDs follow the correct format
        assertTrue(parkingLot.validateSpotIdFormat(), "All spot IDs should follow correct format");
        
        // Verify total number of spots (3 floors × 2 rows × 4 spots = 24)
        assertEquals(24, parkingLot.getAllSpots().size(), "Should have 24 spots total");
        
        System.out.println("✓ Multi-floor test passed");
    }

    /**
     * Test specific spot ID format requirements.
     */
    public void testSpotIdFormat() {
        System.out.println("Testing spot ID format...");
        
        // Test the static method directly
        String spotId1 = Floor.generateSpotId(1, 1, 1);
        assertEquals("F1-R1-S1", spotId1, "Spot ID format should be F1-R1-S1");
        
        String spotId2 = Floor.generateSpotId(5, 10, 25);
        assertEquals("F5-R10-S25", spotId2, "Spot ID format should be F5-R10-S25");
        
        // Test format validation
        assertTrue("F1-R1-S1".matches("F\\d+-R\\d+-S\\d+"), "Should match format pattern");
        assertTrue("F10-R5-S20".matches("F\\d+-R\\d+-S\\d+"), "Should match format pattern");
        assertFalse("F1R1S1".matches("F\\d+-R\\d+-S\\d+"), "Should not match without dashes");
        assertFalse("1-1-1".matches("F\\d+-R\\d+-S\\d+"), "Should not match without F-R-S prefix");
        
        System.out.println("✓ Spot ID format test passed");
    }

    // Simple assertion methods
    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    private void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }
}