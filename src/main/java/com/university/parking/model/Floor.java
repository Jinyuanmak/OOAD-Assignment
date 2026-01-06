package com.university.parking.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a floor in the parking lot with rows and spots.
 * Generates unique spot identifiers following the format "F{floor}-R{row}-S{spot}".
 */
public class Floor {
    private int floorNumber;
    private List<List<ParkingSpot>> rows;
    private int totalSpots;

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.rows = new ArrayList<>();
        this.totalSpots = 0;
    }

    /**
     * Creates a row with the specified number of spots and spot types.
     * @param rowNumber the row number (1-based)
     * @param spotsInRow the number of spots in this row
     * @param spotTypes the types of spots in this row (must match spotsInRow length)
     */
    public void createRow(int rowNumber, int spotsInRow, SpotType[] spotTypes) {
        if (spotTypes.length != spotsInRow) {
            throw new IllegalArgumentException("Number of spot types must match number of spots in row");
        }

        List<ParkingSpot> row = new ArrayList<>();
        for (int spotNumber = 1; spotNumber <= spotsInRow; spotNumber++) {
            String spotId = generateSpotId(floorNumber, rowNumber, spotNumber);
            SpotType spotType = spotTypes[spotNumber - 1];
            ParkingSpot spot = new ParkingSpot(spotId, spotType);
            row.add(spot);
        }
        
        // Ensure we have enough rows
        while (rows.size() < rowNumber) {
            rows.add(new ArrayList<>());
        }
        
        rows.set(rowNumber - 1, row);
        totalSpots += spotsInRow;
    }

    /**
     * Generates a unique spot identifier following the format "F{floor}-R{row}-S{spot}".
     * @param floor the floor number
     * @param row the row number
     * @param spot the spot number
     * @return the unique spot identifier
     */
    public static String generateSpotId(int floor, int row, int spot) {
        return String.format("F%d-R%d-S%d", floor, row, spot);
    }

    /**
     * Gets all spots on this floor.
     * @return list of all parking spots
     */
    public List<ParkingSpot> getAllSpots() {
        List<ParkingSpot> allSpots = new ArrayList<>();
        for (List<ParkingSpot> row : rows) {
            allSpots.addAll(row);
        }
        return allSpots;
    }

    /**
     * Gets available spots on this floor.
     * @return list of available parking spots
     */
    public List<ParkingSpot> getAvailableSpots() {
        List<ParkingSpot> availableSpots = new ArrayList<>();
        for (ParkingSpot spot : getAllSpots()) {
            if (spot.isAvailable()) {
                availableSpots.add(spot);
            }
        }
        return availableSpots;
    }

    /**
     * Finds a spot by its ID.
     * @param spotId the spot identifier
     * @return the parking spot or null if not found
     */
    public ParkingSpot findSpotById(String spotId) {
        for (ParkingSpot spot : getAllSpots()) {
            if (spot.getSpotId().equals(spotId)) {
                return spot;
            }
        }
        return null;
    }

    // Getters and setters
    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public List<List<ParkingSpot>> getRows() {
        return rows;
    }

    public void setRows(List<List<ParkingSpot>> rows) {
        this.rows = rows;
    }

    public int getTotalSpots() {
        return totalSpots;
    }

    public void setTotalSpots(int totalSpots) {
        this.totalSpots = totalSpots;
    }

    @Override
    public String toString() {
        return "Floor{" +
                "floorNumber=" + floorNumber +
                ", totalSpots=" + totalSpots +
                ", rows=" + rows.size() +
                '}';
    }
}