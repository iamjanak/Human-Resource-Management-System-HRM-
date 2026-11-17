
package com.bhel.ui;

import com.bhel.client.HRMClient;
import com.bhel.model.Employee;
import com.bhel.model.FamilyMember;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeProfileFrame extends JFrame {

    private static final Color NAVY = new Color(18, 39, 67);
    private static final Color BG = new Color(245, 247, 250);
    private static final Color TEXT = new Color(28, 39, 52);
    private static final Color GREEN = new Color(0, 111, 96);

    private final HRMClient hrmClient;
    private final int employeeId;

    private Employee employee;
    private List<FamilyMember> familyMembers = new ArrayList<>();

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField icPassportField;
    private JTextField usernameField;

    private DefaultTableModel tableModel;
    private JTable familyTable;

    public EmployeeProfileFrame(Employee employee) {

        this.employee = employee;
        this.employeeId = employee.getEmployeeId();

        hrmClient = new HRMClient();

        setTitle("BHEL HRM | Employee Profile");
        setSize(1050, 720);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        loadProfile();
        loadFamilyMembers();
    }

    // ================= HEADER =================

    private JPanel buildHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(18, 25, 18, 25));

        JLabel title = new JLabel("My Employee Profile");

        title.setFont(new Font("Segoe UI", Font.BOLD, 23));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Manage your personal and family information"
        );

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(210, 220, 235));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitle);

        JButton closeButton = new JButton("Close");

        closeButton.addActionListener(e -> dispose());

        header.add(textPanel, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        return header;
    }

    // ================= MAIN CONTENT =================

    private JPanel buildContent() {

        JPanel content = new JPanel();
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(buildProfilePanel());
        content.add(Box.createVerticalStrut(20));
        content.add(buildFamilyPanel());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(15);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    // ================= PROFILE PANEL =================

    private JPanel buildProfilePanel() {

        JPanel panel = createCard();

        panel.setLayout(new BorderLayout(10, 15));

        JLabel heading = new JLabel("Personal Information");

        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(NAVY);

        JPanel fields = new JPanel(new GridLayout(2, 2, 20, 15));
        fields.setOpaque(false);

        firstNameField = new JTextField();
        lastNameField = new JTextField();
        icPassportField = new JTextField();
        usernameField = new JTextField();

        usernameField.setEditable(false);
        usernameField.setBackground(new Color(235, 238, 242));

        fields.add(createField("First Name", firstNameField));
        fields.add(createField("Last Name", lastNameField));
        fields.add(createField("IC / Passport Number", icPassportField));
        fields.add(createField("Username", usernameField));

        JButton updateButton = new JButton("Save Profile");

        styleButton(updateButton, GREEN);

        updateButton.addActionListener(e -> updateProfile());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(updateButton);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(fields, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // ================= FAMILY PANEL =================

    private JPanel buildFamilyPanel() {

        JPanel panel = createCard();

        panel.setLayout(new BorderLayout(10, 15));

        JLabel heading = new JLabel("Family Details");

        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(NAVY);

        String[] columns = {
                "Family ID",
                "Name",
                "Relationship",
                "Contact Number",
                "Date of Birth"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        familyTable = new JTable(tableModel);

        familyTable.setRowHeight(30);
        familyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        familyTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        familyTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane tableScroll = new JScrollPane(familyTable);
        tableScroll.setPreferredSize(new Dimension(900, 180));

        JButton addButton = new JButton("＋ Add Family Member");
        JButton editButton = new JButton("Edit Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton refreshButton = new JButton("Refresh");

        styleButton(addButton, GREEN);
        styleButton(editButton, NAVY);
        styleButton(deleteButton, new Color(180, 55, 55));
        styleButton(refreshButton, new Color(90, 105, 125));

        addButton.addActionListener(e -> addFamilyMember());
        editButton.addActionListener(e -> editFamilyMember());
        deleteButton.addActionListener(e -> deleteFamilyMember());
        refreshButton.addActionListener(e -> loadFamilyMembers());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);

        actions.add(refreshButton);
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    // ================= LOAD PROFILE =================

    private void loadProfile() {

        try {

            Employee updatedEmployee =
                    hrmClient.getEmployeeById(employeeId);

            if (updatedEmployee == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Employee profile was not found.",
                        "Profile Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            employee = updatedEmployee;

            firstNameField.setText(employee.getFirstName());
            lastNameField.setText(employee.getLastName());
            icPassportField.setText(employee.getIcPassport());
            usernameField.setText(employee.getUsername());

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Unable to Load Profile",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= UPDATE PROFILE =================

    private void updateProfile() {

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String icPassport = icPassportField.getText().trim();

        if (firstName.isEmpty()
                || lastName.isEmpty()
                || icPassport.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please complete all personal information fields."
            );

            return;
        }

        try {

            String result = hrmClient.updateEmployee(
                    employeeId,
                    firstName,
                    lastName,
                    icPassport
            );

            JOptionPane.showMessageDialog(this, result);

            loadProfile();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to update profile: " + e.getMessage(),
                    "Update Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= LOAD FAMILY DETAILS =================

    private void loadFamilyMembers() {

        try {

            familyMembers = hrmClient.getFamilyMembers(employeeId);

            tableModel.setRowCount(0);

            if (familyMembers == null) {
                familyMembers = new ArrayList<>();
                return;
            }

            for (FamilyMember member : familyMembers) {

                tableModel.addRow(new Object[]{
                        member.getFamilyId(),
                        member.getName(),
                        member.getRelationship(),
                        member.getContactNumber(),
                        member.getDateOfBirth()
                });
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load family details: " + e.getMessage(),
                    "Family Details Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= ADD FAMILY MEMBER =================

    private void addFamilyMember() {

        JTextField name = new JTextField();
        JTextField relationship = new JTextField();
        JTextField contact = new JTextField();
        JTextField dob = new JTextField();

        JPanel form = createFamilyForm(
                name, relationship, contact, dob
        );

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Add Family Member",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (!validateFamilyForm(name, relationship, contact, dob)) {
            return;
        }

        FamilyMember member = new FamilyMember(
                0,
                employeeId,
                name.getText().trim(),
                relationship.getText().trim(),
                contact.getText().trim(),
                dob.getText().trim()
        );

        try {

            String response = hrmClient.addFamilyMember(member);

            JOptionPane.showMessageDialog(this, response);

            loadFamilyMembers();

        } catch (Exception e) {

            showError("Unable to add family member: " + e.getMessage());
        }
    }

    // ================= EDIT FAMILY MEMBER =================

    private void editFamilyMember() {

        int selectedRow = familyTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a family member first."
            );

            return;
        }

        FamilyMember oldMember = familyMembers.get(selectedRow);

        JTextField name = new JTextField(oldMember.getName());
        JTextField relationship = new JTextField(
                oldMember.getRelationship()
        );
        JTextField contact = new JTextField(
                oldMember.getContactNumber()
        );
        JTextField dob = new JTextField(
                oldMember.getDateOfBirth()
        );

        JPanel form = createFamilyForm(
                name, relationship, contact, dob
        );

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Edit Family Member",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (!validateFamilyForm(name, relationship, contact, dob)) {
            return;
        }

        FamilyMember updatedMember = new FamilyMember(
                oldMember.getFamilyId(),
                employeeId,
                name.getText().trim(),
                relationship.getText().trim(),
                contact.getText().trim(),
                dob.getText().trim()
        );

        try {

            String response =
                    hrmClient.updateFamilyMember(updatedMember);

            JOptionPane.showMessageDialog(this, response);

            loadFamilyMembers();

        } catch (Exception e) {

            showError("Unable to update family member: " + e.getMessage());
        }
    }

    // ================= DELETE FAMILY MEMBER =================

    private void deleteFamilyMember() {

        int selectedRow = familyTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a family member first."
            );

            return;
        }

        FamilyMember member = familyMembers.get(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete family member: " + member.getName() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            String response =
                    hrmClient.deleteFamilyMember(
                            member.getFamilyId()
                    );

            JOptionPane.showMessageDialog(this, response);

            loadFamilyMembers();

        } catch (Exception e) {

            showError("Unable to delete family member: " + e.getMessage());
        }
    }

    // ================= FAMILY FORM =================

    private JPanel createFamilyForm(
            JTextField name,
            JTextField relationship,
            JTextField contact,
            JTextField dob) {

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));

        form.add(new JLabel("Name:"));
        form.add(name);

        form.add(new JLabel("Relationship:"));
        form.add(relationship);

        form.add(new JLabel("Contact Number:"));
        form.add(contact);

        form.add(new JLabel("Date of Birth (YYYY-MM-DD):"));
        form.add(dob);

        return form;
    }

    private boolean validateFamilyForm(
            JTextField name,
            JTextField relationship,
            JTextField contact,
            JTextField dob) {

        if (name.getText().trim().isEmpty()
                || relationship.getText().trim().isEmpty()
                || contact.getText().trim().isEmpty()
                || dob.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please complete all family member fields."
            );

            return false;
        }

        try {

            java.time.LocalDate.parse(dob.getText().trim());

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter date of birth in YYYY-MM-DD format."
            );

            return false;
        }

        return true;
    }

    // ================= REUSABLE COMPONENTS =================

    private JPanel createCard() {

        JPanel panel = new JPanel();

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 235)
                        ),
                        new EmptyBorder(18, 20, 18, 20)
                )
        );

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return panel;
    }

    private JPanel createField(
            String label,
            JTextField field) {

        JPanel panel = new JPanel();

        panel.setOpaque(false);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(label);

        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(TEXT);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 35)
        );

        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);

        return panel;
    }

    private void styleButton(JButton button, Color color) {

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 15, 9, 15));
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "BHEL HRM Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
