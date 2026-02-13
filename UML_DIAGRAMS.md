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
    
    class Reservation {
        -Long id
        -String licensePlate
        -String spotId
        -LocalDateTime startTime
        -LocalDateTime endTime
        -boolean isActive
        -LocalDateTime createdAt
        -double prepaidAmount
        +isValidAt(LocalDateTime) boolean
        +isCurrentlyValid() boolean
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
    ParkingSpot "1" o-- "*" Reservation : reserved_by
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
        RESERVED_SPOT_VIOLATION
        UNPAID_BALANCE
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
    
    class ReservationDAO {
        -DatabaseManager dbManager
        +save(Reservation) boolean
        +findValidReservation(String, String, LocalDateTime) Reservation
        +findByLicensePlate(String) List~Reservation~
        +findBySpotId(String) List~Reservation~
        +isSpotReserved(String, LocalDateTime, LocalDateTime) boolean
        +cancel(Long) boolean
        +findAll() List~Reservation~
    }
    
    VehicleDAO ..> DatabaseManager : uses
    ParkingSpotDAO ..> DatabaseManager : uses
    FloorDAO ..> DatabaseManager : uses
    FineDAO ..> DatabaseManager : uses
    PaymentDAO ..> DatabaseManager : uses
    ParkingLotDAO ..> DatabaseManager : uses
    ReservationDAO ..> DatabaseManager : uses
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
        -ReservationDAO reservationDAO
        -FineManager fineManager
        -PaymentProcessor paymentProcessor
        +processExit(String, double, PaymentMethod) ExitSummary
        +calculateExitSummary(String) ExitSummary
        +validateExit(String) String
        +generatePaymentSummary(String, List~Fine~) PaymentSummary
        +getUnpaidFines(String) List~Fine~
    }
    
    VehicleEntryController ..> VehicleDAO : uses
    VehicleEntryController ..> ParkingSpotDAO : uses
    VehicleExitController ..> VehicleDAO : uses
    VehicleExitController ..> ParkingSpotDAO : uses
    VehicleExitController ..> FineDAO : uses
    VehicleExitController ..> PaymentDAO : uses
    VehicleExitController ..> ReservationDAO : uses
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
        -boolean hasPrepaidReservation
        -boolean isWithinGracePeriod
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
    
    class ReservationPanel {
        -ParkingLot parkingLot
        -ReservationDAO reservationDAO
        -PaymentDAO paymentDAO
        -ParkingLotDAO parkingLotDAO
        -JTextField licensePlateField
        -JComboBox spotIdComboBox
        -JTextField hoursField
        -JTable reservationTable
        +createReservation()
        +cancelReservation()
        +loadReservations()
        +refreshAvailableSpots()
    }
    
    ParkingApplication ..> MainFrame : creates
    ParkingApplication ..> ModernMainFrame : creates
    MainFrame *-- VehicleEntryPanel
    MainFrame *-- VehicleExitPanel
    MainFrame *-- AdminPanel
    MainFrame *-- ReportingPanel
    MainFrame *-- ReservationPanel
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
    participant ReservationDAO
    participant FineManager
    participant PaymentProcessor
    participant VehicleDAO
    participant SpotDAO
    participant PaymentDAO
    
    User->>ExitPanel: Enter License Plate
    ExitPanel->>ExitController: calculateExitSummary()
    ExitController->>VehicleDAO: findByLicensePlate()
    VehicleDAO-->>ExitController: Vehicle Data
    ExitController->>ReservationDAO: findValidReservation()
    ReservationDAO-->>ExitController: Reservation (if exists)
    ExitController->>ExitController: Check Grace Period (15 min)
    ExitController->>FineManager: checkAndIssueFine()
    FineManager-->>ExitController: Fine (if any)
    ExitController-->>ExitPanel: Exit Summary
    ExitPanel-->>User: Show Summary
    
    alt Prepaid Reservation or Grace Period
        ExitPanel->>ExitPanel: Lock Payment Amount Field
        alt CASH Payment
            User->>ExitPanel: Select CASH
            ExitPanel->>User: Show Cash Denomination Dialog
            User->>ExitPanel: Insert Cash (RM 1/5/10/50/100)
            ExitPanel->>ExitController: processExit(0.00, CASH)
            ExitController->>PaymentProcessor: processPayment()
            PaymentProcessor-->>ExitController: Payment
            ExitController-->>ExitPanel: Receipt with Refund Notice
            ExitPanel->>User: Show Receipt Dialog
            ExitPanel->>User: Ask to Save PDF
        else CARD Payment
            User->>ExitPanel: Select CARD
            ExitPanel->>ExitController: processExit(0.00, CARD)
            ExitController->>PaymentProcessor: processPayment()
            PaymentProcessor-->>ExitController: Payment
            ExitController-->>ExitPanel: Receipt
            ExitPanel->>User: Show Receipt Dialog
            ExitPanel->>User: Ask to Save PDF
        end
    else Normal Exit
        User->>ExitPanel: Confirm Payment
        ExitPanel->>ExitController: processExit()
        ExitController->>PaymentProcessor: processPayment()
        PaymentProcessor->>PaymentDAO: save(payment)
        PaymentDAO-->>PaymentProcessor: Payment ID
        PaymentProcessor-->>ExitController: Payment
    end
    
    ExitController->>VehicleDAO: update(vehicle)
    ExitController->>SpotDAO: vacateSpot()
    ExitController->>ExitController: generateReceipt()
    ExitController-->>ExitPanel: Receipt
    ExitPanel-->>User: Show Receipt
