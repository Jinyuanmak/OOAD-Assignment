package com.university.parking.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents the main parking lot with multiple floors.
 * Manages the overall parking facility structure and operations.
 */
public class ParkingLot {
    private String name;
    private List<Floor> floors;
    private double totalRevenue;
    private FineCalculationContext fineCalculationContext;
    private LocalDateTime strategyChangeTime;

    public ParkingLot(String name) {
        this.name = name;
        this.floors = new ArrayList<>();
        this.totalRevenue = 0.0;
        this.fineCalculationContext = new FineCalculationContext();
        this.strategyChangeTime = LocalDateTime.now();
    }

    /**
     * Adds a floor to the parking lot.
     * @param floor the floor to add
     */
    public void addFloor(Floor floor) {
        floors.add(floor);
    }

    /**
     * Creates a floor with the specified configuration.
     * @param floorNumber the floor number
     * @param rows configuration for each row (number of spots and their types)
     * @return the created floor
     */
    public Floor createFloor(int floorNumber, List<RowConfiguration> rows) {
        Floor floor = new Floor(floorNumber);
        
        for (int i = 0; i < rows.size(); i++) {
            RowConfiguration rowConfig = rows.get(i);
            floor.createRow(i + 1, rowConfig.getSpotCount(), rowConfig.getSpotTypes());
        }
        
        addFloor(floor);
        return floor;
    }

    /**
     * Gets all spots across all floors.
     * @return list of all parking spots
     */
    public List<ParkingSpot> getAllSpots() {
        List<ParkingSpot> allSpots = new ArrayList<>();
        for (Floor floor : floors) {
            allSpots.addAll(floor.getAllSpots());
        }
        return allSpots;
    }

    /**
     * Validates that all spot IDs are unique across the entire parking lot.
     * @return true if all spot IDs are unique
     */
    public boolean validateUniqueSpotIds() {
        Set<String> spotIds = new HashSet<>();
        for (ParkingSpot spot : getAllSpots()) {
            if (!spotIds.add(spot.getSpotId())) {
                return false; // Duplicate found
            }
        }
        return true;
    }

    /**
     * Validates that all spot IDs follow the correct format "F{floor}-R{row}-S{spot}".
     * @return true if all spot IDs follow the correct format
     */
    public boolean validateSpotIdFormat() {
        for (ParkingSpot spot : getAllSpots()) {
            String spotId = spot.getSpotId();
            if (!spotId.matches("F\\d+-R\\d+-S\\d+")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds available spots that match the vehicle type.
     * @param vehicleType the type of vehicle
     * @return list of available compatible spots
     */
    public List<ParkingSpot> findAvailableSpots(VehicleType vehicleType) {
        List<ParkingSpot> availableSpots = new ArrayList<>();
        for (Floor floor : floors) {
            for (ParkingSpot spot : floor.getAvailableSpots()) {
                Vehicle tempVehicle = new Vehicle("TEMP", vehicleType, vehicleType == VehicleType.HANDICAPPED);
                if (tempVehicle.canParkInSpot(spot.getType())) {
                    availableSpots.add(spot);
                }
            }
        }
        return availableSpots;
    }

    /**
     * Finds a parking spot by its ID.
     * @param spotId the spot ID to search for (e.g., "F1-R2-S3")
     * @return the parking spot or null if not found
     */
    public ParkingSpot findSpotById(String spotId) {
        for (Floor floor : floors) {
            ParkingSpot spot = floor.findSpotById(spotId);
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public void setFloors(List<Floor> floors) {
        this.floors = floors;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    /**
     * Adds revenue to the total revenue.
     * @param amount the amount to add
     */
    public void addRevenue(double amount) {
        this.totalRevenue += amount;
    }

    public FineCalculationContext getFineCalculationContext() {
        return fineCalculationContext;
    }

    public void setFineCalculationContext(FineCalculationContext fineCalculationContext) {
        this.fineCalculationContext = fineCalculationContext;
    }

    /**
     * Changes the fine calculation strategy.
     * Records the time of change to ensure only future entries are affected.
     * @param strategy the new fine calculation strategy
     */
    public void changeFineStrategy(FineCalculationStrategy strategy) {
        this.fineCalculationContext.setStrategy(strategy);
        this.strategyChangeTime = LocalDateTime.now();
    }

    public LocalDateTime getStrategyChangeTime() {
        return strategyChangeTime;
    }

    public void setStrategyChangeTime(LocalDateTime strategyChangeTime) {
        this.strategyChangeTime = strategyChangeTime;
    }

    @Override
    public String toString() {
        return "ParkingLot{" +
                "name='" + name + '\'' +
                ", floors=" + floors.size() +
                ", totalRevenue=" + totalRevenue +
                '}';
    }

    /**
     * Helper class to configure rows when creating floors.
     */
    public static class RowConfiguration {
        private int spotCount;
        private SpotType[] spotTypes;

        public RowConfiguration(int spotCount, SpotType[] spotTypes) {
            this.spotCount = spotCount;
            this.spotTypes = spotTypes;
        }

        public int getSpotCount() {
            return spotCount;
        }

        public SpotType[] getSpotTypes() {
            return spotTypes;
        }
    }
}