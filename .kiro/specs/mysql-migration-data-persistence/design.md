# MySQL Migration and Data Persistence - Design Document

## Architecture Overview

The current application creates a new parking lot in memory on every startup. We need to add a persistence layer that:
1. Saves the parking lot structure to MySQL on first run
2. Loads the parking lot structure from MySQL on subsequent runs
3. Maintains real-time synchronization of all state changes

## Component Design

### 1. ParkingLotDAO (NEW)

**Purpose:** Handle loading and saving of complete parking lot structure

**Methods:**
```java
public class ParkingLotDAO {
    // Check if parking lot exists in database
    public boolean parkingLotExists() throws SQLException
    
    // Load complete parking lot from database
    public ParkingLot loadParkingLot() throws SQLException
    
    // Save parking lot structure (for first run)
    public Long saveParkingLot(ParkingLot parkingLot) throws SQLException
    
    // Load all floors for a parking lot
    private List<Floor> loadFloors(Long parkingLotId) throws SQLException
    
    // Load all spots for a floor
    private List<ParkingSpot> loadSpots(Long floorId) throws SQLException
    
    // Load active vehicles and assign to spots
    private void loadActiveVehicles(ParkingLot parkingLot) throws SQLException
}
```

**Loading Algorithm:**
1. Query `parking_lots` table for parking lot record
2. If not found, return null (first run scenario)
3. If found, load all floors from `floors` table
4. For each floor, load all spots from `parking_spots` table
5. Query `vehicles` table for vehicles with `exit_time IS NULL` (active sessions)
6. For each active vehicle, find its spot and assign it
7. Reconstruct the complete `ParkingLot` object with all relationships

### 2. FloorDAO (NEW)

**Purpose:** Handle floor-specific operations

**Methods:**
```java
public class FloorDAO {
    // Save a floor to database
    public Long save(Long parkingLotId, Floor floor) throws SQLException
    
    // Load all floors for a parking lot
    public List<Floor> findByParkingLotId(Long parkingLotId) throws SQLException
    
    // Get floor database ID by floor number
    public Long getFloorId(Long parkingLotId, int floorNumber) throws SQLException
}
```

### 3. DatabaseManager Updates

**Changes:**
- Add constructor parameter for database name (for testing)
- Default to `parking_lot` for production
- Allow tests to use `parking_lot_test`

```java
public class DatabaseManager {
    private String databaseName = "parking_lot"; // default
    
    // Existing constructor for production
    public DatabaseManager() throws SQLException {
        this("parking_lot");
    }
    
    // New constructor for testing
    public DatabaseManager(String databaseName) throws SQLException {
        this.databaseName = databaseName;
        // Build JDBC URL with database name
        this.jdbcUrl = "jdbc:mysql://localhost:3306/" + databaseName + 
                       "?createDatabaseIfNotExist=true";
    }
}
```

### 4. ParkingApplication Startup Flow

**Current Flow:**
```
main() → initializeDatabase() → createDefaultParkingLot() → launch GUI
```

**New Flow:**
```
main() → initializeDatabase() → loadOrCreateParkingLot() → launch GUI
```

**loadOrCreateParkingLot() Logic:**
```java
private static ParkingLot loadOrCreateParkingLot(DatabaseManager dbManager) {
    ParkingLotDAO parkingLotDAO = new ParkingLotDAO(dbManager);
    
    // Try to load existing parking lot
    if (parkingLotDAO.parkingLotExists()) {
        System.out.println("Loading existing parking lot from database...");
        ParkingLot parkingLot = parkingLotDAO.loadParkingLot();
        System.out.println("Loaded parking lot with " + 
                          parkingLot.getFloors().size() + " floors");
        return parkingLot;
    }
    
    // First run - create default and save
    System.out.println("First run detected. Creating default parking lot...");
    ParkingLot parkingLot = createDefaultParkingLot();
    parkingLotDAO.saveParkingLot(parkingLot);
    System.out.println("Default parking lot saved to database");
    return parkingLot;
}
```

### 5. Test Database Configuration

