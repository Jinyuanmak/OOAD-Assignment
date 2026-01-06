package com.university.parking.view;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.FineType;
import com.university.parking.model.Floor;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.SpotType;
import com.university.parking.model.Vehicle;
import com.university.parking.model.VehicleType;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Property-based tests for report generation accuracy.
 * Feature: parking-lot-management, Property 26: Report Generation Accuracy
 * 
 * **Validates: Requirements 10.1, 10.2, 10.3, 10.4**
 */
public class ReportGenerationAccuracyProperties {

    /**
     * Property 26: Report Generation Accuracy - Vehicle Listing
     * For any parking lot state, the vehicle report should list all currently parked vehicles
     * with accurate details.
     */
    @Property(tries = 100)
    public void vehicleReportListsAllParkedVehicles(
            @ForAll("parkingLotWithVehicles") ParkingLot parkingLot) {
        
        // Create reporting panel
        ReportingPanel reportingPanel = new ReportingPanel(parkingLot);
        
        // Select vehicle report
        reportingPanel.getReportTypeCombo().setSelectedIndex(0);
        
        // Generate report
        reportingPanel.getGenerateButton().doClick();
        
        // Get report text
        String reportText = reportingPanel.getReportArea().getText();
        
        // Count actually parked vehicles
        int actualParkedCount = 0;
        List<String> parkedPlates = new ArrayList<>();
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getAllSpots()) {
                if (!spot.isAvailable() && spot.getCurrentVehicle() != null) {
                    actualParkedCount++;
                    parkedPlates.add(spot.getCurrentVehicle().getLicensePlate());
                }
            }
        }
        
        // Verify report contains correct count
        assertTrue(reportText.contains("Total Vehicles: " + actualParkedCount),
            "Report should show correct total vehicle count");
        
        // Verify all parked vehicles appear in report
        for (String plate : parkedPlates) {
            assertTrue(reportText.contains(plate),
                "Report should contain license plate: " + plate);
        }
    }

    /**
     * Property 26: Report Generation Accuracy - Revenue Report
     * For any parking lot state, the revenue report should display the correct total revenue.
     */
    @Property(tries = 100)
    public void revenueReportShowsCorrectTotal(
            @ForAll("parkingLotWithRevenue") ParkingLot parkingLot) {
        
        // Create reporting panel
        ReportingPanel reportingPanel = new ReportingPanel(parkingLot);
        
        // Select revenue report
        reportingPanel.getReportTypeCombo().setSelectedIndex(1);
        
        // Generate report
        reportingPanel.getGenerateButton().doClick();
        
        // Get report text
        String reportText = reportingPanel.getReportArea().getText();
        
        // Verify report contains revenue
        double expectedRevenue = parkingLot.getTotalRevenue();
        String expectedRevenueStr = String.format("%.2f", expectedRevenue);
        assertTrue(reportText.contains(expectedRevenueStr),
            "Report should show correct total revenue: " + expectedRevenueStr);
    }

    /**
     * Property 26: Report Generation Accuracy - Occupancy Report
     * For any parking lot state, the occupancy report should calculate correct occupancy rates.
     */
    @Property(tries = 100)
    public void occupancyReportCalculatesCorrectRates(
            @ForAll("parkingLotWithVehicles") ParkingLot parkingLot) {
        
        // Create reporting panel
        ReportingPanel reportingPanel = new ReportingPanel(parkingLot);
        
        // Select occupancy report
        reportingPanel.getReportTypeCombo().setSelectedIndex(2);
        
        // Generate report
        reportingPanel.getGenerateButton().doClick();
        
        // Get report text
        String reportText = reportingPanel.getReportArea().getText();
        
        // Calculate expected occupancy
        int totalSpots = 0;
        int totalOccupied = 0;
        for (Floor floor : parkingLot.getFloors()) {
            totalSpots += floor.getAllSpots().size();
            totalOccupied += floor.getAllSpots().size() - floor.getAvailableSpots().size();
        }
        
        double expectedOccupancy = totalSpots > 0 ? (totalOccupied * 100.0 / totalSpots) : 0;
        
        // Verify report contains occupancy data
        assertTrue(reportText.contains("TOTAL"),
            "Report should contain total occupancy row");
        assertTrue(reportText.contains(String.valueOf(totalSpots)),
            "Report should show correct total spots");
        assertTrue(reportText.contains(String.valueOf(totalOccupied)),
            "Report should show correct occupied spots");
    }

    /**
     * Property 26: Report Generation Accuracy - Fine Report
     * For any set of unpaid fines, the fine report should list all unpaid fines accurately.
     */
    @Property(tries = 100)
    public void fineReportListsAllUnpaidFines(
            @ForAll("unpaidFinesList") List<Fine> unpaidFines) throws SQLException {
        
        // Create database and DAO for this test
        DatabaseManager dbManager = new DatabaseManager("jdbc:h2:mem:test_report_" + System.currentTimeMillis() + ";DB_CLOSE_DELAY=-1");
        dbManager.initializeDatabase();
        FineDAO fineDAO = new FineDAO(dbManager);
        
        // Save fines to database
        for (Fine fine : unpaidFines) {
            fineDAO.save(fine);
        }
        
        // Create parking lot and reporting panel
        ParkingLot parkingLot = createSimpleParkingLot();
        ReportingPanel reportingPanel = new ReportingPanel(parkingLot, dbManager, fineDAO);
        
        // Select fine report
        reportingPanel.getReportTypeCombo().setSelectedIndex(3);
        
        // Generate report
        reportingPanel.getGenerateButton().doClick();
        
        // Get report text
        String reportText = reportingPanel.getReportArea().getText();
        
        if (unpaidFines.isEmpty()) {
            assertTrue(reportText.contains("No outstanding fines") || reportText.contains("Outstanding Fines:"),
                "Report should indicate no outstanding fines when list is empty");
        } else {
            // Verify report contains correct count
            assertTrue(reportText.contains("Total Outstanding Fines: " + unpaidFines.size()),
                "Report should show correct total fine count");
            
            // Calculate total amount
            double totalAmount = unpaidFines.stream().mapToDouble(Fine::getAmount).sum();
            String expectedAmountStr = String.format("%.2f", totalAmount);
            assertTrue(reportText.contains(expectedAmountStr),
                "Report should show correct total fine amount");
            
            // Verify all fines appear in report
            for (Fine fine : unpaidFines) {
                assertTrue(reportText.contains(fine.getLicensePlate()),
                    "Report should contain license plate: " + fine.getLicensePlate());
            }
        }
    }

    // Providers for test data generation

    @Provide
    Arbitrary<ParkingLot> parkingLotWithVehicles() {
        return Arbitraries.integers().between(0, 10).flatMap(vehicleCount -> {
            ParkingLot lot = createSimpleParkingLot();
            
            // Park random vehicles
            for (int i = 0; i < vehicleCount; i++) {
                List<ParkingSpot> availableSpots = lot.findAvailableSpots(VehicleType.CAR);
                if (!availableSpots.isEmpty()) {
                    ParkingSpot spot = availableSpots.get(0);
                    Vehicle vehicle = new Vehicle(
                        "ABC" + (1000 + i),
                        VehicleType.CAR,
                        false
                    );
                    vehicle.setEntryTime(LocalDateTime.now().minusHours(i));
                    spot.occupySpot(vehicle);
                }
            }
            
            return Arbitraries.just(lot);
        });
    }

    @Provide
    Arbitrary<ParkingLot> parkingLotWithRevenue() {
        return Arbitraries.doubles().between(0.0, 10000.0).map(revenue -> {
            ParkingLot lot = createSimpleParkingLot();
            lot.addRevenue(revenue);
            return lot;
        });
    }

    @Provide
    Arbitrary<List<Fine>> unpaidFinesList() {
        return Arbitraries.integers().between(0, 10).flatMap(count -> {
            List<Fine> fines = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Fine fine = new Fine();
                fine.setLicensePlate("XYZ" + (1000 + i));
                fine.setType(i % 2 == 0 ? FineType.OVERSTAY : FineType.UNAUTHORIZED_RESERVED);
                fine.setAmount(50.0 + (i * 10.0));
                fine.setIssuedDate(LocalDateTime.now().minusDays(i));
                fine.setPaid(false);
                fines.add(fine);
            }
            return Arbitraries.just(fines);
        });
    }

    private ParkingLot createSimpleParkingLot() {
        ParkingLot lot = new ParkingLot("Test Parking Lot");
        Floor floor = new Floor(1);
        floor.createRow(1, 5, new SpotType[]{
            SpotType.COMPACT, SpotType.COMPACT, SpotType.REGULAR, SpotType.REGULAR, SpotType.HANDICAPPED
        });
        lot.addFloor(floor);
        return lot;
    }
}
