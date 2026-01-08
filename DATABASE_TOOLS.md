# Database Tools Used in Parking Lot Management System

## Database: MySQL + JDBC with Laragon ✅

This project uses:
- **MySQL Database** - Relational database management system
- **JDBC (Java Database Connectivity)** - Standard Java API for database access
- **Laragon** - Local development environment for MySQL

---

## What is MySQL?

**MySQL** is one of the most popular open-source relational database management systems. It is:
- Fast and reliable
- Widely used in production environments
- Supports standard SQL syntax
- ACID compliant

## What is Laragon?

**Laragon** is a portable, isolated, fast & powerful universal development environment for PHP, Node.js, Python, Java, Go, Ruby. It includes:
- MySQL Server (default port: 3306)
- Apache/Nginx web server
- phpMyAdmin for database management
- Easy to start/stop services

## What is JDBC?

**JDBC (Java Database Connectivity)** is the standard Java API for connecting to databases. It provides:
- Database connections
- SQL query execution
- Result processing
- Transaction management

---

## Prerequisites

### 1. Install Laragon
Download and install Laragon from: https://laragon.org/download/

### 2. Start MySQL in Laragon
1. Open Laragon
2. Click "Start All" or right-click and select "MySQL > Start"
3. MySQL will run on `localhost:3306`

### 3. Default Laragon MySQL Credentials
| Setting | Value |
|---------|-------|
| Host | localhost |
| Port | 3306 |
| Username | root |
| Password | (empty - no password) |

---

## MySQL Configuration

### Maven Dependency (pom.xml)
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
</dependency>
```

### JDBC Connection URL
```java
jdbc:mysql://localhost:3306/parking_lot?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

**Breakdown:**
- `jdbc:mysql://` - JDBC driver for MySQL
- `localhost:3306` - Laragon MySQL server address
- `parking_lot` - Database name (auto-created)
- `useSSL=false` - Disable SSL for local development
- `serverTimezone=UTC` - Set timezone
- `allowPublicKeyRetrieval=true` - Allow public key retrieval

### Connection Settings in Code
```java
// DatabaseManager.java
private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/parking_lot?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private static final String DEFAULT_USER = "root";
private static final String DEFAULT_PASSWORD = "";  // Laragon default: no password
```

---

## JDBC (Java Database Connectivity)

### JDBC Classes Used
All JDBC classes are from the `java.sql` package:

| JDBC Class | Purpose | Used In |
|------------|---------|---------|
| `Connection` | Database connection object | All DAO classes |
| `DriverManager` | Creates database connections | DatabaseManager.java |
| `PreparedStatement` | Parameterized SQL queries (prevents SQL injection) | All DAO classes |
| `Statement` | Basic SQL statement execution | DatabaseManager.java |
| `ResultSet` | Holds query results | All DAO classes |
| `SQLException` | Database error handling | All DAO classes |
| `Timestamp` | Date/time values for database | VehicleDAO, FineDAO, PaymentDAO |

### JDBC Import Statements
```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
```

---

## Database Schema

### Database Name
```
parking_lot
```

### Tables (7 tables with InnoDB engine)

| Table | Purpose |
|-------|---------|
| `parking_lots` | Parking lot configuration and revenue |
| `floors` | Floor information |
| `parking_spots` | Parking spot details and status |
| `vehicles` | Vehicle records |
| `parking_sessions` | Entry/exit sessions with tickets |
| `fines` | Fine records |
| `payments` | Payment transactions |

### Table Definitions (MySQL Syntax)

#### 1. parking_lots
```sql
CREATE TABLE IF NOT EXISTS parking_lots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    total_floors INT NOT NULL DEFAULT 0,
    total_revenue DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    current_fine_strategy VARCHAR(50) DEFAULT 'FIXED'
) ENGINE=InnoDB;
```

#### 2. floors
```sql
CREATE TABLE IF NOT EXISTS floors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL,
    floor_number INT NOT NULL,
    total_spots INT NOT NULL DEFAULT 0,
    FOREIGN KEY (parking_lot_id) REFERENCES parking_lots(id)
) ENGINE=InnoDB;
```

#### 3. parking_spots
```sql
CREATE TABLE IF NOT EXISTS parking_spots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    floor_id BIGINT NOT NULL,
    spot_id VARCHAR(50) NOT NULL UNIQUE,
    spot_type VARCHAR(20) NOT NULL,
    hourly_rate DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    current_vehicle_id BIGINT,
    FOREIGN KEY (floor_id) REFERENCES floors(id)
) ENGINE=InnoDB;
```

#### 4. vehicles
```sql
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    vehicle_type VARCHAR(20) NOT NULL,
    is_handicapped BOOLEAN NOT NULL DEFAULT FALSE,
    entry_time DATETIME,
    exit_time DATETIME,
    assigned_spot_id VARCHAR(50)
) ENGINE=InnoDB;
```

#### 5. parking_sessions
```sql
CREATE TABLE IF NOT EXISTS parking_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    spot_id VARCHAR(50) NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME,
    duration_hours INT,
    ticket_number VARCHAR(100) NOT NULL UNIQUE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
) ENGINE=InnoDB;
```

#### 6. fines
```sql
CREATE TABLE IF NOT EXISTS fines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    fine_type VARCHAR(30) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    issued_date DATETIME NOT NULL,
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,
    parking_session_id BIGINT,
    FOREIGN KEY (parking_session_id) REFERENCES parking_sessions(id)
) ENGINE=InnoDB;
```

#### 7. payments
```sql
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    parking_fee DECIMAL(10,2) NOT NULL,
    fine_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_date DATETIME NOT NULL,
    parking_session_id BIGINT,
    FOREIGN KEY (parking_session_id) REFERENCES parking_sessions(id)
) ENGINE=InnoDB;
```

