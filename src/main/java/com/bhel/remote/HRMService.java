
package com.bhel.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.bhel.model.Employee;
import com.bhel.model.FamilyMember;
import com.bhel.model.LeaveApplication;

public interface HRMService extends Remote {

    // USER LOGIN
    String login(
            String username,
            String password,
            String role)
            throws RemoteException;

    // EMPLOYEE REGISTRATION
    String registerEmployee(
            String firstName,
            String lastName,
            String icPassport,
            String username,
            String password)
            throws RemoteException;

    // RETRIEVE ALL EMPLOYEES
    List<Employee> getAllEmployees()
            throws RemoteException;

    // UPDATE EMPLOYEE DETAILS
    String updateEmployee(
            int employeeId,
            String firstName,
            String lastName,
            String icPassport)
            throws RemoteException;


    // ==============================
    // LEAVE MANAGEMENT
    // ==============================

    // APPLY FOR LEAVE
    String applyLeave(LeaveApplication leave)
            throws RemoteException;

    // RETRIEVE EMPLOYEE LEAVE APPLICATIONS
    List<LeaveApplication> getEmployeeLeaves(
            int employeeId)
            throws RemoteException;

    // RETRIEVE ALL LEAVE APPLICATIONS (ADMIN)
    List<LeaveApplication> getAllLeaveApplications()
            throws RemoteException;

    // APPROVE OR REJECT LEAVE (ADMIN)
    String reviewLeave(
            int leaveId,
            String status,
            String reviewedBy)
            throws RemoteException;

    // EMPLOYEE LOGIN - RETURNS EMPLOYEE DETAILS
        Employee loginEmployee(
        String username,
        String password)
        throws RemoteException;

}
