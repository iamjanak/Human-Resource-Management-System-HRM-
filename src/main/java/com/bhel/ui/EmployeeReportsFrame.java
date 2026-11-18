
package com.bhel.ui;

import com.bhel.model.Employee;
import com.bhel.remote.HRMService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.List;

public class EmployeeReportsFrame extends JFrame {

    private final Color NAVY = new Color(18, 39, 67);
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color BORDER = new Color(220, 226, 234);

    private final HRMService hrmService;

    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel totalEmployeesLabel;
    private JTextField searchField;

    public EmployeeReportsFrame(HRMService hrmService) {

        this.hrmService = hrmService;

        setTitle("Employee Reports - BHEL HRM");
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(LIGHT_BG);

        createHeader();
        createMainContent();

        loadEmployeeReports();
    }

    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(22, 28, 22, 28));

        JLabel title = new JLabel("Employee Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "View and manage employee report information"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(210, 220, 235));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);
    }

    private void createMainContent() {

        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(LIGHT_BG);
        mainPanel.setBorder(new EmptyBorder(25, 28, 25, 28));

        // Summary card
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 22, 18, 22)
        ));
        summaryPanel.setPreferredSize(new Dimension(250, 90));

        JLabel summaryTitle = new JLabel("TOTAL EMPLOYEES");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        summaryTitle.setForeground(Color.GRAY);

        totalEmployeesLabel = new JLabel("0");
        totalEmployeesLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        totalEmployeesLabel.setForeground(NAVY);

        summaryPanel.add(summaryTitle, BorderLayout.NORTH);
        summaryPanel.add(totalEmployeesLabel, BorderLayout.CENTER);

        // Search and action buttons
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(300, 38));

        searchField.setToolTipText(
                "Search by employee name, ID, IC/Passport or username"
        );

        searchField.addActionListener(e -> applySearch());

        JButton searchButton = createButton("Search");
        JButton refreshButton = createButton("Refresh");
        JButton printButton = createButton("Print Report");

        searchButton.addActionListener(e -> applySearch());

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadEmployeeReports();
        });

        printButton.addActionListener(e -> printReport());

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(refreshButton);
        buttonPanel.add(printButton);

        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Table
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
        employeeTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        employeeTable.setRowHeight(32);
        employeeTable.setSelectionBackground(new Color(220, 232, 248));
        employeeTable.setSelectionForeground(Color.BLACK);
        employeeTable.setGridColor(BORDER);
        employeeTable.setShowVerticalLines(false);

        employeeTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );
        employeeTable.getTableHeader().setBackground(NAVY);
        employeeTable.getTableHeader().setForeground(Color.WHITE);
        employeeTable.getTableHeader().setPreferredSize(
                new Dimension(0, 38)
        );

        sorter = new TableRowSorter<>(tableModel);
        employeeTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("Employee Records");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        tableTitle.setForeground(NAVY);

        JPanel tableHeading = new JPanel(new BorderLayout());
        tableHeading.setOpaque(false);
        tableHeading.add(tableTitle, BorderLayout.WEST);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 12));
        tablePanel.setOpaque(false);
        tablePanel.add(tableHeading, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 18));
        contentPanel.setOpaque(false);
        contentPanel.add(summaryPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JButton createButton(String text) {

        JButton button = new JButton(text);

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(NAVY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void loadEmployeeReports() {

        try {

            List<Employee> employees = hrmService.getAllEmployees();

            tableModel.setRowCount(0);

            if (employees != null) {

                for (Employee employee : employees) {

                    if (employee == null) {
                        continue;
                    }

                    tableModel.addRow(new Object[]{
                            employee.getEmployeeId(),
                            employee.getFirstName(),
                            employee.getLastName(),
                            employee.getIcPassport(),
                            employee.getUsername()
                    });
                }
            }

            totalEmployeesLabel.setText(
                    String.valueOf(tableModel.getRowCount())
            );

        } catch (RemoteException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load employee reports.\n" + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void applySearch() {

        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(
                    RowFilter.regexFilter(
                            "(?i)" + java.util.regex.Pattern.quote(searchText)
                    )
            );
        }
    }

    private void printReport() {

        try {

            boolean complete = employeeTable.print(
                    JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat("BHEL Employee Report"),
                    new java.text.MessageFormat("Page {0}")
            );

            if (complete) {
                JOptionPane.showMessageDialog(
                        this,
                        "Report printed successfully."
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to print report: " + e.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
