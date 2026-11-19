
package com.bhel.ui;

import com.bhel.remote.HRMService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.*;

public class SystemSettingsFrame extends JFrame {

    private final Color NAVY = new Color(18, 39, 67);
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color WHITE = Color.WHITE;
    private final Color BORDER = new Color(220, 225, 235);
    private final Color TEXT_GRAY = new Color(105, 115, 130);

    private final HRMService hrmService;
    private final String accountUsername;

    private JTextField usernameField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    private JTextField checkInField;
    private JTextField checkOutField;
    private JTextField gracePeriodField;

    private JTextField annualLeaveField;
    private JTextField sickLeaveField;
    private JTextField emergencyLeaveField;

    private JCheckBox notificationsCheckBox;

    public SystemSettingsFrame(HRMService hrmService) {
        this(hrmService, "Admin");
    }

    public SystemSettingsFrame(HRMService hrmService, String accountUsername) {

        this.hrmService = hrmService;
        this.accountUsername = accountUsername == null
                || accountUsername.trim().isEmpty() ? "Admin" : accountUsername;

        setTitle("BHEL HRM - System Settings");
        setSize(1050, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());
        KeyboardNavigation.installEscape(this, this::dispose);
        getContentPane().setBackground(LIGHT_BG);

        createHeader();
        createMainContent();
    }

