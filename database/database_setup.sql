-- ============================================
-- Parking Lot Management System - MySQL Setup
-- ============================================
-- Run this SQL script in phpMyAdmin or MySQL Workbench
-- to manually create the database and tables
-- ============================================

-- Step 0: Set Timezone (Important for elapsed time calculations)
-- Adjust this to your local timezone:
-- Malaysia/Singapore/China: '+08:00'
-- Thailand/Vietnam: '+07:00'
-- Japan/Korea: '+09:00'
-- India: '+05:30'
-- UK: '+00:00'
SET GLOBAL time_zone = '+08:00';
SET SESSION time_zone = '+08:00';

-- Step 1: Create Database
CREATE DATABASE IF NOT EXISTS parking_lot;
USE parking_lot;

-- Step 2: Create Tables

-- Table 1: parking_lots
CREATE TABLE IF NOT EXISTS parking_lots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    total_floors INT NOT NULL DEFAULT 0,
    total_revenue DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    current_fine_strategy VARCHAR(50) DEFAULT 'FIXED'
) ENGINE=InnoDB;

-- Table 2: floors
CREATE TABLE IF NOT EXISTS floors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL,
    floor_number INT NOT NULL,
    total_spots INT NOT NULL DEFAULT 0,
    FOREIGN KEY (parking_lot_id) REFERENCES parking_lots(id)
) ENGINE=InnoDB;

-- Table 3: parking_spots
CREATE TABLE IF NOT EXISTS parking_spots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    floor_id BIGINT NOT NULL,
    spot_id VARCHAR(50) NOT NULL UNIQUE,
    spot_type VARCHAR(20) NOT NULL,
    hourly_rate DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    current_vehicle_id BIGINT,
    FOREIGN KEY (floor_id) REFERENCES floors(id)
) ENGINE=InnoDB;

-- Table 4: vehicles
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    vehicle_type VARCHAR(20) NOT NULL,
    is_handicapped BOOLEAN NOT NULL DEFAULT FALSE,
    entry_time DATETIME,
    exit_time DATETIME,
    assigned_spot_id VARCHAR(50)
) ENGINE=InnoDB;

-- ============================================
-- VIEW: vehicles_with_duration
-- ============================================
-- This VIEW calculates elapsed time in real-time (no caching)
-- Used for:
-- 1. Accurate parking duration calculation
-- 2. Automatic overstay detection (> 24 hours)
-- 3. Fee calculation based on actual elapsed time
-- 4. Timezone-aware calculations (respects session timezone)
-- ============================================
CREATE OR REPLACE VIEW vehicles_with_duration AS
SELECT 
    v.*,
    -- Elapsed seconds
    CASE 
        WHEN v.exit_time IS NOT NULL THEN 
            TIMESTAMPDIFF(SECOND, v.entry_time, v.exit_time)
        WHEN v.entry_time IS NOT NULL THEN 
            TIMESTAMPDIFF(SECOND, v.entry_time, CURRENT_TIMESTAMP)
        ELSE 0
    END as elapsed_seconds,
    -- Elapsed minutes
    CASE 
        WHEN v.exit_time IS NOT NULL THEN 
            TIMESTAMPDIFF(MINUTE, v.entry_time, v.exit_time)
        WHEN v.entry_time IS NOT NULL THEN 
            TIMESTAMPDIFF(MINUTE, v.entry_time, CURRENT_TIMESTAMP)
        ELSE 0
    END as elapsed_minutes,
    -- Elapsed hours (used for fee calculation)
    CASE 
        WHEN v.exit_time IS NOT NULL THEN 
            TIMESTAMPDIFF(HOUR, v.entry_time, v.exit_time)
        WHEN v.entry_time IS NOT NULL THEN 
            TIMESTAMPDIFF(HOUR, v.entry_time, CURRENT_TIMESTAMP)
        ELSE 0
    END as elapsed_hours,
    -- Overstay flag (TRUE if > 24 hours)
    CASE 
        WHEN v.exit_time IS NOT NULL THEN 
            TIMESTAMPDIFF(HOUR, v.entry_time, v.exit_time) > 24
        WHEN v.entry_time IS NOT NULL THEN 
            TIMESTAMPDIFF(HOUR, v.entry_time, CURRENT_TIMESTAMP) > 24
        ELSE FALSE
    END as is_overstay
FROM vehicles v;

