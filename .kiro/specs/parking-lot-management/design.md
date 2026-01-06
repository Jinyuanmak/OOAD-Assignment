# Design Document

## Overview

The University Parking Lot Management System is a desktop application built using Java Swing that implements a comprehensive parking facility management solution. The system follows the Model-View-Controller (MVC) architectural pattern to ensure separation of concerns, maintainability, and scalability. The application manages multi-level parking facilities with different spot types, handles vehicle entry/exit operations, processes payments, and manages fines through an intuitive graphical user interface.

## Architecture

The system employs a layered MVC architecture with the following key components:

### Model Layer
- **Domain Models**: Core business entities (ParkingLot, Vehicle, ParkingSpot, Fine, Payment)
- **Data Access Layer**: Database operations and persistence management
- **Business Logic**: Core algorithms for fee calculation, fine management, and spot allocation

### View Layer
- **Swing GUI Components**: JFrames, JPanels, and custom components
- **Admin Panel**: Administrative interface for system oversight
- **Entry/Exit Panel**: Operational interface for vehicle processing
- **Reporting Panel**: Data visualization and report generation

### Controller Layer
- **Event Handlers**: User interaction processing
- **Business Controllers**: Coordination between models and views
- **Validation Controllers**: Input validation and error handling

The architecture supports loose coupling between components, enabling independent testing and maintenance of each layer.

## Components and Interfaces

### Core Domain Models

#### ParkingLot
```java
public class ParkingLot {
    private List<Floor> floors;
    private FineCalculationStrategy fineStrategy;
    private double totalRevenue;
    
    public List<ParkingSpot> findAvailableSpots(VehicleType vehicleType);
    public boolean parkVehicle(Vehicle vehicle, String spotId);
    public ParkingTransaction exitVehicle(String licensePlate);
    public void setFineCalculationStrategy(FineCalculationStrategy strategy);
}
```

#### Vehicle
```java
public class Vehicle {
    private String licensePlate;
    private VehicleType type;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private boolean isHandicapped;
    
    public long calculateParkingDuration();
    public boolean canParkInSpot(SpotType spotType);
}
```

#### ParkingSpot
```java
public class ParkingSpot {
    private String spotId;
    private SpotType type;
    private SpotStatus status;
    private Vehicle currentVehicle;
    private double hourlyRate;
    
    public boolean isAvailable();
    public void occupySpot(Vehicle vehicle);
    public void vacateSpot();
}
```

#### Fine
```java
public class Fine {
    private String licensePlate;
    private FineType type;
    private double amount;
    private LocalDateTime issuedDate;
    private boolean isPaid;
    
    public double calculateFineAmount(FineCalculationStrategy strategy);
}
```

### Strategy Pattern for Fine Calculation

```java
public interface FineCalculationStrategy {
    double calculateFine(long overstayHours);
}

public class FixedFineStrategy implements FineCalculationStrategy {
    public double calculateFine(long overstayHours) {
        return 50.0; // RM 50 flat fine
    }
}

public class ProgressiveFineStrategy implements FineCalculationStrategy {
    public double calculateFine(long overstayHours) {
        // Progressive calculation logic
    }
}

public class HourlyFineStrategy implements FineCalculationStrategy {
    public double calculateFine(long overstayHours) {
        return overstayHours * 20.0; // RM 20 per hour
    }
}
```

### GUI Components

#### MainFrame
```java
public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private AdminPanel adminPanel;
    private EntryExitPanel entryExitPanel;
    private ReportingPanel reportingPanel;
    
    public void initializeComponents();
    public void setupEventHandlers();
}
```

#### AdminPanel
```java
public class AdminPanel extends JPanel {
    private JTable floorTable;
    private JLabel occupancyLabel;
    private JLabel revenueLabel;
    private JComboBox<FineCalculationStrategy> fineStrategyCombo;
    
    public void refreshOccupancyData();
    public void updateRevenueDisplay();
    public void changeFineStrategy();
}
```

#### EntryExitPanel
```java
public class EntryExitPanel extends JPanel {
    private VehicleEntryPanel entryPanel;
    private VehicleExitPanel exitPanel;
    private PaymentPanel paymentPanel;
    
    public void processVehicleEntry();
    public void processVehicleExit();
    public void handlePayment();
}
```

