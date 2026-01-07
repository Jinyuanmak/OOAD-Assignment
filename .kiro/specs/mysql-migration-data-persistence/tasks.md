# Implementation Tasks

## Phase 1: Remove H2 Dependencies ✓ Ready to Implement

### Task 1.1: Remove H2 from pom.xml
**File:** `pom.xml`
**Action:** Remove H2 dependency completely
**Verification:** Run `mvn clean compile` - should succeed without H2

### Task 1.2: Create TestDatabaseConfig Utility
**File:** `src/test/java/com/university/parking/util/TestDatabaseConfig.java` (NEW)
**Action:** 
- Create utility class for test database management
- Add method to create test DatabaseManager
- Add method to clean all tables
- Add method to reset database between tests

**Code:**
```java
public class TestDatabaseConfig {
    private static final String TEST_DB_NAME = "parking_lot_test";
    
    public static DatabaseManager createTestDatabaseManager() throws SQLException
    public static void cleanDatabase(DatabaseManager dbManager) throws SQLException
    public static void resetDatabase(DatabaseManager dbManager) throws SQLException
}
```

### Task 1.3: Update DatabaseManager for Test Support
**File:** `src/main/java/com/university/parking/dao/DatabaseManager.java`
**Action:**
- Add constructor parameter for database name
- Keep default constructor for production use
- Update JDBC URL to use database name parameter

**Changes:**
```java
private String databaseName;

public DatabaseManager() throws SQLException {
    this("parking_lot");
}

public DatabaseManager(String databaseName) throws SQLException {
    this.databaseName = databaseName;
    // Update JDBC URL construction
}
```

### Task 1.4: Update DataPersistenceConsistencyProperties
**File:** `src/test/java/com/university/parking/dao/DataPersistenceConsistencyProperties.java`
**Action:**
- Replace H2 in-memory database with MySQL test database
- Use `TestDatabaseConfig.createTestDatabaseManager()`
- Add cleanup logic to reset database between tests
- Update all test methods

### Task 1.5: Update FinePersistenceAcrossSessionsProperties
**File:** `src/test/java/com/university/parking/util/FinePersistenceAcrossSessionsProperties.java`
**Action:**
- Replace H2 in-memory database with MySQL test database
- Use `TestDatabaseConfig.createTestDatabaseManager()`
- Add cleanup logic
- Update all test methods

## Phase 2: Create Data Loading Infrastructure ✓ Ready to Implement

### Task 2.1: Create FloorDAO
**File:** `src/main/java/com/university/parking/dao/FloorDAO.java` (NEW)
**Action:** Create DAO for floor operations

**Methods:**
```java
public Long save(Long parkingLotId, Floor floor) throws SQLException
public List<Floor> findByParkingLotId(Long parkingLotId) throws SQLException
public Long getFloorId(Long parkingLotId, int floorNumber) throws SQLException
public Floor findById(Long id) throws SQLException
```

### Task 2.2: Create ParkingLotDAO
**File:** `src/main/java/com/university/parking/dao/ParkingLotDAO.java` (NEW)
**Action:** Create DAO for parking lot loading/saving

**Methods:**
```java
public boolean parkingLotExists() throws SQLException
public ParkingLot loadParkingLot() throws SQLException
public Long saveParkingLot(ParkingLot parkingLot) throws SQLException
private List<Floor> loadFloors(Long parkingLotId) throws SQLException
private List<ParkingSpot> loadSpots(Long floorId) throws SQLException
private void loadActiveVehicles(ParkingLot parkingLot) throws SQLException
```

**Key Logic:**
- Check if parking_lots table has any records
- Load parking lot, floors, spots in hierarchical order
- Query vehicles WHERE exit_time IS NULL
- Reconstruct Floor objects with their spots
- Assign active vehicles to their spots
- Return fully populated ParkingLot object

### Task 2.3: Update VehicleDAO for Active Vehicle Loading
**File:** `src/main/java/com/university/parking/dao/VehicleDAO.java`
**Action:** Add method to find active vehicles

**New Method:**
```java
public List<Vehicle> findActiveVehicles() throws SQLException {
    // SELECT * FROM vehicles WHERE exit_time IS NULL
}

public Vehicle findActiveBySpotId(Long spotId) throws SQLException {
    // SELECT * FROM vehicles WHERE spot_id = ? AND exit_time IS NULL
}
```

## Phase 3: Update Application Startup ✓ Ready to Implement

### Task 3.1: Add loadOrCreateParkingLot Method
**File:** `src/main/java/com/university/parking/ParkingApplication.java`
**Action:** Add method to load parking lot from database or create default

**New Method:**
```java
private static ParkingLot loadOrCreateParkingLot(DatabaseManager dbManager) 
        throws SQLException {
    ParkingLotDAO parkingLotDAO = new ParkingLotDAO(dbManager);
    
    if (parkingLotDAO.parkingLotExists()) {
        System.out.println("Loading existing parking lot from database...");
        return parkingLotDAO.loadParkingLot();
    }
    
    System.out.println("First run - creating default parking lot...");
    ParkingLot parkingLot = createDefaultParkingLot();
    parkingLotDAO.saveParkingLot(parkingLot);
    return parkingLot;
}
```