**TestDatabaseConfig Utility:**
```java
public class TestDatabaseConfig {
    private static final String TEST_DB_NAME = "parking_lot_test";
    
    // Create test database manager
    public static DatabaseManager createTestDatabaseManager() throws SQLException {
        DatabaseManager dbManager = new DatabaseManager(TEST_DB_NAME);
        dbManager.initializeDatabase();
        return dbManager;
    }
    
    // Clean all tables for fresh test
    public static void cleanDatabase(DatabaseManager dbManager) throws SQLException {
        // Delete in correct order (respect foreign keys)
        executeUpdate(dbManager, "DELETE FROM payments");
        executeUpdate(dbManager, "DELETE FROM fines");
        executeUpdate(dbManager, "DELETE FROM vehicles");
        executeUpdate(dbManager, "DELETE FROM parking_spots");
        executeUpdate(dbManager, "DELETE FROM floors");
        executeUpdate(dbManager, "DELETE FROM parking_lots");
    }
}
```

## Database Schema Considerations

### Current Schema
The existing schema already supports this design:
- `parking_lots` table stores parking lot metadata
- `floors` table has `parking_lot_id` foreign key
- `parking_spots` table has `floor_id` foreign key and `status` column
- `vehicles` table has `spot_id` foreign key and `exit_time` column

### Key Relationships
```
parking_lots (1) → (N) floors
floors (1) → (N) parking_spots
parking_spots (1) → (0..1) vehicles (active)
vehicles (1) → (N) fines
vehicles (1) → (N) payments
```

### Active Vehicle Query
```sql
SELECT v.*, ps.spot_id 
FROM vehicles v
JOIN parking_spots ps ON v.spot_id = ps.id
WHERE v.exit_time IS NULL
```

## Data Synchronization Strategy

### Real-time Updates
All state changes must immediately update the database:

1. **Vehicle Entry:**
   - Insert vehicle record
   - Update spot status to OCCUPIED
   - Update spot's current_vehicle_id

2. **Vehicle Exit:**
   - Update vehicle exit_time
   - Update spot status to AVAILABLE
   - Clear spot's current_vehicle_id

3. **Fine Creation:**
   - Insert fine record immediately

4. **Payment Processing:**
   - Insert payment record
   - Update fine paid status

### Consistency Guarantees
- Use transactions for multi-table updates
- Ensure spot status always matches vehicle presence
- Validate data integrity on load

## Testing Strategy

### Unit Tests
- Test `ParkingLotDAO.loadParkingLot()` with various scenarios
- Test `ParkingLotDAO.saveParkingLot()` creates all records
- Test active vehicle loading
- Test empty database scenario

### Integration Tests
- Test complete startup flow with empty database
- Test complete startup flow with existing data
- Test data persistence across simulated restarts

### Property-Based Tests
- Update existing property tests to use MySQL test database
- Add cleanup in `@BeforeEach` to reset test database
- Ensure tests are isolated and repeatable

## Migration Steps

### Step 1: Remove H2
- Remove H2 dependency from `pom.xml`
- Verify no H2 imports remain

### Step 2: Create New DAOs
- Implement `ParkingLotDAO`
- Implement `FloorDAO`
- Add tests for each DAO

### Step 3: Update DatabaseManager
- Add database name parameter
- Update JDBC URL construction
- Test with both production and test databases

### Step 4: Update Application Startup
- Implement `loadOrCreateParkingLot()`
- Update `main()` method
- Test first run scenario
- Test subsequent run scenario

### Step 5: Update Tests
- Create `TestDatabaseConfig` utility
- Update `DataPersistenceConsistencyProperties`
- Update `FinePersistenceAcrossSessionsProperties`
- Add cleanup logic to all tests

### Step 6: Verification
- Manual test: Start app, park vehicles, close app, restart app
- Verify vehicles still present
- Verify spot status preserved
- Verify fines preserved

## Rollback Plan

If issues arise:
1. Keep H2 dependency temporarily with `<scope>test</scope>`
2. Use feature flag to enable/disable data loading
3. Fall back to in-memory mode if database load fails

## Performance Considerations

- Loading parking lot on startup: ~100-500ms (acceptable)
- Real-time updates: Already implemented in DAOs
- No performance degradation expected

## Security Considerations

- MySQL credentials in `DatabaseManager` (already handled)
- SQL injection prevention: Use PreparedStatements (already done)
- Database access control: Laragon default (user responsibility)
