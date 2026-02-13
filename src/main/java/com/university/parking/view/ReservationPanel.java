package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.ParkingLotDAO;
import com.university.parking.dao.PaymentDAO;
import com.university.parking.dao.ReservationDAO;
import com.university.parking.model.Floor;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;
import com.university.parking.model.Payment;
import com.university.parking.model.PaymentMethod;
import com.university.parking.model.Reservation;
import com.university.parking.model.SpotType;

/**
 * Panel for managing parking spot reservations.
 * Allows creating, viewing, and cancelling reservations for RESERVED spots.
 */
public class ReservationPanel extends JPanel {
    private final ParkingLot parkingLot;
    private final DatabaseManager dbManager;
    private final ReservationDAO reservationDAO;
    private final PaymentDAO paymentDAO;
    private final ParkingLotDAO parkingLotDAO;
    
    private JTextField licensePlateField;
    private JComboBox<String> spotIdComboBox;
    private JTextField hoursField;
    private JTable reservationTable;
    private DefaultTableModel tableModel;
    
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final double RESERVED_SPOT_HOURLY_RATE = 10.0; // RM 10 per hour for RESERVED spots

    public ReservationPanel(ParkingLot parkingLot, DatabaseManager dbManager) {
        this.parkingLot = parkingLot;
        this.dbManager = dbManager;
        this.reservationDAO = dbManager != null ? new ReservationDAO(dbManager) : null;
        this.paymentDAO = dbManager != null ? new PaymentDAO(dbManager) : null;
        this.parkingLotDAO = dbManager != null ? new ParkingLotDAO(dbManager) : null;
        
        setLayout(new BorderLayout(10, 10));
        initializeUI();
        loadReservations();
    }

    private void initializeUI() {
        // Top panel - Create reservation
        JPanel createPanel = new JPanel(new GridBagLayout());
        createPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Create Reservation"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // License Plate
        gbc.gridx = 0;
        gbc.gridy = 0;
        createPanel.add(new JLabel("License Plate:"), gbc);
        
        gbc.gridx = 1;
        licensePlateField = new JTextField(15);
        createPanel.add(licensePlateField, gbc);

        // Spot ID
        gbc.gridx = 0;
        gbc.gridy = 1;
        createPanel.add(new JLabel("Reserved Spot ID:"), gbc);
        
        gbc.gridx = 1;
        spotIdComboBox = new JComboBox<>();
        spotIdComboBox.setPreferredSize(new java.awt.Dimension(200, 25));
        createPanel.add(spotIdComboBox, gbc);

        // Duration
        gbc.gridx = 0;
        gbc.gridy = 2;
        createPanel.add(new JLabel("Duration (hours):"), gbc);
        
        gbc.gridx = 1;
        hoursField = new JTextField(15);
        hoursField.setText("24");
        createPanel.add(hoursField, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton createButton = new JButton("Create Reservation");
        createButton.addActionListener(e -> createReservation());
        buttonPanel.add(createButton);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            refreshAvailableSpots();
            loadReservations();
        });
        buttonPanel.add(refreshButton);
        
        createPanel.add(buttonPanel, gbc);

        add(createPanel, BorderLayout.NORTH);

        // Center panel - Reservations table
        String[] columns = {"ID", "License Plate", "Spot ID", "Start Time", "End Time", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reservationTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reservationTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel - Cancel button
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton cancelButton = new JButton("Cancel Selected Reservation");
        cancelButton.addActionListener(e -> cancelReservation());
        bottomPanel.add(cancelButton);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Load available spots initially
        refreshAvailableSpots();
    }

