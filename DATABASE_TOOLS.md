# Database Tools - Parking Lot Management System

## Overview

**Database**: MySQL 8.0 with JDBC  
**Tool**: Laragon (includes phpMyAdmin)  
**Connection**: localhost:3306, user=root, password=(empty)

---

## Quick Setup

### Option 1: Automatic (Recommended)
1. Start Laragon → Click "Start All"
2. Run application → Database creates automatically

### Option 2: Manual
1. Start Laragon
2. Open phpMyAdmin (right-click Laragon → MySQL → phpMyAdmin)
3. Run SQL script: `database/database_setup.sql`

---

## Database Schema

**Database Name**: `parking_lot`

### Tables (7 Tables)

| Table             | Purpose               | Key Columns                                                           |
|-------------------|-----------------------|-----------------------------------------------------------------------|
| **parking_lots**  | Parking lot config    | name, total_floors, total_revenue, current_fine_strategy              |
| **floors**        | Floor info            | floor_number, total_spots                                             |
| **parking_spots** | Spot details          | spot_id, spot_type, hourly_rate, status                               |
| **vehicles**      | Vehicle records       | license_plate, vehicle_type, is_handicapped, entry_time, exit_time    |
| **fines**         | Fine records          | license_plate, fine_type, amount, is_paid                             |
| **payments**      | Payment transactions  | license_plate, parking_fee, fine_amount, payment_method               |
| **reservations**  | Reservations          | license_plate, spot_id, start_time, end_time, prepaid_amount          |

### View (1 View)

**vehicles_with_duration**: Real-time elapsed time calculation
- Calculates: `elapsed_seconds`, `elapsed_minutes`, `elapsed_hours`, `is_overstay`
- Updates automatically on every query
- Timezone-aware (UTC+8)

---

## Connection Details

**JDBC URL:**
```
jdbc:mysql://localhost:3306/parking_lot?useSSL=false&serverTimezone=Asia/Singapore&allowPublicKeyRetrieval=true
```

**Maven Dependency:**
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
</dependency>
```

---

## Data Access Objects (DAO)

Location: `src/main/java/com/university/parking/dao/`

| DAO Class         | Purpose                                           |
|-------------------|---------------------------------------------------|
| DatabaseManager   | Connection pooling, schema initialization         |
| VehicleDAO        | Vehicle CRUD, uses vehicles_with_duration VIEW    |
| ParkingSpotDAO    | Parking spot operations                           |
| FineDAO           | Fine management (create, find, mark paid)         |
| PaymentDAO        | Payment records                                   |
| ParkingLotDAO     | Config and revenue updates                        |
| FloorDAO          | Floor management                                  |
| ReservationDAO    | Reservation CRUD                                  |

---

## Common SQL Queries

### View Parked Vehicles
```sql
SELECT license_plate, vehicle_type, assigned_spot_id, 
       entry_time, elapsed_hours, is_overstay
FROM vehicles_with_duration
WHERE exit_time IS NULL;
```

### View Unpaid Fines
```sql
SELECT license_plate, fine_type, amount, issued_date
FROM fines
WHERE is_paid = FALSE;
```

### View Total Revenue
```sql
SELECT total_revenue, current_fine_strategy
FROM parking_lots WHERE id = 1;
```

### View Active Reservations
```sql
SELECT license_plate, spot_id, start_time, end_time, prepaid_amount
FROM reservations
WHERE is_active = TRUE AND end_time > NOW();
```

---

## Testing with SQL

### Insert Test Vehicle (2 hours ago)
```sql
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST001', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'F1-R1-S1');
```

### Insert Overstay Vehicle (26 hours ago)
```sql
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('OVERSTAY001', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 26 HOUR), 'F1-R2-S1');
```

### Reset All Data
```sql
DELETE FROM payments;
DELETE FROM fines;
DELETE FROM reservations;
DELETE FROM vehicles;
UPDATE parking_spots SET status = 'AVAILABLE', current_vehicle_id = NULL;
UPDATE parking_lots SET total_revenue = 0 WHERE id = 1;
```

---

## Using Laragon

### Start MySQL
1. Open Laragon
2. Click "Start All"
3. MySQL runs on localhost:3306

### Access phpMyAdmin
- Right-click Laragon → MySQL → phpMyAdmin
- Or open: http://localhost/phpmyadmin
- Login: root / (empty password)

### View Database
1. Click "parking_lot" database
2. Browse tables
3. Run SQL queries in "SQL" tab

---

## Troubleshooting

| Issue                         | Solution                                              |
|-------------------------------|-------------------------------------------------------|
| "Communications link failure" | Start Laragon, click "Start All"                      |
| "Access denied"               | Check password (default: empty)                       |
| "Unknown database"            | Run application once or execute database_setup.sql    |
| "Table doesn't exist"         | Run application once or execute database_setup.sql    |
| "Incorrect datetime value"    | Check timezone: `SELECT @@session.time_zone;`         |

---

## Key Features

- **Connection Pooling**: 10 reusable connections
- **Real-Time Tracking**: vehicles_with_duration VIEW
- **Timezone**: Asia/Singapore (UTC+8)
- **Auto-Setup**: Database and tables created automatically
- **JDBC**: Standard Java database connectivity

---

## Files

- Setup Script: `database/database_setup.sql`
- Test Data: `database/test_fine_strategies.sql`

---

**Contact**: chiushiaoying@student.mmu.edu.my
