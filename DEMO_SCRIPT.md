# University Parking Lot Management System - Demo Script

## 5-Minute Demo Video Script

---

Welcome to the University Parking Lot Management System, a comprehensive parking facility solution built with Java Swing and MySQL. This system manages a 75-spot parking facility across 5 floors, supporting multiple vehicle types and spot categories including Compact, Regular, Handicapped, and Reserved spots.

Let me start by showing you the dashboard. Here you can see real-time statistics including total capacity, current occupancy, available spots, and revenue generated. The system features both classic and modern user interfaces - today we're using the modern UI with its contemporary design.

Now let's demonstrate vehicle entry. I'll enter a car with license plate ABC1234. The system automatically finds an available Regular spot and assigns F1-R2-S3. Entry time is recorded, and the parking session begins. For handicapped vehicles, the system prioritizes handicapped spots with FREE parking - handicapped card holders pay RM 0.00 per hour when parked in handicapped spots. If a handicapped vehicle parks in a non-handicapped spot, they pay the standard rate for that spot type.

One of our key features is the reservation system. Let me create a reservation for license plate RESERVE01 on spot F1-R3-S15 for 24 hours. The system calculates the prepaid amount - 24 hours times RM 10 per hour equals RM 240. I'll select CARD payment. The reservation is now active, and this vehicle can enter and exit multiple times within 24 hours without additional parking charges. The prepaid amount has already been added to the revenue. Now watch what happens when a different vehicle, WRONG123, tries to park in this reserved spot. The system automatically issues a RM 100 RESERVED_SPOT_VIOLATION fine. The vehicle can still park but must pay the fine upon exit.

Let me demonstrate the 15-minute grace period feature. I'll enter vehicle GRACE001, and if it exits within 15 minutes, there's no charge - perfect for quick drop-offs. Looking at the exit panel, the payment summary shows RM 0.00 for parking fee with "15-MIN GRACE PERIOD" status. For CASH payment, the system prompts for cash insertion - I'll select RM 10. The receipt shows cash inserted RM 10 and refund amount RM 10, providing full refund. For CARD payment, no amount is needed and the vehicle exits immediately.

Now let's process a normal vehicle exit. I'll look up ABC1234 which has been parked for 2 hours. The payment summary shows parking fee of RM 10 - that's 2 hours times RM 5 per hour for a Regular spot. I'll enter RM 10 and select CARD payment. A receipt dialog appears with all transaction details, and the system asks if I want to save it as PDF. The receipt includes license plate, spot ID, entry and exit times, itemized charges, and payment method.

For the prepaid reservation exit, let me process RESERVE01. Notice the payment summary shows RM 0.00 with "PREPAID RESERVATION" status, and the payment amount field is locked. For CASH payment, the system still shows the denomination dialog to ensure proper cash handling. I'll select RM 10, and the receipt shows the prepaid status with cash inserted and full refund amount.

The system supports three fine calculation strategies for vehicles that overstay beyond 24 hours. The FIXED strategy charges RM 50 flat regardless of duration. The HOURLY strategy charges RM 20 per hour of overstay. The PROGRESSIVE strategy uses tiered fines based on days parked - Day 2 is RM 50, Day 3 adds RM 100 for a total of RM 150, Day 4 adds another RM 150 for RM 300 total, and Day 5 and beyond adds RM 200 for a maximum of RM 500. Let me process an overstay vehicle that's been parked for 50 hours. The payment summary shows parking fee of RM 250 plus an overstay fine of RM 150 since it's in Day 3, totaling RM 400.

The system also handles partial payments. If a customer pays only RM 200 instead of the full RM 400, the system processes it as a partial payment and creates an UNPAID_BALANCE fine for the remaining RM 200. This fine persists and will appear when the vehicle enters again.

For reporting and analytics, the system provides four comprehensive reports. The Vehicle Report shows all currently parked vehicles with entry times and overstay status. The Revenue Report displays total revenue with breakdowns by floor and spot type. The Occupancy Report shows utilization rates across floors and spot types. The Fine Report lists all fines with their types, amounts, and payment status. All reports can be exported in TXT, PDF, or CSV formats for further analysis.

In the Reservations panel, you can view all active and expired reservations. To cancel a reservation, select it and click cancel. The system shows a detailed confirmation dialog with remaining time and prepaid amount, along with a warning that no refund will be issued. After cancellation, the spot becomes available for new reservations.

Administrators can change the fine calculation strategy from the Admin panel. When changing from FIXED to PROGRESSIVE, the system records the exact change time. The new strategy applies only to vehicles entering after the change, while existing parked vehicles keep their original strategy for fair treatment.

To summarize, this system provides prepaid reservations with multiple entry-exit capability, a 15-minute grace period for quick stops, three flexible fine calculation strategies, automatic RM 100 fines for reserved spot violations, partial payment support with unpaid balance tracking, proper cash refund handling for zero-payment exits, real-time duration tracking with timezone awareness, comprehensive reporting with multiple export formats, receipt generation in both text and PDF, and complete database persistence for historical tracking. Thank you for watching this demonstration of the University Parking Lot Management System.

---

## Test Data Setup

Before recording, run `database/test_fine_strategies.sql` to insert test vehicles: ABC1234 (normal car, 2 hours), HANDI001 (handicapped), RESERVE01 (with reservation), WRONG123 (for violation), GRACE001 (for grace period), and TEST-PROG-50H (overstay vehicle, 50 hours).

## Recording Tips

Speak clearly at a moderate pace, pause after each action to let viewers see results, use mouse cursor to highlight important elements, demonstrate both CASH and CARD payment methods, show receipt dialogs and PDF save options, and navigate smoothly between panels to maintain flow.