    /**
     * Refreshes the dropdown with available RESERVED spots.
     */
    private void refreshAvailableSpots() {
        spotIdComboBox.removeAllItems();
        
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getAllSpots()) {
                if (spot.getType() == SpotType.RESERVED && spot.isAvailable()) {
                    spotIdComboBox.addItem(spot.getSpotId());
                }
            }
        }
        
        if (spotIdComboBox.getItemCount() == 0) {
            spotIdComboBox.addItem("No available RESERVED spots");
        }
    }

    private void createReservation() {
        if (reservationDAO == null) {
            JOptionPane.showMessageDialog(this, 
                "Database not available. Cannot create reservations.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String licensePlate = licensePlateField.getText().trim().toUpperCase();
        String spotId = (String) spotIdComboBox.getSelectedItem();
        String hoursText = hoursField.getText().trim();

        // Validate inputs
        if (licensePlate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter license plate", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (spotId == null || spotId.equals("No available RESERVED spots")) {
            JOptionPane.showMessageDialog(this, "No available RESERVED spots to reserve", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate spot exists and is RESERVED type
        ParkingSpot spot = findSpotById(spotId);
        if (spot == null) {
            JOptionPane.showMessageDialog(this, "Spot not found: " + spotId, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (spot.getType() != SpotType.RESERVED) {
            JOptionPane.showMessageDialog(this, 
                "Spot " + spotId + " is not a RESERVED spot. Only RESERVED spots can be reserved.\n" +
                "Spot type: " + spot.getType(),
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int hours;
        try {
            hours = Integer.parseInt(hoursText);
            if (hours <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid duration (positive number)", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create reservation
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(hours);
        
        // Calculate prepaid amount
        double prepaidAmount = hours * RESERVED_SPOT_HOURLY_RATE;

        // Show payment dialog
        String[] paymentOptions = {"CASH", "CARD"};
        int paymentChoice = JOptionPane.showOptionDialog(this,
            "Reservation Details:\n\n" +
            "License Plate: " + licensePlate + "\n" +
            "Spot: " + spotId + "\n" +
            "Duration: " + hours + " hours\n" +
            "Start: " + startTime.format(DISPLAY_FORMAT) + "\n" +
            "End: " + endTime.format(DISPLAY_FORMAT) + "\n\n" +
            "PREPAID AMOUNT: RM " + String.format("%.2f", prepaidAmount) + "\n\n" +
            "Please select payment method:",
            "Confirm Payment",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            paymentOptions,
            paymentOptions[0]);

        if (paymentChoice == JOptionPane.CLOSED_OPTION) {
            return; // User cancelled
        }

        PaymentMethod paymentMethod = paymentChoice == 0 ? PaymentMethod.CASH : PaymentMethod.CARD;

        // Check if spot is already reserved during this time
        try {
            if (reservationDAO.isSpotReserved(spotId, startTime, endTime)) {
                JOptionPane.showMessageDialog(this, 
                    "Spot " + spotId + " is already reserved during this time period",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create reservation
            Reservation reservation = new Reservation(licensePlate, spotId, startTime, endTime);
            reservation.setPrepaidAmount(prepaidAmount);
            boolean success = reservationDAO.save(reservation);

            if (success) {
                // Create payment record
                Payment payment = new Payment(licensePlate, prepaidAmount, 0.0, paymentMethod);
                payment.setPaymentDate(LocalDateTime.now());
                
                // Save payment to database
                if (paymentDAO != null) {
                    try {
                        paymentDAO.save(payment);
                    } catch (SQLException e) {
                        System.err.println("Warning: Failed to save payment: " + e.getMessage());
                    }
                }
                
                // Update parking lot revenue
                parkingLot.setTotalRevenue(parkingLot.getTotalRevenue() + prepaidAmount);
                
                // Update revenue in database
                if (parkingLotDAO != null) {
                    try {
                        parkingLotDAO.updateRevenue(parkingLot.getTotalRevenue());
                    } catch (SQLException e) {
                        System.err.println("Warning: Failed to update revenue: " + e.getMessage());
                    }
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Reservation created successfully!\n\n" +
                    "License Plate: " + licensePlate + "\n" +
                    "Spot: " + spotId + "\n" +
                    "Start: " + startTime.format(DISPLAY_FORMAT) + "\n" +
                    "End: " + endTime.format(DISPLAY_FORMAT) + "\n" +
                    "Duration: " + hours + " hours\n" +
                    "Prepaid Amount: RM " + String.format("%.2f", prepaidAmount) + "\n" +
                    "Payment Method: " + paymentMethod + "\n\n" +
                    "Payment received and added to revenue.\n" +
                    "Vehicle can enter/exit multiple times within this period.\n" +
                    "After expiration, RM 100 fine will be charged if parking without new reservation.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Clear fields
                licensePlateField.setText("");
                hoursField.setText("24");
                
                // Refresh dropdown and table
                refreshAvailableSpots();
                loadReservations();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create reservation", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelReservation() {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reservation to cancel", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long reservationId = (Long) tableModel.getValueAt(selectedRow, 0);
        String licensePlate = (String) tableModel.getValueAt(selectedRow, 1);
        String spotId = (String) tableModel.getValueAt(selectedRow, 2);
        String startTimeStr = (String) tableModel.getValueAt(selectedRow, 3);
        String endTimeStr = (String) tableModel.getValueAt(selectedRow, 4);
        String status = (String) tableModel.getValueAt(selectedRow, 5);
        
        // Get the full reservation object to calculate remaining time
        try {
            List<Reservation> allReservations = reservationDAO.findAll();
            Reservation selectedReservation = null;
            for (Reservation r : allReservations) {
                if (r.getId().equals(reservationId)) {
                    selectedReservation = r;
                    break;
                }
            }
            
            if (selectedReservation == null) {
                JOptionPane.showMessageDialog(this, "Reservation not found", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Calculate remaining time
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTime = selectedReservation.getEndTime();
            String remainingTimeMsg;
            
            if (now.isAfter(endTime)) {
                remainingTimeMsg = "EXPIRED";
            } else {
                java.time.Duration remaining = java.time.Duration.between(now, endTime);
                long hours = remaining.toHours();
                long minutes = remaining.toMinutes() % 60;
                remainingTimeMsg = hours + " hours " + minutes + " minutes";
            }
            
            // Show detailed confirmation dialog
            String message = "Are you sure you want to cancel this reservation?\n\n" +
                "License Plate: " + licensePlate + "\n" +
                "Spot: " + spotId + "\n" +
                "Start: " + startTimeStr + "\n" +
                "End: " + endTimeStr + "\n" +
                "Status: " + status + "\n" +
                "Remaining Time: " + remainingTimeMsg + "\n" +
                "Prepaid Amount: RM " + String.format("%.2f", selectedReservation.getPrepaidAmount()) + "\n\n" +
                "WARNING: No refund will be issued for cancelled reservations.";
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                message,
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = reservationDAO.cancel(reservationId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Reservation cancelled successfully", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshAvailableSpots();
                    loadReservations();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel reservation", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadReservations() {
        if (reservationDAO == null) {
            return;
        }

        try {
            List<Reservation> reservations = reservationDAO.findAll();
            tableModel.setRowCount(0);
            
            for (Reservation reservation : reservations) {
                String status = reservation.isActive() ? 
                    (reservation.isCurrentlyValid() ? "ACTIVE" : "EXPIRED") : "CANCELLED";
                
                tableModel.addRow(new Object[]{
                    reservation.getId(),
                    reservation.getLicensePlate(),
                    reservation.getSpotId(),
                    reservation.getStartTime().format(DISPLAY_FORMAT),
                    reservation.getEndTime().format(DISPLAY_FORMAT),
                    status
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading reservations: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ParkingSpot findSpotById(String spotId) {
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getAllSpots()) {
                if (spot.getSpotId().equals(spotId)) {
                    return spot;
                }
            }
        }
        return null;
    }
    
    /**
     * Public method to refresh the panel data.
     * Called when vehicles enter or exit to update available spots.
     */
    public void refreshData() {
        refreshAvailableSpots();
        loadReservations();
    }
}
