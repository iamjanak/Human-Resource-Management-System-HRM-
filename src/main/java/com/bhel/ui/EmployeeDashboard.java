
package com.bhel.ui;

import com.bhel.model.Employee;
import com.bhel.model.LeaveApplication;
import com.bhel.remote.HRMService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.Naming;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class EmployeeDashboard extends JFrame {

    private final Color NAVY = new Color(18, 39, 67);
    private final Color NAVY_LIGHT = new Color(29, 54, 83);
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color BORDER = new Color(220, 225, 235);
    private final Color TEXT_GRAY = new Color(105, 115, 130);

    private final Employee employee;
    private final String username;
    private final int employeeId;
    private HRMService hrmService;

    private JPanel contentPanel;
    private JButton activeButton;

    public EmployeeDashboard(Employee employee) {

        this.employee = employee;
        this.username = employee.getUsername();
        this.employeeId = employee.getEmployeeId();

        setTitle("BHEL HRM - Employee Dashboard");
        setSize(1150, 700);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        KeyboardNavigation.installEscape(this, this::logout);

        connectToServer();

        createHeader();
        createMainLayout();

        setVisible(true);
    }

    private void connectToServer() {
        try {
            hrmService = (HRMService) Naming.lookup(
                    "rmi://localhost:1099/HRMService");
        } catch (Exception e) {
            hrmService = null;
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    this,
                    "Unable to connect to HRM server.\n" + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE));
        }
    }

    // ================= HEADER =================

    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(17, 25, 17, 25));

        JLabel title = new JLabel("BHEL HRM");

        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel portal = new JLabel("EMPLOYEE PORTAL");

        portal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        portal.setForeground(new Color(190, 205, 225));

        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));

        brandPanel.add(title);
        brandPanel.add(Box.createVerticalStrut(3));
        brandPanel.add(portal);

        JLabel welcome = new JLabel("Welcome, " + getDisplayName());

        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        welcome.setForeground(Color.WHITE);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(welcome, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // ================= MAIN LAYOUT =================

    private void createMainLayout() {

        JPanel body = new JPanel(new BorderLayout());

        body.add(createSidebar(), BorderLayout.WEST);

        contentPanel = new JPanel(new BorderLayout());

        contentPanel.setBackground(LIGHT_BG);
        contentPanel.setBorder(new EmptyBorder(30, 35, 30, 35));

        body.add(contentPanel, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);

        showDashboard();
    }

    // ================= SIDEBAR =================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel(new BorderLayout());

        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBackground(NAVY);
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));

        JPanel menuPanel = new JPanel();

        menuPanel.setBackground(NAVY);
        menuPanel.setLayout(
                new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JLabel menuTitle = new JLabel("EMPLOYEE MENU");

        menuTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 12));

        menuTitle.setForeground(new Color(170, 185, 205));
        menuTitle.setBorder(
                new EmptyBorder(5, 12, 20, 5));

        menuPanel.add(menuTitle);

        addMenuItem(menuPanel, "Dashboard", () -> showDashboard());

        addMenuItem(menuPanel, "My Attendance", this::showMyAttendance);

        addMenuItem(menuPanel, "My Profile", () -> openProfile());

        addMenuItem(menuPanel, "Family Details", () -> openProfile());

        addMenuItem(menuPanel, "Apply for Leave",
                () -> openLeaveManagement("Apply for Leave"));

        addMenuItem(menuPanel, "My Leave Applications",
                () -> openLeaveManagement("My Leave Applications"));

        addMenuItem(menuPanel, "Leave Balance",
                this::showLeaveBalance);

        JPanel logoutPanel = new JPanel(new BorderLayout());

        logoutPanel.setBackground(NAVY);
        logoutPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton logoutButton = createMenuButton("Logout");

        logoutButton.addActionListener(e -> logout());

        logoutPanel.add(logoutButton, BorderLayout.CENTER);

        sidebar.add(menuPanel, BorderLayout.NORTH);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    // ================= SIDEBAR MENU BUTTON =================

    private void addMenuItem(JPanel menuPanel,
                             String text,
                             Runnable action) {

        JButton button = createMenuButton(text);

        button.addActionListener(e -> {

            if (activeButton != null) {
                activeButton.setBackground(NAVY);
            }

            activeButton = button;
            activeButton.setBackground(NAVY_LIGHT);

            action.run();
        });

        menuPanel.add(button);
        menuPanel.add(Box.createVerticalStrut(7));
    }

    private JButton createMenuButton(String text) {

        JButton button = new JButton(text);

        button.setFont(
                new Font("Segoe UI", Font.PLAIN, 14));

        button.setForeground(Color.WHITE);
        button.setBackground(NAVY);

        button.setHorizontalAlignment(SwingConstants.LEFT);

        button.setBorder(
                new EmptyBorder(13, 15, 13, 10));

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 48));

        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        return button;
    }

    // ================= DASHBOARD CONTENT =================

    private void showDashboard() {

        contentPanel.removeAll();

        JPanel dashboard = new JPanel(new BorderLayout(20, 22));

        dashboard.setBackground(LIGHT_BG);

        JPanel headingPanel = new JPanel();
        headingPanel.setBackground(LIGHT_BG);
        headingPanel.setLayout(
                new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Good day, " + getFirstName());

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 28));

        heading.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Here is your BHEL employee account overview.");

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 14));

        subtitle.setForeground(TEXT_GRAY);

        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(8));
        headingPanel.add(subtitle);

        JPanel dashboardBody = new JPanel();
        dashboardBody.setBackground(LIGHT_BG);
        dashboardBody.setLayout(
                new BoxLayout(dashboardBody, BoxLayout.Y_AXIS));

        JLabel overviewHeading = sectionHeading("My Information");
        dashboardBody.add(overviewHeading);
        dashboardBody.add(Box.createVerticalStrut(14));

        JPanel summaryCards = new JPanel(new GridLayout(1, 3, 16, 16));
        summaryCards.setBackground(LIGHT_BG);
        summaryCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 132));
        summaryCards.add(createSummaryCard(
                "EMPLOYEE ID", String.valueOf(employeeId), "BHEL employee record"));
        summaryCards.add(createSummaryCard(
                "FULL NAME", getDisplayName(), "Your registered name"));
        summaryCards.add(createSummaryCard(
                "USERNAME", username, "Your employee portal account"));

        dashboardBody.add(summaryCards);
        dashboardBody.add(Box.createVerticalStrut(28));

        JLabel quickAccessHeading = sectionHeading("Quick Access");
        dashboardBody.add(quickAccessHeading);
        dashboardBody.add(Box.createVerticalStrut(14));

        JPanel quickAccess = new JPanel(new GridLayout(1, 3, 16, 16));
        quickAccess.setBackground(LIGHT_BG);
        quickAccess.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        quickAccess.add(createDashboardCard(
                "My Profile", "View and update your personal information",
                this::openProfile));
        quickAccess.add(createDashboardCard(
                "Family Details", "View and manage your family information",
                this::openProfile));
        quickAccess.add(createDashboardCard(
                "My Attendance", "Check in or out and view your attendance history",
                this::showMyAttendance));

        dashboardBody.add(quickAccess);

        JScrollPane scrollPane = new JScrollPane(dashboardBody);
        scrollPane.setBorder(null);
        scrollPane.setBackground(LIGHT_BG);
        scrollPane.getViewport().setBackground(LIGHT_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        dashboard.add(headingPanel, BorderLayout.NORTH);
        dashboard.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(dashboard, BorderLayout.CENTER);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JLabel sectionHeading(String text) {
        JLabel heading = new JLabel(text);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 19));
        heading.setForeground(NAVY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        return heading;
    }

    private JPanel createSummaryCard(String label, String value,
                                     String description) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.BOLD, 10));
        labelText.setForeground(TEXT_GRAY);

        JLabel valueText = new JLabel(value);
        valueText.setFont(new Font("Segoe UI", Font.BOLD, 19));
        valueText.setForeground(NAVY);

        JLabel descriptionText = new JLabel(description);
        descriptionText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descriptionText.setForeground(TEXT_GRAY);

        card.add(labelText);
        card.add(Box.createVerticalStrut(8));
        card.add(valueText);
        card.add(Box.createVerticalStrut(5));
        card.add(descriptionText);
        return card;
    }

    private String getDisplayName() {
        String firstName = employee.getFirstName() == null
                ? "" : employee.getFirstName().trim();
        String lastName = employee.getLastName() == null
                ? "" : employee.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? username : fullName;
    }

    private String getFirstName() {
        String firstName = employee.getFirstName();
        return firstName == null || firstName.trim().isEmpty()
                ? username : firstName.trim();
    }

    // ================= DASHBOARD CARD =================

    private JButton createDashboardCard(
            String title,
            String description,
            Runnable action) {

        JButton card = new JButton();

        card.setLayout(new BorderLayout(10, 12));
        card.setBackground(Color.WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(25, 22, 25, 22)
                ));

        card.setFocusPainted(false);
        card.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        JLabel cardTitle = new JLabel(title);

        cardTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 19));

        cardTitle.setForeground(NAVY);

        JLabel cardDescription = new JLabel(
                "<html><div style='width:230px;'>"
                        + description
                        + "</div></html>");

        cardDescription.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));

        cardDescription.setForeground(TEXT_GRAY);

        JPanel textPanel = new JPanel();

        textPanel.setOpaque(false);
        textPanel.setLayout(
                new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        textPanel.add(cardTitle);
        textPanel.add(Box.createVerticalStrut(12));
        textPanel.add(cardDescription);

        card.add(textPanel, BorderLayout.CENTER);

        card.addActionListener(e -> action.run());

        return card;
    }

    // ================= OPEN EMPLOYEE PROFILE =================

    private void openProfile() {

        try {

            EmployeeProfileFrame profileFrame =
                    new EmployeeProfileFrame(employee);

            profileFrame.setVisible(true);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to open Employee Profile:\n"
                            + e.getMessage(),
                    "Profile Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    private void showMyAttendance() {
        if (hrmService == null) {
            JOptionPane.showMessageDialog(this,
                    "The HRM server is unavailable. Start the server and try again.",
                    "My Attendance", JOptionPane.ERROR_MESSAGE);
            return;
        }

        contentPanel.removeAll();
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setBackground(LIGHT_BG);

        JLabel title = new JLabel("My Attendance");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Enter today's attendance times and remarks.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_GRAY);
        JPanel heading = new JPanel();
        heading.setBackground(LIGHT_BG);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitle);

        JPanel attendancePanel = new JPanel(new BorderLayout(0, 14));
        attendancePanel.setBackground(LIGHT_BG);

        JPanel formCard = new JPanel(new BorderLayout(0, 12));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 20, 16, 20)));
        JLabel formTitle = new JLabel("Submit Attendance");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        formTitle.setForeground(NAVY);

        AttendanceTimeInput startTimeInput = new AttendanceTimeInput(false);
        AttendanceTimeInput endTimeInput = new AttendanceTimeInput(true);
        JTextArea remarksArea = new JTextArea(3, 30);
        remarksArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        remarksArea.setBorder(new EmptyBorder(7, 8, 7, 8));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 7, 10);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        fields.add(attendanceFieldLabel("Start Time *"), c);
        c.gridx = 1; c.weightx = 1;
        fields.add(startTimeInput, c);
        c.gridx = 2; c.weightx = 0;
        fields.add(attendanceFieldLabel("End Time"), c);
        c.gridx = 3; c.weightx = 1;
        fields.add(endTimeInput, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        fields.add(attendanceFieldLabel("Remarks *"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1; c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        JScrollPane remarksScroll = new JScrollPane(remarksArea);
        remarksScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        fields.add(remarksScroll, c);

        JLabel dateNote = new JLabel("Attendance date: today (server set)  |  Use arrows or type hours/minutes; select AM/PM");
        dateNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateNote.setForeground(TEXT_GRAY);
        JButton submitButton = createAttendanceButton("Submit Attendance");
        JPanel formFooter = new JPanel(new BorderLayout());
        formFooter.setOpaque(false);
        formFooter.add(dateNote, BorderLayout.WEST);
        formFooter.add(submitButton, BorderLayout.EAST);

        JPanel formContent = new JPanel(new BorderLayout(0, 8));
        formContent.setOpaque(false);
        formContent.add(formTitle, BorderLayout.NORTH);
        formContent.add(fields, BorderLayout.CENTER);
        formContent.add(formFooter, BorderLayout.SOUTH);
        formCard.add(formContent, BorderLayout.CENTER);

        JLabel todayStatus = new JLabel("Loading today's attendance...");
        todayStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        todayStatus.setForeground(NAVY);
        DefaultTableModel model = new DefaultTableModel(new Object[]{
                "Date", "Start Time", "End Time", "Working Hours", "Status", "Remarks"
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable historyTable = new JTable(model);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.setRowHeight(29);
        historyTable.setGridColor(BORDER);
        historyTable.setSelectionBackground(new Color(220, 232, 248));
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(NAVY);
        historyTable.getTableHeader().setForeground(Color.WHITE);
        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        JButton refreshButton = new JButton("Refresh History");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setForeground(NAVY);
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setFocusPainted(false);

        Runnable refresh = () -> loadEmployeeAttendance(model, todayStatus,
                startTimeInput, endTimeInput, remarksArea);
        submitButton.addActionListener(e -> {
            String remarks = remarksArea.getText().trim();
            if (startTimeInput.isEmpty() || remarks.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Start Time and Remarks are required.",
                        "Attendance Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                LocalTime startTime = startTimeInput.getTime();
                LocalTime endTime = endTimeInput.isEmpty() ? null : endTimeInput.getTime();
                if (endTime != null && endTime.isBefore(startTime)) {
                    JOptionPane.showMessageDialog(this,
                            "End Time cannot be earlier than Start Time.",
                            "Attendance Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String response = hrmService.submitEmployeeAttendance(
                        employee.getAttendanceSessionToken(), startTime, endTime, remarks);
                boolean success = response != null
                        && response.startsWith("Attendance submitted successfully");
                JOptionPane.showMessageDialog(this, response,
                        "Attendance", success ? JOptionPane.INFORMATION_MESSAGE
                                : JOptionPane.WARNING_MESSAGE);
                if (success) remarksArea.setText("");
                refresh.run();
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Choose an hour, enter minutes from 00 to 59, and select AM or PM.",
                        "Attendance Validation", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                showAttendanceError("Unable to submit attendance", ex);
            }
        });
        refreshButton.addActionListener(e -> refresh.run());

        JPanel historyPanel = new JPanel(new BorderLayout(0, 8));
        historyPanel.setBackground(Color.WHITE);
        historyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(14, 16, 14, 16)));
        JPanel historyHeading = new JPanel(new BorderLayout());
        historyHeading.setOpaque(false);
        JLabel historyTitle = new JLabel("My Attendance History");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        historyTitle.setForeground(NAVY);
        historyHeading.add(todayStatus, BorderLayout.WEST);
        historyHeading.add(refreshButton, BorderLayout.EAST);
        historyPanel.add(historyHeading, BorderLayout.NORTH);
        historyPanel.add(historyScroll, BorderLayout.CENTER);

        attendancePanel.add(formCard, BorderLayout.NORTH);
        attendancePanel.add(historyPanel, BorderLayout.CENTER);
        page.add(heading, BorderLayout.NORTH);
        page.add(attendancePanel, BorderLayout.CENTER);
        contentPanel.add(page, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
        refresh.run();
    }

    private JLabel attendanceFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(NAVY);
        return label;
    }

    private JButton createAttendanceButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(NAVY);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadEmployeeAttendance(DefaultTableModel model, JLabel todayStatus,
                                        AttendanceTimeInput startTimeInput,
                                        AttendanceTimeInput endTimeInput,
                                        JTextArea remarksArea) {
        try {
            List<com.bhel.model.Attendance> records =
                    hrmService.getEmployeeAttendance(employee.getAttendanceSessionToken());
            model.setRowCount(0);
            LocalDate today = LocalDate.now();
            boolean foundToday = false;
            if (records != null) for (com.bhel.model.Attendance record : records) {
                if (record == null) continue;
                String checkIn = record.getCheckIn() == null ? "" : record.getCheckIn().toString();
                String checkOut = record.getCheckOut() == null ? "" : record.getCheckOut().toString();
                model.addRow(new Object[]{record.getAttendanceDate(), checkIn, checkOut,
                        attendanceHours(record), record.getStatus(), record.getRemarks()});
                if (today.equals(record.getAttendanceDate())) {
                    foundToday = true;
                    startTimeInput.setTime(record.getCheckIn());
                    endTimeInput.setTime(record.getCheckOut());
                    String remarks = record.getRemarks() == null ? "" : record.getRemarks();
                    remarks = remarks.replaceFirst("^Employee (entered|updated):\\s*", "");
                    if (!remarksArea.hasFocus()) remarksArea.setText(remarks);
                    todayStatus.setText("Today's record: " + checkIn
                            + (checkOut.isEmpty() ? " - End Time not entered" : " - " + checkOut)
                            + " (" + (record.getStatus() == null ? "" : record.getStatus()) + ")");
                }
            }
            if (!foundToday) todayStatus.setText("No attendance submitted for today yet.");
            todayStatus.setForeground(NAVY);
        } catch (Exception e) {
            String detail = e.getMessage() == null ? e.toString() : e.getMessage();
            todayStatus.setForeground(new Color(170, 35, 45));
            todayStatus.setText("<html><div style='width:650px'>Unable to load attendance:<br>"
                    + escapeHtml(detail) + "</div></html>");
            todayStatus.setToolTipText(detail);
        }
    }

    private class AttendanceTimeInput extends JPanel {
        private final JSpinner hour = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        private final JSpinner minute = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        private final JComboBox<String> meridiem = new JComboBox<>(new String[]{"AM", "PM"});
        private final JCheckBox enabledCheck = new JCheckBox("Set");
        private final boolean optional;

        AttendanceTimeInput(boolean optional) {
            super(new FlowLayout(FlowLayout.LEFT, 5, 0));
            this.optional = optional;
            setOpaque(false);
            styleTimeSpinner(hour, "Hour (1–12)", "00");
            styleTimeSpinner(minute, "Minutes (0–59)", "00");
            meridiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            meridiem.setBackground(Color.WHITE);
            meridiem.setPreferredSize(new Dimension(70, 34));
            enabledCheck.setOpaque(false);
            enabledCheck.setForeground(NAVY);
            enabledCheck.setVisible(optional);
            enabledCheck.addActionListener(e -> setControlsEnabled(enabledCheck.isSelected()));
            if (optional) setControlsEnabled(false);
            else setControlsEnabled(true);
            if (optional) add(enabledCheck);
            add(hour);
            add(new JLabel(":"));
            add(minute);
            add(meridiem);
        }

        boolean isEmpty() { return optional && !enabledCheck.isSelected(); }

        LocalTime getTime() {
            if (isEmpty()) return null;
            commitSpinner(hour);
            commitSpinner(minute);
            int h = (Integer) hour.getValue();
            int m = (Integer) minute.getValue();
            if ("AM".equals(meridiem.getSelectedItem())) h = h == 12 ? 0 : h;
            else h = h == 12 ? 12 : h + 12;
            return LocalTime.of(h, m);
        }

        void setTime(LocalTime time) {
            if (optional) {
                enabledCheck.setSelected(time != null);
                setControlsEnabled(time != null);
            }
            if (time == null) return;
            int h = time.getHour();
            meridiem.setSelectedItem(h < 12 ? "AM" : "PM");
            int twelveHour = h % 12;
            hour.setValue(twelveHour == 0 ? 12 : twelveHour);
            minute.setValue(time.getMinute());
        }

        private void styleTimeSpinner(JSpinner spinner, String tooltip, String format) {
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            spinner.setToolTipText(tooltip + "; use the arrows or type a value");
            spinner.setPreferredSize(new Dimension(spinner == hour ? 88 : 88, 34));
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, format);
            editor.getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 13));
            editor.getTextField().setHorizontalAlignment(JTextField.CENTER);
            editor.getTextField().setBorder(new EmptyBorder(3, 4, 3, 4));
            spinner.setEditor(editor);
        }

        private void commitSpinner(JSpinner spinner) {
            try {
                spinner.commitEdit();
            } catch (java.text.ParseException e) {
                throw new java.time.format.DateTimeParseException("Enter a valid time", "", 0, e);
            }
        }

        private void setControlsEnabled(boolean enabled) {
            hour.setEnabled(enabled);
            minute.setEnabled(enabled);
            meridiem.setEnabled(enabled);
        }
    }
    private String attendanceHours(com.bhel.model.Attendance record) {
        if (record.getCheckIn() == null || record.getCheckOut() == null) return "";
        Duration duration = Duration.between(record.getCheckIn(), record.getCheckOut());
        if (duration.isNegative()) duration = duration.plusDays(1);
        return String.format(Locale.ROOT, "%.2f", duration.toMinutes() / 60.0);
    }

    private void showAttendanceError(String title, Exception e) {
        JOptionPane.showMessageDialog(this, title + ":\n" + e.getMessage(),
                "Attendance Error", JOptionPane.ERROR_MESSAGE);
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void openLeaveManagement(String pageTitle) {
        if (hrmService == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "The HRM server is unavailable. Start the server and try again.",
                    pageTitle,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        LeaveManagementFrame leaveFrame =
                new LeaveManagementFrame(hrmService, employeeId);
        leaveFrame.setTitle("BHEL HRM - " + pageTitle);
        leaveFrame.setVisible(true);
    }

    private void showLeaveBalance() {
        contentPanel.removeAll();

        if (hrmService == null) {
            JOptionPane.showMessageDialog(this,
                    "The HRM server is unavailable. Start the server and try again.",
                    "Leave Balance", JOptionPane.ERROR_MESSAGE);
            showDashboard();
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate yearStart = LocalDate.of(today.getYear(), 1, 1);
        try {
            List<LeaveApplication> applications =
                    hrmService.getEmployeeLeaves(employeeId);

            double annualAccrued = 1.0;
            double sickAccrued = 1.0;
            double emergencyAccrued = 1.0;

            int annualUsed = approvedDaysTaken(
                    applications, "Annual Leave", yearStart, today);
            int sickUsed = approvedDaysTaken(
                    applications, "Sick Leave", yearStart, today);
            int emergencyUsed = approvedDaysTaken(
                    applications, "Emergency Leave", yearStart, today);

            JPanel balances = new JPanel(new GridLayout(1, 3, 16, 16));
            balances.setBackground(LIGHT_BG);
            balances.add(createBalanceCard(
                    "Annual Leave", annualAccrued, annualUsed));
            balances.add(createBalanceCard(
                    "Sick Leave", sickAccrued, sickUsed));
            balances.add(createBalanceCard(
                    "Emergency Leave", emergencyAccrued, emergencyUsed));

            JLabel policyNote = new JLabel("<html>Each leave type starts with "
                    + "a 1-day balance. Approved leave used is counted through today, using "
                    + "inclusive calendar days. Pending and future leave is not deducted.</html>");
            policyNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            policyNote.setForeground(TEXT_GRAY);

            JButton applicationsButton =
                    new JButton("View My Leave Applications");
            applicationsButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
            applicationsButton.setForeground(Color.WHITE);
            applicationsButton.setBackground(NAVY);
            applicationsButton.setFocusPainted(false);
            applicationsButton.setBorder(new EmptyBorder(11, 17, 11, 17));
            applicationsButton.addActionListener(
                    e -> openLeaveManagement("My Leave Applications"));

            JPanel footer = new JPanel();
            footer.setBackground(LIGHT_BG);
            footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
            footer.add(policyNote);
            footer.add(Box.createVerticalStrut(14));
            footer.add(applicationsButton);

            JPanel balanceContent = new JPanel(new BorderLayout(0, 22));
            balanceContent.setBackground(LIGHT_BG);
            balanceContent.add(balances, BorderLayout.NORTH);
            balanceContent.add(footer, BorderLayout.CENTER);

            JLabel title = new JLabel("My Leave Balance");
            title.setFont(new Font("Segoe UI", Font.BOLD, 28));
            title.setForeground(NAVY);

            JLabel subtitle = new JLabel(
                    "Your leave accrual and approved leave used this year.");
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            subtitle.setForeground(TEXT_GRAY);

            JPanel heading = new JPanel();
            heading.setBackground(LIGHT_BG);
            heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
            heading.add(title);
            heading.add(Box.createVerticalStrut(8));
            heading.add(subtitle);

            JPanel page = new JPanel(new BorderLayout(0, 18));
            page.setBackground(LIGHT_BG);
            page.add(heading, BorderLayout.NORTH);
            page.add(balanceContent, BorderLayout.CENTER);

            contentPanel.add(page, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load leave balance: " + e.getMessage(),
                    "Leave Balance Error", JOptionPane.ERROR_MESSAGE);
            showDashboard();
        }
    }

    private int approvedDaysTaken(List<LeaveApplication> applications,
                                  String leaveType,
                                  LocalDate yearStart,
                                  LocalDate throughDate) {
        int totalDays = 0;
        for (LeaveApplication application : applications) {
            if (application == null
                    || application.getStatus() == null
                    || !"Approved".equalsIgnoreCase(application.getStatus().trim())
                    || application.getLeaveType() == null
                    || !leaveType.equalsIgnoreCase(
                            application.getLeaveType().trim())
                    || application.getStartDate() == null
                    || application.getEndDate() == null) {
                continue;
            }

            LocalDate start = application.getStartDate().isBefore(yearStart)
                    ? yearStart : application.getStartDate();
            LocalDate end = application.getEndDate().isAfter(throughDate)
                    ? throughDate : application.getEndDate();
            if (!end.isBefore(start)) {
                totalDays += (int) ChronoUnit.DAYS.between(start, end) + 1;
            }
        }
        return totalDays;
    }

    private JPanel createBalanceCard(String leaveType, double accrued,
                                     int used) {
        double available = accrued - used;
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 18, 18)));

        JLabel typeLabel = new JLabel(leaveType);
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        typeLabel.setForeground(NAVY);

        JLabel availableValue = new JLabel(formatDays(available) + " days");
        availableValue.setFont(new Font("Segoe UI", Font.BOLD, 25));
        availableValue.setForeground(NAVY);

        JLabel accruedLabel = new JLabel(
                "Starting balance: " + formatDays(accrued) + " days");
        JLabel usedLabel = new JLabel("Approved used: " + used + " days");
        for (JLabel detail : new JLabel[]{accruedLabel, usedLabel}) {
            detail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            detail.setForeground(TEXT_GRAY);
        }

        card.add(typeLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(availableValue);
        card.add(Box.createVerticalStrut(12));
        card.add(accruedLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(usedLabel);
        return card;
    }

    private String formatDays(double days) {
        return days == Math.rint(days)
                ? String.format(Locale.ROOT, "%.0f", days)
                : String.format(Locale.ROOT, "%.1f", days);
    }

    // ================= LOGOUT =================

    private void logout() {

        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {

            try {
                if (hrmService != null && employee.getAttendanceSessionToken() != null) {
                    hrmService.endEmployeeAttendanceSession(
                            employee.getAttendanceSessionToken());
                }
            } catch (Exception ignored) {
                // The session expires on the server even if logout cannot reach it.
            }

            dispose();

            new com.bhel.client.LoginFrame().setVisible(true);
        }
    }
}
