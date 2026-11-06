
package com.bhel.ui;

import com.bhel.model.Employee;
import com.bhel.remote.HRMService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.RemoteException;
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
                "Employee ID",
                "First Name",
                "Last Name",
                "IC / Passport",
                "Username"
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

                tableModel.addRow(new Object[]{
                        employee.getEmployeeId(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getIcPassport(),
                        employee.getUsername()
                });
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

        String searchText =
                searchField.getText().trim().toLowerCase();

        tableModel.setRowCount(0);

        try {

            List<Employee> employees =
                    hrmService.getAllEmployees();

            for (Employee employee : employees) {

                String firstName =
                        employee.getFirstName().toLowerCase();

                String lastName =
                        employee.getLastName().toLowerCase();

                String username =
                        employee.getUsername().toLowerCase();

                String icPassport =
                        employee.getIcPassport().toLowerCase();

                if (firstName.contains(searchText)
                        || lastName.contains(searchText)
                        || username.contains(searchText)
                        || icPassport.contains(searchText)) {

                    tableModel.addRow(new Object[]{
                            employee.getEmployeeId(),
                            employee.getFirstName(),
                            employee.getLastName(),
                            employee.getIcPassport(),
                            employee.getUsername()
                    });
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

        int employeeId =
                Integer.parseInt(
                        tableModel.getValueAt(
                                selectedRow, 0).toString());

        String firstName =
                tableModel.getValueAt(selectedRow, 1).toString();

        String lastName =
                tableModel.getValueAt(selectedRow, 2).toString();

        String icPassport =
                tableModel.getValueAt(selectedRow, 3).toString();

        JTextField firstNameField =
                new JTextField(firstName);

        JTextField lastNameField =
                new JTextField(lastName);

        JTextField icPassportField =
                new JTextField(icPassport);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        panel.add(new JLabel("First Name:"));
        panel.add(firstNameField);

        panel.add(new JLabel("Last Name:"));
        panel.add(lastNameField);

        panel.add(new JLabel("IC / Passport:"));
        panel.add(icPassportField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Employee",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {

            String updatedFirstName =
                    firstNameField.getText().trim();

            String updatedLastName =
                    lastNameField.getText().trim();

            String updatedIcPassport =
                    icPassportField.getText().trim();

            if (updatedFirstName.isEmpty()
                    || updatedLastName.isEmpty()
                    || updatedIcPassport.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "All fields are required.");

                return;
            }

            try {

                String response =
                        hrmService.updateEmployee(
                                employeeId,
                                updatedFirstName,
                                updatedLastName,
                                updatedIcPassport);

                JOptionPane.showMessageDialog(
                        this,
                        response);

                if (response.contains("successfully")) {
                    loadEmployees();
                }

            } catch (RemoteException e) {

                JOptionPane.showMessageDialog(
                        this,
                        "Update failed: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
