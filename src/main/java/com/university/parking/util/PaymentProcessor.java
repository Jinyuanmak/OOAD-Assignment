package com.university.parking.util;

import java.time.LocalDateTime;

import com.university.parking.model.Payment;
import com.university.parking.model.PaymentMethod;
import com.university.parking.model.SpotType;
import com.university.parking.model.VehicleType;

/**
 * Payment processing system for the parking lot.
 * Handles cash and card payments, validates payment amounts, and generates receipts.
 * Requirements: 6.1, 6.2, 6.4, 6.5
 */
public class PaymentProcessor {

    /**
     * Validates that the payment amount is sufficient to cover the total charges.
     * 
     * @param amountPaid the amount being paid
     * @param totalCharges the total amount due (parking fee + fines)
     * @return true if payment is sufficient, false otherwise
     */
    public static boolean validatePayment(double amountPaid, double totalCharges) {
        if (amountPaid < 0 || totalCharges < 0) {
            throw new IllegalArgumentException("Payment amounts cannot be negative");
        }
        return amountPaid >= totalCharges;
    }

    /**
     * Calculates the remaining balance after payment.
     * 
     * @param amountPaid the amount being paid
     * @param totalCharges the total amount due
     * @return the remaining balance (0 if fully paid, positive if underpaid)
     */
    public static double calculateRemainingBalance(double amountPaid, double totalCharges) {
        if (amountPaid < 0 || totalCharges < 0) {
            throw new IllegalArgumentException("Payment amounts cannot be negative");
        }
        return Math.max(0, totalCharges - amountPaid);
    }

