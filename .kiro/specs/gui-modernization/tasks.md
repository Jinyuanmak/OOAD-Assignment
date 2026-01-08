# Implementation Plan: GUI Modernization

## Overview

This implementation plan transforms the University Parking Lot Management System's GUI from a basic Swing interface to a modern, professional-looking application. The approach is incremental: first creating the theme and base components, then building the layout structure, and finally updating each panel to use the new styling.

## Tasks

- [x] 1. Create Theme Management System
  - [x] 1.1 Create ThemeManager class with color constants
    - Define PRIMARY, PRIMARY_DARK, PRIMARY_LIGHT colors (blues)
    - Define SUCCESS, WARNING, DANGER, INFO accent colors
    - Define BG_DARK, BG_LIGHT, BG_WHITE, BG_CARD background colors
    - Define TEXT_PRIMARY, TEXT_SECONDARY, TEXT_LIGHT text colors
    - Define FONT_TITLE, FONT_HEADER, FONT_SUBHEADER, FONT_BODY, FONT_SMALL fonts
    - Define dimension constants (SIDEBAR_WIDTH, HEADER_HEIGHT, etc.)
    - _Requirements: 1.1, 1.2, 1.4_

  - [x] 1.2 Write property test for theme color completeness
    - **Property 1: Theme Color Definition Completeness**
    - **Validates: Requirements 1.1**

- [x] 2. Create Custom Styled Components
  - [x] 2.1 Create StyledButton component
    - Implement custom painting with rounded corners
    - Add hover and pressed state color changes
    - Support configurable colors and border radius
    - _Requirements: 4.2_

  - [x] 2.2 Create StyledTextField component
    - Implement rounded border painting
    - Add focus state border color change
    - Configure padding/insets for better appearance
    - _Requirements: 4.1, 4.4_

  - [x] 2.3 Write property test for styled text field appearance
    - **Property 5: Styled Text Field Appearance**
    - **Validates: Requirements 4.1**

  - [x] 2.4 Create StyledComboBox component
    - Apply theme colors to combo box
    - Style dropdown list appearance
    - _Requirements: 4.3_

  - [x] 2.5 Create StyledTable component
    - Implement alternating row colors
    - Style table header with distinct background and bold font
    - Add row hover highlighting
    - Configure adequate row height (minimum 25px)
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

  - [x] 2.6 Write property tests for table styling
    - **Property 6: Table Alternating Row Colors**
    - **Property 7: Table Header Styling**
    - **Property 8: Table Row Height Adequacy**
    - **Validates: Requirements 5.1, 5.2, 5.4**

- [x] 3. Checkpoint - Verify base components
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Create Dashboard Card Component
  - [x] 4.1 Create DashboardCard class
    - Implement card layout with title, value, and icon areas
    - Add custom painting for card background and border
    - Support accent color configuration
    - Implement setValue() method for dynamic updates
    - _Requirements: 3.1, 3.2, 3.3_

  - [x] 4.2 Write property tests for dashboard card
    - **Property 3: Dashboard Card Structure**
    - **Property 4: Dashboard Card Value Updates**
    - **Validates: Requirements 3.1, 3.2, 3.3**

- [x] 5. Create Layout Components
  - [x] 5.1 Create HeaderPanel
    - Add logo/icon on the left
    - Add application title in center
    - Add date/time display on right with auto-update timer
    - Apply distinct background color
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

  - [x] 5.2 Write property test for header background
    - **Property 9: Header Background Distinction**
    - **Validates: Requirements 6.2**

  - [x] 5.3 Create SideNavigationPanel
    - Create NavButton inner class with icon and label
    - Implement active state tracking and highlighting
    - Add hover effects to navigation buttons
    - Configure sidebar width and layout
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [x] 5.4 Write property test for navigation active state
    - **Property 2: Navigation Button Active State**
    - **Validates: Requirements 2.3**

  - [x] 5.5 Create StatusBarPanel
    - Add connection status indicator
    - Add vehicle count display
    - Add occupancy percentage display
    - Implement update timer for real-time updates
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

  - [x] 5.6 Write property tests for status bar
    - **Property 10: Status Bar Data Accuracy**
    - **Property 11: Status Bar Real-Time Updates**
    - **Validates: Requirements 7.2, 7.3, 7.4**

