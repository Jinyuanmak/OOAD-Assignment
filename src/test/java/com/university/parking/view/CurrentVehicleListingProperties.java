package com.university.parking.view;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.TableModel;

import com.university.parking.model.Floor;
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
 * Property-based tests for Current Vehicle Listing.
 * 
 * Feature: parking-lot-management, Property 22: Current Vehicle Listing
 * Validates: Requirements 8.4
 */
public class CurrentVehicleListingProperties {

    @Property(tries = 100)
    void allParkedVehiclesAppearInTable(
            @ForAll("parkingLotWithSpots") ParkingLot parkingLot,
            @ForAll @IntRange(min = 0, max = 100) int percentToOccupy) {
        
        // Get all spots and occupy some of them
        List<ParkingSpot> allSpots = parkingLot.getAllSpots();
        if (allSpots.isEmpty()) return;
        
        Set<String> parkedLicensePlates = new HashSet<>();
        int spotsToOccupy = (allSpots.size() * percentToOccupy) / 100;
        
        for (int i = 0; i < spotsToOccupy && i < allSpots.size(); i++) {
            ParkingSpot spot = allSpots.get(i);
            if (spot.isAvailable()) {
                String licensePlate = "TEST" + i;
                Vehicle vehicle = new Vehicle(licensePlate, VehicleType.CAR, false);
                vehicle.setEntryTime(java.time.LocalDateTime.now()); // Set entry time
                spot.occupySpot(vehicle);
                parkedLicensePlates.add(licensePlate);
            }
        }
        
        // Create admin panel and check vehicle table
        AdminPanel adminPanel = new AdminPanel(parkingLot);
        TableModel vehicleTable = adminPanel.getVehicleTable().getModel();
        
        // Collect license plates from table
        Set<String> tableLicensePlates = new HashSet<>();
        for (int row = 0; row < vehicleTable.getRowCount(); row++) {
            String licensePlate = (String) vehicleTable.getValueAt(row, 0);
            tableLicensePlates.add(licensePlate);
        }
        
        // All parked vehicles should appear in table
        assert tableLicensePlates.equals(parkedLicensePlates) : 
            String.format("Table should contain all parked vehicles. Expected: %s, Got: %s", 
                parkedLicensePlates, tableLicensePlates);
    }

    @Property(tries = 100)
    void vehicleTableContainsCompleteDetails(
            @ForAll("parkingLotWithSpots") ParkingLot parkingLot) {
        
        // Park at least one vehicle
        List<ParkingSpot> allSpots = parkingLot.getAllSpots();
        if (allSpots.isEmpty()) return;
        
        ParkingSpot spot = allSpots.get(0);
        if (spot.isAvailable()) {
            Vehicle vehicle = new Vehicle("ABC123", VehicleType.CAR, false);
            vehicle.setEntryTime(java.time.LocalDateTime.now()); // Set entry time
            spot.occupySpot(vehicle);
        }
        
        // Create admin panel and check vehicle table
        AdminPanel adminPanel = new AdminPanel(parkingLot);
        TableModel vehicleTable = adminPanel.getVehicleTable().getModel();
        
        if (vehicleTable.getRowCount() > 0) {
            // Check that table has required columns: License Plate, Type, Spot, Entry Time
            assert vehicleTable.getColumnCount() >= 4 : 
                "Vehicle table should have at least 4 columns";
            
            // Check first row has non-null values
            for (int col = 0; col < 4; col++) {
                Object value = vehicleTable.getValueAt(0, col);
                assert value != null : 
                    String.format("Column %d should not be null", col);
            }
        }
    }

    @Property(tries = 100)
    void emptyParkingLotShowsNoVehicles(
            @ForAll("parkingLotWithSpots") ParkingLot parkingLot) {
        
        // Ensure all spots are empty
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getAllSpots()) {
                if (!spot.isAvailable()) {
                    spot.vacateSpot();
                }
            }
        }
        
        // Create admin panel and check vehicle table
        AdminPanel adminPanel = new AdminPanel(parkingLot);
        TableModel vehicleTable = adminPanel.getVehicleTable().getModel();
        
        assert vehicleTable.getRowCount() == 0 : 
            "Vehicle table should be empty when no vehicles are parked";
    }

    @Property(tries = 100)
    void vehicleCountMatchesOccupiedSpots(
            @ForAll("parkingLotWithSpots") ParkingLot parkingLot,
            @ForAll @IntRange(min = 0, max = 100) int percentToOccupy) {
        
        // Get all spots and occupy some of them
        List<ParkingSpot> allSpots = parkingLot.getAllSpots();
        if (allSpots.isEmpty()) return;
        
        int spotsToOccupy = (allSpots.size() * percentToOccupy) / 100;
        int actuallyOccupied = 0;
        
        for (int i = 0; i < spotsToOccupy && i < allSpots.size(); i++) {
            ParkingSpot spot = allSpots.get(i);
            if (spot.isAvailable()) {
                Vehicle vehicle = new Vehicle("TEST" + i, VehicleType.CAR, false);
                vehicle.setEntryTime(java.time.LocalDateTime.now()); // Set entry time
                spot.occupySpot(vehicle);
                actuallyOccupied++;
            }
        }
        
        // Create admin panel and check vehicle table
        AdminPanel adminPanel = new AdminPanel(parkingLot);
        TableModel vehicleTable = adminPanel.getVehicleTable().getModel();
        
        assert vehicleTable.getRowCount() == actuallyOccupied : 
            String.format("Vehicle table should have %d rows but has %d", 
                actuallyOccupied, vehicleTable.getRowCount());
    }

    @Provide
    Arbitrary<ParkingLot> parkingLotWithSpots() {
        return Arbitraries.integers().between(1, 3).map(numFloors -> {
            // Create a fresh parking lot for each test
            ParkingLot lot = new ParkingLot("Test Lot " + System.nanoTime());
            
            for (int f = 1; f <= numFloors; f++) {
                Floor floor = new Floor(f);
                
                // Create 1-2 rows per floor
                int numRows = 1 + (f % 2);
                for (int r = 1; r <= numRows; r++) {
                    // Create 2-4 spots per row
                    int numSpots = 2 + (r % 3);
                    SpotType[] types = new SpotType[numSpots];
                    for (int s = 0; s < numSpots; s++) {
                        types[s] = SpotType.values()[s % SpotType.values().length];
                    }
                    floor.createRow(r, numSpots, types);
                }
                
                lot.addFloor(floor);
            }
            
            return lot;
        });
    }
}