    // ================= HEADER =================

    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("System Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel(
                "Configure and manage BHEL HRM system preferences"
        );

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(195, 207, 225));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton closeButton = new JButton("Close");

        closeButton.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        closeButton.setForeground(NAVY);
        closeButton.setBackground(WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(
                new EmptyBorder(10, 20, 10, 20)
        );

        closeButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        closeButton.addActionListener(e -> dispose());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // ================= MAIN CONTENT =================

    private void createMainContent() {

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));

        mainPanel.setBackground(LIGHT_BG);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JTabbedPane tabs = new JTabbedPane();

        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(WHITE);
        tabs.setForeground(NAVY);

        tabs.addTab("Account", createAccountPanel());
        tabs.addTab("Attendance", createAttendancePanel());
        tabs.addTab("Leave", createLeavePanel());
        tabs.addTab("Database", createDatabasePanel());
        tabs.addTab("System Info", createSystemInfoPanel());

        mainPanel.add(tabs, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        bottomPanel.setOpaque(false);

        JLabel note = new JLabel(
                "Settings interface - database persistence is not yet connected."
        );

        note.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        note.setForeground(TEXT_GRAY);

        JButton saveButton = new JButton("Save Settings");

        saveButton.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        saveButton.setForeground(WHITE);
        saveButton.setBackground(NAVY);
        saveButton.setFocusPainted(false);
        saveButton.setBorder(
                new EmptyBorder(12, 25, 12, 25)
        );

        saveButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        saveButton.addActionListener(e -> saveSettings());

        bottomPanel.add(note, BorderLayout.WEST);
        bottomPanel.add(saveButton, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    // ================= ACCOUNT SETTINGS =================

    private JPanel createAccountPanel() {

        JPanel panel = createSectionPanel();

        addSectionHeading(
                panel,
                "Account Settings",
                "Manage your administrator account information."
        );

        JLabel resetHelp = new JLabel(
                "Forgot an employee's password? HR can reset it without their old password.");
        resetHelp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resetHelp.setForeground(TEXT_GRAY);

        JButton resetEmployeePasswordButton =
                new JButton("Reset Employee Password");
        resetEmployeePasswordButton.setFont(
                new Font("Segoe UI", Font.BOLD, 12));
        resetEmployeePasswordButton.setForeground(WHITE);
        resetEmployeePasswordButton.setBackground(NAVY);
        resetEmployeePasswordButton.setFocusPainted(false);
        resetEmployeePasswordButton.setBorder(
                new EmptyBorder(9, 14, 9, 14));
        resetEmployeePasswordButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetEmployeePasswordButton.addActionListener(
                e -> openEmployeePasswordResetDialog());

        panel.add(Box.createVerticalStrut(14));
        panel.add(resetHelp);
        panel.add(Box.createVerticalStrut(6));
        panel.add(resetEmployeePasswordButton);
        panel.add(Box.createVerticalStrut(14));

        usernameField = new JTextField(accountUsername);
        usernameField.setEditable(false);
        currentPasswordField = new JPasswordField();
        newPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        addField(panel, "Username", usernameField);
        addField(panel, "Current Password", currentPasswordField);
        addField(panel, "New Password", newPasswordField);
        addField(panel, "Confirm New Password", confirmPasswordField);

        JLabel information = new JLabel(
                "Password changes require backend authentication."
        );

        information.setFont(
                new Font("Segoe UI", Font.ITALIC, 12)
        );

        information.setForeground(TEXT_GRAY);

        panel.add(information);

        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.addActionListener(e -> changePassword());
        panel.add(Box.createVerticalStrut(10));
        panel.add(changePasswordButton);

        return panel;
    }

    // ================= ATTENDANCE SETTINGS =================

    private JPanel createAttendancePanel() {

        JPanel panel = createSectionPanel();

        addSectionHeading(
                panel,
                "Attendance Settings",
                "Configure the standard employee working schedule."
        );

        checkInField = new JTextField("09:00");
        checkOutField = new JTextField("17:00");
        gracePeriodField = new JTextField("15");

        addField(
                panel,
                "Standard Check-in Time (HH:mm)",
                checkInField
        );

        addField(
                panel,
                "Standard Check-out Time (HH:mm)",
                checkOutField
        );

        addField(
                panel,
                "Late Arrival Grace Period (Minutes)",
                gracePeriodField
        );

        notificationsCheckBox = new JCheckBox(
                "Enable attendance-related notifications"
        );

        notificationsCheckBox.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        notificationsCheckBox.setBackground(WHITE);
        notificationsCheckBox.setForeground(NAVY);

        panel.add(Box.createVerticalStrut(15));
        panel.add(notificationsCheckBox);

        return panel;
    }

    // ================= LEAVE SETTINGS =================

    private JPanel createLeavePanel() {

        JPanel panel = createSectionPanel();

        addSectionHeading(
                panel,
                "Leave Settings",
                "Configure the standard leave allowance for employees."
        );

        annualLeaveField = new JTextField("14");
        sickLeaveField = new JTextField("14");
        emergencyLeaveField = new JTextField("3");

        addField(
                panel,
                "Annual Leave Allowance (Days)",
                annualLeaveField
        );

        addField(
                panel,
                "Sick Leave Allowance (Days)",
                sickLeaveField
        );

        addField(
                panel,
                "Emergency Leave Allowance (Days)",
                emergencyLeaveField
        );

        JLabel note = new JLabel(
                "These are proposed default values. Confirm your organisation's policy."
        );

        note.setFont(
                new Font("Segoe UI", Font.ITALIC, 12)
        );

        note.setForeground(TEXT_GRAY);

        panel.add(Box.createVerticalStrut(15));
        panel.add(note);

        return panel;
    }

    // ================= DATABASE SETTINGS =================

    private JPanel createDatabasePanel() {

        JPanel panel = createSectionPanel();

        addSectionHeading(
                panel,
                "Database Settings",
                "View the current HRM server connection information."
        );

        addReadOnlyField(
                panel,
                "Database Type",
                "PostgreSQL"
        );

        addReadOnlyField(
                panel,
                "Database Connection",
                "Managed by DBConnection.java"
        );

        addReadOnlyField(
                panel,
                "RMI Registry",
                "localhost:1099"
        );

        addReadOnlyField(
                panel,
                "Remote Service",
                "HRMService"
        );

        String connectionStatus = hrmService != null
                ? "RMI service reference available"
                : "RMI service not connected";

        addReadOnlyField(
                panel,
                "RMI Connection Status",
                connectionStatus
        );

        JLabel note = new JLabel(
                "Database credentials are managed securely on the server."
        );

        note.setFont(
                new Font("Segoe UI", Font.ITALIC, 12)
        );

        note.setForeground(TEXT_GRAY);

        panel.add(Box.createVerticalStrut(15));
        panel.add(note);

        return panel;
    }

    // ================= SYSTEM INFORMATION =================

    private JPanel createSystemInfoPanel() {

        JPanel panel = createSectionPanel();

        addSectionHeading(
                panel,
                "System Information",
                "Application and runtime environment information."
        );

        addReadOnlyField(
                panel,
                "Application Name",
                "BHEL Human Resource Management System"
        );

        addReadOnlyField(
                panel,
                "Application Version",
                "1.0"
        );

        addReadOnlyField(
                panel,
                "Java Version",
                System.getProperty("java.version")
        );

        addReadOnlyField(
                panel,
                "Operating System",
                System.getProperty("os.name")
        );

        addReadOnlyField(
                panel,
                "Operating System Version",
                System.getProperty("os.version")
        );

        addReadOnlyField(
                panel,
                "Java Vendor",
                System.getProperty("java.vendor")
        );

        return panel;
    }

    // ================= PANEL DESIGN =================

    private JPanel createSectionPanel() {

        JPanel panel = new JPanel();

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBackground(WHITE);
        panel.setBorder(
                new EmptyBorder(25, 30, 25, 30)
        );

        return panel;
    }

    // ================= SECTION HEADING =================

    private void addSectionHeading(
            JPanel panel,
            String title,
            String description) {

        JLabel heading = new JLabel(title);

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 21)
        );

        heading.setForeground(NAVY);

        JLabel subtitle = new JLabel(description);

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        subtitle.setForeground(TEXT_GRAY);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(7));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(25));
    }

    // ================= FORM FIELD =================

    private void addField(
            JPanel panel,
            String labelText,
            JComponent field) {

        JLabel label = new JLabel(labelText);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        label.setForeground(NAVY);

        field.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        field.setPreferredSize(new Dimension(350, 38));
        field.setMaximumSize(new Dimension(450, 38));

        if (field instanceof JTextField) {
            field.setBorder(
                    BorderFactory.createCompoundBorder(
                            new LineBorder(BORDER),
                            new EmptyBorder(5, 10, 5, 10)
                    )
            );
        }

        panel.add(label);
        panel.add(Box.createVerticalStrut(7));
        panel.add(field);
        panel.add(Box.createVerticalStrut(18));
    }

    // ================= READ ONLY FIELD =================

    private void addReadOnlyField(
            JPanel panel,
            String labelText,
            String value) {

        JLabel label = new JLabel(labelText);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        label.setForeground(NAVY);

        JTextField field = new JTextField(value);

        field.setEditable(false);
        field.setBackground(new Color(247, 249, 252));
        field.setForeground(TEXT_GRAY);
        field.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER),
                        new EmptyBorder(5, 10, 5, 10)
                )
        );

        field.setMaximumSize(new Dimension(500, 38));

        panel.add(label);
        panel.add(Box.createVerticalStrut(7));
        panel.add(field);
        panel.add(Box.createVerticalStrut(18));
    }

    // ================= SAVE SETTINGS =================

    private void saveSettings() {

        try {

            String checkIn = checkInField.getText().trim();
            String checkOut = checkOutField.getText().trim();

            int gracePeriod = Integer.parseInt(
                    gracePeriodField.getText().trim()
            );

            int annualLeave = Integer.parseInt(
                    annualLeaveField.getText().trim()
            );

            int sickLeave = Integer.parseInt(
                    sickLeaveField.getText().trim()
            );

            int emergencyLeave = Integer.parseInt(
                    emergencyLeaveField.getText().trim()
            );

            java.time.LocalTime in =
                    java.time.LocalTime.parse(checkIn);

            java.time.LocalTime out =
                    java.time.LocalTime.parse(checkOut);

            if (!out.isAfter(in)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Check-out time must be after check-in time.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (gracePeriod < 0
                    || annualLeave < 0
                    || sickLeave < 0
                    || emergencyLeave < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Leave allowances and grace period cannot be negative.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Settings validation successful.\n\n"
                            + "The settings interface is ready, but permanent "
                            + "database saving has not yet been implemented.",
                    "Settings",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid whole numbers for grace period "
                            + "and leave allowances.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (java.time.format.DateTimeParseException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter time in HH:mm format, for example 09:00.",
                    "Invalid Time",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void changePassword() {
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (currentPassword.isEmpty() || newPassword.isEmpty()
                || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Enter your current password and both new password fields.",
                    "Password Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "New password and confirmation do not match.",
                    "Password Mismatch", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (newPassword.equals(currentPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Choose a new password different from your current password.",
                    "Invalid Password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (hrmService == null) {
            JOptionPane.showMessageDialog(this, "Server connection is unavailable.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String response = hrmService.changePassword(
                    accountUsername, currentPassword, newPassword);
            JOptionPane.showMessageDialog(this, response, "Change Password",
                    response.startsWith("Password changed successfully")
                            ? JOptionPane.INFORMATION_MESSAGE
                            : JOptionPane.ERROR_MESSAGE);
            if (response.startsWith("Password changed successfully")) {
                currentPasswordField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Password change request failed: " + e.getMessage(),
                    "Change Password Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openEmployeePasswordResetDialog() {
        if (hrmService == null) {
            JOptionPane.showMessageDialog(this,
                    "Server connection is unavailable.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField employeeUsernameField = new JTextField(18);
        JPasswordField adminPasswordField = new JPasswordField(18);
        JPasswordField newPasswordField = new JPasswordField(18);
        JPasswordField confirmPasswordField = new JPasswordField(18);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Employee username:"));
        form.add(employeeUsernameField);
        form.add(new JLabel("Your HR password:"));
        form.add(adminPasswordField);
        form.add(new JLabel("Temporary new password:"));
        form.add(newPasswordField);
        form.add(new JLabel("Confirm new password:"));
        form.add(confirmPasswordField);

        int choice = JOptionPane.showConfirmDialog(this, form,
                "Reset Employee Password", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String employeeUsername = employeeUsernameField.getText().trim();
        char[] adminPasswordChars = adminPasswordField.getPassword();
        char[] newPasswordChars = newPasswordField.getPassword();
        char[] confirmPasswordChars = confirmPasswordField.getPassword();

        if (employeeUsername.isEmpty() || adminPasswordChars.length == 0
                || newPasswordChars.length == 0
                || confirmPasswordChars.length == 0) {
            java.util.Arrays.fill(adminPasswordChars, '\0');
            java.util.Arrays.fill(newPasswordChars, '\0');
            java.util.Arrays.fill(confirmPasswordChars, '\0');
            JOptionPane.showMessageDialog(this,
                    "Complete every field to reset the employee password.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!java.util.Arrays.equals(newPasswordChars, confirmPasswordChars)) {
            java.util.Arrays.fill(adminPasswordChars, '\0');
            java.util.Arrays.fill(newPasswordChars, '\0');
            java.util.Arrays.fill(confirmPasswordChars, '\0');
            JOptionPane.showMessageDialog(this,
                    "The new password fields do not match.",
                    "Password Mismatch", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String response = hrmService.adminResetEmployeePassword(
                    accountUsername,
                    new String(adminPasswordChars),
                    employeeUsername,
                    new String(newPasswordChars));
            boolean reset = response != null
                    && response.startsWith(
                            "Employee password reset successfully");
            JOptionPane.showMessageDialog(this, response,
                    "Reset Employee Password",
                    reset ? JOptionPane.INFORMATION_MESSAGE
                            : JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Password reset request failed: " + e.getMessage(),
                    "Reset Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            java.util.Arrays.fill(adminPasswordChars, '\0');
            java.util.Arrays.fill(newPasswordChars, '\0');
            java.util.Arrays.fill(confirmPasswordChars, '\0');
        }
    }
}
