package com.university.parking.view;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import com.university.parking.model.VehicleType;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

/**
 * Property-based tests for UI Event Handling.
 * 
 * Feature: parking-lot-management, Property 28: UI Event Handling
 * Validates: Requirements 12.3
 */
public class UIEventHandlingProperties {

    static {
        EventHandler.setSuppressDialogs(true);
    }

    @Property(tries = 5)
    void buttonClickTriggersActionListener(
            @ForAll @AlphaChars @StringLength(min = 1, max = 10) String buttonText) {
        AtomicBoolean triggered = new AtomicBoolean(false);
        JButton button = new JButton(buttonText);
        button.addActionListener(e -> triggered.set(true));
        EventHandler.triggerButtonClick(button);
        assert triggered.get() : "Button click should trigger listener";
    }

    @Property(tries = 5)
    void comboBoxSelectionTriggersActionListener(
            @ForAll @IntRange(min = 0, max = 3) int index) {
        AtomicBoolean triggered = new AtomicBoolean(false);
        JComboBox<VehicleType> combo = new JComboBox<>(VehicleType.values());
        combo.addActionListener(e -> triggered.set(true));
        combo.setSelectedIndex(index);
        assert triggered.get() : "Combo selection should trigger listener";
    }

    @Property(tries = 5)
    void multipleListenersAllTriggered(
            @ForAll @IntRange(min = 1, max = 3) int count) {
        JButton button = new JButton("Test");
        AtomicInteger triggerCount = new AtomicInteger(0);
        for (int i = 0; i < count; i++) {
            button.addActionListener(e -> triggerCount.incrementAndGet());
        }
        EventHandler.triggerButtonClick(button);
        assert triggerCount.get() == count : "All listeners should trigger";
    }

    @Property(tries = 5)
    void checkboxStateReflected(@ForAll boolean initial, @ForAll boolean newState) {
        JCheckBox cb = new JCheckBox("Test", initial);
        cb.setSelected(newState);
        assert cb.isSelected() == newState : "Checkbox state should match";
    }

    @Property(tries = 5)
    void textFieldValueRetrieved(
            @ForAll @AlphaChars @StringLength(min = 0, max = 20) String text) {
        JTextField field = new JTextField();
        field.setText(text);
        assert field.getText().equals(text) : "Text field value should match";
    }

    @Property(tries = 5)
    void tableSelectionReported(
            @ForAll @IntRange(min = 1, max = 5) int rows,
            @ForAll @IntRange(min = 0, max = 4) int selected) {
        if (selected >= rows) return;
        Object[][] data = new Object[rows][2];
        for (int i = 0; i < rows; i++) data[i] = new Object[]{"R" + i, i};
        JTable table = new JTable(data, new String[]{"A", "B"});
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionInterval(selected, selected);
        assert table.getSelectedRow() == selected : "Selected row should match";
    }
}
