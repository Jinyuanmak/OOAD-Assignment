-- ============================================
-- Test Fine Calculation Strategies
-- ============================================
-- This script inserts test vehicles to trigger different fine strategies
-- Run these queries in phpMyAdmin or MySQL Workbench
-- ============================================

USE parking_lot;

-- ============================================
-- IMPORTANT: Set your timezone first!
-- ============================================
SET SESSION time_zone = '+08:00';

-- ============================================
-- CLEANUP: Remove existing test data (optional)
-- ============================================
-- DELETE FROM fines WHERE license_plate LIKE 'TEST%';
-- DELETE FROM vehicles WHERE license_plate LIKE 'TEST%';

-- ============================================
-- TEST 1: FIXED FINE STRATEGY (RM 50 flat)
-- ============================================
-- Set parking lot to use FIXED strategy
UPDATE parking_lots SET current_fine_strategy = 'FIXED' WHERE id = 1;

-- Insert vehicle that overstayed (26 hours ago)
-- This will trigger OVERSTAY fine with FIXED strategy = RM 50
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-FIXED', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 26 HOUR), 'F1-R1-S1');

-- Verify the vehicle is marked as overstay
SELECT 
    license_plate,
    entry_time,
    elapsed_hours,
    is_overstay,
    'Should be TRUE (> 24 hours)' as expected_overstay
FROM vehicles_with_duration
WHERE license_plate = 'TEST-FIXED';

-- Expected Result:
-- When you process exit for 'TEST-FIXED', it should generate:
-- - OVERSTAY fine: RM 50.00 (FIXED strategy)
-- - Parking fee: 26 hours × hourly rate (depends on spot type)

-- ============================================
-- TEST 2: HOURLY FINE STRATEGY (RM 20/hour)
-- ============================================
-- Set parking lot to use HOURLY strategy
UPDATE parking_lots SET current_fine_strategy = 'HOURLY' WHERE id = 1;

-- Insert vehicle that overstayed by 3 hours (27 hours ago)
-- This will trigger OVERSTAY fine with HOURLY strategy = 3 hours × RM 20 = RM 60
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-HOURLY', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 27 HOUR), 'F1-R1-S2');

-- Verify the vehicle is marked as overstay
SELECT 
    license_plate,
    entry_time,
    elapsed_hours,
    is_overstay,
    (elapsed_hours - 24) as overstay_hours,
    'Should be 3 hours overstay' as expected
FROM vehicles_with_duration
WHERE license_plate = 'TEST-HOURLY';

-- Expected Result:
-- When you process exit for 'TEST-HOURLY', it should generate:
-- - OVERSTAY fine: (27 - 24) × RM 20 = 3 × RM 20 = RM 60.00
-- - Parking fee: 27 hours × hourly rate

-- ============================================
-- TEST 3: PROGRESSIVE FINE STRATEGY (Tiered by Days)
-- ============================================
-- Progressive Fine Tiers (based on TOTAL parking hours):
-- - 0-24 hours (Day 1): No fine (normal parking)
-- - 24-48 hours (Day 2): RM 50 fine (1st day overstay)
-- - 48-72 hours (Day 3): Additional RM 100 (Total: RM 150) (2nd day overstay)
-- - 72-96 hours (Day 4): Additional RM 150 (Total: RM 300) (3rd day overstay)
-- - 96+ hours (Day 5+): Additional RM 200 (Total: RM 500) (4th day overstay)
-- ============================================

-- Set parking lot to use PROGRESSIVE strategy
UPDATE parking_lots SET current_fine_strategy = 'PROGRESSIVE' WHERE id = 1;

-- Test Case 3A: 25 hours total (Day 2 - 1 hour into overstay)
-- Expected fine: RM 50 (Day 2 fine)
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-PROG-25H', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 25 HOUR), 'F1-R1-S1');

-- Test Case 3B: 50 hours total (Day 3 - 2 hours into 2nd day overstay)
-- Expected fine: RM 50 + RM 100 = RM 150 (Day 2 + Day 3)
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-PROG-50H', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 50 HOUR), 'F1-R1-S2');

-- Test Case 3C: 75 hours total (Day 4 - 3 hours into 3rd day overstay)
-- Expected fine: RM 50 + RM 100 + RM 150 = RM 300 (Day 2 + Day 3 + Day 4)
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-PROG-75H', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 75 HOUR), 'F1-R1-S3');

