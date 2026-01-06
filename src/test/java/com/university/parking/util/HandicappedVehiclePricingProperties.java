package com.university.parking.util;

import org.junit.jupiter.api.Assertions;

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
 * Property-based tests for handicapped vehicle pricing.
 * Feature: parking-lot-management, Property 10: Handicapped Vehicle Pricing
 * Validates: Requirements 7.1, 7.2
 */
public class HandicappedVehiclePricingProperties {

    /**
     * Property 10: Handicapped Vehicle Pricing (Part 1)
     * For any handicapped vehicle, the hourly rate should be RM 2/hour 
     * when parked in handicapped spots.
     */
    @Property(tries = 100)
    void handicappedVehicleInHandicappedSpotChargesTwoPerHour(
            @ForAll @IntRange(min = 1, max = 168) int durationHours,
            @ForAll VehicleType vehicleType
    ) {
        // Create a handicapped vehicle
        Vehicle vehicle = new Vehicle("HANDI-123", vehicleType, true);
        
        // Create a handicapped spot
        ParkingSpot spot = new ParkingSpot("F1-R1-S1", SpotType.HANDICAPPED);

        // Calculate fee
        double calculatedFee = FeeCalculator.calculateParkingFee(vehicle, spot, durationHours);

        // Expected fee is duration × RM 2/hour
        double expectedFee = durationHours * 2.0;

        // Assert the fee matches the expected calculation
        Assertions.assertEquals(expectedFee, calculatedFee, 0.01,
            String.format("Handicapped vehicle in handicapped spot should charge RM 2/hour: " +
                "expected %.2f but got %.2f for %d hours",
                expectedFee, calculatedFee, durationHours));
    }

    /**
     * Property 10: Handicapped Vehicle Pricing (Part 2)
     * For any handicapped vehicle, when parked in non-handicapped spots, 
     * the system should charge the standard rate for that spot type.
     */
    @Property(tries = 100)
    void handicappedVehicleInNonHandicappedSpotChargesStandardRate(
            @ForAll @IntRange(min = 1, max = 168) int durationHours,
            @ForAll VehicleType vehicleType,
            @ForAll("nonHandicappedSpotTypes") SpotType spotType
    ) {
        // Create a handicapped vehicle
        Vehicle vehicle = new Vehicle("HANDI-456", vehicleType, true);
        
        // Create a non-handicapped spot
        ParkingSpot spot = new ParkingSpot("F1-R2-S5", spotType);

        // Calculate fee
        double calculatedFee = FeeCalculator.calculateParkingFee(vehicle, spot, durationHours);

        // Expected fee is duration × standard spot rate
        double expectedFee = durationHours * spot.getHourlyRate();

        // Assert the fee matches the expected calculation
        Assertions.assertEquals(expectedFee, calculatedFee, 0.01,
            String.format("Handicapped vehicle in %s spot should charge standard rate (RM %.2f/hour): " +
                "expected %.2f but got %.2f for %d hours",
                spotType, spot.getHourlyRate(), expectedFee, calculatedFee, durationHours));
    }

    @Provide
    Arbitrary<SpotType> nonHandicappedSpotTypes() {
        return Arbitraries.of(SpotType.COMPACT, SpotType.REGULAR, SpotType.RESERVED);
    }

    /**
     * Property test to verify non-handicapped vehicles are not affected by handicapped pricing.
     */
    @Property(tries = 100)
    void nonHandicappedVehiclesChargeStandardRateInAllSpots(
            @ForAll @IntRange(min = 1, max = 168) int durationHours,
            @ForAll VehicleType vehicleType,
            @ForAll SpotType spotType
    ) {
        // Create a non-handicapped vehicle
        Vehicle vehicle = new Vehicle("REGULAR-789", vehicleType, false);
        
        // Create any spot
        ParkingSpot spot = new ParkingSpot("F2-R3-S7", spotType);

        // Calculate fee
        double calculatedFee = FeeCalculator.calculateParkingFee(vehicle, spot, durationHours);

        // Expected fee is always duration × standard spot rate
        double expectedFee = durationHours * spot.getHourlyRate();

        // Assert the fee matches the expected calculation
        Assertions.assertEquals(expectedFee, calculatedFee, 0.01,
            String.format("Non-handicapped vehicle should always charge standard rate: " +
                "expected %.2f but got %.2f for %d hours in %s spot",
                expectedFee, calculatedFee, durationHours, spotType));
    }
}
