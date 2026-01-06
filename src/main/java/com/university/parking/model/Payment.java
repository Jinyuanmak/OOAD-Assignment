package com.university.parking.model;

import java.time.LocalDateTime;

/**
 * Represents a payment transaction in the parking system.
 * Contains payment details including fees, fines, and payment method.
 */
public class Payment {
    private Long id;
    private String licensePlate;
    private double parkingFee;
    private double fineAmount;
    private double totalAmount;
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentDate;
    private Long parkingSessionId;

    public Payment() {}

    public Payment(String licensePlate, double parkingFee, double fineAmount, PaymentMethod paymentMethod) {
        this.licensePlate = licensePlate;
        this.parkingFee = parkingFee;
        this.fineAmount = fineAmount;
        this.totalAmount = parkingFee + fineAmount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = LocalDateTime.now();
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

    public double getParkingFee() {
        return parkingFee;
    }

    public void setParkingFee(double parkingFee) {
        this.parkingFee = parkingFee;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Long getParkingSessionId() {
        return parkingSessionId;
    }

    public void setParkingSessionId(Long parkingSessionId) {
        this.parkingSessionId = parkingSessionId;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", licensePlate='" + licensePlate + '\'' +
                ", parkingFee=" + parkingFee +
                ", fineAmount=" + fineAmount +
                ", totalAmount=" + totalAmount +
                ", paymentMethod=" + paymentMethod +
                ", paymentDate=" + paymentDate +
                ", parkingSessionId=" + parkingSessionId +
                '}';
    }
}