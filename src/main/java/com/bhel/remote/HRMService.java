
package com.bhel.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.time.LocalDate;

import com.bhel.model.Employee;
import com.bhel.model.FamilyMember;
import com.bhel.model.LeaveApplication;
import com.bhel.model.Department;
import com.bhel.model.Attendance;
import com.bhel.model.Payslip;
import com.bhel.model.TimeRequest;
import java.math.BigDecimal;
import java.time.YearMonth;

public interface HRMService extends Remote {

    // ==========================================
    // LOGIN
    // ==========================================

    String login(String username, String password, String role)
            throws RemoteException;

    Employee loginEmployee(String username, String password)
            throws RemoteException;

    String changePassword(String username, String currentPassword,
                          String newPassword) throws RemoteException;

    String adminResetEmployeePassword(String adminUsername,
                                      String adminPassword,
                                      String employeeUsername,
                                      String newPassword)
            throws RemoteException;


    // ==========================================
    // EMPLOYEE MANAGEMENT
    // ==========================================

    String registerEmployee(
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
            String password
    ) throws RemoteException;

    List<Employee> getAllEmployees() throws RemoteException;

    Employee getEmployeeById(int employeeId) throws RemoteException;

    String updateEmployee(
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
            String employmentStatus
    ) throws RemoteException;


    // ==========================================
    // LEAVE MANAGEMENT
    // ==========================================

    String applyLeave(LeaveApplication leave)
            throws RemoteException;

    List<LeaveApplication> getEmployeeLeaves(int employeeId)
            throws RemoteException;

    List<LeaveApplication> getAllLeaveApplications()
            throws RemoteException;

    String reviewLeave(
            int leaveId,
            String status,
            String reviewedBy
    ) throws RemoteException;


    // ==========================================
    // FAMILY DETAILS MANAGEMENT
    // ==========================================

    String addFamilyMember(FamilyMember familyMember)
            throws RemoteException;

    List<FamilyMember> getFamilyMembers(int employeeId)
            throws RemoteException;

    String updateFamilyMember(FamilyMember familyMember)
            throws RemoteException;

    String deleteFamilyMember(int familyId)
            throws RemoteException;


    // ==========================================
    // DEPARTMENT MANAGEMENT
    // ==========================================

    String addDepartment(Department department)
            throws RemoteException;

    List<Department> getAllDepartments()
            throws RemoteException;

    String updateDepartment(Department department)
            throws RemoteException;

    String deleteDepartment(int departmentId)
            throws RemoteException;


    // ==========================================
    // ATTENDANCE MANAGEMENT
    // ==========================================

    String markAttendance(Attendance attendance)
            throws RemoteException;

    String submitEmployeeAttendance(String attendanceSessionToken,
                                    java.time.LocalTime checkIn,
                                    java.time.LocalTime checkOut,
                                    String remarks)
            throws RemoteException;

    List<Attendance> getEmployeeAttendance(String attendanceSessionToken)
            throws RemoteException;

    void endEmployeeAttendanceSession(String attendanceSessionToken)
            throws RemoteException;

    List<Attendance> getAllAttendance()
            throws RemoteException;

    String updateAttendance(Attendance attendance)
            throws RemoteException;

    String deleteAttendance(int attendanceId)
            throws RemoteException;

    String setMonthlySalary(int employeeId, BigDecimal monthlySalary)
            throws RemoteException;

    BigDecimal getMonthlySalary(int employeeId) throws RemoteException;

    Payslip generatePayslip(int employeeId, YearMonth payPeriod)
            throws RemoteException;

    String updatePayslip(int employeeId, YearMonth payPeriod,
                         BigDecimal grossPay, BigDecimal deductions)
            throws RemoteException;

    List<Payslip> getEmployeePayslips(String attendanceSessionToken) throws RemoteException;

    List<Payslip> getAllPayslips() throws RemoteException;

    String submitTimeRequest(String attendanceSessionToken, String requestType,
                             LocalDate requestDate,
                             java.time.LocalTime startTime,
                             java.time.LocalTime endTime, String reason)
            throws RemoteException;

    List<TimeRequest> getEmployeeTimeRequests(String attendanceSessionToken)
            throws RemoteException;

    List<TimeRequest> getAllTimeRequests() throws RemoteException;

    String reviewTimeRequest(int requestId, String status, String reviewedBy)
            throws RemoteException;

}
