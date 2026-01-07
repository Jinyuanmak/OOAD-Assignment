# Database Tools Used in Parking Lot Management System

## Database: H2 Database ✅

This project uses **H2 Database** - a lightweight, embedded Java SQL database.

---

## What is H2 Database?

**H2** is a relational database management system written in Java. It can be:
- Embedded in Java applications (what we use)
- Run as a standalone server
- Used for in-memory or file-based storage

### Why H2?
- ✅ **No installation required** - Just add the JAR dependency
- ✅ **Embedded** - Runs inside the Java application
- ✅ **Lightweight** - Small footprint (~2 MB)
- ✅ **Fast** - Excellent performance for small to medium applications
- ✅ **SQL Standard** - Supports standard SQL syntax
- ✅ **JDBC Compatible** - Works with standard Java database APIs
- ✅ **Persistent** - Data saved to file, survives application restart

---

## H2 Configuration

### Maven Dependency (pom.xml)
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.1.214</version>
</dependency>
```

### Connection URL
```java
jdbc:h2:./parking_lot_db;DB_CLOSE_DELAY=-1
```

**Breakdown:**
- `jdbc:h2:` - JDBC driver for H2
- `./parking_lot_db` - Database file in current directory
- `DB_CLOSE_DELAY=-1` - Keep database open while application runs

### Database File
```
parking_lot_db.mv.db  (auto-created in project root)
```

---

## Database Schema

### Tables Created

| Table | Purpose |
|-------|---------|
| `parking_lots` | Parking lot configuration and revenue |
| `floors` | Floor information |
| `parking_spots` | Parking spot details and status |
| `vehicles` | Vehicle records |
| `parking_sessions` | Entry/exit sessions with tickets |
| `fines` | Fine records |
| `payments` | Payment transactions |

### Table Definitions

#### 1. parking_lots
```sql
CREATE TABLE parking_lots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    total_floors INT NOT NULL DEFAULT 0,
    total_revenue DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    current_fine_strategy VARCHAR(50) DEFAULT 'FIXED'
);
```

#### 2. floors
```sql
CREATE TABLE floors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL,
    floor_number INT NOT NULL,
    total_spots INT NOT NULL DEFAULT 0,
    FOREIGN KEY (parking_lot_id) REFERENCES parking_lots(id)
);
```

#### 3. parking_spots
```sql
CREATE TABLE parking_spots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    floor_id BIGINT NOT NULL,
    spot_id VARCHAR(50) NOT NULL UNIQUE,
    spot_type VARCHAR(20) NOT NULL,
    hourly_rate DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    current_vehicle_id BIGINT,
    FOREIGN KEY (floor_id) REFERENCES floors(id)
);
```

#### 4. vehicles
```sql
CREATE TABLE vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    vehicle_type VARCHAR(20) NOT NULL,
    is_handicapped BOOLEAN NOT NULL DEFAULT FALSE,
    entry_time TIMESTAMP,
    exit_time TIMESTAMP,
    assigned_spot_id VARCHAR(50)
);
```

#### 5. parking_sessions
```sql
CREATE TABLE parking_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    spot_id VARCHAR(50) NOT NULL,
    entry_time TIMESTAMP NOT NULL,
    exit_time TIMESTAMP,
    duration_hours INT,
    ticket_number VARCHAR(100) NOT NULL UNIQUE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);
```

#### 6. fines
```sql
CREATE TABLE fines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    fine_type VARCHAR(30) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    issued_date TIMESTAMP NOT NULL,
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,
    parking_session_id BIGINT,
    FOREIGN KEY (parking_session_id) REFERENCES parking_sessions(id)
);
```

#### 7. payments
```sql
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    parking_fee DECIMAL(10,2) NOT NULL,
    fine_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    parking_session_id BIGINT,
    FOREIGN KEY (parking_session_id) REFERENCES parking_sessions(id)
);
```

---

## Data Access Layer (DAO)

### DAO Classes Location
```
src/main/java/com/university/parking/dao/
```

### DAO Files

| File | Purpose |
|------|---------|
| **DatabaseManager.java** | Connection pooling, schema initialization |
| **VehicleDAO.java** | Vehicle CRUD operations |
| **ParkingSpotDAO.java** | Parking spot operations |
| **FineDAO.java** | Fine management |
| **PaymentDAO.java** | Payment records |

---

## How Database is Used

### 1. DatabaseManager.java
Manages database connections and initializes schema:
```java
public class DatabaseManager {
    private static final String DEFAULT_DB_URL = "jdbc:h2:./parking_lot_db;DB_CLOSE_DELAY=-1";
    
