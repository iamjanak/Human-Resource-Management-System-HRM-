
package com.bhel.ui;

import com.bhel.model.Employee;
import com.bhel.remote.HRMService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public class EmployeeManagementFrame extends JFrame {

    private final Color NAVY = new Color(22, 39, 63);
    private final Color LIGHT_BG = new Color(245, 247, 250);

    private final HRMService hrmService;

    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public EmployeeManagementFrame(HRMService hrmService) {

        this.hrmService = hrmService;

        setTitle("BHEL HRM - Employee Management");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());
        KeyboardNavigation.installEscape(this, this::dispose);
        getContentPane().setBackground(LIGHT_BG);

        createHeader();
        createTable();
        createButtons();

        loadEmployees();
    }

    // HEADER
    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(
                BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Employee Management");

        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        header.add(title, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);
    }

    // EMPLOYEE TABLE
    private void createTable() {

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(LIGHT_BG);
        centerPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBackground(LIGHT_BG);

        JLabel searchLabel = new JLabel("Search Employee:");

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(250, 35));

        JButton searchButton = new JButton("Search");

        searchButton.addActionListener(e -> searchEmployees());

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        String[] columns = {
                "Employee ID", "User ID", "Created At", "First Name",
                "Last Name", "IC / Passport", "Username", "Gender",
                "Date of Birth", "Phone", "Email", "Address",
                "Department", "Designation", "Joining Date",
                "Employment Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        employeeTable = new JTable(tableModel);

        employeeTable.setRowHeight(28);
        employeeTable.setFont(new Font("Arial", Font.PLAIN, 14));

        employeeTable.getTableHeader()
                .setBackground(NAVY);

        employeeTable.getTableHeader()
                .setForeground(Color.WHITE);

        employeeTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane =
                new JScrollPane(employeeTable);

        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    // BUTTONS
    private void createButtons() {

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 15, 15));

        buttonPanel.setBackground(LIGHT_BG);

        JButton refreshButton = new JButton("Refresh");
        JButton editButton = new JButton("Edit Employee");
        JButton closeButton = new JButton("Close");

        refreshButton.addActionListener(e -> loadEmployees());

        editButton.addActionListener(e -> editEmployee());

        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(refreshButton);
        buttonPanel.add(editButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // LOAD EMPLOYEES FROM RMI SERVER
    private void loadEmployees() {

        try {

            List<Employee> employees =
                    hrmService.getAllEmployees();

            tableModel.setRowCount(0);

            for (Employee employee : employees) {
                tableModel.addRow(employeeRow(employee));
            }

        } catch (RemoteException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load employee records.\n"
                            + e.getMessage(),
                    "Server Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // SEARCH EMPLOYEES
    private void searchEmployees() {

        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            loadEmployees();
            return;
        }

        tableModel.setRowCount(0);

        try {

            List<Employee> employees =
                    hrmService.getAllEmployees();

            for (Employee employee : employees) {
                Object[] values = employeeRow(employee);
                boolean matches = false;
                for (Object value : values) {
                    if (value != null && value.toString().toLowerCase()
                            .contains(searchText)) {
                        matches = true;
                        break;
                    }
                }
                if (matches) {
                    tableModel.addRow(values);
                }
            }

        } catch (RemoteException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Search failed: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // EDIT EMPLOYEE
    private void editEmployee() {

        int selectedRow =
                employeeTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an employee first.");

            return;
        }

        int employeeId = Integer.parseInt(
                tableModel.getValueAt(selectedRow, 0).toString());

        try {
            Employee employee = hrmService.getEmployeeById(employeeId);
            if (employee == null) {
                JOptionPane.showMessageDialog(this,
                        "Employee record was not found.",
                        "Edit Employee", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JTextField firstNameField = new JTextField(employee.getFirstName());
            JTextField lastNameField = new JTextField(employee.getLastName());
            JTextField icPassportField = new JTextField(employee.getIcPassport());
            JComboBox<String> genderField = new JComboBox<>(
                    new String[]{"", "Female", "Male", "Other"});
            genderField.setSelectedItem(employee.getGender() == null
                    ? "" : employee.getGender());
            JTextField dobField = new JTextField(dateText(employee.getDateOfBirth()));
            JTextField phoneField = new JTextField(valueText(employee.getPhone()));
            JTextField emailField = new JTextField(valueText(employee.getEmail()));
            JTextField addressField = new JTextField(valueText(employee.getAddress()));
            JTextField departmentField = new JTextField(valueText(employee.getDepartment()));
            JTextField designationField = new JTextField(valueText(employee.getDesignation()));
            JTextField joiningField = new JTextField(dateText(employee.getJoiningDate()));
            JComboBox<String> statusField = new JComboBox<>(new String[]{
                    "Active", "On Leave", "Resigned", "Terminated"});
            statusField.setSelectedItem(employee.getEmploymentStatus() == null
                    ? "Active" : employee.getEmploymentStatus());

            JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
            addEditField(form, "First Name", firstNameField);
            addEditField(form, "Last Name", lastNameField);
            addEditField(form, "IC / Passport", icPassportField);
            addEditField(form, "Gender", genderField);
            addEditField(form, "Date of Birth (YYYY-MM-DD)", dobField);
            addEditField(form, "Phone", phoneField);
            addEditField(form, "Email", emailField);
            addEditField(form, "Address", addressField);
            addEditField(form, "Department", departmentField);
            addEditField(form, "Designation", designationField);
            addEditField(form, "Joining Date (YYYY-MM-DD)", joiningField);
            addEditField(form, "Employment Status", statusField);

            JScrollPane scrollForm = new JScrollPane(form);
            scrollForm.setPreferredSize(new Dimension(520, 480));

            int result = JOptionPane.showConfirmDialog(
                    this, scrollForm, "Edit Employee",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            LocalDate dateOfBirth = parseOptionalDate(dobField.getText());
            LocalDate joiningDate = parseOptionalDate(joiningField.getText());
            String response = hrmService.updateEmployee(
                    employeeId,
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    icPassportField.getText().trim(),
                    (String) genderField.getSelectedItem(),
                    dateOfBirth,
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    addressField.getText().trim(),
                    departmentField.getText().trim(),
                    designationField.getText().trim(),
                    joiningDate,
                    (String) statusField.getSelectedItem());

            JOptionPane.showMessageDialog(this, response);
            if (response != null && response.contains("successfully")) {
                loadEmployees();
            }
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Dates must use YYYY-MM-DD format.",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                    "Update failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Object[] employeeRow(Employee employee) {
        return new Object[]{
                employee.getEmployeeId(), employee.getUserId(),
                employee.getCreatedAt(), employee.getFirstName(),
                employee.getLastName(), employee.getIcPassport(),
                employee.getUsername(), employee.getGender(),
                employee.getDateOfBirth(), employee.getPhone(),
                employee.getEmail(), employee.getAddress(),
                employee.getDepartment(), employee.getDesignation(),
                employee.getJoiningDate(), employee.getEmploymentStatus()
        };
    }

    private void addEditField(JPanel form, String label, JComponent field) {
        form.add(new JLabel(label + ":"));
        form.add(field);
    }

    private LocalDate parseOptionalDate(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : LocalDate.parse(trimmed);
    }

    private String dateText(LocalDate value) {
        return value == null ? "" : value.toString();
    }

    private String valueText(String value) {
        return value == null ? "" : value;
    }
}
