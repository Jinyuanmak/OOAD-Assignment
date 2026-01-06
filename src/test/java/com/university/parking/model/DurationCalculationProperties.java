package com.university.parking.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for parking duration calculation accuracy.
 * Feature: parking-lot-management, Property 4: Duration Calculation Accuracy
 * Validates: Requirements 2.7, 4.3
 */
public class DurationCalculationProperties {

    /**
     * Property 4: Duration Calculation Accuracy
     * For any parking session, the calculated duration should be the ceiling of the 
     * actual time difference in hours
     */
    @Property(tries = 100)
    void durationCalculationAccuracy(
            @ForAll @IntRange(min = 0, max = 23) int entryHour,
            @ForAll @IntRange(min = 0, max = 59) int entryMinute,
            @ForAll @IntRange(min = 1, max = 168) int parkingMinutes) { // Up to 7 days
        
        // Create a vehicle
        Vehicle vehicle = new Vehicle("TEST123", VehicleType.CAR, false);
        
        // Set entry time
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 1, entryHour, entryMinute);
        vehicle.setEntryTime(entryTime);
        
        // Set exit time by adding parking minutes
        LocalDateTime exitTime = entryTime.plusMinutes(parkingMinutes);
        vehicle.setExitTime(exitTime);
        
        // Calculate expected duration using ceiling rounding
        double exactHours = parkingMinutes / 60.0;
        long expectedDuration = (long) Math.ceil(exactHours);
        
        // Get actual duration from vehicle
        long actualDuration = vehicle.calculateParkingDuration();
        
