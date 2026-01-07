# University Parking Lot Management System

A comprehensive parking facility management system **built with Java Swing GUI**, featuring real-time vehicle tracking, automated fee calculation, fine management, and persistent data storage.

## GUI Technology

**100% Java Swing Implementation**
- Complete graphical user interface built with Java Swing components
- Tabbed interface with 4 main panels (Admin, Entry, Exit, Reports)
- Professional forms, tables, buttons, and dialogs
- See `JAVA_SWING_USAGE.md` for detailed Swing component documentation

## Features

### Core Functionality
- **Multi-Floor Parking Management** - 5 floors with 75 total parking spots
- **Multiple Spot Types** - Compact, Regular, Handicapped, and Reserved spots
- **Vehicle Entry & Exit** - Automated ticket generation and payment processing
- **Real-Time Tracking** - Monitor all parked vehicles and spot availability
- **Fine Management** - Automated fine generation for violations and unpaid balances
- **Payment Processing** - Support for Cash and Credit Card payments
- **Persistent Storage** - H2 database for data persistence across sessions

### Advanced Features
- **Unpaid Balance Tracking** - Partial payments create fines that persist across sessions
- **Handicapped Pricing** - Special RM 2/hour rate for handicapped vehicles in designated spots
- **Progressive Fine System** - Configurable fine calculation strategies
- **Comprehensive Reporting** - Revenue, occupancy, vehicle, and fine reports
- **Admin Panel** - System statistics and fine management

## Quick Start

### Prerequisites
- Java 11 or higher
- Maven (included in project: `apache-maven-3.9.12`)

### Running the Application

**Easiest Method - Double-Click:**
```
Double-click: run-parking-system.bat
```

**Alternative Methods:**
```bash
# Using Maven
apache-maven-3.9.12\bin\mvn.cmd exec:java

# Using JAR directly
java -jar target\parking-lot-management.jar
```

## Project Structure

```
parking-lot-management/
├── src/
│   ├── main/java/com/university/parking/
│   │   ├── controller/          # Business logic controllers
│   │   ├── dao/                 # Database access layer
│   │   ├── model/               # Domain models
│   │   ├── util/                # Utility classes
│   │   ├── view/                # GUI components
│   │   └── ParkingApplication.java
│   └── test/java/               # Comprehensive test suite (146 tests)
├── target/
│   └── parking-lot-management.jar  # Executable JAR (2.6 MB)
├── pom.xml                      # Maven configuration
└── run-parking-system.bat       # Quick launch script
```

## System Architecture

### Layers
1. **View Layer** - Java Swing GUI components
2. **Controller Layer** - Business logic and workflow management
3. **Model Layer** - Domain entities and business rules
4. **DAO Layer** - Database persistence
5. **Util Layer** - Helper classes and utilities

### Key Components
- **ParkingLot** - Main domain model managing floors and spots
- **VehicleEntryController** - Handles vehicle parking operations
- **VehicleExitController** - Manages exit and payment processing
- **FeeCalculator** - Calculates parking fees based on duration and spot type
- **FineManager** - Generates and manages fines
- **DatabaseManager** - H2 database connection and schema management

## Pricing Structure

| Spot Type | Rate | Special Conditions |
|-----------|------|-------------------|
| Compact | RM 2/hour | Motorcycles only |
| Regular | RM 5/hour | Cars, SUVs, Trucks |
| Handicapped | RM 2/hour | FREE for handicapped card holders (RM 2/hour rate) |
| Reserved | RM 10/hour | VIP customers only |

**Minimum charge:** 1 hour (ceiling rounded)

## Fine System

| Fine Type | Amount | Trigger |
|-----------|--------|---------|
| Unauthorized Parking | RM 50 | Non-authorized vehicle in reserved spot |
| Overstay | Configurable | Vehicle parked > 24 hours |
| Unpaid Balance | Variable | Insufficient payment on exit |

### Fine Calculation Schemes (Admin Selectable)

**Option A: Fixed Fine Scheme**
- Flat RM 50 fine for overstaying

**Option B: Progressive Fine Scheme**
- First 24 hours: RM 50
- Hours 24-48: Additional RM 100
- Hours 48-72: Additional RM 150
- Above 72 hours: Additional RM 200

**Option C: Hourly Fine Scheme**
- RM 20 per hour for overstaying

Admin can select any scheme from the Admin Panel. The selected scheme applies to future entries only.

## Database Technology

**H2 Database - Embedded Java SQL Database**

### Why H2?
- ✅ No installation required - embedded in the application
- ✅ Lightweight (~2 MB) and fast
- ✅ Data persists to file across sessions
- ✅ Standard SQL syntax
- ✅ JDBC compatible

### Configuration
```xml
<!-- Maven Dependency (pom.xml) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.1.214</version>
</dependency>
```

```java
// Connection URL
jdbc:h2:./parking_lot_db;DB_CLOSE_DELAY=-1
```

### Database Tables (7 tables)
| Table | Purpose |
|-------|---------|
| `parking_lots` | Parking lot configuration and revenue |
| `floors` | Floor information |
| `parking_spots` | Spot details and status |
| `vehicles` | Vehicle entry/exit records |
| `parking_sessions` | Entry/exit sessions with tickets |
| `fines` | Fine records and payment status |
| `payments` | Payment transactions |

### DAO Classes (Data Access Layer)
Located in `src/main/java/com/university/parking/dao/`:
| File | Purpose |
|------|---------|
| **DatabaseManager.java** | Connection pooling (10 connections), schema initialization |
| **VehicleDAO.java** | Vehicle CRUD operations |
| **ParkingSpotDAO.java** | Parking spot operations |
| **FineDAO.java** | Fine management |
| **PaymentDAO.java** | Payment records |

