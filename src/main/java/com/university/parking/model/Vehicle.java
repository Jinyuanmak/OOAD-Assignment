package com.university.parking.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents a vehicle in the parking system.
 * Contains vehicle information and parking-related data.
 */
public class Vehicle {
    private String licensePlate;
    private VehicleType type;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private boolean isHandicapped;
    private String assignedSpotId;
    
    // Real-time elapsed time fields (computed from VIEW)
    private Long elapsedSeconds;
    private Long elapsedMinutes;
    private Long elapsedHours;
    private Boolean isOverstay;

    public Vehicle() {}

    public Vehicle(String licensePlate, VehicleType type, boolean isHandicapped) {
        this.licensePlate = licensePlate;
        this.type = type;
        this.isHandicapped = isHandicapped;
    }

    /**
     * Calculates the parking duration in hours using ceiling rounding.
     * @return parking duration in hours (rounded up)
     */
    public long calculateParkingDuration() {
        if (entryTime == null || exitTime == null) {
            return 0;
        }
        long minutes = ChronoUnit.MINUTES.between(entryTime, exitTime);
        return (long) Math.ceil(minutes / 60.0);
    }

    /**
     * Determines if this vehicle can park in the specified spot type.
     * @param spotType the spot type to check
     * @return true if the vehicle can park in this spot type
     */
    public boolean canParkInSpot(SpotType spotType) {
        // Handicapped vehicles can park in any spot type
        if (isHandicapped) {
            return true;
        }

        switch (type) {
            case MOTORCYCLE:
                return spotType == SpotType.COMPACT;
            case CAR:
                return spotType == SpotType.COMPACT || spotType == SpotType.REGULAR;
            case SUV_TRUCK:
                return spotType == SpotType.REGULAR;
            case HANDICAPPED:
                return true; // Handicapped vehicles can park anywhere
            default:
                return false;
        }
    }

    // Getters and setters
    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public boolean isHandicapped() {
        return isHandicapped;
    }

    public void setHandicapped(boolean handicapped) {
        isHandicapped = handicapped;
    }

    public String getAssignedSpotId() {
        return assignedSpotId;
    }

    public void setAssignedSpotId(String assignedSpotId) {
        this.assignedSpotId = assignedSpotId;
    }

    // Getters for elapsed time fields (read-only, computed by database VIEW)
    public Long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public Long getElapsedMinutes() {
        return elapsedMinutes;
    }

    public Long getElapsedHours() {
        return elapsedHours;
    }

    public Boolean getIsOverstay() {
        return isOverstay;
    }

    // Public setters for DAO use (these fields are computed by database VIEW)
    public void setElapsedSeconds(Long elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public void setElapsedMinutes(Long elapsedMinutes) {
        this.elapsedMinutes = elapsedMinutes;
    }

    public void setElapsedHours(Long elapsedHours) {
        this.elapsedHours = elapsedHours;
    }

    public void setIsOverstay(Boolean isOverstay) {
        this.isOverstay = isOverstay;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "licensePlate='" + licensePlate + '\'' +
                ", type=" + type +
                ", entryTime=" + entryTime +
                ", exitTime=" + exitTime +
                ", isHandicapped=" + isHandicapped +
                ", assignedSpotId='" + assignedSpotId + '\'' +
                '}';
    }
}