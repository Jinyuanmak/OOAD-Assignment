# University Parking Lot Management System

A comprehensive parking facility management system built with Java Swing and MySQL.

## Features

### Vehicle Management
- Vehicle entry with automatic spot assignment
- Vehicle exit with fee calculation
- Support for multiple vehicle types (Motorcycle, Car, SUV/Truck, Handicapped)
- Real-time parking duration tracking

### Parking Spot Management
- Multi-floor parking structure
- Different spot types: Compact, Regular, Handicapped, Reserved
- Dynamic hourly rates based on spot type
- Automatic spot availability tracking

### Fine Management
- Automatic fine issuance for overstay (>24 hours)
- Multiple fine calculation strategies:
  - Fixed: RM 50 flat fine
  - Hourly: RM 10 per hour
  - Progressive: RM 20 base + RM 5 per additional hour
- Fine payment tracking

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

1. Start MySQL server
2. Create database:
```sql
CREATE DATABASE parking_management;
```

3. Run the database setup script:
```bash
mysql -u root -p parking_management < database/database_setup.sql
```

4. Update database credentials in `DatabaseManager.java` if needed:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/parking_management";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "your_password";
```

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
4. Check "Handicapped" if applicable
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
1. Navigate to "Admin" panel
2. Change fine calculation strategy
3. View system statistics
4. Manage parking configuration

### Reports
1. Navigate to "Reporting" panel
2. Select report type
3. View report data
4. Export to TXT, PDF, or CSV format

## Configuration

### Parking Lot Setup
The system initializes with:
- 3 floors
- 10 spots per floor
- Mixed spot types (Compact, Regular, Handicapped)

Modify in `ParkingApplication.java` to customize.

### Hourly Rates
- Compact: RM 2.00/hour
- Regular: RM 5.00/hour
- Handicapped: RM 2.00/hour
- Reserved: RM 10.00/hour

Modify in `SpotType.java` enum.

### Fine Strategies
Default: Fixed (RM 50)

Change via Admin panel or modify in `ParkingLot.java`.

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

### Views
- `current_vehicles_with_elapsed_time`: Real-time duration calculation

## Design Patterns

- **Strategy Pattern**: Fine calculation strategies
- **DAO Pattern**: Data access abstraction
- **MVC Pattern**: Separation of concerns
- **Singleton Pattern**: Database connection management

## License

This project is developed for educational purposes.

## Contributors

University Parking Management Team
