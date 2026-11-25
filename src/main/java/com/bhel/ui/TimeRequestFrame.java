package com.bhel.ui;

import com.bhel.model.TimeRequest;
import com.bhel.remote.HRMService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Employee time requests and HR review queue. */
public class TimeRequestFrame extends JFrame {
    private static final Color NAVY = new Color(18, 39, 67);
    private static final Color BG = new Color(245, 247, 250);
    private static final Color BORDER = new Color(220, 225, 235);
    private static final Color MUTED = new Color(105, 115, 130);
    private static final String[] REQUEST_TYPES = {
            "Short Time Away / Late Arrival / Early Departure",
            "Attendance Time Correction",
            "Overtime"
    };

    private final HRMService service;
    private final String sessionToken;
    private final boolean admin;
    private final String reviewerName;
    private final JComboBox<String> requestType = new JComboBox<>(REQUEST_TYPES);
    private final JSpinner requestDate = new JSpinner(new SpinnerDateModel());
    private final JSpinner startTime = new JSpinner(new SpinnerDateModel());
    private final JSpinner endTime = new JSpinner(new SpinnerDateModel());
    private final JTextArea reason = new JTextArea(4, 30);
    private final DefaultTableModel model;
    private final JTable table;
    private List<TimeRequest> requests = new ArrayList<>();

    public TimeRequestFrame(HRMService service, String sessionToken) {
        this(service, sessionToken, false, null);
    }

    public TimeRequestFrame(HRMService service, String reviewerName, boolean adminMode) {
        this(service, null, true,
                reviewerName == null || reviewerName.isBlank() ? "Admin" : reviewerName);
    }

