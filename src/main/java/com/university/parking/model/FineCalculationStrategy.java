package com.university.parking.model;

/**
 * Strategy interface for calculating fine amounts.
 * Different strategies can be applied based on parking lot policy.
 */
public interface FineCalculationStrategy {
    /**
     * Calculates the fine amount based on overstay hours.
     * @param overstayHours the number of hours the vehicle overstayed
     * @return the calculated fine amount in RM
     */
    double calculateFine(long overstayHours);
    
    /**
     * Gets the name of this strategy.
     * @return the strategy name
     */
    String getStrategyName();
}