-- Test Case 3D: 100 hours total (Day 5 - 4 hours into 4th day overstay)
-- Expected fine: RM 50 + RM 100 + RM 150 + RM 200 = RM 500 (All days)
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-PROG-100H', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 100 HOUR), 'F1-R1-S4');

-- Verify all progressive test vehicles
SELECT 
    license_plate,
    entry_time,
    elapsed_hours,
    is_overstay,
    (elapsed_hours - 24) as overstay_hours,
    CASE 
        WHEN elapsed_hours < 24 THEN 'No fine (Day 1 - normal parking)'
        WHEN elapsed_hours < 48 THEN 'RM 50 (Day 2)'
        WHEN elapsed_hours < 72 THEN 'RM 150 (Day 2+3)'
        WHEN elapsed_hours < 96 THEN 'RM 300 (Day 2+3+4)'
        ELSE 'RM 500 (Day 2+3+4+5)'
    END as expected_fine
FROM vehicles_with_duration
WHERE license_plate LIKE 'TEST-PROG%'
ORDER BY elapsed_hours;

-- Expected Results Summary:
-- TEST-PROG-25H (25 hrs = Day 2):   RM 50
-- TEST-PROG-50H (50 hrs = Day 3):   RM 150
-- TEST-PROG-75H (75 hrs = Day 4):   RM 300
-- TEST-PROG-100H (100 hrs = Day 5): RM 500

-- ============================================
-- TEST 4: RESERVED SPOT VIOLATION FINE (RM 100)
-- ============================================
-- Insert vehicle entering RESERVED spot without reservation
-- This will trigger RESERVED_SPOT_VIOLATION fine = RM 100

-- First, ensure there's a RESERVED spot available
-- (Assuming F1-R1-S15 is a RESERVED spot based on your parking lot setup)

INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-7', 'CAR', FALSE, INTERVAL 20 MINUTE, 'F1-R3-S3');

-- Expected Result:
-- When 'TEST-RESERVED' enters a RESERVED spot without valid reservation:
-- - RESERVED_SPOT_VIOLATION fine: RM 100.00 (issued immediately at entry)
-- - At exit: Parking fee + RM 100 fine

-- ============================================
-- TEST 5: GRACE PERIOD (15-minute U-turn) - NO FINE
-- ============================================
-- Insert vehicle that entered 10 minutes ago
-- This should NOT trigger any fine (within grace period)

INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-GRACE', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 10 MINUTE), 'F1-R1-S4');

-- Verify the vehicle is within grace period
SELECT 
    license_plate,
    entry_time,
    elapsed_minutes,
    'Should be < 15 minutes' as expected
FROM vehicles_with_duration
WHERE license_plate = 'TEST-GRACE';

-- Expected Result:
-- When you process exit for 'TEST-GRACE' within 15 minutes:
-- - Parking fee: RM 0.00 (grace period)
-- - No fines

-- ============================================
-- TEST 6: UNPAID BALANCE FINE (Partial Payment)
-- ============================================
-- This fine is created automatically when partial payment is made
-- Example: Total due = RM 100, Paid = RM 50, Unpaid balance = RM 50

-- Insert a normal vehicle (2 hours ago)
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-PARTIAL', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'F1-R1-S5');

-- Expected Result:
-- When you process exit for 'TEST-PARTIAL' with partial payment:
-- - Total due: 2 hours × hourly rate (e.g., RM 10)
-- - If you pay only RM 5, system creates UNPAID_BALANCE fine: RM 5
-- - Next time this vehicle enters, it will show RM 5 unpaid fine

-- ============================================
-- TEST 7: PREPAID RESERVATION - NO FINE
-- ============================================
-- Create a reservation for a RESERVED spot
-- Vehicle with valid reservation should NOT get any fines

-- Insert reservation (valid for next 24 hours)
INSERT INTO reservations (license_plate, spot_id, start_time, end_time, is_active, created_at, prepaid_amount)
VALUES ('TEST-PREPAID', 'F1-R1-S15', NOW(), DATE_ADD(NOW(), INTERVAL 24 HOUR), TRUE, NOW(), 240.00);

-- Insert vehicle with valid reservation
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-PREPAID', 'CAR', FALSE, NOW(), 'F1-R1-S15');

