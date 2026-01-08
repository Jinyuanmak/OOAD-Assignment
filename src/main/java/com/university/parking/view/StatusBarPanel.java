package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.university.parking.model.ParkingLot;
import com.university.parking.model.ParkingSpot;

/**
 * Status bar panel showing real-time system information.
 * Displays connection status, vehicle count, and occupancy percentage.
 * 
 * Requirements: 7.1, 7.2, 7.3, 7.4
 */
public class StatusBarPanel extends JPanel {
    
    private ParkingLot parkingLot;
    
    private JLabel connectionStatusLabel;
    private JLabel vehicleCountLabel;
    private JLabel occupancyRateLabel;
    
    private Timer updateTimer;
    
    // Status indicator colors
    private static final Color STATUS_CONNECTED = ThemeManager.SUCCESS;
    private static final Color STATUS_DISCONNECTED = ThemeManager.DANGER;
    
    // Update interval in milliseconds
    private static final int UPDATE_INTERVAL = 5000;
    
    // Connection status
    private boolean connected = true;
    
    /**
     * Creates a new StatusBarPanel connected to the specified parking lot.
     * 
     * @param parkingLot the parking lot to monitor
     */
    public StatusBarPanel(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
        initializeComponents();
        setupLayout();
        startUpdateTimer();
        updateStatus();
    }
    
    private void initializeComponents() {
        // Connection status indicator
        connectionStatusLabel = new JLabel("● Connected");
        connectionStatusLabel.setFont(ThemeManager.FONT_SMALL);
        connectionStatusLabel.setForeground(STATUS_CONNECTED);
        
        // Vehicle count display
        vehicleCountLabel = new JLabel("Vehicles: 0");
        vehicleCountLabel.setFont(ThemeManager.FONT_SMALL);
        vehicleCountLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        
        // Occupancy rate display
        occupancyRateLabel = new JLabel("Occupancy: 0.0%");
        occupancyRateLabel.setFont(ThemeManager.FONT_SMALL);
        occupancyRateLabel.setForeground(ThemeManager.TEXT_PRIMARY);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_WHITE);
        setPreferredSize(new Dimension(0, ThemeManager.STATUS_BAR_HEIGHT));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.BG_LIGHT),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        // Left panel with connection status
        JPanel leftPanel = createTransparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.add(connectionStatusLabel);
        
        // Right panel with stats
        JPanel rightPanel = createTransparentPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        rightPanel.add(vehicleCountLabel);
        rightPanel.add(createSeparator());
        rightPanel.add(occupancyRateLabel);
        
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }
    
    private JPanel createTransparentPanel(FlowLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }
    
    private JLabel createSeparator() {
        JLabel separator = new JLabel("|");
        separator.setForeground(ThemeManager.TEXT_SECONDARY);
        return separator;
    }
    
    private void startUpdateTimer() {
        updateTimer = new Timer(UPDATE_INTERVAL, e -> updateStatus());
        updateTimer.start();
    }
    
    /**
     * Updates the status bar with current parking lot data.
     */
    public void updateStatus() {
        if (parkingLot == null) {
            setDisconnected();
            return;
        }
        
        try {
            // Get all spots
            List<ParkingSpot> allSpots = parkingLot.getAllSpots();
            int totalSpots = allSpots.size();
            
            // Count occupied spots (parked vehicles)
            int occupiedSpots = 0;
            for (ParkingSpot spot : allSpots) {
                if (!spot.isAvailable()) {
                    occupiedSpots++;
                }
            }
            
            // Update vehicle count
            vehicleCountLabel.setText("Vehicles: " + occupiedSpots);
            
            // Calculate and update occupancy rate
            double occupancyRate = 0.0;
            if (totalSpots > 0) {
                occupancyRate = (double) occupiedSpots / totalSpots * 100.0;
            }
            occupancyRateLabel.setText(String.format("Occupancy: %.1f%%", occupancyRate));
            
            // Update connection status
            setConnected();
            
        } catch (Exception e) {
            setDisconnected();
        }
    }
    
    /**
     * Sets the connection status to connected.
     */
    public void setConnected() {
        connected = true;
        connectionStatusLabel.setText("● Connected");
        connectionStatusLabel.setForeground(STATUS_CONNECTED);
    }
    
    /**
     * Sets the connection status to disconnected.
     */
    public void setDisconnected() {
        connected = false;
        connectionStatusLabel.setText("● Disconnected");
        connectionStatusLabel.setForeground(STATUS_DISCONNECTED);
    }
    
    /**
     * Checks if the status bar shows connected status.
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Gets the displayed vehicle count.
     * 
     * @return the vehicle count as displayed
     */
    public int getDisplayedVehicleCount() {
        String text = vehicleCountLabel.getText();
        try {
            return Integer.parseInt(text.replace("Vehicles: ", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Gets the displayed occupancy rate.
     * 
     * @return the occupancy rate as displayed (percentage)
     */
    public double getDisplayedOccupancyRate() {
        String text = occupancyRateLabel.getText();
        try {
            return Double.parseDouble(text.replace("Occupancy: ", "").replace("%", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * Gets the actual vehicle count from the parking lot.
     * 
     * @return the actual number of parked vehicles
     */
    public int getActualVehicleCount() {
        if (parkingLot == null) {
            return 0;
        }
        
        int count = 0;
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            if (!spot.isAvailable()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Gets the actual occupancy rate from the parking lot.
     * 
     * @return the actual occupancy rate (percentage)
     */
    public double getActualOccupancyRate() {
        if (parkingLot == null) {
            return 0.0;
        }
        
        List<ParkingSpot> allSpots = parkingLot.getAllSpots();
        int totalSpots = allSpots.size();
        
        if (totalSpots == 0) {
            return 0.0;
        }
        
        int occupiedSpots = 0;
        for (ParkingSpot spot : allSpots) {
            if (!spot.isAvailable()) {
                occupiedSpots++;
            }
        }
        
        return (double) occupiedSpots / totalSpots * 100.0;
    }
    
    /**
     * Stops the update timer.
     * Should be called when the panel is no longer needed.
     */
    public void stopTimer() {
        if (updateTimer != null && updateTimer.isRunning()) {
            updateTimer.stop();
        }
    }
    
    /**
     * Gets the parking lot being monitored.
     * 
     * @return the parking lot
     */
    public ParkingLot getParkingLot() {
        return parkingLot;
    }
    
    /**
     * Sets the parking lot to monitor.
     * 
     * @param parkingLot the parking lot
     */
    public void setParkingLot(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
        updateStatus();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Paint background
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.dispose();
        super.paintComponent(g);
    }
}
