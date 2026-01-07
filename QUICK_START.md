# Quick Start Guide

## Prerequisites

### 1. Install Laragon (for MySQL)
Download from: https://laragon.org/download/

### 2. Start MySQL
1. Open Laragon
2. Click **"Start All"** button
3. Wait for MySQL to show green indicator

---

## Run the Application in 3 Steps

### Step 1: Start the Application
```
Double-click: run-parking-system.bat
```

### Step 2: Park a Vehicle
1. Go to **Vehicle Entry** tab
2. Enter license plate: `ABC123`
3. Select vehicle type: `Car`
4. Select spot type: `Regular`
5. Click **Park Vehicle**

### Step 3: Process Exit
1. Go to **Vehicle Exit** tab
2. Enter license plate: `ABC123`
3. Click **Lookup Vehicle**
4. Enter payment amount (shown in summary)
5. Click **Process Payment**

---

## That's It!

You're now ready to use the parking lot management system.

For detailed instructions, see **USER_GUIDE.md**

For technical details, see **README.md**

---

## System Requirements

- Java 11 or higher
- Laragon (with MySQL)
- Windows operating system
- 100 MB free disk space

---

## Need Help?

1. **Can't start the application?**
   - Check if Java is installed: `java -version`
   - Make sure Laragon MySQL is running (green indicator)

2. **Database connection errors?**
   - Start Laragon and click "Start All"
   - Check MySQL is running on port 3306

3. **View database data?**
   - Right-click Laragon > MySQL > phpMyAdmin
   - Browse `parking_lot_db` database

4. **Want to learn more?**
   - Read `USER_GUIDE.md` for complete instructions
   - Read `README.md` for technical documentation
   - Read `DATABASE_TOOLS.md` for database details

---

**Version:** 1.0.1