        Assertions.assertEquals(expectedDuration, actualDuration,
            String.format("Duration calculation failed: %d minutes should result in %d hours (ceiling of %.2f), but got %d",
                parkingMinutes, expectedDuration, exactHours, actualDuration));
    }

    /**
     * Property test for edge cases with exact hour boundaries.
     */
    @Property(tries = 100)
    void exactHourBoundaries(
            @ForAll @IntRange(min = 1, max = 24) int exactHours) {
        
        Vehicle vehicle = new Vehicle("EXACT123", VehicleType.CAR, false);
        
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime exitTime = entryTime.plusHours(exactHours);
        
        vehicle.setEntryTime(entryTime);
        vehicle.setExitTime(exitTime);
        
        long calculatedDuration = vehicle.calculateParkingDuration();
        
        // For exact hours, ceiling should return the same number
        Assertions.assertEquals(exactHours, calculatedDuration,
            "Exact hour parking should return the same number of hours");
    }

    /**
     * Property test for fractional hours to ensure ceiling rounding.
     */
    @Property(tries = 100)
    void fractionalHoursCeilingRounding(
            @ForAll @IntRange(min = 1, max = 12) int baseHours,
            @ForAll @IntRange(min = 1, max = 59) int additionalMinutes) {
        
        Vehicle vehicle = new Vehicle("FRAC123", VehicleType.CAR, false);
        
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime exitTime = entryTime.plusHours(baseHours).plusMinutes(additionalMinutes);
        
        vehicle.setEntryTime(entryTime);
        vehicle.setExitTime(exitTime);
        
        long calculatedDuration = vehicle.calculateParkingDuration();
        
        // Should always round up to the next hour
        long expectedDuration = baseHours + 1;
        
        Assertions.assertEquals(expectedDuration, calculatedDuration,
            String.format("Fractional hours (%d hours + %d minutes) should round up to %d hours, but got %d",
                baseHours, additionalMinutes, expectedDuration, calculatedDuration));
    }

    /**
     * Property test for zero and null time handling.
     */
    @Property(tries = 100)
    void nullAndZeroTimeHandling(@ForAll VehicleType vehicleType) {
        
        Vehicle vehicle = new Vehicle("NULL123", vehicleType, false);
        
        // Test with both times null
        vehicle.setEntryTime(null);
        vehicle.setExitTime(null);
        long duration1 = vehicle.calculateParkingDuration();
        Assertions.assertEquals(0, duration1, "Duration should be 0 when both times are null");
        
        // Test with only entry time set
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        vehicle.setEntryTime(entryTime);
        vehicle.setExitTime(null);
        long duration2 = vehicle.calculateParkingDuration();
        Assertions.assertEquals(0, duration2, "Duration should be 0 when exit time is null");
        
        // Test with only exit time set
        vehicle.setEntryTime(null);
        vehicle.setExitTime(entryTime);
        long duration3 = vehicle.calculateParkingDuration();
        Assertions.assertEquals(0, duration3, "Duration should be 0 when entry time is null");
    }

    /**
     * Property test for very short parking durations (less than 1 hour).
     */
    @Property(tries = 100)
    void shortParkingDurations(
            @ForAll @IntRange(min = 1, max = 59) int parkingMinutes) {
        
        Vehicle vehicle = new Vehicle("SHORT123", VehicleType.MOTORCYCLE, false);
        
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 1, 14, 30);
        LocalDateTime exitTime = entryTime.plusMinutes(parkingMinutes);
        
        vehicle.setEntryTime(entryTime);
        vehicle.setExitTime(exitTime);
        
        long calculatedDuration = vehicle.calculateParkingDuration();
        
        // Any parking time less than 60 minutes should round up to 1 hour
        Assertions.assertEquals(1, calculatedDuration,
            String.format("Parking for %d minutes should round up to 1 hour, but got %d",
                parkingMinutes, calculatedDuration));
    }

    /**
     * Property test for long parking durations (multiple days).
     */
    @Property(tries = 100)
    void longParkingDurations(
            @ForAll @IntRange(min = 1, max = 7) int days,
            @ForAll @IntRange(min = 0, max = 23) int additionalHours,
            @ForAll @IntRange(min = 0, max = 59) int additionalMinutes) {
        
        Vehicle vehicle = new Vehicle("LONG123", VehicleType.SUV_TRUCK, false);
        
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 1, 8, 0);
        LocalDateTime exitTime = entryTime.plusDays(days)
                                          .plusHours(additionalHours)
                                          .plusMinutes(additionalMinutes);
        
        vehicle.setEntryTime(entryTime);
        vehicle.setExitTime(exitTime);
        
        long calculatedDuration = vehicle.calculateParkingDuration();
        
        // Calculate expected duration
        long totalMinutes = (days * 24 * 60) + (additionalHours * 60) + additionalMinutes;
        long expectedDuration = (long) Math.ceil(totalMinutes / 60.0);
        
        Assertions.assertEquals(expectedDuration, calculatedDuration,
            String.format("Long parking duration calculation failed for %d days, %d hours, %d minutes",
                days, additionalHours, additionalMinutes));
    }

    /**
     * Property test to ensure duration is always non-negative and reasonable.
     */
    @Property(tries = 100)
    void durationIsNonNegativeAndReasonable(
            @ForAll @IntRange(min = 1, max = 1440) int parkingMinutes) { // Up to 24 hours
        
        Vehicle vehicle = new Vehicle("POSITIVE123", VehicleType.CAR, true);
        
        LocalDateTime entryTime = LocalDateTime.of(2024, 6, 15, 12, 0);
        LocalDateTime exitTime = entryTime.plusMinutes(parkingMinutes);
        
        vehicle.setEntryTime(entryTime);
        vehicle.setExitTime(exitTime);
        
        long calculatedDuration = vehicle.calculateParkingDuration();
        
        // Duration should always be positive
        Assertions.assertTrue(calculatedDuration > 0,
            "Calculated duration should always be positive for valid parking sessions");
        
        // Duration should be reasonable (not more than the ceiling of actual time)
        long maxExpectedDuration = (long) Math.ceil(parkingMinutes / 60.0);
        Assertions.assertTrue(calculatedDuration <= maxExpectedDuration,
            "Calculated duration should not exceed the ceiling of actual parking time");
        
        // Duration should be at least 1 hour for any positive parking time
        Assertions.assertTrue(calculatedDuration >= 1,
            "Any positive parking time should result in at least 1 hour duration");
    }
}