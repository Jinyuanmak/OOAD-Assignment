package com.university.parking.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;

import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.SpotType;
import com.university.parking.model.VehicleType;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Property-based tests for ticket format compliance.
 * Feature: parking-lot-management, Property 6: Ticket Format Compliance
 * Validates: Requirements 3.4, 3.5
 */
public class TicketFormatComplianceProperties {

    // Pattern for ticket format: T-{PLATE}-{TIMESTAMP}
    // TIMESTAMP format: yyyyMMddHHmmss (14 digits)
    private static final Pattern TICKET_PATTERN = Pattern.compile("T-[A-Z0-9]+-\\d{14}");

    /**
     * Property 6: Ticket Format Compliance
     * For any vehicle entry, the generated ticket should follow the format 
     * "T-{PLATE}-{TIMESTAMP}" and contain spot location and entry time.
     */
    @Property(tries = 100)
    void ticketFollowsCorrectFormat(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("entryTimes") LocalDateTime entryTime) {
        
        // Create parking lot and controller
        ParkingLot parkingLot = createSimpleParkingLot();
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        // Generate ticket
        String ticket = controller.generateTicket(licensePlate, entryTime);
        
        // Verify ticket format matches pattern
        Assertions.assertTrue(TICKET_PATTERN.matcher(ticket).matches(),
            "Ticket should match format T-{PLATE}-{TIMESTAMP}: " + ticket);
    }

    /**
     * Property: Ticket contains the license plate.
     */
    @Property(tries = 100)
    void ticketContainsLicensePlate(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("entryTimes") LocalDateTime entryTime) {
        
        ParkingLot parkingLot = createSimpleParkingLot();
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        String ticket = controller.generateTicket(licensePlate, entryTime);
        String normalizedPlate = licensePlate.trim().toUpperCase();
        
        // Verify ticket contains the license plate
        Assertions.assertTrue(ticket.contains(normalizedPlate),
            "Ticket should contain license plate: " + normalizedPlate + " in " + ticket);
    }

    /**
     * Property: Ticket starts with "T-" prefix.
     */
    @Property(tries = 100)
    void ticketStartsWithCorrectPrefix(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("entryTimes") LocalDateTime entryTime) {
        
        ParkingLot parkingLot = createSimpleParkingLot();
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        String ticket = controller.generateTicket(licensePlate, entryTime);
        
        // Verify ticket starts with "T-"
        Assertions.assertTrue(ticket.startsWith("T-"),
            "Ticket should start with 'T-': " + ticket);
    }

    /**
     * Property: Entry result contains spot location and entry time.
     * Requirement 3.5: Display ticket containing spot location and entry time
     */
    @Property(tries = 100)
    void entryResultContainsSpotLocationAndEntryTime(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("vehicleTypes") VehicleType vehicleType) {
        
        // Create parking lot with compatible spots
        ParkingLot parkingLot = createParkingLotWithAllSpotTypes();
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        // Find an available spot for the vehicle type
        List<ParkingSpot> availableSpots = controller.findAvailableSpots(vehicleType);
        if (availableSpots.isEmpty()) {
            return; // Skip if no compatible spots
        }
        
        String spotId = availableSpots.get(0).getSpotId();
        boolean isHandicapped = vehicleType == VehicleType.HANDICAPPED;
        
        // Process entry
        VehicleEntryController.EntryResult result = controller.processEntry(
            licensePlate, vehicleType, isHandicapped, spotId);
        
        // Verify result contains spot location
        Assertions.assertNotNull(result.getSpot(), "Result should contain spot");
        Assertions.assertEquals(spotId, result.getSpot().getSpotId(),
            "Result should contain correct spot location");
        
        // Verify result contains entry time
        Assertions.assertNotNull(result.getVehicle().getEntryTime(),
            "Result should contain entry time");
        
        // Verify ticket display contains spot location and entry time
        String ticketDisplay = result.getTicketDisplay();
        Assertions.assertTrue(ticketDisplay.contains(spotId),
            "Ticket display should contain spot location: " + spotId);
        Assertions.assertTrue(ticketDisplay.contains("Entry Time"),
            "Ticket display should contain entry time label");
    }

