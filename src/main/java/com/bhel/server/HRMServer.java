
package com.bhel.server;

import com.bhel.database.DBConnection;
import com.bhel.remote.HRMService;
import com.bhel.security.PasswordUtil;

import com.bhel.model.FamilyMember;
import com.bhel.model.LeaveApplication;
import java.sql.Date;
import java.time.LocalDate;
import com.bhel.model.Employee;
import java.util.ArrayList;
import java.util.List;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CountDownLatch;

public class HRMServer extends UnicastRemoteObject implements HRMService {

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

    String sql = "SELECT e.employee_id, e.first_name, "
            + "e.last_name, e.ic_passport, u.username, "
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

                    return new Employee(
                            result.getInt("employee_id"),
                            result.getString("first_name"),
                            result.getString("last_name"),
                            result.getString("ic_passport"),
                            result.getString("username")
                    );
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
    // REGISTER EMPLOYEE
    @Override
    public String registerEmployee(
            String firstName,
            String lastName,
            String icPassport,
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
                + "(first_name, last_name, ic_passport, user_id) "
                + "VALUES (?, ?, ?, ?)";

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
                    statement.setInt(4, userId);

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
    
    // GET ALL EMPLOYEES
    @Override
    public List<Employee> getAllEmployees() throws RemoteException {

        List<Employee> employees = new ArrayList<>();

        String sql = "SELECT e.employee_id, e.first_name, "
                + "e.last_name, e.ic_passport, u.username "
                + "FROM employees e "
                + "JOIN users u ON e.user_id = u.user_id "
                + "ORDER BY e.employee_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                Employee employee = new Employee(
                        result.getInt("employee_id"),
                        result.getString("first_name"),
                        result.getString("last_name"),
                        result.getString("ic_passport"),
                        result.getString("username")
                );

                employees.add(employee);
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
            String icPassport) throws RemoteException {

        String checkSql =
                "SELECT employee_id FROM employees "
                + "WHERE ic_passport = ? AND employee_id <> ?";

        String updateSql =
                "UPDATE employees "
                + "SET first_name = ?, last_name = ?, ic_passport = ? "
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
                statement.setInt(4, employeeId);

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

        String sql = "SELECT * FROM leave_applications "
                + "ORDER BY applied_date DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

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

        String sql = "UPDATE leave_applications "
                + "SET status = ?, reviewed_by = ?, "
                + "reviewed_date = CURRENT_TIMESTAMP "
                + "WHERE leave_id = ? AND status = 'Pending'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setString(2, reviewedBy);
            statement.setInt(3, leaveId);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                return "Leave application " + status.toLowerCase()
                        + " successfully!";
            }

            return "Leave application not found or already reviewed.";

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

        String sql = "SELECT e.employee_id, e.first_name, "
                + "e.last_name, e.ic_passport, u.username "
                + "FROM employees e "
                + "JOIN users u ON e.user_id = u.user_id "
                + "WHERE e.employee_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new Employee(
                            result.getInt("employee_id"),
                            result.getString("first_name"),
                            result.getString("last_name"),
                            result.getString("ic_passport"),
                            result.getString("username")
                    );
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

}