### Task 3.2: Update main() Method
**File:** `src/main/java/com/university/parking/ParkingApplication.java`
**Action:** Replace `createDefaultParkingLot()` with `loadOrCreateParkingLot()`

**Change:**
```java
// OLD:
ParkingLot parkingLot = createDefaultParkingLot();

// NEW:
ParkingLot parkingLot = loadOrCreateParkingLot(dbManager);
```

### Task 3.3: Update Vehicle Entry to Save Spot Association
**File:** `src/main/java/com/university/parking/controller/VehicleEntryController.java`
**Action:** Ensure spot status is updated when vehicle parks

**Verify:**
- VehicleDAO.save() includes spot_id
- ParkingSpotDAO.updateStatus() is called
- Both operations succeed or both fail (transaction)

### Task 3.4: Update Vehicle Exit to Clear Spot Association
**File:** `src/main/java/com/university/parking/controller/VehicleExitController.java`
**Action:** Ensure spot status is updated when vehicle exits

**Verify:**
- Vehicle exit_time is set
- Spot status updated to AVAILABLE
- Both operations succeed or both fail (transaction)

## Phase 4: Testing and Verification ✓ Ready to Test

### Task 4.1: Unit Test ParkingLotDAO
**File:** `src/test/java/com/university/parking/dao/ParkingLotDAOTest.java` (NEW)
**Action:** Create comprehensive unit tests

**Test Cases:**
- testParkingLotExistsWhenEmpty()
- testParkingLotExistsWhenPresent()
- testSaveParkingLot()
- testLoadParkingLot()
- testLoadParkingLotWithActiveVehicles()
- testLoadParkingLotPreservesSpotStatus()

### Task 4.2: Integration Test - First Run
**File:** `src/test/java/com/university/parking/integration/FirstRunIntegrationTest.java` (NEW)
**Action:** Test application startup with empty database

**Test Steps:**
1. Clean test database
2. Start application (simulate)
3. Verify default parking lot created
4. Verify data saved to database
5. Query database directly to confirm

### Task 4.3: Integration Test - Subsequent Run
**File:** `src/test/java/com/university/parking/integration/DataPersistenceIntegrationTest.java` (NEW)
**Action:** Test data persistence across restarts

**Test Steps:**
1. Clean test database
2. Create parking lot and save
3. Park some vehicles
4. Simulate restart (reload from database)
5. Verify all vehicles still present
6. Verify spot status preserved
7. Verify fines preserved

### Task 4.4: Manual Testing
**Action:** Manual verification of complete flow

**Test Scenario:**
1. Delete MySQL database `parking_lot`
2. Start application
3. Verify default parking lot created
4. Park 3 vehicles in different spots
5. Create 1 unpaid fine
6. Close application
7. Restart application
8. Verify 3 vehicles still parked
9. Verify spot status shows OCCUPIED
10. Verify unpaid fine still present

### Task 4.5: Run All Tests
**Action:** Verify all tests pass with MySQL

**Commands:**
```bash
mvn clean test
mvn clean verify
```

**Expected:** All tests pass, no H2 references

## Phase 5: Documentation Updates ✓ Ready to Document

### Task 5.1: Update README.md
**File:** `README.md`
**Action:** Update database section to reflect pure MySQL

**Changes:**
- Remove any H2 references
- Emphasize data persistence feature
- Add note about first run vs subsequent runs

### Task 5.2: Update QUICK_START.md
**File:** `QUICK_START.md`
**Action:** Update quick start guide

**Changes:**
- Clarify that data persists across restarts
- Add troubleshooting for database connection issues
- Add note about test database

### Task 5.3: Update DATABASE_TOOLS.md
**File:** `DATABASE_TOOLS.md`
**Action:** Update database documentation

**Changes:**
- Remove H2 section completely
- Add section on data persistence
- Document test database configuration

### Task 5.4: Create CHANGELOG Entry
**File:** `CHANGELOG.md`
**Action:** Document the changes

**Entry:**
```markdown
## [1.1.0] - 2026-01-07

### Changed
- Migrated from H2 to pure MySQL database
- Application now loads existing data from MySQL on startup
- Parking lot configuration, vehicles, and fines persist across restarts

### Added
- ParkingLotDAO for loading/saving parking lot structure
- FloorDAO for floor operations
- TestDatabaseConfig utility for test database management
- Data persistence integration tests

### Removed
- H2 database dependency
- In-memory database for tests
```

## Verification Checklist

- [ ] H2 dependency removed from pom.xml
- [ ] All tests use MySQL test database
- [ ] TestDatabaseConfig utility created
- [ ] FloorDAO created and tested
- [ ] ParkingLotDAO created and tested
- [ ] VehicleDAO has active vehicle methods
- [ ] ParkingApplication loads from database
- [ ] First run creates and saves default parking lot
- [ ] Subsequent runs load existing parking lot
- [ ] Vehicle parking persists across restarts
- [ ] Spot status persists across restarts
- [ ] Fines persist across restarts
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Manual testing completed successfully
- [ ] Documentation updated
- [ ] CHANGELOG updated

## Estimated Effort

- Phase 1: 2 hours
- Phase 2: 3 hours
- Phase 3: 2 hours
- Phase 4: 2 hours
- Phase 5: 1 hour

**Total: ~10 hours**