-- Table 5: fines
-- ============================================
-- Fine Types:
-- 1. OVERSTAY: Generated automatically when vehicle > 24 hours (RM 50.00)
-- 2. UNPAID_BALANCE: Created when partial payment made (variable amount)
-- 3. UNAUTHORIZED_RESERVED: Reserved spot violation (if implemented)
-- ============================================
CREATE TABLE IF NOT EXISTS fines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    fine_type VARCHAR(30) NOT NULL COMMENT 'OVERSTAY, UNPAID_BALANCE, UNAUTHORIZED_RESERVED',
    amount DECIMAL(10,2) NOT NULL,
    issued_date DATETIME NOT NULL,
    is_paid BOOLEAN NOT NULL DEFAULT FALSE COMMENT '1=PAID, 0=UNPAID'
) ENGINE=InnoDB;

-- Table 6: payments
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    parking_fee DECIMAL(10,2) NOT NULL,
    fine_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL COMMENT 'CASH or CARD',
    payment_date DATETIME NOT NULL
) ENGINE=InnoDB;

-- Table 7: reservations
-- ============================================
-- Reservation System:
-- Allows vehicles to reserve RESERVED spots in advance
-- Validates reservations during vehicle entry
-- Issues UNAUTHORIZED_RESERVED fine (RM 100) if parking without valid reservation
-- ============================================
CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL COMMENT 'Vehicle license plate',
    spot_id VARCHAR(50) NOT NULL COMMENT 'Reserved spot ID (must be RESERVED type)',
    start_time DATETIME NOT NULL COMMENT 'Reservation start time',
    end_time DATETIME NOT NULL COMMENT 'Reservation end time',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '1=ACTIVE, 0=CANCELLED',
    created_at DATETIME NOT NULL COMMENT 'Reservation creation time',
    prepaid_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Amount paid upfront for reservation (RM 10/hr × duration)',
    INDEX idx_license_plate (license_plate),
    INDEX idx_spot_id (spot_id),
    INDEX idx_time_range (start_time, end_time)
) ENGINE=InnoDB;

-- ============================================
-- Verification Queries
-- ============================================

-- Show all tables
SHOW TABLES;

-- Show VIEWs
SHOW FULL TABLES WHERE Table_type = 'VIEW';

-- Verify table structures
DESCRIBE parking_lots;
DESCRIBE floors;
DESCRIBE parking_spots;
DESCRIBE vehicles;
DESCRIBE fines;
DESCRIBE payments;

-- Verify VIEW structure
DESCRIBE vehicles_with_duration;

-- ============================================
-- Test Queries for Real-Time Elapsed Time
-- ============================================

-- Check current timezone setting
SELECT @@session.time_zone, @@global.time_zone;

-- View all active vehicles with elapsed time
SELECT 
    license_plate,
    entry_time,
    elapsed_hours,
    is_overstay
FROM vehicles_with_duration
WHERE exit_time IS NULL;

-- ============================================
-- Sample Test Data (Optional)
-- ============================================

-- Insert a test vehicle (2 hours ago)
-- INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
-- VALUES ('TEST001', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'F1-R1-S1');

-- Insert an overstay vehicle (26 hours ago)
-- INSERT INTO vehicles (license_plate, vehicle_type, is_handicapped, entry_time, assigned_spot_id)
-- VALUES ('OVERSTAY001', 'CAR', FALSE, DATE_SUB(NOW(), INTERVAL 26 HOUR), 'F1-R2-S1');

-- ============================================
-- Cleanup Queries (Use with caution!)
-- ============================================

-- Reset all data (keeps structure)
-- DELETE FROM payments;
-- DELETE FROM fines;
-- DELETE FROM vehicles;
-- UPDATE parking_spots SET status = 'AVAILABLE', current_vehicle_id = NULL;
-- UPDATE parking_lots SET total_revenue = 0 WHERE id = 1;

-- ============================================
-- Setup Complete!
-- ============================================
-- You can now run the Parking Lot Management application
-- It will connect to this database automatically
-- 
-- Key Features:
-- ✅ Real-time elapsed time tracking via VIEW
-- ✅ Automatic overstay detection (> 24 hours)
-- ✅ Timezone-aware calculations (UTC+8)
-- ✅ Partial payment support with unpaid balance
-- ✅ Fine persistence across sessions
-- ============================================