-- Expected Result:
-- When 'TEST-PREPAID' exits:
-- - Parking fee: RM 0.00 (prepaid reservation)
-- - No fines (valid reservation)

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- View all test vehicles with their elapsed time
SELECT 
    license_plate,
    entry_time,
    elapsed_hours,
    elapsed_minutes,
    is_overstay,
    assigned_spot_id
FROM vehicles_with_duration
WHERE license_plate LIKE 'TEST%'
ORDER BY entry_time;

-- View current fine strategy
SELECT 
    name,
    current_fine_strategy,
    'FIXED=RM50, HOURLY=RM20/hr, PROGRESSIVE=RM50+RM10/hr' as strategy_info
FROM parking_lots
WHERE id = 1;

-- View all reservations
SELECT 
    license_plate,
    spot_id,
    start_time,
    end_time,
    is_active,
    prepaid_amount
FROM reservations
WHERE license_plate LIKE 'TEST%';

-- ============================================
-- TESTING INSTRUCTIONS
-- ============================================
-- 
-- 1. Run this script to insert test vehicles
-- 2. Open the Parking Lot Management application
-- 3. Go to "Vehicle Exit" panel
-- 4. Process exit for each test vehicle:
--    - TEST-FIXED: Should show RM 50 overstay fine
--    - TEST-HOURLY: Should show RM 60 overstay fine (3 hrs × RM 20)
--    - TEST-PROGRESSIVE: Should show RM 100 overstay fine (RM 50 + 5 × RM 10)
--    - TEST-RESERVED: Should show RM 100 reserved spot violation fine
--    - TEST-GRACE: Should show RM 0.00 parking fee (grace period)
--    - TEST-PARTIAL: Pay partial amount to create unpaid balance fine
--    - TEST-PREPAID: Should show RM 0.00 (prepaid reservation)
-- 
-- 5. Verify fines in "Admin Panel" → "Fine Report"
-- 
-- ============================================
-- TEST 8: HANDICAPPED CARD HOLDER PRICING
-- ============================================
-- Test handicapped card holder pricing logic
-- New requirement:
-- - ANY vehicle type WITH card holder in HANDICAPPED spot: FREE (RM 0/hour)
-- - ANY vehicle type WITH card holder in other spots: RM 2/hour (discounted)
-- - ANY vehicle type WITHOUT card holder: Normal spot rate

-- Test Case 8A: HANDICAPPED vehicle type WITH card holder in HANDICAPPED spot (2 hours)
-- Expected: 2 hours × RM 0.00 = RM 0.00 (FREE)
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-1', 'HANDICAPPED', TRUE, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'F1-R3-S1');

-- Test Case 8B: CAR vehicle type WITH card holder in HANDICAPPED spot (2 hours)
-- Expected: 2 hours × RM 0.00 = RM 0.00 (FREE)
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-2', 'CAR', TRUE, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'F1-R3-S2');

-- Test Case 8C: HANDICAPPED vehicle type WITH card holder in REGULAR spot (2 hours)
-- Expected: 2 hours × RM 2.00 = RM 4.00 (discounted rate)
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-3', 'HANDICAPPED', TRUE, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'F1-R2-S1');

-- Test Case 8D: CAR vehicle type WITH card holder in REGULAR spot (2 hours)
-- Expected: 2 hours × RM 2.00 = RM 4.00 (discounted rate)
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-4', 'CAR', TRUE, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'F1-R2-S2');

-- Test Case 8E: HANDICAPPED vehicle type WITHOUT card holder in HANDICAPPED spot (2 hours)
-- Expected: 2 hours × RM 5.00 = RM 10.00 (normal handicapped spot rate)
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-5', 'HANDICAPPED', FALSE, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'F1-R3-S1');

-- Test Case 8F: CAR vehicle type WITHOUT card holder in REGULAR spot (2 hours)
-- Expected: 2 hours × RM 5.00 = RM 10.00 (normal regular spot rate)
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-6', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'F1-R2-S3');

