package com.university.parking.util;

import org.junit.jupiter.api.Assertions;

import com.university.parking.model.PaymentMethod;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.DoubleRange;

/**
 * Property-based tests for payment validation.
 * Feature: parking-lot-management, Property 11: Payment Validation
 * Validates: Requirements 6.4, 6.5
 */
public class PaymentValidationProperties {

    /**
     * Property 11: Payment Validation (Part 1)
     * For any payment transaction, the system should validate that the payment amount 
     * covers the total charges (parking fee + fines).
     */
    @Property(tries = 100)
    void paymentValidationAcceptsSufficientPayment(
            @ForAll @DoubleRange(min = 0.0, max = 1000.0) double parkingFee,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double fineAmount,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double extraAmount
    ) {
        double totalCharges = parkingFee + fineAmount;
        double amountPaid = totalCharges + extraAmount; // Always sufficient or exact

        boolean isValid = PaymentProcessor.validatePayment(amountPaid, totalCharges);

        Assertions.assertTrue(isValid,
            String.format("Payment validation should accept sufficient payment: " +
                "paid %.2f for total charges %.2f", amountPaid, totalCharges));
    }

    /**
     * Property 11: Payment Validation (Part 2)
     * For any payment transaction, when payment is insufficient, 
     * the system should display the remaining balance due.
     */
    @Property(tries = 100)
    void paymentValidationRejectsInsufficientPayment(
            @ForAll @DoubleRange(min = 10.0, max = 1000.0) double parkingFee,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double fineAmount,
            @ForAll @DoubleRange(min = 0.01, max = 0.99) double shortfallRatio
    ) {
        double totalCharges = parkingFee + fineAmount;
        // Calculate shortfall as a percentage of total charges to ensure amountPaid stays positive
        double shortfall = totalCharges * shortfallRatio;
        double amountPaid = totalCharges - shortfall; // Always insufficient but positive

        boolean isValid = PaymentProcessor.validatePayment(amountPaid, totalCharges);

        Assertions.assertFalse(isValid,
            String.format("Payment validation should reject insufficient payment: " +
                "paid %.2f for total charges %.2f", amountPaid, totalCharges));
    }

    /**
     * Property test for remaining balance calculation.
     * The remaining balance should always equal max(0, totalCharges - amountPaid).
     */
    @Property(tries = 100)
    void remainingBalanceCalculationIsCorrect(
            @ForAll @DoubleRange(min = 0.0, max = 1000.0) double parkingFee,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double fineAmount,
            @ForAll @DoubleRange(min = 0.0, max = 2000.0) double amountPaid
    ) {
        double totalCharges = parkingFee + fineAmount;
        double remainingBalance = PaymentProcessor.calculateRemainingBalance(amountPaid, totalCharges);

        double expectedBalance = Math.max(0, totalCharges - amountPaid);

        Assertions.assertEquals(expectedBalance, remainingBalance, 0.01,
            String.format("Remaining balance calculation incorrect: " +
                "expected %.2f but got %.2f (total: %.2f, paid: %.2f)",
                expectedBalance, remainingBalance, totalCharges, amountPaid));
    }

    /**
     * Property test for exact payment (no remaining balance).
     */
    @Property(tries = 100)
    void exactPaymentHasZeroRemainingBalance(
            @ForAll @DoubleRange(min = 0.0, max = 1000.0) double parkingFee,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double fineAmount
    ) {
        double totalCharges = parkingFee + fineAmount;
        double amountPaid = totalCharges; // Exact payment

        double remainingBalance = PaymentProcessor.calculateRemainingBalance(amountPaid, totalCharges);

        Assertions.assertEquals(0.0, remainingBalance, 0.01,
            String.format("Exact payment should have zero remaining balance: " +
                "paid %.2f for total charges %.2f, but remaining balance is %.2f",
                amountPaid, totalCharges, remainingBalance));
    }

    /**
     * Property test for overpayment (should still have zero remaining balance).
     */
    @Property(tries = 100)
    void overpaymentHasZeroRemainingBalance(
            @ForAll @DoubleRange(min = 0.0, max = 1000.0) double parkingFee,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double fineAmount,
            @ForAll @DoubleRange(min = 0.1, max = 100.0) double extraAmount
    ) {
        double totalCharges = parkingFee + fineAmount;
        double amountPaid = totalCharges + extraAmount; // Overpayment

        double remainingBalance = PaymentProcessor.calculateRemainingBalance(amountPaid, totalCharges);

        Assertions.assertEquals(0.0, remainingBalance, 0.01,
            String.format("Overpayment should have zero remaining balance: " +
                "paid %.2f for total charges %.2f, but remaining balance is %.2f",
                amountPaid, totalCharges, remainingBalance));
    }

    /**
     * Property test for payment method validation.
     */
    @Property(tries = 100)
    void validPaymentMethodsAreAccepted(@ForAll PaymentMethod paymentMethod) {
        boolean isValid = PaymentProcessor.isValidPaymentMethod(paymentMethod);

        Assertions.assertTrue(isValid,
            String.format("Payment method %s should be valid", paymentMethod));
    }

    /**
     * Property test for payment processing with valid inputs.
     */
    @Property(tries = 100)
    void paymentProcessingCreatesValidPaymentRecord(
            @ForAll("licensePlates") String licensePlate,
            @ForAll @DoubleRange(min = 0.0, max = 1000.0) double parkingFee,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double fineAmount,
            @ForAll @DoubleRange(min = 0.0, max = 2000.0) double amountPaid,
            @ForAll PaymentMethod paymentMethod
    ) {
        var payment = PaymentProcessor.processPayment(
            licensePlate, parkingFee, fineAmount, amountPaid, paymentMethod);

        Assertions.assertNotNull(payment, "Payment record should not be null");
        Assertions.assertEquals(licensePlate, payment.getLicensePlate());
        Assertions.assertEquals(parkingFee, payment.getParkingFee(), 0.01);
        Assertions.assertEquals(fineAmount, payment.getFineAmount(), 0.01);
        Assertions.assertEquals(parkingFee + fineAmount, payment.getTotalAmount(), 0.01);
        Assertions.assertEquals(paymentMethod, payment.getPaymentMethod());
        Assertions.assertNotNull(payment.getPaymentDate());
    }

    @Provide
    Arbitrary<String> licensePlates() {
        return Arbitraries.strings()
            .alpha().numeric()
            .ofMinLength(3)
            .ofMaxLength(10)
            .map(String::toUpperCase);
    }
}
