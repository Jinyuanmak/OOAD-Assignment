package com.university.parking.util;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;

import com.university.parking.model.PaymentMethod;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for receipt content completeness.
 * Feature: parking-lot-management, Property 12: Receipt Content Completeness
 * Validates: Requirements 6.3
 */
public class ReceiptContentCompletenessProperties {

    /**
     * Property 12: Receipt Content Completeness
     * For any completed payment, the generated receipt should contain entry time, 
     * exit time, duration, fee breakdown, fines, total paid, payment method, 
     * and remaining balance.
     */
    @Property(tries = 100)
    void receiptContainsAllRequiredInformation(
            @ForAll("licensePlates") String licensePlate,
            @ForAll @IntRange(min = 1, max = 168) int durationHours,
            @ForAll @DoubleRange(min = 0.0, max = 1000.0) double parkingFee,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double fineAmount,
            @ForAll @DoubleRange(min = 0.0, max = 2000.0) double amountPaid,
            @ForAll PaymentMethod paymentMethod,
            @ForAll("spotIds") String spotId
    ) {
        // Create entry and exit times
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime exitTime = entryTime.plusHours(durationHours);

        // Generate receipt
        Receipt receipt = PaymentProcessor.generateReceipt(
            licensePlate, entryTime, exitTime, durationHours,
            parkingFee, fineAmount, amountPaid, paymentMethod, spotId
        );

        // Verify all required fields are present
        Assertions.assertNotNull(receipt, "Receipt should not be null");
        Assertions.assertEquals(licensePlate, receipt.getLicensePlate());
        Assertions.assertEquals(entryTime, receipt.getEntryTime());
        Assertions.assertEquals(exitTime, receipt.getExitTime());
        Assertions.assertEquals(durationHours, receipt.getDurationHours());
        Assertions.assertEquals(parkingFee, receipt.getParkingFee(), 0.01);
        Assertions.assertEquals(fineAmount, receipt.getFineAmount(), 0.01);
        Assertions.assertEquals(parkingFee + fineAmount, receipt.getTotalAmount(), 0.01);
        Assertions.assertEquals(amountPaid, receipt.getAmountPaid(), 0.01);
        Assertions.assertEquals(paymentMethod, receipt.getPaymentMethod());
        Assertions.assertEquals(spotId, receipt.getSpotId());
        Assertions.assertNotNull(receipt.getPaymentDate());

        // Verify remaining balance calculation
        double expectedBalance = Math.max(0, (parkingFee + fineAmount) - amountPaid);
        Assertions.assertEquals(expectedBalance, receipt.getRemainingBalance(), 0.01);
    }

    /**
     * Property test to verify receipt text contains all required information.
     */
    @Property(tries = 100)
    void receiptTextContainsAllRequiredFields(
            @ForAll("licensePlates") String licensePlate,
            @ForAll @IntRange(min = 1, max = 168) int durationHours,
            @ForAll @DoubleRange(min = 0.0, max = 1000.0) double parkingFee,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double fineAmount,
            @ForAll @DoubleRange(min = 0.0, max = 2000.0) double amountPaid,
            @ForAll PaymentMethod paymentMethod,
            @ForAll("spotIds") String spotId
    ) {
        LocalDateTime entryTime = LocalDateTime.of(2024, 6, 15, 14, 30);
        LocalDateTime exitTime = entryTime.plusHours(durationHours);

        Receipt receipt = PaymentProcessor.generateReceipt(
            licensePlate, entryTime, exitTime, durationHours,
            parkingFee, fineAmount, amountPaid, paymentMethod, spotId
        );

        String receiptText = receipt.generateReceiptText();

        // Verify receipt text contains all required information
        Assertions.assertTrue(receiptText.contains(licensePlate),
            "Receipt should contain license plate");
        Assertions.assertTrue(receiptText.contains(spotId),
            "Receipt should contain spot ID");
        Assertions.assertTrue(receiptText.contains("Entry Time"),
            "Receipt should contain entry time label");
        Assertions.assertTrue(receiptText.contains("Exit Time"),
            "Receipt should contain exit time label");
        Assertions.assertTrue(receiptText.contains("Duration"),
            "Receipt should contain duration label");
        Assertions.assertTrue(receiptText.contains("Parking Fee"),
            "Receipt should contain parking fee label");
        Assertions.assertTrue(receiptText.contains("Fines"),
            "Receipt should contain fines label");
        Assertions.assertTrue(receiptText.contains("Total Amount"),
            "Receipt should contain total amount label");
        Assertions.assertTrue(receiptText.contains("Amount Paid"),
            "Receipt should contain amount paid label");
        Assertions.assertTrue(receiptText.contains("Payment Method"),
            "Receipt should contain payment method label");
        Assertions.assertTrue(receiptText.contains("Remaining Balance"),
            "Receipt should contain remaining balance label");
        Assertions.assertTrue(receiptText.contains("Payment Date"),
            "Receipt should contain payment date label");
    }

    /**
     * Property test to verify receipt formatting is consistent.
     */
    @Property(tries = 100)
    void receiptFormattingIsConsistent(
            @ForAll("licensePlates") String licensePlate,
            @ForAll @IntRange(min = 1, max = 24) int durationHours,
            @ForAll @DoubleRange(min = 10.0, max = 500.0) double parkingFee,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double fineAmount,
            @ForAll @DoubleRange(min = 10.0, max = 600.0) double amountPaid,
            @ForAll PaymentMethod paymentMethod,
            @ForAll("spotIds") String spotId
    ) {
        LocalDateTime entryTime = LocalDateTime.of(2024, 3, 20, 8, 15);
        LocalDateTime exitTime = entryTime.plusHours(durationHours);

        Receipt receipt = PaymentProcessor.generateReceipt(
            licensePlate, entryTime, exitTime, durationHours,
            parkingFee, fineAmount, amountPaid, paymentMethod, spotId
        );

        String receiptText = receipt.generateReceiptText();

        // Verify receipt has proper structure
        Assertions.assertTrue(receiptText.contains("PARKING RECEIPT"),
            "Receipt should have a title");
        Assertions.assertTrue(receiptText.contains("========"),
            "Receipt should have separator lines");
        Assertions.assertTrue(receiptText.contains("Thank you"),
            "Receipt should have a thank you message");
        
        // Verify receipt is not empty
        Assertions.assertTrue(receiptText.length() > 100,
            "Receipt should have substantial content");
    }

    @Provide
    Arbitrary<String> licensePlates() {
        return Arbitraries.strings()
            .alpha().numeric()
            .ofMinLength(3)
            .ofMaxLength(10)
            .map(String::toUpperCase);
    }

    @Provide
    Arbitrary<String> spotIds() {
        return Arbitraries.integers().between(1, 5)
            .flatMap(floor -> Arbitraries.integers().between(1, 10)
                .flatMap(row -> Arbitraries.integers().between(1, 20)
                    .map(spot -> String.format("F%d-R%d-S%d", floor, row, spot))));
    }
}
