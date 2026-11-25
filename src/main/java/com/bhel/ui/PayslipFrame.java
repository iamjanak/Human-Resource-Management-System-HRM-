package com.bhel.ui;

import com.bhel.model.Employee;
import com.bhel.model.Payslip;
import com.bhel.remote.HRMService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

/** Payroll administration and employee payslip viewer. */
public class PayslipFrame extends JFrame {
    private static final Color NAVY = new Color(18, 39, 67);
    private static final Color BG = new Color(245, 247, 250);
    private static final Color BORDER = new Color(220, 225, 235);
    private static final Color MUTED = new Color(105, 115, 130);

    private final HRMService service;
    private final String attendanceSessionToken;
    private final boolean admin;
    private final JComboBox<Employee> employees = new JComboBox<>();
    private final JTextField salary = new JTextField(12);
    private final JTextField grossPay = new JTextField(10);
    private final JTextField deductions = new JTextField(9);
    private final JTextField period = new JTextField(8);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Pay period", "Employee ID", "Employee", "Department",
                    "Gross pay", "Deductions", "Net pay", "Generated"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<Payslip> slips = List.of();

    public PayslipFrame(HRMService service, String attendanceSessionToken, boolean admin) {
        this.service = service;
        this.attendanceSessionToken = attendanceSessionToken;
        this.admin = admin;
        setTitle(admin ? "BHEL HRM - Payroll & Payslips" : "BHEL HRM - My Payslips");
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(16, 16));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(24, 26, 24, 26));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        period.setText(YearMonth.now().toString());
        styleTable();
        if (admin) loadEmployees(); else reload();
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(admin ? "Payroll & Payslips" : "My Payslips");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel(admin
                ? "Manage monthly salary records and issue employee pay statements."
                : "Select a pay period to review or print your salary statement.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(subtitle);
        return panel;
    }

    private JPanel buildContent() {
        JPanel outer = new JPanel(new BorderLayout(0, 14));
        outer.setOpaque(false);
        if (admin) outer.add(buildAdminControls(), BorderLayout.NORTH);

        table.setRowHeight(36);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        if (admin) {
            table.getSelectionModel().addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting()) loadSelectedPayslipIntoEditor();
            });
        }
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) previewSelected();
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        outer.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        JButton refresh = button("Refresh");
        refresh.addActionListener(event -> reload());
        footer.add(refresh);
        JButton view = button("View / Print selected payslip");
        view.addActionListener(event -> previewSelected());
        footer.add(view);
        outer.add(footer, BorderLayout.SOUTH);
        return outer;
    }

    private JPanel buildAdminControls() {
        JPanel controls = new JPanel(new GridLayout(2, 1, 0, 4));
        controls.setBackground(Color.WHITE);
        controls.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 10, 8, 10)));
        JPanel salaryRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        salaryRow.setOpaque(false);
        salaryRow.add(label("Employee"));
        employees.setPreferredSize(new Dimension(220, 34));
        salaryRow.add(employees);
        salaryRow.add(label("Monthly salary (INR)"));
        salary.setPreferredSize(new Dimension(125, 34));
        salaryRow.add(salary);
        JButton save = button("Save salary");
        save.addActionListener(event -> saveSalary());
        salaryRow.add(save);

        JPanel payslipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        payslipRow.setOpaque(false);
        payslipRow.add(label("Pay period"));
        period.setPreferredSize(new Dimension(105, 34));
        payslipRow.add(period);
        payslipRow.add(label("Gross pay (INR)"));
        grossPay.setPreferredSize(new Dimension(105, 34));
        payslipRow.add(grossPay);
        payslipRow.add(label("Deductions (INR)"));
        deductions.setPreferredSize(new Dimension(95, 34));
        payslipRow.add(deductions);
        JButton generate = button("Generate payslip");
        generate.addActionListener(event -> generate());
        payslipRow.add(generate);
        JButton update = button("Update selected");
        update.addActionListener(event -> updateSelectedPayslip());
        payslipRow.add(update);
        controls.add(salaryRow);
        controls.add(payslipRow);
        employees.addActionListener(event -> loadSalary());
        return controls;
    }

    private JLabel label(String text) {
        JLabel result = new JLabel(text);
        result.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        result.setForeground(MUTED);
        return result;
    }

    private JButton button(String text) {
        JButton result = new JButton(text);
        result.setFont(new Font("Segoe UI", Font.BOLD, 12));
        result.setForeground(Color.WHITE);
        result.setBackground(NAVY);
        result.setFocusPainted(false);
        result.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return result;
    }

    private void styleTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(220, 232, 248));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(235, 239, 245));
        table.getTableHeader().setForeground(NAVY);
    }

    private void loadEmployees() {
        try {
            for (Employee employee : service.getAllEmployees()) employees.addItem(employee);
            employees.setRenderer((list, value, index, selected, focus) -> {
                JLabel result = new JLabel(value == null ? "Select employee"
                        : value.getEmployeeId() + " - " + value.getFirstName() + " " + value.getLastName());
                result.setOpaque(true);
                result.setBorder(new EmptyBorder(5, 8, 5, 8));
                result.setBackground(selected ? list.getSelectionBackground() : Color.WHITE);
                result.setForeground(selected ? list.getSelectionForeground() : NAVY);
                return result;
            });
            reload();
        } catch (Exception e) {
            showError("Unable to load employees", e);
        }
    }

    private Employee selectedEmployee() { return (Employee) employees.getSelectedItem(); }

    private void loadSalary() {
        Employee employee = selectedEmployee();
        if (employee == null) return;
        try {
            BigDecimal value = service.getMonthlySalary(employee.getEmployeeId());
            salary.setText(value == null ? "" : value.toPlainString());
        } catch (Exception e) {
            showError("Unable to load salary", e);
        }
    }

    private void saveSalary() {
        Employee employee = selectedEmployee();
        if (employee == null) { error("Choose an employee first."); return; }
        try {
            BigDecimal amount = new BigDecimal(salary.getText().trim());
            if (amount.signum() < 0 || amount.scale() > 2) throw new NumberFormatException();
            JOptionPane.showMessageDialog(this,
                    service.setMonthlySalary(employee.getEmployeeId(), amount),
                    "Payroll", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            error("Enter a non-negative salary with up to two decimal places.");
        } catch (Exception e) {
            showError("Unable to save salary", e);
        }
    }

    private void generate() {
        Employee employee = selectedEmployee();
        if (employee == null) { error("Choose an employee first."); return; }
        try {
            YearMonth month = YearMonth.parse(period.getText().trim());
            service.generatePayslip(employee.getEmployeeId(), month);
            reload();
            grossPay.setText(salary.getText().trim());
            deductions.setText("0.00");
            JOptionPane.showMessageDialog(this, "Payslip generated for " + month + ".",
                    "Payroll", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            showError("Unable to generate payslip", e);
        }
    }

    private void loadSelectedPayslipIntoEditor() {
        int selected = table.getSelectedRow();
        if (selected < 0) return;
        int row = table.convertRowIndexToModel(selected);
        if (row < 0 || row >= slips.size()) return;
        Payslip payslip = slips.get(row);
        for (int i = 0; i < employees.getItemCount(); i++) {
            Employee employee = employees.getItemAt(i);
            if (employee.getEmployeeId() == payslip.getEmployeeId()) {
                employees.setSelectedIndex(i);
                break;
            }
        }
        period.setText(payslip.getPayPeriod().toString());
        grossPay.setText(payslip.getGrossPay().toPlainString());
        deductions.setText(payslip.getDeductions().toPlainString());
    }

    private void updateSelectedPayslip() {
        Employee employee = selectedEmployee();
        if (employee == null) { error("Select an employee and an existing payslip first."); return; }
        try {
            YearMonth month = YearMonth.parse(period.getText().trim());
            BigDecimal gross = new BigDecimal(grossPay.getText().trim());
            BigDecimal deduction = new BigDecimal(deductions.getText().trim());
            if (gross.signum() < 0 || deduction.signum() < 0
                    || gross.scale() > 2 || deduction.scale() > 2
                    || deduction.compareTo(gross) > 0) {
                error("Enter valid amounts with up to two decimal places. Deductions cannot exceed gross pay.");
                return;
            }
            String result = service.updatePayslip(employee.getEmployeeId(), month, gross, deduction);
            reload();
            JOptionPane.showMessageDialog(this, result, "Payroll", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            error("Enter valid gross pay and deduction amounts.");
        } catch (Exception e) {
            showError("Unable to update payslip", e);
        }
    }

    private void reload() {
        try {
            slips = admin ? service.getAllPayslips()
                    : service.getEmployeePayslips(attendanceSessionToken);
            model.setRowCount(0);
            NumberFormat money = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"));
            for (Payslip payslip : slips) {
                model.addRow(new Object[]{payslip.getPayPeriod(), payslip.getEmployeeId(),
                        payslip.getEmployeeName(), payslip.getDepartment(),
                        money.format(payslip.getGrossPay()), money.format(payslip.getDeductions()),
                        money.format(payslip.getNetPay()), payslip.getGeneratedOn()});
            }
        } catch (Exception e) {
            showError("Unable to load payslips", e);
        }
    }

    private void previewSelected() {
        int selected = table.getSelectedRow();
        if (selected < 0) { error("Select a payslip first."); return; }
        int row = table.convertRowIndexToModel(selected);
        showPayslip(slips.get(row));
    }

    private void showPayslip(Payslip payslip) {
        JEditorPane document = new JEditorPane("text/html", payslipHtml(payslip));
        document.setEditable(false);
        document.setMargin(new Insets(28, 34, 28, 34));
        document.setBackground(Color.WHITE);

        JDialog preview = new JDialog(this, "Salary statement - " + payslip.getPayPeriod(), true);
        preview.setLayout(new BorderLayout(12, 12));
        preview.getContentPane().setBackground(BG);
        JScrollPane scroll = new JScrollPane(document);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        preview.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        actions.setBackground(BG);
        JButton close = new JButton("Close");
        close.addActionListener(event -> preview.dispose());
        JButton print = button("Print / Save as PDF");
        print.addActionListener(event -> {
            try { document.print(); }
            catch (Exception e) { showError("Unable to print payslip", e); }
        });
        actions.add(close);
        actions.add(print);
        preview.add(actions, BorderLayout.SOUTH);
        preview.setSize(820, 850);
        preview.setLocationRelativeTo(this);
        preview.setVisible(true);
    }

    private String payslipHtml(Payslip p) {
        NumberFormat money = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"));
        String cell = "style='padding:9px 11px;border:1px solid #dce1eb'";
        return "<html><body style='font-family:Segoe UI;color:#243247'>"
                + "<table width='100%'><tr><td><h1 style='color:#122743;margin-bottom:2px'>BHEL</h1>"
                + "<div style='color:#687386'>HUMAN RESOURCES DEPARTMENT</div></td>"
                + "<td align='right'><h2 style='color:#122743'>SALARY SLIP</h2><b>Pay period:</b> "
                + p.getPayPeriod() + "</td></tr></table><hr>"
                + "<h3 style='color:#122743'>Employee details</h3>"
                + "<table width='100%' cellspacing='0' cellpadding='2'>"
                + infoRow("Employee name", p.getEmployeeName(), "Employee ID", String.valueOf(p.getEmployeeId()))
                + infoRow("Department", p.getDepartment(), "Designation", p.getDesignation())
                + infoRow("IC / Passport", p.getIcPassport(), "Gender", p.getGender())
                + infoRow("Date of birth", String.valueOf(p.getDateOfBirth()), "Employment status", p.getEmploymentStatus())
                + infoRow("Phone", p.getPhone(), "Email", p.getEmail())
                + infoRow("Joining date", String.valueOf(p.getJoiningDate()), "Generated on", String.valueOf(p.getGeneratedOn()))
                + "</table><p><b>Address:</b> " + esc(p.getAddress()) + "</p>"
                + "<h3 style='color:#122743'>Earnings</h3><table width='100%' cellspacing='0' cellpadding='0'>"
                + "<tr style='background:#eaf0f7'><th align='left' " + cell + ">Pay component</th><th align='right' " + cell + ">Amount</th></tr>"
                + "<tr><td " + cell + ">Monthly gross salary</td><td align='right' " + cell + ">" + money.format(p.getGrossPay()) + "</td></tr>"
                + "<tr><td " + cell + ">Additional allowances</td><td align='right' " + cell + ">Not configured</td></tr></table>"
                + "<h3 style='color:#122743'>Deductions</h3><table width='100%' cellspacing='0' cellpadding='0'>"
                + "<tr style='background:#eaf0f7'><th align='left' " + cell + ">Deduction component</th><th align='right' " + cell + ">Amount</th></tr>"
                + "<tr><td " + cell + ">Total deductions</td><td align='right' " + cell + ">" + money.format(p.getDeductions()) + "</td></tr></table>"
                + "<table width='100%' style='margin-top:18px;background:#122743;color:white'><tr>"
                + "<td style='padding:14px'><b>NET PAY</b></td><td align='right' style='padding:14px'><b>"
                + money.format(p.getNetPay()) + "</b></td></tr></table>"
                + "<p style='color:#687386;font-size:10px;margin-top:24px'>This statement uses the monthly gross salary configured in BHEL HRM. "
                + "Allowances, tax, statutory deductions, bank details and attendance-based adjustments are not configured.</p>"
                + "<p style='color:#687386;font-size:10px'>Computer-generated statement; no signature required.</p>"
                + "</body></html>";
    }

    private String infoRow(String label1, String value1, String label2, String value2) {
        return "<tr><td style='color:#687386;padding:5px 10px 5px 0'><b>" + label1 + "</b></td><td>" + esc(value1)
                + "</td><td style='color:#687386;padding:5px 10px 5px 18px'><b>" + label2 + "</b></td><td>" + esc(value2) + "</td></tr>";
    }

    private String esc(String value) {
        if (value == null || "null".equals(value)) return "Not provided";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Payroll", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String title, Exception e) {
        String details = e.getMessage() == null ? "" : e.getMessage();
        if (details.toLowerCase(Locale.ROOT).contains("unrecognized method hash")
                || details.toLowerCase(Locale.ROOT).contains("unmarshalexception")) {
            details = "The HRM server is running an older version than this client. "
                    + "Stop the server, rebuild the project, start the server again, then reopen the client.";
        }
        JOptionPane.showMessageDialog(this, title + ":\n" + details,
                "Payroll", JOptionPane.ERROR_MESSAGE);
    }
}
