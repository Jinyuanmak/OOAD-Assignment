package com.university.parking.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Sidebar navigation panel with styled buttons and active state tracking.
 * Provides navigation between different sections of the application.
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4
 */
public class SideNavigationPanel extends JPanel {
    
    private List<NavButton> navButtons;
    private NavButton activeButton;
    private ActionListener navigationListener;
    
    // Navigation item identifiers
    public static final String NAV_DASHBOARD = "Dashboard";
    public static final String NAV_ENTRY = "Vehicle Entry";
    public static final String NAV_EXIT = "Vehicle Exit";
    public static final String NAV_REPORTS = "Reports";
    
    /**
     * Creates a new SideNavigationPanel with the specified navigation listener.
     * 
     * @param listener the action listener to handle navigation events
     */
    public SideNavigationPanel(ActionListener listener) {
        this.navigationListener = listener;
        this.navButtons = new ArrayList<>();
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        // Create navigation buttons without icons
        addNavButton(NAV_DASHBOARD, "");
        addNavButton(NAV_ENTRY, "");
        addNavButton(NAV_EXIT, "");
        addNavButton(NAV_REPORTS, "");
        
        // Set first button as active by default
        if (!navButtons.isEmpty()) {
            setActiveButton(0);
        }
    }
    
    private void addNavButton(String text, String icon) {
        NavButton button = new NavButton(text, icon);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setActiveButton(navButtons.indexOf(button));
                if (navigationListener != null) {
                    navigationListener.actionPerformed(
                        new java.awt.event.ActionEvent(button, 
                            java.awt.event.ActionEvent.ACTION_PERFORMED, text));
                }
            }
        });
        navButtons.add(button);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_DARK);
        setPreferredSize(new Dimension(ThemeManager.SIDEBAR_WIDTH, 0));
        
        // Create buttons container
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Add navigation buttons
        for (NavButton button : navButtons) {
            buttonsPanel.add(button);
            buttonsPanel.add(Box.createVerticalStrut(5));
        }
        
        // Add filler to push buttons to top
        buttonsPanel.add(Box.createVerticalGlue());
        
        add(buttonsPanel, BorderLayout.CENTER);
    }
    
    /**
     * Sets the active navigation button by index.
     * 
     * @param index the index of the button to activate
     */
    public void setActiveButton(int index) {
        if (index < 0 || index >= navButtons.size()) {
            return;
        }
        
        // Deactivate current active button
        if (activeButton != null) {
            activeButton.setActive(false);
        }
        
        // Activate new button
        activeButton = navButtons.get(index);
        activeButton.setActive(true);
    }
    
    /**
     * Sets the active navigation button by name.
     * 
     * @param name the name of the button to activate
     */
    public void setActiveButton(String name) {
        for (int i = 0; i < navButtons.size(); i++) {
            if (navButtons.get(i).getText().equals(name)) {
                setActiveButton(i);
                return;
            }
        }
    }
    
    /**
     * Gets the currently active button.
     * 
     * @return the active NavButton, or null if none is active
     */
    public NavButton getActiveButton() {
        return activeButton;
    }
    
    /**
     * Gets all navigation buttons.
     * 
     * @return list of all NavButton instances
     */
    public List<NavButton> getNavButtons() {
        return new ArrayList<>(navButtons);
    }
    
    /**
     * Gets the number of navigation buttons.
     * 
     * @return the count of navigation buttons
     */
    public int getButtonCount() {
        return navButtons.size();
    }
    
    /**
     * Inner class representing a navigation button with icon and label.
     */
    public class NavButton extends JPanel {
        
        private String text;
        private String icon;
        private boolean active;
        private boolean hovered;
        
        private JLabel iconLabel;
        private JLabel textLabel;
        
        // Colors defined as instance fields since non-static inner class
        private final Color ACTIVE_BG = new Color(52, 73, 94);
        private final Color HOVER_BG = new Color(44, 62, 80).brighter();
        private final Color NORMAL_BG = new Color(0, 0, 0, 0); // Transparent
        
        /**
         * Creates a new NavButton with the specified text and icon.
         * 
         * @param text the button label text
         * @param icon the icon character/emoji
         */
        public NavButton(String text, String icon) {
            this.text = text;
            this.icon = icon;
            this.active = false;
            this.hovered = false;
            
            initializeComponents();
            setupLayout();
            setupMouseListeners();
        }
        
        private void initializeComponents() {
            iconLabel = new JLabel(icon);
            iconLabel.setFont(ThemeManager.FONT_HEADER);
            iconLabel.setForeground(ThemeManager.TEXT_LIGHT);
            
            textLabel = new JLabel(text);
            textLabel.setFont(ThemeManager.FONT_BODY);
            textLabel.setForeground(ThemeManager.TEXT_LIGHT);
        }
        
        private void setupLayout() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
            setOpaque(false);
            setMaximumSize(new Dimension(ThemeManager.SIDEBAR_WIDTH, 50));
            setPreferredSize(new Dimension(ThemeManager.SIDEBAR_WIDTH - 10, 45));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            add(iconLabel);
            add(textLabel);
        }
        
        private void setupMouseListeners() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }
        
        /**
         * Sets whether this button is active.
         * 
         * @param active true to activate, false to deactivate
         */
        public void setActive(boolean active) {
            this.active = active;
            repaint();
        }
        
        /**
         * Checks if this button is active.
         * 
         * @return true if active, false otherwise
         */
        public boolean isActive() {
            return active;
        }
        
        /**
         * Checks if this button is being hovered.
         * 
         * @return true if hovered, false otherwise
         */
        public boolean isHovered() {
            return hovered;
        }
        
        /**
         * Gets the button text.
         * 
         * @return the button label text
         */
        public String getText() {
            return text;
        }
        
        /**
         * Gets the current background color based on state.
         * 
         * @return the background color for the current state
         */
        public Color getCurrentBackgroundColor() {
            if (active) {
                return ACTIVE_BG;
            } else if (hovered) {
                return HOVER_BG;
            }
            return NORMAL_BG;
        }
        
        /**
         * Gets the active background color.
         * 
         * @return the color used when button is active
         */
        public Color getActiveBackgroundColor() {
            return ACTIVE_BG;
        }
        
        /**
         * Gets the inactive/normal background color.
         * 
         * @return the color used when button is inactive
         */
        public Color getInactiveBackgroundColor() {
            return NORMAL_BG;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw background based on state
            Color bgColor = getCurrentBackgroundColor();
            if (bgColor.getAlpha() > 0) {
                g2d.setColor(bgColor);
                g2d.fillRoundRect(5, 0, getWidth() - 10, getHeight(), 8, 8);
            }
            
            // Draw active indicator bar
            if (active) {
                g2d.setColor(ThemeManager.PRIMARY_LIGHT);
                g2d.fillRoundRect(0, 5, 4, getHeight() - 10, 2, 2);
            }
            
            g2d.dispose();
            super.paintComponent(g);
        }
    }
}
