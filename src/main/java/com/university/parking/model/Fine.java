package com.university.parking.model;

import java.time.LocalDateTime;

/**
 * Represents a fine issued to a vehicle for parking violations.
 * Fines are linked to license plates and persist across parking sessions.
 */
public class Fine {
    private Long id;
    private String licensePlate;
    private FineType type;
    private double amount;
    private LocalDateTime issuedDate;
    private boolean isPaid;

    public Fine() {}

    public Fine(String licensePlate, FineType type, double amount) {
        this.licensePlate = licensePlate;
        this.type = type;
        this.amount = amount;
        this.issuedDate = LocalDateTime.now();
        this.isPaid = false;
    }

    /**
     * Calculates the fine amount using the provided strategy.
     * @param strategy the fine calculation strategy
     * @param overstayHours the number of hours overstayed
     * @return the calculated fine amount
     */
    public double calculateFineAmount(FineCalculationStrategy strategy, long overstayHours) {
        return strategy.calculateFine(overstayHours);
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

    public FineType getType() {
        return type;
    }

    public void setType(FineType type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDateTime issuedDate) {
        this.issuedDate = issuedDate;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    @Override
    public String toString() {
        return "Fine{" +
                "id=" + id +
                ", licensePlate='" + licensePlate + '\'' +
                ", type=" + type +
                ", amount=" + amount +
                ", issuedDate=" + issuedDate +
                ", isPaid=" + isPaid +
                '}';
    }
}