    /**
     * Property: Ticket timestamp corresponds to entry time.
     */
    @Property(tries = 100)
    void ticketTimestampMatchesEntryTime(
            @ForAll("licensePlates") String licensePlate,
            @ForAll("entryTimes") LocalDateTime entryTime) {
        
        ParkingLot parkingLot = createSimpleParkingLot();
        VehicleEntryController controller = new VehicleEntryController(parkingLot);
        
        String ticket = controller.generateTicket(licensePlate, entryTime);
        
        // Extract timestamp from ticket (last 14 characters)
        String[] parts = ticket.split("-");
        String timestamp = parts[parts.length - 1];
        
        // Verify timestamp format (yyyyMMddHHmmss)
        Assertions.assertEquals(14, timestamp.length(),
            "Timestamp should be 14 digits: " + timestamp);
        
        // Verify timestamp contains correct date components
        String expectedYear = String.format("%04d", entryTime.getYear());
        String expectedMonth = String.format("%02d", entryTime.getMonthValue());
        String expectedDay = String.format("%02d", entryTime.getDayOfMonth());
        
        Assertions.assertTrue(timestamp.startsWith(expectedYear),
            "Timestamp should start with year: " + expectedYear);
        Assertions.assertTrue(timestamp.substring(4, 6).equals(expectedMonth),
            "Timestamp should contain month: " + expectedMonth);
        Assertions.assertTrue(timestamp.substring(6, 8).equals(expectedDay),
            "Timestamp should contain day: " + expectedDay);
    }

    @Provide
    Arbitrary<String> licensePlates() {
        // Generate realistic license plate formats
        Arbitrary<String> letters = Arbitraries.strings()
            .withCharRange('A', 'Z')
            .ofMinLength(1)
            .ofMaxLength(3);
        Arbitrary<String> numbers = Arbitraries.strings()
            .withCharRange('0', '9')
            .ofMinLength(1)
            .ofMaxLength(4);
        
        return Combinators.combine(letters, numbers)
            .as((l, n) -> l + n);
    }

    @Provide
    Arbitrary<LocalDateTime> entryTimes() {
        return Arbitraries.integers().between(2020, 2025)
            .flatMap(year -> Arbitraries.integers().between(1, 12)
                .flatMap(month -> Arbitraries.integers().between(1, 28)
                    .flatMap(day -> Arbitraries.integers().between(0, 23)
                        .flatMap(hour -> Arbitraries.integers().between(0, 59)
                            .flatMap(minute -> Arbitraries.integers().between(0, 59)
                                .map(second -> LocalDateTime.of(year, month, day, hour, minute, second)))))));
    }

    @Provide
    Arbitrary<VehicleType> vehicleTypes() {
        return Arbitraries.of(VehicleType.values());
    }

    private ParkingLot createSimpleParkingLot() {
        ParkingLot parkingLot = new ParkingLot("Test Parking Lot");
        List<ParkingLot.RowConfiguration> rowConfigs = new ArrayList<>();
        SpotType[] spotTypes = {SpotType.REGULAR, SpotType.COMPACT};
        rowConfigs.add(new ParkingLot.RowConfiguration(2, spotTypes));
        parkingLot.createFloor(1, rowConfigs);
        return parkingLot;
    }

    private ParkingLot createParkingLotWithAllSpotTypes() {
        ParkingLot parkingLot = new ParkingLot("Test Parking Lot");
        List<ParkingLot.RowConfiguration> rowConfigs = new ArrayList<>();
        SpotType[] spotTypes = SpotType.values();
        rowConfigs.add(new ParkingLot.RowConfiguration(spotTypes.length, spotTypes));
        parkingLot.createFloor(1, rowConfigs);
        return parkingLot;
    }
}
