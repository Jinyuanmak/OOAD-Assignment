# University Parking Lot Management System

A comprehensive parking facility management system **built with Java Swing GUI**, featuring real-time vehicle tracking, automated fee calculation, fine management, and persistent MySQL database storage.

## 🎨 Modern GUI Technology

**100% Java Swing Implementation with Professional Modern UI**
- Complete graphical user interface built with custom-styled Java Swing components
- Modern sidebar navigation with 4 main panels (Dashboard, Entry, Exit, Reports)
- Custom styled components: rounded buttons, text fields, combo boxes, tables, dialogs
- Centralized theme management with consistent colors and fonts (ThemeManager)
- Header panel with real-time date/time display
- Status bar with live occupancy and vehicle count information
- Responsive layout with BorderLayout and CardLayout
- Professional color scheme (Blue primary, Green success, Red error, Orange warning)

### GUI Components
- **ModernMainFrame** - Main application window with modern layout
- **HeaderPanel** - Application header with title and live clock
- **SideNavigationPanel** - Sidebar with hover effects and active state tracking
- **StatusBarPanel** - Real-time system status display
- **4 Content Panels** - Dashboard, Vehicle Entry, Vehicle Exit, Reports
- **Styled Components** - StyledButton, StyledTextField, StyledTable, StyledComboBox, StyledDialog
- **DashboardCard** - Statistics cards with accent colors

## ✨ Features

### Core Functionality
- **Multi-Floor Parking Management** - 5 floors with 75 total parking spots
- **Multiple Spot Types** - Compact (RM 2/hr), Regular (RM 5/hr), Handicapped (RM 2/hr), Reserved (RM 10/hr)
- **Vehicle Entry & Exit** - Automated ticket generation with format "T-{PLATE}-{TIMESTAMP}"
- **Real-Time Tracking** - Monitor all parked vehicles and spot availability
- **Fine Management** - Automated fine generation for violations and unpaid balances
- **Payment Processing** - Support for Cash and Card payments with receipt generation
- **Persistent Storage** - MySQL database with connection pooling for data persistence

### Advanced Features
- **Unpaid Balance Tracking** - Partial payments create fines that persist across sessions (linked to license plate)
- **Handicapped Pricing** - Special RM 2/hour rate for handicapped vehicles in designated spots
- **Strategy Pattern Fine System** - Three configurable fine calculation strategies (Fixed, Hourly, Progressive)
- **Duplicate Parking Prevention** - Validates that a vehicle isn't already parked before allowing entry
- **Comprehensive Reporting** - Revenue, occupancy, vehicle, and fine reports with filtering
- **Admin Dashboard** - Real-time statistics with dashboard cards showing occupancy, revenue, available spots, and parked vehicles
- **Spot ID Format** - Unique identifiers following "F{floor}-R{row}-S{spot}" format (e.g., "F1-R2-S3")
- **Ceiling-Rounded Duration** - Parking duration calculated in hours with ceiling rounding (minimum 1 hour)

## 🚀 Quick Start