### Data Access Layer

#### DatabaseManager
```java
public class DatabaseManager {
    private Connection connection;
    
    public void initializeDatabase();
    public void saveVehicle(Vehicle vehicle);
    public Vehicle findVehicleByLicensePlate(String licensePlate);
    public List<Fine> getUnpaidFines(String licensePlate);
    public void saveFine(Fine fine);
    public void savePayment(Payment payment);
}
```

## Data Models

### Database Schema

The system uses a relational database with the following key tables:

#### parking_lots
- id (PRIMARY KEY)
- name (VARCHAR)
- total_floors (INTEGER)
- total_revenue (DECIMAL)
- current_fine_strategy (VARCHAR)

#### floors
- id (PRIMARY KEY)
- parking_lot_id (FOREIGN KEY)
- floor_number (INTEGER)
- total_spots (INTEGER)

#### parking_spots
- id (PRIMARY KEY)
- floor_id (FOREIGN KEY)
- spot_id (VARCHAR UNIQUE)
- spot_type (ENUM: COMPACT, REGULAR, HANDICAPPED, RESERVED)
- hourly_rate (DECIMAL)
- status (ENUM: AVAILABLE, OCCUPIED)
- current_vehicle_id (FOREIGN KEY, NULLABLE)

#### vehicles
- id (PRIMARY KEY)
- license_plate (VARCHAR UNIQUE)
- vehicle_type (ENUM: MOTORCYCLE, CAR, SUV_TRUCK, HANDICAPPED)
- is_handicapped (BOOLEAN)
- entry_time (TIMESTAMP)
- exit_time (TIMESTAMP, NULLABLE)
- assigned_spot_id (VARCHAR)

#### fines
- id (PRIMARY KEY)
- license_plate (VARCHAR)
- fine_type (ENUM: OVERSTAY, UNAUTHORIZED_RESERVED)
- amount (DECIMAL)
- issued_date (TIMESTAMP)
- is_paid (BOOLEAN)
- parking_session_id (FOREIGN KEY)

#### payments
- id (PRIMARY KEY)
- license_plate (VARCHAR)
- parking_fee (DECIMAL)
- fine_amount (DECIMAL)
- total_amount (DECIMAL)
- payment_method (ENUM: CASH, CARD)
- payment_date (TIMESTAMP)
- parking_session_id (FOREIGN KEY)

#### parking_sessions
- id (PRIMARY KEY)
- vehicle_id (FOREIGN KEY)
- spot_id (VARCHAR)
- entry_time (TIMESTAMP)
- exit_time (TIMESTAMP, NULLABLE)
- duration_hours (INTEGER)
- ticket_number (VARCHAR UNIQUE)

### Enumerations

