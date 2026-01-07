package com.university.parking.util;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.model.Fine;
import com.university.parking.model.FineType;
import com.university.parking.model.FixedFineStrategy;
import com.university.parking.model.SpotType;
import com.university.parking.model.Vehicle;
import com.university.parking.model.VehicleType;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for fine generation rules.
 * Feature: parking-lot-management, Property 7: Fine Generation Rules
 * Validates: Requirements 5.1, 5.2, 5.5
 */
public class FineGenerationRulesProperties {
    
    private DatabaseManager dbManager;
    
    @BeforeEach
    void setUp() throws SQLException {
        dbManager = TestDatabaseConfig.createTestDatabaseManager();
        TestDatabaseConfig.cleanDatabase(dbManager);
    }
    
    @AfterEach
    void tearDown() throws SQLException {
        if (dbManager != null) {
            TestDatabaseConfig.cleanDatabase(dbManager);
            dbManager.shutdown();
        }
    }

    /**
     * Property 7: Fine Generation Rules
     * For any vehicle that stays over 24 hours or parks in reserved spots without authorization,
     * the system should generate appropriate fines linked to the license plate.
     */
    @Property(tries = 100)
    void vehicleOverstaying24HoursShouldGenerateOverstayFine(
            @ForAll @IntRange(min = 25, max = 168) int hoursParked,
            @ForAll VehicleType vehicleType
    ) {
        FineDAO fineDAO = new FineDAO(dbManager);
        FineManager fineManager = new FineManager(fineDAO);

        // Create a vehicle that has been parked for more than 24 hours
        Vehicle vehicle = new Vehicle("TEST-" + hoursParked, vehicleType, false);
        vehicle.setEntryTime(LocalDateTime.now().minusHours(hoursParked));
        vehicle.setExitTime(LocalDateTime.now());

        // Generate fines using Fixed strategy
        Fine overstayFine = fineManager.checkAndGenerateOverstayFine(vehicle, new FixedFineStrategy());

        // Assert that an overstay fine was generated
        Assertions.assertNotNull(overstayFine,
            String.format("Overstay fine should be generated for vehicle parked %d hours", hoursParked));
        Assertions.assertEquals(FineType.OVERSTAY, overstayFine.getType());
        Assertions.assertEquals(vehicle.getLicensePlate(), overstayFine.getLicensePlate());
        Assertions.assertTrue(overstayFine.getAmount() > 0,
            "Fine amount should be greater than 0");
    }

    @Property(tries = 100)
    void vehicleWithin24HoursShouldNotGenerateOverstayFine(
            @ForAll @IntRange(min = 1, max = 24) int hoursParked,
            @ForAll VehicleType vehicleType
    ) {
        FineDAO fineDAO = new FineDAO(dbManager);
        FineManager fineManager = new FineManager(fineDAO);

        // Create a vehicle that has been parked for 24 hours or less
        Vehicle vehicle = new Vehicle("TEST-" + hoursParked, vehicleType, false);
        vehicle.setEntryTime(LocalDateTime.now().minusHours(hoursParked));
        vehicle.setExitTime(LocalDateTime.now());

        // Check for overstay fine
        Fine overstayFine = fineManager.checkAndGenerateOverstayFine(vehicle, new FixedFineStrategy());

        // Assert that no overstay fine was generated
        Assertions.assertNull(overstayFine,
            String.format("No overstay fine should be generated for vehicle parked %d hours", hoursParked));
    }

    @Property(tries = 100)
    void unauthorizedVehicleInReservedSpotShouldGenerateFine(
            @ForAll VehicleType vehicleType
    ) {
        FineDAO fineDAO = new FineDAO(dbManager);
        FineManager fineManager = new FineManager(fineDAO);

        String licensePlate = "UNAUTH-" + vehicleType.name();

        // Check for unauthorized reserved parking fine
        Fine unauthorizedFine = fineManager.checkAndGenerateUnauthorizedReservedFine(
            licensePlate, SpotType.RESERVED, false, new FixedFineStrategy());

        // Assert that an unauthorized parking fine was generated
        Assertions.assertNotNull(unauthorizedFine,
            "Unauthorized parking fine should be generated for vehicle in reserved spot");
        Assertions.assertEquals(FineType.UNAUTHORIZED_RESERVED, unauthorizedFine.getType());
        Assertions.assertEquals(licensePlate, unauthorizedFine.getLicensePlate());
        Assertions.assertTrue(unauthorizedFine.getAmount() > 0,
            "Fine amount should be greater than 0");
    }

    @Property(tries = 100)
    void authorizedVehicleInReservedSpotShouldNotGenerateFine(
            @ForAll VehicleType vehicleType
    ) {
        FineDAO fineDAO = new FineDAO(dbManager);
        FineManager fineManager = new FineManager(fineDAO);

        String licensePlate = "AUTH-" + vehicleType.name();

        // Check for unauthorized reserved parking fine with authorization
        Fine unauthorizedFine = fineManager.checkAndGenerateUnauthorizedReservedFine(
            licensePlate, SpotType.RESERVED, true, new FixedFineStrategy());

        // Assert that no fine was generated
        Assertions.assertNull(unauthorizedFine,
            "No fine should be generated for authorized vehicle in reserved spot");
    }

    @Property(tries = 100)
    void vehicleInNonReservedSpotShouldNotGenerateUnauthorizedFine(
            @ForAll VehicleType vehicleType
    ) {
        FineDAO fineDAO = new FineDAO(dbManager);
        FineManager fineManager = new FineManager(fineDAO);

        String licensePlate = "REGULAR-" + vehicleType.name();

        // Test with different non-reserved spot types
        SpotType[] nonReservedSpots = {SpotType.COMPACT, SpotType.REGULAR, SpotType.HANDICAPPED};
        
        for (SpotType spotType : nonReservedSpots) {
            Fine unauthorizedFine = fineManager.checkAndGenerateUnauthorizedReservedFine(
                licensePlate, spotType, false, new FixedFineStrategy());

            // Assert that no fine was generated
            Assertions.assertNull(unauthorizedFine,
                String.format("No unauthorized fine should be generated for vehicle in %s spot", spotType));
        }
    }

    @Property(tries = 100)
    void finesShouldBeLinkedToLicensePlate(
            @ForAll @IntRange(min = 25, max = 168) int hoursParked,
            @ForAll VehicleType vehicleType
    ) {
        FineDAO fineDAO = new FineDAO(dbManager);
        FineManager fineManager = new FineManager(fineDAO);

        String licensePlate = "PLATE-" + hoursParked + "-" + vehicleType.name();
        Vehicle vehicle = new Vehicle(licensePlate, vehicleType, false);
        vehicle.setEntryTime(LocalDateTime.now().minusHours(hoursParked));
        vehicle.setExitTime(LocalDateTime.now());

        // Generate all fines
        List<Fine> fines = fineManager.generateFines(vehicle, SpotType.REGULAR, false, new FixedFineStrategy());

        // Assert that all fines are linked to the correct license plate
        for (Fine fine : fines) {
            Assertions.assertEquals(licensePlate, fine.getLicensePlate(),
                "Fine should be linked to the vehicle's license plate");
        }
    }
}
