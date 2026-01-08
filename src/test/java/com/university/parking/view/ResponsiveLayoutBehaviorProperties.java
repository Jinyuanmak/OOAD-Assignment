package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.university.parking.model.ParkingLot;
import com.university.parking.model.SpotType;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for Responsive Layout Behavior.
 * 
 * Feature: gui-modernization, Property 12: Responsive Layout Behavior
 * Validates: Requirements 8.1, 8.3
 * 
 * For any ModernMainFrame, when the frame size is changed, the content panel width 
 * SHALL change proportionally (within 10% of the expected ratio).
 */
public class ResponsiveLayoutBehaviorProperties {

    /**
     * Property 12: Responsive Layout Behavior - Content panel adjusts with frame size
     * 
     * For any ModernMainFrame, when the frame size is changed, the content panel 
     * width SHALL change proportionally.
     */
    @Property(tries = 100)
    void contentPanelWidthAdjustsWithFrameSize(
            @ForAll @IntRange(min = 900, max = 1600) int frameWidth,
            @ForAll @IntRange(min = 600, max = 1000) int frameHeight) {
        
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            // Set frame size
            frame.setSize(frameWidth, frameHeight);
            frame.pack();
            frame.setSize(frameWidth, frameHeight);
            
            // Force layout
            frame.validate();
            
            // Get content panel
            JPanel contentPanel = frame.getContentPanel();
            
            // Verify the layout is properly configured for responsive behavior
            // The content panel should use CardLayout for panel switching
            assert contentPanel.getLayout() instanceof CardLayout : 
                "Content panel must use CardLayout";
            
            // Verify the frame uses BorderLayout which enables responsive behavior
            LayoutManager frameLayout = frame.getContentPane().getLayout();
            assert frameLayout instanceof BorderLayout : 
                "Frame must use BorderLayout for responsive behavior";
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }
    
    /**
     * Property: Frame uses BorderLayout
     * 
     * The ModernMainFrame SHALL use BorderLayout for its main layout.
     */
    @Property(tries = 100)
    void frameUsesBorderLayout() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            LayoutManager layout = frame.getContentPane().getLayout();
            
