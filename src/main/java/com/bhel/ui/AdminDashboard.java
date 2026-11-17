
package com.bhel.ui;

import com.bhel.client.LoginFrame;
import com.bhel.remote.HRMService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.rmi.Naming;

public class AdminDashboard extends JFrame {

    private final Color NAVY = new Color(18, 39, 67);
    private final Color NAVY_LIGHT = new Color(29, 54, 83);
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color BORDER = new Color(220, 225, 235);
    private final Color TEXT_GRAY = new Color(105, 115, 130);
    private final Color WHITE = Color.WHITE;

    private HRMService hrmService;

    private JPanel contentPanel;
    private JButton activeButton;

    private JLabel totalEmployeesValue;
    private JLabel pendingLeavesValue;

    private final String username;

    public AdminDashboard() {
        this("Admin");
    }

    public AdminDashboard(String username) {

        this.username = username == null || username.trim().isEmpty()
                ? "Admin" : username;

        setTitle("BHEL HRM - Admin Dashboard");
        setSize(1200, 750);
        setMinimumSize(new Dimension(1050, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        connectToServer();

        createHeader();
        createMainLayout();

        setVisible(true);
    }

    // ================= RMI CONNECTION =================

    private void connectToServer() {

        try {
            hrmService = (HRMService) Naming.lookup(
                    "rmi://localhost:1099/HRMService");

        } catch (Exception e) {
            hrmService = null;

            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(
                            this,
                            "Unable to connect to HRM server.\n"
                                    + e.getMessage(),
                            "Connection Error",
                            JOptionPane.ERROR_MESSAGE
                    ));
        }
    }

    // ================= HEADER =================

    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(17, 25, 17, 25));

