package com.university.parking.model;

/**
 * Progressive fine calculation strategy with tiered escalation.
 * Fines increase based on overstay duration tiers:
 * - 0-24 hours: No fine (normal parking)
 * - 24-48 hours (Day 2): RM 50 fine
 * - 48-72 hours (Day 3): Additional RM 100 (Total: RM 150)
 * - 72-96 hours (Day 4): Additional RM 150 (Total: RM 300)
 * - 96+ hours (Day 5+): Additional RM 200 (Total: RM 500)
 */
public class ProgressiveFineStrategy implements FineCalculationStrategy {
    private static final double DAY_2_FINE = 50.0;   // 24-48 hours (1st day overstay)
    private static final double DAY_3_FINE = 100.0;  // 48-72 hours (2nd day overstay)
    private static final double DAY_4_FINE = 150.0;  // 72-96 hours (3rd day overstay)
    private static final double DAY_5_FINE = 200.0;  // 96+ hours (4th day overstay)
    
    @Override
    public double calculateFine(long overstayHours) {
        double totalFine = 0.0;
        
        // Day 2: 24-48 hours (1st day overstay)
        if (overstayHours > 0) {
            totalFine += DAY_2_FINE;
        }
        
        // Day 3: 48-72 hours (2nd day overstay)
        if (overstayHours > 24) {
            totalFine += DAY_3_FINE;
        }
        
        // Day 4: 72-96 hours (3rd day overstay)
        if (overstayHours > 48) {
            totalFine += DAY_4_FINE;
        }
        
        // Day 5+: 96+ hours (4th day overstay)
        if (overstayHours > 72) {
            totalFine += DAY_5_FINE;
        }
        
        return totalFine;
    }
    
    @Override
    public String getStrategyName() {
        return "PROGRESSIVE";
    }
}
