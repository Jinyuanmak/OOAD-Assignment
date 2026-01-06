package com.university.parking.model;

import java.time.LocalDateTime;

/**
 * Represents a parking session for a vehicle.
 * Contains session information including entry/exit times and ticket details.
 */
public class ParkingSession {
    private Long id;
    private Long vehicleId;
    private String spotId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Integer durationHours;
    private String ticketNumber;

    public ParkingSession() {}

    public ParkingSession(Long vehicleId, String spotId, String ticketNumber) {
        this.vehicleId = vehicleId;
        this.spotId = spotId;
        this.ticketNumber = ticketNumber;
        this.entryTime = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getSpotId() {
        return spotId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
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

    public Integer getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    @Override
    public String toString() {
        return "ParkingSession{" +
                "id=" + id +
                ", vehicleId=" + vehicleId +
                ", spotId='" + spotId + '\'' +
                ", entryTime=" + entryTime +
                ", exitTime=" + exitTime +
                ", durationHours=" + durationHours +
                ", ticketNumber='" + ticketNumber + '\'' +
                '}';
    }
}