    /**
     * Processes a payment transaction.
     * Validates the payment method and amount, then creates a Payment record.
     * 
     * @param licensePlate the vehicle's license plate
     * @param parkingFee the parking fee amount
     * @param fineAmount the total fine amount
     * @param amountPaid the amount being paid
     * @param paymentMethod the payment method (CASH or CARD)
     * @return a Payment object representing the transaction
     * @throws IllegalArgumentException if payment method is invalid or amounts are negative
     */
    public static Payment processPayment(String licensePlate, double parkingFee, 
                                        double fineAmount, double amountPaid,
                                        PaymentMethod paymentMethod) {
        // Validate inputs
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate cannot be null or empty");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }
        if (parkingFee < 0 || fineAmount < 0 || amountPaid < 0) {
            throw new IllegalArgumentException("Payment amounts cannot be negative");
        }

        // Create payment record
        Payment payment = new Payment(licensePlate, parkingFee, fineAmount, paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        
        // Note: The Payment model already calculates totalAmount in its constructor
        // The actual amount paid might be less than total, creating a remaining balance
        
        return payment;
    }

    /**
     * Generates a receipt for a completed payment transaction.
     * 
     * @param licensePlate the vehicle's license plate
     * @param entryTime the parking entry time
     * @param exitTime the parking exit time
     * @param durationHours the parking duration in hours
     * @param parkingFee the parking fee
     * @param fineAmount the total fine amount
     * @param amountPaid the amount paid
     * @param paymentMethod the payment method used
     * @param spotId the parking spot ID
     * @return a Receipt object containing all transaction details
     */
    public static Receipt generateReceipt(String licensePlate, LocalDateTime entryTime,
                                         LocalDateTime exitTime, long durationHours,
                                         double parkingFee, double fineAmount,
                                         double amountPaid, PaymentMethod paymentMethod,
                                         String spotId) {
        return generateReceipt(licensePlate, entryTime, exitTime, durationHours,
                              parkingFee, fineAmount, amountPaid, paymentMethod, spotId, false, false, false, null, null, 0.0);
    }

    /**
     * Generates a receipt for a completed payment transaction with prepaid reservation flag.
     * 
     * @param licensePlate the vehicle's license plate
     * @param entryTime the parking entry time
     * @param exitTime the parking exit time
     * @param durationHours the parking duration in hours
     * @param parkingFee the parking fee
     * @param fineAmount the total fine amount
     * @param amountPaid the amount paid
     * @param paymentMethod the payment method used
     * @param spotId the parking spot ID
     * @param isPrepaidReservation whether this is a prepaid reservation
     * @return a Receipt object containing all transaction details
     */
    public static Receipt generateReceipt(String licensePlate, LocalDateTime entryTime,
                                         LocalDateTime exitTime, long durationHours,
                                         double parkingFee, double fineAmount,
                                         double amountPaid, PaymentMethod paymentMethod,
                                         String spotId, boolean isPrepaidReservation) {
        return generateReceipt(licensePlate, entryTime, exitTime, durationHours,
                              parkingFee, fineAmount, amountPaid, paymentMethod, spotId, 
                              isPrepaidReservation, false, false, null, null, 0.0);
    }

    /**
     * Generates a receipt for a completed payment transaction with all flags.
     * 
     * @param licensePlate the vehicle's license plate
     * @param entryTime the parking entry time
     * @param exitTime the parking exit time
     * @param durationHours the parking duration in hours
     * @param parkingFee the parking fee
     * @param fineAmount the total fine amount
     * @param amountPaid the amount paid
     * @param paymentMethod the payment method used
     * @param spotId the parking spot ID
     * @param isPrepaidReservation whether this is a prepaid reservation
     * @param isWithinGracePeriod whether this is within 15-minute grace period
     * @param isCardHolder whether the vehicle has handicapped card holder status
     * @return a Receipt object containing all transaction details
     */
    public static Receipt generateReceipt(String licensePlate, LocalDateTime entryTime,
                                         LocalDateTime exitTime, long durationHours,
                                         double parkingFee, double fineAmount,
                                         double amountPaid, PaymentMethod paymentMethod,
                                         String spotId, boolean isPrepaidReservation,
                                         boolean isWithinGracePeriod, boolean isCardHolder) {
        return generateReceipt(licensePlate, entryTime, exitTime, durationHours,
                              parkingFee, fineAmount, amountPaid, paymentMethod, spotId, 
                              isPrepaidReservation, isWithinGracePeriod, isCardHolder, null, null, 0.0);
    }

    /**
     * Generates a receipt for a completed payment transaction with all details.
     * 
     * @param licensePlate the vehicle's license plate
     * @param entryTime the parking entry time
     * @param exitTime the parking exit time
     * @param durationHours the parking duration in hours
     * @param parkingFee the parking fee
     * @param fineAmount the total fine amount
     * @param amountPaid the amount paid
     * @param paymentMethod the payment method used
     * @param spotId the parking spot ID
     * @param isPrepaidReservation whether this is a prepaid reservation
     * @param isWithinGracePeriod whether this is within 15-minute grace period
     * @param isCardHolder whether the vehicle has handicapped card holder status
     * @param vehicleType the type of vehicle
     * @param spotType the type of parking spot
     * @param spotRate the hourly rate of the spot
     * @return a Receipt object containing all transaction details
     */
    public static Receipt generateReceipt(String licensePlate, LocalDateTime entryTime,
                                         LocalDateTime exitTime, long durationHours,
                                         double parkingFee, double fineAmount,
                                         double amountPaid, PaymentMethod paymentMethod,
                                         String spotId, boolean isPrepaidReservation,
                                         boolean isWithinGracePeriod, boolean isCardHolder,
                                         VehicleType vehicleType, SpotType spotType, double spotRate) {
        return new Receipt(licensePlate, entryTime, exitTime, durationHours,
                          parkingFee, fineAmount, amountPaid, paymentMethod, spotId, 
                          isPrepaidReservation, isWithinGracePeriod, isCardHolder,
                          vehicleType, spotType, spotRate);
    }

    /**
     * Validates that a payment method is supported.
     * 
     * @param paymentMethod the payment method to validate
     * @return true if the payment method is CASH or CARD
     */
    public static boolean isValidPaymentMethod(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.CASH || paymentMethod == PaymentMethod.CARD;
    }
}
