
package com.bhel.ui;

import com.bhel.client.HRMClient;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class RegisterEmployeeFrame extends JFrame {

    private final Color NAVY = new Color(22, 39, 63);
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color TEXT = new Color(55, 65, 81);

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField icPassportField;
    private JComboBox<String> genderField;
    private JTextField dateOfBirthField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField addressField;
    private JTextField departmentField;
    private JTextField designationField;
    private JTextField joiningDateField;
    private JComboBox<String> employmentStatusField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private HRMClient client = new HRMClient();

    public RegisterEmployeeFrame() {

        setTitle("BHEL HRM - Register Employee");
        setSize(780, 760);
        setMinimumSize(new Dimension(700, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        KeyboardNavigation.installEscape(this, this::dispose);

        // Header
        JPanel header = new JPanel();
        header.setBackground(NAVY);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("Register New Employee");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Create employee information and login credentials");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.WHITE);

        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);

        add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(LIGHT_BG);
        form.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        icPassportField = new JTextField(20);
        genderField = new JComboBox<>(new String[]{
                "", "Female", "Male", "Other"});
        dateOfBirthField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        addressField = new JTextField(20);
        departmentField = new JTextField(20);
        designationField = new JTextField(20);
        joiningDateField = new JTextField(LocalDate.now().toString(), 20);
        employmentStatusField = new JComboBox<>(new String[]{
                "Active", "On Leave", "Resigned", "Terminated"});
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);

        addFormField(form, gbc, 0, "First Name", firstNameField);
        addFormField(form, gbc, 1, "Last Name", lastNameField);
        addFormField(form, gbc, 2, "IC / Passport Number", icPassportField);
        addFormField(form, gbc, 3, "Gender", genderField);
        addFormField(form, gbc, 4, "Date of Birth (YYYY-MM-DD)", dateOfBirthField);
        addFormField(form, gbc, 5, "Phone", phoneField);
        addFormField(form, gbc, 6, "Email", emailField);
        addFormField(form, gbc, 7, "Address", addressField);
        addFormField(form, gbc, 8, "Department", departmentField);
        addFormField(form, gbc, 9, "Designation", designationField);
        addFormField(form, gbc, 10, "Joining Date (YYYY-MM-DD)", joiningDateField);
        addFormField(form, gbc, 11, "Employment Status", employmentStatusField);
        addFormField(form, gbc, 12, "Username", usernameField);
        addFormField(form, gbc, 13, "Password", passwordField);

        add(new JScrollPane(form), BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(LIGHT_BG);
        buttonPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 20, 25, 30));

        JButton clearButton = new JButton("Clear");
        JButton registerButton = new JButton("Register Employee");

        registerButton.setBackground(NAVY);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setFocusPainted(false);

        clearButton.addActionListener(e -> clearFields());
        registerButton.addActionListener(e -> registerEmployee());

        buttonPanel.add(clearButton);
        buttonPanel.add(registerButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormField(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String labelText,
            JComponent field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT);

        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;

        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(field, gbc);
    }

    private void registerEmployee() {

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String icPassport = icPassportField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        LocalDate dateOfBirth;
        LocalDate joiningDate;

        try {
            dateOfBirth = parseOptionalDate(dateOfBirthField.getText());
            joiningDate = parseOptionalDate(joiningDateField.getText());
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Dates must use YYYY-MM-DD format.",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (firstName.isEmpty()
                || lastName.isEmpty()
                || icPassport.isEmpty()
                || username.isEmpty()
                || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please fill in all fields.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);

            return;
        }

        try {

            String result = client.registerEmployee(
                    firstName,
                    lastName,
                    icPassport,
                    (String) genderField.getSelectedItem(),
                    dateOfBirth,
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    addressField.getText().trim(),
                    departmentField.getText().trim(),
                    designationField.getText().trim(),
                    joiningDate,
                    (String) employmentStatusField.getSelectedItem(),
                    username,
                    password);

            if ("Employee registered successfully!".equals(result)) {

                JOptionPane.showMessageDialog(
                        this,
                        result,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                clearFields();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        result,
                        "Registration Failed",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to register employee: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {

        firstNameField.setText("");
        lastNameField.setText("");
        icPassportField.setText("");
        genderField.setSelectedIndex(0);
        dateOfBirthField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
        departmentField.setText("");
        designationField.setText("");
        joiningDateField.setText(LocalDate.now().toString());
        employmentStatusField.setSelectedItem("Active");
        usernameField.setText("");
        passwordField.setText("");
    }

    private LocalDate parseOptionalDate(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : LocalDate.parse(trimmed);
    }
}
