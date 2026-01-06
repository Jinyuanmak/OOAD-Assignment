package com.university.parking.model;

/**
 * Fixed fine calculation strategy.
 * Applies a flat fine of RM 50 regardless of overstay duration.
 */
public class FixedFineStrategy implements FineCalculationStrategy {
    private static final double FIXED_FINE_AMOUNT = 50.0;
    
    @Override
    public double calculateFine(long overstayHours) {
        return FIXED_FINE_AMOUNT;
    }
    
    @Override
    public String getStrategyName() {
        return "FIXED";
    }
}
