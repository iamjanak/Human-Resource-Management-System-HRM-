
package com.bhel.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;

    private final Color NAVY = new Color(18, 39, 67);
    private final Color LIGHT_BG = new Color(245, 247, 250);

    public LoginFrame() {

        setTitle("BHEL | Human Resource Management System");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setBackground(Color.WHITE);

        // LEFT PANEL
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(NAVY);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(100, 45, 50, 35));

        JLabel brand = new JLabel("BHEL");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 42));
        brand.setForeground(Color.WHITE);

        JLabel systemTitle = new JLabel("Human Resource");
        systemTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        systemTitle.setForeground(Color.WHITE);

        JLabel systemTitle2 = new JLabel("Management System");
        systemTitle2.setFont(new Font("Segoe UI", Font.BOLD, 25));
        systemTitle2.setForeground(Color.WHITE);

        JLabel description = new JLabel(
                "<html><div style='width:260px;'>"
                + "A centralized platform for managing employee "
                + "information, leave applications and HR activities."
                + "</div></html>"
        );
        description.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        description.setForeground(new Color(215, 225, 240));

        leftPanel.add(brand);
        leftPanel.add(Box.createVerticalStrut(35));
        leftPanel.add(systemTitle);
        leftPanel.add(systemTitle2);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(description);

        // RIGHT PANEL
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(75, 55, 50, 55));

        JLabel loginTitle = new JLabel("Welcome Back");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        loginTitle.setForeground(NAVY);

        JLabel subtitle = new JLabel("Sign in to access your account");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);

        JLabel roleLabel = new JLabel("Login As");
        styleLabel(roleLabel);

        roleBox = new JComboBox<>(new String[]{
                "Admin / HR Staff",
                "Employee"
        });
        styleCombo(roleBox);

        JLabel usernameLabel = new JLabel("Username / Employee ID");
        styleLabel(usernameLabel);

        usernameField = new JTextField();
        styleField(usernameField);

        JLabel passwordLabel = new JLabel("Password");
        styleLabel(passwordLabel);

        passwordField = new JPasswordField();
        styleField(passwordField);

        JButton loginButton = new JButton("Sign In");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(NAVY);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JButton clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clearButton.setBackground(LIGHT_BG);
        clearButton.setFocusPainted(false);
        clearButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        loginButton.addActionListener(e -> login());
        clearButton.addActionListener(e -> clearFields());

        rightPanel.add(loginTitle);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(subtitle);
        rightPanel.add(Box.createVerticalStrut(35));

        rightPanel.add(roleLabel);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(roleBox);
        rightPanel.add(Box.createVerticalStrut(20));

        rightPanel.add(usernameLabel);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(usernameField);
        rightPanel.add(Box.createVerticalStrut(20));

        rightPanel.add(passwordLabel);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(passwordField);
        rightPanel.add(Box.createVerticalStrut(30));

        rightPanel.add(loginButton);
        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(clearButton);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel);
    }

    private void styleLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(NAVY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 225)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    // LOGIN METHOD
    private void login() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = roleBox.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your username and password.",
                    "Login Validation",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        HRMClient client = new HRMClient();

        String response = client.login(username, password, role);

        if ("Login successful!".equals(response)) {

            JOptionPane.showMessageDialog(
                    this,
                    response,
                    "BHEL HRM Login",
                    JOptionPane.INFORMATION_MESSAGE
            );

            if ("Admin / HR Staff".equals(role)) {

                dispose();

                new com.bhel.ui.AdminDashboard().setVisible(true);

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Employee dashboard will be implemented next."
                );
            }

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    response,
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // CLEAR METHOD
    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        roleBox.setSelectedIndex(0);
    }

}