---

## Data Access Layer (DAO) with JDBC

### DAO Classes Location
```
src/main/java/com/university/parking/dao/
```

### DAO Files (All Use JDBC)

| File | Purpose | JDBC Classes Used |
|------|---------|-------------------|
| **DatabaseManager.java** | Connection pooling, schema initialization | Connection, DriverManager, Statement |
| **VehicleDAO.java** | Vehicle CRUD operations | Connection, PreparedStatement, ResultSet, Timestamp |
| **ParkingSpotDAO.java** | Parking spot operations | Connection, PreparedStatement, ResultSet |
| **FineDAO.java** | Fine management | Connection, PreparedStatement, ResultSet, Timestamp |
| **PaymentDAO.java** | Payment records | Connection, PreparedStatement, ResultSet, Timestamp |

---

## JDBC Code Examples

### 1. DatabaseManager.java - Creating MySQL Connection
```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/parking_lot?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
}
```

### 2. Creating Database (MySQL Specific)
```java
private void createDatabaseIfNotExists() throws SQLException {
    String baseUrl = "jdbc:mysql://localhost:3306?useSSL=false&serverTimezone=UTC";
    try (Connection conn = DriverManager.getConnection(baseUrl, user, password);
         Statement stmt = conn.createStatement()) {
        stmt.execute("CREATE DATABASE IF NOT EXISTS parking_lot");
    }
}
```

### 3. INSERT with PreparedStatement
```java
public Long save(Vehicle vehicle) throws SQLException {
    String sql = "INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time) " +
                 "VALUES (?, ?, ?, ?)";
    
    Connection conn = dbManager.getConnection();
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, vehicle.getLicensePlate());
        stmt.setString(2, vehicle.getType().name());
        stmt.setBoolean(3, vehicle.isHandicapped());
        stmt.setTimestamp(4, Timestamp.valueOf(vehicle.getEntryTime()));
        
        stmt.executeUpdate();
        
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
    } finally {
        dbManager.releaseConnection(conn);
    }
    return null;
}
```

### 4. SELECT with ResultSet
```java
public Vehicle findByLicensePlate(String licensePlate) throws SQLException {
    String sql = "SELECT * FROM vehicles WHERE license_plate = ?";
    
    Connection conn = dbManager.getConnection();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, licensePlate);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return mapResultSetToVehicle(rs);
            }
        }
    } finally {
        dbManager.releaseConnection(conn);
    }
    return null;
}
```

---

## How to Use with Laragon

### Step 1: Start Laragon
1. Open Laragon application
2. Click "Start All" button
3. Wait for MySQL to start (green indicator)

### Step 2: Run the Application
```
Double-click: run-parking-system.bat
```

### Step 3: Verify Database
1. Open phpMyAdmin in Laragon (right-click > MySQL > phpMyAdmin)
2. Look for `parking_lot` database
3. Check that all 7 tables are created

---

## Viewing Data in phpMyAdmin

### Access phpMyAdmin
1. Right-click Laragon tray icon
2. Select "MySQL" > "phpMyAdmin"
3. Or open browser: http://localhost/phpmyadmin

### View Tables
1. Click on `parking_lot` database
2. Browse tables: vehicles, parking_spots, fines, payments, etc.
3. Run SQL queries directly

---

## Connection Pooling

The system implements connection pooling for efficient database access:

```java
private final BlockingQueue<Connection> connectionPool;
private static final int DEFAULT_POOL_SIZE = 10;

public Connection getConnection() throws SQLException {
    Connection conn = connectionPool.poll();
    if (conn == null || conn.isClosed()) {
        return createConnection();
    }
    return conn;
}

public void releaseConnection(Connection conn) {
    if (conn != null && !conn.isClosed()) {
        connectionPool.offer(conn);
    }
}
```

---

## Troubleshooting

### "Communications link failure"
- Make sure Laragon is running
- Check MySQL is started (green indicator in Laragon)
- Verify port 3306 is not blocked

### "Access denied for user 'root'"
- Laragon default has no password for root
- If you set a password, update `DEFAULT_PASSWORD` in DatabaseManager.java

### "Unknown database 'parking_lot'"
- The application auto-creates the database
- Or manually create: `CREATE DATABASE parking_lot;`

### "Table doesn't exist"
- Run the application once to create tables
- Or run the CREATE TABLE statements manually in phpMyAdmin

---

## Summary

| Aspect | Details |
|--------|---------|
| **Database** | MySQL |
| **Version** | 8.0+ (via Laragon) |
| **Development Tool** | Laragon |
| **Connection API** | JDBC (Java Database Connectivity) |
| **JDBC Driver** | mysql-connector-j 8.0.33 |
| **Host** | localhost |
| **Port** | 3306 |
| **Username** | root |
| **Password** | (empty) |
| **Database Name** | parking_lot |
| **Tables** | 7 tables (InnoDB engine) |
| **DAO Classes** | 5 classes (all using JDBC) |
| **Connection Pool** | 10 connections |

---

## Why MySQL + Laragon for This Project?

1. **Industry Standard** - MySQL is widely used in production
2. **Easy Setup** - Laragon provides one-click MySQL installation
3. **phpMyAdmin** - Visual database management included
4. **JDBC Compatible** - Standard Java database connectivity
5. **Reliable** - ACID compliant with InnoDB engine
6. **Scalable** - Can handle large amounts of data
7. **Free** - Open source and free to use

---

## References

- MySQL Official: https://www.mysql.com/
- Laragon: https://laragon.org/
- MySQL Connector/J: https://dev.mysql.com/downloads/connector/j/
- JDBC Tutorial: https://docs.oracle.com/javase/tutorial/jdbc/
