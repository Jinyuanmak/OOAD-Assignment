package com.university.parking.controller;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.dao.ParkingLotDAO;
import com.university.parking.dao.ParkingSpotDAO;
import com.university.parking.dao.PaymentDAO;
import com.university.parking.dao.VehicleDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.FixedFineStrategy;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.Payment;
import com.university.parking.model.PaymentMethod;
import com.university.parking.model.SpotStatus;
import com.university.parking.model.Vehicle;
import com.university.parking.util.FeeCalculator;
import com.university.parking.util.FineManager;
import com.university.parking.util.PaymentProcessor;
import com.university.parking.util.Receipt;

/**
 * Controller for handling vehicle exit operations.
 * Manages vehicle lookup, fee calculation, fine checking, and payment processing.
 * 
 * Requirements: 4.1, 4.2, 4.5, 4.6, 4.8
 */
public class VehicleExitController {
    private final ParkingLot parkingLot;
    private final DatabaseManager dbManager;
    private final FineDAO fineDAO;
    private final FineManager fineManager;
    private final PaymentDAO paymentDAO;
    private final ParkingSpotDAO spotDAO;
    private final VehicleDAO vehicleDAO;
    private final ParkingLotDAO parkingLotDAO;
    private final List<Fine> unpaidFines;

    public VehicleExitController(ParkingLot parkingLot) {
        this(parkingLot, null, null);
    }
    
    public VehicleExitController(ParkingLot parkingLot, DatabaseManager dbManager, FineDAO fineDAO) {
        this.parkingLot = parkingLot;
        this.dbManager = dbManager;
        this.fineDAO = fineDAO;
        this.fineManager = fineDAO != null ? new FineManager(fineDAO) : null;
        this.paymentDAO = dbManager != null ? new PaymentDAO(dbManager) : null;
        this.spotDAO = dbManager != null ? new ParkingSpotDAO(dbManager) : null;
        this.vehicleDAO = dbManager != null ? new VehicleDAO(dbManager) : null;
        this.parkingLotDAO = dbManager != null ? new ParkingLotDAO(dbManager) : null;
        this.unpaidFines = new ArrayList<>();
    }

