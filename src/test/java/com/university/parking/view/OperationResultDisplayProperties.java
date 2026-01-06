package com.university.parking.view;

import javax.swing.JTextArea;

import com.university.parking.controller.VehicleEntryController;
import com.university.parking.controller.VehicleExitController;
import com.university.parking.model.FixedFineStrategy;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.PaymentMethod;
import com.university.parking.model.SpotType;
import com.university.parking.model.VehicleType;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

/**
 * Property-based tests for Operation Result Display.
 * 
 * Feature: parking-lot-management, Property 25: Operation Result Display
 * Validates: Requirements 9.5
 */
public class OperationResultDisplayProperties {

    static {
        EventHandler.setSuppressDialogs(true);
    }

    @Property(tries = 100)
    void entryOperationDisplaysTicketInformation(
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll boolean isHandicapped) {
        
        // Create parking lot with available spots
        ParkingLot parkingLot = createTestParkingLot();
        
        VehicleEntryPanel entryPanel = new VehicleEntryPanel(parkingLot);
        VehicleEntryController controller = entryPanel.getEntryController();
        
        // Find an available spot
        String spotId = parkingLot.getAllSpots().stream()
            .filter(s -> s.isAvailable())
            .findFirst()
            .map(s -> s.getSpotId())
            .orElse(null);
        
        if (spotId == null) return; // No available spots
        
        try {
            // Process entry
            VehicleEntryController.EntryResult result = controller.processEntry(
                licensePlate, vehicleType, isHandicapped, spotId
            );
            
            // Get ticket display
            String ticketDisplay = result.getTicketDisplay();
            
            // Verify ticket display contains required information (Requirement 9.5)
            assert ticketDisplay != null && !ticketDisplay.isEmpty() : 
                "Entry operation should display ticket information";
            assert ticketDisplay.contains(licensePlate.toUpperCase()) : 
                "Ticket should contain license plate";
            assert ticketDisplay.contains(spotId) : 
                "Ticket should contain spot location";
            assert ticketDisplay.contains("Entry Time") : 
                "Ticket should contain entry time";
            assert ticketDisplay.contains("Ticket Number") : 
                "Ticket should contain ticket number";
                
        } catch (IllegalArgumentException e) {
            // Vehicle type incompatible with available spots - skip
        }
    }

    @Property(tries = 100)
    void exitOperationDisplaysPaymentSummary(
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll @IntRange(min = 1, max = 10) int parkingHours) {
        
        // Create parking lot and park a vehicle
        ParkingLot parkingLot = createTestParkingLot();
        
        VehicleEntryController entryController = new VehicleEntryController(parkingLot);
        VehicleExitController exitController = new VehicleExitController(parkingLot);
        
        // Find an available spot
        String spotId = parkingLot.getAllSpots().stream()
            .filter(s -> s.isAvailable())
            .findFirst()
            .map(s -> s.getSpotId())
            .orElse(null);
        
        if (spotId == null) return;
        
        try {
            // Process entry
            entryController.processEntry(licensePlate, vehicleType, false, spotId);
            
            // Generate payment summary
            VehicleExitController.PaymentSummary summary = 
                exitController.generatePaymentSummary(licensePlate.toUpperCase(), 
                    exitController.getUnpaidFines(licensePlate.toUpperCase()));
            
            // Get display text
            String displayText = summary.getDisplayText();
            
            // Verify payment summary contains required information (Requirement 9.5)
            assert displayText != null && !displayText.isEmpty() : 
                "Exit operation should display payment summary";
            assert displayText.contains("License Plate") || displayText.contains(licensePlate.toUpperCase()) : 
                "Summary should contain license plate";
            assert displayText.contains("Parking Fee") || displayText.contains("Fee") : 
                "Summary should contain parking fee";
            assert displayText.toUpperCase().contains("TOTAL") : 
                "Summary should contain total amount";
                
        } catch (IllegalArgumentException e) {
            // Vehicle type incompatible or other issue - skip
        }
    }

