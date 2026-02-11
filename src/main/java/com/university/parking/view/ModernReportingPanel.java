package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.FineCalculationStrategy;
import com.university.parking.model.FixedFineStrategy;
import com.university.parking.model.Floor;
import com.university.parking.model.HourlyFineStrategy;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.ProgressiveFineStrategy;
import com.university.parking.model.SpotType;
import com.university.parking.model.Vehicle;
import com.university.parking.util.ReportExporter;

/**
 * Modern reporting panel with styled components.
 * Supports vehicle listing, revenue, occupancy, and fine reports.
 * 
 * Requirements: 4.2, 4.3, 10.1, 10.2, 10.3, 10.4
 */
public class ModernReportingPanel extends BasePanel {
    
    private StyledComboBox<String> reportTypeCombo;
    private JTextArea reportArea;
    private StyledButton generateButton;
    private StyledButton clearButton;
    private StyledButton exportTxtButton;
    private StyledButton exportPdfButton;
    private StyledButton exportCsvButton;
    
    @SuppressWarnings("unused")
    private final DatabaseManager dbManager;
    private final FineDAO fineDAO;
    private List<Fine> currentFines;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ModernReportingPanel(ParkingLot parkingLot) {
        this(parkingLot, null, null);
    }
    
    public ModernReportingPanel(ParkingLot parkingLot, DatabaseManager dbManager, FineDAO fineDAO) {
        super(parkingLot);
        this.dbManager = dbManager;
        this.fineDAO = fineDAO;
        this.currentFines = new ArrayList<>();
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(ThemeManager.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top panel - Report selection with styled components
        add(createSelectionPanel(), BorderLayout.NORTH);

        // Center panel - Styled report display
        add(createReportDisplayPanel(), BorderLayout.CENTER);
    }


    private JPanel createSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_CARD, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Report Selection");
        titleLabel.setFont(ThemeManager.FONT_SUBHEADER);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Controls panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        controlsPanel.setOpaque(false);
        
        JLabel typeLabel = new JLabel("Report Type:");
        typeLabel.setFont(ThemeManager.FONT_BODY);
        typeLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        controlsPanel.add(typeLabel);
        
        // Styled combo box
        reportTypeCombo = new StyledComboBox<>(new String[]{
            "Current Vehicles",
            "Revenue Report",
            "Occupancy Report",
            "Fine Report"
        });
        reportTypeCombo.setPreferredSize(new Dimension(200, 36));
        controlsPanel.add(reportTypeCombo);
        
        // Styled generate button
        generateButton = new StyledButton("Generate Report", ThemeManager.SUCCESS);
        generateButton.addActionListener(e -> generateReport());
        controlsPanel.add(generateButton);
        
        // Styled clear button
        clearButton = new StyledButton("Clear", ThemeManager.TEXT_SECONDARY);
        clearButton.addActionListener(e -> reportArea.setText(""));
        controlsPanel.add(clearButton);
        
        // Export buttons
        exportTxtButton = new StyledButton("Export TXT", ThemeManager.PRIMARY);
        exportTxtButton.addActionListener(e -> exportReport(ReportExporter.ExportFormat.TXT));
        controlsPanel.add(exportTxtButton);
        
        exportPdfButton = new StyledButton("Export PDF", ThemeManager.PRIMARY);
        exportPdfButton.addActionListener(e -> exportReport(ReportExporter.ExportFormat.PDF));
        controlsPanel.add(exportPdfButton);
        
        exportCsvButton = new StyledButton("Export CSV", ThemeManager.PRIMARY);
        exportCsvButton.addActionListener(e -> exportReport(ReportExporter.ExportFormat.CSV));
        controlsPanel.add(exportCsvButton);
        
        panel.add(controlsPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createReportDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BG_CARD, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Report Output");
        titleLabel.setFont(ThemeManager.FONT_SUBHEADER);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Styled report area with distinct background
        reportArea = new StyledReportArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        reportArea.setMargin(new Insets(15, 15, 15, 15));
        reportArea.setBackground(ThemeManager.BG_CARD);
        reportArea.setForeground(ThemeManager.TEXT_PRIMARY);
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.PRIMARY_LIGHT, 1));
        scrollPane.getViewport().setBackground(ThemeManager.BG_CARD);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }


    private void generateReport() {
        int selectedIndex = reportTypeCombo.getSelectedIndex();
        
        switch (selectedIndex) {
            case 0:
                generateVehicleReport();
                break;
            case 1:
                generateRevenueReport();
                break;
            case 2:
                generateOccupancyReport();
                break;
            case 3:
                generateFineReport();
                break;
            default:
                StyledDialog.showError(this, "Please select a report type");
        }
    }

    /**
     * Generates a report listing all currently parked vehicles.
     * Requirement 10.1
     */
    private void generateVehicleReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(60)).append("\n");
        sb.append("         CURRENT VEHICLES REPORT\n");
        sb.append("         Generated: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n");
        sb.append("═".repeat(60)).append("\n\n");

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

        sb.append("\n").append("─".repeat(60)).append("\n");
        sb.append("Total Vehicles: ").append(count).append("\n");
        sb.append("═".repeat(60));

        reportArea.setText(sb.toString());
    }

    /**
     * Generates a revenue report.
     * Requirement 10.2
     */
    private void generateRevenueReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(60)).append("\n");
        sb.append("            REVENUE REPORT\n");
        sb.append("         Generated: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n");
        sb.append("═".repeat(60)).append("\n\n");

        sb.append("Total Revenue Collected: RM ").append(String.format("%.2f", parkingLot.getTotalRevenue())).append("\n\n");

        // Revenue breakdown by floor
        sb.append("Revenue by Floor:\n");
        sb.append("─".repeat(40)).append("\n");
        
        for (Floor floor : parkingLot.getFloors()) {
            int occupied = floor.getAllSpots().size() - floor.getAvailableSpots().size();
            sb.append(String.format("Floor %d: %d vehicles currently parked\n",
                floor.getFloorNumber(), occupied));
        }

        sb.append("\n").append("═".repeat(60));

        reportArea.setText(sb.toString());
    }


    /**
     * Generates an occupancy report.
     * Requirement 10.3
     */
    private void generateOccupancyReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(60)).append("\n");
        sb.append("           OCCUPANCY REPORT\n");
        sb.append("         Generated: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n");
        sb.append("═".repeat(60)).append("\n\n");

        int totalSpots = 0;
        int totalOccupied = 0;

        sb.append(String.format("%-10s %-12s %-12s %-12s %s\n",
            "Floor", "Total Spots", "Available", "Occupied", "Occupancy %"));
        sb.append("─".repeat(60)).append("\n");

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

        sb.append("─".repeat(60)).append("\n");
        double totalOccupancy = totalSpots > 0 ? (totalOccupied * 100.0 / totalSpots) : 0;
        sb.append(String.format("%-10s %-12d %-12d %-12d %.1f%%\n",
            "TOTAL", totalSpots, totalSpots - totalOccupied, totalOccupied, totalOccupancy));

        // Breakdown by spot type
        sb.append("\n\nOccupancy by Spot Type:\n");
        sb.append("─".repeat(40)).append("\n");
        
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

        sb.append("\n").append("═".repeat(60));

        reportArea.setText(sb.toString());
    }

    /**
     * Generates a fine report.
     * Requirement 10.4
     */
    private void generateFineReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(60)).append("\n");
        sb.append("             FINE REPORT\n");
        sb.append("         Generated: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n");
        sb.append("═".repeat(60)).append("\n\n");

        sb.append("Current Fine Strategy: ");
        FineCalculationStrategy strategy = parkingLot.getFineCalculationContext().getStrategy();
        if (strategy instanceof FixedFineStrategy) {
            sb.append("Fixed (RM 50)\n");
        } else if (strategy instanceof ProgressiveFineStrategy) {
            sb.append("Progressive (RM 50 + escalating)\n");
        } else if (strategy instanceof HourlyFineStrategy) {
            sb.append("Hourly (RM 20/hour)\n");
        } else {
            sb.append("Unknown\n");
        }

        sb.append("\nOutstanding Fines:\n");
        sb.append("─".repeat(60)).append("\n");
        
        currentFines = new ArrayList<>();
        
        if (fineDAO != null) {
            try {
                List<Fine> unpaidFines = fineDAO.findAllUnpaid();
                currentFines = unpaidFines;
                
                if (unpaidFines.isEmpty()) {
                    sb.append("No outstanding fines.\n");
                } else {
                    sb.append(String.format("%-5s %-15s %-20s %-15s %s\n",
                        "No.", "License Plate", "Fine Type", "Amount (RM)", "Issued Date"));
                    sb.append("─".repeat(60)).append("\n");
                    
                    int count = 0;
                    double totalAmount = 0;
                    for (Fine fine : unpaidFines) {
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
                    
                    sb.append("─".repeat(60)).append("\n");
                    sb.append(String.format("Total Outstanding Fines: %d\n", count));
                    sb.append(String.format("Total Amount: RM %.2f\n", totalAmount));
                }
            } catch (SQLException e) {
                sb.append("Error loading fine data: ").append(e.getMessage()).append("\n");
            }
        } else {
            sb.append("(Fine data unavailable - database not connected)\n");
        }

        sb.append("\n").append("═".repeat(60));

        reportArea.setText(sb.toString());
    }
    
    /**
     * Exports the current report to the specified format.
     */
    private void exportReport(ReportExporter.ExportFormat format) {
        if (reportArea.getText().trim().isEmpty()) {
            StyledDialog.showError(this, "Please generate a report first before exporting");
            return;
        }
        
        // Get report type
        int selectedIndex = reportTypeCombo.getSelectedIndex();
        ReportExporter.ReportType reportType;
        
        switch (selectedIndex) {
            case 0:
                reportType = ReportExporter.ReportType.VEHICLE;
                break;
            case 1:
                reportType = ReportExporter.ReportType.REVENUE;
                break;
            case 2:
                reportType = ReportExporter.ReportType.OCCUPANCY;
                break;
            case 3:
                reportType = ReportExporter.ReportType.FINE;
                break;
            default:
                StyledDialog.showError(this, "Please select a report type");
                return;
        }
        
        // Choose save location
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report");
        fileChooser.setSelectedFile(new File(reportType.getFileName() + format.getExtension()));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String directory = selectedFile.getParent();
            
            try {
                File exportedFile = ReportExporter.exportReport(
                    reportType, 
                    format, 
                    parkingLot, 
                    currentFines, 
                    directory
                );
                
                JOptionPane.showMessageDialog(this,
                    "Report exported successfully to:\n" + exportedFile.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (IOException e) {
                StyledDialog.showError(this, "Failed to export report: " + e.getMessage());
            }
        }
    }


    @Override
    public void refreshData() {
        // Nothing to refresh automatically
    }

    // Getters for testing
    public StyledComboBox<String> getReportTypeCombo() {
        return reportTypeCombo;
    }

    public JTextArea getReportArea() {
        return reportArea;
    }

    public StyledButton getGenerateButton() {
        return generateButton;
    }
    
    public StyledButton getClearButton() {
        return clearButton;
    }
    
    /**
     * Custom styled text area for report output with distinct background.
     * Provides visual distinction from the parent panel.
     */
    private static class StyledReportArea extends JTextArea {
        
        private static final int BORDER_RADIUS = 8;
        
        public StyledReportArea() {
            super();
            setOpaque(false);
            setBorder(new EmptyBorder(10, 10, 10, 10));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw rounded background
            g2d.setColor(getBackground());
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS);
            
            g2d.dispose();
            
            super.paintComponent(g);
        }
    }
}