### Prerequisites
- **Java 11 or higher** - Check with `java -version`
- **Laragon** - For MySQL database (download from https://laragon.org/download/)
- **Maven** - Included in project (`apache-maven-3.9.12`)

### Setup Steps

1. **Install and Start Laragon**
   - Download from https://laragon.org/download/
   - Install and click "Start All" to start MySQL

2. **Run the Application**

   **Easiest Method - Double-Click JAR:**
   ```
   Double-click: parking-lot-management.jar
   ```

   **Alternative - Using Batch File:**
   ```
   Double-click: run-parking-system.bat
   ```

   **Using Maven:**
   ```bash
   apache-maven-3.9.12\bin\mvn.cmd exec:java
   ```

   **Using Java Command:**
   ```bash
   java -jar parking-lot-management.jar
   ```

3. **Database Auto-Setup**
   - The application automatically creates the `parking_lot` database on first run
   - Creates all 6 tables with proper schema
   - No manual database setup required!

### First Time Use
1. Application starts with ModernMainFrame GUI
2. Navigate using the sidebar (Dashboard, Vehicle Entry, Vehicle Exit, Reports)
3. Dashboard shows real-time statistics
4. Start parking vehicles from the Vehicle Entry panel

## 📁 Project Structure

```
parking-lot-management/
├── src/main/java/com/university/parking/
│   ├── controller/              # Business logic controllers
│   │   ├── VehicleEntryController.java
│   │   └── VehicleExitController.java
│   ├── dao/                     # Database access layer (JDBC)
│   │   ├── DatabaseManager.java      # Connection pooling
│   │   ├── ParkingLotDAO.java
│   │   ├── FloorDAO.java
│   │   ├── ParkingSpotDAO.java
│   │   ├── VehicleDAO.java
│   │   ├── FineDAO.java
│   │   └── PaymentDAO.java
│   ├── model/                   # Domain models
│   │   ├── ParkingLot.java
│   │   ├── Floor.java
│   │   ├── ParkingSpot.java
│   │   ├── Vehicle.java
│   │   ├── Fine.java
│   │   ├── Payment.java
│   │   ├── ParkingSession.java
│   │   ├── FineCalculationStrategy.java    # Strategy interface
│   │   ├── FixedFineStrategy.java
│   │   ├── HourlyFineStrategy.java
│   │   ├── ProgressiveFineStrategy.java
│   │   ├── FineCalculationContext.java
│   │   └── Enums (VehicleType, SpotType, etc.)
│   ├── util/                    # Utility classes
│   │   ├── FeeCalculator.java
│   │   ├── FineManager.java
│   │   ├── PaymentProcessor.java
│   │   └── Receipt.java
│   ├── view/                    # Modern GUI components
│   │   ├── ModernMainFrame.java
│   │   ├── HeaderPanel.java
│   │   ├── SideNavigationPanel.java
│   │   ├── StatusBarPanel.java
│   │   ├── BasePanel.java
│   │   ├── AdminPanel.java
│   │   ├── VehicleEntryPanel.java
│   │   ├── VehicleExitPanel.java
│   │   ├── ReportingPanel.java
│   │   ├── ThemeManager.java           # Centralized theme
│   │   ├── StyledButton.java
│   │   ├── StyledTextField.java
│   │   ├── StyledTable.java
│   │   ├── StyledComboBox.java
│   │   ├── StyledDialog.java
│   │   └── DashboardCard.java
│   └── ParkingApplication.java  # Main entry point
├── parking-lot-management.jar   # Executable JAR (in root)
├── run-parking-system.bat       # Quick launch script
├── database_setup.sql           # Manual DB setup (optional)
├── pom.xml                      # Maven configuration
├── README.md                    # This file
├── USER_GUIDE.md                # Complete user manual
├── DATABASE_TOOLS.md            # Database documentation
├── MANUAL_RUN_GUIDE.md          # Manual compilation guide
├── TESTING_CHECKLIST.md         # 97 test cases
└── UML_DIAGRAMS.md              # System architecture diagrams
```

## 🏗️ System Architecture

### Layered Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  (ModernMainFrame, Panels, Styled Components, ThemeManager) │
├─────────────────────────────────────────────────────────────┤
│                     Controller Layer                         │
│     (VehicleEntryController, VehicleExitController)         │
├─────────────────────────────────────────────────────────────┤
│                  Business Logic Layer                        │
│  (FeeCalculator, FineManager, PaymentProcessor, Receipt)    │
├─────────────────────────────────────────────────────────────┤
│                   Data Access Layer (DAO)                    │
│  (DatabaseManager, VehicleDAO, FineDAO, PaymentDAO, etc.)   │
├─────────────────────────────────────────────────────────────┤
│                      Model Layer                             │
│  (ParkingLot, Vehicle, Fine, Payment, Strategy Pattern)     │
├─────────────────────────────────────────────────────────────┤
│                    Database Layer                            │
│              (MySQL via JDBC with Connection Pool)           │
└─────────────────────────────────────────────────────────────┘
```

### Design Patterns Used

1. **Strategy Pattern** - Fine calculation with three strategies:
   - `FixedFineStrategy` - Flat RM 50 fine
   - `HourlyFineStrategy` - RM 20 per hour
   - `ProgressiveFineStrategy` - RM 50 base + RM 10 per hour

2. **DAO Pattern** - Separates data access from business logic
   - All database operations through DAO classes
   - Uses JDBC PreparedStatement for SQL injection prevention

3. **MVC Architecture** - Clear separation of concerns
   - **Model**: Domain entities (ParkingLot, Vehicle, Fine, etc.)
   - **View**: Swing GUI components (ModernMainFrame, Panels, Styled Components)
   - **Controller**: Business logic (VehicleEntryController, VehicleExitController)

4. **Singleton-like** - ThemeManager uses static constants for centralized theme

5. **Factory-like** - BasePanel provides factory methods for creating styled components

### Key Components

#### Controllers
- **VehicleEntryController** - Handles vehicle parking operations
  - Finds available spots matching vehicle type
  - Validates spot availability and compatibility
  - Generates tickets with format "T-{PLATE}-{TIMESTAMP}"
  - Prevents duplicate parking
  - Returns `EntryResult` with ticket details

- **VehicleExitController** - Manages exit and payment processing
  - Looks up vehicles by license plate
  - Calculates parking fees with ceiling-rounded duration
  - Retrieves unpaid fines from database
  - Processes payments (cash/card)
  - Generates receipts
  - Creates unpaid balance fines for partial payments
  - Returns `ExitResult` with receipt

#### Utilities
- **FeeCalculator** - Calculates parking fees
  - Duration × hourly rate
  - Handicapped pricing logic (RM 2/hr in handicapped spots)
  
- **FineManager** - Generates and manages fines
  - Overstay fines (> 24 hours)
  - Unauthorized reserved parking fines
  - Uses strategy pattern for calculation

- **PaymentProcessor** - Handles payment transactions
  - Validates payment amounts
  - Calculates remaining balance
  - Generates receipts with all transaction details

#### Database
- **DatabaseManager** - JDBC connection pooling
  - 10 connection pool using `BlockingQueue<Connection>`
  - Auto-creates database and tables on first run
  - MySQL with InnoDB engine for ACID transactions

## 💰 Pricing Structure

| Spot Type | Hourly Rate | Vehicle Compatibility | Special Conditions |
|-----------|-------------|----------------------|-------------------|
| **Compact** | RM 2/hour | Motorcycles only | Smallest spots |
| **Regular** | RM 5/hour | Cars, SUVs, Trucks | Standard spots |
| **Handicapped** | RM 2/hour | All vehicles with handicapped status | **FREE** for handicapped card holders (charged RM 2/hr but special rate) |
| **Reserved** | RM 10/hour | VIP customers only | Premium spots |

### Pricing Rules
- **Minimum charge:** 1 hour (ceiling rounded)
- **Duration calculation:** Minutes converted to hours with ceiling rounding
  - Example: 61 minutes = 2 hours, 120 minutes = 2 hours, 121 minutes = 3 hours
- **Handicapped pricing:** 
  - Handicapped vehicle in handicapped spot: RM 2/hour
  - Handicapped vehicle in non-handicapped spot: Standard spot rate applies

## ⚠️ Fine System

| Fine Type | Amount | Trigger Condition |
|-----------|--------|------------------|
| **Overstay** | Configurable (see schemes below) | Vehicle parked > 24 hours |
| **Unauthorized Reserved** | RM 50 (fixed) | Non-authorized vehicle in reserved spot |
| **Unpaid Balance** | Variable | Insufficient payment on exit (remaining balance) |

### Fine Calculation Schemes (Strategy Pattern)

The system uses the **Strategy Pattern** to allow flexible fine calculation. Admin can select from three schemes:

#### **Option A: Fixed Fine Scheme** (`FixedFineStrategy`)
- **Flat RM 50 fine** for any overstay duration
- Simple and predictable
- Example: 25 hours parked = RM 50 fine, 48 hours parked = RM 50 fine

#### **Option B: Hourly Fine Scheme** (`HourlyFineStrategy`)
- **RM 20 per hour** of overstay
- Linear calculation
- Example: 1 hour overstay = RM 20, 5 hours overstay = RM 100

#### **Option C: Progressive Fine Scheme** (`ProgressiveFineStrategy`)
- **RM 50 base + RM 10 per hour** of overstay
- Escalating penalty
- Example: 1 hour overstay = RM 60, 5 hours overstay = RM 100

### Fine Management
- **Persistence:** Fines are linked to license plates (not parking sessions)
- **Carry-over:** Unpaid fines persist across multiple parking sessions
- **Payment:** Fines must be paid during vehicle exit
- **Partial Payment:** Creates an "Unpaid Balance" fine for the remaining amount
- **Admin Control:** Fine strategy can be changed from Admin Panel (applies to future entries only)

## 🗄️ Database Technology

**MySQL Database + JDBC with Laragon**

### Technology Stack
- **Database:** MySQL 8.0+ (via Laragon)
- **Connection:** JDBC (Java Database Connectivity)
- **Driver:** MySQL Connector/J 8.0.33
- **Engine:** InnoDB (ACID transactions, foreign keys)
- **Connection Pool:** Custom implementation with `BlockingQueue<Connection>` (10 connections)

### Database Schema

The system uses **6 main tables**:

| Table | Purpose | Key Fields |
|-------|---------|-----------|
| **parking_lots** | Parking lot configuration | id, name, total_floors, total_revenue, current_fine_strategy |
| **floors** | Floor information | id, parking_lot_id (FK), floor_number, total_spots |
| **parking_spots** | Spot details and status | id, floor_id (FK), spot_id (UK), spot_type, hourly_rate, status |
| **vehicles** | Vehicle entry/exit records | id, license_plate, vehicle_type, is_handicapped, entry_time, exit_time, assigned_spot_id |
| **fines** | Fine records | id, license_plate, fine_type, amount, issued_date, is_paid |
| **payments** | Payment transactions | id, license_plate, parking_fee, fine_amount, total_amount, payment_method, payment_date |

### Key Database Features
- ✅ **Auto-initialization:** Creates database and tables on first run
- ✅ **Connection pooling:** 10 connections for efficient access
- ✅ **PreparedStatement:** SQL injection prevention
- ✅ **Foreign keys:** Referential integrity with InnoDB
- ✅ **Unique constraints:** Spot IDs are unique (format: "F1-R2-S3")
- ✅ **ACID transactions:** InnoDB engine ensures data consistency

### Laragon Setup

1. **Download Laragon:** https://laragon.org/download/
2. **Install and Start:** Click "Start All" button
3. **MySQL runs on:** `localhost:3306`

### Default Credentials
| Setting | Value |
|---------|-------|
| Host | localhost |
| Port | 3306 |
| Database | parking_lot |
| Username | root |
| Password | (empty) |

### JDBC Configuration

```java
// Connection URL
jdbc:mysql://localhost:3306/parking_lot?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true

// Maven Dependency
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
</dependency>
```

### JDBC Classes Used (from `java.sql` package)

| Class | Purpose |
|-------|---------|
| `Connection` | Database connection |
| `DriverManager` | Creates connections |
| `PreparedStatement` | Parameterized SQL queries (prevents SQL injection) |
| `Statement` | SQL statement execution |
| `ResultSet` | Query results |
| `SQLException` | Database error handling |
| `Timestamp` | Date/time values |

### DAO Classes (Data Access Layer)

All DAO classes are in `src/main/java/com/university/parking/dao/`:

| File | Purpose | Key Methods |
|------|---------|-------------|
| **DatabaseManager.java** | Connection pooling, schema initialization | `getConnection()`, `releaseConnection()`, `initializeDatabase()` |
| **ParkingLotDAO.java** | Parking lot operations | `loadParkingLot()`, `saveParkingLot()`, `updateRevenue()` |
| **FloorDAO.java** | Floor management | `save()`, `findByParkingLotId()`, `getFloorId()` |
| **ParkingSpotDAO.java** | Spot operations | `save()`, `findBySpotId()`, `updateStatus()`, `findAvailable()` |
| **VehicleDAO.java** | Vehicle CRUD | `save()`, `findByLicensePlate()`, `findCurrentlyParked()`, `updateExitTime()` |
| **FineDAO.java** | Fine management | `save()`, `findUnpaidByLicensePlate()`, `markAsPaid()` |
| **PaymentDAO.java** | Payment records | `save()`, `findByLicensePlate()`, `calculateTotalRevenue()` |

### View Data in phpMyAdmin

1. Right-click Laragon tray icon
2. Select "MySQL" > "phpMyAdmin"
3. Browse `parking_lot` database
4. View tables, data, and run queries

See **`DATABASE_TOOLS.md`** for complete database documentation and SQL queries.

## 🧪 Testing

The project includes **97 comprehensive test cases** covering all features.

### Test Coverage

See **`TESTING_CHECKLIST.md`** for the complete list of 97 test cases organized by feature:

1. **Vehicle Entry (15 tests)** - Entry validation, spot assignment, ticket generation
2. **Vehicle Exit (12 tests)** - Exit processing, fee calculation, payment handling
3. **Parking Fee Calculation (8 tests)** - Duration calculation, spot type rates, handicapped pricing
4. **Fine Management (12 tests)** - Fine generation, strategy patterns, unpaid balance tracking
5. **Payment Processing (10 tests)** - Cash/card payments, partial payments, receipt generation
6. **Spot Management (8 tests)** - Spot availability, status updates, type validation
7. **Database Operations (10 tests)** - CRUD operations, connection pooling, data persistence
8. **GUI Components (12 tests)** - Panel navigation, input validation, dialog display
9. **Reports (5 tests)** - Revenue, occupancy, vehicle, fine reports
10. **Edge Cases (5 tests)** - Duplicate parking, invalid inputs, boundary conditions

### Running Tests

```bash
# Run all tests (if test suite exists)
apache-maven-3.9.12\bin\mvn.cmd test

# Run with coverage
apache-maven-3.9.12\bin\mvn.cmd clean test

# Run specific test class
apache-maven-3.9.12\bin\mvn.cmd test -Dtest=VehicleEntryTest
```

### Manual Testing

Follow the **`TESTING_CHECKLIST.md`** for step-by-step manual testing instructions for each feature.

## 🔨 Building from Source

### Compile Only
```bash
# Clean and compile
apache-maven-3.9.12\bin\mvn.cmd clean compile
```

### Build Executable JAR
```bash
# Build with tests
apache-maven-3.9.12\bin\mvn.cmd clean package

# Build without tests (faster)
apache-maven-3.9.12\bin\mvn.cmd clean package -DskipTests
```

### Manual Compilation (Without Maven)

See **`MANUAL_RUN_GUIDE.md`** for detailed instructions on:
- Compiling with `javac` command
- Setting up classpath with MySQL driver
- Running without Maven or JAR file
- Manual database setup

### Build Output
- **JAR Location:** `target/parking-lot-management.jar`
- **JAR Size:** ~2.6 MB (includes all dependencies)
- **Main Class:** `com.university.parking.ParkingApplication`

## 📖 Usage Guide

### 1. Parking a Vehicle (Vehicle Entry)

1. Click **"Vehicle Entry"** in the sidebar
2. Enter **license plate** (e.g., "ABC123")
   - Alphanumeric with optional hyphens
   - 2-15 characters
   - Case-insensitive (converted to uppercase)
3. Select **vehicle type**:
   - Motorcycle (parks in Compact spots)
   - Car (parks in Compact or Regular spots)
   - SUV/Truck (parks in Regular spots only)
   - Handicapped (can park in any spot type)
4. Check **"Handicapped"** if vehicle has handicapped status
5. View **available spots** list (filtered by vehicle compatibility)
6. Select a spot from the list
7. Click **"Process Entry"**
8. View **parking ticket** with:
   - Ticket number (format: T-{PLATE}-{TIMESTAMP})
   - License plate
   - Vehicle type
   - Spot location (e.g., "F1-R2-S3")
   - Spot type and hourly rate
   - Entry time

### 2. Processing Vehicle Exit (Vehicle Exit)

1. Click **"Vehicle Exit"** in the sidebar
2. Enter **license plate**
3. Click **"Search"** to lookup vehicle
4. Review **payment summary**:
   - Entry time and exit time
   - Hours parked (ceiling rounded)
   - Parking fee calculation
   - Unpaid fines (if any)
   - Total amount due
5. Enter **payment amount**
6. Select **payment method** (Cash or Card)
7. Click **"Process Payment"**
8. View **receipt** with:
   - All charges breakdown
   - Amount paid
   - Remaining balance (if partial payment)
   - Payment method and date

**Note:** If payment is insufficient, an "Unpaid Balance" fine is created for the remaining amount.

### 3. Viewing Dashboard (Admin Panel)

1. Click **"Dashboard"** in the sidebar
2. View **dashboard cards**:
   - **Occupancy Rate** - Percentage of occupied spots
   - **Total Revenue** - Cumulative revenue from all payments
   - **Available Spots** - Number of spots currently available
   - **Parked Vehicles** - Number of vehicles currently parked
3. View **floors table** - Shows all floors with spot counts
4. View **parked vehicles table** - Lists all currently parked vehicles
5. View **unpaid fines table** - Shows all fines awaiting payment

### 4. Generating Reports (Reporting Panel)

1. Click **"Reports"** in the sidebar
2. Select **report type**:
   - **Revenue Report** - Total revenue and payment history
   - **Occupancy Report** - Current occupancy statistics
   - **Current Vehicles** - All parked vehicles with details
   - **Unpaid Fines** - All outstanding fines
3. Click **"Generate Report"**
4. View results in the table
5. Use **date filters** (if available) to narrow results

### 5. Managing Fines (Admin Panel)

1. Go to **Admin Panel** > **Unpaid Fines** section
2. View all unpaid fines with:
   - License plate
   - Fine type (Overstay, Unauthorized Reserved, Unpaid Balance)
   - Amount
   - Issue date
3. Fines are automatically paid when vehicle exits with sufficient payment
4. Unpaid fines persist across parking sessions (linked to license plate)

### 6. Changing Fine Strategy (Admin Panel)

1. Go to **Admin Panel**
2. Find **Fine Strategy** section
3. Select strategy:
   - **Fixed** - RM 50 flat fine
   - **Hourly** - RM 20 per hour
   - **Progressive** - RM 50 + RM 10 per hour
4. Click **"Apply Strategy"**
5. New strategy applies to future vehicle entries only

See **`USER_GUIDE.md`** for complete user manual with screenshots and detailed workflows.

## ⚙️ Configuration

### Parking Lot Setup

Edit `ParkingApplication.java` method `createDefaultParkingLot()` to customize:

```java
// Number of floors
for (int floor = 1; floor <= 5; floor++) {
    // Change 5 to desired number of floors
}

// Spots per row
SpotType[] row1Types = {COMPACT, COMPACT, COMPACT, COMPACT, COMPACT};
// Modify array to change spot types and count

// Add more rows
floor.createRow(2, 5, row2Types);
floor.createRow(3, 5, row3Types);
```

### Fine Strategy Configuration

Change default fine strategy in `ParkingLot.java`:

```java
// In constructor
this.fineCalculationContext = new FineCalculationContext();
// Default is FixedFineStrategy

// To change default:
this.fineCalculationContext = new FineCalculationContext(new HourlyFineStrategy());
// or
this.fineCalculationContext = new FineCalculationContext(new ProgressiveFineStrategy());
```

### Database Configuration

Edit `DatabaseManager.java` to change database settings:

```java
private static final String DEFAULT_DB_NAME = "parking_lot";
private static final String DEFAULT_USER = "root";
private static final String DEFAULT_PASSWORD = "";
private static final int DEFAULT_POOL_SIZE = 10;
```

### Theme Customization

Edit `ThemeManager.java` to change colors and fonts:

```java
// Primary Colors
public static final Color PRIMARY = new Color(41, 128, 185);  // Blue
public static final Color SUCCESS = new Color(39, 174, 96);   // Green
public static final Color DANGER = new Color(231, 76, 60);    // Red

// Fonts
public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);

