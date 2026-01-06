package com.university.parking.model;

/**
 * Progressive fine calculation strategy.
 * Starts with RM 50 base fine and adds escalating amounts based on overstay duration.
 * Formula: RM 50 + (overstayHours * 10)
 */
public class ProgressiveFineStrategy implements FineCalculationStrategy {
    private static final double BASE_FINE = 50.0;
    private static final double ESCALATION_RATE = 10.0;
    
    @Override
    public double calculateFine(long overstayHours) {
        return BASE_FINE + (overstayHours * ESCALATION_RATE);
    }
    
    @Override
    public String getStrategyName() {
        return "PROGRESSIVE";
    }
}
