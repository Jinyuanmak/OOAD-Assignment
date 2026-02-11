package com.university.parking.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.university.parking.model.Fine;
import com.university.parking.model.Floor;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.SpotType;
import com.university.parking.model.Vehicle;

/**
 * Utility class for exporting reports in various formats (TXT, PDF, CSV).
 * Supports exporting vehicle, revenue, occupancy, and fine reports.
 */
public class ReportExporter {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    public enum ReportType {
        VEHICLE("Current_Vehicles"),
        REVENUE("Revenue_Report"),
        OCCUPANCY("Occupancy_Report"),
        FINE("Fine_Report");
        
        private final String fileName;
        
        ReportType(String fileName) {
            this.fileName = fileName;
        }
        
        public String getFileName() {
            return fileName;
        }
    }
    
    public enum ExportFormat {
        TXT(".txt"),
        PDF(".pdf"),
        CSV(".csv");
        
        private final String extension;
        
        ExportFormat(String extension) {
            this.extension = extension;
        }
        
        public String getExtension() {
            return extension;
        }
    }
    
    /**
     * Exports a report to the specified format.
     */
    public static File exportReport(ReportType reportType, ExportFormat format, 
                                   ParkingLot parkingLot, List<Fine> fines, 
                                   String outputDirectory) throws IOException {
        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        String fileName = reportType.getFileName() + "_" + timestamp + format.getExtension();
        File outputFile = new File(outputDirectory, fileName);
        
        switch (format) {
            case TXT:
                exportToTxt(reportType, parkingLot, fines, outputFile);
                break;
            case PDF:
                exportToPdf(reportType, parkingLot, fines, outputFile);
                break;
            case CSV:
                exportToCsv(reportType, parkingLot, fines, outputFile);
                break;
        }
        
        return outputFile;
    }
    
