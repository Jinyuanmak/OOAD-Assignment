package com.university.parking.model;

import java.time.LocalDateTime;

/**
 * Represents a parking spot reservation.
 * Allows vehicles to reserve specific spots for a time period.
 */
public class Reservation {
    private Long id;
    private String licensePlate;
    private String spotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isActive;
    private LocalDateTime createdAt;
    private double prepaidAmount;  // Amount paid upfront for the reservation

    public Reservation() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    public Reservation(String licensePlate, String spotId, LocalDateTime startTime, LocalDateTime endTime) {
        this();
        this.licensePlate = licensePlate;
        this.spotId = spotId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Checks if the reservation is valid at the given time.
     * @param checkTime the time to check
     * @return true if reservation is active and within time range
     */
    public boolean isValidAt(LocalDateTime checkTime) {
        if (!isActive) {
            return false;
        }
        return !checkTime.isBefore(startTime) && !checkTime.isAfter(endTime);
    }

    /**
     * Checks if the reservation is currently valid.
     * @return true if reservation is active and current time is within range
     */
    public boolean isCurrentlyValid() {
        return isValidAt(LocalDateTime.now());
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getSpotId() {
        return spotId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public double getPrepaidAmount() {
        return prepaidAmount;
    }

    public void setPrepaidAmount(double prepaidAmount) {
        this.prepaidAmount = prepaidAmount;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", licensePlate='" + licensePlate + '\'' +
                ", spotId='" + spotId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", isActive=" + isActive +
                ", prepaidAmount=" + prepaidAmount +
                '}';
    }
}
