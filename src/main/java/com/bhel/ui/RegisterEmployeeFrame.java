
package com.bhel.ui;

import com.bhel.client.HRMClient;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class RegisterEmployeeFrame extends JFrame {

    private final Color NAVY = new Color(18, 39, 67);
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color TEXT = new Color(55, 65, 81);
    private final Color BORDER = new Color(220, 225, 235);
    private final Color GREEN = new Color(0, 111, 96);

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
        setSize(940, 660);
        setMinimumSize(new Dimension(760, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        KeyboardNavigation.installEscape(this, this::dispose);

        JPanel header = new JPanel();
        header.setBackground(NAVY);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(20, 26, 20, 26));

        JLabel title = new JLabel("Register New Employee");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Create employee information and login credentials");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(210, 220, 235));

        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);

        add(header, BorderLayout.NORTH);

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
        styleField(firstNameField);
        styleField(lastNameField);
        styleField(icPassportField);
        styleField(genderField);
        styleField(dateOfBirthField);
        styleField(phoneField);
        styleField(emailField);
        styleField(addressField);
        styleField(departmentField);
        styleField(designationField);
        styleField(joiningDateField);
        styleField(employmentStatusField);
        styleField(usernameField);
        styleField(passwordField);

        JPanel form = new RegistrationContentPanel();
        form.setBackground(LIGHT_BG);
        form.setBorder(BorderFactory.createEmptyBorder(18, 24, 22, 24));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(createSection("Personal Information",
                new String[]{"First name *", "Last name *", "IC / Passport number *",
                        "Gender", "Date of birth (YYYY-MM-DD)", "Phone", "Email", "Address"},
                new JComponent[]{firstNameField, lastNameField, icPassportField,
                        genderField, dateOfBirthField, phoneField, emailField, addressField}));
        form.add(Box.createVerticalStrut(14));
        form.add(createSection("Employment Details",
                new String[]{"Department", "Designation", "Joining date (YYYY-MM-DD)", "Employment status"},
                new JComponent[]{departmentField, designationField, joiningDateField, employmentStatusField}));
        form.add(Box.createVerticalStrut(14));
        form.add(createSection("Login Credentials",
                new String[]{"Username *", "Password *"},
                new JComponent[]{usernameField, passwordField}));

        JScrollPane formScroll = new JScrollPane(form,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(LIGHT_BG);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(formScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setBackground(LIGHT_BG);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 24, 16, 24));
        JLabel hint = new JLabel("Fields marked * are required. Dates use YYYY-MM-DD format.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(new Color(105, 115, 130));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton clearButton = new JButton("Clear Form");
        JButton registerButton = new JButton("Register Employee");
        styleButton(clearButton, false);
        styleButton(registerButton, true);
        clearButton.addActionListener(e -> clearFields());
        registerButton.addActionListener(e -> registerEmployee());
        actions.add(clearButton);
        actions.add(registerButton);
        footer.add(hint, BorderLayout.CENTER);
        footer.add(actions, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createSection(String title, String[] labels, JComponent[] fields) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(17, 19, 18, 19)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heading.setForeground(NAVY);
        JPanel grid = new JPanel(new GridLayout(0, 2, 18, 13));
        grid.setOpaque(false);
        for (int i = 0; i < labels.length; i++) {
            grid.add(createField(labels[i], fields[i]));
        }
        card.add(heading, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createField(String labelText, JComponent field) {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(label);
        wrapper.add(Box.createVerticalStrut(6));
        wrapper.add(field);
        return wrapper;
    }

    private void styleField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(220, 36));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        if (field instanceof JTextField textField) {
            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        } else if (field instanceof JComboBox<?> comboBox) {
            comboBox.setBackground(Color.WHITE);
        }
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(primary ? Color.WHITE : NAVY);
        button.setBackground(primary ? NAVY : Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? NAVY : BORDER),
                BorderFactory.createEmptyBorder(9, 15, 9, 15)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static final class RegistrationContentPanel extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle rect, int orientation, int direction) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle rect, int orientation, int direction) {
            return Math.max(16, rect.height - 16);
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
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