        JLabel title = new JLabel("BHEL HRM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(WHITE);

        JLabel portal = new JLabel("ADMINISTRATION PORTAL");
        portal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        portal.setForeground(new Color(190, 205, 225));

        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(
                new BoxLayout(brandPanel, BoxLayout.Y_AXIS));

        brandPanel.add(title);
        brandPanel.add(Box.createVerticalStrut(3));
        brandPanel.add(portal);

        JLabel welcome = new JLabel("Welcome, " + username);
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        welcome.setForeground(WHITE);

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

        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setBackground(NAVY);
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));

        JPanel menuPanel = new JPanel();

        menuPanel.setBackground(NAVY);
        menuPanel.setLayout(
                new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JLabel menuTitle = new JLabel("ADMIN MENU");
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        menuTitle.setForeground(new Color(170, 185, 205));
        menuTitle.setBorder(new EmptyBorder(5, 12, 20, 5));

        menuPanel.add(menuTitle);

        addMenuItem(menuPanel, "Dashboard", this::showDashboard);
        addMenuItem(menuPanel, "Employee Management", this::openEmployeeManagement);
        addMenuItem(menuPanel, "Register Employee", this::openRegistration);
        addMenuItem(menuPanel, "Leave Management", this::openLeaveManagement);
        addMenuItem(menuPanel, "Employee Reports", this::openReports);
        addMenuItem(menuPanel, "Department Management",
                () -> showMessage("Department Management"));
        addMenuItem(menuPanel, "Attendance Management",
                () -> showMessage("Attendance Management"));
        addMenuItem(menuPanel, "System Settings",
                () -> showMessage("System Settings"));

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

    private void addMenuItem(
            JPanel menuPanel,
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

        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(WHITE);
        button.setBackground(NAVY);
        button.setHorizontalAlignment(SwingConstants.LEFT);

        button.setBorder(new EmptyBorder(13, 15, 13, 10));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 48));

        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        return button;
    }

    // ================= DASHBOARD CONTENT =================

    private void showDashboard() {

        contentPanel.removeAll();

        JPanel dashboard = new JPanel(new BorderLayout(20, 25));
        dashboard.setBackground(LIGHT_BG);

        // Heading

        JPanel headingPanel = new JPanel();
        headingPanel.setBackground(LIGHT_BG);
        headingPanel.setLayout(
                new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Admin Dashboard");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Manage employees, leave requests and HR activities.");

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_GRAY);

        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(8));
        headingPanel.add(subtitle);

        // Dashboard content

        JPanel dashboardBody = new JPanel();
        dashboardBody.setBackground(LIGHT_BG);
        dashboardBody.setLayout(
                new BoxLayout(dashboardBody, BoxLayout.Y_AXIS));

        // Statistics heading

        JLabel overview = new JLabel("HR Overview");
        overview.setFont(new Font("Segoe UI", Font.BOLD, 20));
        overview.setForeground(NAVY);

        dashboardBody.add(overview);
        dashboardBody.add(Box.createVerticalStrut(18));

        // Statistics cards

        JPanel statsPanel = new JPanel(
                new GridLayout(1, 3, 18, 18));

        statsPanel.setBackground(LIGHT_BG);

        totalEmployeesValue = new JLabel("—");
        pendingLeavesValue = new JLabel("—");

        statsPanel.add(createStatCard(
                "Total Employees",
                totalEmployeesValue));

        statsPanel.add(createStatCard(
                "Pending Leave Requests",
                pendingLeavesValue));

        statsPanel.add(createStatCard(
                "HR Modules",
                new JLabel("8")));

        dashboardBody.add(statsPanel);
        dashboardBody.add(Box.createVerticalStrut(30));

        // Quick actions

        JLabel quickActions = new JLabel("Quick Actions");
        quickActions.setFont(new Font("Segoe UI", Font.BOLD, 20));
        quickActions.setForeground(NAVY);

        dashboardBody.add(quickActions);
        dashboardBody.add(Box.createVerticalStrut(18));

        JPanel actionsPanel = new JPanel(
                new GridLayout(2, 3, 15, 15));

        actionsPanel.setBackground(LIGHT_BG);

        actionsPanel.add(createActionCard(
                "Register Employee",
                "Add a new employee",
                this::openRegistration));

        actionsPanel.add(createActionCard(
                "Manage Employees",
                "View and manage employee records",
                this::openEmployeeManagement));

        actionsPanel.add(createActionCard(
                "Leave Management",
                "Review employee leave requests",
                this::openLeaveManagement));

        actionsPanel.add(createActionCard(
                "Employee Reports",
                "View employee reports",
                this::openReports));

        actionsPanel.add(createActionCard(
                "Departments",
                "Manage department information",
                () -> showMessage("Department Management")));

        actionsPanel.add(createActionCard(
                "Attendance",
                "Manage employee attendance",
                () -> showMessage("Attendance Management")));

        dashboardBody.add(actionsPanel);

        JScrollPane scrollPane = new JScrollPane(dashboardBody);
        scrollPane.setBorder(null);
        scrollPane.setBackground(LIGHT_BG);
        scrollPane.getViewport().setBackground(LIGHT_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        dashboard.add(headingPanel, BorderLayout.NORTH);
        dashboard.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(dashboard, BorderLayout.CENTER);

        contentPanel.revalidate();
        contentPanel.repaint();

        loadDashboardStatistics();
    }

    // ================= STATISTICS CARD =================

    private JPanel createStatCard(
            String title,
            JLabel valueLabel) {

        JPanel card = new JPanel();
        card.setBackground(WHITE);

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS));

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(22, 20, 22, 20)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(TEXT_GRAY);

        valueLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(NAVY);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(valueLabel);

        return card;
    }

    // ================= QUICK ACTION CARD =================

    private JButton createActionCard(
            String title,
            String description,
            Runnable action) {

        JButton card = new JButton();
        card.setLayout(new BorderLayout(10, 12));
        card.setBackground(WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(22, 18, 22, 18)));

        card.setFocusPainted(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel cardTitle = new JLabel(title);
        cardTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 17));
        cardTitle.setForeground(NAVY);

        JLabel cardDescription = new JLabel(
                "<html><div style='width:180px;'>"
                        + description
                        + "</div></html>");

        cardDescription.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));
        cardDescription.setForeground(TEXT_GRAY);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(
                new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        textPanel.add(cardTitle);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(cardDescription);

        card.add(textPanel, BorderLayout.CENTER);

        card.addActionListener(e -> action.run());

        return card;
    }

    // ================= LOAD STATISTICS =================

    private void loadDashboardStatistics() {

        if (hrmService == null) {
            return;
        }

        new SwingWorker<int[], Void>() {

            @Override
            protected int[] doInBackground() throws Exception {

                int employeeCount =
                        hrmService.getAllEmployees().size();

                int pendingCount = 0;

                for (Object leave : hrmService.getAllLeaveApplications()) {

                    try {
                        String status = (String) leave.getClass()
                                .getMethod("getStatus")
                                .invoke(leave);

                        if (status != null
                                && status.equalsIgnoreCase("PENDING")) {
                            pendingCount++;
                        }

                    } catch (Exception ignored) {
                        // Ignore an individual leave record if its
                        // status cannot be read.
                    }
                }

                return new int[]{employeeCount, pendingCount};
            }

            @Override
            protected void done() {

                try {
                    int[] stats = get();

                    totalEmployeesValue.setText(
                            String.valueOf(stats[0]));

                    pendingLeavesValue.setText(
                            String.valueOf(stats[1]));

                } catch (Exception e) {
                    totalEmployeesValue.setText("N/A");
                    pendingLeavesValue.setText("N/A");
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    // ================= OPEN EMPLOYEE MANAGEMENT =================

    private void openEmployeeManagement() {

        if (!checkConnection()) {
            return;
        }

        EmployeeManagementFrame frame =
                new EmployeeManagementFrame(hrmService);

        frame.setVisible(true);
    }

    // ================= OPEN REGISTRATION =================

    private void openRegistration() {

        new RegisterEmployeeFrame().setVisible(true);
    }

    // ================= OPEN LEAVE MANAGEMENT =================

    private void openLeaveManagement() {

        if (!checkConnection()) {
            return;
        }

        LeaveManagementFrame frame =
                new LeaveManagementFrame(hrmService, 0);

        frame.setVisible(true);
    }

    // ================= REPORTS =================

    private void openReports() {

        showMessage("Employee Reports");
    }

    // ================= CONNECTION CHECK =================

    private boolean checkConnection() {

        if (hrmService == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "HRM server is not connected.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);

            return false;
        }

        return true;
    }

    // ================= TEMPORARY FEATURE MESSAGE =================

    private void showMessage(String feature) {

        JOptionPane.showMessageDialog(
                this,
                feature + " module will be connected to the HRM server next.",
                "BHEL HRM",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ================= LOGOUT =================

    private void logout() {

        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {

            dispose();

            new LoginFrame().setVisible(true);
        }
    }
}