-- Verify all test vehicles
SELECT 
    v.license_plate,
    v.vehicle_type,
    v.is_handicapped as card_holder,
    ps.spot_type,
    ps.hourly_rate as spot_rate,
    vd.elapsed_hours,
    CASE 
        WHEN v.is_handicapped = TRUE AND ps.spot_type = 'HANDICAPPED' THEN 'RM 0.00/hr (FREE)'
        WHEN v.is_handicapped = TRUE THEN 'RM 2.00/hr (discounted)'
        ELSE CONCAT('RM ', ps.hourly_rate, '/hr (normal)')
    END as expected_rate,
    CASE 
        WHEN v.is_handicapped = TRUE AND ps.spot_type = 'HANDICAPPED' THEN 0.00
        WHEN v.is_handicapped = TRUE THEN vd.elapsed_hours * 2.00
        ELSE vd.elapsed_hours * ps.hourly_rate
    END as expected_fee
FROM vehicles v
JOIN vehicles_with_duration vd ON v.license_plate = vd.license_plate
JOIN parking_spots ps ON v.assigned_spot_id = ps.spot_id
WHERE v.license_plate LIKE 'TEST-HANDI%' OR v.license_plate LIKE 'TEST-CAR%';

-- Expected Results Summary:
-- TEST-HANDI-H (HANDICAPPED + card holder in HANDICAPPED spot): RM 0.00 (FREE)
-- TEST-CAR-H (CAR + card holder in HANDICAPPED spot):           RM 0.00 (FREE)
-- TEST-HANDI-R (HANDICAPPED + card holder in REGULAR spot):     RM 4.00 (2 hrs × RM 2.00)
-- TEST-CAR-R (CAR + card holder in REGULAR spot):               RM 4.00 (2 hrs × RM 2.00)
-- TEST-HANDI-NO (HANDICAPPED + NO card holder in HANDICAPPED):  RM 10.00 (2 hrs × RM 5.00)
-- TEST-CAR-NO (CAR + NO card holder in REGULAR spot):           RM 10.00 (2 hrs × RM 5.00)

-- ============================================
-- CLEANUP AFTER TESTING
-- ============================================
-- 
-- DELETE FROM fines WHERE license_plate LIKE 'TEST%';
-- DELETE FROM payments WHERE license_plate LIKE 'TEST%';
-- DELETE FROM reservations WHERE license_plate LIKE 'TEST%';
-- DELETE FROM vehicles WHERE license_plate LIKE 'TEST%';
-- 
-- ============================================


-- ============================================
-- TEST 9: EXPIRED RESERVATION WITH OVERSTAY FINE
-- ============================================
-- Test vehicle with reservation that stays beyond reservation period
-- Expected: RM 100 (expired reservation) + RM 50 (overstay with FIXED strategy) = RM 150

-- Set parking lot to use FIXED strategy
UPDATE parking_lots SET current_fine_strategy = 'FIXED' WHERE id = 1;

-- Step 1: Create a reservation that started 26 hours ago and ended 2 hours ago (24-hour duration)
INSERT INTO reservations (license_plate, spot_id, start_time, end_time, is_active, created_at, prepaid_amount)
VALUES (
    'TEST-1',
    'F1-R3-S3',
    DATE_SUB(NOW(), INTERVAL 26 HOUR),  -- Started 26 hours ago
    DATE_SUB(NOW(), INTERVAL 2 HOUR),   -- Ended 2 hours ago (24-hour reservation)
    TRUE,
    DATE_SUB(NOW(), INTERVAL 26 HOUR),
    240.00  -- Prepaid for 24 hours
);

-- Step 2: Insert vehicle that entered 26 hours ago (with valid reservation at entry time)
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-1', 'HANDICAPPED', FALSE, DATE_SUB(NOW(), INTERVAL 26 HOUR), 'F1-R3-S3');

-- Verify the setup
SELECT 
    v.license_plate,
    v.entry_time,
    vd.elapsed_hours,
    r.start_time as reservation_start,
    r.end_time as reservation_end,
    CASE 
        WHEN NOW() > r.end_time THEN 'EXPIRED'
        WHEN NOW() BETWEEN r.start_time AND r.end_time THEN 'VALID'
        ELSE 'NOT YET STARTED'
    END as reservation_status,
    TIMESTAMPDIFF(HOUR, r.end_time, NOW()) as hours_overstayed_reservation
FROM vehicles v
JOIN vehicles_with_duration vd ON v.license_plate = vd.license_plate
JOIN reservations r ON v.license_plate = r.license_plate AND v.assigned_spot_id = r.spot_id
WHERE v.license_plate = 'TEST-EXPIRED';

