# Java Swing Usage in Parking Lot Management System

## Overview

**YES**, this project uses **Java Swing** extensively for the entire GUI. The complete user interface is built with Java Swing components.

---

## Main GUI Components

### 1. Main Application Window
**File:** `src/main/java/com/university/parking/view/MainFrame.java`

**Swing Components Used:**
- `JFrame` - Main application window
- `JTabbedPane` - Tabbed interface for different panels
- `JPanel` - Container panels
- `BorderLayout` - Layout manager
- `UIManager` - Look and feel configuration

**Code Example:**
```java
public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    
    private void initializeComponents() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Admin Dashboard", adminPanel);
        tabbedPane.addTab("Vehicle Entry", entryPanel);
        tabbedPane.addTab("Vehicle Exit", exitPanel);
        tabbedPane.addTab("Reports", reportingPanel);
    }
}
```

---

## GUI Panels (All Using Swing)

### 2. Vehicle Entry Panel
**File:** `src/main/java/com/university/parking/view/VehicleEntryPanel.java`

**Swing Components Used:**
- `JPanel` - Main panel container
- `JTextField` - License plate input
- `JComboBox<VehicleType>` - Vehicle type dropdown
- `JCheckBox` - Handicapped card holder checkbox
- `JTable` - Available spots display
- `DefaultTableModel` - Table data model
- `JScrollPane` - Scrollable table view
- `JTextArea` - Ticket display area
- `JButton` - "Park Vehicle" button
- `JLabel` - Field labels
- `GridBagLayout` - Layout manager
- `GridBagConstraints` - Layout constraints

**Features:**
- Input fields for vehicle information
- Dropdown for vehicle type selection
- Table showing available parking spots
- Button to process entry
- Text area displaying generated ticket

---

### 3. Vehicle Exit Panel
**File:** `src/main/java/com/university/parking/view/VehicleExitPanel.java`

**Swing Components Used:**
- `JPanel` - Main panel container
- `JTextField` - License plate lookup and payment amount input
- `JButton` - "Lookup Vehicle" and "Process Payment" buttons
- `JTextArea` - Fee summary display
- `JComboBox<PaymentMethod>` - Payment method dropdown
- `JLabel` - Field labels
- `JScrollPane` - Scrollable text areas
- `GridBagLayout` - Layout manager

**Features:**
- Vehicle lookup by license plate
- Fee calculation display
- Payment amount input
- Payment method selection
- Receipt generation

---

### 4. Admin Panel
**File:** `src/main/java/com/university/parking/view/AdminPanel.java`

**Swing Components Used:**
- `JPanel` - Main panel container
- `JTable` - Display parked vehicles and fines
- `DefaultTableModel` - Table data models
- `JScrollPane` - Scrollable tables
- `JButton` - Action buttons (refresh, view, etc.)
- `JLabel` - Statistics labels
- `JComboBox<FineCalculationScheme>` - Fine scheme selector
- `BorderLayout` - Layout manager
- `FlowLayout` - Button panel layout

**Features:**
- System statistics display
- Currently parked vehicles table
- Unpaid fines table
- Fine scheme selection
- Revenue tracking

---

### 5. Reporting Panel
**File:** `src/main/java/com/university/parking/view/ReportingPanel.java`

**Swing Components Used:**
- `JPanel` - Main panel container
- `JTable` - Report data display
- `DefaultTableModel` - Table data model
- `JScrollPane` - Scrollable table
- `JButton` - Report generation buttons
- `JComboBox<String>` - Report type selector
- `JLabel` - Report headers
- `GridBagLayout` - Layout manager

**Features:**
- Revenue reports
- Occupancy reports
- Vehicle reports
- Fine reports
- Export functionality

---

### 6. Base Panel (Abstract)
**File:** `src/main/java/com/university/parking/view/BasePanel.java`

**Swing Components Used:**
- `JPanel` - Base class for all panels
- `JOptionPane` - Message dialogs (success, error, warning)
- `BorderLayout` - Default layout

**Provides:**
- Common dialog methods (`showSuccess`, `showError`, `showWarning`)
- Consistent panel structure
- Shared functionality for all panels

---

## Swing Utilities

### 7. Input Validator
**File:** `src/main/java/com/university/parking/view/InputValidator.java`

**Purpose:** Validates user input from Swing components
- License plate validation
- Payment amount validation
- Empty field checks

### 8. Event Handler
**File:** `src/main/java/com/university/parking/view/EventHandler.java`

**Purpose:** Handles Swing component events
- Button click events
- Table selection events
- Combo box changes

---

## Complete List of Swing Components Used