```

### 3. Reservation Creation Process

```mermaid
sequenceDiagram
    actor User
    participant ReservationPanel
    participant ReservationDAO
    participant PaymentDAO
    participant ParkingLotDAO
    participant ParkingLot
    
    User->>ReservationPanel: Enter Reservation Details
    ReservationPanel->>ReservationPanel: Validate Inputs
    ReservationPanel->>ReservationPanel: Calculate Prepaid Amount
    ReservationPanel->>User: Show Payment Dialog (CASH/CARD)
    User->>ReservationPanel: Select Payment Method
    
    ReservationPanel->>ReservationDAO: isSpotReserved(spotId, startTime, endTime)
    ReservationDAO-->>ReservationPanel: Availability Status
    
    alt Spot Available
        ReservationPanel->>ReservationDAO: save(reservation)
        ReservationDAO-->>ReservationPanel: Reservation ID
        ReservationPanel->>PaymentDAO: save(payment)
        PaymentDAO-->>ReservationPanel: Payment ID
        ReservationPanel->>ParkingLot: addRevenue(prepaidAmount)
        ParkingLot-->>ReservationPanel: Revenue Updated
        ReservationPanel->>ParkingLotDAO: updateRevenue()
        ParkingLotDAO-->>ReservationPanel: Success
        ReservationPanel-->>User: Show Success Message
        ReservationPanel->>ReservationPanel: refreshAvailableSpots()
        ReservationPanel->>ReservationPanel: loadReservations()
    else Spot Already Reserved
        ReservationPanel-->>User: Show Error Message
    end
```

### 4. Fine Calculation Strategy Change

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

### 5. Report Generation and Export

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

### 6. Reserved Spot Violation Fine

```mermaid
sequenceDiagram
    actor Driver
    participant EntryController
    participant VehicleDAO
    participant ReservationDAO
    participant FineDAO
    participant ParkingSpot
    
    Driver->>EntryController: Enter Reserved Spot
    EntryController->>ParkingSpot: Check Spot Type
    ParkingSpot-->>EntryController: RESERVED
    EntryController->>ReservationDAO: findValidReservation(licensePlate, spotId)
    ReservationDAO-->>EntryController: null (No Valid Reservation)
    EntryController->>FineDAO: Issue RESERVED_SPOT_VIOLATION Fine (RM 100)
    FineDAO-->>EntryController: Fine Created
    EntryController->>VehicleDAO: save(vehicle)
    VehicleDAO-->>EntryController: Vehicle Saved
    EntryController-->>Driver: Entry Allowed with Fine Notice
