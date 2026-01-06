package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.model.ParkingLot;

/**
 * Main application frame for the University Parking Lot Management System.
 * Implements a tabbed interface with Admin, Entry/Exit, and Reporting panels.
 * 
 * Requirements: 12.1, 12.2
 */
public class MainFrame extends JFrame {
    private static final String TITLE = "University Parking Lot Management System";
    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = 768;

    private JTabbedPane tabbedPane;
    private AdminPanel adminPanel;
    private VehicleEntryPanel entryPanel;
    private VehicleExitPanel exitPanel;
    private ReportingPanel reportingPanel;
    private ParkingLot parkingLot;
    private DatabaseManager dbManager;
    private FineDAO fineDAO;

    public MainFrame(ParkingLot parkingLot) {
        this(parkingLot, null, null);
    }
    
    public MainFrame(ParkingLot parkingLot, DatabaseManager dbManager, FineDAO fineDAO) {
        this.parkingLot = parkingLot;
        this.dbManager = dbManager;
        this.fineDAO = fineDAO;
        initializeLookAndFeel();
        initializeComponents();
        setupFrame();
    }

    /**
     * Initializes the look and feel to system default.
     */
    private void initializeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to default look and feel
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }
    }

    /**
     * Initializes all GUI components.
     * Requirement 12.2: Organize functionality into multiple panels or tabs
     */
    private void initializeComponents() {
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Create panels with database integration
        adminPanel = new AdminPanel(parkingLot, dbManager, fineDAO);
        entryPanel = new VehicleEntryPanel(parkingLot, dbManager, fineDAO);
        exitPanel = new VehicleExitPanel(parkingLot, dbManager, fineDAO);
        reportingPanel = new ReportingPanel(parkingLot, dbManager, fineDAO);

        // Add tabs
        tabbedPane.addTab("Admin Dashboard", createTabIcon(), adminPanel, "Administrative functions");
        tabbedPane.addTab("Vehicle Entry", createTabIcon(), entryPanel, "Process vehicle entries");
        tabbedPane.addTab("Vehicle Exit", createTabIcon(), exitPanel, "Process vehicle exits");
        tabbedPane.addTab("Reports", createTabIcon(), reportingPanel, "Generate reports");

        // Add tabbed pane to frame
        add(tabbedPane, BorderLayout.CENTER);

        // Add status bar
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    /**
     * Creates a simple icon for tabs (placeholder).
     */
    private Icon createTabIcon() {
        return null; // No icons for simplicity
    }

    /**
     * Creates the status bar at the bottom of the frame.
     */
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusLabel = new JLabel("Ready");
        statusBar.add(statusLabel);
        return statusBar;
    }

    /**
     * Sets up the main frame properties.
     */
    private void setupFrame() {
        setTitle(TITLE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
    }

    /**
     * Refreshes all panels with current data.
     */
    public void refreshAllPanels() {
        if (adminPanel != null) {
            adminPanel.refreshData();
        }
        if (entryPanel != null) {
            entryPanel.refreshData();
        }
        if (exitPanel != null) {
            exitPanel.refreshData();
        }
        if (reportingPanel != null) {
            reportingPanel.refreshData();
        }
    }

    // Getters for panels (useful for testing)
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public AdminPanel getAdminPanel() {
        return adminPanel;
    }

    public VehicleEntryPanel getEntryPanel() {
        return entryPanel;
    }

    public VehicleExitPanel getExitPanel() {
        return exitPanel;
    }

    public ReportingPanel getReportingPanel() {
        return reportingPanel;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }
}
