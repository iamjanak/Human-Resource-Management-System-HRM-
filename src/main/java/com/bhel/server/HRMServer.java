
package com.bhel.server;

import com.bhel.database.DBConnection;
import com.bhel.remote.HRMService;
import com.bhel.security.PasswordUtil;

import com.bhel.model.FamilyMember;
import com.bhel.model.LeaveApplication;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Instant;
import com.bhel.model.Employee;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;
import com.bhel.model.Department;
import com.bhel.model.Attendance;
import java.sql.Time;
import java.time.LocalTime;

public class HRMServer extends UnicastRemoteObject implements HRMService {

    private final Map<String, AttendanceSession> attendanceSessions =
            new ConcurrentHashMap<>();

    private record AttendanceSession(int employeeId, Instant expiresAt) { }

    public HRMServer() throws RemoteException {
        super();
    }

    // LOGIN
    @Override
    public String login(String username, String password, String role)
            throws RemoteException {

        String selectedRole;

        if ("Admin / HR Staff".equalsIgnoreCase(role)
                || "ADMIN_HR".equalsIgnoreCase(role)) {

            selectedRole = "ADMIN_HR";

        } else if ("Employee".equalsIgnoreCase(role)
                || "EMPLOYEE".equalsIgnoreCase(role)) {

            selectedRole = "EMPLOYEE";

        } else {
            return "Invalid selected role.";
        }

        String sql = "SELECT password_hash, role FROM users "
                + "WHERE username = ? AND is_active = TRUE";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    String storedHash = result.getString("password_hash");
                    String storedRole = result.getString("role");

                    boolean passwordValid =
                            PasswordUtil.verifyPassword(password, storedHash);

                    if (passwordValid && selectedRole.equals(storedRole)) {
                        return "Login successful!";
                    }
                }
            }

            return "Invalid username, password, or selected role.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Login service is temporarily unavailable.";
        }
    }

    // EMPLOYEE LOGIN - GET ACTUAL EMPLOYEE DETAILS
