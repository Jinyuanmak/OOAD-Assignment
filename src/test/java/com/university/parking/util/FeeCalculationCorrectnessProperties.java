package com.university.parking.util;

import org.junit.jupiter.api.Assertions;

import com.university.parking.model.ParkingSpot;
import com.university.parking.model.SpotType;
import com.university.parking.model.Vehicle;
import com.university.parking.model.VehicleType;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for fee calculation correctness.
 * Feature: parking-lot-management, Property 5: Fee Calculation Correctness
 * Validates: Requirements 4.4
 */
public class FeeCalculationCorrectnessProperties {

    /**
     * Property 5: Fee Calculation Correctness
     * For any parking session, the total parking fee should equal 
     * the duration in hours multiplied by the spot's hourly rate.
     */
    @Property(tries = 100)
    void parkingFeeShouldEqualDurationTimesHourlyRate(
            @ForAll @IntRange(min = 0, max = 168) int durationHours, // Up to 1 week
            @ForAll SpotType spotType,
            @ForAll VehicleType vehicleType
    ) {
        // Create a non-handicapped vehicle to test basic fee calculation
        Vehicle vehicle = new Vehicle("TEST-123", vehicleType, false);
        ParkingSpot spot = new ParkingSpot("F1-R1-S1", spotType);

        // Calculate fee
        double calculatedFee = FeeCalculator.calculateParkingFee(vehicle, spot, durationHours);

        // Expected fee is duration × hourly rate
        double expectedFee = durationHours * spot.getHourlyRate();

        // Assert the fee matches the expected calculation
        Assertions.assertEquals(expectedFee, calculatedFee, 0.01,
            String.format("Fee calculation incorrect: expected %.2f but got %.2f for %d hours at RM %.2f/hour",
                expectedFee, calculatedFee, durationHours, spot.getHourlyRate()));
    }
}
