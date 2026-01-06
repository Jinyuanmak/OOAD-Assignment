package com.university.parking.controller;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.VehicleDAO;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSession;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.Vehicle;
import com.university.parking.model.VehicleType;

/**
 * Controller for handling vehicle entry operations.
 * Manages spot availability filtering, spot selection, and ticket generation.
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5
 */
public class VehicleEntryController {
    private final ParkingLot parkingLot;
    private final DatabaseManager dbManager;
    private final VehicleDAO vehicleDAO;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public VehicleEntryController(ParkingLot parkingLot) {
        this(parkingLot, null);
    }
    
    public VehicleEntryController(ParkingLot parkingLot, DatabaseManager dbManager) {
        this.parkingLot = parkingLot;
        this.dbManager = dbManager;
        this.vehicleDAO = dbManager != null ? new VehicleDAO(dbManager) : null;
    }

    /**
     * Finds available spots that match the vehicle type.
     * Requirement 3.1: Display available spots matching the vehicle type
     * 
     * @param vehicleType the type of vehicle requesting entry
     * @return list of available compatible spots
     */
    public List<ParkingSpot> findAvailableSpots(VehicleType vehicleType) {
        return parkingLot.findAvailableSpots(vehicleType);
    }

    /**
     * Finds available spots for a vehicle (considering handicapped status).
     * 
     * @param vehicle the vehicle requesting entry
     * @return list of available compatible spots
     */
    public List<ParkingSpot> findAvailableSpotsForVehicle(Vehicle vehicle) {
        List<ParkingSpot> availableSpots = new ArrayList<>();
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            if (spot.isAvailable() && vehicle.canParkInSpot(spot.getType())) {
                availableSpots.add(spot);
            }
        }
        return availableSpots;
    }

    /**
     * Processes vehicle entry by selecting a spot and generating a ticket.
     * Requirements: 3.2, 3.3, 3.4, 3.5
     * 
     * @param licensePlate the vehicle's license plate
     * @param vehicleType the type of vehicle
     * @param isHandicapped whether the vehicle has handicapped status
     * @param spotId the selected spot ID
     * @return EntryResult containing the ticket and session details
     * @throws IllegalArgumentException if the spot is not available or incompatible
     */
    public EntryResult processEntry(String licensePlate, VehicleType vehicleType, 
                                    boolean isHandicapped, String spotId) {
        // Validate inputs
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate cannot be empty");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }
        if (spotId == null || spotId.trim().isEmpty()) {
            throw new IllegalArgumentException("Spot ID cannot be empty");
        }

        // Check if vehicle is already parked (prevent duplicate parking)
        String normalizedPlate = licensePlate.trim().toUpperCase();
        if (isVehicleAlreadyParked(normalizedPlate)) {
            throw new IllegalArgumentException("Vehicle " + normalizedPlate + " is already parked. Please exit first before parking again.");
        }

        // Find the spot
        ParkingSpot spot = findSpotById(spotId);
        if (spot == null) {
            throw new IllegalArgumentException("Spot not found: " + spotId);
        }

        // Check spot availability
        if (!spot.isAvailable()) {
            throw new IllegalArgumentException("Spot is not available: " + spotId);
        }

        // Create vehicle and check compatibility
        Vehicle vehicle = new Vehicle(licensePlate.trim().toUpperCase(), vehicleType, isHandicapped);
        if (!vehicle.canParkInSpot(spot.getType())) {
            throw new IllegalArgumentException("Vehicle type " + vehicleType + 
                " cannot park in spot type " + spot.getType());
        }

        // Record entry time
        LocalDateTime entryTime = LocalDateTime.now();
        vehicle.setEntryTime(entryTime);

        // Mark spot as occupied (Requirement 3.2)
        spot.occupySpot(vehicle);

        // Generate ticket (Requirements 3.4, 3.5)
        String ticketNumber = generateTicket(licensePlate, entryTime);

        // Create parking session
        ParkingSession session = new ParkingSession();
        session.setSpotId(spotId);
        session.setEntryTime(entryTime);
        session.setTicketNumber(ticketNumber);

        // Persist to database if available
        if (vehicleDAO != null) {
            try {
                vehicleDAO.save(vehicle);
            } catch (SQLException e) {
                System.err.println("Warning: Failed to persist vehicle to database: " + e.getMessage());
                // Continue without persistence
            }
        }

        return new EntryResult(vehicle, spot, session, ticketNumber);
    }

    /**
     * Generates a ticket with format "T-{PLATE}-{TIMESTAMP}".
     * Requirement 3.4: Generate ticket with proper format
     * 
     * @param licensePlate the vehicle's license plate
     * @param entryTime the entry timestamp
     * @return the generated ticket number
     */
    public String generateTicket(String licensePlate, LocalDateTime entryTime) {
        String timestamp = entryTime.format(TIMESTAMP_FORMAT);
        return "T-" + licensePlate.trim().toUpperCase() + "-" + timestamp;
    }

    /**
     * Finds a parking spot by its ID.
     * 
     * @param spotId the spot identifier
     * @return the parking spot or null if not found
     */
    private ParkingSpot findSpotById(String spotId) {
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            if (spot.getSpotId().equals(spotId)) {
                return spot;
            }
        }
        return null;
    }

    /**
     * Checks if a vehicle with the given license plate is already parked.
     * Prevents duplicate parking of the same vehicle.
     * 
     * @param licensePlate the normalized license plate (uppercase, trimmed)
     * @return true if the vehicle is already parked, false otherwise
     */
    private boolean isVehicleAlreadyParked(String licensePlate) {
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            if (!spot.isAvailable() && spot.getCurrentVehicle() != null) {
                if (licensePlate.equals(spot.getCurrentVehicle().getLicensePlate())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Result class containing entry operation details.
     */
    public static class EntryResult {
        private final Vehicle vehicle;
        private final ParkingSpot spot;
        private final ParkingSession session;
        private final String ticketNumber;

        public EntryResult(Vehicle vehicle, ParkingSpot spot, 
                          ParkingSession session, String ticketNumber) {
            this.vehicle = vehicle;
            this.spot = spot;
            this.session = session;
            this.ticketNumber = ticketNumber;
        }

        public Vehicle getVehicle() {
            return vehicle;
        }

        public ParkingSpot getSpot() {
            return spot;
        }

        public ParkingSession getSession() {
            return session;
        }

        public String getTicketNumber() {
            return ticketNumber;
        }

        /**
         * Gets a formatted display string for the ticket.
         * Requirement 3.5: Display ticket containing spot location and entry time
         */
        public String getTicketDisplay() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== PARKING TICKET ===\n");
            sb.append("Ticket Number: ").append(ticketNumber).append("\n");
            sb.append("License Plate: ").append(vehicle.getLicensePlate()).append("\n");
            sb.append("Vehicle Type: ").append(vehicle.getType()).append("\n");
            sb.append("Spot Location: ").append(spot.getSpotId()).append("\n");
            sb.append("Spot Type: ").append(spot.getType()).append("\n");
            sb.append("Hourly Rate: RM ").append(String.format("%.2f", spot.getHourlyRate())).append("\n");
            sb.append("Entry Time: ").append(vehicle.getEntryTime()).append("\n");
            sb.append("======================");
            return sb.toString();
        }
    }
}
