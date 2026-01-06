package com.university.parking.model;

/**
 * Hourly fine calculation strategy.
 * Charges RM 20 per hour of overstay.
 */
public class HourlyFineStrategy implements FineCalculationStrategy {
    private static final double HOURLY_RATE = 20.0;
    
    @Override
    public double calculateFine(long overstayHours) {
        return overstayHours * HOURLY_RATE;
    }
    
    @Override
    public String getStrategyName() {
        return "HOURLY";
    }
}
