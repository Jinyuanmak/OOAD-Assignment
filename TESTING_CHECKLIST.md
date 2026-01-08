# Testing Checklist - Parking Lot Management System

This document lists all functions and features that should be tested to ensure the system works correctly.

---

## 1. Vehicle Entry

### 1.1 Basic Entry
- [ ] Enter license plate and park a vehicle successfully
- [ ] Ticket is generated with correct format (T-PLATE-TIMESTAMP)
- [ ] Entry time is recorded correctly
- [ ] Spot status changes from AVAILABLE to OCCUPIED
- [ ] Vehicle appears in Admin Dashboard "Currently Parked" list

### 1.2 Vehicle Type Restrictions
- [ ] Motorcycle can only park in Compact spots
- [ ] Car can park in Compact or Regular spots
- [ ] SUV/Truck can only park in Regular spots
- [ ] Handicapped vehicle can park in any spot type

### 1.3 Spot Type Selection
- [ ] Only available spots are shown in dropdown
- [ ] Compact spots show RM 2/hour rate
- [ ] Regular spots show RM 5/hour rate
- [ ] Handicapped spots show RM 2/hour rate
- [ ] Reserved spots show RM 10/hour rate

### 1.4 Validation
- [ ] Empty license plate shows error
- [ ] Invalid license plate format shows error
- [ ] Duplicate parking (same plate already parked) shows error
- [ ] No available spots shows appropriate message

---

## 2. Vehicle Exit

### 2.1 Vehicle Lookup
- [ ] Enter license plate and find parked vehicle
- [ ] Shows correct entry time
- [ ] Shows correct parking duration (ceiling rounded to hours)
- [ ] Shows correct parking fee based on spot type and duration

### 2.2 Fee Calculation
- [ ] Compact spot: RM 2/hour
- [ ] Regular spot: RM 5/hour
- [ ] Handicapped spot: RM 2/hour (for handicapped vehicles)
- [ ] Reserved spot: RM 10/hour
- [ ] Duration is ceiling rounded (e.g., 1.5 hours = 2 hours)
- [ ] Minimum charge is 1 hour

### 2.3 Payment Processing
- [ ] Full payment processes successfully
- [ ] Partial payment creates unpaid balance fine
- [ ] Cash payment method works
- [ ] Card payment method works
- [ ] Receipt is generated with all details

### 2.4 After Exit
- [ ] Spot status changes back to AVAILABLE
- [ ] Vehicle removed from "Currently Parked" list
- [ ] Payment recorded in database
- [ ] Revenue updated in Admin Dashboard

### 2.5 Validation
- [ ] Non-existent license plate shows error
- [ ] Vehicle not currently parked shows error
- [ ] Zero or negative payment amount shows error

---

## 3. Fine Management

### 3.1 Fine Generation
- [ ] Overstay fine (>24 hours) is generated correctly
- [ ] Unauthorized parking in Reserved spot generates fine
- [ ] Unpaid balance from partial payment creates fine

### 3.2 Fine Calculation Schemes
- [ ] Fixed Fine: RM 50 flat rate
- [ ] Progressive Fine: RM 50 (24h) + RM 100 (48h) + RM 150 (72h) + RM 200 (>72h)
- [ ] Hourly Fine: RM 20 per hour overstayed

### 3.3 Fine Display
- [ ] Unpaid fines show in Admin Dashboard
- [ ] Unpaid fines show during vehicle exit lookup
- [ ] Fine amount added to total payment due

### 3.4 Fine Payment
- [ ] Fines can be paid during vehicle exit
- [ ] Paid fines marked as paid in database
- [ ] Unpaid fines persist across sessions

---

## 4. Admin Dashboard

### 4.1 Statistics Display
- [ ] Total parking spots count is correct
- [ ] Occupied spots count is correct
- [ ] Available spots count is correct
- [ ] Occupancy percentage is correct
- [ ] Total revenue is correct

### 4.2 Floor Status
- [ ] All 5 floors are displayed
- [ ] Each floor shows correct spot counts
- [ ] Occupied/Available status per floor is accurate

### 4.3 Currently Parked Vehicles
- [ ] All parked vehicles are listed
- [ ] Shows license plate, vehicle type, spot, entry time
- [ ] List updates when vehicles enter/exit

### 4.4 Unpaid Fines
- [ ] All unpaid fines are listed
- [ ] Shows license plate, fine type, amount, date
- [ ] List updates when fines are paid

### 4.5 Fine Strategy Selection
- [ ] Can select Fixed Fine scheme
- [ ] Can select Progressive Fine scheme
- [ ] Can select Hourly Fine scheme
- [ ] Selected scheme applies to future entries only

### 4.6 Dashboard Cards (Modern UI)
- [ ] Occupancy card shows correct percentage
- [ ] Revenue card shows correct total
- [ ] Available spots card shows correct count
- [ ] Parked vehicles card shows correct count

---

## 5. Reports

### 5.1 Revenue Report
- [ ] Shows total revenue from all payments
- [ ] Breakdown by payment method (Cash/Card)
- [ ] Accurate calculation

### 5.2 Occupancy Report
- [ ] Shows current occupancy rate
- [ ] Breakdown by floor
- [ ] Breakdown by spot type

