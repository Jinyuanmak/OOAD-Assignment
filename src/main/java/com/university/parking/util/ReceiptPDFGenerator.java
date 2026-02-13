package com.university.parking.util;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * Utility class for generating PDF receipts using Apache PDFBox.
 */
public class ReceiptPDFGenerator {
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Generates a PDF receipt and saves it to the specified file path.
     * 
     * @param receipt The receipt object containing all transaction details
     * @param filePath The file path where the PDF should be saved
     * @throws IOException If there's an error creating or writing the PDF
     */
    public static void generatePDF(Receipt receipt, String filePath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yPosition = page.getMediaBox().getHeight() - margin;
                float fontSize = 12;
                float leading = 1.5f * fontSize;
                
                // Header
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("UNIVERSITY PARKING LOT");
                contentStream.endText();
                yPosition -= leading * 1.5f;
                
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("PAYMENT RECEIPT");
                contentStream.endText();
                yPosition -= leading * 2;
                
                // Draw line
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= leading;
                
                // Vehicle Information
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("VEHICLE INFORMATION");
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("License Plate: " + receipt.getLicensePlate());
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Parking Spot: " + receipt.getSpotId());
                contentStream.endText();
                yPosition -= leading * 1.5f;
                
                // Parking Duration
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("PARKING DURATION");
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Entry Time: " + receipt.getEntryTime().format(DATE_FORMATTER));
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Exit Time: " + receipt.getExitTime().format(DATE_FORMATTER));
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Duration: " + receipt.getDurationHours() + " hour(s)");
                contentStream.endText();
                yPosition -= leading * 1.5f;
                
                // Charges
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("CHARGES");
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                if (receipt.isPrepaidReservation()) {
                    contentStream.showText(String.format("Parking Fee: RM %.2f (PREPAID)", receipt.getParkingFee()));
                } else if (receipt.isWithinGracePeriod()) {
                    contentStream.showText(String.format("Parking Fee: RM %.2f (15-MIN GRACE)", receipt.getParkingFee()));
                } else {
                    contentStream.showText(String.format("Parking Fee: RM %.2f", receipt.getParkingFee()));
                }
                contentStream.endText();
                yPosition -= leading;
                
                if (receipt.getFineAmount() > 0) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(String.format("Fines: RM %.2f", receipt.getFineAmount()));
                    contentStream.endText();
                    yPosition -= leading;
                }
                
                // Draw line
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= leading;
                
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(String.format("TOTAL AMOUNT: RM %.2f", receipt.getTotalAmount()));
                contentStream.endText();
                yPosition -= leading * 1.5f;
                
                // Draw line
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= leading;
                
                // Payment Details
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("PAYMENT DETAILS");
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(String.format("Amount Paid: RM %.2f", receipt.getAmountPaid()));
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Payment Method: " + receipt.getPaymentMethod());
                contentStream.endText();
                yPosition -= leading;
                
                if (receipt.getRemainingBalance() > 0) {
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(String.format("BALANCE DUE: RM %.2f", receipt.getRemainingBalance()));
                    contentStream.endText();
                    yPosition -= leading;
                } else if (receipt.getChangeAmount() > 0) {
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(String.format("CHANGE: RM %.2f", receipt.getChangeAmount()));
                    contentStream.endText();
                    yPosition -= leading;
                    
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Status: PAID IN FULL");
                    contentStream.endText();
                    yPosition -= leading;
                } else {
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Status: PAID IN FULL");
                    contentStream.endText();
                    yPosition -= leading;
                }
                
                contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Payment Date: " + receipt.getPaymentDate().format(DATE_FORMATTER));
                contentStream.endText();
                yPosition -= leading * 2;
                
                // Draw line
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= leading * 1.5f;
                
                // Footer - Centered
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 11);
                
                // Calculate center position for each line
                String thankYouText = "Thank you for parking with us!";
                float thankYouWidth = PDType1Font.HELVETICA_OBLIQUE.getStringWidth(thankYouText) / 1000 * 11;
                float thankYouX = (page.getMediaBox().getWidth() - thankYouWidth) / 2;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(thankYouX, yPosition);
                contentStream.showText(thankYouText);
                contentStream.endText();
                yPosition -= leading;
                
                String journeyText = "Have a safe journey!";
                float journeyWidth = PDType1Font.HELVETICA_OBLIQUE.getStringWidth(journeyText) / 1000 * 11;
                float journeyX = (page.getMediaBox().getWidth() - journeyWidth) / 2;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(journeyX, yPosition);
                contentStream.showText(journeyText);
                contentStream.endText();
                yPosition -= leading * 1.5f;
                
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                String inquiryText = "For inquiries: chiushiaoying@student.mmu.edu.my";
                float inquiryWidth = PDType1Font.HELVETICA.getStringWidth(inquiryText) / 1000 * 10;
                float inquiryX = (page.getMediaBox().getWidth() - inquiryWidth) / 2;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(inquiryX, yPosition);
                contentStream.showText(inquiryText);
                contentStream.endText();
            }
            
            document.save(filePath);
        }
    }
}
