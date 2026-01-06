package com.university.parking.model;

/**
 * Context class for fine calculation strategy.
 * Allows dynamic selection and application of different fine calculation strategies.
 */
public class FineCalculationContext {
    private FineCalculationStrategy strategy;
    
    /**
     * Creates a context with the default Fixed strategy.
     */
    public FineCalculationContext() {
        this.strategy = new FixedFineStrategy();
    }
    
    /**
     * Creates a context with a specific strategy.
     * @param strategy the fine calculation strategy to use
     */
    public FineCalculationContext(FineCalculationStrategy strategy) {
        this.strategy = strategy;
    }
    
    /**
     * Sets the fine calculation strategy.
     * @param strategy the strategy to use
     */
    public void setStrategy(FineCalculationStrategy strategy) {
        this.strategy = strategy;
    }
    
    /**
     * Gets the current strategy.
     * @return the current fine calculation strategy
     */
    public FineCalculationStrategy getStrategy() {
        return strategy;
    }
    
    /**
     * Calculates a fine using the current strategy.
     * @param overstayHours the number of hours overstayed
     * @return the calculated fine amount
     */
    public double calculateFine(long overstayHours) {
        return strategy.calculateFine(overstayHours);
    }
    
    /**
     * Gets the name of the current strategy.
     * @return the strategy name
     */
    public String getCurrentStrategyName() {
        return strategy.getStrategyName();
    }
}
