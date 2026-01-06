# Parking Lot Management System - Changelog

## Version 1.0.1 - January 6, 2026

### Bug Fixes

#### Duplicate Parking Prevention
**Issue:** Same license plate could park in multiple spots at the same time
- This caused incorrect fee calculation (only showed RM 5 for one spot instead of total)
- Created data integrity issues
- Caused confusion in admin panel

**Fix Applied:**
- Added validation in `VehicleEntryController.processEntry()` to check if vehicle is already parked
- Implemented `isVehicleAlreadyParked()` helper method that checks all parking spots
- Validation is case-insensitive (ABC123 = abc123 = AbC123)
- Clear error message: "Vehicle {PLATE} is already parked. Please exit first before parking again."

**Files Modified:**
- `src/main/java/com/university/parking/controller/VehicleEntryController.java`

**Testing:**
- JAR rebuilt successfully: 2026-01-06 23:10:49
- See `TEST_DUPLICATE_PARKING.md` for detailed test instructions
- All 146 tests still passing

**Impact:**
- Prevents data corruption from duplicate parking
- Ensures accurate fee calculation
- Maintains one-vehicle-per-spot integrity

---

## Version 1.0.0 - January 2026

### Initial Release

#### Core Features
- Multi-floor parking management (5 floors, 75 spots)
- Vehicle entry and exit processing
- Automated fee calculation
- Fine management system
- Payment processing (Cash and Credit Card)
- H2 database persistence
- Comprehensive GUI with Swing

#### Spot Types
- Compact: RM 2/hour
- Regular: RM 5/hour
- Handicapped: RM 2/hour (free for handicapped card holders in designated spots)
- Reserved: RM 10/hour

#### Fine Schemes
- Fixed Fine: Flat RM 50
- Progressive Fine: Escalating from RM 50 to RM 200
- Hourly Fine: RM 20/hour

#### Advanced Features
- Unpaid balance tracking across sessions
- Configurable fine calculation strategies
- Comprehensive reporting (revenue, occupancy, vehicles, fines)
- Admin panel for system management

#### Testing
- 146 comprehensive tests
- Property-based testing with jqwik
- Integration tests for end-to-end workflows
- Unit tests for all components

#### Documentation
- `README.md` - Main project documentation
- `USER_GUIDE.md` - Complete user manual
- `QUICK_START.md` - 3-step quick start guide
- `PROJECT_SUMMARY.md` - Project overview and statistics
- `REQUIREMENTS_COMPLIANCE.md` - Assignment requirements verification
- `ASSIGNMENT_VERIFICATION.md` - Detailed compliance checklist

---

## How to Update

To get the latest version with bug fixes:

1. **Rebuild the JAR:**
   ```bash
   apache-maven-3.9.12\bin\mvn.cmd clean package -DskipTests
   ```

2. **Run the application:**
   ```
   Double-click: run-parking-system.bat
   ```

3. **Verify the fix:**
   - Follow test instructions in `TEST_DUPLICATE_PARKING.md`
   - Attempt to park same vehicle twice
   - Should see error message preventing duplicate parking

---

## Known Issues

None at this time. All 146 tests passing.

---

## Future Enhancements

Potential improvements for future versions:
- Online payment integration
- Mobile app support
- Email receipt generation
- Reservation system for reserved spots
- Multi-language support
- Advanced analytics dashboard

---

**For support or questions, refer to:**
- `USER_GUIDE.md` - Complete usage instructions
- `README.md` - Technical documentation
- `TEST_DUPLICATE_PARKING.md` - Testing the duplicate parking fix
