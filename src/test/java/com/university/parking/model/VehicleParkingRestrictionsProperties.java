package com.university.parking.model;

import org.junit.jupiter.api.Assertions;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

/**
 * Property-based tests for vehicle parking restrictions.
 * Feature: parking-lot-management, Property 2: Vehicle Parking Restrictions
 * Validates: Requirements 2.2, 2.3, 2.4, 2.5
 */
public class VehicleParkingRestrictionsProperties {

    /**
     * Property 2: Vehicle Parking Restrictions
     * For any vehicle and spot combination, the system should only allow parking if the vehicle type 
     * is compatible with the spot type (motorcycles in compact only, cars in compact/regular, 
     * SUV/trucks in regular only, handicapped vehicles in any)
     */
    @Property(tries = 100)
    void vehicleParkingRestrictions(
            @ForAll VehicleType vehicleType,
            @ForAll SpotType spotType,
            @ForAll boolean isHandicapped) {
        
        // Create a vehicle with the given type and handicapped status
        String licensePlate = "TEST" + vehicleType.name() + spotType.name();
        Vehicle vehicle = new Vehicle(licensePlate, vehicleType, isHandicapped);
        
        boolean canPark = vehicle.canParkInSpot(spotType);
        
        // If the vehicle is handicapped (regardless of type), it can park anywhere
        if (isHandicapped) {
            Assertions.assertTrue(canPark, 
                "Handicapped vehicles should be able to park in any spot type: " + 
                vehicleType + " in " + spotType);
            return;
        }
        
        // Check parking restrictions based on vehicle type
        switch (vehicleType) {
            case MOTORCYCLE:
                // Motorcycles can only park in COMPACT spots
                if (spotType == SpotType.COMPACT) {
                    Assertions.assertTrue(canPark, 
                        "Motorcycles should be able to park in COMPACT spots");
                } else {
                    Assertions.assertFalse(canPark, 
                        "Motorcycles should NOT be able to park in " + spotType + " spots");
                }
                break;
                
            case CAR:
                // Cars can park in COMPACT or REGULAR spots
                if (spotType == SpotType.COMPACT || spotType == SpotType.REGULAR) {
                    Assertions.assertTrue(canPark, 
                        "Cars should be able to park in " + spotType + " spots");
                } else {
                    Assertions.assertFalse(canPark, 
                        "Cars should NOT be able to park in " + spotType + " spots");
                }
                break;
                
            case SUV_TRUCK:
                // SUV/Trucks can only park in REGULAR spots
                if (spotType == SpotType.REGULAR) {
                    Assertions.assertTrue(canPark, 
                        "SUV/Trucks should be able to park in REGULAR spots");
                } else {
                    Assertions.assertFalse(canPark, 
                        "SUV/Trucks should NOT be able to park in " + spotType + " spots");
                }
                break;
                
            case HANDICAPPED:
                // HANDICAPPED vehicle type can park anywhere (same as isHandicapped flag)
                Assertions.assertTrue(canPark, 
                    "HANDICAPPED vehicle type should be able to park in any spot type: " + spotType);
                break;
                
            default:
                Assertions.fail("Unknown vehicle type: " + vehicleType);
        }
    }

    /**
     * Property test specifically for handicapped vehicles to ensure they can park anywhere.
     */
    @Property(tries = 100)
    void handicappedVehiclesCanParkAnywhere(
            @ForAll VehicleType vehicleType,
            @ForAll SpotType spotType) {
        
        // Create a handicapped vehicle
        String licensePlate = "HANDICAPPED" + vehicleType.name();
        Vehicle handicappedVehicle = new Vehicle(licensePlate, vehicleType, true);
        
        boolean canPark = handicappedVehicle.canParkInSpot(spotType);
        
        Assertions.assertTrue(canPark, 
            "Handicapped vehicles should be able to park in any spot type: " + 
            vehicleType + " in " + spotType);
    }

    /**
     * Property test for non-handicapped vehicles to ensure restrictions are enforced.
     */
    @Property(tries = 100)
    void nonHandicappedVehicleRestrictions(
            @ForAll VehicleType vehicleType,
            @ForAll SpotType spotType) {
        
        // Skip HANDICAPPED vehicle type as it's always allowed
        if (vehicleType == VehicleType.HANDICAPPED) {
            return;
        }
        
        // Create a non-handicapped vehicle
        String licensePlate = "REGULAR" + vehicleType.name();
        Vehicle regularVehicle = new Vehicle(licensePlate, vehicleType, false);
        
        boolean canPark = regularVehicle.canParkInSpot(spotType);
        
        // Verify the specific restrictions
        switch (vehicleType) {
            case MOTORCYCLE:
                boolean motorcycleCanPark = (spotType == SpotType.COMPACT);
                Assertions.assertEquals(motorcycleCanPark, canPark,
                    "Motorcycle parking restriction violated for spot type: " + spotType);
                break;
                
            case CAR:
                boolean carCanPark = (spotType == SpotType.COMPACT || spotType == SpotType.REGULAR);
                Assertions.assertEquals(carCanPark, canPark,
                    "Car parking restriction violated for spot type: " + spotType);
                break;
                
            case SUV_TRUCK:
                boolean suvCanPark = (spotType == SpotType.REGULAR);
                Assertions.assertEquals(suvCanPark, canPark,
                    "SUV/Truck parking restriction violated for spot type: " + spotType);
                break;
        }
    }

    /**
     * Property test to verify that vehicle type HANDICAPPED behaves the same as isHandicapped flag.
     */
    @Property(tries = 100)
    void handicappedTypeVsFlagConsistency(@ForAll SpotType spotType) {
        
        // Create two vehicles: one with HANDICAPPED type, one with isHandicapped flag
        Vehicle handicappedTypeVehicle = new Vehicle("TYPE", VehicleType.HANDICAPPED, false);
        Vehicle handicappedFlagVehicle = new Vehicle("FLAG", VehicleType.CAR, true);
        
        boolean typeCanPark = handicappedTypeVehicle.canParkInSpot(spotType);
        boolean flagCanPark = handicappedFlagVehicle.canParkInSpot(spotType);
        
        Assertions.assertTrue(typeCanPark, 
            "HANDICAPPED vehicle type should be able to park in " + spotType);
        Assertions.assertTrue(flagCanPark, 
            "Vehicle with handicapped flag should be able to park in " + spotType);
        Assertions.assertEquals(typeCanPark, flagCanPark, 
            "HANDICAPPED type and handicapped flag should have same parking permissions");
    }
}