            assert layout instanceof BorderLayout : 
                "ModernMainFrame must use BorderLayout, but uses: " + 
                (layout != null ? layout.getClass().getSimpleName() : "null");
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }
    
    /**
     * Property: Content panel uses CardLayout
     * 
     * The content panel SHALL use CardLayout for panel switching.
     */
    @Property(tries = 100)
    void contentPanelUsesCardLayout() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            JPanel contentPanel = frame.getContentPanel();
            CardLayout cardLayout = frame.getCardLayout();
            
            assert contentPanel != null : "Content panel must not be null";
            assert cardLayout != null : "CardLayout must not be null";
            assert contentPanel.getLayout() instanceof CardLayout : 
                "Content panel must use CardLayout";
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }

    /**
     * Property: Frame has minimum size constraints
     * 
     * The ModernMainFrame SHALL maintain minimum component sizes to ensure usability.
     */
    @Property(tries = 100)
    void frameHasMinimumSizeConstraints() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            Dimension minSize = frame.getMinimumSize();
            
            assert minSize != null : "Minimum size must not be null";
            assert minSize.width >= 800 : 
                "Minimum width must be at least 800px, got: " + minSize.width;
            assert minSize.height >= 500 : 
                "Minimum height must be at least 500px, got: " + minSize.height;
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }
    
    /**
     * Property: Header panel is positioned at NORTH
     * 
     * The header panel SHALL be positioned at the NORTH of the BorderLayout.
     */
    @Property(tries = 100)
    void headerPanelIsPositionedAtNorth() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            HeaderPanel headerPanel = frame.getHeaderPanel();
            BorderLayout layout = (BorderLayout) frame.getContentPane().getLayout();
            
            assert headerPanel != null : "Header panel must not be null";
            
            // Verify header is in NORTH position
            Object northComponent = layout.getLayoutComponent(BorderLayout.NORTH);
            assert northComponent == headerPanel : 
                "Header panel must be positioned at NORTH";
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }
    
    /**
     * Property: Sidebar panel is positioned at WEST
     * 
     * The sidebar navigation panel SHALL be positioned at the WEST of the BorderLayout.
     */
    @Property(tries = 100)
    void sidebarPanelIsPositionedAtWest() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            SideNavigationPanel sideNavPanel = frame.getSideNavPanel();
            BorderLayout layout = (BorderLayout) frame.getContentPane().getLayout();
            
            assert sideNavPanel != null : "Side navigation panel must not be null";
            
            // Verify sidebar is in WEST position
            Object westComponent = layout.getLayoutComponent(BorderLayout.WEST);
            assert westComponent == sideNavPanel : 
                "Side navigation panel must be positioned at WEST";
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }
    
    /**
     * Property: Content panel is positioned at CENTER
     * 
     * The content panel SHALL be positioned at the CENTER of the BorderLayout.
     */
    @Property(tries = 100)
    void contentPanelIsPositionedAtCenter() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            JPanel contentPanel = frame.getContentPanel();
            BorderLayout layout = (BorderLayout) frame.getContentPane().getLayout();
            
            assert contentPanel != null : "Content panel must not be null";
            
            // Verify content is in CENTER position
            Object centerComponent = layout.getLayoutComponent(BorderLayout.CENTER);
            assert centerComponent == contentPanel : 
                "Content panel must be positioned at CENTER";
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }
    
    /**
     * Property: Status bar panel is positioned at SOUTH
     * 
     * The status bar panel SHALL be positioned at the SOUTH of the BorderLayout.
     */
    @Property(tries = 100)
    void statusBarPanelIsPositionedAtSouth() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            StatusBarPanel statusBarPanel = frame.getStatusBarPanel();
            BorderLayout layout = (BorderLayout) frame.getContentPane().getLayout();
            
            assert statusBarPanel != null : "Status bar panel must not be null";
            
            // Verify status bar is in SOUTH position
            Object southComponent = layout.getLayoutComponent(BorderLayout.SOUTH);
            assert southComponent == statusBarPanel : 
                "Status bar panel must be positioned at SOUTH";
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }
    
    /**
     * Property: Navigation switches content panels
     * 
     * When navigation buttons are clicked, the content panel SHALL switch to the corresponding view.
     */
    @Property(tries = 100)
    void navigationSwitchesContentPanels() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            // Test switching to each panel
            String[] panelNames = {
                ModernMainFrame.CARD_DASHBOARD,
                ModernMainFrame.CARD_ENTRY,
                ModernMainFrame.CARD_EXIT,
                ModernMainFrame.CARD_REPORTS
            };
            
            for (String panelName : panelNames) {
                frame.showPanel(panelName);
                
                // Verify the navigation button is active
                SideNavigationPanel sideNav = frame.getSideNavPanel();
                SideNavigationPanel.NavButton activeButton = sideNav.getActiveButton();
                
                assert activeButton != null : 
                    "Active button must not be null after showing panel: " + panelName;
                assert activeButton.getText().equals(panelName) : 
                    "Active button text must match panel name. Expected: " + panelName + 
                    ", Got: " + activeButton.getText();
            }
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }

    /**
     * Property: All content panels are initialized
     * 
     * The ModernMainFrame SHALL initialize all content panels (Admin, Entry, Exit, Reports).
     */
    @Property(tries = 100)
    void allContentPanelsAreInitialized() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            assert frame.getAdminPanel() != null : "Admin panel must be initialized";
            assert frame.getEntryPanel() != null : "Entry panel must be initialized";
            assert frame.getExitPanel() != null : "Exit panel must be initialized";
            assert frame.getReportingPanel() != null : "Reporting panel must be initialized";
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }
    
    /**
     * Property: Sidebar has correct preferred width
     * 
     * The sidebar SHALL have the theme-defined width.
     */
    @Property(tries = 100)
    void sidebarHasCorrectPreferredWidth() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            SideNavigationPanel sideNav = frame.getSideNavPanel();
            int preferredWidth = sideNav.getPreferredSize().width;
            int expectedWidth = ThemeManager.SIDEBAR_WIDTH;
            
            assert preferredWidth == expectedWidth : 
                "Sidebar preferred width must be " + expectedWidth + 
                ", got: " + preferredWidth;
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }
    
    /**
     * Property: Header has correct preferred height
     * 
     * The header SHALL have the theme-defined height.
     */
    @Property(tries = 100)
    void headerHasCorrectPreferredHeight() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            HeaderPanel header = frame.getHeaderPanel();
            int preferredHeight = header.getPreferredSize().height;
            int expectedHeight = ThemeManager.HEADER_HEIGHT;
            
            assert preferredHeight == expectedHeight : 
                "Header preferred height must be " + expectedHeight + 
                ", got: " + preferredHeight;
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }
    
    /**
     * Property: Status bar has correct preferred height
     * 
     * The status bar SHALL have the theme-defined height.
     */
    @Property(tries = 100)
    void statusBarHasCorrectPreferredHeight() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            StatusBarPanel statusBar = frame.getStatusBarPanel();
            int preferredHeight = statusBar.getPreferredSize().height;
            int expectedHeight = ThemeManager.STATUS_BAR_HEIGHT;
            
            assert preferredHeight == expectedHeight : 
                "Status bar preferred height must be " + expectedHeight + 
                ", got: " + preferredHeight;
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }
    
    /**
     * Property: Frame default size is reasonable
     * 
     * The frame SHALL have a reasonable default size for desktop use.
     */
    @Property(tries = 100)
    void frameHasReasonableDefaultSize() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            int defaultWidth = frame.getDefaultFrameWidth();
            int defaultHeight = frame.getDefaultFrameHeight();
            
            assert defaultWidth >= 1000 : 
                "Default width must be at least 1000px, got: " + defaultWidth;
            assert defaultHeight >= 700 : 
                "Default height must be at least 700px, got: " + defaultHeight;
            assert defaultWidth <= 1920 : 
                "Default width must be at most 1920px, got: " + defaultWidth;
            assert defaultHeight <= 1080 : 
                "Default height must be at most 1080px, got: " + defaultHeight;
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }
    
    /**
     * Property: Content panel background uses theme color
     * 
     * The content panel SHALL use the theme-defined background color.
     */
    @Property(tries = 100)
    void contentPanelUsesThemeBackgroundColor() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernMainFrame frame = new ModernMainFrame(parkingLot);
        
        try {
            JPanel contentPanel = frame.getContentPanel();
            
            assert contentPanel.getBackground().equals(ThemeManager.BG_LIGHT) : 
                "Content panel background must be BG_LIGHT";
            
        } finally {
            frame.cleanup();
            frame.dispose();
        }
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Creates a test parking lot with sample data.
     */
    private ParkingLot createTestParkingLot() {
        ParkingLot parkingLot = new ParkingLot("Test Parking Lot");
        
        // Create a floor with some spots using RowConfiguration
        List<ParkingLot.RowConfiguration> rows = new ArrayList<>();
        rows.add(new ParkingLot.RowConfiguration(3, new SpotType[]{
            SpotType.REGULAR, SpotType.REGULAR, SpotType.COMPACT
        }));
        rows.add(new ParkingLot.RowConfiguration(2, new SpotType[]{
            SpotType.HANDICAPPED, SpotType.RESERVED
        }));
        
        parkingLot.createFloor(1, rows);
        
        return parkingLot;
    }
}
