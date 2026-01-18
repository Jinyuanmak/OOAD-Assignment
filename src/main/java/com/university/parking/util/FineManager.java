package com.university.parking.util;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.university.parking.dao.FineDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.FineCalculationStrategy;
import com.university.parking.model.FineType;
import com.university.parking.model.SpotType;
import com.university.parking.model.Vehicle;

/**
 * Manages fine generation and processing for parking violations.
 * Handles overstaying fines and unauthorized parking fines.
 */
public class FineManager {
    private static final long OVERSTAY_THRESHOLD_HOURS = 24;
    private final FineDAO fineDAO;

    public FineManager(FineDAO fineDAO) {
        this.fineDAO = fineDAO;
    }

    /**
     * Checks if a vehicle has overstayed and generates a fine if necessary.
     * Uses the elapsed_hours from the database VIEW if available, otherwise calculates manually.
     * @param vehicle the vehicle to check
     * @param strategy the fine calculation strategy to use
     * @return the generated fine, or null if no fine is needed
     */
    public Fine checkAndGenerateOverstayFine(Vehicle vehicle, FineCalculationStrategy strategy) {
        if (vehicle.getEntryTime() == null) {
            return null;
        }

        long hoursParked;
        
        // Use elapsed_hours from VIEW if available (real-time tracking)
        if (vehicle.getElapsedHours() != null) {
            hoursParked = vehicle.getElapsedHours();
        } else {
            // Fallback to manual calculation
            LocalDateTime currentTime = vehicle.getExitTime() != null ? vehicle.getExitTime() : LocalDateTime.now();
            hoursParked = ChronoUnit.HOURS.between(vehicle.getEntryTime(), currentTime);
        }

        if (hoursParked > OVERSTAY_THRESHOLD_HOURS) {
            long overstayHours = hoursParked - OVERSTAY_THRESHOLD_HOURS;
            double fineAmount = strategy.calculateFine(overstayHours);
            return new Fine(vehicle.getLicensePlate(), FineType.OVERSTAY, fineAmount);
        }

        return null;
    }

    /**
     * Checks if a vehicle is parked in a reserved spot without authorization and generates a fine.
     * @param licensePlate the vehicle's license plate
     * @param spotType the type of spot the vehicle is parked in
     * @param isAuthorized whether the vehicle is authorized for reserved parking
     * @param strategy the fine calculation strategy to use
     * @return the generated fine, or null if no fine is needed
     */
    public Fine checkAndGenerateUnauthorizedReservedFine(String licensePlate, SpotType spotType, 
                                                          boolean isAuthorized, FineCalculationStrategy strategy) {
        if (spotType == SpotType.RESERVED && !isAuthorized) {
            // For unauthorized reserved parking, use a fixed amount (1 hour overstay equivalent)
            double fineAmount = strategy.calculateFine(1);
            return new Fine(licensePlate, FineType.UNAUTHORIZED_RESERVED, fineAmount);
        }

        return null;
    }

    /**
     * Generates all applicable fines for a vehicle.
     * @param vehicle the vehicle to check
     * @param spotType the type of spot the vehicle is parked in
     * @param isAuthorizedForReserved whether the vehicle is authorized for reserved parking
     * @param strategy the fine calculation strategy to use
     * @return list of generated fines
     */
    public List<Fine> generateFines(Vehicle vehicle, SpotType spotType, boolean isAuthorizedForReserved, 
                                     FineCalculationStrategy strategy) {
        List<Fine> fines = new ArrayList<>();

        // Check for overstay fine
        Fine overstayFine = checkAndGenerateOverstayFine(vehicle, strategy);
        if (overstayFine != null) {
            fines.add(overstayFine);
        }

        // Check for unauthorized reserved parking fine
        Fine unauthorizedFine = checkAndGenerateUnauthorizedReservedFine(
            vehicle.getLicensePlate(), spotType, isAuthorizedForReserved, strategy);
        if (unauthorizedFine != null) {
            fines.add(unauthorizedFine);
        }

        return fines;
    }

    /**
     * Saves a fine to the database.
     * @param fine the fine to save
     * @return the generated fine ID
     * @throws SQLException if database operation fails
     */
    public Long saveFine(Fine fine) throws SQLException {
        return fineDAO.save(fine);
    }

    /**
     * Gets all unpaid fines for a license plate.
     * Fines persist across parking sessions.
     * @param licensePlate the license plate to check
     * @return list of unpaid fines
     * @throws SQLException if database operation fails
     */
    public List<Fine> getUnpaidFines(String licensePlate) throws SQLException {
        return fineDAO.findUnpaidByLicensePlate(licensePlate);
    }

    /**
     * Marks fines as paid.
     * @param fineIds the IDs of fines to mark as paid
     * @throws SQLException if database operation fails
     */
    public void markFinesAsPaid(List<Long> fineIds) throws SQLException {
        for (Long fineId : fineIds) {
            fineDAO.markAsPaid(fineId);
        }
    }

    /**
     * Calculates the total amount of unpaid fines for a license plate.
     * @param licensePlate the license plate to check
     * @return the total unpaid fine amount
     * @throws SQLException if database operation fails
     */
    public double calculateTotalUnpaidFines(String licensePlate) throws SQLException {
        List<Fine> unpaidFines = getUnpaidFines(licensePlate);
        return unpaidFines.stream()
                .mapToDouble(Fine::getAmount)
                .sum();
    }
}
