# User Guide - Parking Lot Management System

## Getting Started

### Starting the Application
Double-click `run-parking-system.bat` or run:
```bash
java -jar target\parking-lot-management.jar
```

The application window will open with four main tabs.

---

## Tab 1: Vehicle Entry

### How to Park a Vehicle

1. **Enter License Plate**
   - Type the vehicle's license plate (e.g., "ABC123")
   - Letters and numbers only, 2-15 characters

2. **Select Vehicle Type**
   - Car
   - Motorcycle
   - Handicapped

3. **Select Spot Type**
   - **Compact** (RM 3/hour) - For cars and motorcycles
   - **Regular** (RM 5/hour) - For all vehicles
   - **Handicapped** (RM 2/hour) - For handicapped vehicles only
   - **Reserved** (RM 8/hour) - For authorized vehicles

4. **Click "Park Vehicle"**
   - System assigns an available spot
   - Displays ticket with spot location and entry time
   - Spot is marked as occupied

### Tips
- License plates are case-insensitive (ABC123 = abc123)
- System prevents duplicate parking (same plate can't park twice)
- Handicapped vehicles get special pricing in handicapped spots

---

## Tab 2: Vehicle Exit

### How to Process Exit and Payment

1. **Lookup Vehicle**
   - Enter license plate
   - Click "Lookup Vehicle"
   - System displays payment summary

2. **Review Payment Summary**
   - Hours parked (rounded up to nearest hour)
   - Parking fee
   - Any unpaid fines
   - **Total Due**

3. **Enter Payment**
   - Type payment amount in RM
   - Select payment method (Cash or Credit Card)
   - Click "Process Payment"

4. **Receipt**
   - Receipt displays in right panel
   - Shows all charges and payment details
   - Spot is released for next vehicle

### Partial Payments
If you pay less than the total due:
- System accepts the partial payment
- Creates an **Unpaid Balance Fine** for the remaining amount
- Fine appears when the same vehicle parks again
- Must be paid on next visit

**Example:**
- Total due: RM 5.00
- You pay: RM 2.00
- Unpaid balance fine: RM 3.00
- Next visit total: Parking fee + RM 3.00

---

## Tab 3: Admin Panel

### System Statistics
View real-time information:
- Total spots available
- Currently occupied spots
- Occupancy rate (%)
- Total revenue collected

### Fine Management
- View all unpaid fines
- See fine details (license plate, type, amount)
- Monitor fine status

### Refresh Data
Click "Refresh Data" to update all statistics.

---

## Tab 4: Reporting

### Available Reports

1. **Revenue Report**
   - Total revenue collected
   - Payment breakdown by method
   - Useful for financial tracking

2. **Occupancy Report**
   - Current occupancy rate
   - Available vs occupied spots
   - Spot utilization statistics

3. **Current Vehicles Report**
   - List of all parked vehicles
   - License plates and spot locations
   - Entry times and durations

4. **Unpaid Fines Report**
   - All outstanding fines
   - Fine types and amounts
   - License plates with unpaid fines

### Generating Reports
1. Click the report button you want
2. Report displays in the text area
3. Review the information

---

## Pricing Information

### Parking Rates

| Spot Type | Standard Rate | Handicapped Vehicle |
|-----------|--------------|---------------------|
| Compact | RM 3/hour | RM 3/hour |
| Regular | RM 5/hour | RM 5/hour |
| Handicapped | RM 5/hour | RM 2/hour |
| Reserved | RM 8/hour | RM 8/hour |

**Notes:**
- Minimum charge: 1 hour
- Duration rounded up (1.5 hours = 2 hours)
- Handicapped vehicles get RM 2/hour only in handicapped spots

### Fine Types

| Fine Type | Amount | When Applied |
|-----------|--------|--------------|
| Unauthorized Parking | RM 50 | Non-authorized vehicle in reserved spot |
| Overstay Fine | Progressive | Vehicle parked over 24 hours |
| Unpaid Balance | Variable | Insufficient payment on exit |

---

## Common Scenarios

### Scenario 1: Quick Parking (Under 1 Hour)
1. Park vehicle at 2:00 PM
2. Exit at 2:30 PM (30 minutes)
3. Charged for 1 hour (minimum)
4. Regular spot: RM 5.00

### Scenario 2: Multi-Hour Parking
1. Park vehicle at 9:00 AM
2. Exit at 2:15 PM (5 hours 15 minutes)
3. Charged for 6 hours (rounded up)
4. Regular spot: RM 30.00

### Scenario 3: Partial Payment
1. Park vehicle, total due: RM 10.00
2. Pay only RM 6.00
3. Unpaid balance fine: RM 4.00 created
4. Next visit: New parking fee + RM 4.00 fine

### Scenario 4: Handicapped Vehicle
1. Handicapped vehicle parks in handicapped spot
2. Parked for 3 hours
3. Rate: RM 2/hour
4. Total: RM 6.00

---

## Tips and Best Practices

### For Efficient Operation
- Always lookup vehicle before processing payment
- Review payment summary carefully
- Ensure payment amount covers total due
- Check receipt for accuracy

### For Customers
- Remember your license plate
- Note your parking spot location
- Keep your entry ticket
- Pay full amount to avoid fines

### For Administrators
- Regularly check unpaid fines
- Monitor occupancy rates
- Generate reports for record-keeping
- Refresh data periodically

---

## Troubleshooting

### "Vehicle not found"
- Check license plate spelling
- Ensure vehicle is currently parked
- License plates are case-insensitive

### "No available spots"
- All spots of selected type are occupied
- Try different spot type
- Wait for a vehicle to exit

### "Invalid license plate"
- Must be 2-15 characters
- Only letters, numbers, and hyphens
- No special characters

### Payment Issues
- Payment amount must be positive
- Enter numbers only (no RM symbol)
- Use decimal point for cents (e.g., 5.50)

---

## Data Persistence

All data is automatically saved to the database:
- Vehicle entry/exit records
- Payment transactions
- Fine records
- Spot status

Data persists across application restarts. The database file is `parking_lot_db.mv.db` in the project folder.

---

## Support

For technical issues or questions:
1. Check the README.md file
2. Review this user guide
3. Check console for error messages
4. Contact system administrator

---

**Version:** 1.0.0  
**Last Updated:** January 2026
