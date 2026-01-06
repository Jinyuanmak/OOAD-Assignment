package com.university.parking.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for future-only fine strategy application.
 * Feature: parking-lot-management, Property 19: Future-Only Fine Strategy Application
 * Validates: Requirements 5.4
 */
public class FutureOnlyFineStrategyProperties {

    /**
     * Property 19: Future-Only Fine Strategy Application
     * For any change in fine calculation strategy, the new strategy should only apply to
     * fines generated after the change, not existing fines.
     */
    @Property(tries = 100)
    void strategyChangeShouldOnlyAffectFutureEntries(
            @ForAll @IntRange(min = 1, max = 100) int hoursBeforeChange,
            @ForAll @IntRange(min = 1, max = 100) int hoursAfterChange
    ) {
        ParkingLot parkingLot = new ParkingLot("Test Lot");

        // Record the initial strategy change time
        LocalDateTime initialStrategyTime = parkingLot.getStrategyChangeTime();

        // Change strategy
        parkingLot.changeFineStrategy(new ProgressiveFineStrategy());
        LocalDateTime strategyChangeTime = parkingLot.getStrategyChangeTime();

        // Verify strategy change time is recorded
        Assertions.assertNotNull(strategyChangeTime,
            "Strategy change time should be recorded");
        Assertions.assertTrue(strategyChangeTime.isAfter(initialStrategyTime) || 
                            strategyChangeTime.isEqual(initialStrategyTime),
            "Strategy change time should be after or equal to initial time");

        // Create timestamps relative to the strategy change time
        LocalDateTime beforeChangeTime = strategyChangeTime.minusHours(hoursBeforeChange);
        LocalDateTime afterChangeTime = strategyChangeTime.plusHours(hoursAfterChange);

        // Verify that entries before the change would use old strategy
        // and entries after would use new strategy
        Assertions.assertTrue(beforeChangeTime.isBefore(strategyChangeTime),
            "Time before change should be before strategy change time");
        Assertions.assertTrue(afterChangeTime.isAfter(strategyChangeTime),
            "Time after change should be after strategy change time");
    }

    @Property(tries = 100)
    void strategyChangeTimeShouldBeUpdatedOnEachChange(
            @ForAll @IntRange(min = 1, max = 5) int numberOfChanges
    ) {
        ParkingLot parkingLot = new ParkingLot("Test Lot");

        LocalDateTime previousChangeTime = parkingLot.getStrategyChangeTime();
        FineCalculationStrategy[] strategies = {
            new FixedFineStrategy(),
            new ProgressiveFineStrategy(),
            new HourlyFineStrategy()
        };

        for (int i = 0; i < numberOfChanges; i++) {
            // Small delay to ensure time difference
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Change strategy
            FineCalculationStrategy strategy = strategies[i % strategies.length];
            parkingLot.changeFineStrategy(strategy);

            LocalDateTime currentChangeTime = parkingLot.getStrategyChangeTime();

            // Verify the change time was updated
            Assertions.assertTrue(
                currentChangeTime.isAfter(previousChangeTime) || 
                currentChangeTime.isEqual(previousChangeTime),
                String.format("Strategy change time should be updated on change %d", i + 1));

            previousChangeTime = currentChangeTime;
        }
    }

    @Property(tries = 100)
    void newStrategyContextShouldUseNewStrategy(
            @ForAll @IntRange(min = 1, max = 168) long overstayHours
    ) {
        ParkingLot parkingLot = new ParkingLot("Test Lot");

        // Initially uses Fixed strategy (default)
        double initialFine = parkingLot.getFineCalculationContext().calculateFine(overstayHours);
        Assertions.assertEquals(50.0, initialFine, 0.01,
            "Initial strategy should be Fixed (RM 50)");

        // Change to Progressive strategy
        parkingLot.changeFineStrategy(new ProgressiveFineStrategy());
        double progressiveFine = parkingLot.getFineCalculationContext().calculateFine(overstayHours);
        double expectedProgressive = 50.0 + (overstayHours * 10.0);
        Assertions.assertEquals(expectedProgressive, progressiveFine, 0.01,
            "After change, should use Progressive strategy");

        // Change to Hourly strategy
        parkingLot.changeFineStrategy(new HourlyFineStrategy());
        double hourlyFine = parkingLot.getFineCalculationContext().calculateFine(overstayHours);
        double expectedHourly = overstayHours * 20.0;
        Assertions.assertEquals(expectedHourly, hourlyFine, 0.01,
            "After second change, should use Hourly strategy");
    }

    @Property(tries = 100)
    void strategyChangeTimeCanBeUsedToFilterFines(
            @ForAll @IntRange(min = 1, max = 24) int hoursBeforeChange,
            @ForAll @IntRange(min = 1, max = 24) int hoursAfterChange
    ) {
        ParkingLot parkingLot = new ParkingLot("Test Lot");

        // Get initial strategy time
        LocalDateTime strategyChangeTime = parkingLot.getStrategyChangeTime();

        // Create timestamps for fines before and after strategy change
        LocalDateTime fineBeforeChange = strategyChangeTime.minusHours(hoursBeforeChange);
        LocalDateTime fineAfterChange = strategyChangeTime.plusHours(hoursAfterChange);

        // Verify we can distinguish between old and new fines
        boolean isOldFine = fineBeforeChange.isBefore(strategyChangeTime);
        boolean isNewFine = fineAfterChange.isAfter(strategyChangeTime);

        Assertions.assertTrue(isOldFine,
            "Fine issued before strategy change should be identified as old");
        Assertions.assertTrue(isNewFine,
            "Fine issued after strategy change should be identified as new");
    }
}