    public void initializeDatabase() throws SQLException {
        initializeConnectionPool();
        createTables();
    }
    
    public Connection getConnection() throws SQLException {
        return connectionPool.take();
    }
}
```

### 2. VehicleDAO.java
Handles vehicle database operations:
```java
public class VehicleDAO {
    public void save(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles (license_plate, vehicle_type, ...) VALUES (?, ?, ...)";
        // Execute SQL
    }
    
    public Vehicle findByLicensePlate(String plate) throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE license_plate = ?";
        // Execute query and return vehicle
    }
}
```

### 3. FineDAO.java
Manages fine records:
```java
public class FineDAO {
    public void save(Fine fine) throws SQLException {
        String sql = "INSERT INTO fines (license_plate, fine_type, amount, ...) VALUES (?, ?, ?, ...)";
        // Execute SQL
    }
    
    public List<Fine> findUnpaidByLicensePlate(String plate) throws SQLException {
        String sql = "SELECT * FROM fines WHERE license_plate = ? AND is_paid = FALSE";
        // Execute query and return fines
    }
}
```

---

## Connection Pooling

The system implements connection pooling for efficient database access:

```java
private final BlockingQueue<Connection> connectionPool;
private static final int DEFAULT_POOL_SIZE = 10;

private void initializeConnectionPool() throws SQLException {
    for (int i = 0; i < poolSize; i++) {
        connectionPool.offer(createConnection());
    }
}

public Connection getConnection() throws SQLException {
    try {
        return connectionPool.take();
    } catch (InterruptedException e) {
        throw new SQLException("Interrupted while waiting for connection", e);
    }
}

public void releaseConnection(Connection conn) {
    if (conn != null) {
        connectionPool.offer(conn);
    }
}
```

---

## Database Features Used

### SQL Features
- ✅ **CREATE TABLE** - Schema creation
- ✅ **INSERT** - Adding records
- ✅ **SELECT** - Querying data
- ✅ **UPDATE** - Modifying records
- ✅ **DELETE** - Removing records
- ✅ **Foreign Keys** - Referential integrity
- ✅ **AUTO_INCREMENT** - Auto-generated IDs
- ✅ **Timestamps** - Date/time tracking

### JDBC Features
- ✅ **PreparedStatement** - Parameterized queries (SQL injection prevention)
- ✅ **ResultSet** - Query results processing
- ✅ **Connection pooling** - Efficient connection management
- ✅ **Transaction support** - Data integrity

---

## Data Persistence

### What Gets Saved
- ✅ Vehicle entry/exit records
- ✅ Parking spot status
- ✅ Payment transactions
- ✅ Fine records (including unpaid balances)
- ✅ Revenue totals

### Persistence Across Sessions
When you restart the application:
- All parked vehicles are remembered
- Unpaid fines persist
- Payment history is preserved
- Revenue totals are maintained

---

## Database File Location

```
Project Root/
├── parking_lot_db.mv.db      ← Main database file
├── parking_lot_db.trace.db   ← Trace/log file (optional)
└── ...
```

### To Reset Database
Delete `parking_lot_db.mv.db` and restart the application. A fresh database will be created automatically.

---

## Summary

| Aspect | Details |
|--------|---------|
| **Database** | H2 Database |
| **Version** | 2.1.214 |
| **Type** | Embedded, File-based |
| **Connection** | JDBC |
| **Tables** | 7 tables |
| **DAO Classes** | 5 classes |
| **Connection Pool** | 10 connections |
| **File** | parking_lot_db.mv.db |

---

## Why H2 for This Project?

1. **No Setup Required** - Works out of the box
2. **Portable** - Database file travels with the application
3. **Java Native** - Perfect integration with Java/Swing
4. **SQL Standard** - Easy to understand and maintain
5. **Lightweight** - Minimal resource usage
6. **Reliable** - ACID compliant transactions

---

## References

- H2 Database Official: https://www.h2database.com/
- H2 Maven: https://mvnrepository.com/artifact/com.h2database/h2
- JDBC Tutorial: https://docs.oracle.com/javase/tutorial/jdbc/