| Swing Component | Usage | Files |
|----------------|-------|-------|
| `JFrame` | Main window | MainFrame.java |
| `JPanel` | Container panels | All panel files |
| `JTabbedPane` | Tab interface | MainFrame.java |
| `JTable` | Data display | All panel files |
| `DefaultTableModel` | Table data | All panel files |
| `JTextField` | Text input | Entry/Exit panels |
| `JTextArea` | Multi-line text | Entry/Exit panels |
| `JButton` | Action buttons | All panel files |
| `JLabel` | Text labels | All panel files |
| `JComboBox` | Dropdown lists | All panel files |
| `JCheckBox` | Checkboxes | VehicleEntryPanel |
| `JScrollPane` | Scrollable views | All panel files |
| `JOptionPane` | Dialog boxes | BasePanel.java |
| `BorderLayout` | Layout manager | All panel files |
| `GridBagLayout` | Layout manager | Entry/Exit panels |
| `FlowLayout` | Layout manager | AdminPanel.java |
| `UIManager` | Look and feel | MainFrame.java |

---

## GUI Architecture

```
MainFrame (JFrame)
├── JTabbedPane
│   ├── AdminPanel (JPanel)
│   │   ├── Statistics Section
│   │   ├── Parked Vehicles Table (JTable)
│   │   ├── Fines Table (JTable)
│   │   └── Fine Scheme Selector (JComboBox)
│   │
│   ├── VehicleEntryPanel (JPanel)
│   │   ├── Input Form (JTextField, JComboBox, JCheckBox)
│   │   ├── Available Spots Table (JTable)
│   │   ├── Ticket Display (JTextArea)
│   │   └── Park Button (JButton)
│   │
│   ├── VehicleExitPanel (JPanel)
│   │   ├── Lookup Form (JTextField, JButton)
│   │   ├── Fee Summary (JTextArea)
│   │   ├── Payment Form (JTextField, JComboBox)
│   │   └── Process Payment Button (JButton)
│   │
│   └── ReportingPanel (JPanel)
│       ├── Report Type Selector (JComboBox)
│       ├── Report Table (JTable)
│       └── Generate Button (JButton)
│
└── Status Bar (JPanel with JLabel)
```

---

## How to See the Swing GUI

### Run the Application:
```
Double-click: run-parking-system.bat
```

### What You'll See:
1. **Main Window** - JFrame with title "University Parking Lot Management System"
2. **Tabs** - JTabbedPane with 4 tabs (Admin, Entry, Exit, Reports)
3. **Forms** - JTextField, JComboBox, JCheckBox for input
4. **Tables** - JTable showing data (spots, vehicles, fines)
5. **Buttons** - JButton for actions (Park, Exit, Generate Report)
6. **Dialogs** - JOptionPane for success/error messages

---

## Key Swing Features Implemented

### 1. Event Handling
- Button click listeners (`ActionListener`)
- Table selection listeners (`ListSelectionListener`)
- Combo box change listeners

### 2. Layout Management
- `BorderLayout` - Main panel structure
- `GridBagLayout` - Complex form layouts
- `FlowLayout` - Button panels

### 3. Data Display
- `JTable` with `DefaultTableModel` - Dynamic data tables
- `JTextArea` - Multi-line text display
- `JLabel` - Static text and statistics

### 4. User Input
- `JTextField` - Single-line text input
- `JComboBox` - Dropdown selections
- `JCheckBox` - Boolean options

### 5. Dialogs
- `JOptionPane.showMessageDialog()` - Success messages
- `JOptionPane.showMessageDialog()` with ERROR_MESSAGE - Error alerts
- `JOptionPane.showMessageDialog()` with WARNING_MESSAGE - Warnings

---

## Screenshots Description

When you run the application, you'll see:

### Admin Dashboard Tab
- Statistics panel showing total spots, occupied spots, revenue
- Table of currently parked vehicles
- Table of unpaid fines
- Fine scheme selector dropdown

### Vehicle Entry Tab
- License plate input field
- Vehicle type dropdown (Car, Motorcycle, SUV, Handicapped)
- Handicapped card checkbox
- Table showing available parking spots
- "Park Vehicle" button
- Ticket display area

### Vehicle Exit Tab
- License plate lookup field
- "Lookup Vehicle" button
- Fee summary text area
- Payment amount input
- Payment method dropdown (Cash, Credit Card)
- "Process Payment" button

### Reports Tab
- Report type selector
- Data table showing report results
- "Generate Report" button

---

## Summary

**Java Swing is used for 100% of the GUI.**

Every visual element you see in the application is a Swing component:
- All windows, panels, and tabs
- All input fields and buttons
- All tables and text displays
- All dialogs and messages

The entire user interface is built with Java Swing, following the MVC (Model-View-Controller) design pattern where the `view` package contains all Swing GUI code.

---

## File Locations

All Swing GUI code is in:
```
src/main/java/com/university/parking/view/
├── MainFrame.java          (Main window - JFrame)
├── AdminPanel.java         (Admin tab - JPanel)
├── VehicleEntryPanel.java  (Entry tab - JPanel)
├── VehicleExitPanel.java   (Exit tab - JPanel)
├── ReportingPanel.java     (Reports tab - JPanel)
├── BasePanel.java          (Base class for panels)
├── InputValidator.java     (Input validation)
└── EventHandler.java       (Event handling)
```

**Total:** 8 Java Swing GUI files with ~2,000+ lines of Swing code
