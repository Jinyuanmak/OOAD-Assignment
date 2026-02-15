package com.university.parking.util;

import com.university.parking.model.ParkingSpot;
import com.university.parking.model.SpotType;
import com.university.parking.model.Vehicle;

/**
 * Fee calculation engine for the parking system.
 * Handles basic parking fee calculation and handicapped card holder pricing logic.
 */
public class FeeCalculator {

    /**
     * Calculates the parking fee for a vehicle based on duration and spot type.
     * Implements basic fee calculation: duration × hourly rate
     * Applies handicapped card holder pricing logic as per requirements 7.1 and 7.2.
     * 
     * Card holders in handicapped spots park for FREE (RM 0/hour).
     * Card holders in other spots get RM 2/hour discounted rate.
     * 
     * @param vehicle the vehicle that parked
     * @param spot the parking spot where the vehicle parked
     * @param durationHours the parking duration in hours (already ceiling-rounded)
     * @return the calculated parking fee in RM
     */
    public static double calculateParkingFee(Vehicle vehicle, ParkingSpot spot, long durationHours) {
        if (vehicle == null || spot == null || durationHours < 0) {
            throw new IllegalArgumentException("Invalid input for fee calculation");
        }

        double hourlyRate = getApplicableHourlyRate(vehicle, spot);
        return durationHours * hourlyRate;
    }

    /**
     * Determines the applicable hourly rate based on vehicle and spot type.
     * Implements handicapped card holder pricing logic:
     * - ANY vehicle type WITH card holder in HANDICAPPED spot: FREE (RM 0/hour)
     * - ANY vehicle type WITH card holder in other spots: RM 2/hour (discounted rate)
     * - ANY vehicle type WITHOUT card holder: standard spot rate
     * 
     * @param vehicle the vehicle
     * @param spot the parking spot
     * @return the applicable hourly rate in RM
     */
    private static double getApplicableHourlyRate(Vehicle vehicle, ParkingSpot spot) {
        // Card holder in handicapped spot: FREE
        if (vehicle.isHandicapped() && spot.getType() == SpotType.HANDICAPPED) {
            return 0.0;  // FREE for card holders in handicapped spots
        }
        
        // Card holder in other spots: RM 2/hour discounted rate
        if (vehicle.isHandicapped()) {
            return 2.0;  // RM 2/hour discounted rate for card holders in non-handicapped spots
        }
        
        // All other cases: standard spot rate
        return spot.getHourlyRate();
    }

    /**
     * Calculates the total amount due including parking fee and fines.
     * 
     * @param parkingFee the parking fee
     * @param fineAmount the total fine amount
     * @return the total amount due
     */
    public static double calculateTotalAmount(double parkingFee, double fineAmount) {
        return parkingFee + fineAmount;
    }
}
