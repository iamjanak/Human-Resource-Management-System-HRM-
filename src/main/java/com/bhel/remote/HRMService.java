
package com.bhel.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.bhel.model.Employee;
import com.bhel.model.FamilyMember;
import com.bhel.model.LeaveApplication;

public interface HRMService extends Remote {

    // Login
    String login(String username, String password, String role)
            throws RemoteException;

    Employee loginEmployee(String username, String password)
            throws RemoteException;

    // Employee Management
    String registerEmployee(String firstName, String lastName,
                            String icPassport, String username,
                            String password) throws RemoteException;

    List<Employee> getAllEmployees() throws RemoteException;

    Employee getEmployeeById(int employeeId) throws RemoteException;

    String updateEmployee(int employeeId, String firstName,
                          String lastName, String icPassport)
            throws RemoteException;

    // Leave Management
    String applyLeave(LeaveApplication leave) throws RemoteException;

    List<LeaveApplication> getEmployeeLeaves(int employeeId)
            throws RemoteException;

    List<LeaveApplication> getAllLeaveApplications()
            throws RemoteException;

    String reviewLeave(int leaveId, String status, String reviewedBy)
            throws RemoteException;

    // Family Details Management
    String addFamilyMember(FamilyMember familyMember)
            throws RemoteException;

    List<FamilyMember> getFamilyMembers(int employeeId)
            throws RemoteException;

    String updateFamilyMember(FamilyMember familyMember)
            throws RemoteException;

    String deleteFamilyMember(int familyId)
            throws RemoteException;
}
