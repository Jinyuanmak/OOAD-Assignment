package com.university.parking.model;

import org.junit.jupiter.api.Assertions;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.LongRange;

/**
 * Property-based tests for fine calculation strategy application.
 * Feature: parking-lot-management, Property 8: Fine Calculation Strategy Application
 * Validates: Requirements 5.3
 */
public class FineCalculationStrategyProperties {

    /**
     * Property 8: Fine Calculation Strategy Application
     * For any fine calculation, the result should match the currently selected strategy
     * (Fixed: RM 50, Progressive: RM 50 + escalating amounts, Hourly: RM 20/hour).
     */
    @Property(tries = 100)
    void fixedStrategyShouldAlwaysReturnFiftyRM(
            @ForAll @LongRange(min = 1, max = 168) long overstayHours
    ) {
        FineCalculationStrategy strategy = new FixedFineStrategy();
        double fine = strategy.calculateFine(overstayHours);
        
        Assertions.assertEquals(50.0, fine, 0.01,
            String.format("Fixed strategy should always return RM 50, but got RM %.2f for %d hours",
                fine, overstayHours));
    }

    @Property(tries = 100)
    void progressiveStrategyShouldReturnBasePlusEscalation(
            @ForAll @LongRange(min = 1, max = 168) long overstayHours
    ) {
        FineCalculationStrategy strategy = new ProgressiveFineStrategy();
        double fine = strategy.calculateFine(overstayHours);
        
        // Progressive: RM 50 + (overstayHours * 10)
        double expectedFine = 50.0 + (overstayHours * 10.0);
        
        Assertions.assertEquals(expectedFine, fine, 0.01,
            String.format("Progressive strategy should return RM %.2f for %d hours, but got RM %.2f",
                expectedFine, overstayHours, fine));
    }

    @Property(tries = 100)
    void hourlyStrategyShouldReturnTwentyPerHour(
            @ForAll @LongRange(min = 1, max = 168) long overstayHours
    ) {
        FineCalculationStrategy strategy = new HourlyFineStrategy();
        double fine = strategy.calculateFine(overstayHours);
        
        // Hourly: RM 20 per hour
        double expectedFine = overstayHours * 20.0;
        
        Assertions.assertEquals(expectedFine, fine, 0.01,
            String.format("Hourly strategy should return RM %.2f for %d hours, but got RM %.2f",
                expectedFine, overstayHours, fine));
    }

    @Property(tries = 100)
    void contextShouldApplySelectedStrategy(
            @ForAll @LongRange(min = 1, max = 168) long overstayHours
    ) {
        // Test with Fixed strategy
        FineCalculationContext context = new FineCalculationContext(new FixedFineStrategy());
        double fixedFine = context.calculateFine(overstayHours);
        Assertions.assertEquals(50.0, fixedFine, 0.01);
        
        // Change to Progressive strategy
        context.setStrategy(new ProgressiveFineStrategy());
        double progressiveFine = context.calculateFine(overstayHours);
        double expectedProgressive = 50.0 + (overstayHours * 10.0);
        Assertions.assertEquals(expectedProgressive, progressiveFine, 0.01);
        
        // Change to Hourly strategy
        context.setStrategy(new HourlyFineStrategy());
        double hourlyFine = context.calculateFine(overstayHours);
        double expectedHourly = overstayHours * 20.0;
        Assertions.assertEquals(expectedHourly, hourlyFine, 0.01);
    }
}
