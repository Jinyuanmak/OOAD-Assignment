package com.university.parking.view;

import com.university.parking.model.Floor;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.SpotType;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.DoubleRange;

/**
 * Property-based tests for Revenue Tracking Accuracy.
 * 
 * Feature: parking-lot-management, Property 21: Revenue Tracking Accuracy
 * Validates: Requirements 8.3
 */
public class RevenueTrackingAccuracyProperties {

    @Property(tries = 100)
    void addingRevenueIncreasesTotalRevenue(
            @ForAll("parkingLotWithSpots") ParkingLot parkingLot,
            @ForAll @DoubleRange(min = 0.0, max = 1000.0) double paymentAmount) {
        
        double initialRevenue = parkingLot.getTotalRevenue();
        parkingLot.addRevenue(paymentAmount);
        double finalRevenue = parkingLot.getTotalRevenue();
        
        double expectedRevenue = initialRevenue + paymentAmount;
        
        assert Math.abs(finalRevenue - expectedRevenue) < 0.01 : 
            String.format("Revenue should increase by %.2f (from %.2f to %.2f), but was %.2f", 
                paymentAmount, initialRevenue, expectedRevenue, finalRevenue);
    }

    @Property(tries = 100)
    void multiplePaymentsAccumulateCorrectly(
            @ForAll("parkingLotWithSpots") ParkingLot parkingLot,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double payment1,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double payment2,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double payment3) {
        
        double initialRevenue = parkingLot.getTotalRevenue();
        
        parkingLot.addRevenue(payment1);
        parkingLot.addRevenue(payment2);
        parkingLot.addRevenue(payment3);
        
        double finalRevenue = parkingLot.getTotalRevenue();
        double expectedRevenue = initialRevenue + payment1 + payment2 + payment3;
        
        assert Math.abs(finalRevenue - expectedRevenue) < 0.01 : 
            String.format("Revenue should be %.2f but was %.2f", expectedRevenue, finalRevenue);
    }

    @Property(tries = 100)
    void adminPanelDisplaysCorrectRevenue(
            @ForAll("parkingLotWithSpots") ParkingLot parkingLot,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double revenue) {
        
        parkingLot.setTotalRevenue(revenue);
        
        AdminPanel adminPanel = new AdminPanel(parkingLot);
        String labelText = adminPanel.getRevenueLabel().getText();
        
        // Extract revenue from label text "Total Revenue: RM X.XX"
        String revenueStr = labelText.replaceAll("[^0-9.]", "");
        double displayedRevenue = Double.parseDouble(revenueStr);
        
        assert Math.abs(displayedRevenue - revenue) < 0.01 : 
            String.format("Admin panel should display revenue %.2f but displayed %.2f", 
                revenue, displayedRevenue);
    }

    @Property(tries = 100)
    void revenueNeverDecreases(
            @ForAll("parkingLotWithSpots") ParkingLot parkingLot,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double payment) {
        
        double initialRevenue = parkingLot.getTotalRevenue();
        parkingLot.addRevenue(payment);
        double finalRevenue = parkingLot.getTotalRevenue();
        
        assert finalRevenue >= initialRevenue : 
            "Revenue should never decrease after adding payment";
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
