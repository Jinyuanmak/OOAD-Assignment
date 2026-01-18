# UML Diagrams Update Summary

## ✅ Updates Applied to UML_DIAGRAMS.md

The UML diagrams have been updated to reflect all recent changes and enhancements to the parking management system.

### 1. Model Layer Updates

#### Vehicle Class
**Added Fields:**
- `elapsedSeconds: Long` - Real-time seconds parked
- `elapsedMinutes: Long` - Real-time minutes parked
- `elapsedHours: Long` - Real-time hours parked (used for fee calculation)
- `isOverstay: Boolean` - Flag indicating if vehicle has overstayed (> 24 hours)

**Added Methods:**
- `getElapsedSeconds()`, `getElapsedMinutes()`, `getElapsedHours()`, `getIsOverstay()`
- `setElapsedSeconds()`, `setElapsedMinutes()`, `setElapsedHours()`, `setIsOverstay()`

#### ParkingLot Class
**Added Method:**
- `findSpotById(String): ParkingSpot` - Finds spot by ID across all floors

### 2. Controller Layer Updates

#### VehicleExitController Class
**Added Field:**
- `fineManager: FineManager` - Manages fine generation and processing

**Updated Relationships:**
- Now uses `FineManager` for overstay fine generation
- `FineManager` uses `FineCalculationStrategy` (FixedFineStrategy)

### 3. Sequence Diagram Updates

#### Vehicle Exit & Payment Flow
**Major Changes:**
1. **Vehicle Lookup Enhanced:**
   - Checks both in-memory and database
   - Loads from `vehicles_with_duration` VIEW
   - Syncs in-memory spot with database state

2. **Overstay Fine Generation Added:**
   - Checks if `elapsed_hours > 24` OR `isOverstay = TRUE`
   - Calls `FineManager.checkAndGenerateOverstayFine()`
   - Saves fine to database immediately
   - Prevents duplicates

3. **Partial Payment Flow Updated:**
   - **Always** marks original fines as paid
   - Creates `UNPAID_BALANCE` fine for remaining amount
   - Shows partial payment dialog to user
   - Unpaid balance persists to next visit

### 4. ER Diagram Updates

#### New Database VIEW
**Added: `vehicles_with_duration`**
- Real-time VIEW that calculates elapsed time
- Fields:
  - All original vehicle fields
  - `elapsed_seconds` (computed)
  - `elapsed_minutes` (computed)
  - `elapsed_hours` (computed)
  - `is_overstay` (computed, TRUE if > 24 hours)
- Timezone-aware (UTC+8)
- No caching - always real-time

### 5. Documentation Updates

#### Added Section: Recent Updates (January 2026)
Documents all new features:
1. Real-Time Elapsed Time Tracking
2. Automatic Overstay Fine Generation
3. Partial Payment Handling
4. Database Vehicle Lookup
5. Auto-Clear Entry Form

#### Enhanced Notes Section
- Added implementation details for new features
- Documented fine management flow
- Explained timezone configuration
- Described VIEW usage

---

## Key Changes Summary

| Component | What Changed | Why |
|-----------|-------------|-----|
| **Vehicle Model** | Added elapsed time fields | Real-time tracking from VIEW |
| **ParkingLot** | Added findSpotById() | Support database vehicle lookup |
| **VehicleExitController** | Added FineManager | Automatic overstay fine generation |
| **Exit Sequence** | Enhanced lookup & payment flow | Handle database vehicles & partial payments |
| **ER Diagram** | Added vehicles_with_duration VIEW | Real-time elapsed time calculation |
| **Documentation** | Added Recent Updates section | Document new features |

---

## Verification Checklist

- [x] Vehicle class shows elapsed time fields
- [x] ParkingLot class shows findSpotById() method
- [x] VehicleExitController shows FineManager field
- [x] Relationships show VehicleExitController → FineManager
- [x] Sequence diagram shows overstay fine generation
- [x] Sequence diagram shows partial payment flow
- [x] ER diagram includes vehicles_with_duration VIEW
- [x] Notes section documents new features
- [x] Recent Updates section added

---

## How to View Updated Diagrams

1. **GitHub**: Automatically renders Mermaid diagrams
2. **VS Code**: Install "Markdown Preview Mermaid Support" extension
3. **Online**: Use https://mermaid.live/ to paste and view diagrams

---

## Files Updated

- ✅ `UML_DIAGRAMS.md` - All diagrams and documentation updated
- ✅ `UML_UPDATE_SUMMARY.md` - This summary document (NEW)

---

## Next Steps

The UML diagrams now accurately reflect the current implementation. Use these diagrams for:
- System documentation
- Code reviews
- Onboarding new developers
- Architecture discussions
- Academic submissions

All diagrams are up-to-date with the latest code as of January 19, 2026.