### 5.3 Current Vehicles Report
- [ ] Lists all currently parked vehicles
- [ ] Shows entry time and duration
- [ ] Shows assigned spot

### 5.4 Unpaid Fines Report
- [ ] Lists all unpaid fines
- [ ] Shows fine type and amount
- [ ] Shows issue date

---

## 6. Database Persistence

### 6.1 Data Saving
- [ ] Parked vehicles persist after app restart
- [ ] Payments persist after app restart
- [ ] Fines persist after app restart
- [ ] Parking lot configuration persists

### 6.2 Data Loading
- [ ] App loads existing data on startup
- [ ] Parked vehicles restored correctly
- [ ] Unpaid fines restored correctly
- [ ] Revenue total restored correctly

### 6.3 Database Connection
- [ ] App connects to MySQL successfully
- [ ] App works without database (memory-only mode)
- [ ] Connection errors handled gracefully

---

## 7. User Interface

### 7.1 Navigation
- [ ] Sidebar navigation works (Dashboard, Entry, Exit, Reports)
- [ ] Active button is highlighted
- [ ] Panel switching works correctly

### 7.2 Header Panel
- [ ] Application title displays correctly
- [ ] Date/time updates in real-time

### 7.3 Status Bar
- [ ] Shows database connection status
- [ ] Shows vehicle count
- [ ] Shows occupancy percentage
- [ ] Updates in real-time

### 7.4 Styled Components
- [ ] Buttons have hover effects
- [ ] Text fields have focus styling
- [ ] Tables have alternating row colors
- [ ] Dialogs show correct colors (success=green, error=red)

### 7.5 Input Validation
- [ ] Empty fields show error messages
- [ ] Invalid input shows appropriate error
- [ ] Error dialogs are clear and helpful

---

## 8. Edge Cases

### 8.1 Boundary Conditions
- [ ] Parking for exactly 1 hour
- [ ] Parking for exactly 24 hours (overstay boundary)
- [ ] Payment of exact amount due
- [ ] Payment of RM 0.01 less than due

### 8.2 Special Characters
- [ ] License plate with numbers only
- [ ] License plate with letters only
- [ ] License plate with mixed characters

### 8.3 Concurrent Operations
- [ ] Multiple vehicles can be parked simultaneously
- [ ] Multiple vehicles can exit simultaneously
- [ ] Data remains consistent

### 8.4 Error Recovery
- [ ] App handles database disconnection gracefully
- [ ] App recovers from invalid input
- [ ] No data corruption on errors

---

## 9. Pricing Verification

### 9.1 Spot Type Rates
| Spot Type | Expected Rate | Test Result |
|-----------|---------------|-------------|
| Compact | RM 2/hour | [ ] Pass |
| Regular | RM 5/hour | [ ] Pass |
| Handicapped | RM 2/hour | [ ] Pass |
| Reserved | RM 10/hour | [ ] Pass |

### 9.2 Duration Calculation Examples
| Actual Time | Expected Hours | Expected Fee (Regular) | Test Result |
|-------------|----------------|------------------------|-------------|
| 30 minutes | 1 hour | RM 5 | [ ] Pass |
| 1 hour | 1 hour | RM 5 | [ ] Pass |
| 1.5 hours | 2 hours | RM 10 | [ ] Pass |
| 2 hours | 2 hours | RM 10 | [ ] Pass |
| 3.1 hours | 4 hours | RM 20 | [ ] Pass |

---

## 10. Quick Test Scenarios

### Scenario 1: Basic Entry and Exit
1. Start application
2. Go to Vehicle Entry
3. Enter plate: TEST001, Type: Car, Spot: Regular
4. Click Park Vehicle
5. Go to Vehicle Exit
6. Enter plate: TEST001
7. Click Lookup
8. Verify fee calculation
9. Enter payment amount
10. Click Process Payment
11. Verify receipt and spot freed

### Scenario 2: Partial Payment
1. Park vehicle TEST002
2. Exit with payment less than total due
3. Verify unpaid balance fine created
4. Park TEST002 again
5. Exit and verify previous fine shows up

### Scenario 3: Handicapped Vehicle
1. Park handicapped vehicle in Regular spot
2. Verify rate is RM 2/hour (not RM 5/hour)
3. Exit and verify correct fee

### Scenario 4: Fine Strategy Change
1. Note current fine strategy in Admin
2. Change to different strategy
3. Park new vehicle for >24 hours
4. Verify new strategy applies to fine calculation

---

## Test Summary

| Category | Total Tests | Passed | Failed |
|----------|-------------|--------|--------|
| Vehicle Entry | 15 | | |
| Vehicle Exit | 14 | | |
| Fine Management | 11 | | |
| Admin Dashboard | 16 | | |
| Reports | 8 | | |
| Database | 9 | | |
| User Interface | 14 | | |
| Edge Cases | 10 | | |
| **TOTAL** | **97** | | |

---

## Notes

- Test with MySQL database running (Laragon started)
- Test each feature independently first, then test workflows
- Document any bugs found with steps to reproduce
- Verify data in phpMyAdmin after database operations
