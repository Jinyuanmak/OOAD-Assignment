package com.university.parking.view;

import java.util.List;

import com.university.parking.model.Floor;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.SpotType;
import com.university.parking.model.Vehicle;
import com.university.parking.model.VehicleType;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for Status Bar Data Accuracy and Real-Time Updates.
 * 
 * Feature: gui-modernization, Property 10: Status Bar Data Accuracy
 * Feature: gui-modernization, Property 11: Status Bar Real-Time Updates
 * Validates: Requirements 7.2, 7.3, 7.4
 * 
 * For any StatusBarPanel connected to a ParkingLot, the displayed vehicle count 
 * SHALL equal the actual number of parked vehicles, and the displayed occupancy 
 * percentage SHALL equal the calculated occupancy rate (within 0.1% tolerance).
 */
public class StatusBarDataAccuracyProperties {

    /**
     * Property 10: Status Bar Data Accuracy - Vehicle Count
     * 
     * For any StatusBarPanel, the displayed vehicle count SHALL equal 
     * the actual number of parked vehicles.
     */
    @Property(tries = 100)
    void displayedVehicleCountMatchesActual(
            @ForAll @IntRange(min = 1, max = 5) int numFloors,
            @ForAll @IntRange(min = 1, max = 10) int spotsPerFloor,
            @ForAll @IntRange(min = 0, max = 100) int occupancyPercent) {
        
        ParkingLot parkingLot = createTestParkingLot(numFloors, spotsPerFloor);
        int totalSpots = numFloors * spotsPerFloor;
        int spotsToOccupy = Math.min((totalSpots * occupancyPercent) / 100, totalSpots);
        
        occupySpots(parkingLot, spotsToOccupy);
        
        StatusBarPanel statusBar = new StatusBarPanel(parkingLot);
        statusBar.updateStatus();
        
        int displayedCount = statusBar.getDisplayedVehicleCount();
        int actualCount = statusBar.getActualVehicleCount();
        
        assert displayedCount == actualCount : 
            "Displayed vehicle count (" + displayedCount + 
            ") should equal actual count (" + actualCount + ")";
        
        statusBar.stopTimer();
    }
    
    /**
     * Property 10: Status Bar Data Accuracy - Occupancy Rate
     * 
     * For any StatusBarPanel, the displayed occupancy percentage SHALL equal 
     * the calculated occupancy rate (within 0.1% tolerance).
     */
    @Property(tries = 100)
    void displayedOccupancyRateMatchesActual(
            @ForAll @IntRange(min = 1, max = 5) int numFloors,
            @ForAll @IntRange(min = 1, max = 10) int spotsPerFloor,
            @ForAll @IntRange(min = 0, max = 100) int occupancyPercent) {
        
        ParkingLot parkingLot = createTestParkingLot(numFloors, spotsPerFloor);
        int totalSpots = numFloors * spotsPerFloor;
        int spotsToOccupy = Math.min((totalSpots * occupancyPercent) / 100, totalSpots);
        
        occupySpots(parkingLot, spotsToOccupy);
        
        StatusBarPanel statusBar = new StatusBarPanel(parkingLot);
        statusBar.updateStatus();
        
        double displayedRate = statusBar.getDisplayedOccupancyRate();
        double actualRate = statusBar.getActualOccupancyRate();
        
        // Allow 0.1% tolerance due to rounding
        double tolerance = 0.1;
        assert Math.abs(displayedRate - actualRate) <= tolerance : 
            "Displayed occupancy rate (" + displayedRate + 
            "%) should equal actual rate (" + actualRate + "%) within " + tolerance + "% tolerance";
        
        statusBar.stopTimer();
    }
    
    /**
     * Property 11: Status Bar Real-Time Updates
     * 
     * When the underlying ParkingLot state changes (vehicle enters or exits), 
     * calling updateStatus() SHALL reflect the new state in the displayed values.
     */
    @Property(tries = 100)
    void statusBarReflectsStateChangesAfterUpdate(
            @ForAll @IntRange(min = 1, max = 3) int numFloors,
            @ForAll @IntRange(min = 5, max = 10) int spotsPerFloor) {
        
        ParkingLot parkingLot = createTestParkingLot(numFloors, spotsPerFloor);
        StatusBarPanel statusBar = new StatusBarPanel(parkingLot);
        
        // Initial state - no vehicles
        statusBar.updateStatus();
        int initialCount = statusBar.getDisplayedVehicleCount();
        assert initialCount == 0 : "Initial vehicle count should be 0";
        
        // Add some vehicles
        occupySpots(parkingLot, 3);
        statusBar.updateStatus();
        int afterAddCount = statusBar.getDisplayedVehicleCount();
        assert afterAddCount == 3 : 
            "After adding 3 vehicles, count should be 3 but was " + afterAddCount;
        
        // Remove a vehicle
        vacateFirstOccupiedSpot(parkingLot);
        statusBar.updateStatus();
        int afterRemoveCount = statusBar.getDisplayedVehicleCount();
        assert afterRemoveCount == 2 : 
            "After removing 1 vehicle, count should be 2 but was " + afterRemoveCount;
        
        statusBar.stopTimer();
    }
    
