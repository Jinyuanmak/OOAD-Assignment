# University Parking Lot Management System

A comprehensive parking facility management system built with Java Swing and MySQL.

## Features

### Vehicle Management
- Vehicle entry with automatic spot assignment
- Vehicle exit with fee calculation
- Support for multiple vehicle types (Motorcycle, Car, SUV/Truck, Handicapped)
- Handicapped card holder discount: RM 2/hour in any spot, FREE in handicapped spots
- Real-time parking duration tracking
- Card holder status displayed on entry ticket and exit receipt

### Parking Spot Management
- Multi-floor parking structure
- Different spot types: Compact, Regular, Handicapped, Reserved
- Dynamic hourly rates based on spot type
- Automatic spot availability tracking

### Fine Management
- Automatic fine issuance for overstay (>24 hours)
- Automatic fine for unauthorized reserved spot parking (RM 100)
- Automatic fine for expired reservations (RM 100)
- Multiple fine calculation strategies:
  - Fixed: RM 50 flat fine
  - Hourly: RM 20 per hour
  - Progressive: Tiered by days
    - Day 2 (24-48h): RM 50
    - Day 3 (48-72h): +RM 100 (Total: RM 150)
    - Day 4 (72-96h): +RM 150 (Total: RM 300)
    - Day 5+ (96h+): +RM 200 (Total: RM 500)
- Fine payment tracking
- Unpaid balance tracking across sessions
- Detailed fine breakdown by type in exit summary

### Reservation System
- Reserve specific RESERVED parking spots in advance
- Prepaid payment: Pay upfront for entire reservation period (RM 10/hour × 24 hours = RM 240)
- Set reservation time windows (start and end times)
- Multiple entry/exit allowed within reservation period
- Validation during vehicle entry
- Automatic RM 100 fine for unauthorized reserved spot usage
- Expired reservation fine: RM 100 if vehicle stays beyond reservation period
- Overstay fine: Additional fine if vehicle exceeds 24 hours total parking time
- View and manage all reservations
- Cancel reservations when needed (no refund policy)

**How to Use**:
1. Navigate to "Reservations" panel
2. Enter license plate and reserved spot ID
3. Set duration in hours (default: 24 hours)
4. Pay prepaid amount upfront (e.g., 24 hours × RM 10 = RM 240)
5. Click "Create Reservation"
6. Vehicle can enter/exit multiple times within reservation period
7. If correct vehicle enters → No additional charges (prepaid)
8. If wrong vehicle enters → RM 100 fine issued automatically
9. If vehicle stays beyond reservation → RM 100 expired reservation fine + overstay fine

### Payment Processing
- Multiple payment methods: Cash, Credit Card, Debit Card, E-Wallet, Online Banking
- Automatic change calculation
- Receipt generation (Text and PDF formats)

### Reporting & Analytics
- Vehicle Report: Current parked vehicles
- Revenue Report: Total revenue and breakdown by floor
- Occupancy Report: Spot utilization by floor and type
- Fine Report: Outstanding fines
- Export reports in TXT, PDF, and CSV formats

### User Interface
- Classic UI: Traditional Swing interface
- Modern UI: Contemporary design with themes
- Dashboard with real-time statistics
- Easy navigation and intuitive controls

## Technology Stack

- **Language**: Java 11
- **UI Framework**: Java Swing
- **Database**: MySQL 8.0
- **Build Tool**: Maven 3.9.12
- **PDF Generation**: Apache PDFBox 2.0.30
- **Database Driver**: MySQL Connector/J 8.0.33

## Prerequisites

- Java Development Kit (JDK) 11 or higher
- MySQL Server 8.0 or higher
- Maven 3.9.12 (included in project)

## Database Setup

1. Start MySQL server (e.g., via Laragon, XAMPP, or standalone MySQL)

2. The application will automatically create the database on first run, or you can manually set it up:

**Option A: Automatic Setup (Recommended)**
- Just run the application, it will create the `parking_lot` database automatically

**Option B: Manual Setup**
- Open phpMyAdmin or MySQL Workbench
- Run the SQL script: `database/database_setup.sql`
- This creates the `parking_lot` database with all tables and views

3. Update database credentials if needed in `DatabaseManager.java`:
```java
private static final String DEFAULT_DB_NAME = "parking_lot";
private static final String DEFAULT_USER = "root";
private static final String DEFAULT_PASSWORD = "";  // Empty for Laragon/XAMPP
```

**Note**: The system uses UTC+8 timezone (Asia/Singapore) for accurate time tracking.

## Build & Run

### Build the project:
```bash
apache-maven-3.9.12\bin\mvn.cmd clean package -DskipTests
```

### Run the application:
```bash
java -jar parking-lot-management.jar
```

Or use the batch file:
```bash
run-parking-system.bat
```

## Project Structure

```
src/main/java/com/university/parking/
├── model/              # Domain entities and business logic
├── dao/                # Data access layer
├── controller/         # Business logic controllers
├── util/               # Utility classes (calculators, generators)
├── view/               # UI components
└── ParkingApplication.java  # Main entry point
```

