package com.university.parking.view;

import java.awt.Color;
import java.util.List;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for Navigation Button Active State.
 * 
 * Feature: gui-modernization, Property 2: Navigation Button Active State
 * Validates: Requirements 2.3
 * 
 * For any SideNavigationPanel with multiple navigation buttons, when a button 
 * is clicked, that button's background color SHALL change to indicate active 
 * state, and all other buttons SHALL have the inactive background color.
 */
public class NavigationActiveStateProperties {

    /**
     * Property 2: Navigation Button Active State
     * 
     * For any SideNavigationPanel, when a button is set as active,
     * only that button should have the active background color.
     */
    @Property(tries = 100)
    void onlyOneButtonIsActiveAtATime(@ForAll @IntRange(min = 0, max = 3) int activeIndex) {
        SideNavigationPanel navPanel = new SideNavigationPanel(null);
        
        // Set the button at activeIndex as active
        navPanel.setActiveButton(activeIndex);
        
        List<SideNavigationPanel.NavButton> buttons = navPanel.getNavButtons();
        
        // Verify only the selected button is active
        for (int i = 0; i < buttons.size(); i++) {
            SideNavigationPanel.NavButton button = buttons.get(i);
            if (i == activeIndex) {
                assert button.isActive() : 
                    "Button at index " + i + " should be active";
            } else {
                assert !button.isActive() : 
                    "Button at index " + i + " should NOT be active when button " + 
                    activeIndex + " is selected";
            }
        }
    }
    
    /**
     * Property: Active button has distinct background color
     * 
     * The active button should have a different background than inactive buttons.
     */
    @Property(tries = 100)
    void activeButtonHasDistinctBackgroundColor(@ForAll @IntRange(min = 0, max = 3) int activeIndex) {
        SideNavigationPanel navPanel = new SideNavigationPanel(null);
        navPanel.setActiveButton(activeIndex);
        
        List<SideNavigationPanel.NavButton> buttons = navPanel.getNavButtons();
        SideNavigationPanel.NavButton activeButton = buttons.get(activeIndex);
        
        Color activeColor = activeButton.getCurrentBackgroundColor();
        Color inactiveColor = activeButton.getInactiveBackgroundColor();
        
        assert !activeColor.equals(inactiveColor) : 
            "Active button background color should differ from inactive color";
    }
    
    /**
     * Property: Inactive buttons have consistent background color
     * 
     * All inactive buttons should have the same background color.
     */
    @Property(tries = 100)
    void inactiveButtonsHaveConsistentBackgroundColor(@ForAll @IntRange(min = 0, max = 3) int activeIndex) {
        SideNavigationPanel navPanel = new SideNavigationPanel(null);
        navPanel.setActiveButton(activeIndex);
        
        List<SideNavigationPanel.NavButton> buttons = navPanel.getNavButtons();
        
        for (int i = 0; i < buttons.size(); i++) {
            if (i != activeIndex) {
                SideNavigationPanel.NavButton button = buttons.get(i);
                Color buttonColor = button.getCurrentBackgroundColor();
                Color expectedInactiveColor = button.getInactiveBackgroundColor();
                assert buttonColor.equals(expectedInactiveColor) : 
                    "Inactive button at index " + i + " should have inactive background color";
            }
        }
    }
    
    /**
     * Property: Changing active button updates states correctly
     * 
     * When changing from one active button to another, states should update.
     */
    @Property(tries = 100)
    void changingActiveButtonUpdatesStates(
            @ForAll @IntRange(min = 0, max = 3) int firstIndex,
            @ForAll @IntRange(min = 0, max = 3) int secondIndex) {
        
        SideNavigationPanel navPanel = new SideNavigationPanel(null);
        
        // Set first button active
        navPanel.setActiveButton(firstIndex);
        
        // Set second button active
        navPanel.setActiveButton(secondIndex);
        
        List<SideNavigationPanel.NavButton> buttons = navPanel.getNavButtons();
        
        // Verify only the second button is now active
        for (int i = 0; i < buttons.size(); i++) {
            SideNavigationPanel.NavButton button = buttons.get(i);
            if (i == secondIndex) {
                assert button.isActive() : 
                    "Button at index " + i + " should be active after change";
            } else {
                assert !button.isActive() : 
                    "Button at index " + i + " should NOT be active after change to " + secondIndex;
            }
        }
    }
    