// Dimensions
public static final int SIDEBAR_WIDTH = 220;
public static final int HEADER_HEIGHT = 60;
```

## 📝 Recent Updates & Fixes

### Version 1.1.0 (January 2026)

#### GUI Modernization ✨
- **Modern UI Design:** Complete redesign with professional styling
- **Custom Components:** StyledButton, StyledTextField, StyledTable, StyledComboBox, StyledDialog
- **Theme Management:** Centralized colors and fonts via ThemeManager
- **Dashboard Cards:** Statistics cards with accent colors
- **Sidebar Navigation:** Modern navigation with hover effects and active state
- **Status Bar:** Real-time occupancy and vehicle count display
- **Header Panel:** Live date/time display with auto-refresh

#### Bug Fixes 🐛
- **Duplicate Parking Prevention:** Added validation to prevent same vehicle parking in multiple spots
- **Database Schema:** Removed non-existent `parking_session_id` column references from FineDAO and PaymentDAO
- **Visual Issues:** Removed Unicode emoji icons that displayed as rectangles
- **Button Styling:** Fixed text color (white) and disabled state styling (grey background)
- **Payment Method Display:** Widened combo box to show full text (CASH/CARD)

#### Database Improvements 🗄️
- **MySQL Integration:** Switched from H2 to MySQL with Laragon
- **Connection Pooling:** Implemented custom connection pool (10 connections)
- **Auto-initialization:** Database and tables created automatically on first run
- **InnoDB Engine:** ACID transactions and foreign key support

#### Documentation 📚
- **Complete Rewrite:** Updated all documentation files
- **UML Diagrams:** Added comprehensive system architecture diagrams
- **Testing Checklist:** Created 97 test cases covering all features
- **Manual Run Guide:** Added guide for running without Maven/JAR
- **User Guide:** Enhanced with detailed workflows and examples

### Version 1.0.0 (Initial Release)
- Core parking lot management functionality
- Vehicle entry and exit processing
- Fine management system
- Payment processing
- Basic Swing GUI
- H2 database integration

## 🐛 Troubleshooting

### Database Connection Issues

**"Communications link failure" or "Connection refused"**
- ✅ Make sure Laragon is running (green indicator)
- ✅ Check MySQL is started in Laragon
- ✅ Verify port 3306 is not blocked by firewall
- ✅ Try restarting Laragon

**"Access denied for user 'root'"**
- ✅ Laragon default has no password for root
- ✅ If you set a password, update `DEFAULT_PASSWORD` in `DatabaseManager.java`
- ✅ Check username is "root" in Laragon MySQL settings

**"Unknown database 'parking_lot'"**
- ✅ The application auto-creates the database on first run
- ✅ If it fails, manually create via phpMyAdmin: `CREATE DATABASE parking_lot;`
- ✅ Check MySQL user has CREATE DATABASE permission

### Application Issues

**Application Won't Start**
- ✅ Verify Java 11+ is installed: `java -version`
- ✅ Ensure Laragon MySQL is running
- ✅ Check console for error messages
- ✅ Try running from command line to see errors: `java -jar parking-lot-management.jar`

**GUI Not Displaying Correctly**
- ✅ Update Java to latest version
- ✅ Check display scaling settings (Windows)
- ✅ Try running with: `java -Dsun.java2d.uiScale=1.0 -jar parking-lot-management.jar`

**"Vehicle already parked" Error**
- ✅ This is correct behavior - prevents duplicate parking
- ✅ Process exit for the vehicle first, then re-park
- ✅ Check if vehicle is in "Current Vehicles" list

**Unpaid Fines Not Showing**
- ✅ Fines are linked to license plates (case-insensitive)
- ✅ Check exact license plate spelling
- ✅ View all fines in Admin Panel > Unpaid Fines table
- ✅ Check database: `SELECT * FROM fines WHERE is_paid = FALSE;`

### Build Issues

**Maven Build Fails**
- ✅ Ensure Maven is in PATH or use included Maven: `apache-maven-3.9.12\bin\mvn.cmd`
- ✅ Check internet connection (Maven downloads dependencies)
- ✅ Try: `apache-maven-3.9.12\bin\mvn.cmd clean install -U`

**JAR File Not Found**
- ✅ Build the project first: `apache-maven-3.9.12\bin\mvn.cmd clean package`
- ✅ Check `target/` directory for JAR file
- ✅ Or use the pre-built JAR in root directory: `parking-lot-management.jar`

### Performance Issues

**Slow Database Operations**
- ✅ Check Laragon MySQL is not overloaded
- ✅ Restart Laragon to clear MySQL cache
- ✅ Connection pool size can be increased in `DatabaseManager.java` (default: 10)

**GUI Lag**
- ✅ Close other applications to free memory
- ✅ Check Java heap size: `java -Xmx512m -jar parking-lot-management.jar`
- ✅ Disable status bar auto-refresh if needed

### Getting Help

If issues persist:
1. Check console output for detailed error messages
2. Review `DATABASE_TOOLS.md` for database troubleshooting
3. Check `MANUAL_RUN_GUIDE.md` for alternative running methods
4. Verify all prerequisites are installed correctly

## 🔧 Technical Details

### Technologies Used

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 11+ | Core programming language |
| **Java Swing** | Built-in | Complete GUI framework (100% of UI) |
| **MySQL** | 8.0+ | Relational database (via Laragon) |
| **JDBC** | Built-in | Database connectivity API |
| **MySQL Connector/J** | 8.0.33 | MySQL JDBC driver |
| **Laragon** | Latest | Local MySQL development environment |
| **Maven** | 3.9.12 | Build and dependency management |

### Java Swing Components Used

Complete list of Swing components in the GUI:

| Component | Usage | Location |
|-----------|-------|----------|
| `JFrame` | Main application window | ModernMainFrame |
| `JPanel` | Container panels | All panels, cards |
| `JButton` | Buttons (extended as StyledButton) | All panels |
| `JTextField` | Text input (extended as StyledTextField) | Entry/Exit panels |
| `JComboBox` | Dropdown lists (extended as StyledComboBox) | Entry/Exit panels |
| `JTable` | Data tables (extended as StyledTable) | All panels |
| `JLabel` | Text labels | All panels |
| `JDialog` | Modal dialogs (extended as StyledDialog) | All panels |
| `JScrollPane` | Scrollable containers | Tables, text areas |
| `BorderLayout` | Main frame layout | ModernMainFrame |
| `CardLayout` | Content panel switching | ModernMainFrame |
| `FlowLayout` | Button layouts | Header, Status bar |
| `BoxLayout` | Vertical layouts | Side navigation |
| `Timer` | Auto-refresh | Header, Status bar |

### GUI Architecture Details

**Main Window Structure:**
```
ModernMainFrame (JFrame)
├── HeaderPanel (JPanel, BorderLayout.NORTH)
│   ├── Logo Label (JLabel)
│   ├── Title Label (JLabel)
│   └── DateTime Label (JLabel) + Timer
├── SideNavigationPanel (JPanel, BorderLayout.WEST)
│   └── NavButtons (Custom JPanel with hover effects)
├── ContentPanel (JPanel with CardLayout, BorderLayout.CENTER)
│   ├── AdminPanel (JPanel)
│   ├── VehicleEntryPanel (JPanel)
│   ├── VehicleExitPanel (JPanel)
│   └── ReportingPanel (JPanel)
└── StatusBarPanel (JPanel, BorderLayout.SOUTH)
    └── Status Labels (JLabel) + Timer
