# Requirements Document

## Introduction

The University Parking Lot Management System is a standalone GUI application designed to manage a multi-level parking facility. The system handles vehicle parking, spot allocation, payment processing, and fine management for various vehicle types across multiple floors with different spot categories.

## Glossary

- **System**: The University Parking Lot Management System
- **Parking_Lot**: The multi-level parking facility with floors, rows, and spots
- **Vehicle**: Any motorized or non-motorized transport (motorcycle, car, SUV/truck, handicapped vehicle)
- **Spot**: Individual parking space with unique identifier and type
- **Ticket**: Generated document containing parking information and entry details
- **Fine**: Penalty charge for overstaying or unauthorized parking
- **Admin_Panel**: Administrative interface for system management
- **Entry_Exit_Panel**: User interface for vehicle entry and exit operations
- **Payment_Processor**: Component handling cash and card payments

## Requirements

### Requirement 1: Parking Lot Structure Management

**User Story:** As a parking lot administrator, I want to manage a multi-level parking structure with different spot types, so that I can accommodate various vehicle types and pricing tiers.

#### Acceptance Criteria

1. THE Parking_Lot SHALL support multiple floors (minimum 5 floors)
2. WHEN a floor is created, THE System SHALL organize spots into rows with unique identifiers
3. THE System SHALL support four spot types: Compact (RM 2/hour), Regular (RM 5/hour), Handicapped (RM 2/hour), and Reserved (RM 10/hour)
4. WHEN a spot is created, THE System SHALL assign a unique identifier following format "F{floor}-R{row}-S{spot}"
5. THE System SHALL track spot status as either available or occupied
6. WHEN a spot is occupied, THE System SHALL record the current vehicle information
7. THE System SHALL maintain hourly rates for each spot type

### Requirement 2: Vehicle Type Management

**User Story:** As a system operator, I want to manage different vehicle types with specific parking restrictions, so that vehicles are assigned to appropriate spots.

#### Acceptance Criteria

1. THE System SHALL support four vehicle types: Motorcycle, Car, SUV/Truck, and Handicapped Vehicle
2. WHEN a motorcycle enters, THE System SHALL only allow parking in Compact spots
3. WHEN a car enters, THE System SHALL allow parking in Compact or Regular spots
4. WHEN an SUV/Truck enters, THE System SHALL only allow parking in Regular spots
5. WHEN a handicapped vehicle enters, THE System SHALL allow parking in any spot type
6. THE System SHALL record license plate number, vehicle type, entry time, and exit time for each vehicle
7. WHEN calculating duration, THE System SHALL round up to the nearest hour using ceiling rounding

### Requirement 3: Vehicle Entry Process

**User Story:** As a parking attendant, I want to process vehicle entries efficiently, so that customers can quickly find and occupy suitable parking spots.

#### Acceptance Criteria

1. WHEN a vehicle requests entry, THE System SHALL display available spots matching the vehicle type
2. WHEN a user selects a spot, THE System SHALL mark the spot as occupied
3. WHEN entry is confirmed, THE System SHALL record the entry time and assign the spot to the vehicle
4. WHEN entry is processed, THE System SHALL generate a ticket with format "T-{PLATE}-{TIMESTAMP}"
5. THE System SHALL display the ticket containing spot location and entry time

### Requirement 4: Vehicle Exit Process

**User Story:** As a parking attendant, I want to process vehicle exits with accurate fee calculation, so that customers pay the correct amount including any applicable fines.

#### Acceptance Criteria

1. WHEN a vehicle exits, THE System SHALL accept license plate number as input
2. WHEN license plate is entered, THE System SHALL locate the vehicle and retrieve entry time
3. WHEN calculating fees, THE System SHALL compute parking duration in hours using ceiling rounding
4. WHEN calculating total cost, THE System SHALL multiply duration by spot hourly rate
5. WHEN processing exit, THE System SHALL check for unpaid fines linked to the license plate
6. WHEN displaying payment summary, THE System SHALL show hours parked, parking fee, unpaid fines, and total due
7. WHEN payment is completed, THE System SHALL mark the spot as available
8. WHEN exit is finalized, THE System SHALL generate an exit receipt

### Requirement 5: Fine Management System

**User Story:** As a parking administrator, I want to manage fines for overstaying and unauthorized parking, so that parking regulations are enforced consistently.

#### Acceptance Criteria