    /**
     * Property: getActiveButton returns the correct button
     * 
     * The getActiveButton method should return the currently active button.
     */
    @Property(tries = 100)
    void getActiveButtonReturnsCorrectButton(@ForAll @IntRange(min = 0, max = 3) int activeIndex) {
        SideNavigationPanel navPanel = new SideNavigationPanel(null);
        navPanel.setActiveButton(activeIndex);
        
        SideNavigationPanel.NavButton activeButton = navPanel.getActiveButton();
        List<SideNavigationPanel.NavButton> buttons = navPanel.getNavButtons();
        
        assert activeButton != null : "Active button should not be null";
        assert activeButton == buttons.get(activeIndex) : 
            "getActiveButton should return the button at index " + activeIndex;
    }
    
    /**
     * Property: Navigation panel has expected number of buttons
     * 
     * The panel should have 4 navigation buttons (Dashboard, Entry, Exit, Reports).
     */
    @Property(tries = 100)
    void navigationPanelHasExpectedButtonCount() {
        SideNavigationPanel navPanel = new SideNavigationPanel(null);
        
        int buttonCount = navPanel.getButtonCount();
        
        assert buttonCount == 4 : 
            "Navigation panel should have 4 buttons, but has " + buttonCount;
    }
    
    /**
     * Property: Each button has non-empty text
     * 
     * All navigation buttons should have meaningful labels.
     */
    @Property(tries = 100)
    void allButtonsHaveNonEmptyText() {
        SideNavigationPanel navPanel = new SideNavigationPanel(null);
        
        List<SideNavigationPanel.NavButton> buttons = navPanel.getNavButtons();
        
        for (int i = 0; i < buttons.size(); i++) {
            String text = buttons.get(i).getText();
            assert text != null : "Button " + i + " text should not be null";
            assert !text.trim().isEmpty() : "Button " + i + " text should not be empty";
        }
    }
    
    /**
     * Property: Setting active by name works correctly
     * 
     * Setting active button by name should work the same as by index.
     */
    @Property(tries = 100)
    void setActiveByNameWorksCorrectly() {
        SideNavigationPanel navPanel = new SideNavigationPanel(null);
        
        String[] navNames = {
            SideNavigationPanel.NAV_DASHBOARD,
            SideNavigationPanel.NAV_ENTRY,
            SideNavigationPanel.NAV_EXIT,
            SideNavigationPanel.NAV_REPORTS
        };
        
        for (int i = 0; i < navNames.length; i++) {
            navPanel.setActiveButton(navNames[i]);
            
            SideNavigationPanel.NavButton activeButton = navPanel.getActiveButton();
            assert activeButton != null : "Active button should not be null";
            assert activeButton.getText().equals(navNames[i]) : 
                "Active button text should be " + navNames[i] + " but was " + activeButton.getText();
        }
    }
    
    /**
     * Property: Panel uses correct sidebar width
     * 
     * The navigation panel should use the theme-defined sidebar width.
     */
    @Property(tries = 100)
    void panelUsesCorrectSidebarWidth() {
        SideNavigationPanel navPanel = new SideNavigationPanel(null);
        
        int preferredWidth = navPanel.getPreferredSize().width;
        int expectedWidth = ThemeManager.SIDEBAR_WIDTH;
        
        assert preferredWidth == expectedWidth : 
            "Panel preferred width should be " + expectedWidth + " but was " + preferredWidth;
    }
}
