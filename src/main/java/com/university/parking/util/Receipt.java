package com.university.parking.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.university.parking.model.PaymentMethod;

/**
 * Represents a payment receipt containing all transaction details.
 * Includes entry time, exit time, duration, fee breakdown, fines, 
 * total paid, payment method, and remaining balance.
 */
public class Receipt {
    private String licensePlate;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private long durationHours;
    private double parkingFee;
    private double fineAmount;
    private double totalAmount;
    private double amountPaid;
    private double remainingBalance;
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentDate;
    private String spotId;

    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Receipt(String licensePlate, LocalDateTime entryTime, LocalDateTime exitTime,
                   long durationHours, double parkingFee, double fineAmount,
                   double amountPaid, PaymentMethod paymentMethod, String spotId) {
        this.licensePlate = licensePlate;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.durationHours = durationHours;
        this.parkingFee = parkingFee;
        this.fineAmount = fineAmount;
        this.totalAmount = parkingFee + fineAmount;
        this.amountPaid = amountPaid;
        this.remainingBalance = Math.max(0, totalAmount - amountPaid);
        this.paymentMethod = paymentMethod;
        this.paymentDate = LocalDateTime.now();
        this.spotId = spotId;
    }

    /**
     * Generates a formatted receipt string.
     * @return formatted receipt text
     */
    public String generateReceiptText() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("========================================\n");
        receipt.append("       PARKING RECEIPT\n");
        receipt.append("========================================\n");
        receipt.append(String.format("License Plate: %s\n", licensePlate));
        receipt.append(String.format("Spot: %s\n", spotId));
        receipt.append(String.format("Entry Time: %s\n", entryTime.format(DATE_FORMATTER)));
        receipt.append(String.format("Exit Time: %s\n", exitTime.format(DATE_FORMATTER)));
        receipt.append(String.format("Duration: %d hour(s)\n", durationHours));
        receipt.append("----------------------------------------\n");
        receipt.append(String.format("Parking Fee: RM %.2f\n", parkingFee));
        receipt.append(String.format("Fines: RM %.2f\n", fineAmount));
        receipt.append("----------------------------------------\n");
        receipt.append(String.format("Total Amount: RM %.2f\n", totalAmount));
        receipt.append(String.format("Amount Paid: RM %.2f\n", amountPaid));
        receipt.append(String.format("Payment Method: %s\n", paymentMethod));
        receipt.append(String.format("Remaining Balance: RM %.2f\n", remainingBalance));
        receipt.append("----------------------------------------\n");
        receipt.append(String.format("Payment Date: %s\n", paymentDate.format(DATE_FORMATTER)));
        receipt.append("========================================\n");
        receipt.append("     Thank you for parking with us!\n");
        receipt.append("========================================\n");
        return receipt.toString();
    }

    // Getters
    public String getLicensePlate() {
        return licensePlate;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public long getDurationHours() {
        return durationHours;
    }

    public double getParkingFee() {
        return parkingFee;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public double getRemainingBalance() {
        return remainingBalance;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public String getSpotId() {
        return spotId;
    }

    @Override
    public String toString() {
        return generateReceiptText();
    }
}
