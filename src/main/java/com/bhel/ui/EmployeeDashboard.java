
package com.bhel.ui;

import com.bhel.model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EmployeeDashboard extends JFrame {

    private final Color NAVY = new Color(18, 39, 67);
    private final Color LIGHT_BG = new Color(245, 247, 250);

    private final Employee employee;
    private final String username;
    private final int employeeId;

    public EmployeeDashboard(Employee employee) {

        this.employee = employee;
        this.username = employee.getUsername();
        this.employeeId = employee.getEmployeeId();

        setTitle("BHEL HRM - Employee Dashboard");
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        createHeader();
        createDashboard();
    }

    // HEADER
    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(NAVY);
        header.setBorder(
                new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("BHEL HRM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel welcome = new JLabel(
                "Welcome, " + username);

        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        welcome.setForeground(Color.WHITE);

        header.add(title, BorderLayout.WEST);
        header.add(welcome, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // DASHBOARD
    private void createDashboard() {

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));

        mainPanel.setBackground(LIGHT_BG);
        mainPanel.setBorder(
                new EmptyBorder(30, 35, 30, 35));

        JLabel heading = new JLabel("Employee Dashboard");

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 28));

        heading.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Manage your employee information and leave activities.");

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 14));

        subtitle.setForeground(Color.GRAY);

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(LIGHT_BG);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        titlePanel.add(heading);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(subtitle);

        // DASHBOARD BUTTONS
        JPanel cardPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardPanel.setBackground(LIGHT_BG);

        JButton profileButton = createButton("My Profile");
        JButton leaveButton = createButton("Apply for Leave");
        JButton balanceButton = createButton("Leave Balance");
        JButton applicationsButton =
                createButton("My Leave Applications");

        profileButton.addActionListener(e ->
                showMessage("Employee Profile"));

        leaveButton.addActionListener(e ->
                showMessage("Leave Application"));

        balanceButton.addActionListener(e ->
                showMessage("Leave Balance"));

        applicationsButton.addActionListener(e ->
                showMessage("My Leave Applications"));

        cardPanel.add(profileButton);
        cardPanel.add(leaveButton);
        cardPanel.add(balanceButton);
        cardPanel.add(applicationsButton);

        // FOOTER
        JPanel footer = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        footer.setBackground(LIGHT_BG);

        JButton logoutButton = new JButton("Logout");

        logoutButton.setFont(
                new Font("Segoe UI", Font.BOLD, 14));

        logoutButton.setBackground(NAVY);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);

        logoutButton.addActionListener(e -> logout());

        footer.add(logoutButton);

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        mainPanel.add(footer, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    // BUTTON STYLE
    private JButton createButton(String text) {

        JButton button = new JButton(text);

        button.setFont(
                new Font("Segoe UI", Font.BOLD, 18));

        button.setForeground(NAVY);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(220, 225, 235)),
                        new EmptyBorder(30, 15, 30, 15)));

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    // TEMPORARY MESSAGE
    private void showMessage(String feature) {

        JOptionPane.showMessageDialog(
                this,
                feature + " will be connected to the HRM server next.",
                "BHEL HRM",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // LOGOUT
    private void logout() {

        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {

            dispose();

            new com.bhel.client.LoginFrame().setVisible(true);
        }
    }
}