### Database Features
- ✅ Connection pooling for efficient access
- ✅ Prepared statements (SQL injection prevention)
- ✅ Foreign key relationships
- ✅ ACID transactions
- ✅ Auto-generated IDs

### Database File
```
parking_lot_db.mv.db  (auto-created in project root)
```

**To reset database:** Delete `parking_lot_db.mv.db` and restart the application.

See `DATABASE_TOOLS.md` for complete database documentation.

## Testing

The project includes 146 comprehensive tests:
- **Property-Based Tests** - 100+ iterations per property using jqwik
- **Integration Tests** - End-to-end workflow validation
- **Unit Tests** - Component-level testing

```bash
# Run all tests
apache-maven-3.9.12\bin\mvn.cmd test

# Run tests with coverage
apache-maven-3.9.12\bin\mvn.cmd clean test
```

## Building from Source

```bash
# Clean and compile
apache-maven-3.9.12\bin\mvn.cmd clean compile

# Build executable JAR
apache-maven-3.9.12\bin\mvn.cmd clean package

# Build without tests
apache-maven-3.9.12\bin\mvn.cmd clean package -DskipTests
```

## Usage Guide

### Parking a Vehicle
1. Go to **Vehicle Entry** tab
2. Enter license plate (e.g., "ABC123")
3. Select vehicle type (Car, Motorcycle, Handicapped)
4. Choose spot type
5. Click "Park Vehicle"
6. Note the assigned spot and entry time

### Processing Exit
1. Go to **Vehicle Exit** tab
2. Enter license plate
3. Click "Lookup Vehicle"
4. Review parking fee and any fines
5. Enter payment amount
6. Select payment method
7. Click "Process Payment"

### Viewing Reports
1. Go to **Reporting** tab
2. Select report type:
   - Revenue Report
   - Occupancy Report
   - Current Vehicles
   - Unpaid Fines
3. Click "Generate Report"

### Admin Functions
1. Go to **Admin Panel** tab
2. View system statistics
3. Manage fines
4. Monitor revenue

## Configuration

### Parking Lot Setup
Edit `ParkingApplication.createDefaultParkingLot()` to customize:
- Number of floors
- Spots per floor
- Spot type distribution

### Fine Strategies
Modify `FineCalculationContext` to change fine calculation:
- Fixed amount
- Hourly rate
- Progressive (base + escalation)

## Recent Fixes

### Duplicate Parking Prevention (2026-01-06)
**Issue:** Same license plate could park in multiple spots simultaneously, causing incorrect fee calculation.

**Fix:** Added validation to prevent duplicate parking. The system now checks if a vehicle is already parked before allowing entry.

**Details:**
- Case-insensitive validation (ABC123 = abc123)
- Clear error message: "Vehicle {PLATE} is already parked. Please exit first before parking again."
- Vehicle can re-park after exiting normally
- See `TEST_DUPLICATE_PARKING.md` for test instructions

## Troubleshooting

### "No suitable driver found for jdbc:h2"
- Ensure you're running `parking-lot-management.jar` (2.6 MB), not `parking-lot-management-1.0.0.jar`
- Use `run-parking-system.bat` which uses the correct JAR

### Database Errors
- Delete `parking_lot_db.mv.db` and restart
- Check write permissions in project directory

### Application Won't Start
- Verify Java 11+ is installed: `java -version`
- Check console for error messages
- Ensure no other instance is running

## Technical Details

### Technologies Used
- **Java 11** - Core language
- **Java Swing** - Complete GUI framework (100% of UI)
  - JFrame, JPanel, JTabbedPane for window structure
  - JTable, JTextField, JComboBox for data input/display
  - JButton, JLabel, JTextArea for user interaction
  - JOptionPane for dialogs and messages
  - See `JAVA_SWING_USAGE.md` for complete component list
- **H2 Database 2.1.214** - Embedded SQL database
  - File-based persistence (`parking_lot_db.mv.db`)
  - JDBC connection with connection pooling
  - 7 database tables with foreign key relationships
  - See `DATABASE_TOOLS.md` for complete database documentation
- **Maven 3.9.12** - Build and dependency management
- **JUnit 5** - Unit testing
- **jqwik 1.7.4** - Property-based testing

### GUI Architecture
- **8 Swing GUI Classes** in `src/main/java/com/university/parking/view/`
- **MainFrame** - Main application window (JFrame)
- **4 Panel Classes** - Admin, Entry, Exit, Reports (all JPanel)
- **BasePanel** - Abstract base class for common functionality
- **InputValidator** - Form validation
- **EventHandler** - Event handling

### Design Patterns
- **MVC** - Separation of concerns (View = Swing GUI)
- **DAO** - Data access abstraction
- **Strategy** - Fine calculation strategies
- **Observer** - UI event handling

## License

This project is developed for educational purposes as part of the University Object-Oriented Analysis and Design course.

## Authors

Developed by students of the University OOAD course.

## Version

**Version:** 1.0.1  
**Build Date:** January 2026  
**Tests Passing:** 146/146 ✓

## Documentation Files

| File | Description |
|------|-------------|
| `README.md` | Main project documentation (this file) |
| `USER_GUIDE.md` | Complete user manual |
| `QUICK_START.md` | 3-step quick start guide |
| `JAVA_SWING_USAGE.md` | Detailed Java Swing GUI documentation |
| `DATABASE_TOOLS.md` | H2 Database documentation |
| `PROJECT_SUMMARY.md` | Project overview and statistics |
| `CHANGELOG.md` | Version history and changes |
| `REQUIREMENTS_COMPLIANCE.md` | Assignment requirements verification |
