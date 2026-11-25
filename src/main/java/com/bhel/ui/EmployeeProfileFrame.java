
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
import java.time.LocalDate;

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
    private JTextField employeeIdField;
    private JTextField userIdField;
    private JTextField createdAtField;
    private JTextField usernameField;
    private JComboBox<String> genderField;
    private JTextField dateOfBirthField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField addressField;
    private JLabel profileNameLabel;
    private JLabel profileRoleLabel;
    private JLabel profileDepartmentLabel;
    private JLabel profileIdLabel;
    private JLabel summaryDepartment;
    private JLabel summaryDesignation;
    private JLabel summaryStatus;
    private JLabel summaryJoiningDate;
    private InitialsAvatar initialsAvatar;

    private DefaultTableModel tableModel;
    private JTable familyTable;

    public EmployeeProfileFrame(Employee employee) {

        this.employee = employee;
        this.employeeId = employee.getEmployeeId();

        hrmClient = new HRMClient();

        setTitle("BHEL HRM | Employee Profile");
        setSize(960, 640);
        setMinimumSize(new Dimension(760, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());
        KeyboardNavigation.installEscape(this, this::dispose);
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
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.setForeground(NAVY);
        closeButton.setBackground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(new EmptyBorder(9, 18, 9, 18));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());

        header.add(textPanel, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        return header;
    }

    // ================= MAIN CONTENT =================

    private JPanel buildContent() {
        JPanel profileDetails = buildProfilePanel();
        JPanel summaryContent = new ProfileContentPanel();
        summaryContent.setBackground(BG);
        summaryContent.setBorder(new EmptyBorder(18, 18, 20, 24));
        summaryContent.setLayout(new BoxLayout(summaryContent, BoxLayout.Y_AXIS));

        JLabel summaryTitle = new JLabel("Profile Summary");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        summaryTitle.setForeground(NAVY);
        summaryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryContent.add(summaryTitle);
        summaryContent.add(Box.createVerticalStrut(14));
        summaryContent.add(buildJobInformationPanel());
        summaryContent.add(Box.createVerticalStrut(14));
        summaryContent.add(profileDetails);
        summaryContent.add(Box.createVerticalStrut(14));
        summaryContent.add(buildFamilyPanel());

        JScrollPane summaryScroll = new JScrollPane(summaryContent,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        summaryScroll.setBorder(null);
        summaryScroll.getViewport().setBackground(BG);
        summaryScroll.getVerticalScrollBar().setUnitIncrement(15);

        JPanel identityColumn = new JPanel(new BorderLayout());
        identityColumn.setBackground(BG);
        identityColumn.setBorder(new EmptyBorder(18, 20, 18, 10));
        identityColumn.setPreferredSize(new Dimension(285, 0));
        identityColumn.add(buildIdentityCard(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                identityColumn, summaryScroll);
        split.setBorder(null);
        split.setDividerSize(1);
        split.setResizeWeight(0.0);
        split.setDividerLocation(285);
        split.setContinuousLayout(true);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(split, BorderLayout.CENTER);
        return wrapper;
    }

    // ================= PROFILE PANEL =================

    private JPanel buildProfilePanel() {
        JPanel profileSections = new JPanel();
        profileSections.setOpaque(false);
        profileSections.setLayout(new BoxLayout(profileSections, BoxLayout.Y_AXIS));
        employeeIdField = readOnlyField();
        userIdField = readOnlyField();
        createdAtField = readOnlyField();
        firstNameField = new JTextField();
        lastNameField = new JTextField();
        icPassportField = new JTextField();
        usernameField = readOnlyField();
        genderField = new JComboBox<>(new String[]{"", "Female", "Male", "Other"});
        dateOfBirthField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();
        addressField = new JTextField();
        JPanel personalCard = createCard();
        personalCard.setLayout(new BorderLayout(10, 14));
        personalCard.add(sectionHeading("Personal Information"), BorderLayout.NORTH);
        JPanel personalFields = new JPanel(new GridLayout(0, 2, 16, 13));
        personalFields.setOpaque(false);
        personalFields.add(createField("First name", firstNameField));
        personalFields.add(createField("Last name", lastNameField));
        personalFields.add(createField("IC / Passport number", icPassportField));
        personalFields.add(createField("Gender", genderField));
        personalFields.add(createField("Date of birth (YYYY-MM-DD)", dateOfBirthField));
        personalFields.add(createField("Phone", phoneField));
        personalFields.add(createField("Email", emailField));
        personalFields.add(createField("Address", addressField));
        personalCard.add(personalFields, BorderLayout.CENTER);
        JLabel editNote = new JLabel("You can update your name, identification, and contact details.");
        editNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        editNote.setForeground(new Color(105, 115, 130));
        JButton updateButton = new JButton("Save Profile");
        styleButton(updateButton, GREEN);
        updateButton.addActionListener(e -> updateProfile());
        JPanel actions = new JPanel(new BorderLayout(8, 0));
        actions.setOpaque(false);
        actions.add(editNote, BorderLayout.CENTER);
        actions.add(updateButton, BorderLayout.EAST);
        personalCard.add(actions, BorderLayout.SOUTH);

        JPanel accountCard = createCard();
        accountCard.setLayout(new BorderLayout(10, 12));
        accountCard.add(sectionHeading("Account Information"), BorderLayout.NORTH);
        JPanel accountFields = new JPanel(new GridLayout(0, 2, 16, 13));
        accountFields.setOpaque(false);
        accountFields.add(createField("Employee ID", employeeIdField));
        accountFields.add(createField("User ID", userIdField));
        accountFields.add(createField("Username", usernameField));
        accountFields.add(createField("Account created", createdAtField));
        accountCard.add(accountFields, BorderLayout.CENTER);

        profileSections.add(personalCard);
        profileSections.add(Box.createVerticalStrut(14));
        profileSections.add(accountCard);
        profileSections.setAlignmentX(Component.LEFT_ALIGNMENT);
        return profileSections;
    }

    private JPanel buildIdentityCard() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        initialsAvatar = new InitialsAvatar();
        initialsAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(8));
        card.add(initialsAvatar);
        card.add(Box.createVerticalStrut(16));

        profileNameLabel = new JLabel("Employee");
        profileNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 21));
        profileNameLabel.setForeground(NAVY);
        profileNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(profileNameLabel);
        card.add(Box.createVerticalStrut(5));

        profileRoleLabel = identityDetailLabel("");
        profileDepartmentLabel = identityDetailLabel("");
        card.add(profileRoleLabel);
        card.add(Box.createVerticalStrut(3));
        card.add(profileDepartmentLabel);
        card.add(Box.createVerticalStrut(13));

        profileIdLabel = new JLabel("EMPLOYEE ID  -");
        profileIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        profileIdLabel.setForeground(Color.WHITE);
        profileIdLabel.setOpaque(true);
        profileIdLabel.setBackground(GREEN);
        profileIdLabel.setBorder(new EmptyBorder(6, 9, 6, 9));
        profileIdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(profileIdLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(new JSeparator());
        card.add(Box.createVerticalStrut(12));

        JLabel hint = new JLabel("Profile overview");
        hint.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hint.setForeground(NAVY);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(hint);
        card.add(Box.createVerticalStrut(6));
        JLabel help = new JLabel("<html><div style='width:185px'>Keep your contact details current. Department, role, and employment status are managed by HR.</div></html>");
        help.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        help.setForeground(new Color(105, 115, 130));
        help.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(help);
        card.setPreferredSize(new Dimension(245, 390));
        return card;
    }

    private JPanel buildJobInformationPanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(10, 12));
        card.add(sectionHeading("Job Information"), BorderLayout.NORTH);
        JPanel rows = new JPanel(new GridLayout(0, 2, 14, 10));
        rows.setOpaque(false);
        summaryDepartment = summaryValue();
        summaryDesignation = summaryValue();
        summaryStatus = summaryValue();
        summaryJoiningDate = summaryValue();
        rows.add(summaryRow("Department", summaryDepartment));
        rows.add(summaryRow("Job title", summaryDesignation));
        rows.add(summaryRow("Status", summaryStatus));
        rows.add(summaryRow("Join date", summaryJoiningDate));
        card.add(rows, BorderLayout.CENTER);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private JPanel summaryRow(String title, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT);
        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        return row;
    }

    private JLabel summaryValue() {
        JLabel label = new JLabel("—");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(NAVY);
        return label;
    }

    private JLabel identityDetailLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(105, 115, 130));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel sectionHeading(String text) {
        JLabel heading = new JLabel(text);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heading.setForeground(NAVY);
        return heading;
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

        familyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        familyTable.setFillsViewportHeight(true);

        JScrollPane tableScroll = new JScrollPane(familyTable);
        tableScroll.setPreferredSize(new Dimension(0, 155));

        JButton addButton = new JButton("Add Family Member");
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

        JPanel actions = new JPanel(new GridLayout(2, 2, 8, 8));
        actions.setOpaque(false);
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(refreshButton);

        JPanel familyHeader = new JPanel();
        familyHeader.setOpaque(false);
        familyHeader.setLayout(new BoxLayout(familyHeader, BoxLayout.Y_AXIS));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        familyHeader.add(heading);
        familyHeader.add(Box.createVerticalStrut(12));
        familyHeader.add(actions);

        panel.add(familyHeader, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);

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

            employeeIdField.setText(String.valueOf(employee.getEmployeeId()));
            userIdField.setText(String.valueOf(employee.getUserId()));
            createdAtField.setText(employee.getCreatedAt() == null
                    ? "" : employee.getCreatedAt().toString());
            firstNameField.setText(employee.getFirstName());
            lastNameField.setText(employee.getLastName());
            icPassportField.setText(employee.getIcPassport());
            usernameField.setText(employee.getUsername());
            genderField.setSelectedItem(employee.getGender() == null
                    ? "" : employee.getGender());
            dateOfBirthField.setText(employee.getDateOfBirth() == null
                    ? "" : employee.getDateOfBirth().toString());
            phoneField.setText(employee.getPhone());
            emailField.setText(employee.getEmail());
            addressField.setText(employee.getAddress());
            summaryDepartment.setText(displayValue(employee.getDepartment()));
            summaryDesignation.setText(displayValue(employee.getDesignation()));
            summaryJoiningDate.setText(employee.getJoiningDate() == null
                    ? "Not provided" : employee.getJoiningDate().toString());
            summaryStatus.setText(displayValue(employee.getEmploymentStatus()));
            profileNameLabel.setText((employee.getFirstName() + " "
                    + employee.getLastName()).trim());
            profileRoleLabel.setText(displayValue(employee.getDesignation()));
            profileDepartmentLabel.setText(displayValue(employee.getDepartment()));
            profileIdLabel.setText("EMPLOYEE ID  " + employee.getEmployeeId());
            initialsAvatar.setInitials(initials(employee.getFirstName(), employee.getLastName()));

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
        LocalDate dateOfBirth;
        try {
            dateOfBirth = parseOptionalDate(dateOfBirthField.getText());
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Date of birth must use YYYY-MM-DD format.",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

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
                    icPassport,
                    (String) genderField.getSelectedItem(),
                    dateOfBirth,
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    addressField.getText().trim(),
                    employee.getDepartment(),
                    employee.getDesignation(),
                    employee.getJoiningDate(),
                    employee.getEmploymentStatus()
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
            JComponent field) {

        JPanel panel = new JPanel();

        panel.setOpaque(false);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(label);

        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 35)
        );
        field.setPreferredSize(new Dimension(180, 35));

        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);

        return panel;
    }

    private String displayValue(String value) {
        return value == null || value.isBlank() ? "Not provided" : value.trim();
    }

    private String initials(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        String result = (first.isEmpty() ? "" : first.substring(0, 1))
                + (last.isEmpty() ? "" : last.substring(0, 1));
        return result.isEmpty() ? "HR" : result.toUpperCase(java.util.Locale.ROOT);
    }

    private static final class ProfileContentPanel extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle rect, int orientation, int direction) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle rect, int orientation, int direction) {
            return Math.max(16, rect.height - 16);
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static final class InitialsAvatar extends JComponent {
        private String initials = "HR";

        private InitialsAvatar() {
            setPreferredSize(new Dimension(88, 88));
            setMinimumSize(new Dimension(88, 88));
            setMaximumSize(new Dimension(88, 88));
        }

        private void setInitials(String initials) {
            this.initials = initials;
            repaint();
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(222, 240, 235));
            g.fillOval(3, 3, getWidth() - 6, getHeight() - 6);
            g.setFont(new Font("Segoe UI", Font.BOLD, 26));
            g.setColor(GREEN);
            FontMetrics metrics = g.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(initials)) / 2;
            int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g.drawString(initials, x, y);
            g.dispose();
        }
    }

    private JTextField readOnlyField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setBackground(new Color(235, 238, 242));
        return field;
    }

    private LocalDate parseOptionalDate(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : LocalDate.parse(trimmed);
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
