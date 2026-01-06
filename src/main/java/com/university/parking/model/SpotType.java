package com.university.parking.model;

/**
 * Enumeration representing different types of parking spots with their hourly rates.
 * Each spot type has a specific hourly rate in Malaysian Ringgit (RM).
 */
public enum SpotType {
    COMPACT(2.0),
    REGULAR(5.0),
    HANDICAPPED(2.0),
    RESERVED(10.0);

    private final double hourlyRate;

    SpotType(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    /**
     * Gets the hourly rate for this spot type.
     * @return the hourly rate in RM
     */
    public double getHourlyRate() {
        return hourlyRate;
    }
}