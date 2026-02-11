# University Parking Lot Management System - UML Diagrams

## Table of Contents
1. [Class Diagram](#class-diagram)
2. [Sequence Diagrams](#sequence-diagrams)
3. [State Diagram](#state-diagram)
4. [Use Case Diagram](#use-case-diagram)
5. [Architecture Overview](#architecture-overview)

---

## Class Diagram

### Core Domain Model

```mermaid
classDiagram
    class ParkingLot {
        -String name
        -List~Floor~ floors
        -double totalRevenue
        -FineCalculationContext fineCalculationContext
        -LocalDateTime strategyChangeTime
        +addFloor(Floor)
        +createFloor(int, List~RowConfiguration~) Floor
        +getAllSpots() List~ParkingSpot~
        +findAvailableSpots(VehicleType) List~ParkingSpot~
        +findSpotById(String) ParkingSpot
        +validateUniqueSpotIds() boolean
        +validateSpotIdFormat() boolean
        +changeFineStrategy(FineCalculationStrategy)
        +addRevenue(double)
    }
    
    class Floor {
        -int floorNumber
        -List~List~ParkingSpot~~ rows
        -int totalSpots
        +createRow(int, int, SpotType[])
        +getAllSpots() List~ParkingSpot~
        +getAvailableSpots() List~ParkingSpot~
        +findSpotById(String) ParkingSpot
        +generateSpotId(int, int, int)$ String
    }
    
    class ParkingSpot {
        -String spotId
        -SpotType type
        -SpotStatus status
        -Vehicle currentVehicle
        -double hourlyRate
        +isAvailable() boolean
        +occupySpot(Vehicle)
        +vacateSpot()
        +assignVehicle(Vehicle)
    }
    
    class Vehicle {
        -String licensePlate
        -VehicleType type
        -LocalDateTime entryTime
        -LocalDateTime exitTime
        -boolean isHandicapped
        -String assignedSpotId
        -Long elapsedSeconds
        -Long elapsedMinutes
        -Long elapsedHours
        -Boolean isOverstay
        +calculateParkingDuration() long
        +canParkInSpot(SpotType) boolean
    }
    
    class Fine {
        -Long id
        -String licensePlate
        -FineType type
        -double amount
        -LocalDateTime issuedDate
        -boolean isPaid
        +calculateFineAmount(FineCalculationStrategy, long) double
    }
    
    class Payment {
        -Long id
        -String licensePlate
        -double parkingFee
        -double fineAmount
        -double totalAmount
        -PaymentMethod paymentMethod
        -LocalDateTime paymentDate
        -Long parkingSessionId
    }
    
    class ParkingSession {
        -Long id
        -String licensePlate
        -String spotId
        -LocalDateTime entryTime
        -LocalDateTime exitTime
        -double parkingFee
        -double fineAmount
        -double totalAmount
    }
    
    ParkingLot "1" *-- "1..*" Floor : contains
    Floor "1" *-- "*" ParkingSpot : contains
    ParkingSpot "1" o-- "0..1" Vehicle : parks
```

### Enumerations

```mermaid
classDiagram
    class SpotType {
        <<enumeration>>
        COMPACT
        REGULAR
        HANDICAPPED
        RESERVED
        +getHourlyRate() double
    }
    
    class VehicleType {
        <<enumeration>>
        MOTORCYCLE
        CAR
        SUV_TRUCK
        HANDICAPPED
    }
    
    class SpotStatus {
        <<enumeration>>
        AVAILABLE
        OCCUPIED
        RESERVED
        MAINTENANCE
    }
    
    class FineType {
        <<enumeration>>
        OVERSTAY
        UNAUTHORIZED_PARKING
        EXPIRED_PERMIT
    }
    
    class PaymentMethod {
        <<enumeration>>
        CASH
        CREDIT_CARD
        DEBIT_CARD
        EWALLET
        ONLINE_BANKING
    }
```

### Strategy Pattern - Fine Calculation

```mermaid
classDiagram
    class FineCalculationStrategy {
        <<interface>>
        +calculateFine(long) double
        +getStrategyName() String
    }
    
    class FixedFineStrategy {
        -double FIXED_FINE_AMOUNT = 50.0
        +calculateFine(long) double
        +getStrategyName() String
    }
    
    class HourlyFineStrategy {
        -double HOURLY_RATE = 10.0
        +calculateFine(long) double
        +getStrategyName() String
    }
    
    class ProgressiveFineStrategy {
        -double BASE_FINE = 20.0
        -double ADDITIONAL_RATE = 5.0
        +calculateFine(long) double
        +getStrategyName() String
    }
    
    class FineCalculationContext {
        -FineCalculationStrategy strategy
        +setStrategy(FineCalculationStrategy)
        +getStrategy() FineCalculationStrategy
        +calculateFine(long) double
        +getCurrentStrategyName() String
    }
    
    FineCalculationStrategy <|.. FixedFineStrategy : implements
    FineCalculationStrategy <|.. HourlyFineStrategy : implements
    FineCalculationStrategy <|.. ProgressiveFineStrategy : implements
    FineCalculationContext o-- FineCalculationStrategy : uses
```

### DAO Layer

```mermaid
classDiagram
    class DatabaseManager {
        -Connection connection
        -String DB_URL
        -String DB_USER
        -String DB_PASSWORD
        +getConnection() Connection
        +closeConnection()
        +initializeDatabase()
        +testConnection() boolean
    }
    
    class VehicleDAO {
        -DatabaseManager dbManager
        +save(Vehicle) boolean
        +findByLicensePlate(String) Vehicle
        +findAll() List~Vehicle~
        +update(Vehicle) boolean
        +delete(String) boolean
        +findCurrentVehicles() List~Vehicle~
    }
    
    class ParkingSpotDAO {
        -DatabaseManager dbManager
        +save(ParkingSpot) boolean
        +findById(String) ParkingSpot
        +findAll() List~ParkingSpot~
        +update(ParkingSpot) boolean
        +delete(String) boolean
    }
    
    class FloorDAO {
        -DatabaseManager dbManager
        +save(Floor) boolean
        +findById(Long) Floor
        +findAll() List~Floor~
        +update(Floor) boolean
        +delete(Long) boolean
    }
    
    class FineDAO {
        -DatabaseManager dbManager
        +save(Fine) boolean
        +findByLicensePlate(String) List~Fine~
        +findUnpaid(String) List~Fine~
        +update(Fine) boolean
        +delete(Long) boolean
        +markAsPaid(Long) boolean
    }
    
    class PaymentDAO {
        -DatabaseManager dbManager
        +save(Payment) boolean
        +findByLicensePlate(String) List~Payment~
        +findAll() List~Payment~
        +update(Payment) boolean
        +delete(Long) boolean
    }
    
    class ParkingLotDAO {
        -DatabaseManager dbManager
        +save(ParkingLot) boolean
        +load() ParkingLot
        +update(ParkingLot) boolean
    }
    
    VehicleDAO ..> DatabaseManager : uses
    ParkingSpotDAO ..> DatabaseManager : uses
    FloorDAO ..> DatabaseManager : uses
    FineDAO ..> DatabaseManager : uses
    PaymentDAO ..> DatabaseManager : uses
    ParkingLotDAO ..> DatabaseManager : uses
```

### Controller Layer

```mermaid
classDiagram
    class VehicleEntryController {
        -ParkingLot parkingLot
        -VehicleDAO vehicleDAO
        -ParkingSpotDAO spotDAO
        +processEntry(String, VehicleType, boolean, String) boolean
        +findAvailableSpot(VehicleType) ParkingSpot
        +validateEntry(String, String) String
    }
    
    class VehicleExitController {
        -ParkingLot parkingLot
        -VehicleDAO vehicleDAO
        -ParkingSpotDAO spotDAO
        -FineDAO fineDAO
        -PaymentDAO paymentDAO
        -FineManager fineManager
        -PaymentProcessor paymentProcessor
        +processExit(String, double, PaymentMethod) ExitSummary
        +calculateExitSummary(String) ExitSummary
        +validateExit(String) String
    }
    
    VehicleEntryController ..> VehicleDAO : uses
    VehicleEntryController ..> ParkingSpotDAO : uses
    VehicleExitController ..> VehicleDAO : uses
    VehicleExitController ..> ParkingSpotDAO : uses
    VehicleExitController ..> FineDAO : uses
    VehicleExitController ..> PaymentDAO : uses
```

### Utility Layer

```mermaid
classDiagram
    class FeeCalculator {
        +calculateFee(ParkingSpot, LocalDateTime, LocalDateTime)$ double
        +calculateDuration(LocalDateTime, LocalDateTime)$ long
    }
    
    class FineManager {
        -FineDAO fineDAO
        -ParkingLot parkingLot
        +checkAndIssueFine(Vehicle) Fine
        +getUnpaidFines(String) List~Fine~
        +payFine(Long) boolean
    }
    
    class PaymentProcessor {
        -PaymentDAO paymentDAO
        +processPayment(String, double, double, PaymentMethod) Payment
        +validatePayment(double, double) boolean
    }
    
    class Receipt {
        -String receiptId
        -String licensePlate
        -String spotId
        -LocalDateTime entryTime
        -LocalDateTime exitTime
        -double parkingFee
        -double fineAmount
        -double totalDue
        -double amountPaid
        -double changeAmount
        -PaymentMethod paymentMethod
        +generateReceiptText() String
        +saveToFile(String) boolean
    }
    
    class ReceiptPDFGenerator {
        +generatePDF(Receipt, String)$ File
    }
    
    class ReportExporter {
        +exportReport(ReportType, ExportFormat, ParkingLot, List~Fine~, String)$ File
        -exportToTxt(ReportType, ParkingLot, List~Fine~, File)$
        -exportToPdf(ReportType, ParkingLot, List~Fine~, File)$
        -exportToCsv(ReportType, ParkingLot, List~Fine~, File)$
        -exportVehiclePdf(ParkingLot, File)$
        -exportRevenuePdf(ParkingLot, File)$
        -exportOccupancyPdf(ParkingLot, File)$
        -exportFinePdf(List~Fine~, File)$
    }
    
    FineManager ..> FineDAO : uses
    PaymentProcessor ..> PaymentDAO : uses
```

### View Layer - Main Frames

```mermaid
classDiagram
    class ParkingApplication {
        +main(String[])$ void
    }
    
    class MainFrame {
        -ParkingLot parkingLot
        -VehicleEntryPanel entryPanel
        -VehicleExitPanel exitPanel
        -AdminPanel adminPanel
        -ReportingPanel reportingPanel
        +initializeUI()
        +switchPanel(String)
    }
    
    class ModernMainFrame {
        -ParkingLot parkingLot
        -HeaderPanel headerPanel
        -SideNavigationPanel sideNavPanel
        -JPanel contentPanel
        -StatusBarPanel statusBarPanel
        -ThemeManager themeManager
        +initializeUI()
        +switchPanel(String)
        +applyTheme()
    }
    
    class VehicleEntryPanel {
        -VehicleEntryController controller
        -JTextField licensePlateField
        -JComboBox vehicleTypeCombo
        -JCheckBox handicappedCheck
        +setupUI()
        +handleEntry()
    }
    
    class VehicleExitPanel {
        -VehicleExitController controller
        -JTextField licensePlateField
        -JTextField amountField
        -JComboBox paymentMethodCombo
        +setupUI()
        +handleExit()
    }
    
    class AdminPanel {
        -ParkingLot parkingLot
        -JComboBox strategyCombo
        +setupUI()
        +changeStrategy()
    }
    
    class ReportingPanel {
        -ParkingLot parkingLot
        -JTable reportTable
        +setupUI()
        +generateReport(ReportType)
        +exportReport(ExportFormat)
    }
    
    ParkingApplication ..> MainFrame : creates
    ParkingApplication ..> ModernMainFrame : creates
    MainFrame *-- VehicleEntryPanel
    MainFrame *-- VehicleExitPanel
    MainFrame *-- AdminPanel
    MainFrame *-- ReportingPanel
```

### View Layer - UI Components

```mermaid
classDiagram
    class BasePanel {
        #ParkingLot parkingLot
        #setupUI()*
        #refreshData()*
    }
    
    class HeaderPanel {
        -JLabel titleLabel
        -JLabel userLabel
        +updateUser(String)
    }
    
    class SideNavigationPanel {
        -List~JButton~ buttons
        -JButton activeButton
        +setActive(JButton)
        +addButton(String, ActionListener) JButton
    }
    
    class StatusBarPanel {
        -JLabel statusLabel
        +updateStatus(String)
    }
    
    class DashboardCard {
        -String title
        -String value
        -Icon icon
        +updateValue(String)
        +setIcon(Icon)
    }
    
    class StyledButton {
        -boolean isPrimary
        -boolean isSecondary
        +applyStyle()
    }
    
    class StyledTextField {
        -String placeholder
        +setPlaceholder(String)
        +getText() String
    }
    
    class StyledComboBox {
        -List items
        +addItem(Object)
        +getSelected() Object
    }
    
    class StyledTable {
        -TableModel model
        -String[] columns
        +setData(Object[][])
        +refresh()
    }
    
    class StyledDialog {
        -String title
        -String message
        +show()
        +showError(String)$
        +showSuccess(String)$
    }
    
    class ThemeManager {
        -String currentTheme
        -Map~String,Color~ colors
        +applyTheme(String)
        +getColor(String) Color
        +setTheme(String)
    }
    
    class InputValidator {
        +validateLicensePlate(String)$ boolean
        +validateAmount(String)$ boolean
        +validateSpotId(String)$ boolean
    }
    
    class EventHandler {
        +onClick()
        +onChange()
        +onSubmit()
    }
    
    VehicleEntryPanel --|> BasePanel
    VehicleExitPanel --|> BasePanel
    AdminPanel --|> BasePanel
    ReportingPanel --|> BasePanel
```

---

## Sequence Diagrams

### 1. Vehicle Entry Process

```mermaid
sequenceDiagram
    actor User
    participant EntryPanel
    participant EntryController
    participant VehicleDAO
    participant SpotDAO
    participant ParkingLot
    
    User->>EntryPanel: Enter Details
    EntryPanel->>EntryController: processEntry()
    EntryController->>ParkingLot: findAvailableSpots()
    ParkingLot-->>EntryController: Available Spots
    EntryController->>VehicleDAO: save(vehicle)
    VehicleDAO-->>EntryController: Vehicle ID
    EntryController->>SpotDAO: update(spot)
    SpotDAO-->>EntryController: Success
    EntryController-->>EntryPanel: Entry Success
    EntryPanel-->>User: Show Success
```

### 2. Vehicle Exit Process

```mermaid
sequenceDiagram
    actor User
    participant ExitPanel
    participant ExitController
    participant FineManager
    participant PaymentProcessor
    participant VehicleDAO
    participant SpotDAO
    participant PaymentDAO
    
    User->>ExitPanel: Enter License Plate
    ExitPanel->>ExitController: calculateExitSummary()
    ExitController->>VehicleDAO: findByLicensePlate()
    VehicleDAO-->>ExitController: Vehicle Data
    ExitController->>FineManager: checkAndIssueFine()
    FineManager-->>ExitController: Fine (if any)
    ExitController-->>ExitPanel: Exit Summary
    ExitPanel-->>User: Show Summary
    
    User->>ExitPanel: Confirm Payment
    ExitPanel->>ExitController: processExit()
    ExitController->>PaymentProcessor: processPayment()
    PaymentProcessor->>PaymentDAO: save(payment)
    PaymentDAO-->>PaymentProcessor: Payment ID
    PaymentProcessor-->>ExitController: Payment
    ExitController->>VehicleDAO: update(vehicle)
    ExitController->>SpotDAO: vacateSpot()
    ExitController->>ExitController: generateReceipt()
    ExitController-->>ExitPanel: Receipt
    ExitPanel-->>User: Show Receipt
```

### 3. Fine Calculation Strategy Change

```mermaid
sequenceDiagram
    actor Admin
    participant AdminPanel
    participant ParkingLot
    participant FineCalculationContext
    participant FineCalculationStrategy
    
    Admin->>AdminPanel: Select Strategy
    AdminPanel->>ParkingLot: changeFineStrategy()
    ParkingLot->>FineCalculationContext: setStrategy()
    FineCalculationContext->>FineCalculationStrategy: new Strategy()
    FineCalculationStrategy-->>FineCalculationContext: Strategy Instance
    FineCalculationContext-->>ParkingLot: Strategy Set
    ParkingLot->>ParkingLot: setStrategyChangeTime()
    ParkingLot-->>AdminPanel: Success
    AdminPanel-->>Admin: Show Success
```

### 4. Report Generation and Export

```mermaid
sequenceDiagram
    actor User
    participant ReportingPanel
    participant ParkingLot
    participant ReportExporter
    participant PDFBox
    
    User->>ReportingPanel: Select Report Type
    ReportingPanel->>ParkingLot: getData()
    ParkingLot-->>ReportingPanel: Report Data
    ReportingPanel-->>User: Display Report
    
    User->>ReportingPanel: Click Export
    ReportingPanel->>ReportExporter: exportReport(type, format, data)
    ReportExporter->>PDFBox: createDocument()
    ReportExporter->>PDFBox: addContent()
    ReportExporter->>PDFBox: save()
    PDFBox-->>ReportExporter: File Created
    ReportExporter-->>ReportingPanel: File Path
    ReportingPanel-->>User: Show Success
```

---

## State Diagrams

### Parking Spot State Diagram

```mermaid
stateDiagram-v2
    [*] --> AVAILABLE
    AVAILABLE --> OCCUPIED : occupySpot(vehicle)
    OCCUPIED --> AVAILABLE : vacateSpot()
    AVAILABLE --> MAINTENANCE : markForMaintenance()
    MAINTENANCE --> AVAILABLE : completeMaintenance()
    AVAILABLE --> RESERVED : reserve()
    RESERVED --> AVAILABLE : cancelReservation()
```

### Vehicle Parking Session State Diagram

```mermaid
stateDiagram-v2
    [*] --> ENTERED : Vehicle enters
    ENTERED --> PARKED : Entry time recorded
    PARKED --> OVERSTAY : time > 24 hours
    PARKED --> EXITING : processExit()
    OVERSTAY --> EXITING : processExit()
    EXITING --> PAYMENT_PROCESSING : Calculate charges
    PAYMENT_PROCESSING --> EXITED : Payment successful
    EXITED --> [*]
```

### Fine State Diagram

```mermaid
stateDiagram-v2
    [*] --> ISSUED : Fine issued
    ISSUED --> PAID : payFine()
    ISSUED --> UNPAID : time passes
    UNPAID --> PAID : payFine()
    PAID --> [*]
```

---

## Use Case Diagram

```mermaid
graph TB
    subgraph System[University Parking Lot Management System]
        UC1[Enter Parking Lot]
        UC2[Find Available Parking Spot]
        UC3[Exit Parking Lot]
        UC4[Calculate Parking Fee]
        UC5[Calculate Fine]
        UC6[Make Payment]
        UC7[Generate Receipt]
        UC8[View Parking Status]
        UC9[View Revenue Report]
        UC10[View Occupancy Report]
        UC11[View Vehicle Report]
        UC12[View Fine Report]
        UC13[Export Report]
        UC14[Change Fine Calculation Strategy]
        UC15[Manage Parking Spots]
        UC16[View System Dashboard]
        UC17[Persist Vehicle Data]
        UC18[Persist Payment Data]
        UC19[Persist Fine Data]
        UC20[Calculate Real-time Parking Duration]
    end
    
    Driver((Driver))
    Admin((Admin))
    Database((Database))
    
    Driver --> UC1
    UC1 -.->|include| UC2
    Driver --> UC3
    UC3 -.->|include| UC4
    UC4 -.->|extend| UC5
    UC4 -.->|include| UC6
    UC6 -.->|include| UC7
    Driver --> UC8
    
    Admin --> UC9
    Admin --> UC10
    Admin --> UC11
    Admin --> UC12
    UC9 -.->|include| UC13
    UC10 -.->|include| UC13
    UC11 -.->|include| UC13
    UC12 -.->|include| UC13
    Admin --> UC14
    Admin --> UC15
    Admin --> UC16
    
    Database --> UC17
    Database --> UC18
    Database --> UC19
    Database --> UC20
```

---

## Architecture Overview

### Layered Architecture

```mermaid
graph TB
    subgraph Presentation[PRESENTATION LAYER]
        UI[Swing UI Components<br/>MainFrame / ModernMainFrame<br/>Entry/Exit/Admin/Reporting Panels<br/>Styled Components<br/>Theme Manager, Input Validator]
    end
    
    subgraph Controller[CONTROLLER LAYER]
        CTRL[Business Logic Controllers<br/>VehicleEntryController<br/>VehicleExitController]
    end
    
    subgraph Service[SERVICE/UTILITY LAYER]
        SVC[Business Services<br/>FeeCalculator, FineManager<br/>PaymentProcessor, Receipt<br/>ReceiptPDFGenerator, ReportExporter]
    end
    
    subgraph DAO[DATA ACCESS LAYER]
        DA[Data Access Objects<br/>DatabaseManager<br/>VehicleDAO, ParkingSpotDAO<br/>FloorDAO, FineDAO<br/>PaymentDAO, ParkingLotDAO]
    end
    
    subgraph Database[DATABASE LAYER]
        DB[MySQL Database<br/>Tables: parking_lots, floors,<br/>parking_spots, vehicles,<br/>fines, payments, parking_sessions<br/>Views: current_vehicles_with_elapsed_time]
    end
    
    subgraph Domain[DOMAIN MODEL LAYER]
        DM[Domain Entities<br/>ParkingLot, Floor, ParkingSpot<br/>Vehicle, Fine, Payment<br/>Enumerations<br/>Strategy Pattern]
    end
    
    UI -->|User Actions| CTRL
    CTRL -->|Business Operations| SVC
    SVC -->|Data Operations| DA
    DA -->|SQL Queries| DB
    DB -->|Persistent Storage| DM
    CTRL --> DM
    SVC --> DM
    DA --> DM
```

### Database Schema

```mermaid
erDiagram
    PARKING_LOTS ||--o{ FLOORS : contains
    FLOORS ||--o{ PARKING_SPOTS : contains
    PARKING_SPOTS ||--o| VEHICLES : parks
    VEHICLES ||--o{ FINES : may_have
    VEHICLES ||--o{ PAYMENTS : makes
    VEHICLES ||--o{ PARKING_SESSIONS : creates
    
    PARKING_LOTS {
        bigint id PK
        varchar name
        decimal total_revenue
        varchar fine_strategy
        datetime strategy_change_time
    }
    
    FLOORS {
        bigint id PK
        bigint parking_lot_id FK
        int floor_number
        int total_spots
    }
    
    PARKING_SPOTS {
        bigint id PK
        varchar spot_id UK
        bigint floor_id FK
        varchar spot_type
        varchar status
        decimal hourly_rate
        varchar current_vehicle_license_plate FK
    }
    
    VEHICLES {
        varchar license_plate PK
        varchar vehicle_type
        datetime entry_time
        datetime exit_time
        boolean is_handicapped
        varchar assigned_spot_id FK
    }
    
    FINES {
        bigint id PK
        varchar license_plate FK
        varchar fine_type
        decimal amount
        datetime issued_date
        boolean is_paid
    }
    
    PAYMENTS {
        bigint id PK
        varchar license_plate FK
        decimal parking_fee
        decimal fine_amount
        decimal total_amount
        varchar payment_method
        datetime payment_date
        bigint parking_session_id FK
    }
    
    PARKING_SESSIONS {
        bigint id PK
        varchar license_plate
        varchar spot_id
        datetime entry_time
        datetime exit_time
        decimal parking_fee
        decimal fine_amount
        decimal total_amount
    }
```

---

## Design Patterns

### Strategy Pattern Implementation

```mermaid
graph TB
    subgraph Strategy Pattern
        Context[FineCalculationContext]
        Interface[FineCalculationStrategy Interface]
        Fixed[FixedFineStrategy<br/>50 RM flat]
        Hourly[HourlyFineStrategy<br/>10 RM per hour]
        Progressive[ProgressiveFineStrategy<br/>20 RM base + 5 RM per hour]
        
        Context -->|uses| Interface
        Interface <|..|implements| Fixed
        Interface <|..|implements| Hourly
        Interface <|..|implements| Progressive
    end
    
    ParkingLot -->|contains| Context
    Admin -->|changes strategy| ParkingLot
```

---

## Summary

This UML documentation provides a comprehensive view of the University Parking Lot Management System using Mermaid diagrams:

- **Class Diagrams**: Detailed class structures across all layers (Model, DAO, Controller, Utility, View)
- **Package Diagram**: Organization of classes into logical packages
- **Sequence Diagrams**: Key workflows (Vehicle Entry, Exit, Fine Strategy Change, Report Export)
- **State Diagrams**: State transitions for Parking Spots, Vehicles, and Fines
- **Use Case Diagram**: Actor interactions and system functionality
- **Architecture Overview**: Layered architecture and design patterns
- **Database Schema**: Entity relationship diagram

The system follows a clean layered architecture with clear separation of concerns:
- **Presentation Layer**: Swing UI components
- **Controller Layer**: Business logic
- **Service Layer**: Utility services
- **DAO Layer**: Data access
- **Database Layer**: MySQL persistence
- **Domain Model**: Core business entities

Key design patterns implemented:
- Strategy Pattern for fine calculations
- DAO Pattern for data access
- MVC Pattern for application structure
- Singleton for database management