    @Property(tries = 100)
    void paymentOperationDisplaysReceipt(
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType,
            @ForAll @DoubleRange(min = 10.0, max = 100.0) double paymentAmount,
            @ForAll("paymentMethods") PaymentMethod paymentMethod) {
        
        // Create parking lot and park a vehicle
        ParkingLot parkingLot = createTestParkingLot();
        
        VehicleEntryController entryController = new VehicleEntryController(parkingLot);
        VehicleExitController exitController = new VehicleExitController(parkingLot);
        
        // Find an available spot
        String spotId = parkingLot.getAllSpots().stream()
            .filter(s -> s.isAvailable())
            .findFirst()
            .map(s -> s.getSpotId())
            .orElse(null);
        
        if (spotId == null) return;
        
        try {
            // Process entry
            entryController.processEntry(licensePlate, vehicleType, false, spotId);
            
            // Process exit with payment
            VehicleExitController.ExitResult result = exitController.processExit(
                licensePlate.toUpperCase(), paymentAmount, paymentMethod, 
                exitController.getUnpaidFines(licensePlate.toUpperCase())
            );
            
            // Get receipt text
            String receiptText = result.getReceipt().generateReceiptText();
            
            // Verify receipt contains required information (Requirement 9.5)
            assert receiptText != null && !receiptText.isEmpty() : 
                "Payment operation should display receipt";
            assert receiptText.contains(licensePlate.toUpperCase()) : 
                "Receipt should contain license plate";
            assert receiptText.contains("Entry Time") || receiptText.contains("Entry") : 
                "Receipt should contain entry time";
            assert receiptText.contains("Exit Time") || receiptText.contains("Exit") : 
                "Receipt should contain exit time";
            assert receiptText.contains("Total") || receiptText.contains("Amount") : 
                "Receipt should contain payment amount";
            assert receiptText.contains(paymentMethod.toString()) : 
                "Receipt should contain payment method";
                
        } catch (IllegalArgumentException e) {
            // Vehicle type incompatible or other issue - skip
        }
    }

    @Property(tries = 100)
    void operationResultsAreNonEmpty(
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType) {
        
        // Create parking lot
        ParkingLot parkingLot = createTestParkingLot();
        
        VehicleEntryController entryController = new VehicleEntryController(parkingLot);
        
        // Find an available spot
        String spotId = parkingLot.getAllSpots().stream()
            .filter(s -> s.isAvailable())
            .findFirst()
            .map(s -> s.getSpotId())
            .orElse(null);
        
        if (spotId == null) return;
        
        try {
            // Process entry
            VehicleEntryController.EntryResult result = entryController.processEntry(
                licensePlate, vehicleType, false, spotId
            );
            
            // Verify result display is not empty (Requirement 9.5)
            String display = result.getTicketDisplay();
            assert display != null : "Operation result should not be null";
            assert !display.trim().isEmpty() : "Operation result should not be empty";
            assert display.length() > 10 : "Operation result should contain meaningful information";
            
        } catch (IllegalArgumentException e) {
            // Vehicle type incompatible - skip
        }
    }

    @Property(tries = 100)
    void textAreaDisplaysOperationResults(
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType) {
        
        // Create parking lot
        ParkingLot parkingLot = createTestParkingLot();
        
        VehicleEntryPanel entryPanel = new VehicleEntryPanel(parkingLot);
        JTextArea ticketArea = entryPanel.getTicketArea();
        
        // Find an available spot
        String spotId = parkingLot.getAllSpots().stream()
            .filter(s -> s.isAvailable())
            .findFirst()
            .map(s -> s.getSpotId())
            .orElse(null);
        
        if (spotId == null) return;
        
        try {
            // Process entry
            VehicleEntryController.EntryResult result = entryPanel.getEntryController()
                .processEntry(licensePlate, vehicleType, false, spotId);
            
            // Simulate displaying in text area
            ticketArea.setText(result.getTicketDisplay());
            
            // Verify text area contains the result (Requirement 9.5)
            String displayedText = ticketArea.getText();
            assert displayedText != null && !displayedText.isEmpty() : 
                "Text area should display operation results";
            assert displayedText.contains(licensePlate.toUpperCase()) : 
                "Displayed result should contain license plate";
                
        } catch (IllegalArgumentException e) {
            // Vehicle type incompatible - skip
        }
    }

    @Provide
    Arbitrary<VehicleType> vehicleTypes() {
        return Arbitraries.of(VehicleType.values());
    }

    @Provide
    Arbitrary<PaymentMethod> paymentMethods() {
        return Arbitraries.of(PaymentMethod.values());
    }

    /**
     * Helper method to create a test parking lot with available spots.
     */
    private ParkingLot createTestParkingLot() {
        ParkingLot parkingLot = new ParkingLot("Test Lot");
        
        // Create row configurations with various spot types
        java.util.List<ParkingLot.RowConfiguration> rowConfigs = new java.util.ArrayList<>();
        SpotType[] spotTypes = {SpotType.COMPACT, SpotType.REGULAR, SpotType.HANDICAPPED, SpotType.RESERVED};
        rowConfigs.add(new ParkingLot.RowConfiguration(4, spotTypes));
        
        // Create a floor with spots
        parkingLot.createFloor(1, rowConfigs);
        
        // Set fine calculation strategy
        parkingLot.getFineCalculationContext().setStrategy(new FixedFineStrategy());
        
        return parkingLot;
    }
}