    /**
     * Property: Occupancy rate updates correctly when vehicles enter/exit
     */
    @Property(tries = 100)
    void occupancyRateUpdatesOnStateChange(
            @ForAll @IntRange(min = 1, max = 3) int numFloors,
            @ForAll @IntRange(min = 5, max = 10) int spotsPerFloor) {
        
        ParkingLot parkingLot = createTestParkingLot(numFloors, spotsPerFloor);
        StatusBarPanel statusBar = new StatusBarPanel(parkingLot);
        int totalSpots = numFloors * spotsPerFloor;
        
        // Initial state
        statusBar.updateStatus();
        double initialRate = statusBar.getDisplayedOccupancyRate();
        assert initialRate == 0.0 : "Initial occupancy rate should be 0%";
        
        // Add vehicles
        int vehiclesToAdd = totalSpots / 2;
        occupySpots(parkingLot, vehiclesToAdd);
        statusBar.updateStatus();
        
        double expectedRate = (double) vehiclesToAdd / totalSpots * 100.0;
        double actualRate = statusBar.getDisplayedOccupancyRate();
        
        assert Math.abs(actualRate - expectedRate) <= 0.1 : 
            "Occupancy rate should be approximately " + expectedRate + 
            "% but was " + actualRate + "%";
        
        statusBar.stopTimer();
    }
    
    /**
     * Property: Status bar shows connected when parking lot is valid
     */
    @Property(tries = 100)
    void statusBarShowsConnectedWithValidParkingLot() {
        ParkingLot parkingLot = createTestParkingLot(2, 5);
        StatusBarPanel statusBar = new StatusBarPanel(parkingLot);
        statusBar.updateStatus();
        
        assert statusBar.isConnected() : 
            "Status bar should show connected with valid parking lot";
        
        statusBar.stopTimer();
    }
    
    /**
     * Property: Status bar shows disconnected when parking lot is null
     */
    @Property(tries = 100)
    void statusBarShowsDisconnectedWithNullParkingLot() {
        StatusBarPanel statusBar = new StatusBarPanel(null);
        statusBar.updateStatus();
        
        assert !statusBar.isConnected() : 
            "Status bar should show disconnected with null parking lot";
        
        statusBar.stopTimer();
    }
    
    /**
     * Property: Status bar has correct preferred height
     */
    @Property(tries = 100)
    void statusBarHasCorrectPreferredHeight() {
        ParkingLot parkingLot = createTestParkingLot(1, 5);
        StatusBarPanel statusBar = new StatusBarPanel(parkingLot);
        
        int preferredHeight = statusBar.getPreferredSize().height;
        int expectedHeight = ThemeManager.STATUS_BAR_HEIGHT;
        
        assert preferredHeight == expectedHeight : 
            "Status bar preferred height should be " + expectedHeight + 
            " but was " + preferredHeight;
        
        statusBar.stopTimer();
    }
    
    /**
     * Property: Empty parking lot shows 0% occupancy
     */
    @Property(tries = 100)
    void emptyParkingLotShowsZeroOccupancy(
            @ForAll @IntRange(min = 1, max = 5) int numFloors,
            @ForAll @IntRange(min = 1, max = 10) int spotsPerFloor) {
        
        ParkingLot parkingLot = createTestParkingLot(numFloors, spotsPerFloor);
        StatusBarPanel statusBar = new StatusBarPanel(parkingLot);
        statusBar.updateStatus();
        
        double occupancyRate = statusBar.getDisplayedOccupancyRate();
        
        assert occupancyRate == 0.0 : 
            "Empty parking lot should show 0% occupancy but showed " + occupancyRate + "%";
        
        statusBar.stopTimer();
    }
    
    /**
     * Property: Full parking lot shows 100% occupancy
     */
    @Property(tries = 100)
    void fullParkingLotShowsFullOccupancy(
            @ForAll @IntRange(min = 1, max = 3) int numFloors,
            @ForAll @IntRange(min = 1, max = 5) int spotsPerFloor) {
        
        ParkingLot parkingLot = createTestParkingLot(numFloors, spotsPerFloor);
        int totalSpots = numFloors * spotsPerFloor;
        
        // Occupy all spots
        occupySpots(parkingLot, totalSpots);
        
        StatusBarPanel statusBar = new StatusBarPanel(parkingLot);
        statusBar.updateStatus();
        
        double occupancyRate = statusBar.getDisplayedOccupancyRate();
        
        assert occupancyRate == 100.0 : 
            "Full parking lot should show 100% occupancy but showed " + occupancyRate + "%";
        
        statusBar.stopTimer();
    }
    
    // Helper methods
    
    private ParkingLot createTestParkingLot(int numFloors, int spotsPerFloor) {
        ParkingLot parkingLot = new ParkingLot("Test Parking Lot");
        
        for (int f = 1; f <= numFloors; f++) {
            Floor floor = new Floor(f);
            SpotType[] spotTypes = new SpotType[spotsPerFloor];
            for (int i = 0; i < spotsPerFloor; i++) {
                spotTypes[i] = SpotType.REGULAR;
            }
            floor.createRow(1, spotsPerFloor, spotTypes);
            parkingLot.addFloor(floor);
        }
        
        return parkingLot;
    }
    
    private void occupySpots(ParkingLot parkingLot, int count) {
        List<ParkingSpot> allSpots = parkingLot.getAllSpots();
        int occupied = 0;
        
        for (ParkingSpot spot : allSpots) {
            if (occupied >= count) {
                break;
            }
            if (spot.isAvailable()) {
                Vehicle vehicle = new Vehicle("TEST-" + occupied, VehicleType.CAR, false);
                spot.occupySpot(vehicle);
                occupied++;
            }
        }
    }
    
    private void vacateFirstOccupiedSpot(ParkingLot parkingLot) {
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            if (!spot.isAvailable()) {
                spot.vacateSpot();
                return;
            }
        }
    }
}
