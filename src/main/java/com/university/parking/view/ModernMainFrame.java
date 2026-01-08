package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.university.parking.dao.DatabaseManager;
import com.university.parking.dao.FineDAO;
import com.university.parking.model.ParkingLot;

/**
 * Modern main application frame with sidebar navigation and modern styling.
 * Uses BorderLayout with header (NORTH), sidebar (WEST), content (CENTER), status (SOUTH).
 * Content panel uses CardLayout for switching between different views.
 * 
 * Requirements: 8.1, 8.2, 8.3, 8.4
 */
public class ModernMainFrame extends JFrame {
    
    private static final String TITLE = "University Parking Management System";
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;
    private static final int MIN_WIDTH = 900;
    private static final int MIN_HEIGHT = 600;
    
    // Layout components
    private HeaderPanel headerPanel;
    private SideNavigationPanel sideNavPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private StatusBarPanel statusBarPanel;
    
    // Content panels
    private AdminPanel adminPanel;
    private VehicleEntryPanel entryPanel;
    private VehicleExitPanel exitPanel;
    private ReportingPanel reportingPanel;
    
    // Data
    private final ParkingLot parkingLot;
    private final DatabaseManager dbManager;
    private final FineDAO fineDAO;
    
    // Card names for CardLayout
    public static final String CARD_DASHBOARD = SideNavigationPanel.NAV_DASHBOARD;
    public static final String CARD_ENTRY = SideNavigationPanel.NAV_ENTRY;
    public static final String CARD_EXIT = SideNavigationPanel.NAV_EXIT;
    public static final String CARD_REPORTS = SideNavigationPanel.NAV_REPORTS;
    
    /**
     * Creates a new ModernMainFrame with the specified parking lot.
     * 
     * @param parkingLot the parking lot to manage
     */
    public ModernMainFrame(ParkingLot parkingLot) {
        this(parkingLot, null, null);
    }
    
