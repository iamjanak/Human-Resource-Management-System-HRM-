
package com.bhel.ui;

import com.bhel.model.LeaveApplication;
import com.bhel.remote.HRMService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class LeaveManagementFrame extends JFrame {

    private final Color NAVY = new Color(22, 39, 63);
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT = new Color(55, 65, 81);

    private final HRMService hrmService;
    private final int employeeId;

    private JComboBox<String> leaveType;
    private DatePickerField startDatePicker;
    private DatePickerField endDatePicker;
    private JTextArea reasonArea;

    private JTable leaveTable;
    private DefaultTableModel tableModel;

    // Fixed: declare the missing panels
    private JPanel formContainer;
    private JPanel tablePanel;

    public LeaveManagementFrame(HRMService hrmService, int employeeId) {

        this.hrmService = hrmService;
        this.employeeId = employeeId;

        setTitle("BHEL HRM - Leave Management");
        setSize(1150, 780);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(LIGHT_BG);

        createHeader();

        // Main content holds the form and table
        JPanel mainContent = new JPanel(new BorderLayout(0, 12));
        mainContent.setBackground(LIGHT_BG);

        add(mainContent, BorderLayout.CENTER);

        createApplicationForm();
        createLeaveTable();

        mainContent.add(formContainer, BorderLayout.NORTH);
        mainContent.add(tablePanel, BorderLayout.CENTER);

        loadLeaveApplications();
    }

    // ==========================================
    // HEADER
    // ==========================================

    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JLabel title = new JLabel("Leave Management");
        title.setForeground(WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JLabel subtitle = new JLabel(
                "Apply for leave and manage your leave applications");

        subtitle.setForeground(new Color(210, 220, 235));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);
    }

    // ==========================================
    // APPLICATION FORM
    // ==========================================

    private void createApplicationForm() {

        JPanel formPanel = new JPanel(new BorderLayout(15, 15));
        formPanel.setBackground(WHITE);

        formPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 235)),
                        new EmptyBorder(18, 22, 15, 22)));

        JLabel formTitle = new JLabel("Apply for Leave");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        formTitle.setForeground(NAVY);

        leaveType = new JComboBox<>(new String[]{
                "Annual Leave",
                "Sick Leave",
                "Emergency Leave",
                "Unpaid Leave"
        });

        leaveType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        leaveType.setPreferredSize(new Dimension(180, 38));

        startDatePicker = new DatePickerField();
        endDatePicker = new DatePickerField();

        JPanel fieldsPanel = new JPanel(
                new GridLayout(1, 3, 18, 0));

        fieldsPanel.setOpaque(false);

        fieldsPanel.add(createFieldPanel("Leave Type", leaveType));
        fieldsPanel.add(createFieldPanel("Start Date", startDatePicker));
        fieldsPanel.add(createFieldPanel("End Date", endDatePicker));

        // Reason field
        reasonArea = new JTextArea(3, 30);
        reasonArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setPreferredSize(new Dimension(500, 75));

        JPanel reasonPanel = new JPanel(new BorderLayout(0, 6));
        reasonPanel.setOpaque(false);

        JLabel reasonLabel = new JLabel("Reason for Leave");
        reasonLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        reasonLabel.setForeground(TEXT);

        reasonPanel.add(reasonLabel, BorderLayout.NORTH);
        reasonPanel.add(reasonScroll, BorderLayout.CENTER);

        // Buttons
        JButton refreshButton = new JButton("Refresh");
        JButton submitButton = new JButton("Apply for Leave");

        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 13));

        refreshButton.setPreferredSize(new Dimension(110, 40));
        submitButton.setPreferredSize(new Dimension(165, 40));

        refreshButton.setBackground(new Color(235, 239, 245));
        refreshButton.setForeground(NAVY);

        submitButton.setBackground(NAVY);
        submitButton.setForeground(WHITE);

        refreshButton.setFocusPainted(false);
        submitButton.setFocusPainted(false);

        refreshButton.addActionListener(e -> loadLeaveApplications());
        submitButton.addActionListener(e -> applyLeave());

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 3));

        buttonPanel.setOpaque(false);
        buttonPanel.add(refreshButton);
        buttonPanel.add(submitButton);

        // Form content
        JPanel formContent = new JPanel(
                new BorderLayout(0, 12));

        formContent.setOpaque(false);

        formContent.add(fieldsPanel, BorderLayout.NORTH);
        formContent.add(reasonPanel, BorderLayout.CENTER);
        formContent.add(buttonPanel, BorderLayout.SOUTH);

        JPanel completeForm = new JPanel(
                new BorderLayout(0, 12));

        completeForm.setOpaque(false);
        completeForm.add(formTitle, BorderLayout.NORTH);
        completeForm.add(formContent, BorderLayout.CENTER);

        formPanel.add(completeForm, BorderLayout.CENTER);

        formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(LIGHT_BG);
        formContainer.setBorder(
                new EmptyBorder(15, 25, 5, 25));

        formContainer.add(formPanel, BorderLayout.CENTER);
    }

    // ==========================================
    // FIELD PANEL
    // ==========================================

    private JPanel createFieldPanel(
            String label,
            JComponent component) {

        JPanel panel = new JPanel(new BorderLayout(0, 7));
        panel.setOpaque(false);

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fieldLabel.setForeground(TEXT);

        panel.add(fieldLabel, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // CALENDAR DATE PICKER
    // ==========================================

    private class DatePickerField extends JPanel {

        private final JTextField dateField;
        private LocalDate selectedDate;

        DatePickerField() {

            setLayout(new BorderLayout(5, 0));
            setOpaque(false);

            dateField = new JTextField();
            dateField.setEditable(false);
            dateField.setFont(
                    new Font("Segoe UI", Font.PLAIN, 14));

            dateField.setBackground(WHITE);
            dateField.setPreferredSize(
                    new Dimension(130, 38));

            JButton calendarButton = new JButton("Choose");
            calendarButton.setFont(
                    new Font("Segoe UI", Font.BOLD, 12));

            calendarButton.setBackground(
                    new Color(235, 239, 245));

            calendarButton.setForeground(NAVY);
            calendarButton.setFocusPainted(false);

            calendarButton.addActionListener(
                    e -> showCalendar());

            add(dateField, BorderLayout.CENTER);
            add(calendarButton, BorderLayout.EAST);
        }

        public LocalDate getDate() {
            return selectedDate;
        }

        public void clear() {
            selectedDate = null;
            dateField.setText("");
        }

        private void showCalendar() {

            JDialog dialog = new JDialog(
                    LeaveManagementFrame.this,
                    "Select Date",
                    true);

            dialog.setSize(340, 350);
            dialog.setLocationRelativeTo(
                    LeaveManagementFrame.this);

            dialog.setLayout(new BorderLayout());

            JPanel calendarPanel = new JPanel(
                    new BorderLayout(5, 10));

            calendarPanel.setBorder(
                    new EmptyBorder(15, 15, 15, 15));

            YearMonth initialMonth = YearMonth.from(
                    selectedDate != null
                            ? selectedDate
                            : LocalDate.now());

            final YearMonth[] currentMonth = {initialMonth};

            JLabel monthLabel = new JLabel(
                    "", SwingConstants.CENTER);

            monthLabel.setFont(
                    new Font("Segoe UI", Font.BOLD, 17));

            monthLabel.setForeground(NAVY);

            JPanel daysPanel = new JPanel(
                    new GridLayout(0, 7, 4, 4));

            // Refresh calendar
            Runnable refreshCalendar = () -> {

                daysPanel.removeAll();

                monthLabel.setText(
                        currentMonth[0].getMonth().getDisplayName(
                                TextStyle.FULL,
                                Locale.ENGLISH)
                                + " "
                                + currentMonth[0].getYear());

                String[] weekdays = {
                        "Mon", "Tue", "Wed",
                        "Thu", "Fri", "Sat", "Sun"
                };

                for (String day : weekdays) {

                    JLabel label = new JLabel(
                            day, SwingConstants.CENTER);

                    label.setFont(
                            new Font("Segoe UI", Font.BOLD, 11));

                    label.setForeground(NAVY);
                    daysPanel.add(label);
                }

                int offset = currentMonth[0]
                        .atDay(1)
                        .getDayOfWeek()
                        .getValue() - 1;

                for (int i = 0; i < offset; i++) {
                    daysPanel.add(new JLabel(""));
                }

                int daysInMonth =
                        currentMonth[0].lengthOfMonth();

                for (int day = 1; day <= daysInMonth; day++) {

                    final int selectedDay = day;

                    JButton dayButton = new JButton(
                            String.valueOf(day));

                    dayButton.setFocusPainted(false);
                    dayButton.setMargin(
                            new Insets(2, 2, 2, 2));

                    dayButton.setFont(
                            new Font("Segoe UI", Font.PLAIN, 12));

                    dayButton.setBackground(WHITE);

                    LocalDate date =
                            currentMonth[0].atDay(day);

                    if (date.equals(selectedDate)) {
                        dayButton.setBackground(
                                new Color(190, 210, 240));
                    }

                    dayButton.addActionListener(e -> {

                        selectedDate =
                                currentMonth[0].atDay(selectedDay);

                        dateField.setText(
                                selectedDate.toString());

                        dialog.dispose();
                    });

                    daysPanel.add(dayButton);
                }

                daysPanel.revalidate();
                daysPanel.repaint();
            };

            // Month navigation
            JButton previousButton = new JButton("<");
            JButton nextButton = new JButton(">");

            previousButton.addActionListener(e -> {

                currentMonth[0] =
                        currentMonth[0].minusMonths(1);

                refreshCalendar.run();
            });

            nextButton.addActionListener(e -> {

                currentMonth[0] =
                        currentMonth[0].plusMonths(1);

                refreshCalendar.run();
            });

            JPanel monthNavigation = new JPanel(
                    new BorderLayout(10, 0));

            monthNavigation.add(
                    previousButton, BorderLayout.WEST);

            monthNavigation.add(
                    monthLabel, BorderLayout.CENTER);

            monthNavigation.add(
                    nextButton, BorderLayout.EAST);

            calendarPanel.add(
                    monthNavigation, BorderLayout.NORTH);

            calendarPanel.add(
                    daysPanel, BorderLayout.CENTER);

            refreshCalendar.run();

            dialog.add(calendarPanel, BorderLayout.CENTER);
            dialog.setVisible(true);
        }
    }

    // ==========================================
    // LEAVE HISTORY TABLE
    // ==========================================

    private void createLeaveTable() {

        String[] columns = {
                "Leave ID",
                "Leave Type",
                "Start Date",
                "End Date",
                "Reason",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {
                return false;
            }
        };

        leaveTable = new JTable(tableModel);

        leaveTable.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));

        leaveTable.setRowHeight(36);
        leaveTable.setBackground(WHITE);
        leaveTable.setForeground(TEXT);

        leaveTable.setSelectionBackground(
                new Color(220, 230, 245));

        leaveTable.setSelectionForeground(NAVY);

        leaveTable.setAutoResizeMode(
                JTable.AUTO_RESIZE_OFF);

        leaveTable.setFillsViewportHeight(true);

        // Table header
        leaveTable.getTableHeader().setBackground(NAVY);
        leaveTable.getTableHeader().setForeground(WHITE);

        leaveTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13));

        leaveTable.getTableHeader().setPreferredSize(
                new Dimension(0, 42));

        // Column widths
        int[] widths = {
                75, 140, 120, 120, 300, 110
        };

        for (int i = 0; i < widths.length; i++) {

            leaveTable.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        leaveTable.getColumnModel()
                .getColumn(4)
                .setCellRenderer(new ReasonCellRenderer());

        JScrollPane scrollPane = new JScrollPane(
                leaveTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getViewport().setBackground(WHITE);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(220, 225, 230)));

        // Table panel
        tablePanel = new JPanel(
                new BorderLayout(0, 12));

        tablePanel.setBackground(LIGHT_BG);

        tablePanel.setBorder(
                new EmptyBorder(10, 25, 20, 25));

        JLabel historyTitle =
                new JLabel("My Leave Applications");

        historyTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 19));

        historyTitle.setForeground(NAVY);

        tablePanel.add(
                historyTitle, BorderLayout.NORTH);

        tablePanel.add(
                scrollPane, BorderLayout.CENTER);
    }

    // ==========================================
    // REASON CELL RENDERER
    // ==========================================

    private class ReasonCellRenderer extends JTextArea
            implements TableCellRenderer {

        ReasonCellRenderer() {

            setLineWrap(true);
            setWrapStyleWord(true);

            setFont(
                    new Font("Segoe UI", Font.PLAIN, 13));

            setBorder(
                    new EmptyBorder(7, 8, 7, 8));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            setText(value == null ? "" : value.toString());

            setBackground(
                    isSelected
                            ? table.getSelectionBackground()
                            : WHITE);

            setForeground(
                    isSelected
                            ? table.getSelectionForeground()
                            : TEXT);

            return this;
        }
    }

    // ==========================================
    // APPLY FOR LEAVE
    // ==========================================

    private void applyLeave() {

        try {

            String type = leaveType.getSelectedItem().toString();

            LocalDate startDate = startDatePicker.getDate();
            LocalDate endDate = endDatePicker.getDate();

            String reason = reasonArea.getText().trim();

            if (startDate == null || endDate == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select both start date and end date.",
                        "Missing Date",
                        JOptionPane.WARNING_MESSAGE);

                return;
            }

            if (endDate.isBefore(startDate)) {

                JOptionPane.showMessageDialog(
                        this,
                        "End date cannot be before start date.",
                        "Invalid Date",
                        JOptionPane.WARNING_MESSAGE);

                return;
            }

            if (reason.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter the reason for leave.",
                        "Missing Reason",
                        JOptionPane.WARNING_MESSAGE);

                return;
            }

            if (employeeId <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Invalid employee ID. Please log in again.",
                        "Employee Error",
                        JOptionPane.ERROR_MESSAGE);

                return;
            }

            LeaveApplication leave = new LeaveApplication(
                    employeeId,
                    type,
                    startDate,
                    endDate,
                    reason);

            String response = hrmService.applyLeave(leave);

            JOptionPane.showMessageDialog(this, response);

            if (response != null &&
                    response.toLowerCase().contains("successfully")) {

                startDatePicker.clear();
                endDatePicker.clear();
                reasonArea.setText("");

                loadLeaveApplications();
            }

        } catch (RemoteException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Leave application failed: " + e.getMessage(),
                    "Server Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // LOAD LEAVE APPLICATIONS
    // ==========================================

    private void loadLeaveApplications() {

        try {

            tableModel.setRowCount(0);

            if (employeeId <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Invalid employee ID. Cannot load leave records.",
                        "Employee Error",
                        JOptionPane.ERROR_MESSAGE);

                return;
            }

            for (LeaveApplication leave :
                    hrmService.getEmployeeLeaves(employeeId)) {

                tableModel.addRow(new Object[]{
                        leave.getLeaveId(),
                        leave.getLeaveType(),
                        leave.getStartDate(),
                        leave.getEndDate(),
                        leave.getReason(),
                        leave.getStatus()
                });
            }

            updateReasonRowHeights();

        } catch (RemoteException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load leave applications: "
                            + e.getMessage(),
                    "Server Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // ADJUST ROW HEIGHT FOR REASON TEXT
    // ==========================================

    private void updateReasonRowHeights() {

        for (int row = 0; row < leaveTable.getRowCount(); row++) {

            Component component = leaveTable.prepareRenderer(
                    leaveTable.getCellRenderer(row, 4),
                    row,
                    4);

            int height = component.getPreferredSize().height;

            leaveTable.setRowHeight(
                    row,
                    Math.max(36, height));
        }
    }
}
