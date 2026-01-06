package com.university.parking.view;

import java.util.List;

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
 * Property-based tests for Occupancy Rate Calculation.
 * 
 * Feature: parking-lot-management, Property 20: Occupancy Rate Calculation
 * Validates: Requirements 8.2
 */
public class OccupancyRateCalculationProperties {

    @Property(tries = 100)
    void occupancyRateEqualsOccupiedDividedByTotal(
            @ForAll("parkingLotWithSpots") ParkingLot parkingLot,
            @ForAll @IntRange(min = 0, max = 100) int percentToOccupy) {
        
        // Get all spots
        List<ParkingSpot> allSpots = parkingLot.getAllSpots();
        if (allSpots.isEmpty()) return;
        
        // Occupy a percentage of spots
        int spotsToOccupy = (allSpots.size() * percentToOccupy) / 100;
        for (int i = 0; i < spotsToOccupy && i < allSpots.size(); i++) {
            ParkingSpot spot = allSpots.get(i);
            if (spot.isAvailable()) {
                Vehicle vehicle = new Vehicle("TEST" + i, VehicleType.CAR, false);
                spot.occupySpot(vehicle);
            }
        }
        
        // Calculate expected occupancy rate
        int totalSpots = 0;
        int occupiedSpots = 0;
        
        for (Floor floor : parkingLot.getFloors()) {
            totalSpots += floor.getAllSpots().size();
            occupiedSpots += floor.getAllSpots().size() - floor.getAvailableSpots().size();
        }
        
        double expectedRate = totalSpots > 0 ? (occupiedSpots * 100.0 / totalSpots) : 0;
        
        // Create admin panel and check occupancy label
        AdminPanel adminPanel = new AdminPanel(parkingLot);
        String labelText = adminPanel.getOccupancyLabel().getText();
        
        // Extract the rate from the label text
        String rateStr = labelText.replaceAll("[^0-9.]", "");
        double actualRate = Double.parseDouble(rateStr);
        
        // Allow small floating point differences
        assert Math.abs(actualRate - expectedRate) < 0.1 : 
            String.format("Occupancy rate should be %.1f%% but was %.1f%%", expectedRate, actualRate);
    }

    @Property(tries = 100)
    void occupancyRateIsZeroWhenAllSpotsAvailable(
            @ForAll("parkingLotWithSpots") ParkingLot parkingLot) {
        
        // Ensure all spots are available
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getAllSpots()) {
                if (!spot.isAvailable()) {
                    spot.vacateSpot();
                }
            }
        }
        
        AdminPanel adminPanel = new AdminPanel(parkingLot);
        String labelText = adminPanel.getOccupancyLabel().getText();
        String rateStr = labelText.replaceAll("[^0-9.]", "");
        double actualRate = Double.parseDouble(rateStr);
        
        assert actualRate == 0.0 : "Occupancy rate should be 0% when all spots are available";
    }

    @Property(tries = 100)
    void occupancyRateIsHundredWhenAllSpotsOccupied(
            @ForAll("parkingLotWithSpots") ParkingLot parkingLot) {
        
        List<ParkingSpot> allSpots = parkingLot.getAllSpots();
        if (allSpots.isEmpty()) return;
        
        // Occupy all spots
        for (int i = 0; i < allSpots.size(); i++) {
            ParkingSpot spot = allSpots.get(i);
            if (spot.isAvailable()) {
                Vehicle vehicle = new Vehicle("TEST" + i, VehicleType.CAR, false);
                spot.occupySpot(vehicle);
            }
        }
        
        AdminPanel adminPanel = new AdminPanel(parkingLot);
        String labelText = adminPanel.getOccupancyLabel().getText();
        String rateStr = labelText.replaceAll("[^0-9.]", "");
        double actualRate = Double.parseDouble(rateStr);
        
        assert actualRate == 100.0 : "Occupancy rate should be 100% when all spots are occupied";
    }

    @Provide
    Arbitrary<ParkingLot> parkingLotWithSpots() {
        return Arbitraries.integers().between(1, 3).flatMap(numFloors -> {
            ParkingLot lot = new ParkingLot("Test Lot");
            
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
            
            return Arbitraries.just(lot);
        });
    }
}
