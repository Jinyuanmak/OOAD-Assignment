# UML Diagrams - Parking Lot Management System

This document contains UML diagrams representing the actual system architecture using Mermaid syntax.

## Table of Contents
1. [Class Diagram - Model Layer](#1-class-diagram---model-layer)
2. [Class Diagram - DAO Layer](#2-class-diagram---dao-layer)
3. [Class Diagram - View Layer](#3-class-diagram---view-layer)
4. [Class Diagram - Controller & Utility](#4-class-diagram---controller--utility)
5. [Use Case Diagram](#5-use-case-diagram)
6. [Sequence Diagram - Vehicle Entry](#6-sequence-diagram---vehicle-entry)
7. [Sequence Diagram - Vehicle Exit & Payment](#7-sequence-diagram---vehicle-exit--payment)
8. [Component Diagram](#8-component-diagram)
9. [Package Diagram](#9-package-diagram)
10. [Entity Relationship Diagram](#10-entity-relationship-diagram-database)

---

## 1. Class Diagram - Model Layer

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
        +validateUniqueSpotIds() boolean
        +validateSpotIdFormat() boolean
        +findAvailableSpots(VehicleType) List~ParkingSpot~
        +addRevenue(double)
        +changeFineStrategy(FineCalculationStrategy)
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
        +assignVehicle(Vehicle)
        +vacateSpot()
    }

    class Vehicle {
        -String licensePlate
        -VehicleType type
        -LocalDateTime entryTime
        -LocalDateTime exitTime
        -boolean isHandicapped
        -String assignedSpotId
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
        -Long vehicleId
        -String spotId
        -LocalDateTime entryTime
        -LocalDateTime exitTime
        -Integer durationHours
        -String ticketNumber
    }

    class VehicleType {
        <<enumeration>>
        MOTORCYCLE
        CAR
        SUV_TRUCK
        HANDICAPPED
    }

    class SpotType {
        <<enumeration>>
        COMPACT
        REGULAR
        HANDICAPPED
        RESERVED
        -double hourlyRate
        +getHourlyRate() double
    }

    class SpotStatus {
        <<enumeration>>
        AVAILABLE
        OCCUPIED
    }

    class FineType {
        <<enumeration>>
        OVERSTAY
        UNAUTHORIZED_RESERVED
        UNPAID_BALANCE
    }

    class PaymentMethod {
        <<enumeration>>
        CASH
        CARD
    }

    class FineCalculationStrategy {
        <<interface>>
        +calculateFine(long) double
        +getStrategyName() String
    }

    class FixedFineStrategy {
        -double FIXED_FINE_AMOUNT$
        +calculateFine(long) double
        +getStrategyName() String
    }

    class HourlyFineStrategy {
        -double HOURLY_RATE$
        +calculateFine(long) double
        +getStrategyName() String
    }

    class ProgressiveFineStrategy {
        -double BASE_FINE$
        -double ESCALATION_RATE$
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

    ParkingLot "1" *-- "many" Floor : contains
    Floor "1" *-- "many" ParkingSpot : contains
    ParkingSpot "1" o-- "0..1" Vehicle : parks
    ParkingSpot -- SpotType
    ParkingSpot -- SpotStatus
    Vehicle -- VehicleType
    Fine -- FineType
    Payment -- PaymentMethod
    
    ParkingLot --> FineCalculationContext : uses
    FineCalculationContext --> FineCalculationStrategy : uses
    FineCalculationStrategy <|.. FixedFineStrategy
    FineCalculationStrategy <|.. HourlyFineStrategy
    FineCalculationStrategy <|.. ProgressiveFineStrategy
```

---

## 2. Class Diagram - DAO Layer

```mermaid
classDiagram
    class DatabaseManager {
        -String databaseName
        -String dbUrl
        -String user
        -String password
        -int poolSize
        -BlockingQueue~Connection~ connectionPool
        -boolean initialized
        +initializeDatabase()
        +getConnection() Connection
        +releaseConnection(Connection)
        +shutdown()
        +isInitialized() boolean
    }

    class ParkingLotDAO {
        -DatabaseManager dbManager
        -FloorDAO floorDAO
        -ParkingSpotDAO spotDAO
        -VehicleDAO vehicleDAO
        +parkingLotExists() boolean
        +loadParkingLot() ParkingLot
        +saveParkingLot(ParkingLot) Long
        +updateRevenue(double)
    }

    class FloorDAO {
        -DatabaseManager dbManager
        +save(Long, Floor) Long
        +findByParkingLotId(Long) List~Floor~
        +getFloorId(Long, int) Long
        +findById(Long) Floor
        +delete(Long)
    }

    class ParkingSpotDAO {
        -DatabaseManager dbManager
        +save(Long, ParkingSpot) Long
        +findById(Long) ParkingSpot
        +findBySpotId(String) ParkingSpot
        +findAvailable() List~ParkingSpot~
        +findByFloorId(Long) List~ParkingSpot~
        +update(String, ParkingSpot)
        +updateStatus(String, SpotStatus)
        +delete(Long)
        +findAll() List~ParkingSpot~
    }

    class VehicleDAO {
        -DatabaseManager dbManager
        +save(Vehicle) Long
        +findById(Long) Vehicle
        +findByLicensePlate(String) Vehicle
        +findCurrentlyParked() List~Vehicle~
        +findActiveVehicles() List~Vehicle~
        +findActiveBySpotId(String) Vehicle
        +update(Long, Vehicle)
        +updateExitTime(String, LocalDateTime)
        +delete(Long)
        +findAll() List~Vehicle~
    }

    class FineDAO {
        -DatabaseManager dbManager
        +save(Fine) Long
        +findById(Long) Fine
        +findUnpaidByLicensePlate(String) List~Fine~
        +findByLicensePlate(String) List~Fine~
        +findAllUnpaid() List~Fine~
        +update(Long, Fine)
        +markAsPaid(Long)
        +delete(Long)
        +findAll() List~Fine~
    }

    class PaymentDAO {
        -DatabaseManager dbManager
        +save(Payment) Long
        +findById(Long) Payment
        +findByLicensePlate(String) List~Payment~
        +findAll() List~Payment~
        +calculateTotalRevenue() double
        +update(Long, Payment)
        +delete(Long)
    }

    DatabaseManager <.. ParkingLotDAO : uses
    DatabaseManager <.. FloorDAO : uses
    DatabaseManager <.. ParkingSpotDAO : uses
    DatabaseManager <.. VehicleDAO : uses
    DatabaseManager <.. FineDAO : uses
    DatabaseManager <.. PaymentDAO : uses
    
    ParkingLotDAO --> FloorDAO : uses
    ParkingLotDAO --> ParkingSpotDAO : uses
    ParkingLotDAO --> VehicleDAO : uses
```

---

## 3. Class Diagram - View Layer

```mermaid
classDiagram
    class ThemeManager {
        <<utility>>
        +Color PRIMARY$
        +Color PRIMARY_DARK$
        +Color PRIMARY_LIGHT$
        +Color SUCCESS$
        +Color WARNING$
        +Color DANGER$
        +Color INFO$
        +Color BG_DARK$
        +Color BG_LIGHT$
        +Color BG_WHITE$
        +Color BG_CARD$
        +Color TEXT_PRIMARY$
        +Color TEXT_SECONDARY$
        +Color TEXT_LIGHT$
        +Font FONT_TITLE$
        +Font FONT_HEADER$
        +Font FONT_SUBHEADER$
        +Font FONT_BODY$
        +Font FONT_SMALL$
        +int SIDEBAR_WIDTH$
        +int HEADER_HEIGHT$
        +int STATUS_BAR_HEIGHT$
        +int CARD_PADDING$
        +int BORDER_RADIUS$
    }

    class ModernMainFrame {
        -HeaderPanel headerPanel
        -SideNavigationPanel sideNavPanel
        -JPanel contentPanel
        -CardLayout cardLayout
        -StatusBarPanel statusBarPanel
        -AdminPanel adminPanel
        -VehicleEntryPanel entryPanel
        -VehicleExitPanel exitPanel
        -ReportingPanel reportingPanel
        -ParkingLot parkingLot
        -DatabaseManager dbManager
        -FineDAO fineDAO
        +showPanel(String)
        +refreshAllPanels()
        +cleanup()
    }

    class HeaderPanel {
        -JLabel logoLabel
        -JLabel titleLabel
        -JLabel dateTimeLabel
        -Timer dateTimeTimer
        -updateDateTime()
        +stopTimer()
        +getHeaderBackground() Color
    }

    class SideNavigationPanel {
        -List~NavButton~ navButtons
        -NavButton activeButton
        -ActionListener navigationListener
        +setActiveButton(int)
        +setActiveButton(String)
        +getActiveButton() NavButton
    }

    class NavButton {
        -String text
        -String icon
        -boolean active
        -boolean hovered
        +setActive(boolean)
        +isActive() boolean
        +getCurrentBackgroundColor() Color
    }

    class StatusBarPanel {
        -ParkingLot parkingLot
        -JLabel connectionStatusLabel
        -JLabel vehicleCountLabel
        -JLabel occupancyRateLabel
        -Timer updateTimer
        -boolean connected
        +updateStatus()
        +setConnected()
        +setDisconnected()
        +getDisplayedVehicleCount() int
        +getDisplayedOccupancyRate() double
        +stopTimer()
    }

    class BasePanel {
        <<abstract>>
        #ParkingLot parkingLot
        +refreshData()*
        #createButton(String) StyledButton
        #createTextField(int) StyledTextField
        #createStyledTable() StyledTable
        #showError(String)
        #showSuccess(String)
        #showInfo(String)
        #showConfirm(String) boolean
        #validateNotEmpty(JTextField, String) boolean
        #validateLicensePlate(String) boolean
    }

    class AdminPanel {
        -DatabaseManager dbManager
        -FineDAO fineDAO
        +refreshData()
    }

    class VehicleEntryPanel {
        -DatabaseManager dbManager
        -FineDAO fineDAO
        +refreshData()
    }

    class VehicleExitPanel {
        -DatabaseManager dbManager
        -FineDAO fineDAO
        +refreshData()
    }

    class ReportingPanel {
        -DatabaseManager dbManager
        -FineDAO fineDAO
        +refreshData()
    }

    class StyledButton {
        -Color normalColor
        -Color hoverColor
        -Color pressedColor
        -Color textColor
        -int borderRadius
        -boolean isHovered
        -boolean isPressed
        +setBorderRadius(int)
        +setNormalColor(Color)
        +setTextColor(Color)
        #paintComponent(Graphics)
    }

    class StyledTextField {
        -Color borderColor
        -Color focusBorderColor
        -int borderRadius
        -boolean hasFocus
        +setBorderRadius(int)
        +setBorderColor(Color)
        +setFocusBorderColor(Color)
        #paintComponent(Graphics)
        #paintBorder(Graphics)
    }

    class StyledTable {
        -Color evenRowColor
        -Color oddRowColor
        -Color hoverColor
        -Color headerColor
        -int hoveredRow
        +setEvenRowColor(Color)
        +setOddRowColor(Color)
        +setHoverColor(Color)
        +prepareRenderer() Component
    }

    class StyledComboBox~E~ {
        +StyledComboBox()
        +StyledComboBox(E[])
        +StyledComboBox(ComboBoxModel~E~)
    }

    class DashboardCard {
        -String title
        -String value
        -Color accentColor
        -Icon icon
        -JLabel titleLabel
        -JLabel valueLabel
        -JLabel iconLabel
        +setValue(String)
        +setIcon(Icon)
        +setTitle(String)
        +setAccentColor(Color)
        #paintComponent(Graphics)
    }

    class StyledDialog {
        <<enumeration>> DialogType
        -DialogType dialogType
        -Color accentColor
        -boolean confirmed
        +showSuccess(Component, String)$
        +showError(Component, String)$
        +showWarning(Component, String)$
        +showInfo(Component, String)$
        +showConfirm(Component, String)$ boolean
        +getAccentColor() Color
        +isConfirmed() boolean
    }

    ModernMainFrame *-- HeaderPanel
    ModernMainFrame *-- SideNavigationPanel
    ModernMainFrame *-- StatusBarPanel
    ModernMainFrame *-- AdminPanel
    ModernMainFrame *-- VehicleEntryPanel
    ModernMainFrame *-- VehicleExitPanel
    ModernMainFrame *-- ReportingPanel

    SideNavigationPanel *-- NavButton

    BasePanel <|-- AdminPanel
    BasePanel <|-- VehicleEntryPanel
    BasePanel <|-- VehicleExitPanel
    BasePanel <|-- ReportingPanel

    BasePanel ..> StyledButton : creates
    BasePanel ..> StyledTextField : creates
    BasePanel ..> StyledTable : creates
    BasePanel ..> StyledDialog : uses

    AdminPanel ..> DashboardCard : uses
    AdminPanel ..> StyledTable : uses

    ThemeManager <.. ModernMainFrame : uses
    ThemeManager <.. StyledButton : uses
    ThemeManager <.. StyledTextField : uses
    ThemeManager <.. StyledTable : uses
    ThemeManager <.. DashboardCard : uses
    ThemeManager <.. HeaderPanel : uses
    ThemeManager <.. StatusBarPanel : uses
```

---

## 4. Class Diagram - Controller & Utility

```mermaid
classDiagram
    class VehicleEntryController {
        -ParkingLot parkingLot
        -DatabaseManager dbManager
        -VehicleDAO vehicleDAO
        -ParkingSpotDAO spotDAO
        +findAvailableSpots(VehicleType) List~ParkingSpot~
        +findAvailableSpotsForVehicle(Vehicle) List~ParkingSpot~
        +processEntry(String, VehicleType, boolean, String) EntryResult
        +generateTicket(String, LocalDateTime) String
    }

    class EntryResult {
        -Vehicle vehicle
        -ParkingSpot spot
        -ParkingSession session
        -String ticketNumber
        +getTicketDisplay() String
    }

    class VehicleExitController {
        -ParkingLot parkingLot
        -DatabaseManager dbManager
        -FineDAO fineDAO
        -PaymentDAO paymentDAO
        -ParkingSpotDAO spotDAO
        -VehicleDAO vehicleDAO
        -ParkingLotDAO parkingLotDAO
        -List~Fine~ unpaidFines
        +lookupVehicle(String) VehicleLookupResult
        +generatePaymentSummary(String, List~Fine~) PaymentSummary
        +processExit(String, double, PaymentMethod, List~Fine~) ExitResult
        +getUnpaidFines(String) List~Fine~
        +addUnpaidFine(Fine)
    }

    class VehicleLookupResult {
        -Vehicle vehicle
        -ParkingSpot spot
    }

    class PaymentSummary {
        -Vehicle vehicle
        -ParkingSpot spot
        -long durationHours
        -double parkingFee
        -List~Fine~ unpaidFines
        -double totalFines
        -double totalDue
        +getDisplayText() String
    }

    class ExitResult {
        -PaymentSummary summary
        -Payment payment
        -Receipt receipt
        -boolean paymentSufficient
        -double remainingBalance
    }

    class FeeCalculator {
        <<utility>>
        +calculateParkingFee(Vehicle, ParkingSpot, long)$ double
        +calculateTotalAmount(double, double)$ double
    }

    class FineManager {
        -FineDAO fineDAO
        +checkAndGenerateOverstayFine(Vehicle, FineCalculationStrategy) Fine
        +checkAndGenerateUnauthorizedReservedFine(String, SpotType, boolean, FineCalculationStrategy) Fine
        +generateFines(Vehicle, SpotType, boolean, FineCalculationStrategy) List~Fine~
        +saveFine(Fine) Long
        +getUnpaidFines(String) List~Fine~
        +markFinesAsPaid(List~Long~)
        +calculateTotalUnpaidFines(String) double
    }

    class PaymentProcessor {
        <<utility>>
        +validatePayment(double, double)$ boolean
        +calculateRemainingBalance(double, double)$ double
        +processPayment(String, double, double, double, PaymentMethod)$ Payment
        +generateReceipt(...)$ Receipt
        +isValidPaymentMethod(PaymentMethod)$ boolean
    }

    class Receipt {
        -String licensePlate
        -LocalDateTime entryTime
        -LocalDateTime exitTime
        -long durationHours
        -double parkingFee
        -double fineAmount
        -double totalAmount
        -double amountPaid
        -double remainingBalance
        -PaymentMethod paymentMethod
        -LocalDateTime paymentDate
        -String spotId
        +generateReceiptText() String
    }

    VehicleEntryController --> VehicleDAO
    VehicleEntryController --> ParkingSpotDAO
    VehicleEntryController ..> EntryResult : creates

    VehicleExitController --> VehicleDAO
    VehicleExitController --> FineDAO
    VehicleExitController --> PaymentDAO
    VehicleExitController --> ParkingSpotDAO
    VehicleExitController --> ParkingLotDAO
    VehicleExitController ..> VehicleLookupResult : creates
    VehicleExitController ..> PaymentSummary : creates
    VehicleExitController ..> ExitResult : creates
    VehicleExitController --> FeeCalculator : uses
    VehicleExitController --> PaymentProcessor : uses

    FineManager --> FineDAO
    PaymentProcessor ..> Receipt : creates

    ExitResult --> PaymentSummary
    ExitResult --> Payment
    ExitResult --> Receipt
```

---

## 5. Use Case Diagram

```mermaid
flowchart TB
    subgraph Actors
        Admin((Admin/Operator))
    end

    subgraph "Parking Lot Management System"
        UC1[View Dashboard]
        UC2[View Parking Statistics]
        UC3[View Reports]
        UC4[Process Vehicle Entry]
        UC5[Generate Parking Ticket]
        UC6[Process Vehicle Exit]
        UC7[Calculate Parking Fee]
        UC8[Process Payment]
        UC9[Check Unpaid Fines]
        UC10[Generate Receipt]
        UC11[View Parked Vehicles]
        UC12[View Unpaid Balances]
        UC13[Manage Fine Strategies]
    end

    Admin --> UC1
    Admin --> UC2
    Admin --> UC3
    Admin --> UC4
    Admin --> UC6
    Admin --> UC11
    Admin --> UC12
    Admin --> UC13

    UC4 --> UC5
    UC6 --> UC7
    UC6 --> UC9
    UC6 --> UC8
    UC8 --> UC10
```

---

## 6. Sequence Diagram - Vehicle Entry

```mermaid
sequenceDiagram
    participant User as Operator
    participant EntryPanel as VehicleEntryPanel
    participant Controller as VehicleEntryController
    participant VehicleDAO
    participant SpotDAO as ParkingSpotDAO
    participant DB as Database

    User->>EntryPanel: Enter license plate, vehicle type, handicapped status
    User->>EntryPanel: Select spot from available list
    User->>EntryPanel: Click "Process Entry"
    
    EntryPanel->>EntryPanel: Validate input
    
    alt Invalid Input
        EntryPanel->>User: Show error dialog
    else Valid Input
        EntryPanel->>Controller: processEntry(licensePlate, vehicleType, isHandicapped, spotId)
        
        Controller->>Controller: Check if vehicle already parked
        
        alt Vehicle Already Parked
            Controller-->>EntryPanel: Error: Vehicle already parked
            EntryPanel->>User: Show error dialog
        else New Entry
            Controller->>Controller: Find spot by ID
            Controller->>Controller: Validate spot availability
            Controller->>Controller: Check vehicle-spot compatibility
            
            alt Spot Not Available or Incompatible
                Controller-->>EntryPanel: Error message
                EntryPanel->>User: Show error dialog
            else Valid Entry
                Controller->>Controller: Create Vehicle object
                Controller->>Controller: Set entry time
                Controller->>Controller: Mark spot as occupied
                Controller->>Controller: Generate ticket number
                
                Controller->>VehicleDAO: save(vehicle)
                VehicleDAO->>DB: INSERT INTO vehicles
                DB-->>VehicleDAO: vehicle_id
                
                Controller->>SpotDAO: updateStatus(spotId, OCCUPIED)
                SpotDAO->>DB: UPDATE parking_spots
                
                Controller-->>EntryPanel: EntryResult with ticket
                EntryPanel->>User: Show success dialog with ticket details
            end
        end
    end
```

---

## 7. Sequence Diagram - Vehicle Exit & Payment

```mermaid
sequenceDiagram
    participant User as Operator
    participant ExitPanel as VehicleExitPanel
    participant Controller as VehicleExitController
    participant FeeCalc as FeeCalculator
    participant PayProc as PaymentProcessor
    participant VehicleDAO
    participant FineDAO
    participant PaymentDAO
    participant SpotDAO as ParkingSpotDAO
    participant LotDAO as ParkingLotDAO
    participant DB as Database

    User->>ExitPanel: Enter license plate
    User->>ExitPanel: Click "Search"
    
    ExitPanel->>Controller: lookupVehicle(licensePlate)
    Controller->>Controller: Search all spots for vehicle
    
    alt Vehicle Not Found
        Controller-->>ExitPanel: null
        ExitPanel->>User: Show error dialog
    else Vehicle Found
        Controller-->>ExitPanel: VehicleLookupResult
        
        ExitPanel->>FineDAO: findUnpaidByLicensePlate(licensePlate)
        FineDAO->>DB: SELECT unpaid fines
        DB-->>FineDAO: Fines list
        FineDAO-->>ExitPanel: List~Fine~
        
        ExitPanel->>Controller: generatePaymentSummary(licensePlate, fines)
        Controller->>Controller: Set exit time
        Controller->>Controller: Calculate duration (ceiling rounded)
        Controller->>FeeCalc: calculateParkingFee(vehicle, spot, duration)
        FeeCalc-->>Controller: Parking fee
        Controller->>Controller: Calculate total fines
        Controller->>FeeCalc: calculateTotalAmount(parkingFee, fines)
        FeeCalc-->>Controller: Total due
        Controller-->>ExitPanel: PaymentSummary
        
        ExitPanel->>User: Display payment summary
        
        User->>ExitPanel: Enter payment amount
        User->>ExitPanel: Select payment method
        User->>ExitPanel: Click "Process Payment"
        
        ExitPanel->>Controller: processExit(licensePlate, amount, method, fines)
        
        Controller->>PayProc: validatePayment(amount, totalDue)
        PayProc-->>Controller: isValid
        
        Controller->>PayProc: processPayment(...)
        PayProc-->>Controller: Payment object
        
        Controller->>PayProc: generateReceipt(...)
        PayProc-->>Controller: Receipt
        
        Controller->>Controller: Vacate spot
        
        Controller->>VehicleDAO: updateExitTime(licensePlate, exitTime)
        VehicleDAO->>DB: UPDATE vehicles SET exit_time
        
        Controller->>SpotDAO: updateStatus(spotId, AVAILABLE)
        SpotDAO->>DB: UPDATE parking_spots SET status
        
        Controller->>Controller: Update parking lot revenue
        Controller->>LotDAO: updateRevenue(totalRevenue)
        LotDAO->>DB: UPDATE parking_lots SET total_revenue
        
        Controller->>PaymentDAO: save(payment)
        PaymentDAO->>DB: INSERT INTO payments
        
        alt Payment Sufficient
            Controller->>FineDAO: markAsPaid(fineIds)
            FineDAO->>DB: UPDATE fines SET is_paid = TRUE
        else Payment Insufficient
            Controller->>FineDAO: save(unpaidBalanceFine)
            FineDAO->>DB: INSERT INTO fines (UNPAID_BALANCE)
        end
        
        Controller-->>ExitPanel: ExitResult with receipt
        ExitPanel->>User: Show receipt dialog
    end
```

---

## 8. Component Diagram

```mermaid
flowchart TB
    subgraph "Presentation Layer"
        UI[Modern GUI Components<br/>ModernMainFrame, Panels]
        Styled[Styled Components<br/>StyledButton, StyledTextField, etc.]
        Theme[Theme Manager]
    end

    subgraph "Controller Layer"
        EntryCtrl[Vehicle Entry Controller]
        ExitCtrl[Vehicle Exit Controller]
    end

    subgraph "Business Logic Layer"
        FeeCalc[Fee Calculator]
        FineManager[Fine Manager]
        PayProcessor[Payment Processor]
        Strategies[Fine Calculation Strategies<br/>Fixed, Hourly, Progressive]
    end

    subgraph "Data Access Layer"
        VehicleDAO[Vehicle DAO]
        SpotDAO[Parking Spot DAO]
        FineDAO[Fine DAO]
        PaymentDAO[Payment DAO]
        FloorDAO[Floor DAO]
        LotDAO[Parking Lot DAO]
        DBManager[Database Manager<br/>Connection Pool]
    end

    subgraph "Model Layer"
        Models[Domain Models<br/>ParkingLot, Vehicle, Fine, etc.]
    end

    subgraph "Database"
        MySQL[(MySQL Database<br/>parking_lot)]
    end

    UI --> EntryCtrl
    UI --> ExitCtrl
    UI --> Theme
    Styled --> Theme

    EntryCtrl --> VehicleDAO
    EntryCtrl --> SpotDAO
    EntryCtrl --> Models
    
    ExitCtrl --> VehicleDAO
    ExitCtrl --> FineDAO
    ExitCtrl --> PaymentDAO
    ExitCtrl --> SpotDAO
    ExitCtrl --> LotDAO
    ExitCtrl --> FeeCalc
    ExitCtrl --> PayProcessor
    ExitCtrl --> Models

    FineManager --> Strategies
    FineManager --> FineDAO
    FineManager --> Models
    
    PayProcessor --> PaymentDAO
    PayProcessor --> Models

    VehicleDAO --> DBManager
    SpotDAO --> DBManager
    FineDAO --> DBManager
    PaymentDAO --> DBManager
    FloorDAO --> DBManager
    LotDAO --> DBManager

    DBManager --> MySQL
    
    VehicleDAO --> Models
    SpotDAO --> Models
    FineDAO --> Models
    PaymentDAO --> Models
    FloorDAO --> Models
    LotDAO --> Models
```

---

## 9. Package Diagram

```mermaid
flowchart TB
    subgraph "com.university.parking"
        Main[ParkingApplication<br/>Main Entry Point]
        
        subgraph model["model"]
            M1[ParkingLot, Floor, ParkingSpot]
            M2[Vehicle, Fine, Payment]
            M3[ParkingSession]
            M4[Enums: VehicleType, SpotType,<br/>SpotStatus, FineType, PaymentMethod]
            M5[Strategy Pattern:<br/>FineCalculationStrategy,<br/>FixedFineStrategy,<br/>HourlyFineStrategy,<br/>ProgressiveFineStrategy,<br/>FineCalculationContext]
        end
        
        subgraph view["view"]
            V1[ModernMainFrame]
            V2[Panels: AdminPanel, VehicleEntryPanel,<br/>VehicleExitPanel, ReportingPanel]
            V3[Layout: HeaderPanel, SideNavigationPanel,<br/>StatusBarPanel]
            V4[Styled Components: StyledButton,<br/>StyledTextField, StyledTable,<br/>StyledComboBox, StyledDialog,<br/>DashboardCard]
            V5[ThemeManager]
            V6[BasePanel]
        end
        
        subgraph controller["controller"]
            C1[VehicleEntryController]
            C2[VehicleExitController]
            C3[Inner Classes: EntryResult,<br/>VehicleLookupResult,<br/>PaymentSummary, ExitResult]
        end
        
        subgraph dao["dao"]
            D1[DatabaseManager]
            D2[ParkingLotDAO, FloorDAO,<br/>ParkingSpotDAO]
            D3[VehicleDAO, FineDAO,<br/>PaymentDAO]
        end
        
        subgraph util["util"]
            U1[FeeCalculator]
            U2[FineManager]
            U3[PaymentProcessor]
            U4[Receipt]
        end
    end

    Main --> view
    view --> controller
    view --> model
    controller --> dao
    controller --> util
    controller --> model
    util --> dao
    util --> model
    dao --> model
```

---

## 10. Entity Relationship Diagram (Database)

```mermaid
erDiagram
    PARKING_LOT ||--o{ FLOOR : contains
    FLOOR ||--o{ PARKING_SPOT : contains
    PARKING_SPOT ||--o| VEHICLE : "currently parks"
    VEHICLE ||--o{ FINE : "has fines"
    VEHICLE ||--o{ PAYMENT : "has payments"

    PARKING_LOT {
        bigint id PK
        varchar name
        int total_floors
        decimal total_revenue
        varchar current_fine_strategy
    }

    FLOOR {
        bigint id PK
        bigint parking_lot_id FK
        int floor_number
        int total_spots
    }

    PARKING_SPOT {
        bigint id PK
        bigint floor_id FK
        varchar spot_id UK "Format: F1-R2-S3"
        varchar spot_type "COMPACT, REGULAR, HANDICAPPED, RESERVED"
        decimal hourly_rate
        varchar status "AVAILABLE, OCCUPIED"
        bigint current_vehicle_id FK
    }

    VEHICLE {
        bigint id PK
        varchar license_plate
        varchar vehicle_type "MOTORCYCLE, CAR, SUV_TRUCK, HANDICAPPED"
        boolean is_handicapped
        datetime entry_time
        datetime exit_time
        varchar assigned_spot_id
    }

    FINE {
        bigint id PK
        varchar license_plate
        varchar fine_type "OVERSTAY, UNAUTHORIZED_RESERVED, UNPAID_BALANCE"
        decimal amount
        datetime issued_date
        boolean is_paid
    }

    PAYMENT {
        bigint id PK
        varchar license_plate
        decimal parking_fee
        decimal fine_amount
        decimal total_amount
        varchar payment_method "CASH, CARD"
        datetime payment_date
    }
```

---

## Notes

### Key Design Patterns

1. **Strategy Pattern**: Used for fine calculation with three strategies (Fixed, Hourly, Progressive)
2. **DAO Pattern**: Separates data access logic from business logic
3. **MVC Architecture**: Clear separation between Model, View, and Controller layers
4. **Singleton-like**: ThemeManager uses static constants for centralized theme management
5. **Factory-like**: BasePanel provides factory methods for creating styled components

### Important Relationships

- **ParkingLot** contains multiple **Floors**, each containing multiple **ParkingSpots**
- **ParkingSpot** can have at most one **Vehicle** parked (0..1 relationship)
- **Vehicle** is identified by license plate and can have multiple **Fines** and **Payments**
- **Fines** persist across parking sessions (linked to license plate, not parking session)
- **DatabaseManager** uses connection pooling for efficient database access
- All styled components use **ThemeManager** for consistent appearance

### Database Design

- Uses MySQL with InnoDB engine for transaction support
- Spot IDs follow format: `F{floor}-R{row}-S{spot}` (e.g., "F1-R2-S3")
- Fines are linked to license plates (not vehicle IDs) to persist across sessions
- Connection pooling implemented with `BlockingQueue<Connection>`
- Database name: `parking_lot` (configurable for testing)

### GUI Architecture

- **ModernMainFrame** uses BorderLayout with CardLayout for content switching
- **SideNavigationPanel** contains **NavButton** instances for navigation
- **BasePanel** provides common functionality for all content panels
- All styled components extend Swing components with custom painting
- Theme colors and fonts centralized in **ThemeManager**

### View these diagrams

- GitHub: Automatically renders Mermaid diagrams
- VS Code: Install "Markdown Preview Mermaid Support" extension
- Other tools: Any Markdown viewer with Mermaid support