    /**
     * Creates a new ModernMainFrame with the specified parking lot and database components.
     * 
     * @param parkingLot the parking lot to manage
     * @param dbManager the database manager
     * @param fineDAO the fine data access object
     */
    public ModernMainFrame(ParkingLot parkingLot, DatabaseManager dbManager, FineDAO fineDAO) {
        this.parkingLot = parkingLot;
        this.dbManager = dbManager;
        this.fineDAO = fineDAO;
        
        initializeLookAndFeel();
        initializeComponents();
        setupLayout();
        setupFrame();
        wireNavigation();
        setupResponsiveBehavior();
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
     */
    private void initializeComponents() {
        // Create header panel
        headerPanel = new HeaderPanel();
        
        // Create side navigation panel with navigation listener
        sideNavPanel = new SideNavigationPanel(this::handleNavigation);
        
        // Create content panel with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(ThemeManager.BG_LIGHT);
        
        // Create content panels
        adminPanel = new AdminPanel(parkingLot, dbManager, fineDAO);
        entryPanel = new VehicleEntryPanel(parkingLot, dbManager, fineDAO);
        exitPanel = new VehicleExitPanel(parkingLot, dbManager, fineDAO);
        reportingPanel = new ReportingPanel(parkingLot, dbManager, fineDAO);
        
        // Add panels to card layout
        contentPanel.add(adminPanel, CARD_DASHBOARD);
        contentPanel.add(entryPanel, CARD_ENTRY);
        contentPanel.add(exitPanel, CARD_EXIT);
        contentPanel.add(reportingPanel, CARD_REPORTS);
        
        // Create status bar panel
        statusBarPanel = new StatusBarPanel(parkingLot);
    }
    
    /**
     * Sets up the main layout using BorderLayout.
     * Header at NORTH, sidebar at WEST, content at CENTER, status bar at SOUTH.
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Add header to NORTH
        add(headerPanel, BorderLayout.NORTH);
        
        // Add sidebar to WEST
        add(sideNavPanel, BorderLayout.WEST);
        
        // Add content panel to CENTER
        add(contentPanel, BorderLayout.CENTER);
        
        // Add status bar to SOUTH
        add(statusBarPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Sets up the main frame properties.
     */
    private void setupFrame() {
        setTitle(TITLE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
    }
    
    /**
     * Wires navigation buttons to panel switching.
     */
    private void wireNavigation() {
        // Show dashboard by default
        showPanel(CARD_DASHBOARD);
    }
    
    /**
     * Sets up responsive behavior for window resizing.
     */
    private void setupResponsiveBehavior() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                handleResize();
            }
        });
    }
    
    /**
     * Handles navigation button clicks.
     * 
     * @param e the action event from the navigation button
     */
    private void handleNavigation(ActionEvent e) {
        String command = e.getActionCommand();
        showPanel(command);
        
        // Refresh the panel being shown
        refreshCurrentPanel(command);
    }

    /**
     * Shows the specified panel in the content area.
     * 
     * @param panelName the name of the panel to show
     */
    public void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
        sideNavPanel.setActiveButton(panelName);
    }
    
    /**
     * Refreshes the specified panel.
     * 
     * @param panelName the name of the panel to refresh
     */
    private void refreshCurrentPanel(String panelName) {
        switch (panelName) {
            case CARD_DASHBOARD:
                if (adminPanel != null) {
                    adminPanel.refreshData();
                }
                break;
            case CARD_ENTRY:
                if (entryPanel != null) {
                    entryPanel.refreshData();
                }
                break;
            case CARD_EXIT:
                if (exitPanel != null) {
                    exitPanel.refreshData();
                }
                break;
            case CARD_REPORTS:
                if (reportingPanel != null) {
                    reportingPanel.refreshData();
                }
                break;
            default:
                // Unknown panel, do nothing
                break;
        }
        
        // Also update status bar
        if (statusBarPanel != null) {
            statusBarPanel.updateStatus();
        }
    }
    
    /**
     * Handles window resize events.
     * Ensures components adjust proportionally.
     */
    private void handleResize() {
        // Revalidate and repaint to ensure proper layout
        revalidate();
        repaint();
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
        if (statusBarPanel != null) {
            statusBarPanel.updateStatus();
        }
    }
    
    /**
     * Stops all timers when the frame is being disposed.
     * Should be called before disposing the frame.
     */
    public void cleanup() {
        if (headerPanel != null) {
            headerPanel.stopTimer();
        }
        if (statusBarPanel != null) {
            statusBarPanel.stopTimer();
        }
    }
    
    @Override
    public void dispose() {
        cleanup();
        super.dispose();
    }
    
    // ==================== Getters for testing ====================
    
    /**
     * Gets the header panel.
     * 
     * @return the header panel
     */
    public HeaderPanel getHeaderPanel() {
        return headerPanel;
    }
    
    /**
     * Gets the side navigation panel.
     * 
     * @return the side navigation panel
     */
    public SideNavigationPanel getSideNavPanel() {
        return sideNavPanel;
    }
    
    /**
     * Gets the content panel.
     * 
     * @return the content panel with CardLayout
     */
    public JPanel getContentPanel() {
        return contentPanel;
    }
    
    /**
     * Gets the CardLayout used for content switching.
     * 
     * @return the card layout
     */
    public CardLayout getCardLayout() {
        return cardLayout;
    }
    
    /**
     * Gets the status bar panel.
     * 
     * @return the status bar panel
     */
    public StatusBarPanel getStatusBarPanel() {
        return statusBarPanel;
    }
    
    /**
     * Gets the admin panel.
     * 
     * @return the admin panel
     */
    public AdminPanel getAdminPanel() {
        return adminPanel;
    }
    
    /**
     * Gets the vehicle entry panel.
     * 
     * @return the vehicle entry panel
     */
    public VehicleEntryPanel getEntryPanel() {
        return entryPanel;
    }
    
    /**
     * Gets the vehicle exit panel.
     * 
     * @return the vehicle exit panel
     */
    public VehicleExitPanel getExitPanel() {
        return exitPanel;
    }
    
    /**
     * Gets the reporting panel.
     * 
     * @return the reporting panel
     */
    public ReportingPanel getReportingPanel() {
        return reportingPanel;
    }
    
    /**
     * Gets the parking lot.
     * 
     * @return the parking lot
     */
    public ParkingLot getParkingLot() {
        return parkingLot;
    }
    
    /**
     * Gets the minimum frame width.
     * 
     * @return the minimum width in pixels
     */
    public int getMinFrameWidth() {
        return MIN_WIDTH;
    }
    
    /**
     * Gets the minimum frame height.
     * 
     * @return the minimum height in pixels
     */
    public int getMinFrameHeight() {
        return MIN_HEIGHT;
    }
    
    /**
     * Gets the default frame width.
     * 
     * @return the default width in pixels
     */
    public int getDefaultFrameWidth() {
        return DEFAULT_WIDTH;
    }
    
    /**
     * Gets the default frame height.
     * 
     * @return the default height in pixels
     */
    public int getDefaultFrameHeight() {
        return DEFAULT_HEIGHT;
    }
    
    /**
     * Gets the current content panel width.
     * 
     * @return the content panel width in pixels
     */
    public int getContentPanelWidth() {
        return contentPanel.getWidth();
    }
    
    /**
     * Gets the current content panel height.
     * 
     * @return the content panel height in pixels
     */
    public int getContentPanelHeight() {
        return contentPanel.getHeight();
    }
}
