-- ============================================
-- Parking Lot Management System - MySQL Setup
-- ============================================
-- Run this SQL script in phpMyAdmin or MySQL Workbench
-- to manually create the database and tables
-- ============================================

-- Step 1: Create Database
CREATE DATABASE IF NOT EXISTS parking_lot_db;
USE parking_lot_db;

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

-- Table 5: fines
CREATE TABLE IF NOT EXISTS fines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    fine_type VARCHAR(30) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    issued_date DATETIME NOT NULL,
    is_paid BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

-- Table 6: payments
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    parking_fee DECIMAL(10,2) NOT NULL,
    fine_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_date DATETIME NOT NULL
) ENGINE=InnoDB;

-- ============================================
-- Verification Queries
-- ============================================

-- Show all tables
SHOW TABLES;

-- Verify table structures
DESCRIBE parking_lots;
DESCRIBE floors;
DESCRIBE parking_spots;
DESCRIBE vehicles;
DESCRIBE fines;
DESCRIBE payments;

-- ============================================
-- Setup Complete!
-- ============================================
-- You can now run the Parking Lot Management application
-- It will connect to this database automatically
-- ============================================
