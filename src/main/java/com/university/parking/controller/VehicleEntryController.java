package com.university.parking.controller;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.dao.ParkingSpotDAO;
import com.university.parking.dao.ReservationDAO;
import com.university.parking.dao.VehicleDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.FineType;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSession;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.SpotStatus;
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
    private final ParkingSpotDAO spotDAO;
    private final ReservationDAO reservationDAO;
    private final FineDAO fineDAO;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final double UNAUTHORIZED_RESERVED_FINE = 100.0; // RM 100 fine for unauthorized parking in reserved spot

    public VehicleEntryController(ParkingLot parkingLot) {
        this(parkingLot, null);
    }
    
    public VehicleEntryController(ParkingLot parkingLot, DatabaseManager dbManager) {
        this.parkingLot = parkingLot;
        this.dbManager = dbManager;
        this.vehicleDAO = dbManager != null ? new VehicleDAO(dbManager) : null;
        this.spotDAO = dbManager != null ? new ParkingSpotDAO(dbManager) : null;
        this.reservationDAO = dbManager != null ? new ReservationDAO(dbManager) : null;
        this.fineDAO = dbManager != null ? new FineDAO(dbManager) : null;
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
        
        // Check if parking in reserved spot - validate reservation
        Fine unauthorizedFine = null;
        if (spot.getType() == com.university.parking.model.SpotType.RESERVED) {
            boolean hasValidReservation = checkReservation(normalizedPlate, spotId);
            if (!hasValidReservation) {
                // Issue UNAUTHORIZED_RESERVED fine
                unauthorizedFine = new Fine(normalizedPlate, FineType.UNAUTHORIZED_RESERVED, UNAUTHORIZED_RESERVED_FINE);
                unauthorizedFine.setIssuedDate(LocalDateTime.now());
                
                // Save fine to database
                if (fineDAO != null) {
                    try {
                        fineDAO.save(unauthorizedFine);
                        System.out.println("UNAUTHORIZED_RESERVED fine issued: RM " + UNAUTHORIZED_RESERVED_FINE + 
                                         " for vehicle " + normalizedPlate + " parking in spot " + spotId);
                    } catch (SQLException e) {
                        System.err.println("Warning: Failed to save unauthorized fine: " + e.getMessage());
                    }
                }
            }
        }

        // Record entry time
        LocalDateTime entryTime = LocalDateTime.now();
        vehicle.setEntryTime(entryTime);
        vehicle.setAssignedSpotId(spotId); // Set assigned spot for database persistence

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
                // Update spot status in database
                if (spotDAO != null) {
                    spotDAO.updateStatus(spotId, SpotStatus.OCCUPIED);
                }
            } catch (SQLException e) {
                System.err.println("Warning: Failed to persist vehicle to database: " + e.getMessage());
                // Continue without persistence
            }
        }

        return new EntryResult(vehicle, spot, session, ticketNumber, unauthorizedFine);
    }

    /**
     * Checks if a vehicle has a valid reservation for the specified spot.
     * @param licensePlate the vehicle's license plate
     * @param spotId the spot ID
     * @return true if vehicle has valid reservation, false otherwise
     */
    private boolean checkReservation(String licensePlate, String spotId) {
        if (reservationDAO == null) {
            return false; // No database, no reservations
        }
        
        try {
            LocalDateTime now = LocalDateTime.now();
            com.university.parking.model.Reservation reservation = 
                reservationDAO.findValidReservation(licensePlate, spotId, now);
            return reservation != null;
        } catch (SQLException e) {
            System.err.println("Error checking reservation: " + e.getMessage());
            return false;
        }
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
        private final Fine unauthorizedFine;

        public EntryResult(Vehicle vehicle, ParkingSpot spot, 
                          ParkingSession session, String ticketNumber) {
            this(vehicle, spot, session, ticketNumber, null);
        }

        public EntryResult(Vehicle vehicle, ParkingSpot spot, 
                          ParkingSession session, String ticketNumber, Fine unauthorizedFine) {
            this.vehicle = vehicle;
            this.spot = spot;
            this.session = session;
            this.ticketNumber = ticketNumber;
            this.unauthorizedFine = unauthorizedFine;
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

        public Fine getUnauthorizedFine() {
            return unauthorizedFine;
        }

        public boolean hasUnauthorizedFine() {
            return unauthorizedFine != null;
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
            sb.append("Card Holder: ").append(vehicle.isHandicapped() ? "YES" : "NO").append("\n");
            sb.append("Spot Location: ").append(spot.getSpotId()).append("\n");
            sb.append("Spot Type: ").append(spot.getType()).append("\n");
            sb.append("Hourly Rate: RM ").append(String.format("%.2f", spot.getHourlyRate())).append("\n");
            sb.append("Entry Time: ").append(vehicle.getEntryTime()).append("\n");
            
            if (hasUnauthorizedFine()) {
                sb.append("\n*** WARNING ***\n");
                sb.append("UNAUTHORIZED RESERVED SPOT PARKING\n");
                sb.append("Fine Issued: RM ").append(String.format("%.2f", unauthorizedFine.getAmount())).append("\n");
                sb.append("This fine must be paid upon exit.\n");
            }
            
            sb.append("======================");
            return sb.toString();
        }
    }
}
