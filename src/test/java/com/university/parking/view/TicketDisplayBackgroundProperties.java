package com.university.parking.view;

import java.awt.Color;

import com.university.parking.model.Floor;
import com.university.parking.model.ParkingLot;
import com.university.parking.model.SpotType;

import net.jqwik.api.Property;

/**
 * Property-based tests for Ticket Display Background Distinction.
 * 
 * Feature: gui-modernization, Property 14: Ticket Display Background Distinction
 * Validates: Requirements 10.3
 * 
 * For any ticket display panel, its background color SHALL differ from 
 * its parent panel's background color.
 */
public class TicketDisplayBackgroundProperties {

    /**
     * Creates a test parking lot for testing purposes.
     */
    private ParkingLot createTestParkingLot() {
        ParkingLot parkingLot = new ParkingLot("Test Lot");
        Floor floor = new Floor(1);
        floor.createRow(1, 2, new SpotType[]{SpotType.REGULAR, SpotType.COMPACT});
        parkingLot.addFloor(floor);
        return parkingLot;
    }

    /**
     * Property 14: Ticket Display Background Distinction
     * 
     * For any ModernEntryPanel, the ticket display panel background color 
     * SHALL differ from the standard white panel background color.
     */
    @Property(tries = 100)
    void ticketDisplayBackgroundDiffersFromWhiteBackground() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernEntryPanel entryPanel = new ModernEntryPanel(parkingLot);
        
        Color ticketBackground = entryPanel.getTicketDisplayBackground();
        Color whiteBackground = ThemeManager.BG_WHITE;
        
        assert ticketBackground != null : "Ticket display background must not be null";
        assert !ticketBackground.equals(whiteBackground) : 
            "Ticket display background (" + colorToString(ticketBackground) + 
            ") must differ from white background (" + colorToString(whiteBackground) + ")";
    }
    
    /**
     * Property: Ticket display background differs from light background
     * 
     * The ticket display should also be distinct from the light content background.
     */
    @Property(tries = 100)
    void ticketDisplayBackgroundDiffersFromLightBackground() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernEntryPanel entryPanel = new ModernEntryPanel(parkingLot);
        
        Color ticketBackground = entryPanel.getTicketDisplayBackground();
        Color lightBackground = ThemeManager.BG_LIGHT;
        
        assert ticketBackground != null : "Ticket display background must not be null";
        assert !ticketBackground.equals(lightBackground) : 
            "Ticket display background must differ from light background";
    }
    
    /**
     * Property: Ticket display panel exists and is accessible
     * 
     * The ticket display panel should be properly initialized.
     */
    @Property(tries = 100)
    void ticketDisplayPanelExists() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernEntryPanel entryPanel = new ModernEntryPanel(parkingLot);
        
        assert entryPanel.getTicketDisplayPanel() != null : 
            "Ticket display panel must exist";
    }
    
    /**
     * Property: Ticket display uses card background color
     * 
     * The ticket display should use the BG_CARD color for visual distinction.
     */
    @Property(tries = 100)
    void ticketDisplayUsesCardBackground() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernEntryPanel entryPanel = new ModernEntryPanel(parkingLot);
        
        Color ticketBackground = entryPanel.getTicketDisplayBackground();
        Color cardBackground = ThemeManager.BG_CARD;
        
        assert ticketBackground != null : "Ticket display background must not be null";
        assert ticketBackground.equals(cardBackground) : 
            "Ticket display should use BG_CARD color for distinction. " +
            "Expected: " + colorToString(cardBackground) + 
            ", Actual: " + colorToString(ticketBackground);
    }
    
    /**
     * Property: Ticket area is properly initialized
     * 
     * The ticket text area should be accessible and editable state should be false.
     */
    @Property(tries = 100)
    void ticketAreaIsProperlyInitialized() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernEntryPanel entryPanel = new ModernEntryPanel(parkingLot);
        
        assert entryPanel.getTicketArea() != null : 
            "Ticket area must exist";
        assert !entryPanel.getTicketArea().isEditable() : 
            "Ticket area should not be editable";
    }
    
    /**
     * Property: Ticket display has visible border
     * 
     * The ticket display panel should have a border for visual separation.
     */
    @Property(tries = 100)
    void ticketDisplayHasBorder() {
        ParkingLot parkingLot = createTestParkingLot();
        ModernEntryPanel entryPanel = new ModernEntryPanel(parkingLot);
        
        assert entryPanel.getTicketDisplayPanel().getBorder() != null : 
            "Ticket display panel should have a border";
    }
    
    /**
     * Helper method to convert color to readable string.
     */
    private String colorToString(Color color) {
        return String.format("RGB(%d, %d, %d)", 
            color.getRed(), color.getGreen(), color.getBlue());
    }
}