1. WHEN a vehicle stays more than 24 hours, THE System SHALL generate an overstaying fine
2. WHEN a vehicle parks in a reserved spot without authorization, THE System SHALL generate an unauthorized parking fine
3. THE System SHALL support three fine calculation schemes: Fixed (RM 50), Progressive (RM 50 + escalating amounts), and Hourly (RM 20/hour)
4. WHEN an administrator selects a fine scheme, THE System SHALL apply it to future entries only
5. THE System SHALL link fines to license plate numbers, not tickets
6. WHEN a customer exits without paying fines, THE System SHALL carry forward unpaid fines to the next visit
7. WHEN processing payment, THE System SHALL allow customers to pay current parking fees and outstanding fines

### Requirement 6: Payment Processing

**User Story:** As a customer, I want to pay for parking using cash or card, so that I can complete my parking transaction conveniently.

#### Acceptance Criteria

1. THE Payment_Processor SHALL accept both cash and card payments
2. WHEN payment is processed, THE System SHALL generate a receipt
3. WHEN generating receipts, THE System SHALL include entry time, exit time, duration, fee breakdown, fines, total paid, payment method, and remaining balance
4. THE System SHALL validate payment amounts against total charges
5. WHEN payment is insufficient, THE System SHALL display the remaining balance due

### Requirement 7: Handicapped Vehicle Pricing

**User Story:** As a handicapped vehicle owner, I want to receive appropriate pricing when parking in designated spots, so that I benefit from accessibility accommodations.

#### Acceptance Criteria

1. WHEN a handicapped vehicle parks in a handicapped spot, THE System SHALL charge RM 2/hour rate
2. WHEN a handicapped vehicle parks in non-handicapped spots, THE System SHALL charge the standard rate for that spot type
3. THE System SHALL verify handicapped status through vehicle registration or card holder identification

### Requirement 8: Administrative Interface

**User Story:** As a parking administrator, I want comprehensive system oversight, so that I can monitor operations and make informed management decisions.

#### Acceptance Criteria

1. THE Admin_Panel SHALL display all floors and their spot statuses
2. WHEN viewing occupancy, THE Admin_Panel SHALL show current occupancy rate as a percentage
3. THE Admin_Panel SHALL display total revenue from fees collected
4. WHEN viewing current vehicles, THE Admin_Panel SHALL list all vehicles currently parked with their details
5. THE Admin_Panel SHALL display all unpaid fines with associated license plates
6. WHEN configuring fines, THE Admin_Panel SHALL allow selection of fine calculation schemes

### Requirement 9: Entry/Exit Interface

**User Story:** As a parking attendant, I want intuitive interfaces for vehicle operations, so that I can efficiently process entries and exits.

#### Acceptance Criteria

1. THE Entry_Exit_Panel SHALL provide separate interfaces for vehicle entry and exit
2. WHEN processing entry, THE Entry_Exit_Panel SHALL display spot selection options
3. WHEN processing exit, THE Entry_Exit_Panel SHALL provide payment processing capabilities
4. THE Entry_Exit_Panel SHALL validate all user inputs and display appropriate error messages
5. WHEN operations complete, THE Entry_Exit_Panel SHALL display results clearly to users

### Requirement 10: Reporting System

**User Story:** As a parking manager, I want comprehensive reports, so that I can analyze parking patterns and financial performance.

#### Acceptance Criteria

1. THE System SHALL generate reports listing all vehicles currently in the parking lot
2. THE System SHALL produce revenue reports showing total fees collected
3. THE System SHALL create occupancy reports displaying utilization statistics
4. THE System SHALL generate fine reports showing outstanding fines by license plate
5. WHEN generating reports, THE System SHALL format data in a readable and organized manner

### Requirement 11: Data Persistence

**User Story:** As a system administrator, I want reliable data storage, so that parking information persists across system sessions.

#### Acceptance Criteria

1. THE System SHALL store all parking lot configuration data persistently
2. WHEN vehicles enter or exit, THE System SHALL persist transaction records
3. THE System SHALL maintain fine records across multiple parking sessions
4. WHEN the system restarts, THE System SHALL restore all current parking states
5. THE System SHALL backup critical data to prevent loss during system failures

### Requirement 12: GUI Implementation

**User Story:** As a system user, I want a professional and intuitive graphical interface, so that I can operate the system efficiently.

#### Acceptance Criteria

1. THE System SHALL implement the user interface using Java Swing
2. THE System SHALL organize functionality into multiple panels or tabs
3. WHEN users interact with buttons, THE System SHALL trigger appropriate actions
4. THE System SHALL validate all inputs and display clear error messages
5. WHEN displaying results, THE System SHALL present information in a clean and organized format
6. THE System SHALL maintain a professional appearance without requiring fancy graphics