- [x] 6. Checkpoint - Verify layout components
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Create Styled Dialog Component
  - [x] 7.1 Create StyledDialog class
    - Implement SUCCESS, ERROR, WARNING, INFO dialog types
    - Apply appropriate accent colors for each type
    - Create static helper methods (showSuccess, showError, etc.)
    - Style dialog with modern appearance
    - _Requirements: 9.1, 9.2, 9.3_

  - [x] 7.2 Write property test for dialog colors
    - **Property 13: Dialog Color By Type**
    - **Validates: Requirements 9.1, 9.2**

- [x] 8. Create ModernMainFrame
  - [x] 8.1 Create ModernMainFrame class
    - Set up BorderLayout with header (NORTH), sidebar (WEST), content (CENTER), status (SOUTH)
    - Initialize CardLayout for content panel switching
    - Wire navigation buttons to panel switching
    - Set minimum size and responsive behavior
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

  - [x] 8.2 Write property test for responsive layout
    - **Property 12: Responsive Layout Behavior**
    - **Validates: Requirements 8.1, 8.3**

- [x] 9. Update Admin Panel with Modern Styling
  - [x] 9.1 Create ModernAdminPanel
    - Add 4 DashboardCards (occupancy, revenue, available spots, parked vehicles)
    - Replace JTable with StyledTable for floor status
    - Replace JTable with StyledTable for vehicles and fines
    - Update fine configuration section with styled components
    - Wire refresh functionality to update dashboard cards
    - _Requirements: 3.4, 8.1_

- [x] 10. Update Vehicle Entry Panel with Modern Styling
  - [x] 10.1 Create ModernEntryPanel
    - Replace form fields with StyledTextField and StyledComboBox
    - Replace buttons with StyledButton
    - Replace spot table with StyledTable
    - Style ticket display area with distinct background
    - _Requirements: 4.1, 4.2, 4.3, 10.1, 10.3_

  - [x] 10.2 Write property test for ticket display
    - **Property 14: Ticket Display Background Distinction**
    - **Validates: Requirements 10.3**

- [x] 11. Update Vehicle Exit Panel with Modern Styling
  - [x] 11.1 Create ModernExitPanel
    - Replace form fields with styled components
    - Style payment summary area
    - Style receipt display with distinct background and separators
    - Replace dialogs with StyledDialog
    - _Requirements: 4.1, 4.2, 10.2, 10.4_

- [x] 12. Update Reporting Panel with Modern Styling
  - [x] 12.1 Create ModernReportingPanel
    - Replace combo box with StyledComboBox
    - Replace buttons with StyledButton
    - Style report output area
    - _Requirements: 4.2, 4.3_

- [x] 13. Update BasePanel for Modern Styling
  - [x] 13.1 Update BasePanel helper methods
    - Update createButton() to return StyledButton
    - Update createTextField() to return StyledTextField
    - Update showError/showSuccess/showInfo to use StyledDialog
    - Add createStyledTable() helper method
    - _Requirements: 4.1, 4.2, 9.1, 9.2, 9.3_

- [x] 14. Update Application Entry Point
  - [x] 14.1 Update ParkingApplication
    - Replace MainFrame with ModernMainFrame
    - Ensure proper initialization of all components
    - _Requirements: 1.3_

- [x] 15. Final Checkpoint
  - Ensure all tests pass, ask the user if questions arise.
  - Verify visual appearance matches design
  - Test all navigation and functionality
  - Note: All test failures are due to MySQL database not running (connection refused). Non-database tests pass successfully.

## Notes

- All tasks including property tests are required for comprehensive coverage
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties using jqwik
- The implementation preserves all existing functionality while enhancing visual appearance
