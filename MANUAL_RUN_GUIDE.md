# Manual Run Guide (Without Maven or Pre-built JAR)

This guide explains how to compile and run the Parking Lot Management System using only Java commands.

---

## Prerequisites

### 1. Java Development Kit (JDK) 11 or higher

**Check if installed:**
```cmd
java -version
javac -version
```

**If not installed:**
- Download from: https://adoptium.net/ (recommended) or https://www.oracle.com/java/technologies/downloads/
- Install and add to PATH

### 2. MySQL Database (via Laragon)

**Install Laragon:**
- Download from: https://laragon.org/download/
- Install and open Laragon
- Click "Start All" to start MySQL

**Verify MySQL is running:**
- MySQL should show green indicator in Laragon
- Default: localhost:3306, username: root, password: (empty)

### 3. MySQL Connector/J Driver

**Download the MySQL JDBC driver:**
- Go to: https://dev.mysql.com/downloads/connector/j/
- Select "Platform Independent"
- Download the ZIP file (e.g., `mysql-connector-j-8.0.33.zip`)
- Extract the JAR file: `mysql-connector-j-8.0.33.jar`
- Place it in the project root folder

---

## Step-by-Step Instructions

### Step 1: Set Up Database

Open phpMyAdmin (right-click Laragon > MySQL > phpMyAdmin) and run:

```sql
CREATE DATABASE IF NOT EXISTS parking_lot;
```

Or run the `database/database_setup.sql` file in phpMyAdmin.

### Step 2: Create Output Directory

Open Command Prompt in the project folder and run:

```cmd
mkdir out
```

### Step 3: Compile All Java Files

Run this command to compile all source files:

```cmd
javac -d out -cp "mysql-connector-j-8.0.33.jar" -sourcepath src/main/java src/main/java/com/university/parking/ParkingApplication.java
```

**What this does:**
- `-d out` = Output compiled .class files to "out" folder
- `-cp "mysql-connector-j-8.0.33.jar"` = Include MySQL driver in classpath
- `-sourcepath src/main/java` = Tell compiler where to find source files
- The compiler will automatically find and compile all dependent classes

### Step 4: Run the Application

```cmd
java -cp "out;mysql-connector-j-8.0.33.jar" com.university.parking.ParkingApplication
```

**What this does:**
- `-cp "out;mysql-connector-j-8.0.33.jar"` = Classpath includes compiled classes and MySQL driver
- `com.university.parking.ParkingApplication` = The main class to run

---

## Quick Reference Commands

**Compile:**
```cmd
javac -d out -cp "mysql-connector-j-8.0.33.jar" -sourcepath src/main/java src/main/java/com/university/parking/ParkingApplication.java
```

**Run:**
```cmd
java -cp "out;mysql-connector-j-8.0.33.jar" com.university.parking.ParkingApplication
```

**Clean and recompile:**
```cmd
rmdir /s /q out
mkdir out
javac -d out -cp "mysql-connector-j-8.0.33.jar" -sourcepath src/main/java src/main/java/com/university/parking/ParkingApplication.java
```

---

## Troubleshooting

### "javac is not recognized"
- JDK is not installed or not in PATH
- Install JDK and add `JAVA_HOME\bin` to your PATH environment variable

### "package com.mysql.cj.jdbc does not exist"
- MySQL driver JAR is missing or path is wrong
- Make sure `mysql-connector-j-8.0.33.jar` is in the project root folder

### "Cannot find symbol" or compilation errors
- Make sure you're running the command from the project root folder
- Check that `src/main/java` folder structure is intact

### "Communications link failure" when running
- MySQL is not running
- Start Laragon and click "Start All"

### "Access denied for user 'root'"
- MySQL password is set but code expects empty password
- Either remove MySQL password or update `DatabaseManager.java`

---

## Folder Structure After Compilation

```
parking-lot-management/
├── src/main/java/...          # Source code (.java files)
├── out/                        # Compiled code (.class files)
│   └── com/university/parking/
│       ├── ParkingApplication.class
│       ├── controller/
│       ├── dao/
│       ├── model/
│       ├── util/
│       └── view/
├── mysql-connector-j-8.0.33.jar  # MySQL driver (you download this)
└── ...
```

---

## Why This Is More Complex Than Using JAR

| Method | Steps | Dependencies to Manage |
|--------|-------|----------------------|
| Double-click JAR | 1 | None (bundled) |
| Maven command | 1 | None (Maven handles it) |
| Manual Java commands | 4+ | MySQL driver JAR |

The pre-built `parking-lot-management.jar` already contains:
- All compiled classes
- MySQL driver bundled inside
- Manifest specifying main class

That's why double-clicking the JAR is the easiest way to run the application.

---

## Summary

1. Install JDK 11+
2. Install Laragon and start MySQL
3. Download MySQL Connector/J JAR
4. Compile: `javac -d out -cp "mysql-connector-j-8.0.33.jar" -sourcepath src/main/java src/main/java/com/university/parking/ParkingApplication.java`
5. Run: `java -cp "out;mysql-connector-j-8.0.33.jar" com.university.parking.ParkingApplication`
