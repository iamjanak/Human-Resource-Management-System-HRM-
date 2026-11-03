
package com.bhel.ui;

import com.bhel.client.LoginFrame;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    private final Color NAVY = new Color(22, 39, 63);
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT = new Color(55, 65, 81);

    public AdminDashboard() {

        setTitle("BHEL HRM - Admin Dashboard");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(240, 750));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("  BHEL HRM");
        logo.setForeground(WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 25));
        logo.setBorder(BorderFactory.createEmptyBorder(30, 10, 35, 10));

        sidebar.add(logo);

        addMenuButton(sidebar, "Dashboard");
        addMenuButton(sidebar, "Employee Management");
        addMenuButton(sidebar, "Register Employee");
        addMenuButton(sidebar, "Leave Management");
        addMenuButton(sidebar, "Employee Reports");

        sidebar.add(Box.createVerticalGlue());

        JButton logoutButton = new JButton("Logout");
        logoutButton.setMaximumSize(new Dimension(220, 45));
        logoutButton.setBackground(new Color(180, 60, 60));
        logoutButton.setForeground(WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutButton.setBorderPainted(false);

        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        sidebar.add(logoutButton);
        sidebar.add(Box.createVerticalStrut(25));

        add(sidebar, BorderLayout.WEST);

        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(LIGHT_BG);
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
        );

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome, Admin!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Human Resource Management System"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(welcomeLabel);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);

        JLabel adminBadge = new JLabel("ADMIN / HR STAFF");
        adminBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        adminBadge.setForeground(NAVY);

        header.add(adminBadge, BorderLayout.EAST);

        mainPanel.add(header, BorderLayout.NORTH);

        // Dashboard body
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(Box.createVerticalStrut(30));

        JLabel overviewLabel = new JLabel("Dashboard Overview");
        overviewLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        overviewLabel.setForeground(NAVY);

        body.add(overviewLabel);
        body.add(Box.createVerticalStrut(20));

        // Statistics cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        cardsPanel.setOpaque(false);

        cardsPanel.add(createStatCard("Total Employees", "—"));
        cardsPanel.add(createStatCard("Pending Leave Requests", "—"));
        cardsPanel.add(createStatCard("Employee Reports", "—"));

        body.add(cardsPanel);
        body.add(Box.createVerticalStrut(35));

        JLabel quickActionsLabel = new JLabel("Quick Actions");
        quickActionsLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        quickActionsLabel.setForeground(NAVY);

        body.add(quickActionsLabel);
        body.add(Box.createVerticalStrut(20));

        JPanel actionsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        actionsPanel.setOpaque(false);

        actionsPanel.add(createActionButton("Register New Employee"));
        actionsPanel.add(createActionButton("Manage Employees"));
        actionsPanel.add(createActionButton("Review Leave Applications"));
        actionsPanel.add(createActionButton("Generate Employee Report"));

        body.add(actionsPanel);

        mainPanel.add(body, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void addMenuButton(JPanel sidebar, String text) {

        JButton button = new JButton(text);

        button.setMaximumSize(new Dimension(240, 48));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(WHITE);
        button.setBackground(NAVY);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(
                BorderFactory.createEmptyBorder(10, 25, 10, 10)
        );

        button.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        text + " module will be implemented next."
                )
        );

        sidebar.add(button);
    }

    private JPanel createStatCard(String title, String value) {

        JPanel card = new JPanel();
        card.setBackground(WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 235)
                        ),
                        BorderFactory.createEmptyBorder(
                                25, 20, 25, 20
                        )
                )
        );

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(TEXT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(NAVY);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(valueLabel);

        return card;
    }

    private JButton createActionButton(String text) {

        JButton button = new JButton(text);

        button.setBackground(WHITE);
        button.setForeground(NAVY);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(250, 65));
        button.setFocusPainted(false);

        button.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        text + " module will be implemented next."
                )
        );

        return button;
    }
}
