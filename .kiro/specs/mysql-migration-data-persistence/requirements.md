# MySQL Migration and Data Persistence Fix

## Overview
Complete the migration from H2 to pure MySQL and fix the critical data persistence issue where the application doesn't load existing data from the database on restart.

## User Stories

### US-1: Pure MySQL Database (No H2)
**As a** system administrator  
**I want** the application to use only MySQL without any H2 dependencies  
**So that** I have a single, consistent database technology stack

**Acceptance Criteria:**
- Remove all H2 dependencies from `pom.xml`
- Update all test files to use MySQL instead of H2 in-memory databases
- Tests should use a test MySQL database (e.g., `parking_lot_test`)
- No H2 code or configuration should remain in the project

### US-2: Data Persistence on Application Restart
**As a** parking lot operator  
**I want** the application to load existing parking lot configuration, floors, spots, vehicles, and fines from MySQL when it starts  
**So that** all data persists across application restarts

**Acceptance Criteria:**
- On startup, check if parking lot data exists in MySQL
- If data exists, load parking lot configuration from `parking_lots` table
- Load all floors from `floors` table
- Load all parking spots from `parking_spots` table with their current status
- Load all active vehicles (not yet exited) from `vehicles` table
- Associate loaded vehicles with their parking spots
- Load all unpaid fines from `fines` table
- If no data exists, create default parking lot configuration (5 floors as currently done)
- Save the default configuration to MySQL for future sessions

### US-3: Parking Spot State Persistence
**As a** parking lot operator  
**I want** parking spot occupancy status to persist across restarts  
**So that** I don't lose track of which spots are occupied

**Acceptance Criteria:**
- When a vehicle parks, update the spot status to OCCUPIED in MySQL
- When a vehicle exits, update the spot status to AVAILABLE in MySQL
- On application restart, spots marked as OCCUPIED should remain occupied
- Occupied spots should be linked to their vehicles in memory

### US-4: Vehicle Session Persistence
**As a** parking lot operator  
**I want** active parking sessions to persist across restarts  
**So that** vehicles that were parked before a restart are still tracked

**Acceptance Criteria:**
- Vehicles without exit times are considered "active" parking sessions
- On restart, load all active vehicles and place them in their assigned spots
- Entry times, vehicle types, and handicapped status should be preserved
- Parking fees should continue to accumulate based on original entry time

## Technical Requirements

### TR-1: Database Manager Enhancement
- Add method `loadParkingLotFromDatabase()` to `DatabaseManager` or create new `ParkingLotDAO`
- Check if parking lot exists in database
- Return null if no data exists (first run)
- Return fully populated `ParkingLot` object if data exists

### TR-2: Application Startup Logic
- Modify `ParkingApplication.main()` to:
  1. Initialize database connection
  2. Attempt to load parking lot from database
  3. If loaded successfully, use loaded data
  4. If no data exists, create default parking lot and save to database

### TR-3: Test Database Configuration
- Create test database setup utility
- Tests should use `parking_lot_test` database
- Add cleanup methods to reset test database between tests
- Update `DatabaseManager` to accept database name parameter for testing

### TR-4: Data Synchronization
- Ensure all vehicle entry/exit operations update database immediately
- Ensure all spot status changes update database immediately
- Ensure all fine operations update database immediately
- No in-memory-only operations that don't persist

## Implementation Plan

### Phase 1: Remove H2 Dependencies
1. Remove H2 from `pom.xml`
2. Create test database configuration utility
3. Update test files to use MySQL test database

### Phase 2: Create Data Loading Infrastructure
1. Create `ParkingLotDAO` class for loading/saving parking lot structure
2. Add methods to load floors, spots, and vehicles
3. Add method to reconstruct `ParkingLot` object from database

### Phase 3: Update Application Startup
1. Modify `ParkingApplication.main()` to load from database
2. Add logic to save default configuration on first run
3. Test startup with empty database
4. Test startup with existing data

### Phase 4: Verify Data Persistence
1. Test vehicle parking persists across restart
2. Test spot status persists across restart
3. Test fines persist across restart
4. Test payments persist across restart

## Files to Modify

1. `pom.xml` - Remove H2 dependency
2. `src/main/java/com/university/parking/ParkingApplication.java` - Add data loading logic
3. `src/main/java/com/university/parking/dao/DatabaseManager.java` - Add database name parameter support
4. `src/main/java/com/university/parking/dao/ParkingLotDAO.java` - NEW: Create for loading parking lot structure
5. `src/test/java/com/university/parking/dao/DataPersistenceConsistencyProperties.java` - Update to use MySQL
6. `src/test/java/com/university/parking/util/FinePersistenceAcrossSessionsProperties.java` - Update to use MySQL
7. `src/test/java/com/university/parking/util/TestDatabaseConfig.java` - NEW: Create for test database setup

## Success Criteria

- Application starts successfully with empty MySQL database
- Application creates default parking lot on first run
- Application saves parking lot structure to MySQL
- Application restarts and loads existing parking lot from MySQL
- Vehicles parked before restart are still present after restart
- Spot occupancy status persists across restarts
- Unpaid fines persist across restarts
- All tests pass using MySQL test database
- No H2 dependencies remain in project