-- Expected Results:
-- When you process exit for 'TEST-EXPIRED':
-- 1. Reservation Status: EXPIRED (ended 2 hours ago)
-- 2. Total parking time: 26 hours
-- 3. Overstay: 26 hours > 24 hours = 2 hours overstay
-- 4. Fines:
--    - RM 100.00 (UNAUTHORIZED_RESERVED - reservation expired, now unauthorized in reserved spot)
--    - RM 50.00 (OVERSTAY - FIXED strategy for 26 hours > 24 hours)
-- 5. Total Fines: RM 150.00
-- 6. Parking Fee: RM 0.00 (prepaid reservation covered the valid period)
-- 7. Total Due: RM 150.00 (fines only)

-- ============================================
-- CLEANUP AFTER TESTING
-- ============================================
-- 
-- DELETE FROM fines WHERE license_plate LIKE 'TEST%';
-- DELETE FROM payments WHERE license_plate LIKE 'TEST%';
-- DELETE FROM reservations WHERE license_plate LIKE 'TEST%';
-- DELETE FROM vehicles WHERE license_plate LIKE 'TEST%';
-- 
-- ============================================


-- ============================================
-- TEST 10: HANDICAPPED CARD HOLDER IN UNAUTHORIZED RESERVED SPOT
-- ============================================
-- Test handicapped vehicle type with card holder parking in reserved spot without reservation
-- Expected: RM 100 (unauthorized) + RM 2 (1 hour × RM 2/hr discounted rate) = RM 102

-- Step 1: Insert the unauthorized reserved spot fine (issued at entry)
INSERT INTO fines (license_plate, fine_type, amount, issue_date, is_paid)
VALUES ('TEST-2', 'UNAUTHORIZED_RESERVED', 100.00, DATE_SUB(NOW(), INTERVAL 1 HOUR), FALSE);

-- Step 2: Insert handicapped vehicle with card holder in RESERVED spot (1 hour ago, beyond grace period)
-- Assuming F1-R1-S15 is a RESERVED spot
INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
VALUES ('TEST-2', 'HANDICAPPED', TRUE, DATE_SUB(NOW(), INTERVAL 1 HOUR), 'F1-R3-S4');

-- Verify the vehicle
SELECT 
    v.license_plate,
    v.vehicle_type,
    v.is_handicapped as card_holder,
    ps.spot_type,
    ps.hourly_rate as spot_rate,
    v.entry_time,
    vd.elapsed_hours,
    vd.elapsed_minutes,
    CASE 
        WHEN v.is_handicapped = TRUE AND ps.spot_type = 'HANDICAPPED' THEN 'RM 0.00/hr (FREE)'
        WHEN v.is_handicapped = TRUE THEN 'RM 2.00/hr (discounted)'
        ELSE CONCAT('RM ', ps.hourly_rate, '/hr (normal)')
    END as expected_rate,
    CASE 
        WHEN v.is_handicapped = TRUE AND ps.spot_type = 'HANDICAPPED' THEN 0.00
        WHEN v.is_handicapped = TRUE THEN vd.elapsed_hours * 2.00
        ELSE vd.elapsed_hours * ps.hourly_rate
    END as expected_parking_fee
FROM vehicles v
JOIN vehicles_with_duration vd ON v.license_plate = vd.license_plate
JOIN parking_spots ps ON v.assigned_spot_id = ps.spot_id
WHERE v.license_plate = 'TEST-HANDI-UNAUTH';

-- Expected Results when you process exit:
-- 1. Vehicle Type: HANDICAPPED
-- 2. Card Holder: YES
-- 3. Spot Type: RESERVED
-- 4. Duration: 1 hour (beyond 15-minute grace period)
-- 5. Parking Fee: 1 hour × RM 2.00 = RM 2.00 (discounted rate for card holder)
-- 6. Fines:
--    - Unauthorized Reserved Spot: RM 100.00 (no valid reservation)
-- 7. Total Fines: RM 100.00
-- 8. TOTAL DUE: RM 2.00 + RM 100.00 = RM 102.00

-- ============================================
-- CLEANUP AFTER TESTING
-- ============================================
-- 
-- DELETE FROM fines WHERE license_plate LIKE 'TEST%';
-- DELETE FROM payments WHERE license_plate LIKE 'TEST%';
-- DELETE FROM reservations WHERE license_plate LIKE 'TEST%';
-- DELETE FROM vehicles WHERE license_plate LIKE 'TEST%';
-- 
-- ============================================
