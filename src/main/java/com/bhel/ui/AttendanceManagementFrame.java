
package com.bhel.ui;

import com.bhel.client.HRMClient;
import com.bhel.model.Attendance;
import com.bhel.model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class AttendanceManagementFrame extends JFrame {

    private static final Color NAVY = new Color(18, 39, 67);
    private static final Color LIGHT_BG = new Color(245, 247, 250);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(45, 55, 72);
    private static final Color MUTED = new Color(120, 130, 145);
    private static final Color BLUE = new Color(49, 108, 180);
    private static final Color GREEN = new Color(35, 145, 100);
    private static final Color RED = new Color(205, 65, 65);
    private static final Color ORANGE = new Color(220, 145, 45);

    private HRMClient hrmClient;

    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField searchField;
    private JTextField dateField;
    private JComboBox<String> statusFilter;

    private JLabel totalLabel;
    private JLabel presentLabel;
    private JLabel absentLabel;
    private JLabel lateLabel;

    private final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    public AttendanceManagementFrame(HRMClient hrmClient) {

        this.hrmClient = hrmClient;

        setTitle("BHEL HRM - Attendance Management");
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());
        KeyboardNavigation.installEscape(this, this::dispose);
        getContentPane().setBackground(LIGHT_BG);

        initComponents();
        loadAttendance();
    }

    private void initComponents() {

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(LIGHT_BG);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header
        mainPanel.add(createHeader(), BorderLayout.NORTH);

        // Center content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout(0, 20));
        centerPanel.setBackground(LIGHT_BG);

        centerPanel.add(createSummaryPanel(), BorderLayout.NORTH);
        centerPanel.add(createAttendancePanel(), BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    // ==========================================
    // HEADER
    // ==========================================

    private JPanel createHeader() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_BG);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(LIGHT_BG);

        JLabel title = new JLabel("Attendance Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Monitor and manage employee daily attendance records"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);

        JLabel dateLabel = new JLabel(
                "Today: " + LocalDate.now().format(DATE_FORMAT)
        );
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dateLabel.setForeground(NAVY);

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(dateLabel, BorderLayout.EAST);

        return panel;
    }

    // ==========================================
    // SUMMARY CARDS
    // ==========================================

    private JPanel createSummaryPanel() {

        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(LIGHT_BG);

        totalLabel = new JLabel("0");
        presentLabel = new JLabel("0");
        absentLabel = new JLabel("0");
        lateLabel = new JLabel("0");

        panel.add(createSummaryCard(
                "Total Employees", totalLabel, BLUE));

        panel.add(createSummaryCard(
                "Present Today", presentLabel, GREEN));

        panel.add(createSummaryCard(
                "Absent Today", absentLabel, RED));

        panel.add(createSummaryCard(
                "Late Today", lateLabel, ORANGE));

        return panel;
    }

    private JPanel createSummaryCard(
            String title,
            JLabel valueLabel,
            Color accent) {

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 4, 0, 0, accent),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 27));
        valueLabel.setForeground(NAVY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // ==========================================
    // ATTENDANCE TABLE AND FILTERS
    // ==========================================

    private JPanel createAttendancePanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top section
        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setBackground(WHITE);

        JLabel heading = new JLabel("Attendance Records");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 19));
        heading.setForeground(NAVY);

        topPanel.add(heading, BorderLayout.NORTH);
        topPanel.add(createFilterPanel(), BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {
                "Attendance ID",
                "Employee ID",
                "Employee Name",
                "Date",
                "Check In",
                "Check Out",
                "Status",
                "Remarks"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        attendanceTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        attendanceTable.setRowSorter(sorter);

        attendanceTable.setRowHeight(38);
        attendanceTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        attendanceTable.setForeground(TEXT);
        attendanceTable.setSelectionBackground(
                new Color(220, 232, 248));
        attendanceTable.setSelectionForeground(NAVY);
        attendanceTable.setShowVerticalLines(false);
        attendanceTable.setGridColor(new Color(235, 238, 243));

        JTableHeader header = attendanceTable.getTableHeader();
        header.setBackground(NAVY);
        header.setForeground(WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(100, 42));
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                new Color(230, 234, 240)));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom buttons
        panel.add(createActionPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFilterPanel() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(WHITE);

        searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        dateField = new JTextField(10);
        dateField.setToolTipText(
                "Optional exact date filter in yyyy-MM-dd format");

        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        statusFilter = new JComboBox<>(new String[]{
                "All Status",
                "Present",
                "Absent",
                "Late",
                "On Leave"
        });

        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton searchButton = createButton("Search", BLUE);
        JButton filterButton = createButton("Apply Filter", NAVY);
        JButton refreshButton = createButton("Refresh", GREEN);
        JButton clearButton = createButton("Clear Filters", BLUE);

        searchButton.addActionListener(e -> applyFilters());
        filterButton.addActionListener(e -> applyFilters());
        refreshButton.addActionListener(e -> loadAttendance());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            dateField.setText("");
            statusFilter.setSelectedItem("All Status");
            sorter.setRowFilter(null);
        });

        panel.add(new JLabel("Search:"));
        panel.add(searchField);

        panel.add(new JLabel("Date:"));
        panel.add(dateField);

        panel.add(statusFilter);

        panel.add(searchButton);
        panel.add(filterButton);
        panel.add(refreshButton);
        panel.add(clearButton);

        return panel;
    }

    // ==========================================
    // ACTION BUTTONS
    // ==========================================

    private JPanel createActionPanel() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 12));
        panel.setBackground(WHITE);

        JButton markButton = createButton(
                "Mark Attendance", GREEN);

        JButton editButton = createButton(
                "Edit Attendance", BLUE);

        JButton deleteButton = createButton(
                "Delete Attendance", RED);

        markButton.addActionListener(e -> openAttendanceDialog(null));

        editButton.addActionListener(e -> editSelectedAttendance());

        deleteButton.addActionListener(e -> deleteSelectedAttendance());

        panel.add(markButton);
        panel.add(editButton);
        panel.add(deleteButton);

        return panel;
    }

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    // ==========================================
    // MARK / EDIT ATTENDANCE DIALOG
    // ==========================================

    private void openAttendanceDialog(Attendance existing) {

        JDialog dialog = new JDialog(this,
                existing == null ? "Mark Attendance" : "Edit Attendance",
                true);

        dialog.setSize(440, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        KeyboardNavigation.installEscape(dialog, dialog::dispose);

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 15));
        form.setBorder(new EmptyBorder(25, 25, 20, 25));
        form.setBackground(WHITE);

        JTextField employeeIdField = new JTextField();
        JTextField dateInput = new JTextField(
                LocalDate.now().format(DATE_FORMAT));

        JTextField checkInField = new JTextField();
        JTextField checkOutField = new JTextField();
        JComboBox<String> statusInput = new JComboBox<>(
                new String[]{
                        "Present", "Absent", "Late", "On Leave"
                });

        JTextField remarksField = new JTextField();

        if (existing != null) {

            employeeIdField.setText(
                    String.valueOf(existing.getEmployeeId()));

            dateInput.setText(
                    existing.getAttendanceDate().format(DATE_FORMAT));

            checkInField.setText(
                    existing.getCheckIn() == null ? ""
                            : existing.getCheckIn().format(TIME_FORMAT));

            checkOutField.setText(
                    existing.getCheckOut() == null ? ""
                            : existing.getCheckOut().format(TIME_FORMAT));

            statusInput.setSelectedItem(existing.getStatus());

            remarksField.setText(existing.getRemarks());
            employeeIdField.setEditable(false);
        }

        addFormRow(form, "Employee ID:", employeeIdField);
        addFormRow(form, "Attendance Date:", dateInput);
        addFormRow(form, "Check In (HH:mm):", checkInField);
        addFormRow(form, "Check Out (HH:mm):", checkOutField);
        addFormRow(form, "Status:", statusInput);
        addFormRow(form, "Remarks:", remarksField);

        JButton saveButton = createButton("Save Attendance", GREEN);
        JButton cancelButton = createButton("Cancel", RED);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        buttons.setBackground(WHITE);
        buttons.add(cancelButton);
        buttons.add(saveButton);

        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {

            try {

                int employeeId = Integer.parseInt(
                        employeeIdField.getText().trim());

                LocalDate date = LocalDate.parse(
                        dateInput.getText().trim(), DATE_FORMAT);

                LocalTime checkIn = parseTime(checkInField.getText());
                LocalTime checkOut = parseTime(checkOutField.getText());

                String status = (String) statusInput.getSelectedItem();

                String remarks = remarksField.getText().trim();

                if (employeeId <= 0) {
                    throw new IllegalArgumentException(
                            "Invalid employee ID.");
                }

                if (checkIn != null && checkOut != null
                        && checkOut.isBefore(checkIn)) {
                    throw new IllegalArgumentException(
                            "Check-out cannot be before check-in.");
                }

                Attendance attendance = new Attendance(
                        existing == null ? 0 : existing.getAttendanceId(),
                        employeeId,
                        "",
                        date,
                        checkIn,
                        checkOut,
                        status,
                        remarks
                );

                String response = existing == null
                        ? hrmClient.markAttendance(attendance)
                        : hrmClient.updateAttendance(attendance);
                boolean saved = response != null
                        && response.toLowerCase().contains("successfully");

                JOptionPane.showMessageDialog(
                        dialog,
                        response,
                        saved ? "Attendance Saved" : "Attendance Error",
                        saved ? JOptionPane.INFORMATION_MESSAGE
                                : JOptionPane.ERROR_MESSAGE);

                if (saved) {
                    dialog.dispose();
                    loadAttendance();
                }

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        dialog,
                        "Please check your input:\n" + ex.getMessage(),
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addFormRow(
            JPanel panel, String label, JComponent component) {

        JLabel text = new JLabel(label);
        text.setFont(new Font("Segoe UI", Font.BOLD, 12));
        text.setForeground(NAVY);

        component.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panel.add(text);
        panel.add(component);
    }

    private LocalTime parseTime(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return LocalTime.parse(value.trim(), TIME_FORMAT);
    }

    // ==========================================
    // EDIT SELECTED ATTENDANCE
    // ==========================================

    private void editSelectedAttendance() {

        int row = attendanceTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select an attendance record.");
            return;
        }

        int modelRow = attendanceTable.convertRowIndexToModel(row);
        int attendanceId = Integer.parseInt(
                tableModel.getValueAt(modelRow, 0).toString());

        Attendance existing = new Attendance(
                attendanceId,
                Integer.parseInt(
                        tableModel.getValueAt(modelRow, 1).toString()),
                String.valueOf(tableModel.getValueAt(modelRow, 2)),
                LocalDate.parse(
                        tableModel.getValueAt(modelRow, 3).toString()),
                parseTime(String.valueOf(
                        tableModel.getValueAt(modelRow, 4))),
                parseTime(String.valueOf(
                        tableModel.getValueAt(modelRow, 5))),
                tableModel.getValueAt(modelRow, 6).toString(),
                String.valueOf(tableModel.getValueAt(modelRow, 7))
        );

        openAttendanceDialog(existing);
    }

    // ==========================================
    // DELETE ATTENDANCE
    // ==========================================

    private void deleteSelectedAttendance() {

        int row = attendanceTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select an attendance record.");
            return;
        }

        int modelRow = attendanceTable.convertRowIndexToModel(row);
        int attendanceId = Integer.parseInt(
                tableModel.getValueAt(modelRow, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete attendance record " + attendanceId + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String response = hrmClient.deleteAttendance(attendanceId);
            boolean deleted = response != null
                    && response.toLowerCase().contains("successfully");
            JOptionPane.showMessageDialog(this, response,
                    deleted ? "Attendance Deleted" : "Attendance Error",
                    deleted ? JOptionPane.INFORMATION_MESSAGE
                            : JOptionPane.ERROR_MESSAGE);
            if (deleted) {
                loadAttendance();
            }
        }
    }

    // ==========================================
    // LOAD ATTENDANCE
    // ==========================================

    private void loadAttendance() {
        try {
            List<Attendance> records = hrmClient.getAllAttendance();
            tableModel.setRowCount(0);

            if (records != null) {
                for (Attendance attendance : records) {
                    if (attendance == null) {
                        continue;
                    }
                    tableModel.addRow(new Object[]{
                            attendance.getAttendanceId(),
                            attendance.getEmployeeId(),
                            attendance.getEmployeeName(),
                            attendance.getAttendanceDate(),
                            attendance.getCheckIn() == null ? ""
                                    : attendance.getCheckIn().format(TIME_FORMAT),
                            attendance.getCheckOut() == null ? ""
                                    : attendance.getCheckOut().format(TIME_FORMAT),
                            attendance.getStatus(),
                            attendance.getRemarks()
                    });
                }
            }

            applyFilters();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load attendance records: " + e.getMessage(),
                    "Attendance Connection Error", JOptionPane.ERROR_MESSAGE);
            updateSummary();
        }
    }

    // ==========================================
    // SEARCH AND FILTER
    // ==========================================

    private void applyFilters() {

        String search = searchField.getText()
                .trim().toLowerCase();

        String date = dateField.getText().trim();

        String status = (String) statusFilter.getSelectedItem();

        if (!date.isEmpty()) {
            try {
                LocalDate.parse(date, DATE_FORMAT);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this, "Date must use yyyy-MM-dd format.",
                        "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!search.isEmpty()) {
            filters.add(RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(search), 1, 2));
        }
        if (!date.isEmpty()) {
            filters.add(RowFilter.regexFilter(
                    "^" + Pattern.quote(date) + "$", 3));
        }
        if (!"All Status".equals(status)) {
            filters.add(RowFilter.regexFilter(
                    "^" + Pattern.quote(status) + "$", 6));
        }

        sorter.setRowFilter(filters.isEmpty()
                ? null : RowFilter.andFilter(filters));
        attendanceTable.clearSelection();
        updateSummary();
    }

    // ==========================================
    // SUMMARY
    // ==========================================

    private void updateSummary() {

        Set<String> employeesToday = new HashSet<>();
        int present = 0;
        int absent = 0;
        int late = 0;

        String today = LocalDate.now().format(DATE_FORMAT);
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (!today.equals(String.valueOf(tableModel.getValueAt(i, 3)))) {
                continue;
            }

            employeesToday.add(String.valueOf(tableModel.getValueAt(i, 1)));

            String status = String.valueOf(
                    tableModel.getValueAt(i, 6));

            if (status.equals("Present")) {
                present++;
            } else if (status.equals("Absent")) {
                absent++;
            } else if (status.equals("Late")) {
                late++;
            }
        }

        totalLabel.setText(String.valueOf(employeesToday.size()));
        presentLabel.setText(String.valueOf(present));
        absentLabel.setText(String.valueOf(absent));
        lateLabel.setText(String.valueOf(late));
    }
}