```

### 7. Grace Period Exit (15-Minute U-Turn)

```mermaid
sequenceDiagram
    actor Driver
    participant ExitPanel
    participant ExitController
    participant VehicleDAO
    
    Driver->>ExitPanel: Enter License Plate (within 15 min)
    ExitPanel->>ExitController: generatePaymentSummary()
    ExitController->>VehicleDAO: findByLicensePlate()
    VehicleDAO-->>ExitController: Vehicle Data
    ExitController->>ExitController: Calculate Duration (< 15 min)
    ExitController-->>ExitPanel: Summary (RM 0.00 - Grace Period)
    ExitPanel->>ExitPanel: Lock Payment Amount Field
    
    alt CASH Payment
        Driver->>ExitPanel: Select CASH
        ExitPanel->>Driver: Show Cash Denomination Dialog
        Driver->>ExitPanel: Insert Cash (e.g., RM 10)
        ExitPanel->>ExitController: processExit(0.00, CASH)
        ExitController-->>ExitPanel: Receipt with Refund Notice
        ExitPanel->>Driver: Show Receipt + Refund RM 10
    else CARD Payment
        Driver->>ExitPanel: Select CARD
        ExitPanel->>ExitController: processExit(0.00, CARD)
        ExitController-->>ExitPanel: Receipt
        ExitPanel->>Driver: Show Receipt
    end
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
        UC21[Create Reservation]
        UC22[Cancel Reservation]
        UC23[View Reservations]
        UC24[Process Prepaid Payment]
        UC25[Apply Grace Period]
        UC26[Process Cash Refund]
        UC27[Issue Reserved Spot Violation Fine]
    end
    
    Driver((Driver))
    Admin((Admin))
    Database((Database))
    
    Driver --> UC1
    UC1 -.->|include| UC2
    UC1 -.->|extend| UC27
    Driver --> UC3
    UC3 -.->|include| UC4
    UC4 -.->|extend| UC5
    UC4 -.->|extend| UC25
    UC4 -.->|include| UC6
    UC6 -.->|extend| UC26
    UC6 -.->|include| UC7
    Driver --> UC8
    Driver --> UC21
    UC21 -.->|include| UC24
    Driver --> UC22
    Driver --> UC23
    
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
    Admin --> UC23
    
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
    PARKING_SPOTS ||--o{ RESERVATIONS : reserved_by
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
    
    RESERVATIONS {
        bigint id PK
        varchar license_plate
        varchar spot_id FK
        datetime start_time
        datetime end_time
        boolean is_active
        datetime created_at
        decimal prepaid_amount
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


---

## New Features Summary

### 1. Reservation System
- **Prepaid Reservations**: Customers pay upfront for reserved spots (RM 10/hour)
- **Multiple Entry/Exit**: Within reservation period, vehicles can enter/exit multiple times without additional charges
- **Reservation Management**: Create, view, and cancel reservations
- **No Refund Policy**: Cancelled reservations do not receive refunds
- **Database Persistence**: Reservations stored in `reservations` table

### 2. Grace Period (15-Minute U-Turn)
- **Zero Charge Exit**: Vehicles exiting within 15 minutes pay RM 0.00 parking fee
- **Applies To**:
  - All non-RESERVED spots
  - RESERVED spots WITHOUT valid reservation (wrong plate number)
- **After Grace Period**: Minimum 1-hour parking fee applies
- **Payment Handling**:
  - CASH: Must insert cash (RM 1/5/10/50/100), then full refund
  - CARD: No amount needed, can exit with RM 0.00

### 3. Reserved Spot Violation Fine
- **New Fine Type**: `RESERVED_SPOT_VIOLATION`
- **Amount**: RM 100 fixed fine
- **Triggered When**: Non-reserved vehicle enters a RESERVED spot
- **Grace Period Interaction**:
  - Exit within 15 min: RM 100 fine only (no parking fee)
  - Exit after 15 min: RM 100 fine + RM 10 parking fee = RM 110

### 4. Prepaid Reservation Exit
- **Zero Payment**: Vehicles with valid reservations pay RM 0.00 at exit
- **Payment Method Handling**:
  - CASH: Shows denomination dialog, inserts cash, receives full refund
  - CARD: No amount needed, exits immediately
- **Receipt Generation**: Shows "PREPAID RESERVATION" status on receipt
- **UI Enhancement**: Payment amount field is locked (disabled) for prepaid exits

### 5. Cash Refund System
- **Denomination Selection**: RM 1, 5, 10, 50, 100
- **Refund Scenarios**:
  - Grace period exits with CASH payment
  - Prepaid reservation exits with CASH payment
- **Receipt Display**: Shows cash inserted and refund amount
- **PDF Generation**: Option to save receipt with refund details

### 6. UI Enhancements
- **Payment Amount Field Locking**: Automatically locks for grace period and prepaid reservation exits
- **Receipt Dialog**: Shows receipt in dialog before asking to save PDF
- **Cash Denomination Dialog**: User-friendly selection for cash payments
- **Status Indicators**: Clear display of "PREPAID", "15-MIN GRACE", or normal payment status

### 7. Fine Types
- **OVERSTAY**: Vehicle parked > 24 hours (strategy-based calculation)
- **UNAUTHORIZED_PARKING**: General unauthorized parking
- **EXPIRED_PERMIT**: Parking with expired permit
- **RESERVED_SPOT_VIOLATION**: Non-reserved vehicle in RESERVED spot (RM 100)
- **UNPAID_BALANCE**: Remaining balance from partial payment

### 8. Reservation Behavior
- **If Vehicle Never Enters**: Reservation remains valid, prepaid amount already collected, spot remains available for other vehicles
- **Expiration**: After end time, reservation status shows "EXPIRED" but remains in database for record-keeping
- **No Entry Required**: System does not require vehicle to physically enter to maintain reservation validity