    /**
     * Exports report to TXT format.
     */
    private static void exportToTxt(ReportType reportType, ParkingLot parkingLot, 
                                    List<Fine> fines, File outputFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            String content = generateReportContent(reportType, parkingLot, fines);
            writer.write(content);
        }
    }
    
    /**
     * Exports report to PDF format with proper formatting.
     */
    private static void exportToPdf(ReportType reportType, ParkingLot parkingLot, 
                                   List<Fine> fines, File outputFile) throws IOException {
        switch (reportType) {
            case VEHICLE:
                exportVehiclePdf(parkingLot, outputFile);
                break;
            case REVENUE:
                exportRevenuePdf(parkingLot, outputFile);
                break;
            case OCCUPANCY:
                exportOccupancyPdf(parkingLot, outputFile);
                break;
            case FINE:
                exportFinePdf(fines, outputFile);
                break;
        }
    }
    
    /**
     * Exports vehicle report to formatted PDF.
     */
    private static void exportVehiclePdf(ParkingLot parkingLot, File outputFile) throws IOException {
        PDDocument document = new PDDocument();
        try {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float fontSize = 12;
            float leading = 1.5f * fontSize;
            
            // Header
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("CURRENT VEHICLES REPORT");
            contentStream.endText();
            yPosition -= leading;
            
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Generated: " + LocalDateTime.now().format(DATE_FORMAT));
            contentStream.endText();
            yPosition -= leading * 1.5f;
            
            // Draw line
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            contentStream.stroke();
            yPosition -= leading;
            
            // Table header with fixed column positions
            float col1 = margin;           // No.
            float col2 = margin + 40;      // License Plate
            float col3 = margin + 150;     // Vehicle Type
            float col4 = margin + 280;     // Spot ID
            float col5 = margin + 370;     // Entry Time
            
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(col1, yPosition);
            contentStream.showText("No.");
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.newLineAtOffset(col2, yPosition);
            contentStream.showText("License Plate");
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.newLineAtOffset(col3, yPosition);
            contentStream.showText("Vehicle Type");
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.newLineAtOffset(col4, yPosition);
            contentStream.showText("Spot ID");
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.newLineAtOffset(col5, yPosition);
            contentStream.showText("Entry Time");
            contentStream.endText();
            yPosition -= leading;
            
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            contentStream.stroke();
            yPosition -= leading * 0.5f;
            
            // Vehicle data
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            int count = 0;
            for (Floor floor : parkingLot.getFloors()) {
                for (ParkingSpot spot : floor.getAllSpots()) {
                    if (!spot.isAvailable() && spot.getCurrentVehicle() != null) {
                        Vehicle vehicle = spot.getCurrentVehicle();
                        count++;
                        
                        if (yPosition < 100) {
                            contentStream.close();
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            contentStream = new PDPageContentStream(document, page);
                            contentStream.setFont(PDType1Font.HELVETICA, 10);
                            yPosition = page.getMediaBox().getHeight() - margin;
                        }
                        
                        contentStream.beginText();
                        contentStream.newLineAtOffset(col1, yPosition);
                        contentStream.showText(String.valueOf(count));
                        contentStream.endText();
                        
                        contentStream.beginText();
                        contentStream.newLineAtOffset(col2, yPosition);
                        contentStream.showText(vehicle.getLicensePlate());
                        contentStream.endText();
                        
                        contentStream.beginText();
                        contentStream.newLineAtOffset(col3, yPosition);
                        contentStream.showText(vehicle.getType().toString());
                        contentStream.endText();
                        
                        contentStream.beginText();
                        contentStream.newLineAtOffset(col4, yPosition);
                        contentStream.showText(spot.getSpotId());
                        contentStream.endText();
                        
                        contentStream.beginText();
                        contentStream.newLineAtOffset(col5, yPosition);
                        contentStream.showText(vehicle.getEntryTime() != null ? 
                            vehicle.getEntryTime().format(DATE_FORMAT) : "N/A");
                        contentStream.endText();
                        
                        yPosition -= leading;
                    }
                }
            }
            
            if (count == 0) {
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("No vehicles currently parked.");
                contentStream.endText();
                yPosition -= leading;
            }
            
            yPosition -= leading;
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            contentStream.stroke();
            yPosition -= leading;
            
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Total Vehicles: " + count);
            contentStream.endText();
            
            contentStream.close();
            document.save(outputFile);
        } finally {
            document.close();
        }
    }
    
    /**
     * Exports revenue report to formatted PDF.
     */
    private static void exportRevenuePdf(ParkingLot parkingLot, File outputFile) throws IOException {
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
                contentStream.showText("REVENUE REPORT");
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Generated: " + LocalDateTime.now().format(DATE_FORMAT));
                contentStream.endText();
                yPosition -= leading * 1.5f;
                
                // Draw line
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= leading * 1.5f;
                
                // Total revenue
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(String.format("Total Revenue Collected: RM %.2f", parkingLot.getTotalRevenue()));
                contentStream.endText();
                yPosition -= leading * 2;
                
                // Revenue by floor
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Revenue by Floor:");
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= leading * 0.5f;
                
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                for (Floor floor : parkingLot.getFloors()) {
                    int occupied = floor.getAllSpots().size() - floor.getAvailableSpots().size();
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(String.format("Floor %d: %d vehicles currently parked",
                        floor.getFloorNumber(), occupied));
                    contentStream.endText();
                    yPosition -= leading;
                }
            }
            
            document.save(outputFile);
        }
    }
    
    /**
     * Exports occupancy report to formatted PDF.
     */
    private static void exportOccupancyPdf(ParkingLot parkingLot, File outputFile) throws IOException {
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
                contentStream.showText("OCCUPANCY REPORT");
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Generated: " + LocalDateTime.now().format(DATE_FORMAT));
                contentStream.endText();
                yPosition -= leading * 1.5f;
                
                // Draw line
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= leading;
                
                // Table header with fixed column positions
                float col1 = margin;           // Floor
                float col2 = margin + 80;      // Total
                float col3 = margin + 160;     // Available
                float col4 = margin + 260;     // Occupied
                float col5 = margin + 360;     // Occupancy %
                
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(col1, yPosition);
                contentStream.showText("Floor");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(col2, yPosition);
                contentStream.showText("Total");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(col3, yPosition);
                contentStream.showText("Available");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(col4, yPosition);
                contentStream.showText("Occupied");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(col5, yPosition);
                contentStream.showText("Occupancy %");
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= leading * 0.5f;
                
                // Floor data
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                int totalSpots = 0;
                int totalOccupied = 0;
                
                for (Floor floor : parkingLot.getFloors()) {
                    int total = floor.getAllSpots().size();
                    int available = floor.getAvailableSpots().size();
                    int occupied = total - available;
                    double occupancy = total > 0 ? (occupied * 100.0 / total) : 0;
                    
                    totalSpots += total;
                    totalOccupied += occupied;
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(col1, yPosition);
                    contentStream.showText(String.valueOf(floor.getFloorNumber()));
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(col2, yPosition);
                    contentStream.showText(String.valueOf(total));
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(col3, yPosition);
                    contentStream.showText(String.valueOf(available));
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(col4, yPosition);
                    contentStream.showText(String.valueOf(occupied));
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(col5, yPosition);
                    contentStream.showText(String.format("%.1f%%", occupancy));
                    contentStream.endText();
                    
                    yPosition -= leading;
                }
                
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= leading;
                
                double totalOccupancy = totalSpots > 0 ? (totalOccupied * 100.0 / totalSpots) : 0;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                
                contentStream.beginText();
                contentStream.newLineAtOffset(col1, yPosition);
                contentStream.showText("TOTAL");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(col2, yPosition);
                contentStream.showText(String.valueOf(totalSpots));
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(col3, yPosition);
                contentStream.showText(String.valueOf(totalSpots - totalOccupied));
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(col4, yPosition);
                contentStream.showText(String.valueOf(totalOccupied));
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(col5, yPosition);
                contentStream.showText(String.format("%.1f%%", totalOccupancy));
                contentStream.endText();
                yPosition -= leading * 2;
                
                // Occupancy by spot type
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Occupancy by Spot Type:");
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= leading * 0.5f;
                
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                for (SpotType type : SpotType.values()) {
                    int typeTotal = 0;
                    int typeOccupied = 0;
                    for (Floor floor : parkingLot.getFloors()) {
                        for (ParkingSpot spot : floor.getAllSpots()) {
                            if (spot.getType() == type) {
                                typeTotal++;
                                if (!spot.isAvailable()) {
                                    typeOccupied++;
                                }
                            }
                        }
                    }
                    double typeOccupancy = typeTotal > 0 ? (typeOccupied * 100.0 / typeTotal) : 0;
                    
                    // Spot Type name
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(type.toString());
                    contentStream.endText();
                    
                    // Colon
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin + 120, yPosition);
                    contentStream.showText(":");
                    contentStream.endText();
                    
                    // Occupied/Total
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin + 140, yPosition);
                    contentStream.showText(typeOccupied + "/" + typeTotal);
                    contentStream.endText();
                    
                    // Percentage
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin + 220, yPosition);
                    contentStream.showText(String.format("(%.1f%%)", typeOccupancy));
                    contentStream.endText();
                    
                    yPosition -= leading;
                }
            }
            
            document.save(outputFile);
        }
    }
    
    /**
     * Exports fine report to formatted PDF.
     */
    private static void exportFinePdf(List<Fine> fines, File outputFile) throws IOException {
        PDDocument document = new PDDocument();
        try {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float fontSize = 12;
            float leading = 1.5f * fontSize;
            
            // Header
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("FINE REPORT");
            contentStream.endText();
            yPosition -= leading;
            
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Generated: " + LocalDateTime.now().format(DATE_FORMAT));
            contentStream.endText();
            yPosition -= leading * 1.5f;
            
            // Draw line
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            contentStream.stroke();
            yPosition -= leading;
            
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Outstanding Fines:");
            contentStream.endText();
            yPosition -= leading;
            
            if (fines == null || fines.isEmpty()) {
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("No outstanding fines.");
                contentStream.endText();
            } else {
                // Table header with fixed column positions
                float col1 = margin;           // No.
                float col2 = margin + 40;      // License Plate
                float col3 = margin + 150;     // Fine Type
                float col4 = margin + 300;     // Amount
                float col5 = margin + 380;     // Issued Date
                
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(col1, yPosition);
                contentStream.showText("No.");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(col2, yPosition);
                contentStream.showText("License Plate");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(col3, yPosition);
                contentStream.showText("Fine Type");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(col4, yPosition);
                contentStream.showText("Amount");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(col5, yPosition);
                contentStream.showText("Issued Date");
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= leading * 0.5f;
                
                // Fine data
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                int count = 0;
                double totalAmount = 0;
                
                for (Fine fine : fines) {
                    count++;
                    totalAmount += fine.getAmount();
                    
                    if (yPosition < 100) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        yPosition = page.getMediaBox().getHeight() - margin;
                    }
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(col1, yPosition);
                    contentStream.showText(String.valueOf(count));
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(col2, yPosition);
                    contentStream.showText(fine.getLicensePlate());
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(col3, yPosition);
                    contentStream.showText(fine.getType().toString());
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(col4, yPosition);
                    contentStream.showText(String.format("RM %.2f", fine.getAmount()));
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(col5, yPosition);
                    contentStream.showText(fine.getIssuedDate().format(DATE_FORMAT));
                    contentStream.endText();
                    
                    yPosition -= leading;
                }
                
                yPosition -= leading;
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= leading;
                
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(String.format("Total Outstanding Fines: %d", count));
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(String.format("Total Amount: RM %.2f", totalAmount));
                contentStream.endText();
            }
            
            contentStream.close();
            document.save(outputFile);
        } finally {
            document.close();
        }
    }
    
    /**
     * Exports report to CSV format.
     */
    private static void exportToCsv(ReportType reportType, ParkingLot parkingLot, 
                                   List<Fine> fines, File outputFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            switch (reportType) {
                case VEHICLE:
                    exportVehicleCsv(parkingLot, writer);
                    break;
                case REVENUE:
                    exportRevenueCsv(parkingLot, writer);
                    break;
                case OCCUPANCY:
                    exportOccupancyCsv(parkingLot, writer);
                    break;
                case FINE:
                    exportFineCsv(fines, writer);
                    break;
            }
        }
    }
    
    /**
     * Generates report content as text.
     */
    private static String generateReportContent(ReportType reportType, ParkingLot parkingLot, 
                                               List<Fine> fines) {
        switch (reportType) {
            case VEHICLE:
                return generateVehicleReport(parkingLot);
            case REVENUE:
                return generateRevenueReport(parkingLot);
            case OCCUPANCY:
                return generateOccupancyReport(parkingLot);
            case FINE:
                return generateFineReport(parkingLot, fines);
            default:
                return "";
        }
    }
    
    private static String generateVehicleReport(ParkingLot parkingLot) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("         CURRENT VEHICLES REPORT\n");
        sb.append("         Generated: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n");
        sb.append("=".repeat(60)).append("\n\n");

        int count = 0;
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getAllSpots()) {
                if (!spot.isAvailable() && spot.getCurrentVehicle() != null) {
                    Vehicle vehicle = spot.getCurrentVehicle();
                    count++;
                    sb.append(String.format("%-5d %-15s %-12s %-12s %s\n",
                        count,
                        vehicle.getLicensePlate(),
                        vehicle.getType(),
                        spot.getSpotId(),
                        vehicle.getEntryTime() != null ? vehicle.getEntryTime().format(DATE_FORMAT) : "N/A"
                    ));
                }
            }
        }

        if (count == 0) {
            sb.append("No vehicles currently parked.\n");
        }

        sb.append("\n").append("-".repeat(60)).append("\n");
        sb.append("Total Vehicles: ").append(count).append("\n");
        sb.append("=".repeat(60));

        return sb.toString();
    }
    
    private static String generateRevenueReport(ParkingLot parkingLot) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("            REVENUE REPORT\n");
        sb.append("         Generated: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n");
        sb.append("=".repeat(60)).append("\n\n");

        sb.append("Total Revenue Collected: RM ").append(String.format("%.2f", parkingLot.getTotalRevenue())).append("\n\n");

        sb.append("Revenue by Floor:\n");
        sb.append("-".repeat(40)).append("\n");
        
        for (Floor floor : parkingLot.getFloors()) {
            int occupied = floor.getAllSpots().size() - floor.getAvailableSpots().size();
            sb.append(String.format("Floor %d: %d vehicles currently parked\n",
                floor.getFloorNumber(), occupied));
        }

        sb.append("\n").append("=".repeat(60));

        return sb.toString();
    }
    
    private static String generateOccupancyReport(ParkingLot parkingLot) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("           OCCUPANCY REPORT\n");
        sb.append("         Generated: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n");
        sb.append("=".repeat(60)).append("\n\n");

        int totalSpots = 0;
        int totalOccupied = 0;

        sb.append(String.format("%-10s %-12s %-12s %-12s %s\n",
            "Floor", "Total Spots", "Available", "Occupied", "Occupancy %"));
        sb.append("-".repeat(60)).append("\n");

        for (Floor floor : parkingLot.getFloors()) {
            int total = floor.getAllSpots().size();
            int available = floor.getAvailableSpots().size();
            int occupied = total - available;
            double occupancy = total > 0 ? (occupied * 100.0 / total) : 0;

            totalSpots += total;
            totalOccupied += occupied;

            sb.append(String.format("%-10d %-12d %-12d %-12d %.1f%%\n",
                floor.getFloorNumber(), total, available, occupied, occupancy));
        }

        sb.append("-".repeat(60)).append("\n");
        double totalOccupancy = totalSpots > 0 ? (totalOccupied * 100.0 / totalSpots) : 0;
        sb.append(String.format("%-10s %-12d %-12d %-12d %.1f%%\n",
            "TOTAL", totalSpots, totalSpots - totalOccupied, totalOccupied, totalOccupancy));

        sb.append("\n\nOccupancy by Spot Type:\n");
        sb.append("-".repeat(40)).append("\n");
        
        for (SpotType type : SpotType.values()) {
            int typeTotal = 0;
            int typeOccupied = 0;
            for (Floor floor : parkingLot.getFloors()) {
                for (ParkingSpot spot : floor.getAllSpots()) {
                    if (spot.getType() == type) {
                        typeTotal++;
                        if (!spot.isAvailable()) {
                            typeOccupied++;
                        }
                    }
                }
            }
            double typeOccupancy = typeTotal > 0 ? (typeOccupied * 100.0 / typeTotal) : 0;
            sb.append(String.format("%-15s: %d/%d (%.1f%%)\n",
                type, typeOccupied, typeTotal, typeOccupancy));
        }

        sb.append("\n").append("=".repeat(60));

        return sb.toString();
    }
    
    private static String generateFineReport(ParkingLot parkingLot, List<Fine> fines) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("             FINE REPORT\n");
        sb.append("         Generated: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n");
        sb.append("=".repeat(60)).append("\n\n");

        sb.append("Outstanding Fines:\n");
        sb.append("-".repeat(60)).append("\n");
        
        if (fines == null || fines.isEmpty()) {
            sb.append("No outstanding fines.\n");
        } else {
            sb.append(String.format("%-5s %-15s %-20s %-15s %s\n",
                "No.", "License Plate", "Fine Type", "Amount (RM)", "Issued Date"));
            sb.append("-".repeat(60)).append("\n");
            
            int count = 0;
            double totalAmount = 0;
            for (Fine fine : fines) {
                count++;
                totalAmount += fine.getAmount();
                sb.append(String.format("%-5d %-15s %-20s %-15.2f %s\n",
                    count,
                    fine.getLicensePlate(),
                    fine.getType(),
                    fine.getAmount(),
                    fine.getIssuedDate().format(DATE_FORMAT)
                ));
            }
            
            sb.append("-".repeat(60)).append("\n");
            sb.append(String.format("Total Outstanding Fines: %d\n", count));
            sb.append(String.format("Total Amount: RM %.2f\n", totalAmount));
        }

        sb.append("\n").append("=".repeat(60));

        return sb.toString();
    }
    
    // CSV Export Methods
    
    private static void exportVehicleCsv(ParkingLot parkingLot, BufferedWriter writer) throws IOException {
        writer.write("No,License Plate,Vehicle Type,Spot ID,Entry Time\n");
        
        int count = 0;
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getAllSpots()) {
                if (!spot.isAvailable() && spot.getCurrentVehicle() != null) {
                    Vehicle vehicle = spot.getCurrentVehicle();
                    count++;
                    writer.write(String.format("%d,%s,%s,%s,%s\n",
                        count,
                        vehicle.getLicensePlate(),
                        vehicle.getType(),
                        spot.getSpotId(),
                        vehicle.getEntryTime() != null ? vehicle.getEntryTime().format(DATE_FORMAT) : "N/A"
                    ));
                }
            }
        }
    }
    
    private static void exportRevenueCsv(ParkingLot parkingLot, BufferedWriter writer) throws IOException {
        writer.write("Metric,Value\n");
        writer.write(String.format("Total Revenue,RM %.2f\n", parkingLot.getTotalRevenue()));
        writer.write("\n");
        writer.write("Floor,Vehicles Parked\n");
        
        for (Floor floor : parkingLot.getFloors()) {
            int occupied = floor.getAllSpots().size() - floor.getAvailableSpots().size();
            writer.write(String.format("%d,%d\n", floor.getFloorNumber(), occupied));
        }
    }
    
    private static void exportOccupancyCsv(ParkingLot parkingLot, BufferedWriter writer) throws IOException {
        writer.write("Floor,Total Spots,Available,Occupied,Occupancy %\n");
        
        int totalSpots = 0;
        int totalOccupied = 0;
        
        for (Floor floor : parkingLot.getFloors()) {
            int total = floor.getAllSpots().size();
            int available = floor.getAvailableSpots().size();
            int occupied = total - available;
            double occupancy = total > 0 ? (occupied * 100.0 / total) : 0;
            
            totalSpots += total;
            totalOccupied += occupied;
            
            writer.write(String.format("%d,%d,%d,%d,%.1f%%\n",
                floor.getFloorNumber(), total, available, occupied, occupancy));
        }
        
        double totalOccupancy = totalSpots > 0 ? (totalOccupied * 100.0 / totalSpots) : 0;
        writer.write(String.format("TOTAL,%d,%d,%d,%.1f%%\n",
            totalSpots, totalSpots - totalOccupied, totalOccupied, totalOccupancy));
        
        writer.write("\n");
        writer.write("Spot Type,Occupied,Total,Occupancy %\n");
        
        for (SpotType type : SpotType.values()) {
            int typeTotal = 0;
            int typeOccupied = 0;
            for (Floor floor : parkingLot.getFloors()) {
                for (ParkingSpot spot : floor.getAllSpots()) {
                    if (spot.getType() == type) {
                        typeTotal++;
                        if (!spot.isAvailable()) {
                            typeOccupied++;
                        }
                    }
                }
            }
            double typeOccupancy = typeTotal > 0 ? (typeOccupied * 100.0 / typeTotal) : 0;
            writer.write(String.format("%s,%d,%d,%.1f%%\n",
                type, typeOccupied, typeTotal, typeOccupancy));
        }
    }
    
    private static void exportFineCsv(List<Fine> fines, BufferedWriter writer) throws IOException {
        writer.write("No,License Plate,Fine Type,Amount (RM),Issued Date\n");
        
        if (fines != null && !fines.isEmpty()) {
            int count = 0;
            for (Fine fine : fines) {
                count++;
                writer.write(String.format("%d,%s,%s,%.2f,%s\n",
                    count,
                    fine.getLicensePlate(),
                    fine.getType(),
                    fine.getAmount(),
                    fine.getIssuedDate().format(DATE_FORMAT)
                ));
            }
        }
    }
}
