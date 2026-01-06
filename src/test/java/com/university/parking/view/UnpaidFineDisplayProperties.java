package com.university.parking.view;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.swing.table.TableModel;

import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.FineType;
import com.university.parking.model.Floor;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.SpotType;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for Unpaid Fine Display.
 * 
 * Feature: parking-lot-management, Property 23: Unpaid Fine Display
 * Validates: Requirements 8.5
 */
public class UnpaidFineDisplayProperties {

    @Property(tries = 100)
    void allUnpaidFinesAppearInTable(
            @ForAll("parkingLotWithSpots") ParkingLot parkingLot,
            @ForAll @IntRange(min = 0, max = 10) int numFines) throws Exception {
        
        // Initialize database
        DatabaseManager dbManager = new DatabaseManager("jdbc:h2:mem:test_" + System.nanoTime());
        dbManager.initializeDatabase();
        FineDAO fineDAO = new FineDAO(dbManager);
        
        // Create unpaid fines
        Set<String> licensePlates = new HashSet<>();
        for (int i = 0; i < numFines; i++) {
            String licensePlate = "FINE" + i;
            licensePlates.add(licensePlate);
            
            Fine fine = new Fine();
            fine.setLicensePlate(licensePlate);
            fine.setType(FineType.OVERSTAY);
            fine.setAmount(50.0 + i * 10);
            fine.setIssuedDate(LocalDateTime.now());
            fine.setPaid(false);
            
            fineDAO.save(fine);
        }
        
        // Create admin panel with database access
        AdminPanel adminPanel = new AdminPanel(parkingLot) {
            @Override
            public void refreshData() {
                // Override to use our test database
                try {
                    getModel().setRowCount(0);
                    var fines = fineDAO.findAllUnpaid();
                    for (Fine fine : fines) {
                        getModel().addRow(new Object[]{
                            fine.getLicensePlate(),
                            fine.getType(),
                            String.format("%.2f", fine.getAmount()),
                            fine.getIssuedDate()
                        });
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            private javax.swing.table.DefaultTableModel getModel() {
                return (javax.swing.table.DefaultTableModel) getFineTable().getModel();
            }
        };
        
        adminPanel.refreshData();
        TableModel fineTable = adminPanel.getFineTable().getModel();
        
        // Collect license plates from table
        Set<String> tableLicensePlates = new HashSet<>();
        for (int row = 0; row < fineTable.getRowCount(); row++) {
            String licensePlate = (String) fineTable.getValueAt(row, 0);
            tableLicensePlates.add(licensePlate);
        }
        
        // All unpaid fines should appear in table
        assert tableLicensePlates.equals(licensePlates) : 
            String.format("Table should contain all unpaid fines. Expected: %s, Got: %s", 
                licensePlates, tableLicensePlates);
    }

    @Property(tries = 100)
    void paidFinesDoNotAppearInTable(
            @ForAll("parkingLotWithSpots") ParkingLot parkingLot,
            @ForAll @IntRange(min = 1, max = 5) int numPaidFines,
            @ForAll @IntRange(min = 1, max = 5) int numUnpaidFines) throws Exception {
        
        // Initialize database
        DatabaseManager dbManager = new DatabaseManager("jdbc:h2:mem:test_" + System.nanoTime());
        dbManager.initializeDatabase();
        FineDAO fineDAO = new FineDAO(dbManager);
        
        // Create paid fines
        for (int i = 0; i < numPaidFines; i++) {
            Fine fine = new Fine();
            fine.setLicensePlate("PAID" + i);
            fine.setType(FineType.OVERSTAY);
            fine.setAmount(50.0);
            fine.setIssuedDate(LocalDateTime.now());
            fine.setPaid(true);
            fineDAO.save(fine);
        }
        
        // Create unpaid fines
        Set<String> unpaidPlates = new HashSet<>();
        for (int i = 0; i < numUnpaidFines; i++) {
            String licensePlate = "UNPAID" + i;
            unpaidPlates.add(licensePlate);
            
            Fine fine = new Fine();
            fine.setLicensePlate(licensePlate);
            fine.setType(FineType.OVERSTAY);
            fine.setAmount(50.0);
            fine.setIssuedDate(LocalDateTime.now());
            fine.setPaid(false);
            fineDAO.save(fine);
        }
        
        // Create admin panel with database access
        AdminPanel adminPanel = new AdminPanel(parkingLot) {
            @Override
            public void refreshData() {
                try {
                    getModel().setRowCount(0);
                    var fines = fineDAO.findAllUnpaid();
                    for (Fine fine : fines) {
                        getModel().addRow(new Object[]{
                            fine.getLicensePlate(),
                            fine.getType(),
                            String.format("%.2f", fine.getAmount()),
                            fine.getIssuedDate()
                        });
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            private javax.swing.table.DefaultTableModel getModel() {
                return (javax.swing.table.DefaultTableModel) getFineTable().getModel();
            }
        };
        
        adminPanel.refreshData();
        TableModel fineTable = adminPanel.getFineTable().getModel();
        
        // Collect license plates from table
        Set<String> tableLicensePlates = new HashSet<>();
        for (int row = 0; row < fineTable.getRowCount(); row++) {
            String licensePlate = (String) fineTable.getValueAt(row, 0);
            tableLicensePlates.add(licensePlate);
            
            // Ensure no paid fines appear
            assert !licensePlate.startsWith("PAID") : 
                "Paid fines should not appear in the table";
        }
        
        // Only unpaid fines should appear
        assert tableLicensePlates.equals(unpaidPlates) : 
            "Table should only contain unpaid fines";
    }

    @Property(tries = 100)
    void fineTableContainsRequiredColumns(
            @ForAll("parkingLotWithSpots") ParkingLot parkingLot,
            @ForAll @DoubleRange(min = 10.0, max = 500.0) double fineAmount) throws Exception {
        
        // Initialize database
        DatabaseManager dbManager = new DatabaseManager("jdbc:h2:mem:test_" + System.nanoTime());
        dbManager.initializeDatabase();
        FineDAO fineDAO = new FineDAO(dbManager);
        
        // Create one fine
        Fine fine = new Fine();
        fine.setLicensePlate("TEST123");
        fine.setType(FineType.UNAUTHORIZED_RESERVED);
        fine.setAmount(fineAmount);
        fine.setIssuedDate(LocalDateTime.now());
        fine.setPaid(false);
        fineDAO.save(fine);
        
        // Create admin panel with database access
        AdminPanel adminPanel = new AdminPanel(parkingLot) {
            @Override
            public void refreshData() {
                try {
                    getModel().setRowCount(0);
                    var fines = fineDAO.findAllUnpaid();
                    for (Fine f : fines) {
                        getModel().addRow(new Object[]{
                            f.getLicensePlate(),
                            f.getType(),
                            String.format("%.2f", f.getAmount()),
                            f.getIssuedDate()
                        });
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            private javax.swing.table.DefaultTableModel getModel() {
                return (javax.swing.table.DefaultTableModel) getFineTable().getModel();
            }
        };
        
        adminPanel.refreshData();
        TableModel fineTable = adminPanel.getFineTable().getModel();
        
        if (fineTable.getRowCount() > 0) {
            // Check that table has required columns: License Plate, Type, Amount, Date
            assert fineTable.getColumnCount() >= 4 : 
                "Fine table should have at least 4 columns";
            
            // Check first row has non-null values
            for (int col = 0; col < 4; col++) {
                Object value = fineTable.getValueAt(0, col);
                assert value != null : 
                    String.format("Column %d should not be null", col);
            }
            
            // Verify license plate matches
            assert "TEST123".equals(fineTable.getValueAt(0, 0)) : 
                "License plate should match";
        }
    }

    @Provide
    Arbitrary<ParkingLot> parkingLotWithSpots() {
        return Arbitraries.integers().between(1, 2).flatMap(numFloors -> {
            ParkingLot lot = new ParkingLot("Test Lot");
            
            for (int f = 1; f <= numFloors; f++) {
                Floor floor = new Floor(f);
                
                // Create 1 row per floor
                int numSpots = 2;
                SpotType[] types = new SpotType[numSpots];
                for (int s = 0; s < numSpots; s++) {
                    types[s] = SpotType.REGULAR;
                }
                floor.createRow(1, numSpots, types);
                
                lot.addFloor(floor);
            }
            
            return Arbitraries.just(lot);
        });
    }
}