    private TimeRequestFrame(HRMService service, String sessionToken,
                             boolean admin, String reviewerName) {
        this.service = service;
        this.sessionToken = sessionToken;
        this.admin = admin;
        this.reviewerName = reviewerName;
        String[] columns = admin
                ? new String[]{"Request ID", "Employee ID", "Employee", "Request type",
                    "Date", "Start", "End", "Reason", "Status", "Reviewed by"}
                : new String[]{"Request ID", "Request type", "Date", "Start",
                    "End", "Reason", "Status", "Reviewed by"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);

        setTitle(admin ? "BHEL HRM - Time Request Management" : "BHEL HRM - Time Requests");
        setSize(1050, 690);
        setMinimumSize(new Dimension(850, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 12));
        getContentPane().setBackground(BG);
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(20, 24, 20, 24));
        KeyboardNavigation.installEscape(this, this::dispose);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        styleTable();
        configureSpinners();
        loadRequests();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(admin ? "Time Request Management" : "Time Requests");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel(admin
                ? "Review employee schedule and attendance time requests."
                : "Request short time away, attendance corrections, or overtime approval.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        if (admin) {
            body.add(buildRequestTable(true), BorderLayout.CENTER);
            body.add(buildAdminActions(), BorderLayout.SOUTH);
            return body;
        }
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.addTab("New Time Request", buildRequestForm());
        tabs.addTab("My Requests", buildRequestTable(false));
        body.add(tabs, BorderLayout.CENTER);
        return body;
    }

    private JPanel buildRequestForm() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BG);
        page.setBorder(new EmptyBorder(14, 0, 0, 0));
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(20, 22, 18, 22)));

        JLabel title = new JLabel("Submit a time request");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(NAVY);
        JPanel fields = new JPanel(new GridLayout(2, 2, 18, 14));
        fields.setOpaque(false);
        fields.add(field("Request type", requestType));
        fields.add(field("Request date", requestDate));
        fields.add(field("Start time", startTime));
        fields.add(field("End time", endTime));

        reason.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reason.setLineWrap(true);
        reason.setWrapStyleWord(true);
        reason.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane reasonScroll = new JScrollPane(reason);
        reasonScroll.setPreferredSize(new Dimension(500, 100));
        JPanel reasonPanel = new JPanel(new BorderLayout(0, 6));
        reasonPanel.setOpaque(false);
        JLabel reasonLabel = new JLabel("Reason and supporting details");
        reasonLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        reasonLabel.setForeground(NAVY);
        reasonPanel.add(reasonLabel, BorderLayout.NORTH);
        reasonPanel.add(reasonScroll, BorderLayout.CENTER);

        JLabel help = new JLabel("For short time away, explain whether this is a late arrival, early departure, or personal time.");
        help.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        help.setForeground(MUTED);
        JButton submit = primaryButton("Submit Request");
        submit.addActionListener(event -> submitRequest());
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);
        footer.add(help, BorderLayout.CENTER);
        footer.add(submit, BorderLayout.EAST);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        fields.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        reasonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        fields.setAlignmentX(Component.LEFT_ALIGNMENT);
        reasonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(fields);
        content.add(Box.createVerticalStrut(16));
        content.add(reasonPanel);
        content.add(Box.createVerticalStrut(12));
        content.add(footer);
        card.add(title, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        page.add(card, BorderLayout.NORTH);
        return page;
    }

    private JPanel field(String title, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 7));
        panel.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(NAVY);
        panel.add(label, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRequestTable(boolean adminTable) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG);
        if (!adminTable) panel.setBorder(new EmptyBorder(14, 0, 0, 0));
        JLabel heading = new JLabel(adminTable ? "Requests awaiting review and history" : "My time request history");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 16));
        heading.setForeground(NAVY);
        panel.add(heading, BorderLayout.NORTH);
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        if (!adminTable) {
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            footer.setOpaque(false);
            JButton refresh = secondaryButton("Refresh requests");
            refresh.addActionListener(event -> loadRequests());
            footer.add(refresh);
            panel.add(footer, BorderLayout.SOUTH);
        }
        return panel;
    }

    private JPanel buildAdminActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setOpaque(false);
        JButton refresh = secondaryButton("Refresh");
        JButton reject = dangerButton("Reject selected");
        JButton approve = primaryButton("Approve selected");
        refresh.addActionListener(event -> loadRequests());
        reject.addActionListener(event -> reviewSelected("Rejected"));
        approve.addActionListener(event -> reviewSelected("Approved"));
        actions.add(refresh);
        actions.add(reject);
        actions.add(approve);
        return actions;
    }

    private void configureSpinners() {
        requestDate.setValue(new Date());
        requestDate.setEditor(new JSpinner.DateEditor(requestDate, "yyyy-MM-dd"));
        setTime(startTime, 9, 0);
        setTime(endTime, 9, 30);
        styleSpinner(requestDate);
        styleSpinner(startTime);
        styleSpinner(endTime);
    }

    private void setTime(JSpinner spinner, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        spinner.setValue(calendar.getTime());
        spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinner.setPreferredSize(new Dimension(180, 36));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 13));
            defaultEditor.getTextField().setBorder(new EmptyBorder(4, 7, 4, 7));
        }
    }

    private void styleTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(220, 232, 248));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(NAVY);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 17, 10, 17));
        return button;
    }

    private JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(NAVY);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private JButton dangerButton(String text) {
        JButton button = primaryButton(text);
        button.setBackground(new Color(170, 45, 55));
        return button;
    }

    private void submitRequest() {
        try {
            LocalDate date = ((Date) requestDate.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            LocalTime start = ((Date) startTime.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
            LocalTime end = ((Date) endTime.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
            if (!end.isAfter(start)) {
                warning("End time must be later than start time.");
                return;
            }
            if (reason.getText().isBlank()) {
                warning("Add a reason so HR can review your request.");
                return;
            }
            String result = service.submitTimeRequest(sessionToken,
                    String.valueOf(requestType.getSelectedItem()), date, start, end,
                    reason.getText().trim());
            JOptionPane.showMessageDialog(this, result, "Time Request",
                    result != null && result.toLowerCase().contains("successfully")
                            ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            if (result != null && result.toLowerCase().contains("successfully")) {
                reason.setText("");
                requestDate.setValue(new Date());
                setTime(startTime, 9, 0);
                setTime(endTime, 9, 30);
                loadRequests();
            }
        } catch (Exception e) {
            showError("Unable to submit time request", e);
        }
    }

    private void loadRequests() {
        try {
            requests = admin ? service.getAllTimeRequests()
                    : service.getEmployeeTimeRequests(sessionToken);
            model.setRowCount(0);
            if (requests == null) return;
            for (TimeRequest item : requests) {
                if (admin) {
                    model.addRow(new Object[]{item.getRequestId(), item.getEmployeeId(),
                            item.getEmployeeName(), item.getRequestType(), item.getRequestDate(),
                            item.getStartTime(), item.getEndTime(), item.getReason(),
                            item.getStatus(), item.getReviewedBy()});
                } else {
                    model.addRow(new Object[]{item.getRequestId(), item.getRequestType(),
                            item.getRequestDate(), item.getStartTime(), item.getEndTime(),
                            item.getReason(), item.getStatus(), item.getReviewedBy()});
                }
            }
        } catch (Exception e) {
            showError("Unable to load time requests", e);
        }
    }

    private void reviewSelected(String decision) {
        int selected = table.getSelectedRow();
        if (selected < 0) { warning("Select a time request first."); return; }
        int row = table.convertRowIndexToModel(selected);
        TimeRequest request = requests.get(row);
        if (!"Pending".equalsIgnoreCase(request.getStatus())) {
            warning("Only pending requests can be reviewed.");
            return;
        }
        try {
            String result = service.reviewTimeRequest(request.getRequestId(), decision, reviewerName);
            JOptionPane.showMessageDialog(this, result, "Time Request Review",
                    result != null && result.toLowerCase().contains("successfully")
                            ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            loadRequests();
        } catch (RemoteException e) {
            showError("Unable to review time request", e);
        }
    }

    private void warning(String message) {
        JOptionPane.showMessageDialog(this, message, "Time Request", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String title, Exception e) {
        JOptionPane.showMessageDialog(this, title + ":\n" + e.getMessage(),
                "Time Request Error", JOptionPane.ERROR_MESSAGE);
    }
}
