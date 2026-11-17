
package com.bhel.ui;

import com.bhel.model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EmployeeDashboard extends JFrame {

    private final Color NAVY = new Color(18, 39, 67);
    private final Color NAVY_LIGHT = new Color(29, 54, 83);
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color BORDER = new Color(220, 225, 235);
    private final Color TEXT_GRAY = new Color(105, 115, 130);

    private final Employee employee;
    private final String username;
    private final int employeeId;

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

        createHeader();
        createMainLayout();

        setVisible(true);
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

        JLabel welcome = new JLabel("Welcome, " + username);

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

        addMenuItem(menuPanel, "My Profile", () -> openProfile());

        addMenuItem(menuPanel, "Family Details", () -> openProfile());

        addMenuItem(menuPanel, "Apply for Leave",
                () -> showMessage("Leave Application"));

        addMenuItem(menuPanel, "My Leave Applications",
                () -> showMessage("My Leave Applications"));

        addMenuItem(menuPanel, "Leave Balance",
                () -> showMessage("Leave Balance"));

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

        JPanel dashboard = new JPanel(new BorderLayout(20, 25));

        dashboard.setBackground(LIGHT_BG);

        // Heading

        JPanel headingPanel = new JPanel();

        headingPanel.setBackground(LIGHT_BG);

        headingPanel.setLayout(
                new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Employee Dashboard");

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 28));

        heading.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Manage your employee information and leave activities.");

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 14));

        subtitle.setForeground(TEXT_GRAY);

        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(8));
        headingPanel.add(subtitle);

        // Dashboard cards

        JPanel cardPanel = new JPanel(
                new GridLayout(2, 2, 20, 20));

        cardPanel.setBackground(LIGHT_BG);

        cardPanel.add(createDashboardCard(
                "My Profile",
                "View and update your personal information",
                () -> openProfile()
        ));

        cardPanel.add(createDashboardCard(
                "Family Details",
                "Manage your family information",
                () -> openProfile()
        ));

        cardPanel.add(createDashboardCard(
                "Apply for Leave",
                "Submit a new leave application",
                () -> showMessage("Leave Application")
        ));

        cardPanel.add(createDashboardCard(
                "Leave Balance",
                "Check your available leave balance",
                () -> showMessage("Leave Balance")
        ));

        dashboard.add(headingPanel, BorderLayout.NORTH);
        dashboard.add(cardPanel, BorderLayout.CENTER);

        contentPanel.add(dashboard, BorderLayout.CENTER);

        contentPanel.revalidate();
        contentPanel.repaint();
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

    // ================= TEMPORARY FEATURE MESSAGE =================

    private void showMessage(String feature) {

        JOptionPane.showMessageDialog(
                this,
                feature + " will be connected to the HRM server next.",
                "BHEL HRM",
                JOptionPane.INFORMATION_MESSAGE
        );
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

            dispose();

            new com.bhel.client.LoginFrame().setVisible(true);
        }
    }
}
