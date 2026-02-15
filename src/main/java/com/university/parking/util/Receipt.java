package com.university.parking.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.university.parking.model.PaymentMethod;
import com.university.parking.model.SpotType;
import com.university.parking.model.VehicleType;

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
    private double changeAmount;
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentDate;
    private String spotId;
    private boolean isPrepaidReservation;
    private boolean isWithinGracePeriod;
    private boolean isCardHolder;
    private VehicleType vehicleType;
    private SpotType spotType;
    private double spotRate;

    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Receipt(String licensePlate, LocalDateTime entryTime, LocalDateTime exitTime,
                   long durationHours, double parkingFee, double fineAmount,
                   double amountPaid, PaymentMethod paymentMethod, String spotId) {
        this(licensePlate, entryTime, exitTime, durationHours, parkingFee, fineAmount, 
             amountPaid, paymentMethod, spotId, false, false, false, null, null, 0.0);
    }

    public Receipt(String licensePlate, LocalDateTime entryTime, LocalDateTime exitTime,
                   long durationHours, double parkingFee, double fineAmount,
                   double amountPaid, PaymentMethod paymentMethod, String spotId, 
                   boolean isPrepaidReservation) {
        this(licensePlate, entryTime, exitTime, durationHours, parkingFee, fineAmount, 
             amountPaid, paymentMethod, spotId, isPrepaidReservation, false, false, null, null, 0.0);
    }

    public Receipt(String licensePlate, LocalDateTime entryTime, LocalDateTime exitTime,
                   long durationHours, double parkingFee, double fineAmount,
                   double amountPaid, PaymentMethod paymentMethod, String spotId, 
                   boolean isPrepaidReservation, boolean isWithinGracePeriod) {
        this(licensePlate, entryTime, exitTime, durationHours, parkingFee, fineAmount, 
             amountPaid, paymentMethod, spotId, isPrepaidReservation, isWithinGracePeriod, false, null, null, 0.0);
    }

    public Receipt(String licensePlate, LocalDateTime entryTime, LocalDateTime exitTime,
                   long durationHours, double parkingFee, double fineAmount,
                   double amountPaid, PaymentMethod paymentMethod, String spotId, 
                   boolean isPrepaidReservation, boolean isWithinGracePeriod, boolean isCardHolder) {
        this(licensePlate, entryTime, exitTime, durationHours, parkingFee, fineAmount, 
             amountPaid, paymentMethod, spotId, isPrepaidReservation, isWithinGracePeriod, isCardHolder, null, null, 0.0);
    }

    public Receipt(String licensePlate, LocalDateTime entryTime, LocalDateTime exitTime,
                   long durationHours, double parkingFee, double fineAmount,
                   double amountPaid, PaymentMethod paymentMethod, String spotId, 
                   boolean isPrepaidReservation, boolean isWithinGracePeriod, boolean isCardHolder,
                   VehicleType vehicleType, SpotType spotType, double spotRate) {
        this.licensePlate = licensePlate;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.durationHours = durationHours;
        this.parkingFee = parkingFee;
        this.fineAmount = fineAmount;
        this.totalAmount = parkingFee + fineAmount;
        this.amountPaid = amountPaid;
        this.remainingBalance = Math.max(0, totalAmount - amountPaid);
        this.changeAmount = Math.max(0, amountPaid - totalAmount);
        this.paymentMethod = paymentMethod;
        this.paymentDate = LocalDateTime.now();
        this.spotId = spotId;
        this.isPrepaidReservation = isPrepaidReservation;
        this.isWithinGracePeriod = isWithinGracePeriod;
        this.isCardHolder = isCardHolder;
        this.vehicleType = vehicleType;
        this.spotType = spotType;
        this.spotRate = spotRate;
    }

    /**
     * Generates a formatted receipt string with modern, beautiful formatting.
     * @return formatted receipt text
     */
    public String generateReceiptText() {
        StringBuilder receipt = new StringBuilder();
        
        // Header
        receipt.append("========================================\n");
        receipt.append("       UNIVERSITY PARKING LOT\n");
        receipt.append("          PAYMENT RECEIPT\n");
        receipt.append("========================================\n");
        receipt.append("\n");
        
        // Vehicle Information
        receipt.append("License Plate : ").append(licensePlate).append("\n");
        if (vehicleType != null) {
            receipt.append("Vehicle Type  : ").append(vehicleType).append("\n");
        }
        receipt.append("Card Holder   : ").append(isCardHolder ? "YES" : "NO").append("\n");
        receipt.append("Parking Spot  : ").append(spotId).append("\n");
        if (spotType != null) {
            receipt.append("Spot Type     : ").append(spotType).append("\n");
        }
        if (spotRate > 0) {
            receipt.append("Spot Rate     : RM ").append(String.format("%.2f", spotRate)).append("/hr\n");
        }
        receipt.append("\n");
        
        // Parking Duration
        receipt.append("Entry Time    : ").append(entryTime.format(DATE_FORMATTER)).append("\n");
        receipt.append("Exit Time     : ").append(exitTime.format(DATE_FORMATTER)).append("\n");
        receipt.append("Duration      : ").append(durationHours).append(" hour(s)\n");
        receipt.append("\n");
        
        // Charges Breakdown
        receipt.append("----------------------------------------\n");
        if (isPrepaidReservation) {
            receipt.append(String.format("Parking Fee   : RM %.2f (PREPAID)\n", parkingFee));
        } else if (isWithinGracePeriod) {
            receipt.append(String.format("Parking Fee   : RM %.2f (15-MIN GRACE)\n", parkingFee));
        } else {
            receipt.append(String.format("Parking Fee   : RM %.2f\n", parkingFee));
        }
        if (fineAmount > 0) {
            receipt.append(String.format("Fines         : RM %.2f\n", fineAmount));
        }
        receipt.append("----------------------------------------\n");
        receipt.append(String.format("TOTAL AMOUNT  : RM %.2f\n", totalAmount));
        receipt.append("========================================\n");
        receipt.append("\n");
        
        // Payment Information
        receipt.append(String.format("Amount Paid   : RM %.2f\n", amountPaid));
        receipt.append("Payment Method: ").append(paymentMethod).append("\n");
        
        if (remainingBalance > 0) {
            receipt.append(String.format("BALANCE DUE   : RM %.2f\n", remainingBalance));
        } else if (changeAmount > 0) {
            receipt.append(String.format("CHANGE        : RM %.2f\n", changeAmount));
            receipt.append("Status        : PAID IN FULL\n");
        } else {
            receipt.append("Status        : PAID IN FULL\n");
        }
        
        receipt.append("Payment Date  : ").append(paymentDate.format(DATE_FORMATTER)).append("\n");
        receipt.append("========================================\n");
        receipt.append("\n");
        
        // Footer
        receipt.append("     Thank you for parking with us!\n");
        receipt.append("     Have a safe journey!\n");
        receipt.append("\n");
        receipt.append("  For inquiries: chiushiaoying@student.mmu.edu.my\n");
        
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

    public double getChangeAmount() {
        return changeAmount;
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

    public boolean isPrepaidReservation() {
        return isPrepaidReservation;
    }

    public boolean isWithinGracePeriod() {
        return isWithinGracePeriod;
    }

    public boolean isCardHolder() {
        return isCardHolder;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public double getSpotRate() {
        return spotRate;
    }

    @Override
    public String toString() {
        return generateReceiptText();
    }
}