@Override
public Employee loginEmployee(String username, String password)
        throws RemoteException {

    String sql = "SELECT e.employee_id, e.user_id, e.created_at, "
            + "e.first_name, e.last_name, e.ic_passport, e.gender, "
            + "e.date_of_birth, e.phone, e.email, e.address, "
            + "e.department, e.designation, e.joining_date, "
            + "e.employment_status, u.username, "
            + "u.password_hash "
            + "FROM users u "
            + "JOIN employees e ON u.user_id = e.user_id "
            + "WHERE u.username = ? "
            + "AND u.role = 'EMPLOYEE' "
            + "AND u.is_active = TRUE";

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setString(1, username);

        try (ResultSet result = statement.executeQuery()) {

            if (result.next()) {

                String storedHash =
                        result.getString("password_hash");

                boolean passwordValid =
                        PasswordUtil.verifyPassword(
                                password, storedHash);

                if (passwordValid) {
                    Employee employee = mapEmployee(result);
                    String token = UUID.randomUUID().toString();
                    attendanceSessions.put(token, new AttendanceSession(
                            employee.getEmployeeId(), Instant.now().plusSeconds(24 * 60 * 60)));
                    employee.setAttendanceSessionToken(token);
                    return employee;
                }
            }

            return null;

        }

    } catch (Exception e) {

        e.printStackTrace();

        throw new RemoteException(
                "Employee login failed.", e);
    }
}

    // CHANGE ACCOUNT PASSWORD
    @Override
    public String changePassword(String username, String currentPassword,
                                 String newPassword) throws RemoteException {

        if (username == null || username.trim().isEmpty()
                || currentPassword == null || currentPassword.isEmpty()
                || newPassword == null || newPassword.isEmpty()) {
            return "Username and all password fields are required.";
        }

        String selectSql = "SELECT user_id, password_hash FROM users "
                + "WHERE username = ? AND is_active = TRUE";
        String updateSql = "UPDATE users SET password_hash = ? "
                + "WHERE user_id = ? AND is_active = TRUE";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int userId;
                String storedHash;
                try (PreparedStatement statement =
                             connection.prepareStatement(selectSql)) {
                    statement.setString(1, username.trim());
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return "Account not found or inactive.";
                        }
                        userId = result.getInt("user_id");
                        storedHash = result.getString("password_hash");
                    }
                }

                if (!PasswordUtil.verifyPassword(currentPassword, storedHash)) {
                    connection.rollback();
                    return "Current password is incorrect.";
                }

                String newHash = PasswordUtil.hashPassword(newPassword);
                try (PreparedStatement statement =
                             connection.prepareStatement(updateSql)) {
                    statement.setString(1, newHash);
                    statement.setInt(2, userId);
                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return "Password was not changed. Please try again.";
                    }
                }

                connection.commit();
                return "Password changed successfully.";
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Password change failed: " + e.getMessage();
        }
    }

    // HR RESET FOR AN EMPLOYEE WHO CANNOT SIGN IN
    @Override
    public String adminResetEmployeePassword(String adminUsername,
                                             String adminPassword,
                                             String employeeUsername,
                                             String newPassword)
            throws RemoteException {

        if (adminUsername == null || adminUsername.trim().isEmpty()
                || adminPassword == null || adminPassword.isEmpty()
                || employeeUsername == null
                || employeeUsername.trim().isEmpty()
                || newPassword == null || newPassword.isEmpty()) {
            return "HR credentials, employee username, and new password are required.";
        }

        String adminSql = "SELECT password_hash FROM users "
                + "WHERE username = ? AND role = 'ADMIN_HR' "
                + "AND is_active = TRUE";
        String updateSql = "UPDATE users SET password_hash = ? "
                + "WHERE username = ? AND role = 'EMPLOYEE' "
                + "AND is_active = TRUE";

        try (Connection connection = DBConnection.getConnection()) {
            String adminHash;
            try (PreparedStatement statement =
                         connection.prepareStatement(adminSql)) {
                statement.setString(1, adminUsername.trim());
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return "HR account was not found or is inactive.";
                    }
                    adminHash = result.getString("password_hash");
                }
            }

            if (!PasswordUtil.verifyPassword(adminPassword, adminHash)) {
                return "HR password is incorrect.";
            }

            String newHash = PasswordUtil.hashPassword(newPassword);
            try (PreparedStatement statement =
                         connection.prepareStatement(updateSql)) {
                statement.setString(1, newHash);
                statement.setString(2, employeeUsername.trim());
                int updated = statement.executeUpdate();
                return updated > 0
                        ? "Employee password reset successfully. Give the employee the new password securely."
                        : "Active employee account was not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Employee password reset failed: " + e.getMessage();
        }
    }

    // REGISTER EMPLOYEE
    @Override
    public String registerEmployee(
            String firstName,
            String lastName,
            String icPassport,
            String gender,
            LocalDate dateOfBirth,
            String phone,
            String email,
            String address,
            String department,
            String designation,
            LocalDate joiningDate,
            String employmentStatus,
            String username,
            String password) throws RemoteException {

        String checkSql =
                "SELECT user_id FROM users WHERE username = ?";

        String checkEmployeeSql =
                "SELECT employee_id FROM employees WHERE ic_passport = ?";

        String userSql =
                "INSERT INTO users (username, password_hash, role) "
                + "VALUES (?, ?, 'EMPLOYEE') RETURNING user_id";

        String employeeSql =
                "INSERT INTO employees "
                + "(first_name, last_name, ic_passport, gender, "
                + "date_of_birth, phone, email, address, department, "
                + "designation, joining_date, employment_status, user_id, "
                + "created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                // Check whether username already exists
                try (PreparedStatement statement =
                             connection.prepareStatement(checkSql)) {

                    statement.setString(1, username);

                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            connection.rollback();
                            return "Username already exists.";
                        }
                    }
                }

                // Check whether IC/Passport already exists
                try (PreparedStatement statement =
                             connection.prepareStatement(checkEmployeeSql)) {

                    statement.setString(1, icPassport);

                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            connection.rollback();
                            return "IC / Passport number already exists.";
                        }
                    }
                }

                // Hash password
                String passwordHash = PasswordUtil.hashPassword(password);

                // Create employee login account
                int userId;

                try (PreparedStatement statement =
                             connection.prepareStatement(userSql)) {

                    statement.setString(1, username);
                    statement.setString(2, passwordHash);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            connection.rollback();
                            return "Unable to create employee account.";
                        }

                        userId = result.getInt("user_id");
                    }
                }

                // Save employee information
                try (PreparedStatement statement =
                             connection.prepareStatement(employeeSql)) {

                    statement.setString(1, firstName);
                    statement.setString(2, lastName);
                    statement.setString(3, icPassport);
                    statement.setString(4, blankToNull(gender));
                    setSqlDate(statement, 5, dateOfBirth);
                    statement.setString(6, blankToNull(phone));
                    statement.setString(7, blankToNull(email));
                    statement.setString(8, blankToNull(address));
                    statement.setString(9, blankToNull(department));
                    statement.setString(10, blankToNull(designation));
                    setSqlDate(statement, 11, joiningDate);
                    statement.setString(12,
                            employmentStatus == null
                                    || employmentStatus.trim().isEmpty()
                                    ? "Active" : employmentStatus.trim());
                    statement.setInt(13, userId);

                    statement.executeUpdate();
                }

                connection.commit();

                return "Employee registered successfully!";

            } catch (Exception e) {

                connection.rollback();
                e.printStackTrace();

                return "Employee registration failed: " + e.getMessage();

            } finally {

                connection.setAutoCommit(true);
            }

        } catch (Exception e) {

            e.printStackTrace();

            return "Registration service is temporarily unavailable.";
        }
    }

    // START RMI SERVER
    public static void main(String[] args) {

        try {
            HRMServer server = new HRMServer();

            LocateRegistry.createRegistry(1099);

            Naming.rebind(
                    "rmi://localhost:1099/HRMService",
                    server);

            System.out.println("BHEL HRM RMI Server is running...");
            System.out.println("Waiting for client requests...");

            new CountDownLatch(1).await();

        } catch (Exception e) {

            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Employee mapEmployee(ResultSet result)
            throws java.sql.SQLException {
        Timestamp createdAt = result.getTimestamp("created_at");
        Date dateOfBirth = result.getDate("date_of_birth");
        Date joiningDate = result.getDate("joining_date");

        return new Employee(
                result.getInt("employee_id"),
                result.getInt("user_id"),
                result.getString("first_name"),
                result.getString("last_name"),
                result.getString("ic_passport"),
                result.getString("username"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                result.getString("gender"),
                dateOfBirth == null ? null : dateOfBirth.toLocalDate(),
                result.getString("phone"),
                result.getString("email"),
                result.getString("address"),
                result.getString("department"),
                result.getString("designation"),
                joiningDate == null ? null : joiningDate.toLocalDate(),
                result.getString("employment_status"));
    }


    private static String blankToNull(String value) {
        return value == null || value.trim().isEmpty()
                ? null : value.trim();
    }


    private static void setSqlDate(PreparedStatement statement,
                                   int parameterIndex,
                                   LocalDate value)
            throws java.sql.SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, java.sql.Types.DATE);
        } else {
            statement.setDate(parameterIndex, Date.valueOf(value));
        }
    }
    
    // GET ALL EMPLOYEES
    @Override
    public List<Employee> getAllEmployees() throws RemoteException {

        List<Employee> employees = new ArrayList<>();

        String sql = "SELECT e.employee_id, e.user_id, e.created_at, "
                + "e.first_name, e.last_name, e.ic_passport, e.gender, "
                + "e.date_of_birth, e.phone, e.email, e.address, "
                + "e.department, e.designation, e.joining_date, "
                + "e.employment_status, u.username "
                + "FROM employees e "
                + "JOIN users u ON e.user_id = u.user_id "
                + "ORDER BY e.employee_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                employees.add(mapEmployee(result));
            }

            return employees;

        } catch (Exception e) {
            e.printStackTrace();

            throw new RemoteException(
                    "Unable to retrieve employee records.", e
            );
        }
    }

    
    // UPDATE EMPLOYEE DETAILS
    @Override
    public String updateEmployee(
            int employeeId,
            String firstName,
            String lastName,
            String icPassport,
            String gender,
            LocalDate dateOfBirth,
            String phone,
            String email,
            String address,
            String department,
            String designation,
            LocalDate joiningDate,
            String employmentStatus) throws RemoteException {

        if (employeeId <= 0 || firstName == null || firstName.trim().isEmpty()
                || lastName == null || lastName.trim().isEmpty()
                || icPassport == null || icPassport.trim().isEmpty()) {
            return "Employee ID, first name, last name, and IC/Passport are required.";
        }

        String checkSql =
                "SELECT employee_id FROM employees "
                + "WHERE ic_passport = ? AND employee_id <> ?";

        String updateSql =
                "UPDATE employees "
                + "SET first_name = ?, last_name = ?, ic_passport = ?, "
                + "gender = ?, date_of_birth = ?, phone = ?, email = ?, "
                + "address = ?, department = ?, designation = ?, "
                + "joining_date = ?, employment_status = ? "
                + "WHERE employee_id = ?";

        try (Connection connection = DBConnection.getConnection()) {

            // Check duplicate IC / Passport
            try (PreparedStatement statement =
                         connection.prepareStatement(checkSql)) {

                statement.setString(1, icPassport);
                statement.setInt(2, employeeId);

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {
                        return "IC / Passport number already exists.";
                    }
                }
            }

            // Update employee
            try (PreparedStatement statement =
                         connection.prepareStatement(updateSql)) {

                statement.setString(1, firstName);
                statement.setString(2, lastName);
                statement.setString(3, icPassport);
                statement.setString(4, blankToNull(gender));
                setSqlDate(statement, 5, dateOfBirth);
                statement.setString(6, blankToNull(phone));
                statement.setString(7, blankToNull(email));
                statement.setString(8, blankToNull(address));
                statement.setString(9, blankToNull(department));
                statement.setString(10, blankToNull(designation));
                setSqlDate(statement, 11, joiningDate);
                statement.setString(12,
                        employmentStatus == null
                                || employmentStatus.trim().isEmpty()
                                ? "Active" : employmentStatus.trim());
                statement.setInt(13, employeeId);

                int rowsUpdated = statement.executeUpdate();

                if (rowsUpdated > 0) {
                    return "Employee updated successfully!";
                }

                return "Employee not found.";
            }

        } catch (Exception e) {

            e.printStackTrace();

            return "Employee update failed: " + e.getMessage();
        }
    }


    
    // ==========================================
    // LEAVE MANAGEMENT
    // ==========================================

    // APPLY FOR LEAVE
    @Override
    public String applyLeave(LeaveApplication leave)
            throws RemoteException {

        String sql = "INSERT INTO leave_applications "
                + "(employee_id, leave_type, start_date, "
                + "end_date, reason) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            if (leave.getStartDate() == null
                    || leave.getEndDate() == null
                    || leave.getEndDate().isBefore(leave.getStartDate())) {
                return "Invalid leave dates.";
            }

            statement.setInt(1, leave.getEmployeeId());
            statement.setString(2, leave.getLeaveType());
            statement.setDate(3, Date.valueOf(leave.getStartDate()));
            statement.setDate(4, Date.valueOf(leave.getEndDate()));
            statement.setString(5, leave.getReason());

            statement.executeUpdate();

            return "Leave application submitted successfully!";

        } catch (Exception e) {
            e.printStackTrace();
            return "Leave application failed: " + e.getMessage();
        }
    }

    // GET EMPLOYEE LEAVE APPLICATIONS
    @Override
    public List<LeaveApplication> getEmployeeLeaves(int employeeId)
            throws RemoteException {

        List<LeaveApplication> leaves = new ArrayList<>();

        String sql = "SELECT * FROM leave_applications "
                + "WHERE employee_id = ? "
                + "ORDER BY applied_date DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    LeaveApplication leave = new LeaveApplication();

                    leave.setLeaveId(result.getInt("leave_id"));
                    leave.setEmployeeId(result.getInt("employee_id"));
                    leave.setLeaveType(result.getString("leave_type"));

                    leave.setStartDate(
                            result.getDate("start_date").toLocalDate());

                    leave.setEndDate(
                            result.getDate("end_date").toLocalDate());

                    leave.setReason(result.getString("reason"));
                    leave.setStatus(result.getString("status"));
                    leave.setReviewedBy(result.getString("reviewed_by"));
                    Timestamp appliedDate = result.getTimestamp("applied_date");
                    leave.setAppliedDate(appliedDate == null
                            ? null : appliedDate.toLocalDateTime());

                    leaves.add(leave);
                }
            }

            return leaves;

        } catch (Exception e) {
            e.printStackTrace();

            throw new RemoteException(
                    "Unable to retrieve employee leave applications.", e);
        }
    }


    // GET ALL LEAVE APPLICATIONS (ADMIN)
    @Override
    public List<LeaveApplication> getAllLeaveApplications()
            throws RemoteException {

        List<LeaveApplication> leaves = new ArrayList<>();

        String sql = "SELECT la.*, e.first_name, e.last_name "
                + "FROM leave_applications la "
                + "JOIN employees e ON la.employee_id = e.employee_id "
                + "ORDER BY la.applied_date DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                LeaveApplication leave = new LeaveApplication();

                leave.setLeaveId(result.getInt("leave_id"));
                leave.setEmployeeId(result.getInt("employee_id"));
                leave.setEmployeeName(
                        (result.getString("first_name") + " "
                                + result.getString("last_name")).trim());
                leave.setLeaveType(result.getString("leave_type"));

                leave.setStartDate(
                        result.getDate("start_date").toLocalDate());

                leave.setEndDate(
                        result.getDate("end_date").toLocalDate());

                leave.setReason(result.getString("reason"));
                leave.setStatus(result.getString("status"));
                leave.setReviewedBy(result.getString("reviewed_by"));
                Timestamp appliedDate = result.getTimestamp("applied_date");
                leave.setAppliedDate(appliedDate == null
                        ? null : appliedDate.toLocalDateTime());

                leaves.add(leave);
            }

            return leaves;

        } catch (Exception e) {
            e.printStackTrace();

            throw new RemoteException(
                    "Unable to retrieve leave applications.", e);
        }
    }


    // APPROVE OR REJECT LEAVE (ADMIN)
    @Override
    public String reviewLeave(
            int leaveId,
            String status,
            String reviewedBy) throws RemoteException {

        if (!"Approved".equalsIgnoreCase(status)
                && !"Rejected".equalsIgnoreCase(status)) {

            return "Invalid leave status.";
        }

        if (reviewedBy == null || reviewedBy.trim().isEmpty()) {
            return "Reviewing HR username is required.";
        }

        String finalStatus = "Approved".equalsIgnoreCase(status)
                ? "Approved" : "Rejected";

        String sql = "UPDATE leave_applications "
                + "SET status = ?, reviewed_by = ?, "
                + "reviewed_date = CURRENT_TIMESTAMP "
                + "WHERE leave_id = ? AND status = 'Pending' "
                + "AND EXISTS (SELECT 1 FROM users u "
                + "WHERE u.username = ? AND u.role = 'ADMIN_HR' "
                + "AND u.is_active = TRUE)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, finalStatus);
            statement.setString(2, reviewedBy.trim());
            statement.setInt(3, leaveId);
            statement.setString(4, reviewedBy.trim());

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                return "Leave application " + status.toLowerCase()
                        + " successfully!";
            }

            return "Request not found, already reviewed, or HR account is invalid.";

        } catch (Exception e) {
            e.printStackTrace();

            return "Leave review failed: " + e.getMessage();
        }
    }




    // ==========================================
    // EMPLOYEE PROFILE MANAGEMENT
    // ==========================================

    // GET EMPLOYEE BY ID
    @Override
    public Employee getEmployeeById(int employeeId)
            throws RemoteException {

        String sql = "SELECT e.employee_id, e.user_id, e.created_at, "
                + "e.first_name, e.last_name, e.ic_passport, e.gender, "
                + "e.date_of_birth, e.phone, e.email, e.address, "
                + "e.department, e.designation, e.joining_date, "
                + "e.employment_status, u.username "
                + "FROM employees e "
                + "JOIN users u ON e.user_id = u.user_id "
                + "WHERE e.employee_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapEmployee(result);
                }
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();

            throw new RemoteException(
                    "Unable to retrieve employee profile.", e);
        }
    }


    // ==========================================
    // FAMILY DETAILS MANAGEMENT
    // ==========================================

    // ADD FAMILY MEMBER
    @Override
    public String addFamilyMember(FamilyMember familyMember)
            throws RemoteException {

        if (familyMember == null
                || familyMember.getName() == null
                || familyMember.getName().trim().isEmpty()
                || familyMember.getRelationship() == null
                || familyMember.getRelationship().trim().isEmpty()
                || familyMember.getDateOfBirth() == null
                || familyMember.getDateOfBirth().trim().isEmpty()) {

            return "Please provide all required family member details.";
        }

        String sql = "INSERT INTO employee_family "
                + "(employee_id, family_member_name, relationship, "
                + "contact_number, date_of_birth) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            LocalDate dob = LocalDate.parse(
                    familyMember.getDateOfBirth());

            if (dob.isAfter(LocalDate.now())) {
                return "Date of birth cannot be in the future.";
            }

            statement.setInt(1, familyMember.getEmployeeId());
            statement.setString(2, familyMember.getName().trim());
            statement.setString(3, familyMember.getRelationship().trim());
            statement.setString(4, familyMember.getContactNumber());
            statement.setDate(5, Date.valueOf(dob));

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                return "Family member added successfully!";
            }

            return "Unable to add family member.";

        } catch (Exception e) {
            e.printStackTrace();

            return "Family member addition failed: " + e.getMessage();
        }
    }


    // GET FAMILY MEMBERS BY EMPLOYEE ID
    @Override
    public List<FamilyMember> getFamilyMembers(int employeeId)
            throws RemoteException {

        List<FamilyMember> familyMembers = new ArrayList<>();

        String sql = "SELECT family_id, employee_id, "
                + "family_member_name, relationship, "
                + "contact_number, date_of_birth "
                + "FROM employee_family "
                + "WHERE employee_id = ? "
                + "ORDER BY family_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Date dob = result.getDate("date_of_birth");

                    FamilyMember member = new FamilyMember(
                            result.getInt("family_id"),
                            result.getInt("employee_id"),
                            result.getString("family_member_name"),
                            result.getString("relationship"),
                            result.getString("contact_number"),
                            dob != null ? dob.toString() : ""
                    );

                    familyMembers.add(member);
                }
            }

            return familyMembers;

        } catch (Exception e) {
            e.printStackTrace();

            throw new RemoteException(
                    "Unable to retrieve family details.", e);
        }
    }


    // UPDATE FAMILY MEMBER
    @Override
    public String updateFamilyMember(FamilyMember familyMember)
            throws RemoteException {

        if (familyMember == null
                || familyMember.getName() == null
                || familyMember.getName().trim().isEmpty()
                || familyMember.getRelationship() == null
                || familyMember.getRelationship().trim().isEmpty()
                || familyMember.getDateOfBirth() == null
                || familyMember.getDateOfBirth().trim().isEmpty()) {

            return "Please provide all required family member details.";
        }

        String sql = "UPDATE employee_family "
                + "SET family_member_name = ?, relationship = ?, "
                + "contact_number = ?, date_of_birth = ? "
                + "WHERE family_id = ? AND employee_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            LocalDate dob = LocalDate.parse(
                    familyMember.getDateOfBirth());

            if (dob.isAfter(LocalDate.now())) {
                return "Date of birth cannot be in the future.";
            }

            statement.setString(1, familyMember.getName().trim());
            statement.setString(2, familyMember.getRelationship().trim());
            statement.setString(3, familyMember.getContactNumber());
            statement.setDate(4, Date.valueOf(dob));
            statement.setInt(5, familyMember.getFamilyId());
            statement.setInt(6, familyMember.getEmployeeId());

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                return "Family member updated successfully!";
            }

            return "Family member not found.";

        } catch (Exception e) {
            e.printStackTrace();

            return "Family member update failed: " + e.getMessage();
        }
    }


    // DELETE FAMILY MEMBER
    @Override
    public String deleteFamilyMember(int familyId)
            throws RemoteException {

        String sql = "DELETE FROM employee_family "
                + "WHERE family_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, familyId);

            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted > 0) {
                return "Family member deleted successfully!";
            }

            return "Family member not found.";

        } catch (Exception e) {
            e.printStackTrace();

            return "Family member deletion failed: " + e.getMessage();
        }
    }
    

    // ==========================================
    // DEPARTMENT MANAGEMENT
    // ==========================================

    // ADD DEPARTMENT
    @Override
    public String addDepartment(Department department)
            throws RemoteException {

        if (department == null
                || department.getDepartmentName() == null
                || department.getDepartmentName().trim().isEmpty()) {
            return "Department name is required.";
        }

        String sql = "INSERT INTO departments "
                + "(department_name, description) VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1, department.getDepartmentName().trim());

            statement.setString(
                    2, department.getDescription() == null
                            ? "" : department.getDescription().trim());

            int rows = statement.executeUpdate();

            return rows > 0
                    ? "Department added successfully!"
                    : "Unable to add department.";

        } catch (java.sql.SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                return "A department with this name already exists.";
            }

            e.printStackTrace();
            return "Department addition failed: " + e.getMessage();

        } catch (Exception e) {
            e.printStackTrace();
            return "Department addition failed: " + e.getMessage();
        }
    }

    // GET ALL DEPARTMENTS
    @Override
    public List<Department> getAllDepartments()
            throws RemoteException {

        List<Department> departments = new ArrayList<>();

        String sql = "SELECT department_id, department_name, description "
                + "FROM departments ORDER BY department_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                Department department = new Department(
                        result.getInt("department_id"),
                        result.getString("department_name"),
                        result.getString("description")
                );

                departments.add(department);
            }

            return departments;

        } catch (Exception e) {
            e.printStackTrace();

            throw new RemoteException(
                    "Unable to retrieve departments.", e);
        }
    }

    // UPDATE DEPARTMENT
    @Override
    public String updateDepartment(Department department)
            throws RemoteException {

        if (department == null
                || department.getDepartmentId() <= 0
                || department.getDepartmentName() == null
                || department.getDepartmentName().trim().isEmpty()) {

            return "Valid department ID and department name are required.";
        }

        String sql = "UPDATE departments "
                + "SET department_name = ?, description = ? "
                + "WHERE department_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1, department.getDepartmentName().trim());

            statement.setString(
                    2, department.getDescription() == null
                            ? "" : department.getDescription().trim());

            statement.setInt(3, department.getDepartmentId());

            int rows = statement.executeUpdate();

            return rows > 0
                    ? "Department updated successfully!"
                    : "Department not found.";

        } catch (java.sql.SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                return "A department with this name already exists.";
            }

            e.printStackTrace();
            return "Department update failed: " + e.getMessage();

        } catch (Exception e) {
            e.printStackTrace();
            return "Department update failed: " + e.getMessage();
        }
    }

    // DELETE DEPARTMENT
    @Override
    public String deleteDepartment(int departmentId)
            throws RemoteException {

        if (departmentId <= 0) {
            return "Invalid department ID.";
        }

        String sql = "DELETE FROM departments WHERE department_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, departmentId);

            int rows = statement.executeUpdate();

            return rows > 0
                    ? "Department deleted successfully!"
                    : "Department not found.";

        } catch (java.sql.SQLException e) {

            if ("23503".equals(e.getSQLState())) {
                return "This department is assigned to employee records "
                        + "and cannot be deleted.";
            }

            e.printStackTrace();
            return "Department deletion failed: " + e.getMessage();

        } catch (Exception e) {
            e.printStackTrace();
            return "Department deletion failed: " + e.getMessage();
        }
    }

    // ==========================================
    // ATTENDANCE MANAGEMENT
    // ==========================================

    @Override
    public String submitEmployeeAttendance(String attendanceSessionToken,
                                           LocalTime checkIn,
                                           LocalTime checkOut,
                                           String remarks)
            throws RemoteException {
        int employeeId = requireAttendanceSession(attendanceSessionToken);
        if (checkIn == null) return "Start Time is required.";
        if (checkOut != null && checkOut.isBefore(checkIn)) {
            return "End Time cannot be earlier than Start Time.";
        }
        if (remarks == null || remarks.isBlank()) return "Remarks are required.";

        String employeeSql = "SELECT employment_status FROM employees "
                + "WHERE employee_id = ? FOR UPDATE";
        String existingSql = "SELECT attendance_id FROM attendance "
                + "WHERE employee_id = ? AND attendance_date = CURRENT_DATE FOR UPDATE";
        String insertSql = "INSERT INTO attendance "
                + "(employee_id, attendance_date, check_in, check_out, status, remarks) "
                + "VALUES (?, CURRENT_DATE, ?, ?, 'Present', ?)";
        String updateSql = "UPDATE attendance SET check_in = ?, check_out = ?, remarks = ? "
                + "WHERE attendance_id = ?";
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(employeeSql)) {
                    statement.setInt(1, employeeId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return "Employee record was not found.";
                        }
                        String employmentStatus = result.getString("employment_status");
                        if (employmentStatus != null
                                && !"active".equalsIgnoreCase(employmentStatus.trim())) {
                            connection.rollback();
                            return "Attendance cannot be submitted for an inactive employee.";
                        }
                    }
                }

                Integer attendanceId = null;
                try (PreparedStatement statement = connection.prepareStatement(existingSql)) {
                    statement.setInt(1, employeeId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) attendanceId = result.getInt("attendance_id");
                    }
                }
                if (attendanceId == null) {
                    try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                        statement.setInt(1, employeeId);
                        statement.setTime(2, Time.valueOf(checkIn));
                        if (checkOut == null) statement.setNull(3, java.sql.Types.TIME);
                        else statement.setTime(3, Time.valueOf(checkOut));
                        statement.setString(4, "Employee entered: " + remarks.trim());
                        statement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                        statement.setTime(1, Time.valueOf(checkIn));
                        if (checkOut == null) statement.setNull(2, java.sql.Types.TIME);
                        else statement.setTime(2, Time.valueOf(checkOut));
                        statement.setString(3, "Employee updated: " + remarks.trim());
                        statement.setInt(4, attendanceId);
                        statement.executeUpdate();
                    }
                }
                connection.commit();
                return "Attendance submitted successfully for today.";
            } catch (java.sql.SQLException e) {
                connection.rollback();
                if ("23505".equals(e.getSQLState())) {
                    return "Attendance is already recorded for today. Refresh and try again.";
                }
                throw e;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RemoteException("Unable to save employee attendance.", e);
        }
    }
    @Override
    public List<Attendance> getEmployeeAttendance(String attendanceSessionToken)
            throws RemoteException {
        int employeeId = requireAttendanceSession(attendanceSessionToken);
        List<Attendance> records = new ArrayList<>();
        String sql = "SELECT a.attendance_id, a.employee_id, e.first_name, e.last_name, "
                + "a.attendance_date, a.check_in, a.check_out, a.status, a.remarks "
                + "FROM attendance a JOIN employees e ON e.employee_id = a.employee_id "
                + "WHERE a.employee_id = ? "
                + "ORDER BY a.attendance_date DESC, a.attendance_id DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Date date = result.getDate("attendance_date");
                    Time checkIn = result.getTime("check_in");
                    Time checkOut = result.getTime("check_out");
                    String name = (valueOrEmpty(result.getString("first_name")) + " "
                            + valueOrEmpty(result.getString("last_name"))).trim();
                    records.add(new Attendance(
                            result.getInt("attendance_id"), employeeId, name,
                            date == null ? null : date.toLocalDate(),
                            checkIn == null ? null : checkIn.toLocalTime(),
                            checkOut == null ? null : checkOut.toLocalTime(),
                            result.getString("status"), result.getString("remarks")));
                }
            }
            return records;
        } catch (Exception e) {
            throw new RemoteException("Unable to retrieve employee attendance history.", e);
        }
    }

    @Override
    public void endEmployeeAttendanceSession(String attendanceSessionToken)
            throws RemoteException {
        requireAttendanceSession(attendanceSessionToken);
        attendanceSessions.remove(attendanceSessionToken);
    }

    private int requireAttendanceSession(String token) throws RemoteException {
        AttendanceSession session = token == null ? null : attendanceSessions.get(token);
        if (session == null || !session.expiresAt().isAfter(Instant.now())) {
            if (token != null) attendanceSessions.remove(token);
            throw new RemoteException("Attendance session expired. Sign in again.");
        }
        return session.employeeId();
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    // 1. MARK ATTENDANCE
    @Override
    public String markAttendance(Attendance attendance)
            throws RemoteException {

        if (attendance == null
                || attendance.getEmployeeId() <= 0
                || attendance.getAttendanceDate() == null
                || attendance.getStatus() == null) {

            return "Please provide valid attendance details.";
        }

        String status = attendance.getStatus().trim();

        if (!status.equals("Present")
                && !status.equals("Absent")
                && !status.equals("Late")
                && !status.equals("On Leave")) {

            return "Invalid attendance status.";
        }

        if (attendance.getCheckIn() != null
                && attendance.getCheckOut() != null
                && attendance.getCheckOut()
                        .isBefore(attendance.getCheckIn())) {

            return "Check-out cannot be before check-in.";
        }

        String sql = "INSERT INTO attendance "
                + "(employee_id, attendance_date, check_in, "
                + "check_out, status, remarks) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, attendance.getEmployeeId());

            statement.setDate(
                    2, Date.valueOf(attendance.getAttendanceDate()));

            if (attendance.getCheckIn() != null) {
                statement.setTime(
                        3, Time.valueOf(attendance.getCheckIn()));
            } else {
                statement.setNull(3, java.sql.Types.TIME);
            }

            if (attendance.getCheckOut() != null) {
                statement.setTime(
                        4, Time.valueOf(attendance.getCheckOut()));
            } else {
                statement.setNull(4, java.sql.Types.TIME);
            }

            statement.setString(5, status);

            statement.setString(
                    6, attendance.getRemarks() == null
                            ? "" : attendance.getRemarks().trim());

            int rows = statement.executeUpdate();

            return rows > 0
                    ? "Attendance marked successfully!"
                    : "Unable to mark attendance.";

        } catch (java.sql.SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                return "Attendance has already been marked "
                        + "for this employee on this date.";
            }

            if ("23503".equals(e.getSQLState())) {
                return "Employee ID does not exist.";
            }

            e.printStackTrace();

            return "Attendance marking failed: " + e.getMessage();

        } catch (Exception e) {

            e.printStackTrace();

            return "Attendance marking failed: " + e.getMessage();
        }
    }


    // 2. GET ALL ATTENDANCE RECORDS
    @Override
    public List<Attendance> getAllAttendance()
            throws RemoteException {

        List<Attendance> attendanceList = new ArrayList<>();

        String sql = "SELECT a.attendance_id, a.employee_id, "
                + "e.first_name, e.last_name, "
                + "a.attendance_date, a.check_in, a.check_out, "
                + "a.status, a.remarks "
                + "FROM attendance a "
                + "JOIN employees e "
                + "ON a.employee_id = e.employee_id "
                + "ORDER BY a.attendance_date DESC, a.attendance_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                Date attendanceDate =
                        result.getDate("attendance_date");

                Time checkInTime =
                        result.getTime("check_in");

                Time checkOutTime =
                        result.getTime("check_out");

                String employeeName =
                        result.getString("first_name") + " "
                        + result.getString("last_name");

                Attendance attendance = new Attendance(
                        result.getInt("attendance_id"),
                        result.getInt("employee_id"),
                        employeeName,
                        attendanceDate.toLocalDate(),
                        checkInTime != null
                                ? checkInTime.toLocalTime() : null,
                        checkOutTime != null
                                ? checkOutTime.toLocalTime() : null,
                        result.getString("status"),
                        result.getString("remarks")
                );

                attendanceList.add(attendance);
            }

            return attendanceList;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RemoteException(
                    "Unable to retrieve attendance records.", e);
        }
    }


    // 3. UPDATE ATTENDANCE
    @Override
    public String updateAttendance(Attendance attendance)
            throws RemoteException {

        if (attendance == null
                || attendance.getAttendanceId() <= 0
                || attendance.getEmployeeId() <= 0
                || attendance.getAttendanceDate() == null
                || attendance.getStatus() == null) {

            return "Valid attendance details are required.";
        }

        String status = attendance.getStatus().trim();

        if (!status.equals("Present")
                && !status.equals("Absent")
                && !status.equals("Late")
                && !status.equals("On Leave")) {

            return "Invalid attendance status.";
        }

        if (attendance.getCheckIn() != null
                && attendance.getCheckOut() != null
                && attendance.getCheckOut()
                        .isBefore(attendance.getCheckIn())) {

            return "Check-out cannot be before check-in.";
        }

        String sql = "UPDATE attendance "
                + "SET employee_id = ?, attendance_date = ?, "
                + "check_in = ?, check_out = ?, "
                + "status = ?, remarks = ? "
                + "WHERE attendance_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, attendance.getEmployeeId());

            statement.setDate(
                    2, Date.valueOf(attendance.getAttendanceDate()));

            if (attendance.getCheckIn() != null) {
                statement.setTime(
                        3, Time.valueOf(attendance.getCheckIn()));
            } else {
                statement.setNull(3, java.sql.Types.TIME);
            }

            if (attendance.getCheckOut() != null) {
                statement.setTime(
                        4, Time.valueOf(attendance.getCheckOut()));
            } else {
                statement.setNull(4, java.sql.Types.TIME);
            }

            statement.setString(5, status);

            statement.setString(
                    6, attendance.getRemarks() == null
                            ? "" : attendance.getRemarks().trim());

            statement.setInt(7, attendance.getAttendanceId());

            int rows = statement.executeUpdate();

            return rows > 0
                    ? "Attendance updated successfully!"
                    : "Attendance record not found.";

        } catch (java.sql.SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                return "An attendance record already exists "
                        + "for this employee on this date.";
            }

            if ("23503".equals(e.getSQLState())) {
                return "Employee ID does not exist.";
            }

            e.printStackTrace();

            return "Attendance update failed: " + e.getMessage();

        } catch (Exception e) {

            e.printStackTrace();

            return "Attendance update failed: " + e.getMessage();
        }
    }


    // 4. DELETE ATTENDANCE
    @Override
    public String deleteAttendance(int attendanceId)
            throws RemoteException {

        if (attendanceId <= 0) {
            return "Invalid attendance ID.";
        }

        String sql = "DELETE FROM attendance "
                + "WHERE attendance_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, attendanceId);

            int rows = statement.executeUpdate();

            return rows > 0
                    ? "Attendance record deleted successfully!"
                    : "Attendance record not found.";

        } catch (Exception e) {

            e.printStackTrace();

            return "Attendance deletion failed: " + e.getMessage();
        }
    }
}
