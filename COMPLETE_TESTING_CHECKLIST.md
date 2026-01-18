# Complete Testing Checklist - Parking Management System

## 📋 Overview
This checklist covers all features and functions implemented in the University Parking Lot Management System. Test each item systematically to ensure the system works correctly.

---

## 1️⃣ Application Startup & Database

### 1.1 Initial Startup

**Steps to Test:**
1. Open Laragon and start MySQL
2. Navigate to project directory
3. Double-click `run-parking-system.bat` OR run: `java -jar parking-lot-management.jar`
4. Observe console output

**Expected Results:**
- [ ] Application starts without errors
- [ ] Console shows: "Starting University Parking Lot Management System..."
- [ ] Console shows: "Database 'parking_lot' created or already exists."
- [ ] Console shows: "All tables created successfully in MySQL database 'parking_lot'."
- [ ] Console shows: "VIEW 'vehicles_with_duration' created for real-time elapsed time tracking."
- [ ] Console shows: "Database initialized successfully"
- [ ] Console shows: "Loaded parking lot: University Parking Lot with 5 floors"
- [ ] GUI window opens successfully

### 1.2 Database Connection

**Steps to Test:**
1. Open phpMyAdmin (http://localhost/phpmyadmin)
2. Check if `parking_lot` database exists
3. Click on `parking_lot` database
4. Verify tables and VIEW

**Expected Results:**
- [ ] Laragon MySQL is running
- [ ] Database `parking_lot` exists
- [ ] 6 tables created: `parking_lots`, `floors`, `parking_spots`, `vehicles`, `fines`, `payments`
- [ ] VIEW `vehicles_with_duration` exists
- [ ] Connection pool initialized (10 connections)
- [ ] Timezone set to Asia/Singapore (UTC+8)
- [ ] No connection errors in console

**Verification Query:**
```sql
USE parking_lot;
SHOW TABLES;
SHOW FULL TABLES WHERE Table_type = 'VIEW';
SELECT * FROM parking_lots;
SELECT * FROM floors;
SELECT COUNT(*) FROM parking_spots; -- Should be 75 spots (5 floors × 3 rows × 5 spots)
```

### 1.3 Data Persistence

**Steps to Test:**
1. Park a vehicle (e.g., TEST001)
2. Close the application
3. Restart the application
4. Go to Dashboard
5. Check "Currently Parked Vehicles" table

**Expected Results:**
- [ ] Parking lot structure loaded from database
- [ ] Active parking sessions restored on startup
- [ ] Console shows: "Restored X active parking sessions" (where X = number of parked vehicles)
- [ ] Dashboard shows previously parked vehicle
- [ ] Spot status remains OCCUPIED

---

## 2️⃣ GUI & Navigation

### 2.1 Main Window

**Steps to Test:**
1. Launch the application
2. Observe the main window layout
3. Check header, sidebar, content area, and status bar

**Expected Results:**
- [ ] Modern main frame opens with sidebar navigation on the left
- [ ] Header panel at top displays "University Parking Lot Management System"
- [ ] Header shows live clock updating every second
- [ ] Status bar at bottom shows database connection status
- [ ] Status bar shows vehicle count and occupancy percentage
- [ ] Window is properly sized (1200×800) and centered on screen
- [ ] All components visible without scrolling

### 2.2 Sidebar Navigation

**Steps to Test:**
1. Click "Dashboard" button
2. Observe panel change and button highlight
3. Click "Vehicle Entry" button
4. Observe panel change and button highlight
5. Click "Vehicle Exit" button
6. Observe panel change and button highlight
7. Click "Reports" button
8. Observe panel change and button highlight
9. Hover mouse over each button

**Expected Results:**
- [ ] Dashboard button navigates to Dashboard panel
- [ ] Vehicle Entry button navigates to Entry panel
- [ ] Vehicle Exit button navigates to Exit panel
- [ ] Reports button navigates to Reports panel
- [ ] Active panel button is highlighted with darker blue background
- [ ] Hover effects work on buttons (lighter blue on hover)
- [ ] Navigation is instant (no lag)
- [ ] Only one button highlighted at a time

### 2.3 Theme & Styling

**Steps to Test:**
1. Navigate through all panels
2. Observe button styles
3. Click on text fields and observe focus
4. View tables with data
5. Trigger success and error dialogs

**Expected Results:**
- [ ] Buttons have rounded corners (border radius)
- [ ] Buttons have proper colors (blue primary, green success, red danger)
- [ ] Text fields have modern styling with borders
- [ ] Text fields show blue border when focused
- [ ] Tables have alternating row colors (white/light gray)
- [ ] Combo boxes display full text without truncation
- [ ] Success dialogs have green accent
- [ ] Error dialogs have red accent
- [ ] Warning dialogs have orange accent
- [ ] Info dialogs have blue accent
- [ ] Colors match theme consistently across all panels

---

## 3️⃣ Dashboard Panel

### 3.1 Dashboard Cards

**Steps to Test:**
1. Navigate to Dashboard panel
2. Observe the four dashboard cards at the top
3. Park a vehicle and click Refresh
4. Exit a vehicle and click Refresh
5. Observe card values update

**Expected Results:**
- [ ] Occupancy Rate card displays percentage (e.g., "13.33%")
- [ ] Occupancy Rate card has blue accent color
- [ ] Total Revenue card shows current revenue (e.g., "RM 150.00")
- [ ] Total Revenue card has green accent color
- [ ] Available Spots card shows count (e.g., "65")
- [ ] Available Spots card has orange accent color
- [ ] Parked Vehicles card shows count (e.g., "10")
- [ ] Parked Vehicles card has purple accent color
- [ ] Cards update when Refresh button clicked
- [ ] Values are accurate and match database

### 3.2 Floors Table

**Steps to Test:**
1. Scroll down to "Floors" section
2. Observe the floors table
3. Count the rows

**Expected Results:**
- [ ] Shows all 5 floors (Floor 1 through Floor 5)
- [ ] Each row displays floor number
- [ ] Each row displays total spots per floor (15 spots each)
- [ ] Table has proper formatting (alternating row colors)
- [ ] Data is accurate (5 floors × 15 spots = 75 total)

### 3.3 Parked Vehicles Table

**Steps to Test:**
1. Park 2-3 vehicles using Vehicle Entry panel
2. Return to Dashboard
3. Observe "Currently Parked Vehicles" table
4. Click Refresh button
5. Exit one vehicle
6. Return to Dashboard and click Refresh

**Expected Results:**
- [ ] Lists all currently parked vehicles
- [ ] Shows columns: License Plate, Vehicle Type, Spot ID, Entry Time
- [ ] Entry time formatted correctly (e.g., "2026-01-19 14:30:00")
- [ ] Updates when vehicles enter (after refresh)
- [ ] Updates when vehicles exit (after refresh)
- [ ] Refresh button reloads data from database
- [ ] Table shows "No vehicles currently parked" when empty

**Test Data:**
```sql
-- Verify parked vehicles
USE parking_lot;
SELECT license_plate, vehicle_type, assigned_spot_id, entry_time 
FROM vehicles 
WHERE exit_time IS NULL;
```

### 3.4 Unpaid Fines Table

**Steps to Test:**
1. Create an overstay fine (see Test 5B for steps)
2. Make a partial payment to create unpaid balance
3. Return to Dashboard
4. Observe "Unpaid Fines" table
5. Click Refresh button
6. Pay the fine completely
7. Return to Dashboard and click Refresh

**Expected Results:**
- [ ] Lists all unpaid fines
- [ ] Shows columns: License Plate, Fine Type, Amount, Issue Date
- [ ] Fine types display correctly (OVERSTAY, UNPAID_BALANCE)
- [ ] Amounts formatted as currency (e.g., "RM 50.00")
- [ ] Issue date formatted correctly
- [ ] Updates when fines are created (after refresh)
- [ ] Updates when fines are paid (after refresh)
- [ ] Refresh button reloads data from database
- [ ] Table shows "No unpaid fines" when empty

**Test Data:**
```sql
-- Verify unpaid fines
USE parking_lot;
SELECT license_plate, fine_type, amount, issued_date 
FROM fines 
WHERE is_paid = 0;
```

### 3.5 Dashboard Refresh

**Steps to Test:**
1. Open phpMyAdmin
2. Insert a vehicle directly via SQL:
```sql
USE parking_lot;
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('SQLTEST', 'CAR', FALSE, NOW(), 'F2-R1-S1');
```
3. Go to Dashboard in application
4. Click Refresh button
5. Observe tables update

**Expected Results:**
- [ ] Refresh button reloads all data from database
- [ ] Dashboard cards update with latest values
- [ ] Parked vehicles table shows SQL-inserted vehicle
- [ ] All counts and statistics accurate
- [ ] No need to restart application

---

## 4️⃣ Vehicle Entry

### 4.1 Input Validation
- [ ] License plate required (shows error if empty)
- [ ] License plate accepts alphanumeric + hyphens
- [ ] License plate converted to uppercase
- [ ] License plate length: 2-15 characters
- [ ] Vehicle type selection required
- [ ] Spot selection required (shows error if not selected)

### 4.2 Available Spots Display
- [ ] Available spots table shows compatible spots
- [ ] Filters by vehicle type:
  - Motorcycle → Only Compact spots
  - Car → Compact and Regular spots
  - SUV/Truck → Only Regular spots
  - Handicapped → All spot types
- [ ] Shows: Spot ID, Type, Hourly Rate
- [ ] Updates when vehicle type changes
- [ ] Refreshes after successful entry

### 4.3 Vehicle Entry Processing
- [ ] Prevents duplicate parking (same license plate already parked)
- [ ] Assigns vehicle to selected spot
- [ ] Generates ticket with format: T-{PLATE}-{TIMESTAMP}
- [ ] Displays ticket in ticket area
- [ ] Shows success message
- [ ] Saves vehicle to database
- [ ] Updates spot status to OCCUPIED
- [ ] Updates dashboard counts

### 4.4 Auto-Clear Form (NEW)
- [ ] License plate field cleared after successful entry
- [ ] Vehicle type reset to MOTORCYCLE
- [ ] Handicapped checkbox unchecked
- [ ] Spot selection cleared
- [ ] Ticket remains visible
- [ ] Available spots list refreshed

### 4.5 Ticket Display
- [ ] Shows ticket number
- [ ] Shows license plate
- [ ] Shows vehicle type
- [ ] Shows spot location (e.g., F1-R2-S3)
- [ ] Shows spot type and hourly rate
- [ ] Shows entry time

**Test Cases:**

**Test 4A: Normal Entry - Motorcycle**

**Steps:**
1. Navigate to Vehicle Entry panel
2. Enter license plate: `MOTO001`
3. Select vehicle type: `MOTORCYCLE`
4. Observe available spots table (should show only Compact spots)
5. Select first available Compact spot (e.g., F1-R1-S1)
6. Click "Process Entry" button
7. Observe success dialog
8. Observe ticket display area
9. Observe form fields

**Expected Results:**
- [ ] Success dialog appears: "Vehicle parked successfully!"
- [ ] Ticket generated with format: T-MOTO001-{timestamp}
- [ ] Ticket shows: License Plate, Vehicle Type, Spot Location, Spot Type, Hourly Rate, Entry Time
- [ ] License plate field cleared (empty)
- [ ] Vehicle type reset to MOTORCYCLE
- [ ] Handicapped checkbox unchecked
- [ ] Spot selection cleared
- [ ] Ticket remains visible
- [ ] Available spots list refreshed (selected spot removed)
- [ ] Dashboard shows 1 parked vehicle

**Test 4B: Normal Entry - Car**

**Steps:**
1. Enter license plate: `CAR001`
2. Select vehicle type: `CAR`
3. Observe available spots (should show Compact AND Regular spots)
4. Select a Regular spot (e.g., F1-R2-S1)
5. Click "Process Entry"

**Expected Results:**
- [ ] Entry successful
- [ ] Ticket generated
- [ ] Form auto-cleared
- [ ] Spot no longer in available list

**Test 4C: Normal Entry - SUV/Truck**

**Steps:**
1. Enter license plate: `TRUCK001`
2. Select vehicle type: `SUV_TRUCK`
3. Observe available spots (should show only Regular spots)
4. Select a Regular spot
5. Click "Process Entry"

**Expected Results:**
- [ ] Entry successful
- [ ] Only Regular spots shown (no Compact spots)
- [ ] Ticket generated correctly

**Test 4D: Handicapped Vehicle**

**Steps:**
1. Enter license plate: `HAND001`
2. Select vehicle type: `HANDICAPPED`
3. Check the "Handicapped" checkbox
4. Observe available spots (should show ALL spot types)
5. Select a Handicapped spot (e.g., F1-R3-S1)
6. Click "Process Entry"

**Expected Results:**
- [ ] Entry successful
- [ ] All spot types available for selection
- [ ] Handicapped spot assigned
- [ ] Hourly rate shown as RM 2/hour
- [ ] Ticket shows handicapped status

**Test 4E: Duplicate Prevention**

**Steps:**
1. Park vehicle with license plate: `DUP001`
2. Wait for form to clear
3. Enter same license plate: `DUP001`
4. Select vehicle type and spot
5. Click "Process Entry"

**Expected Results:**
- [ ] Error dialog appears
- [ ] Message: "Vehicle with license plate DUP001 is already parked"
- [ ] Entry rejected
- [ ] Form not cleared
- [ ] No duplicate entry in database

**Test 4F: Empty License Plate Validation**

**Steps:**
1. Leave license plate field empty
2. Select vehicle type and spot
3. Click "Process Entry"

**Expected Results:**
- [ ] Error dialog appears
- [ ] Message: "Please enter a license plate"
- [ ] Entry rejected
- [ ] Form remains as-is

**Test 4G: No Spot Selected Validation**

**Steps:**
1. Enter license plate: `NOSPOT`
2. Select vehicle type
3. Do NOT select a spot
4. Click "Process Entry"

**Expected Results:**
- [ ] Error dialog appears
- [ ] Message: "Please select a parking spot"
- [ ] Entry rejected

**Test 4H: Vehicle Type Change Updates Spots**

**Steps:**
1. Select vehicle type: `MOTORCYCLE`
2. Observe available spots (only Compact)
3. Change vehicle type to: `CAR`
4. Observe available spots update
5. Change vehicle type to: `SUV_TRUCK`
6. Observe available spots update again

**Expected Results:**
- [ ] MOTORCYCLE: Shows only Compact spots
- [ ] CAR: Shows Compact AND Regular spots
- [ ] SUV_TRUCK: Shows only Regular spots
- [ ] Spot list updates immediately on type change
- [ ] Previously selected spot cleared when type changes

**Test 4I: License Plate Uppercase Conversion**

**Steps:**
1. Enter license plate in lowercase: `abc123`
2. Click "Process Entry" (with valid type and spot)

**Expected Results:**
- [ ] License plate converted to uppercase: `ABC123`
- [ ] Ticket shows: ABC123
- [ ] Database stores: ABC123

**Test 4J: Multiple Sequential Entries**

**Steps:**
1. Park vehicle: `TEST001`
2. Observe form cleared
3. Park vehicle: `TEST002`
4. Observe form cleared
5. Park vehicle: `TEST003`
6. Go to Dashboard
7. Check parked vehicles count

**Expected Results:**
- [ ] All 3 vehicles parked successfully
- [ ] Form cleared after each entry
- [ ] Dashboard shows 3 parked vehicles
- [ ] All tickets generated correctly
- [ ] 3 spots now occupied

---

## 5️⃣ Vehicle Exit & Payment

### 5.1 Vehicle Lookup
- [ ] Enter license plate and click Lookup
- [ ] Finds vehicles in memory (normal entry)
- [ ] Finds vehicles in database only (SQL insert)
- [ ] Displays payment summary
- [ ] Shows: Entry time, Exit time, Hours parked
- [ ] Shows: Parking fee, Unpaid fines, Total due
- [ ] Pre-fills payment amount
- [ ] Enables Process Payment button

### 5.2 Parking Fee Calculation
- [ ] Uses elapsed_hours from database VIEW
- [ ] Ceiling rounded (e.g., 1.5 hours = 2 hours)
- [ ] Minimum 1 hour charge
- [ ] Correct hourly rate by spot type:
  - Compact: RM 2/hour
  - Regular: RM 5/hour
  - Handicapped: RM 2/hour
  - Reserved: RM 10/hour
- [ ] Handicapped vehicles in handicapped spots: RM 2/hour

### 5.3 Overstay Fine Generation (NEW)
- [ ] Automatically generated for vehicles > 24 hours
- [ ] Fine amount: RM 50.00 (FixedFineStrategy)
- [ ] Generated during vehicle lookup
- [ ] Saved to database
- [ ] Included in payment summary
- [ ] Not duplicated if already exists
- [ ] Uses isOverstay flag from VIEW

### 5.4 Payment Processing
- [ ] Accepts payment amount
- [ ] Validates payment is positive number
- [ ] Supports CASH and CARD payment methods
- [ ] Generates receipt
- [ ] Displays receipt in receipt area
- [ ] Shows success message
- [ ] Saves payment to database
- [ ] Updates vehicle exit_time
- [ ] Frees parking spot (status = AVAILABLE)
- [ ] Updates total revenue

### 5.5 Full Payment
- [ ] Marks all fines as paid
- [ ] Clears unpaid fines for license plate
- [ ] Receipt shows all charges
- [ ] No remaining balance

### 5.6 Partial Payment (NEW FIX)
- [ ] Accepts partial payment amount
- [ ] Calculates remaining balance
- [ ] Shows partial payment dialog
- [ ] Marks original fines as PAID
- [ ] Creates UNPAID_BALANCE fine for remaining amount
- [ ] Vehicle can exit with partial payment
- [ ] Spot freed after partial payment
- [ ] Unpaid balance persists to next visit

### 5.7 Unpaid Balance Persistence
- [ ] Unpaid balance appears on next vehicle entry
- [ ] Linked to license plate (not session)
- [ ] Accumulates across multiple visits
- [ ] Can be paid on subsequent exit
- [ ] Original fines NOT duplicated

**Test Cases:**

**Test 5A: Normal Exit (No Overstay) - 2 Hours**

**Preparation:**
```sql
-- Insert vehicle for 2 hours in Compact spot (RM 2/hour)
USE parking_lot;
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST001', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'F1-R1-S1');

-- Update spot status
UPDATE parking_spots SET status = 'OCCUPIED' WHERE spot_id = 'F1-R1-S1';
```

**Steps:**
1. Restart application (to load SQL-inserted vehicle)
2. Navigate to Vehicle Exit panel
3. Enter license plate: `TEST001`
4. Click "Lookup Vehicle" button
5. Observe payment summary display
6. Verify calculations:
   - Hours Parked: 2
   - Parking Fee: 2 hours × RM 2/hour = RM 4.00
   - Unpaid Fines: RM 0.00
   - TOTAL DUE: RM 4.00
7. Payment amount pre-filled with RM 4.00
8. Select payment method: CASH
9. Click "Process Payment" button
10. Observe success dialog
11. Observe receipt display
12. Go to Dashboard and click Refresh

**Expected Results:**
- [ ] Vehicle found successfully
- [ ] Payment summary shows correct values
- [ ] Hours parked: 2
- [ ] Parking fee: RM 4.00
- [ ] No fines
- [ ] Total due: RM 4.00
- [ ] Success dialog: "Payment processed successfully!"
- [ ] Receipt shows all details
- [ ] Spot F1-R1-S1 freed (status = AVAILABLE)
- [ ] Dashboard shows 0 parked vehicles
- [ ] Revenue increased by RM 4.00

**Verification:**
```sql
USE parking_lot;
-- Check vehicle exit time recorded
SELECT license_plate, entry_time, exit_time FROM vehicles WHERE license_plate = 'TEST001';

-- Check payment recorded
SELECT * FROM payments WHERE license_plate = 'TEST001';

-- Check spot freed
SELECT spot_id, status FROM parking_spots WHERE spot_id = 'F1-R1-S1';
```

**Test 5B: Overstay Fine - 26 Hours**

**Preparation:**
```sql
-- Insert vehicle for 26 hours in Compact spot (RM 2/hour)
USE parking_lot;
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('OVER001', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 26 HOUR), 'F1-R2-S1');

-- Update spot status
UPDATE parking_spots SET status = 'OCCUPIED' WHERE spot_id = 'F1-R2-S1';
```

**Steps:**
1. Restart application
2. Navigate to Vehicle Exit panel
3. Enter license plate: `OVER001`
4. Click "Lookup Vehicle"
5. Observe payment summary
6. Verify calculations:
   - Hours Parked: 26
   - Parking Fee: 26 hours × RM 2/hour = RM 52.00
   - Unpaid Fines: RM 50.00 (OVERSTAY)
   - TOTAL DUE: RM 102.00
7. Payment amount pre-filled with RM 102.00
8. Select payment method: CARD
9. Click "Process Payment"
10. Observe success dialog and receipt

**Expected Results:**
- [ ] Vehicle found successfully
- [ ] Hours parked: 26
- [ ] Parking fee: RM 52.00
- [ ] Overstay fine automatically generated: RM 50.00
- [ ] Total due: RM 102.00
- [ ] Payment successful
- [ ] Receipt shows parking fee + fine
- [ ] Spot freed
- [ ] Revenue increased by RM 102.00

**Verification:**
```sql
USE parking_lot;
-- Check overstay fine created and paid
SELECT * FROM fines WHERE license_plate = 'OVER001' AND fine_type = 'OVERSTAY';

-- Should show: is_paid = 1 (TRUE)
```

**Test 5C: Partial Payment - Creates Unpaid Balance**

**Preparation:**
```sql
-- Insert vehicle for 26 hours (Total due: RM 102.00)
USE parking_lot;
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('PARTIAL001', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 26 HOUR), 'F1-R3-S1');

UPDATE parking_spots SET status = 'OCCUPIED' WHERE spot_id = 'F1-R3-S1';
```

**Steps:**
1. Restart application
2. Navigate to Vehicle Exit
3. Enter license plate: `PARTIAL001`
4. Click "Lookup Vehicle"
5. Verify total due: RM 102.00 (RM 52 parking + RM 50 overstay)
6. Change payment amount to: `50.00` (partial payment)
7. Select payment method: CASH
8. Click "Process Payment"
9. Observe partial payment dialog
10. Read message: "Partial payment received. Remaining balance: RM 52.00"
11. Click OK
12. Observe receipt
13. Go to Dashboard, click Refresh
14. Check Unpaid Fines table

**Expected Results:**
- [ ] Partial payment accepted
- [ ] Dialog shows remaining balance: RM 52.00
- [ ] Vehicle exits successfully
- [ ] Spot freed
- [ ] Receipt shows: Amount Paid: RM 50.00, Remaining Balance: RM 52.00
- [ ] Dashboard shows unpaid fine: UNPAID_BALANCE, RM 52.00
- [ ] Original overstay fine marked as PAID

**Verification:**
```sql
USE parking_lot;
-- Check original overstay fine is PAID
SELECT * FROM fines WHERE license_plate = 'PARTIAL001' AND fine_type = 'OVERSTAY';
-- Should show: is_paid = 1

-- Check unpaid balance fine created
SELECT * FROM fines WHERE license_plate = 'PARTIAL001' AND fine_type = 'UNPAID_BALANCE';
-- Should show: amount = 52.00, is_paid = 0
```

**Test 5D: Unpaid Balance on Next Visit**

**Preparation:**
```sql
-- After Test 5C, insert same vehicle again for 2 hours
USE parking_lot;
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('PARTIAL001', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'F2-R1-S1');

UPDATE parking_spots SET status = 'OCCUPIED' WHERE spot_id = 'F2-R1-S1';
```

**Steps:**
1. Restart application
2. Navigate to Vehicle Exit
3. Enter license plate: `PARTIAL001`
4. Click "Lookup Vehicle"
5. Observe payment summary
6. Verify calculations:
   - Hours Parked: 2
   - Parking Fee: RM 4.00 (2 hours × RM 2/hour)
   - Unpaid Fines: RM 52.00 (previous unpaid balance)
   - TOTAL DUE: RM 56.00
7. Verify NO overstay fine (already paid)
8. Pay full amount: RM 56.00
9. Click "Process Payment"
10. Go to Dashboard, check Unpaid Fines table

**Expected Results:**
- [ ] Hours parked: 2
- [ ] Parking fee: RM 4.00
- [ ] Unpaid fines: RM 52.00 (UNPAID_BALANCE only)
- [ ] NO overstay fine (already paid in previous visit)
- [ ] Total due: RM 56.00
- [ ] Payment successful
- [ ] All fines cleared
- [ ] Dashboard shows 0 unpaid fines

**Verification:**
```sql
USE parking_lot;
-- All fines should be paid
SELECT * FROM fines WHERE license_plate = 'PARTIAL001';
-- All rows should show: is_paid = 1
```

**Test 5E: Vehicle Not Found**

**Steps:**
1. Navigate to Vehicle Exit
2. Enter license plate: `NOTEXIST`
3. Click "Lookup Vehicle"

**Expected Results:**
- [ ] Error dialog appears
- [ ] Message: "Vehicle with license plate NOTEXIST not found"
- [ ] Payment summary not displayed
- [ ] Process Payment button disabled

**Test 5F: Empty License Plate**

**Steps:**
1. Navigate to Vehicle Exit
2. Leave license plate field empty
3. Click "Lookup Vehicle"

**Expected Results:**
- [ ] Error dialog appears
- [ ] Message: "Please enter a license plate"
- [ ] No lookup performed

**Test 5G: Database Vehicle Lookup (SQL-Inserted)**

**Preparation:**
```sql
-- Insert vehicle directly via SQL (not through GUI)
USE parking_lot;
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('SQLVEHICLE', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 3 HOUR), 'F2-R2-S1');

UPDATE parking_spots SET status = 'OCCUPIED' WHERE spot_id = 'F2-R2-S1';
```

**Steps:**
1. Do NOT restart application
2. Navigate to Vehicle Exit
3. Enter license plate: `SQLVEHICLE`
4. Click "Lookup Vehicle"

**Expected Results:**
- [ ] Vehicle found successfully (from database, not memory)
- [ ] Payment summary displays correctly
- [ ] Hours parked: 3
- [ ] Parking fee calculated correctly
- [ ] Can process payment normally
- [ ] Spot synced to in-memory parking lot

**Test 5H: Ceiling Rounding - 1.5 Hours**

**Preparation:**
```sql
-- Insert vehicle for 1.5 hours
USE parking_lot;
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('ROUND001', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 90 MINUTE), 'F3-R1-S1');

UPDATE parking_spots SET status = 'OCCUPIED' WHERE spot_id = 'F3-R1-S1';
```

**Steps:**
1. Restart application
2. Navigate to Vehicle Exit
3. Enter license plate: `ROUND001`
4. Click "Lookup Vehicle"
5. Observe hours parked

**Expected Results:**
- [ ] Hours parked: 2 (ceiling rounded from 1.5)
- [ ] Parking fee: 2 hours × RM 2/hour = RM 4.00
- [ ] NOT charged for 1 hour (minimum is 1 hour, but actual is 1.5)

**Test 5I: Exactly 24 Hours (No Overstay)**

**Preparation:**
```sql
-- Insert vehicle for exactly 24 hours
USE parking_lot;
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('EXACT24', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 24 HOUR), 'F3-R2-S1');

UPDATE parking_spots SET status = 'OCCUPIED' WHERE spot_id = 'F3-R2-S1';
```

**Steps:**
1. Restart application
2. Navigate to Vehicle Exit
3. Enter license plate: `EXACT24`
4. Click "Lookup Vehicle"

**Expected Results:**
- [ ] Hours parked: 24
- [ ] Parking fee: RM 48.00
- [ ] NO overstay fine (threshold is > 24 hours, not >= 24)
- [ ] Total due: RM 48.00

**Test 5J: 24.01 Hours (Overstay Triggered)**

**Preparation:**
```sql
-- Insert vehicle for 24 hours and 1 minute
USE parking_lot;
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('OVER24', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 1441 MINUTE), 'F3-R3-S1');

UPDATE parking_spots SET status = 'OCCUPIED' WHERE spot_id = 'F3-R3-S1';
```

**Steps:**
1. Restart application
2. Navigate to Vehicle Exit
3. Enter license plate: `OVER24`
4. Click "Lookup Vehicle"

**Expected Results:**
- [ ] Hours parked: 25 (ceiling rounded from 24.01)
- [ ] Parking fee: RM 50.00
- [ ] Overstay fine: RM 50.00 (triggered)
- [ ] Total due: RM 100.00

---

## 6️⃣ Reports Panel

### 6.1 Revenue Report
- [ ] Select "Revenue Report" from dropdown
- [ ] Click Generate Report
- [ ] Shows all payments
- [ ] Displays: License Plate, Parking Fee, Fine Amount, Total Amount, Payment Method, Date
- [ ] Calculates total revenue
- [ ] Data matches database

### 6.2 Occupancy Report
- [ ] Select "Occupancy Report"
- [ ] Click Generate Report
- [ ] Shows occupancy statistics
- [ ] Displays: Total spots, Occupied spots, Available spots, Occupancy percentage
- [ ] Data is accurate

### 6.3 Current Vehicles Report
- [ ] Select "Current Vehicles"
- [ ] Click Generate Report
- [ ] Lists all parked vehicles
- [ ] Shows: License Plate, Vehicle Type, Spot ID, Entry Time
- [ ] Matches Dashboard parked vehicles

### 6.4 Unpaid Fines Report
- [ ] Select "Unpaid Fines"
- [ ] Click Generate Report
- [ ] Lists all unpaid fines
- [ ] Shows: License Plate, Fine Type, Amount, Issue Date
- [ ] Matches Dashboard unpaid fines

**Test Steps:**
1. Go to Reports panel
2. Test each report type
3. Verify data accuracy
4. Compare with database queries

---

## 7️⃣ Real-Time Elapsed Time Tracking

### 7.1 Database VIEW
- [ ] VIEW `vehicles_with_duration` exists
- [ ] Calculates elapsed_seconds, elapsed_minutes, elapsed_hours
- [ ] Uses timezone-aware calculation (UTC+8)
- [ ] Updates in real-time (no caching)
- [ ] Sets is_overstay flag when > 24 hours

### 7.2 Elapsed Time Display
- [ ] Dashboard shows accurate parking duration
- [ ] Vehicle Exit shows correct hours parked
- [ ] Duration updates automatically on refresh
- [ ] Ceiling rounded for fee calculation

**Test Query:**
```sql
USE parking_lot;

-- Check VIEW calculation
SELECT 
    license_plate,
    entry_time,
    elapsed_seconds,
    elapsed_minutes,
    elapsed_hours,
    is_overstay
FROM vehicles_with_duration
WHERE exit_time IS NULL;
```

---

## 8️⃣ Fine Management System

### 8.1 Fine Types
- [ ] OVERSTAY: RM 50.00 (> 24 hours)
- [ ] UNPAID_BALANCE: Variable (partial payment remainder)
- [ ] UNAUTHORIZED_RESERVED: RM 50.00 (if implemented)

### 8.2 Fine Generation
- [ ] Overstay fine generated automatically
- [ ] Unpaid balance fine created on partial payment
- [ ] Fines saved to database
- [ ] Fines linked to license plate

### 8.3 Fine Payment
- [ ] Fines included in payment summary
- [ ] Marked as paid after full payment
- [ ] Marked as paid after partial payment (original fines)
- [ ] Unpaid balance persists across sessions

### 8.4 Fine Persistence
- [ ] Fines stored in database
- [ ] Retrieved by license plate
- [ ] Persist across application restarts
- [ ] Accumulate over multiple visits

**Test Query:**
```sql
USE parking_lot;

-- Check all fines
SELECT 
    license_plate,
    fine_type,
    amount,
    CASE WHEN is_paid = 1 THEN 'PAID' ELSE 'UNPAID' END AS status,
    issued_date
FROM fines
ORDER BY issued_date DESC;
```

---

## 9️⃣ Data Persistence & Database

### 9.1 Vehicle Records
- [ ] Vehicles saved on entry
- [ ] Entry time recorded
- [ ] Exit time updated on exit
- [ ] Assigned spot ID stored
- [ ] Vehicle type and handicapped status saved

### 9.2 Parking Spot Status
- [ ] Spot status updated to OCCUPIED on entry
- [ ] Spot status updated to AVAILABLE on exit
- [ ] Status persists in database
- [ ] Restored on application restart

### 9.3 Payment Records
- [ ] All payments saved to database
- [ ] Parking fee, fine amount, total amount recorded
- [ ] Payment method and timestamp saved
- [ ] Linked to license plate

### 9.4 Revenue Tracking
- [ ] Total revenue updated on each payment
- [ ] Revenue persists in database
- [ ] Displayed on Dashboard
- [ ] Accurate across sessions

**Test Queries:**
```sql
USE parking_lot;

-- Verify data integrity
SELECT COUNT(*) FROM vehicles;
SELECT COUNT(*) FROM parking_spots WHERE status = 'OCCUPIED';
SELECT COUNT(*) FROM fines WHERE is_paid = 0;
SELECT SUM(total_amount) FROM payments;
SELECT total_revenue FROM parking_lots WHERE id = 1;
```

---

## 🔟 Edge Cases & Error Handling

### 10.1 Input Validation
- [ ] Empty license plate rejected
- [ ] Invalid characters rejected
- [ ] License plate too short/long rejected
- [ ] No vehicle type selected rejected
- [ ] No spot selected rejected
- [ ] Negative payment amount rejected
- [ ] Zero payment amount rejected

### 10.2 Business Logic Validation
- [ ] Duplicate parking prevented
- [ ] Vehicle not found error shown
- [ ] Insufficient payment handled gracefully
- [ ] Database errors caught and logged
- [ ] Connection errors handled

### 10.3 Boundary Conditions
- [ ] 0 hours parked → Charged 1 hour minimum
- [ ] Exactly 24 hours → No overstay fine
- [ ] 24.01 hours → Overstay fine generated
- [ ] Partial payment of RM 0.01 → Creates unpaid balance
- [ ] Full payment exactly → No unpaid balance

### 10.4 Concurrent Operations
- [ ] Multiple vehicles can enter simultaneously
- [ ] Dashboard updates reflect latest data
- [ ] Refresh button reloads from database
- [ ] No race conditions on spot assignment

---

## 1️⃣1️⃣ Performance & Reliability

### 11.1 Application Performance
- [ ] Startup time < 5 seconds
- [ ] Panel navigation is instant
- [ ] Database queries execute quickly
- [ ] No lag when entering/exiting vehicles
- [ ] Refresh operations complete in < 1 second

### 11.2 Database Performance
- [ ] Connection pool handles concurrent requests
- [ ] VIEW calculation is fast
- [ ] No connection leaks
- [ ] Transactions complete successfully
- [ ] No deadlocks or timeouts

### 11.3 Memory Management
- [ ] No memory leaks
- [ ] Application runs stable over time
- [ ] Database connections released properly
- [ ] No excessive memory usage

---

## 1️⃣2️⃣ Integration Testing

### 12.1 End-to-End Flow: Normal Parking
1. [ ] Start application
2. [ ] Enter vehicle (ABC123, CAR, F1-R1-S1)
3. [ ] Verify ticket generated
4. [ ] Check Dashboard shows 1 parked vehicle
5. [ ] Wait 2 hours (or backdate in database)
6. [ ] Exit vehicle
7. [ ] Verify fee: 2 hours × RM 2 = RM 4.00
8. [ ] Pay RM 4.00
9. [ ] Verify spot freed
10. [ ] Check Dashboard shows 0 parked vehicles
11. [ ] Verify payment in database

### 12.2 End-to-End Flow: Overstay with Partial Payment
1. [ ] Insert vehicle via SQL (26 hours ago)
2. [ ] Restart application
3. [ ] Exit vehicle
4. [ ] Verify total: RM 102.00 (RM 52 + RM 50 overstay)
5. [ ] Pay RM 50.00 (partial)
6. [ ] Verify partial payment dialog
7. [ ] Verify overstay fine marked as paid
8. [ ] Verify unpaid balance fine created (RM 52.00)
9. [ ] Insert same vehicle again (2 hours)
10. [ ] Exit vehicle
11. [ ] Verify total: RM 56.00 (RM 4 + RM 52 unpaid balance)
12. [ ] Verify NO overstay fine
13. [ ] Pay RM 56.00 (full)
14. [ ] Verify all fines cleared

### 12.3 End-to-End Flow: Multiple Vehicles
1. [ ] Enter 5 different vehicles
2. [ ] Verify all tickets generated
3. [ ] Check Dashboard shows 5 parked vehicles
4. [ ] Exit 2 vehicles
5. [ ] Verify Dashboard shows 3 parked vehicles
6. [ ] Enter 2 more vehicles
7. [ ] Verify Dashboard shows 5 parked vehicles
8. [ ] Exit all vehicles
9. [ ] Verify Dashboard shows 0 parked vehicles
10. [ ] Verify all spots available

---

## 1️⃣3️⃣ Database Consistency

### 13.1 Data Integrity
- [ ] No orphaned records
- [ ] Foreign keys enforced
- [ ] Spot IDs unique
- [ ] License plates normalized (uppercase)
- [ ] Timestamps accurate (UTC+8)

### 13.2 Referential Integrity
- [ ] Vehicles reference valid spot IDs
- [ ] Fines reference valid license plates
- [ ] Payments reference valid license plates
- [ ] Spots reference valid floor IDs

### 13.3 Transaction Consistency
- [ ] Entry transaction: Vehicle + Spot update atomic
- [ ] Exit transaction: Vehicle + Spot + Payment + Fines atomic
- [ ] No partial updates on error
- [ ] Rollback on failure

**Test Queries:**
```sql
USE parking_lot;

-- Check for orphaned vehicles
SELECT * FROM vehicles WHERE assigned_spot_id NOT IN (SELECT spot_id FROM parking_spots);

-- Check for invalid spot statuses
SELECT * FROM parking_spots WHERE status NOT IN ('AVAILABLE', 'OCCUPIED', 'RESERVED');

-- Check for negative amounts
SELECT * FROM fines WHERE amount < 0;
SELECT * FROM payments WHERE total_amount < 0;

-- Check for future timestamps
SELECT * FROM vehicles WHERE entry_time > NOW();
```

---

## ✅ Testing Summary

### Critical Features (Must Pass)
- [ ] Vehicle Entry works correctly
- [ ] Vehicle Exit works correctly
- [ ] Parking fee calculation accurate
- [ ] Overstay fine generation works
- [ ] Partial payment handling correct
- [ ] Database persistence reliable
- [ ] Dashboard displays accurate data

### Important Features (Should Pass)
- [ ] Auto-clear form after entry
- [ ] Real-time elapsed time tracking
- [ ] Unpaid balance persistence
- [ ] Reports generate correctly
- [ ] GUI navigation smooth
- [ ] Error handling graceful

### Nice-to-Have Features (Optional)
- [ ] Theme styling consistent
- [ ] Performance optimized
- [ ] Memory usage efficient
- [ ] Console output clean

---

## 📊 Test Results Template

| Test Category | Total Tests | Passed | Failed | Notes |
|---------------|-------------|--------|--------|-------|
| 1. Startup & Database | 9 | | | |
| 2. GUI & Navigation | 11 | | | |
| 3. Dashboard | 12 | | | |
| 4. Vehicle Entry | 20 | | | |
| 5. Vehicle Exit | 28 | | | |
| 6. Reports | 12 | | | |
| 7. Real-Time Tracking | 6 | | | |
| 8. Fine Management | 12 | | | |
| 9. Data Persistence | 12 | | | |
| 10. Edge Cases | 15 | | | |
| 11. Performance | 9 | | | |
| 12. Integration | 30 | | | |
| 13. Database Consistency | 12 | | | |
| **TOTAL** | **188** | | | |

---

## 🎯 Success Criteria

The system is considered **fully functional** if:
- ✅ All Critical Features pass (100%)
- ✅ At least 90% of Important Features pass
- ✅ At least 80% of all tests pass
- ✅ No data corruption or loss
- ✅ No critical bugs or crashes

---

## 📝 Notes

- Test in order (1 → 13) for best results
- Clean database between major test scenarios
- Document any failures with screenshots
- Report bugs with reproduction steps
- Retest after fixes applied

**Database Reset Command:**
```sql
USE parking_lot;
DELETE FROM payments;
DELETE FROM fines;
DELETE FROM vehicles;
UPDATE parking_spots SET status = 'AVAILABLE';
UPDATE parking_lots SET total_revenue = 0 WHERE id = 1;
```