```java
public enum VehicleType {
    MOTORCYCLE, CAR, SUV_TRUCK, HANDICAPPED
}

public enum SpotType {
    COMPACT(2.0), REGULAR(5.0), HANDICAPPED(2.0), RESERVED(10.0);
    
    private final double hourlyRate;
    SpotType(double rate) { this.hourlyRate = rate; }
    public double getHourlyRate() { return hourlyRate; }
}

public enum SpotStatus {
    AVAILABLE, OCCUPIED
}

public enum FineType {
    OVERSTAY, UNAUTHORIZED_RESERVED
}

public enum PaymentMethod {
    CASH, CARD
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property Reflection

After analyzing all acceptance criteria, several properties can be consolidated to eliminate redundancy:
- Duration calculation properties (2.7 and 4.3) are identical and can be combined
- Spot status tracking properties can be consolidated into comprehensive state management properties
- Payment and receipt generation properties can be combined where they overlap
- Vehicle parking restriction properties can be grouped by vehicle type

### Core System Properties

**Property 1: Unique Spot Identifier Generation**
*For any* floor with rows and spots, all generated spot IDs should be unique and follow the format "F{floor}-R{row}-S{spot}"
**Validates: Requirements 1.2, 1.4**

**Property 2: Vehicle Parking Restrictions**
*For any* vehicle and spot combination, the system should only allow parking if the vehicle type is compatible with the spot type (motorcycles in compact only, cars in compact/regular, SUV/trucks in regular only, handicapped vehicles in any)
**Validates: Requirements 2.2, 2.3, 2.4, 2.5**

**Property 3: Spot State Consistency**
*For any* parking spot, when a vehicle is assigned to it, the spot status should be occupied and the current vehicle should be recorded; when vacated, the spot should be available with no current vehicle
**Validates: Requirements 1.5, 1.6, 3.2, 4.7**

**Property 4: Duration Calculation Accuracy**
*For any* parking session, the calculated duration should be the ceiling of the actual time difference in hours
**Validates: Requirements 2.7, 4.3**

**Property 5: Fee Calculation Correctness**
*For any* parking session, the total parking fee should equal the duration in hours multiplied by the spot's hourly rate
**Validates: Requirements 4.4**

**Property 6: Ticket Format Compliance**
*For any* vehicle entry, the generated ticket should follow the format "T-{PLATE}-{TIMESTAMP}" and contain spot location and entry time
**Validates: Requirements 3.4, 3.5**

**Property 7: Fine Generation Rules**
*For any* vehicle that stays over 24 hours or parks in reserved spots without authorization, the system should generate appropriate fines linked to the license plate
**Validates: Requirements 5.1, 5.2, 5.5**

**Property 8: Fine Calculation Strategy Application**
*For any* fine calculation, the result should match the currently selected strategy (Fixed: RM 50, Progressive: escalating amounts, Hourly: RM 20/hour)
**Validates: Requirements 5.3**

**Property 9: Fine Persistence Across Sessions**
*For any* license plate with unpaid fines, those fines should appear in subsequent parking sessions until paid
**Validates: Requirements 5.6**

**Property 10: Handicapped Vehicle Pricing**
*For any* handicapped vehicle, the hourly rate should be RM 2/hour when parked in handicapped spots, and the standard spot rate otherwise
**Validates: Requirements 7.1, 7.2**

**Property 11: Payment Validation**
*For any* payment transaction, the system should validate that the payment amount covers the total charges (parking fee + fines) and display remaining balance if insufficient
**Validates: Requirements 6.4, 6.5**

**Property 12: Receipt Content Completeness**
*For any* completed payment, the generated receipt should contain entry time, exit time, duration, fee breakdown, fines, total paid, payment method, and remaining balance
**Validates: Requirements 6.3**

**Property 13: Vehicle Data Recording**
*For any* vehicle entry, the system should record license plate, vehicle type, entry time, and assigned spot; exit time should be recorded upon exit
**Validates: Requirements 2.6, 3.3**

**Property 14: Available Spot Filtering**
*For any* vehicle entry request, the displayed available spots should only include spots that are available and compatible with the vehicle type
**Validates: Requirements 3.1**

**Property 15: Vehicle Lookup Accuracy**
*For any* license plate entered during exit, the system should retrieve the correct vehicle record with accurate entry time and spot assignment
**Validates: Requirements 4.2**

**Property 16: Fine Lookup Integration**
*For any* vehicle exit, the system should check for and display any unpaid fines associated with the license plate
**Validates: Requirements 4.5**

**Property 17: Payment Summary Completeness**
*For any* exit process, the payment summary should display hours parked, parking fee, unpaid fines, and total amount due
**Validates: Requirements 4.6**

**Property 18: Receipt Generation Consistency**
*For any* completed payment, the system should generate both payment receipts and exit receipts
**Validates: Requirements 6.2, 4.8**

**Property 19: Future-Only Fine Strategy Application**
*For any* change in fine calculation strategy, the new strategy should only apply to fines generated after the change, not existing fines
**Validates: Requirements 5.4**

**Property 20: Occupancy Rate Calculation**
*For any* parking lot state, the occupancy rate should equal (occupied spots / total spots) × 100
**Validates: Requirements 8.2**

**Property 21: Revenue Tracking Accuracy**
*For any* completed payment, the total revenue should increase by the amount paid
**Validates: Requirements 8.3**

**Property 22: Current Vehicle Listing**
*For any* point in time, the admin panel should list all vehicles currently parked with their complete details
**Validates: Requirements 8.4**

**Property 23: Unpaid Fine Display**
*For any* unpaid fines in the system, they should appear in the admin panel with associated license plates
**Validates: Requirements 8.5**

**Property 24: Input Validation and Error Handling**
*For any* invalid input provided to the system, appropriate error messages should be displayed without causing system failure
**Validates: Requirements 9.4, 12.4**

**Property 25: Operation Result Display**
*For any* completed operation (entry, exit, payment), the results should be clearly displayed to the user
**Validates: Requirements 9.5**

**Property 26: Report Generation Accuracy**
*For any* report request, the generated report should contain accurate and current data for the requested information type (vehicles, revenue, occupancy, fines)
**Validates: Requirements 10.1, 10.2, 10.3, 10.4**

**Property 27: Data Persistence Consistency**
*For any* system operation that modifies data, the changes should be persisted to the database and survive system restarts
**Validates: Requirements 11.1, 11.2, 11.3, 11.4**

**Property 28: UI Event Handling**
*For any* user interaction with UI components, the appropriate system actions should be triggered
**Validates: Requirements 12.3**

## Error Handling

The system implements comprehensive error handling across all layers:

### Input Validation
- **License Plate Validation**: Ensures proper format and non-empty values
- **Vehicle Type Validation**: Verifies vehicle type is supported
- **Payment Amount Validation**: Ensures positive values and sufficient amounts
- **Spot Selection Validation**: Verifies spot availability and compatibility

### Business Logic Errors
- **Spot Unavailability**: Handles attempts to park in occupied spots
- **Vehicle Not Found**: Manages exit attempts for non-existent vehicles
- **Payment Insufficient**: Processes partial payments and tracks remaining balances
- **Database Connection Failures**: Implements retry mechanisms and graceful degradation

### System Errors
- **Database Errors**: Transaction rollback and error logging
- **GUI Exceptions**: User-friendly error messages and system stability
- **Calculation Errors**: Validation of mathematical operations and boundary conditions

### Error Recovery
- **Transaction Rollback**: Ensures data consistency during failures
- **State Recovery**: Restores system state after unexpected shutdowns
- **User Notification**: Clear error messages with suggested actions

## Testing Strategy

The system employs a dual testing approach combining unit tests and property-based tests to ensure comprehensive coverage and correctness validation.

### Property-Based Testing
Property-based tests will be implemented using **JUnit 5** with **jqwik** library for property-based testing in Java. Each correctness property will be implemented as a separate property-based test with minimum 100 iterations to ensure thorough validation across diverse input spaces.

**Property Test Configuration:**
- **Testing Framework**: JUnit 5 with jqwik
- **Minimum Iterations**: 100 per property test
- **Test Tagging**: Each test tagged with format: **Feature: parking-lot-management, Property {number}: {property_text}**

**Property Test Coverage:**
- Unique identifier generation across all possible floor/row/spot combinations
- Vehicle parking restrictions across all vehicle-spot type combinations
- Fee calculations across various durations and spot types
- Fine calculations across different strategies and overstay periods
- Data persistence across system restart scenarios
- Input validation across all possible invalid input combinations

### Unit Testing
Unit tests complement property-based tests by focusing on specific examples, edge cases, and integration points:

**Core Functionality Tests:**
- Specific parking scenarios (motorcycle in compact spot, car in regular spot)
- Edge cases (exactly 24-hour parking, zero-duration parking)
- Error conditions (invalid license plates, insufficient payments)
- Integration between GUI components and business logic

**Database Integration Tests:**
- CRUD operations for all entities
- Transaction rollback scenarios
- Connection failure handling
- Data consistency across concurrent operations

**GUI Component Tests:**
- Button click event handling
- Input field validation
- Panel navigation and state management
- Error message display

### Test Data Management
- **Test Database**: Separate database instance for testing
- **Data Fixtures**: Predefined test data for consistent testing scenarios
- **Mock Objects**: For external dependencies and GUI components
- **Test Cleanup**: Automated cleanup between test runs

### Continuous Integration
- **Automated Test Execution**: All tests run on code changes
- **Coverage Reporting**: Minimum 90% code coverage requirement
- **Performance Testing**: Response time validation for critical operations
- **Integration Testing**: End-to-end workflow validation

The testing strategy ensures that both universal properties hold across all inputs (property-based tests) and specific scenarios work correctly (unit tests), providing comprehensive validation of system correctness and reliability.