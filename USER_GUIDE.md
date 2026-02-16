# User Guide - Parking Lot Management System

## Quick Start

1. Start MySQL (Laragon: click "Start All")
2. Run: `java -jar parking-lot-management.jar`
3. Database creates automatically on first run

---

## Main Features

### 1. Vehicle Entry
1. Click "Vehicle Entry"
2. Enter license plate (e.g., ABC1234)
3. Select vehicle type (Motorcycle/Car/SUV/Handicapped)
4. Check "Handicapped Card Holder" if applicable
5. Click "Find Available Spots"
6. Select a spot from the table
7. Click "Process Entry"

**Parking Rates:**
- Compact: RM 2/hr
- Regular: RM 5/hr
- Handicapped: FREE for card holders, RM 2/hr for card holders in other spots
- Reserved: RM 10/hr (requires reservation)

### 2. Vehicle Exit
1. Click "Vehicle Exit"
2. Enter license plate
3. Click "Calculate Charges"
4. Review payment summary (shows vehicle type, spot type, spot rate, entry/exit time, fees, fines)
5. Enter payment amount
6. Select payment method (Cash/Card/E-Wallet/etc.)
7. Click "Process Exit"
8. Receipt generated automatically (save as PDF optional)

**15-Minute Grace Period:**
- First 15 minutes FREE (non-reserved spots only)
- Does NOT apply if fines exist

### 3. Reservations
1. Click "Reservations"
2. Enter license plate
3. Select reserved spot from dropdown
4. Enter duration (default: 24 hours)
5. Pay prepaid amount (Duration × RM 10/hr)
6. Click "Create Reservation"

**Benefits:**
- Multiple entry/exit during reservation period
- No additional parking charges
- Guaranteed spot

**Rules:**
- Wrong vehicle: RM 100 fine
- Expired reservation: RM 100 fine + overstay fine
- Cancellation: No refund

### 4. Reports
1. Click "Reporting"
2. Select report type:
   - **Vehicle Report**: Currently parked vehicles
   - **Revenue Report**: Total revenue by floor/spot type
   - **Occupancy Report**: Spot utilization
   - **Fine Report**: All fines (paid/unpaid)
3. Click "Export" (TXT/PDF/CSV)

### 5. Admin Functions
1. Click "Admin" or "Dashboard"
2. View current fine strategy
3. Change strategy:
   - **Fixed**: RM 50 flat fine
   - **Hourly**: RM 20/hr overstay
   - **Progressive**: Day 2=RM 50, Day 3=RM 150, Day 4=RM 300, Day 5+=RM 500

**Note:** New strategy applies only to vehicles entering AFTER the change.

---

## Fine Types

| Fine                  | Amount             | When Issued                       |
|-----------------------|--------------------|-----------------------------------|
| Overstay              | Varies by strategy | Vehicle parked > 24 hours         |
| Unauthorized Reserved | RM 100             | Parking without reservation       |
| Expired Reservation   | RM 100             | Staying beyond reservation period |
| Unpaid Balance        | Variable           | Partial payment made              |

---

## Special Features

- **Handicapped Discount**: FREE in handicapped spots, RM 2/hr in other spots
- **Grace Period**: First 15 minutes FREE (non-reserved spots)
- **Prepaid Reservations**: Pay once, enter/exit multiple times
- **Partial Payment**: Remaining balance becomes unpaid fine
- **Real-Time Tracking**: Duration calculated in real-time (UTC+8)
- **Receipt Generation**: Text and PDF formats

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "Vehicle not found" | Check license plate spelling |
| "No available spots" | All spots occupied, wait or try different vehicle type |
| "Payment insufficient" | Enter amount ≥ total due, check for unpaid fines |
| "Reservation failed" | Click "Refresh", check if spot is RESERVED type |

---

## Contact

Email: chiushiaoying@student.mmu.edu.my
