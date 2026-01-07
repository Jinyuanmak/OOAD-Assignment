package com.university.parking.model;

/**
 * Represents a parking spot in the parking lot.
 * Contains spot information, status, and current vehicle assignment.
 */
public class ParkingSpot {
    private String spotId;
    private SpotType type;
    private SpotStatus status;
    private Vehicle currentVehicle;
    private double hourlyRate;

    public ParkingSpot() {}

    public ParkingSpot(String spotId, SpotType type) {
        this.spotId = spotId;
        this.type = type;
        this.status = SpotStatus.AVAILABLE;
        this.hourlyRate = type.getHourlyRate();
    }

    /**
     * Checks if the spot is available for parking.
     * @return true if the spot is available
     */
    public boolean isAvailable() {
        return status == SpotStatus.AVAILABLE;
    }

    /**
     * Occupies the spot with the specified vehicle.
     * @param vehicle the vehicle to assign to this spot
     */
    public void occupySpot(Vehicle vehicle) {
        if (isAvailable()) {
            this.currentVehicle = vehicle;
            this.status = SpotStatus.OCCUPIED;
        }
    }

    /**
     * Assigns a vehicle to this spot (used when loading from database).
     * Does not check availability status.
     * @param vehicle the vehicle to assign to this spot
     */
    public void assignVehicle(Vehicle vehicle) {
        this.currentVehicle = vehicle;
        this.status = SpotStatus.OCCUPIED;
    }

    /**
     * Vacates the spot, making it available again.
     */
    public void vacateSpot() {
        this.currentVehicle = null;
        this.status = SpotStatus.AVAILABLE;
    }

    // Getters and setters
    public String getSpotId() {
        return spotId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
    }

    public SpotType getType() {
        return type;
    }

    public void setType(SpotType type) {
        this.type = type;
        this.hourlyRate = type.getHourlyRate();
    }

    public SpotStatus getStatus() {
        return status;
    }

    public void setStatus(SpotStatus status) {
        this.status = status;
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    public void setCurrentVehicle(Vehicle currentVehicle) {
        this.currentVehicle = currentVehicle;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    @Override
    public String toString() {
        return "ParkingSpot{" +
                "spotId='" + spotId + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", currentVehicle=" + currentVehicle +
                ", hourlyRate=" + hourlyRate +
                '}';
    }
}