
package com.bhel.ui;

import com.bhel.model.Department;
import com.bhel.remote.HRMService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.List;

public class DepartmentManagementFrame extends JFrame {

    private final Color NAVY = new Color(18, 39, 67);
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color BORDER = new Color(220, 225, 235);

    private final HRMService hrmService;

    private JTable departmentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public DepartmentManagementFrame(HRMService hrmService) {

        this.hrmService = hrmService;

        setTitle("Department Management - BHEL HRM");
        setSize(950, 600);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(LIGHT_BG);

        createHeader();
        createContent();
        loadDepartments();
    }

    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Department Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Add, view, update and remove company departments");

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(210, 220, 235));

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));

        labels.add(title);
        labels.add(Box.createVerticalStrut(5));
        labels.add(subtitle);

        header.add(labels, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);
    }

    private void createContent() {

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(LIGHT_BG);
        main.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Search
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(250, 38));
        searchField.putClientProperty(
                "JTextField.placeholderText", "Search departments...");

        searchField.addActionListener(e -> searchDepartments());

        JButton searchButton = createButton("Search");
        searchButton.addActionListener(e -> searchDepartments());

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Actions
        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton add = createButton("Add Department");
        JButton edit = createButton("Edit Selected");
        JButton delete = createButton("Delete Selected");
        JButton refresh = createButton("Refresh");

        add.addActionListener(e -> addDepartment());
        edit.addActionListener(e -> editDepartment());
        delete.addActionListener(e -> deleteDepartment());

        refresh.addActionListener(e -> {
            searchField.setText("");
            loadDepartments();
        });

        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        actions.add(refresh);

        toolbar.add(searchPanel, BorderLayout.CENTER);
        toolbar.add(actions, BorderLayout.EAST);

        // Table
        String[] columns = {
                "Department ID",
                "Department Name",
                "Description"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        departmentTable = new JTable(tableModel);
        departmentTable.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));
        departmentTable.setRowHeight(32);
        departmentTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        departmentTable.setGridColor(BORDER);
        departmentTable.setShowVerticalLines(false);

        departmentTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13));
        departmentTable.getTableHeader().setBackground(NAVY);
        departmentTable.getTableHeader().setForeground(Color.WHITE);
        departmentTable.getTableHeader().setPreferredSize(
                new Dimension(0, 38));

        departmentTable.getColumnModel().getColumn(0)
                .setPreferredWidth(110);
        departmentTable.getColumnModel().getColumn(1)
                .setPreferredWidth(220);
        departmentTable.getColumnModel().getColumn(2)
                .setPreferredWidth(480);

        JScrollPane scroll = new JScrollPane(departmentTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));

        JLabel hint = new JLabel(
                "Select a department in the table to edit or delete it.");

        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(new Color(105, 115, 130));

        JLabel heading = new JLabel("Department Records");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(NAVY);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
        tablePanel.setOpaque(false);

        tablePanel.add(heading, BorderLayout.NORTH);
        tablePanel.add(scroll, BorderLayout.CENTER);
        tablePanel.add(hint, BorderLayout.SOUTH);

        main.add(toolbar, BorderLayout.NORTH);
        main.add(tablePanel, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);
    }

    private JButton createButton(String text) {

        JButton button = new JButton(text);

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(NAVY);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 13, 10, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    // LOAD DEPARTMENTS
    private void loadDepartments() {

        try {

            List<Department> departments =
                    hrmService.getAllDepartments();

            tableModel.setRowCount(0);

            if (departments != null) {

                for (Department department : departments) {

                    if (department != null) {

                        tableModel.addRow(new Object[]{
                                department.getDepartmentId(),
                                department.getDepartmentName(),
                                department.getDescription()
                        });
                    }
                }
            }

        } catch (RemoteException e) {
            showError("Unable to load departments: " + e.getMessage());
        }
    }

    // ADD DEPARTMENT
    private void addDepartment() {

        JTextField name = new JTextField();
        JTextArea description = new JTextArea(4, 25);

        description.setLineWrap(true);
        description.setWrapStyleWord(true);

        JPanel fields = new JPanel(new GridLayout(0, 1, 4, 4));
        fields.add(new JLabel("Department Name *"));
        fields.add(name);
        fields.add(new JLabel("Description"));

        JPanel panel = new JPanel(new BorderLayout(5, 8));
        panel.add(fields, BorderLayout.NORTH);
        panel.add(new JScrollPane(description), BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Add Department",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String departmentName = name.getText().trim();

        if (departmentName.isEmpty()) {
            showError("Department name is required.");
            return;
        }

        try {

            Department department = new Department(
                    0, departmentName, description.getText().trim());

            String response = hrmService.addDepartment(department);

            JOptionPane.showMessageDialog(this, response);

            if (response != null
                    && response.toLowerCase().contains("success")) {
                loadDepartments();
            }

        } catch (RemoteException e) {
            showError("Unable to add department: " + e.getMessage());
        }
    }

    // EDIT DEPARTMENT
    private void editDepartment() {

        int row = departmentTable.getSelectedRow();

        if (row < 0) {
            showError("Please select a department to edit.");
            return;
        }

        int modelRow = departmentTable.convertRowIndexToModel(row);

        int id = Integer.parseInt(
                tableModel.getValueAt(modelRow, 0).toString());

        JTextField name = new JTextField(
                String.valueOf(tableModel.getValueAt(modelRow, 1)));

        Object descriptionValue =
                tableModel.getValueAt(modelRow, 2);

        JTextArea description = new JTextArea(
                descriptionValue == null ? ""
                        : descriptionValue.toString(), 4, 25);

        description.setLineWrap(true);
        description.setWrapStyleWord(true);

        JPanel labels = new JPanel(new GridLayout(0, 1, 4, 4));
        labels.add(new JLabel("Department Name *"));
        labels.add(name);
        labels.add(new JLabel("Description"));

        JPanel panel = new JPanel(new BorderLayout(5, 8));
        panel.add(labels, BorderLayout.NORTH);
        panel.add(new JScrollPane(description), BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Edit Department",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (name.getText().trim().isEmpty()) {
            showError("Department name is required.");
            return;
        }

        try {

            Department department = new Department(
                    id,
                    name.getText().trim(),
                    description.getText().trim()
            );

            String response = hrmService.updateDepartment(department);

            JOptionPane.showMessageDialog(this, response);

            if (response != null
                    && response.toLowerCase().contains("success")) {
                loadDepartments();
            }

        } catch (RemoteException e) {
            showError("Unable to update department: " + e.getMessage());
        }
    }

    // DELETE DEPARTMENT
    private void deleteDepartment() {

        int row = departmentTable.getSelectedRow();

        if (row < 0) {
            showError("Please select a department to delete.");
            return;
        }

        int modelRow = departmentTable.convertRowIndexToModel(row);

        int id = Integer.parseInt(
                tableModel.getValueAt(modelRow, 0).toString());

        String name = String.valueOf(
                tableModel.getValueAt(modelRow, 1));

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete department '" + name + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            String response = hrmService.deleteDepartment(id);

            JOptionPane.showMessageDialog(this, response);

            if (response != null
                    && response.toLowerCase().contains("success")) {
                loadDepartments();
            }

        } catch (RemoteException e) {
            showError("Unable to delete department: " + e.getMessage());
        }
    }

    // SEARCH DEPARTMENTS
    private void searchDepartments() {

        String query = searchField.getText().trim();

        TableRowSorter<DefaultTableModel> sorter =
                new TableRowSorter<>(tableModel);

        departmentTable.setRowSorter(sorter);

        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(
                    RowFilter.regexFilter(
                            "(?i)" + java.util.regex.Pattern.quote(query)));
        }
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Department Management",
                JOptionPane.ERROR_MESSAGE);
    }
}