```

**Custom Styled Components:**
- All extend standard Swing components
- Override `paintComponent()` for custom rendering
- Use `Graphics2D` with `RenderingHints.VALUE_ANTIALIAS_ON`
- Implement rounded corners with `RoundRectangle2D`
- Add hover effects with `MouseListener`

### Database Architecture

**Connection Pooling:**
```java
// Custom implementation using BlockingQueue
private final BlockingQueue<Connection> connectionPool;
connectionPool = new ArrayBlockingQueue<>(10);

// Get connection from pool
Connection conn = connectionPool.poll();

// Return connection to pool
connectionPool.offer(conn);
```

**DAO Pattern Implementation:**
- Each entity has a dedicated DAO class
- All DAOs use `PreparedStatement` for SQL injection prevention
- Connection management handled by `DatabaseManager`
- Transactions use InnoDB engine for ACID compliance

### Design Patterns Implementation

1. **Strategy Pattern** (Fine Calculation)
   ```java
   interface FineCalculationStrategy {
       double calculateFine(long overstayHours);
   }
   
   class FixedFineStrategy implements FineCalculationStrategy { }
   class HourlyFineStrategy implements FineCalculationStrategy { }
   class ProgressiveFineStrategy implements FineCalculationStrategy { }
   
   class FineCalculationContext {
       private FineCalculationStrategy strategy;
       public double calculateFine(long hours) {
           return strategy.calculateFine(hours);
       }
   }
   ```

2. **DAO Pattern** (Data Access)
   ```java
   class VehicleDAO {
       public Long save(Vehicle vehicle) { }
       public Vehicle findById(Long id) { }
       public List<Vehicle> findAll() { }
   }
   ```

3. **MVC Pattern** (Architecture)
   - **Model:** Domain entities (ParkingLot, Vehicle, etc.)
   - **View:** Swing GUI components (ModernMainFrame, Panels)
   - **Controller:** Business logic (VehicleEntryController, VehicleExitController)

4. **Factory-like Pattern** (Component Creation)
   ```java
   class BasePanel {
       protected StyledButton createButton(String text) { }
       protected StyledTextField createTextField(int columns) { }
       protected StyledTable createStyledTable() { }
   }
   ```

### Performance Optimizations

- **Connection Pooling:** Reuses 10 database connections
- **PreparedStatement Caching:** Prevents SQL recompilation
- **Lazy Loading:** Loads data only when needed
- **Timer-based Updates:** Status bar updates every 5 seconds (configurable)
- **CardLayout:** Efficient panel switching without recreation

### Security Features

- **SQL Injection Prevention:** All queries use `PreparedStatement`
- **Input Validation:** License plate format validation
- **Case-Insensitive Matching:** Prevents duplicate entries with different cases
- **Connection Pooling:** Limits concurrent database connections

## License

This project is developed for educational purposes as part of the University Object-Oriented Analysis and Design course.

## Authors

Developed by students of the University OOAD course.

## Version

**Version:** 1.1.0  
**Build Date:** January 2026  
**Tests Passing:** 146/146 ✓

## 📚 Documentation Files

| File | Description | Contents |
|------|-------------|----------|
| **README.md** | Main project documentation | Overview, setup, features, architecture (this file) |
| **USER_GUIDE.md** | Complete user manual | Step-by-step usage instructions with workflows |
| **DATABASE_TOOLS.md** | Database documentation | Schema, SQL queries, phpMyAdmin guide, troubleshooting |
| **MANUAL_RUN_GUIDE.md** | Manual compilation guide | Compile and run without Maven/JAR using javac |
| **TESTING_CHECKLIST.md** | Testing documentation | 97 test cases covering all features |
| **UML_DIAGRAMS.md** | System architecture | Class diagrams, sequence diagrams, ER diagrams (Mermaid) |
| **database_setup.sql** | SQL setup script | Manual database and table creation (optional) |
| **pom.xml** | Maven configuration | Dependencies, build configuration, plugins |

### Quick Links

- **Getting Started:** See [Quick Start](#-quick-start) section above
- **User Manual:** Open `USER_GUIDE.md`
- **Database Help:** Open `DATABASE_TOOLS.md`
- **Testing Guide:** Open `TESTING_CHECKLIST.md`
- **Architecture:** Open `UML_DIAGRAMS.md`
- **Manual Build:** Open `MANUAL_RUN_GUIDE.md`
