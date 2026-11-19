package com.bhel.ui;

import com.bhel.model.Attendance;
import com.bhel.model.Employee;
import com.bhel.model.FamilyMember;
import com.bhel.model.LeaveApplication;
import com.bhel.remote.HRMService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class EmployeeReportsFrame extends JFrame {
    private static final Color NAVY = new Color(18, 39, 67);
    private static final Color LIGHT_BG = new Color(245, 247, 250);
    private static final Color BORDER = new Color(220, 226, 234);
    private static final Color MUTED = new Color(105, 115, 130);
    private static final String DETAILS = "Employee Details Report";
    private static final String ATTENDANCE = "Attendance Report";
    private static final String LEAVE = "Leave Report";
    private static final String YEARLY = "Yearly Employee Summary Report";

    private final HRMService hrmService;
    private final List<Employee> employees = new ArrayList<>();
    private final JPanel reportBody = new JPanel(new BorderLayout(0, 12));
    private final JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    private final JLabel reportTitle = new JLabel("Select a report");
    private final JLabel reportDescription = new JLabel("Choose one of the four reports from the list.");
    private final JLabel statusLabel = new JLabel(" ");
    private JComboBox<Employee> employeeBox;
    private JComboBox<Integer> yearBox;
    private JComboBox<MonthChoice> monthBox;
    private JTable reportTable;
    private DefaultTableModel reportModel;
    private String selectedReport;

    public EmployeeReportsFrame(HRMService hrmService) {
        this.hrmService = hrmService;
        setTitle("BHEL HRM - Reports");
        setSize(1200, 760);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        KeyboardNavigation.installEscape(this, this::dispose);
        buildInterface();
        loadEmployees();
    }

    private void buildInterface() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(NAVY);
        topBar.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel appTitle = new JLabel("BHEL HRM   ›   Reports");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        appTitle.setForeground(Color.WHITE);
        JButton refreshButton = new JButton("Refresh Employees");
        styleButton(refreshButton, false);
        refreshButton.addActionListener(e -> loadEmployees());
        topBar.add(appTitle, BorderLayout.WEST);
        topBar.add(refreshButton, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("EMPLOYEE REPORTS");
        root.add(new DefaultMutableTreeNode(DETAILS));
        root.add(new DefaultMutableTreeNode(ATTENDANCE));
        root.add(new DefaultMutableTreeNode(LEAVE));
        root.add(new DefaultMutableTreeNode(YEARLY));
        JTree reportTree = new JTree(root);
        reportTree.setRootVisible(true);
        reportTree.setShowsRootHandles(true);
        reportTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        reportTree.setBackground(NAVY);
        reportTree.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        reportTree.setRowHeight(29);
        DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
        treeRenderer.setBackgroundNonSelectionColor(NAVY);
        treeRenderer.setTextNonSelectionColor(new Color(225, 233, 244));
        treeRenderer.setBackgroundSelectionColor(new Color(37, 69, 105));
        treeRenderer.setTextSelectionColor(Color.WHITE);
        treeRenderer.setBorderSelectionColor(new Color(92, 151, 225));
        treeRenderer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reportTree.setCellRenderer(treeRenderer);
        reportTree.addTreeSelectionListener(e -> {
            Object node = reportTree.getLastSelectedPathComponent();
            if (node instanceof DefaultMutableTreeNode selected
                    && selected.isLeaf()) {
                selectReport(String.valueOf(selected.getUserObject()));
            }
        });
        JScrollPane navigation = new JScrollPane(reportTree);
        navigation.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, NAVY));
        navigation.getViewport().setBackground(NAVY);
        navigation.setPreferredSize(new Dimension(285, 0));

        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setBackground(LIGHT_BG);
        right.setBorder(new EmptyBorder(18, 20, 18, 20));
        JPanel heading = new JPanel();
        heading.setBackground(LIGHT_BG);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        reportTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        reportTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        reportTitle.setForeground(NAVY);
        reportDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reportDescription.setForeground(MUTED);
        reportDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.add(reportTitle);
        heading.add(Box.createVerticalStrut(5));
        heading.add(reportDescription);
        right.add(heading, BorderLayout.NORTH);

        reportBody.setOpaque(false);
        right.add(reportBody, BorderLayout.CENTER);
        statusLabel.setBorder(new EmptyBorder(4, 2, 0, 2));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(MUTED);
        right.add(statusLabel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navigation, right);
        split.setResizeWeight(0.25);
        split.setDividerLocation(285);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
        showWelcome();
    }

    private void loadEmployees() {
        try {
            employees.clear();
            List<Employee> result = hrmService.getAllEmployees();
            if (result != null) employees.addAll(result);
            if (employeeBox != null) {
                Employee current = (Employee) employeeBox.getSelectedItem();
                employeeBox.removeAllItems();
                for (Employee employee : employees) employeeBox.addItem(employee);
                if (current != null) selectEmployeeById(current.getEmployeeId());
            }
            statusLabel.setText(employees.size() + " employees available");
        } catch (Exception e) {
            statusLabel.setText("Could not load employee records: " + e.getMessage());
        }
    }

    private void selectEmployeeById(int id) {
        if (employeeBox == null) return;
        for (int i = 0; i < employeeBox.getItemCount(); i++) {
            if (employeeBox.getItemAt(i).getEmployeeId() == id) {
                employeeBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectReport(String report) {
        selectedReport = report;
        reportTitle.setText(report);
        reportDescription.setText(descriptionFor(report));
        reportBody.removeAll();
        filtersPanel.removeAll();

        employeeBox = new JComboBox<>();
        employeeBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        employeeBox.setPreferredSize(new Dimension(260, 31));
        employeeBox.setRenderer((list, employee, index, selected, focus) -> {
            JLabel label = new JLabel(employee == null ? "Select employee"
                    : employeeName(employee) + " (ID " + employee.getEmployeeId() + ")");
            if (selected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });
        for (Employee employee : employees) employeeBox.addItem(employee);

        yearBox = new JComboBox<>();
        yearBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        int currentYear = Year.now().getValue();
        for (int year = currentYear; year >= currentYear - 10; year--) yearBox.addItem(year);
        monthBox = new JComboBox<>();
        monthBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        monthBox.addItem(new MonthChoice(0, "Full year"));
        for (int month = 1; month <= 12; month++) {
            monthBox.addItem(new MonthChoice(month,
                    YearMonth.of(currentYear, month).getMonth()
                            .getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)));
        }

        if (!report.equals(DETAILS) && !report.equals(ATTENDANCE)
                && !report.equals(LEAVE) && !report.equals(YEARLY)) return;
        if (report.equals(DETAILS) || report.equals(ATTENDANCE)
                || report.equals(LEAVE) || report.equals(YEARLY)) {
            filtersPanel.add(new JLabel("Employee:"));
            filtersPanel.add(employeeBox);
        }
        if (report.equals(ATTENDANCE) || report.equals(LEAVE) || report.equals(YEARLY)) {
            filtersPanel.add(new JLabel("Year:"));
            filtersPanel.add(yearBox);
        }
        if (report.equals(ATTENDANCE)) {
            filtersPanel.add(new JLabel("Month:"));
            filtersPanel.add(monthBox);
        }
        JButton generate = new JButton("View Report");
        styleButton(generate, true);
        generate.addActionListener(e -> generateSelectedReport());
        filtersPanel.add(generate);
        filtersPanel.setOpaque(true);
        filtersPanel.setBackground(Color.WHITE);
        filtersPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(9, 10, 9, 10)));
        reportBody.add(filtersPanel, BorderLayout.NORTH);
        showEmptyTable();
        reportBody.revalidate();
        reportBody.repaint();
    }

    private String descriptionFor(String report) {
        return switch (report) {
            case DETAILS -> "Employee ID, name, IC/Passport, department, designation, contact details, status and family details.";
            case ATTENDANCE -> "Attendance dates, check-in/out, working hours and present, absent and late totals.";
            case LEAVE -> "Leave applications, dates, reasons, status and total approved leave days.";
            case YEARLY -> "Profile, family details, attendance summary, leave history and remaining leave balance for a year.";
            default -> "Choose one of the four employee reports.";
        };
    }

    private void showWelcome() {
        reportBody.removeAll();
        JLabel welcome = new JLabel("Select a report from the list on the left.", SwingConstants.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcome.setForeground(NAVY);
        reportBody.add(welcome, BorderLayout.CENTER);
    }

    private void showEmptyTable() {
        setTable(new String[]{"Report data will appear here"}, List.of());
    }

    private void generateSelectedReport() {
        Employee employee = (Employee) employeeBox.getSelectedItem();
        if (employee == null) {
            JOptionPane.showMessageDialog(this, "No employee records are available.",
                    "Report", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int year = (Integer) yearBox.getSelectedItem();
        try {
            switch (selectedReport) {
                case DETAILS -> generateDetails(employee);
                case ATTENDANCE -> generateAttendance(employee, year,
                        ((MonthChoice) monthBox.getSelectedItem()).month);
                case LEAVE -> generateLeave(employee, year);
                case YEARLY -> generateYearly(employee, year);
                default -> showWelcome();
            }
        } catch (Exception e) {
            statusLabel.setText("Report failed: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Unable to generate report.\n" + e.getMessage(),
                    "Report Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateDetails(Employee employee) throws Exception {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"Employee", "Employee ID", employee.getEmployeeId()});
        rows.add(new Object[]{"Employee", "First Name", employee.getFirstName()});
        rows.add(new Object[]{"Employee", "Last Name", employee.getLastName()});
        rows.add(new Object[]{"Employee", "IC / Passport", employee.getIcPassport()});
        rows.add(new Object[]{"Employee", "Username", employee.getUsername()});
        rows.add(new Object[]{"Employee", "Department", employee.getDepartment()});
        rows.add(new Object[]{"Employee", "Designation", employee.getDesignation()});
        rows.add(new Object[]{"Employee", "Phone", employee.getPhone()});
        rows.add(new Object[]{"Employee", "Email", employee.getEmail()});
        rows.add(new Object[]{"Employee", "Address", employee.getAddress()});
        rows.add(new Object[]{"Employee", "Employment Status", employee.getEmploymentStatus()});
        List<FamilyMember> family = hrmService.getFamilyMembers(employee.getEmployeeId());
        if (family != null) for (FamilyMember member : family) {
            rows.add(new Object[]{"Family", member.getName(), member.getRelationship()
                    + "; Contact: " + value(member.getContactNumber())
                    + "; Date of Birth: " + value(member.getDateOfBirth())});
        }
        if (family == null || family.isEmpty()) {
            rows.add(new Object[]{"Family", "Details", "No family details recorded"});
        }
        setReport("Employee Details Report - " + employeeName(employee),
                new String[]{"Section", "Field", "Value"}, rows);
    }

    private void generateAttendance(Employee employee, int year, int month) throws Exception {
        LocalDate start = month == 0 ? LocalDate.of(year, 1, 1) : LocalDate.of(year, month, 1);
        LocalDate end = month == 0 ? LocalDate.of(year, 12, 31) : YearMonth.of(year, month).atEndOfMonth();
        List<Object[]> rows = new ArrayList<>();
        int present = 0, absent = 0, late = 0;
        List<Attendance> records = hrmService.getAllAttendance();
        if (records != null) for (Attendance record : records) {
            if (record == null || record.getEmployeeId() != employee.getEmployeeId()
                    || record.getAttendanceDate() == null
                    || record.getAttendanceDate().isBefore(start)
                    || record.getAttendanceDate().isAfter(end)) continue;
            String status = value(record.getStatus());
            if (status.equalsIgnoreCase("Present")) present++;
            if (status.equalsIgnoreCase("Absent")) absent++;
            if (status.equalsIgnoreCase("Late")) late++;
            rows.add(new Object[]{employee.getEmployeeId(), employeeName(employee), record.getAttendanceDate(),
                    record.getCheckIn(), record.getCheckOut(), hours(record), status});
        }
        rows.sort(Comparator.comparing(row -> row[2].toString()));
        setReport("Attendance Report - " + employeeName(employee) + " - " + start + " to " + end,
                new String[]{"Employee ID", "Employee Name", "Attendance Date", "Check In",
                        "Check Out", "Working Hours", "Status"}, rows);
        statusLabel.setText("Present: " + present + "   |   Absent: " + absent + "   |   Late: " + late
                + "   |   Total attendance records: " + rows.size());
    }

    private void generateLeave(Employee employee, int year) throws Exception {
        List<Object[]> rows = new ArrayList<>();
        int approvedDays = 0;
        List<LeaveApplication> leaves = hrmService.getAllLeaveApplications();
        if (leaves != null) for (LeaveApplication leave : leaves) {
            if (leave == null || leave.getEmployeeId() != employee.getEmployeeId()
                    || !overlapsYear(leave, year)) continue;
            int days = daysWithinYear(leave, year);
            if ("Approved".equalsIgnoreCase(value(leave.getStatus()))) approvedDays += days;
            rows.add(new Object[]{employee.getEmployeeId(), employeeName(employee), leave.getLeaveType(),
                    leave.getStartDate(), leave.getEndDate(), days, leave.getReason(),
                    leave.getAppliedDate(), leave.getStatus()});
        }
        setReport("Leave Report - " + employeeName(employee) + " - " + year,
                new String[]{"Employee ID", "Employee Name", "Leave Type", "Start Date", "End Date",
                        "Leave Days", "Reason", "Application Date", "Status"}, rows);
        statusLabel.setText("Total approved leave days in " + year + ": " + approvedDays);
    }

    private void generateYearly(Employee employee, int year) throws Exception {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"Employee ID", employee.getEmployeeId()});
        rows.add(new Object[]{"Employee Name", employeeName(employee)});
        rows.add(new Object[]{"IC / Passport", employee.getIcPassport()});
        rows.add(new Object[]{"Department / Designation", value(employee.getDepartment())
                + " / " + value(employee.getDesignation())});
        rows.add(new Object[]{"Contact", value(employee.getPhone()) + " / " + value(employee.getEmail())});
        rows.add(new Object[]{"Gender / Date of Birth", value(employee.getGender())
                + " / " + value(employee.getDateOfBirth())});
        rows.add(new Object[]{"Employment Date", employee.getJoiningDate()});
        rows.add(new Object[]{"Employment Status", employee.getEmploymentStatus()});

        List<FamilyMember> family = hrmService.getFamilyMembers(employee.getEmployeeId());
        if (family != null) for (FamilyMember member : family) {
            rows.add(new Object[]{"Family: " + member.getName(), member.getRelationship()
                    + "; contact " + value(member.getContactNumber())
                    + "; date of birth " + value(member.getDateOfBirth())});
        }

        int working = 0, present = 0, absent = 0, late = 0;
        List<Attendance> attendance = hrmService.getAllAttendance();
        if (attendance != null) for (Attendance record : attendance) {
            if (record == null || record.getEmployeeId() != employee.getEmployeeId()
                    || record.getAttendanceDate() == null
                    || record.getAttendanceDate().getYear() != year) continue;
            working++;
            if ("Present".equalsIgnoreCase(value(record.getStatus()))) present++;
            if ("Absent".equalsIgnoreCase(value(record.getStatus()))) absent++;
            if ("Late".equalsIgnoreCase(value(record.getStatus()))) late++;
        }
        rows.add(new Object[]{"Total Working Days", working});
        rows.add(new Object[]{"Present / Absent / Late", present + " / " + absent + " / " + late});

        List<LeaveApplication> leaves = hrmService.getAllLeaveApplications();
        int approvedTotal = 0;
        if (leaves != null) for (LeaveApplication leave : leaves) {
            if (leave == null || leave.getEmployeeId() != employee.getEmployeeId()
                    || !overlapsYear(leave, year)) continue;
            int days = daysWithinYear(leave, year);
            if ("Approved".equalsIgnoreCase(value(leave.getStatus()))) approvedTotal += days;
            rows.add(new Object[]{"Leave: " + value(leave.getLeaveType()),
                    leave.getStartDate() + " to " + leave.getEndDate() + " | " + days
                            + " days | " + value(leave.getStatus()) + " | " + value(leave.getReason())});
        }
        rows.add(new Object[]{"Total Approved Leave Days", approvedTotal});

        int annualUsed = approvedDays(leaves, employee.getEmployeeId(), year, "Annual Leave");
        int sickUsed = approvedDays(leaves, employee.getEmployeeId(), year, "Sick Leave");
        int emergencyUsed = approvedDays(leaves, employee.getEmployeeId(), year, "Emergency Leave");
        rows.add(new Object[]{"Remaining Annual Leave Balance", 1 - annualUsed + " days"});
        rows.add(new Object[]{"Remaining Sick Leave Balance", 1 - sickUsed + " days"});
        rows.add(new Object[]{"Remaining Emergency Leave Balance", 1 - emergencyUsed + " days"});

        setReport(YEARLY + " - " + employeeName(employee) + " - " + year,
                new String[]{"Information", "Value"}, rows);
        statusLabel.setText("Yearly report generated for " + year);
    }

    private void setReport(String title, String[] columns, List<Object[]> rows) {
        reportTitle.setText(selectedReport);
        reportDescription.setText(title);
        statusLabel.setText("" + rows.size() + " report rows");
        setTable(columns, rows);
    }

    private void setTable(String[] columns, List<Object[]> rows) {
        reportBody.removeAll();
        reportBody.add(filtersPanel, BorderLayout.NORTH);
        reportModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (Object[] row : rows) reportModel.addRow(row);
        reportTable = new JTable(reportModel);
        reportTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reportTable.setRowHeight(27);
        reportTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        reportTable.setGridColor(BORDER);
        reportTable.setSelectionBackground(new Color(220, 232, 248));
        reportTable.setSelectionForeground(Color.BLACK);
        reportTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        reportTable.getTableHeader().setBackground(NAVY);
        reportTable.getTableHeader().setForeground(Color.WHITE);

        JPanel results = new JPanel(new BorderLayout(0, 8));
        results.setBackground(Color.WHITE);
        results.setBorder(BorderFactory.createLineBorder(BORDER));
        results.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        actions.setBackground(Color.WHITE);
        JButton export = new JButton("Export CSV");
        JButton print = new JButton("Print / Save PDF");
        styleButton(export, false);
        styleButton(print, true);
        export.addActionListener(e -> exportCsv());
        print.addActionListener(e -> printReport());
        actions.add(export);
        actions.add(print);
        results.add(actions, BorderLayout.SOUTH);
        reportBody.add(results, BorderLayout.CENTER);
        reportBody.revalidate();
        reportBody.repaint();
    }

    private void exportCsv() {
        if (reportModel == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(selectedReport.replaceAll("[^A-Za-z0-9-]", "_") + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (BufferedWriter writer = Files.newBufferedWriter(
                chooser.getSelectedFile().toPath(), StandardCharsets.UTF_8)) {
            for (int c = 0; c < reportModel.getColumnCount(); c++) {
                if (c > 0) writer.write(',');
                writer.write(csv(reportModel.getColumnName(c)));
            }
            writer.newLine();
            for (int r = 0; r < reportModel.getRowCount(); r++) {
                for (int c = 0; c < reportModel.getColumnCount(); c++) {
                    if (c > 0) writer.write(',');
                    writer.write(csv(reportModel.getValueAt(r, c)));
                }
                writer.newLine();
            }
            statusLabel.setText("CSV exported successfully");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "CSV export failed: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printReport() {
        if (reportTable == null) return;
        try {
            reportTable.print(JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat(selectedReport),
                    new java.text.MessageFormat("Page {0}"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Printing failed: " + e.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String csv(Object value) {
        return "\"" + (value == null ? "" : value.toString()).replace("\"", "\"\"") + "\"";
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(primary ? NAVY : Color.WHITE);
        button.setForeground(primary ? Color.WHITE : NAVY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? NAVY : BORDER),
                new EmptyBorder(7, 12, 7, 12)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private int approvedDays(List<LeaveApplication> leaves, int employeeId,
                             int year, String type) {
        int total = 0;
        if (leaves == null) return total;
        for (LeaveApplication leave : leaves) {
            if (leave == null || leave.getEmployeeId() != employeeId
                    || !"Approved".equalsIgnoreCase(value(leave.getStatus()))
                    || !overlapsYear(leave, year)) continue;
            if (type != null && !type.equalsIgnoreCase(value(leave.getLeaveType()))) continue;
            total += daysWithinYear(leave, year);
        }
        return total;
    }

    private boolean overlapsYear(LeaveApplication leave, int year) {
        return leave.getStartDate() != null && leave.getEndDate() != null
                && !leave.getStartDate().isAfter(LocalDate.of(year, 12, 31))
                && !leave.getEndDate().isBefore(LocalDate.of(year, 1, 1));
    }

    private int daysWithinYear(LeaveApplication leave, int year) {
        LocalDate start = leave.getStartDate().isBefore(LocalDate.of(year, 1, 1))
                ? LocalDate.of(year, 1, 1) : leave.getStartDate();
        LocalDate end = leave.getEndDate().isAfter(LocalDate.of(year, 12, 31))
                ? LocalDate.of(year, 12, 31) : leave.getEndDate();
        return end.isBefore(start) ? 0 : (int) ChronoUnit.DAYS.between(start, end) + 1;
    }

    private String hours(Attendance attendance) {
        if (attendance.getCheckIn() == null || attendance.getCheckOut() == null) return "";
        Duration duration = Duration.between(attendance.getCheckIn(), attendance.getCheckOut());
        if (duration.isNegative()) duration = duration.plusDays(1);
        return String.format(Locale.ROOT, "%.2f", duration.toMinutes() / 60.0);
    }

    private String employeeName(Employee employee) {
        return (value(employee.getFirstName()) + " " + value(employee.getLastName())).trim();
    }

    private String value(Object value) { return value == null ? "" : value.toString(); }

    private record MonthChoice(int month, String name) {
        @Override public String toString() { return name; }
    }
}