    /**
     * Looks up a vehicle by license plate.
     * Requirement 4.2: Locate the vehicle and retrieve entry time
     * 
     * @param licensePlate the vehicle's license plate
     * @return VehicleLookupResult containing vehicle and spot info, or null if not found
     */
    public VehicleLookupResult lookupVehicle(String licensePlate) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate cannot be empty");
        }

        String normalizedPlate = licensePlate.trim().toUpperCase();
        
        // First, try to load vehicle from database to get elapsed_hours
        Vehicle dbVehicle = null;
        if (vehicleDAO != null) {
            try {
                dbVehicle = vehicleDAO.findByLicensePlate(normalizedPlate);
            } catch (SQLException e) {
                System.err.println("Warning: Failed to load vehicle from database: " + e.getMessage());
            }
        }
        
        // Search in-memory parking lot for the spot
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            if (!spot.isAvailable() && spot.getCurrentVehicle() != null) {
                Vehicle vehicle = spot.getCurrentVehicle();
                if (normalizedPlate.equals(vehicle.getLicensePlate())) {
                    // If we have database vehicle with elapsed_hours, use those values
                    if (dbVehicle != null && dbVehicle.getElapsedHours() != null) {
                        vehicle.setElapsedSeconds(dbVehicle.getElapsedSeconds());
                        vehicle.setElapsedMinutes(dbVehicle.getElapsedMinutes());
                        vehicle.setElapsedHours(dbVehicle.getElapsedHours());
                        vehicle.setIsOverstay(dbVehicle.getIsOverstay());
                    }
                    return new VehicleLookupResult(vehicle, spot);
                }
            }
        }
        
        // If not found in memory but exists in database, load from database
        if (dbVehicle != null && dbVehicle.getAssignedSpotId() != null) {
            // Find the spot by ID
            ParkingSpot spot = parkingLot.findSpotById(dbVehicle.getAssignedSpotId());
            if (spot != null) {
                // Sync in-memory spot with database state
                if (spot.isAvailable()) {
                    // Spot is available in memory but vehicle is in database - occupy it
                    spot.occupySpot(dbVehicle);
                }
                return new VehicleLookupResult(dbVehicle, spot);
            }
        }
        
        return null;
    }

    /**
     * Generates a payment summary for a vehicle exit.
     * Requirements: 4.3, 4.4, 4.5, 4.6
     * 
     * @param licensePlate the vehicle's license plate
     * @param unpaidFinesList list of unpaid fines for this license plate
     * @return PaymentSummary containing all charges
     * @throws IllegalArgumentException if vehicle not found
     */
    public PaymentSummary generatePaymentSummary(String licensePlate, List<Fine> unpaidFinesList) {
        VehicleLookupResult lookupResult = lookupVehicle(licensePlate);
        if (lookupResult == null) {
            throw new IllegalArgumentException("Vehicle not found: " + licensePlate);
        }

        Vehicle vehicle = lookupResult.getVehicle();
        ParkingSpot spot = lookupResult.getSpot();

        // Set exit time to now for calculation
        LocalDateTime exitTime = LocalDateTime.now();
        vehicle.setExitTime(exitTime);

        // Calculate duration - use elapsed_hours from VIEW if available, otherwise calculate manually
        long durationHours;
        if (vehicle.getElapsedHours() != null && vehicle.getElapsedHours() > 0) {
            // Use elapsed_hours from database VIEW (timezone-aware)
            durationHours = vehicle.getElapsedHours();
        } else {
            // Fallback to manual calculation (ceiling rounded)
            durationHours = vehicle.calculateParkingDuration();
        }
        
        if (durationHours == 0) {
            durationHours = 1; // Minimum 1 hour charge
        }

        // Calculate parking fee
        double parkingFee = FeeCalculator.calculateParkingFee(vehicle, spot, durationHours);

        // Calculate total fines
        double totalFines = 0.0;
        if (unpaidFinesList != null) {
            for (Fine fine : unpaidFinesList) {
                totalFines += fine.getAmount();
            }
        }

        // Calculate total due
        double totalDue = FeeCalculator.calculateTotalAmount(parkingFee, totalFines);

        return new PaymentSummary(
            vehicle,
            spot,
            durationHours,
            parkingFee,
            unpaidFinesList != null ? unpaidFinesList : new ArrayList<>(),
            totalFines,
            totalDue
        );
    }


    /**
     * Processes vehicle exit with payment.
     * Requirements: 4.7, 4.8
     * 
     * @param licensePlate the vehicle's license plate
     * @param amountPaid the amount being paid
     * @param paymentMethod the payment method
     * @param unpaidFinesList list of unpaid fines
     * @return ExitResult containing receipt and transaction details
     * @throws IllegalArgumentException if vehicle not found or payment invalid
     */
    public ExitResult processExit(String licensePlate, double amountPaid, 
                                  PaymentMethod paymentMethod, List<Fine> unpaidFinesList) {
        // Generate payment summary
        PaymentSummary summary = generatePaymentSummary(licensePlate, unpaidFinesList);
        
        // Validate payment
        boolean isPaymentSufficient = PaymentProcessor.validatePayment(amountPaid, summary.getTotalDue());
        double remainingBalance = PaymentProcessor.calculateRemainingBalance(amountPaid, summary.getTotalDue());

        // Process payment
        Payment payment = PaymentProcessor.processPayment(
            licensePlate,
            summary.getParkingFee(),
            summary.getTotalFines(),
            amountPaid,
            paymentMethod
        );

        // Generate receipt
        Receipt receipt = PaymentProcessor.generateReceipt(
            licensePlate,
            summary.getVehicle().getEntryTime(),
            summary.getVehicle().getExitTime(),
            summary.getDurationHours(),
            summary.getParkingFee(),
            summary.getTotalFines(),
            amountPaid,
            paymentMethod,
            summary.getSpot().getSpotId()
        );

        // Mark spot as available (Requirement 4.7)
        summary.getSpot().vacateSpot();
        
        // Update vehicle exit time in database
        if (vehicleDAO != null) {
            try {
                vehicleDAO.updateExitTime(licensePlate.trim().toUpperCase(), summary.getVehicle().getExitTime());
            } catch (SQLException e) {
                System.err.println("Warning: Failed to update vehicle exit time in database: " + e.getMessage());
            }
        }
        
        // Update spot status in database
        if (spotDAO != null) {
            try {
                spotDAO.updateStatus(summary.getSpot().getSpotId(), SpotStatus.AVAILABLE);
            } catch (SQLException e) {
                System.err.println("Warning: Failed to update spot status in database: " + e.getMessage());
            }
        }

        // Update parking lot revenue
        parkingLot.setTotalRevenue(parkingLot.getTotalRevenue() + amountPaid);
        
        // Update revenue in database
        if (parkingLotDAO != null) {
            try {
                parkingLotDAO.updateRevenue(parkingLot.getTotalRevenue());
            } catch (SQLException e) {
                System.err.println("Warning: Failed to update revenue in database: " + e.getMessage());
            }
        }

        // Persist payment to database if available
        if (paymentDAO != null) {
            try {
                paymentDAO.save(payment);
            } catch (SQLException e) {
                System.err.println("Warning: Failed to persist payment to database: " + e.getMessage());
                // Continue without persistence
            }
        }

        // ALWAYS mark the original fines as paid (even for partial payment)
        // The unpaid balance fine will represent what's still owed
        if (fineDAO != null && unpaidFinesList != null) {
            for (Fine fine : unpaidFinesList) {
                if (fine.getId() != null) {
                    try {
                        fineDAO.markAsPaid(fine.getId());
                        fine.setPaid(true);
                    } catch (SQLException e) {
                        System.err.println("Warning: Failed to update fine status: " + e.getMessage());
                    }
                }
            }
        }

        // If payment is insufficient, create a fine for the remaining balance
        if (!isPaymentSufficient && remainingBalance > 0 && fineDAO != null) {
            try {
                Fine unpaidBalanceFine = new Fine(
                    licensePlate.trim().toUpperCase(),
                    com.university.parking.model.FineType.UNPAID_BALANCE,
                    remainingBalance
                );
                unpaidBalanceFine.setPaid(false);
                fineDAO.save(unpaidBalanceFine);
            } catch (SQLException e) {
                System.err.println("Warning: Failed to create unpaid balance fine: " + e.getMessage());
            }
        }

        return new ExitResult(
            summary,
            payment,
            receipt,
            isPaymentSufficient,
            remainingBalance
        );
    }

    /**
     * Adds unpaid fines to track for a license plate.
     * Used for testing and fine management.
     * 
     * @param fine the fine to add
     */
    public void addUnpaidFine(Fine fine) {
        unpaidFines.add(fine);
    }

    /**
     * Gets unpaid fines for a license plate.
     * Requirement 4.5: Check for unpaid fines linked to the license plate
     * Also generates overstay fine if vehicle has overstayed.
     * 
     * @param licensePlate the license plate to check
     * @return list of unpaid fines
     */
    public List<Fine> getUnpaidFines(String licensePlate) {
        List<Fine> finesForPlate = new ArrayList<>();
        String normalizedPlate = licensePlate.trim().toUpperCase();
        
        // Check if vehicle has overstayed and generate fine if needed
        VehicleLookupResult lookupResult = lookupVehicle(normalizedPlate);
        if (lookupResult != null && fineManager != null) {
            Vehicle vehicle = lookupResult.getVehicle();
            
            // Check if vehicle has overstayed (using elapsed_hours from VIEW or isOverstay flag)
            boolean hasOverstayed = false;
            if (vehicle.getIsOverstay() != null && vehicle.getIsOverstay()) {
                hasOverstayed = true;
            } else if (vehicle.getElapsedHours() != null && vehicle.getElapsedHours() > 24) {
                hasOverstayed = true;
            }
            
            // Generate overstay fine if needed
            if (hasOverstayed) {
                try {
                    // Check if overstay fine already exists for this vehicle
                    List<Fine> existingFines = fineDAO.findUnpaidByLicensePlate(normalizedPlate);
                    boolean hasOverstayFine = existingFines.stream()
                        .anyMatch(f -> f.getType() == com.university.parking.model.FineType.OVERSTAY);
                    
                    if (!hasOverstayFine) {
                        // Generate and save overstay fine using FixedFineStrategy (RM 50)
                        Fine overstayFine = fineManager.checkAndGenerateOverstayFine(
                            vehicle, 
                            new FixedFineStrategy()
                        );
                        
                        if (overstayFine != null) {
                            fineDAO.save(overstayFine);
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Warning: Failed to generate overstay fine: " + e.getMessage());
                }
            }
        }
        
        // Try to get all fines from database
        if (fineDAO != null) {
            try {
                finesForPlate = fineDAO.findUnpaidByLicensePlate(normalizedPlate);
                return finesForPlate;
            } catch (SQLException e) {
                System.err.println("Warning: Failed to retrieve fines from database: " + e.getMessage());
                // Fall back to in-memory list
            }
        }
        
        // Fall back to in-memory list
        for (Fine fine : unpaidFines) {
            if (!fine.isPaid() && normalizedPlate.equals(fine.getLicensePlate())) {
                finesForPlate.add(fine);
            }
        }
        return finesForPlate;
    }

    /**
     * Result class for vehicle lookup operations.
     */
    public static class VehicleLookupResult {
        private final Vehicle vehicle;
        private final ParkingSpot spot;

        public VehicleLookupResult(Vehicle vehicle, ParkingSpot spot) {
            this.vehicle = vehicle;
            this.spot = spot;
        }

        public Vehicle getVehicle() {
            return vehicle;
        }

        public ParkingSpot getSpot() {
            return spot;
        }
    }

    /**
     * Payment summary containing all charges for vehicle exit.
     * Requirement 4.6: Show hours parked, parking fee, unpaid fines, and total due
     */
    public static class PaymentSummary {
        private final Vehicle vehicle;
        private final ParkingSpot spot;
        private final long durationHours;
        private final double parkingFee;
        private final List<Fine> unpaidFines;
        private final double totalFines;
        private final double totalDue;

        public PaymentSummary(Vehicle vehicle, ParkingSpot spot, long durationHours,
                             double parkingFee, List<Fine> unpaidFines, 
                             double totalFines, double totalDue) {
            this.vehicle = vehicle;
            this.spot = spot;
            this.durationHours = durationHours;
            this.parkingFee = parkingFee;
            this.unpaidFines = unpaidFines;
            this.totalFines = totalFines;
            this.totalDue = totalDue;
        }

        public Vehicle getVehicle() {
            return vehicle;
        }

        public ParkingSpot getSpot() {
            return spot;
        }

        public long getDurationHours() {
            return durationHours;
        }

        public double getParkingFee() {
            return parkingFee;
        }

        public List<Fine> getUnpaidFines() {
            return unpaidFines;
        }

        public double getTotalFines() {
            return totalFines;
        }

        public double getTotalDue() {
            return totalDue;
        }

        /**
         * Generates a formatted payment summary display.
         */
        public String getDisplayText() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== PAYMENT SUMMARY ===\n");
            sb.append("License Plate: ").append(vehicle.getLicensePlate()).append("\n");
            sb.append("Spot: ").append(spot.getSpotId()).append("\n");
            sb.append("Entry Time: ").append(vehicle.getEntryTime()).append("\n");
            sb.append("Exit Time: ").append(vehicle.getExitTime()).append("\n");
            sb.append("-----------------------\n");
            sb.append("Hours Parked: ").append(durationHours).append("\n");
            sb.append("Parking Fee: RM ").append(String.format("%.2f", parkingFee)).append("\n");
            sb.append("Unpaid Fines: RM ").append(String.format("%.2f", totalFines)).append("\n");
            sb.append("-----------------------\n");
            sb.append("TOTAL DUE: RM ").append(String.format("%.2f", totalDue)).append("\n");
            sb.append("=======================");
            return sb.toString();
        }
    }

    /**
     * Result class for exit operations.
     */
    public static class ExitResult {
        private final PaymentSummary summary;
        private final Payment payment;
        private final Receipt receipt;
        private final boolean paymentSufficient;
        private final double remainingBalance;

        public ExitResult(PaymentSummary summary, Payment payment, Receipt receipt,
                         boolean paymentSufficient, double remainingBalance) {
            this.summary = summary;
            this.payment = payment;
            this.receipt = receipt;
            this.paymentSufficient = paymentSufficient;
            this.remainingBalance = remainingBalance;
        }

        public PaymentSummary getSummary() {
            return summary;
        }

        public Payment getPayment() {
            return payment;
        }

        public Receipt getReceipt() {
            return receipt;
        }

        public boolean isPaymentSufficient() {
            return paymentSufficient;
        }

        public double getRemainingBalance() {
            return remainingBalance;
        }
    }
}