## Usage

### Vehicle Entry
1. Navigate to "Vehicle Entry" panel
2. Enter license plate number
3. Select vehicle type
4. Check "Handicapped Card Holder" if applicable (enables discount: FREE in handicapped spots, RM 2/hr in other spots)
5. Click "Park Vehicle"
6. System assigns available spot automatically

### Vehicle Exit
1. Navigate to "Vehicle Exit" panel
2. Enter license plate number
3. Click "Calculate Charges"
4. Review parking fee and any fines
5. Enter payment amount
6. Select payment method
7. Click "Process Exit"
8. Receipt is generated automatically

### Admin Functions
1. Navigate to "Admin" panel (Dashboard)
2. Change fine calculation strategy
3. View system statistics
4. Manage parking configuration

### Reservations
1. Navigate to "Reservations" panel
2. Enter license plate number
3. Select reserved spot ID from dropdown (shows only available RESERVED spots)
4. Set duration in hours (default: 24 hours)
5. Click "Create Reservation"
6. View all reservations in the table
7. Select and cancel reservations if needed
8. Click "Refresh" to update available spots after vehicles enter/exit

### Reports
1. Navigate to "Reporting" panel
2. Select report type
3. View report data
4. Export to TXT, PDF, or CSV format

## Configuration

### Parking Lot Setup
The system initializes with:
- **5 floors** (Floor 1 to Floor 5)
- **15 spots per floor** (75 total spots)
- **3 rows per floor**:
  - Row 1: 5 Compact spots
  - Row 2: 5 Regular spots
  - Row 3: 2 Handicapped + 2 Reserved + 1 Regular spot

Spot ID format: `F{floor}-R{row}-S{spot}` (e.g., F1-R2-S3)

Modify in `ParkingApplication.java` to customize.

### Hourly Rates
- Compact: RM 2.00/hour
- Regular: RM 5.00/hour
- Handicapped: FREE (RM 0.00/hour) for handicapped card holders in handicapped spots, RM 2.00/hour for card holders in other spots, RM 5.00/hour for non-card holders
- Reserved: RM 10.00/hour

Modify in `SpotType.java` enum.

### Fine Strategies

**Default Strategy**: Fixed (RM 50 flat fine for overstay)

**Available Strategies**:
1. **Fixed**: RM 50 flat fine regardless of overstay duration
2. **Hourly**: RM 20 per hour of overstay
3. **Progressive**: Tiered by days (based on total parking hours)
   - Day 2 (24-48 hours): RM 50
   - Day 3 (48-72 hours): RM 150 (RM 50 + RM 100)
   - Day 4 (72-96 hours): RM 300 (RM 50 + RM 100 + RM 150)
   - Day 5+ (96+ hours): RM 500 (RM 50 + RM 100 + RM 150 + RM 200)

Change strategy via Admin panel. Strategy changes only affect new entries after the change time.

**Fine Types**:
- **OVERSTAY**: Automatically issued when vehicle parks > 24 hours
- **UNPAID_BALANCE**: Created when customer makes partial payment (pays less than total due)
- **UNAUTHORIZED_RESERVED**: Issued when vehicle parks in reserved spot without valid reservation (RM 100 fine)

**Unpaid Balance Handling**:
- If customer pays less than total due, the remaining balance becomes an UNPAID_BALANCE fine
- Unpaid fines persist across sessions and accumulate
- Customer must pay all unpaid fines before exiting
- Example: Total due RM 100, customer pays RM 60 → RM 40 becomes UNPAID_BALANCE fine

## Receipts & Reports

### Receipt Storage
- Text receipts: `receipts/` folder
- PDF receipts: `receipts/` folder
- Format: `receipt_[LICENSE]_[TIMESTAMP].txt/pdf`

### Report Export
- Exported reports saved to user-selected location
- Formats: TXT, PDF, CSV
- PDF reports include formatted tables and headers

## Database Schema

### Main Tables
- `parking_lots`: Parking lot configuration
- `floors`: Floor information
- `parking_spots`: Spot details and status
- `vehicles`: Current parked vehicles
- `fines`: Fine records
- `payments`: Payment transactions
- `parking_sessions`: Historical parking records
- `reservations`: Parking spot reservations

### Views
- `vehicles_with_duration`: Real-time duration calculation

## Reservation System

### How It Works
1. Vehicles can reserve RESERVED spots in advance
2. Reservations have start and end times
3. During vehicle entry, system validates if vehicle has valid reservation for the spot
4. If parking in RESERVED spot without valid reservation → RM 100 fine issued automatically
5. Fine must be paid upon exit along with parking fees

### Reservation Validation
- Checks license plate matches reservation
- Checks spot ID matches reservation
- Checks current time is within reservation window (start_time to end_time)
- Checks reservation is active (not cancelled)

## Design Patterns

- **Strategy Pattern**: Fine calculation strategies
- **DAO Pattern**: Data access abstraction
- **MVC Pattern**: Separation of concerns
- **Singleton Pattern**: Database connection management

## License

This project is developed for educational purposes.

## Contributors

University Parking Management Team
