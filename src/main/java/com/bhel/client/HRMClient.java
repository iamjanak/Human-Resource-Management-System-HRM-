
package com.bhel.client;

import com.bhel.remote.HRMService;
import com.bhel.model.Employee;
import com.bhel.model.FamilyMember;

import java.rmi.Naming;
import java.util.List;

public class HRMClient {

    private HRMService service;

    public HRMClient() {

        try {
            service = (HRMService) Naming.lookup(
                    "rmi://localhost:1099/HRMService"
            );

            System.out.println("Connected to BHEL HRM Server.");

        } catch (Exception e) {
            System.out.println("Unable to connect to HRM Server.");
            e.printStackTrace();
        }
    }

    // Employee and Admin Login
    public String login(String username, String password, String role) {

        try {
            if (service == null) {
                return "Server connection is unavailable.";
            }

            return service.login(username, password, role);

        } catch (Exception e) {
            return "Login request failed: " + e.getMessage();
        }
    }

    // Register Employee
    public String registerEmployee(
            String firstName,
            String lastName,
            String icPassport,
            String username,
            String password) {

        try {
            if (service == null) {
                return "Server connection is unavailable.";
            }

            return service.registerEmployee(
                    firstName,
                    lastName,
                    icPassport,
                    username,
                    password
            );

        } catch (Exception e) {
            return "Employee registration failed: " + e.getMessage();
        }
    }

    // Get All Employees
    public List<Employee> getAllEmployees() {

        try {
            if (service == null) {
                throw new IllegalStateException(
                        "Server connection is unavailable."
                );
            }

            return service.getAllEmployees();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to retrieve employee records: "
                    + e.getMessage(), e
            );
        }
    }

    // Employee Login - Return Employee Object
    public Employee loginEmployee(
            String username,
            String password) {

        try {
            if (service == null) {
                throw new IllegalStateException(
                        "Server connection is unavailable."
                );
            }

            return service.loginEmployee(username, password);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Employee login failed: " + e.getMessage(), e
            );
        }
    }

    // ==========================================
    // PAGE 6 - EMPLOYEE PROFILE & FAMILY DETAILS
    // ==========================================

    // 1. Get Employee Profile
    public Employee getEmployeeById(int employeeId) {

        try {
            if (service == null) {
                throw new IllegalStateException(
                        "Server connection is unavailable."
                );
            }

            return service.getEmployeeById(employeeId);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to retrieve employee profile: "
                    + e.getMessage(), e
            );
        }
    }

    // 2. Update Employee Profile
    public String updateEmployee(
            int employeeId,
            String firstName,
            String lastName,
            String icPassport) {

        try {
            if (service == null) {
                return "Server connection is unavailable.";
            }

            return service.updateEmployee(
                    employeeId,
                    firstName,
                    lastName,
                    icPassport
            );

        } catch (Exception e) {
            return "Unable to update employee profile: "
                    + e.getMessage();
        }
    }

    // 3. Add Family Member
    public String addFamilyMember(FamilyMember familyMember) {

        try {
            if (service == null) {
                return "Server connection is unavailable.";
            }

            return service.addFamilyMember(familyMember);

        } catch (Exception e) {
            return "Unable to add family member: "
                    + e.getMessage();
        }
    }

    // 4. Get Family Members
    public List<FamilyMember> getFamilyMembers(int employeeId) {

        try {
            if (service == null) {
                throw new IllegalStateException(
                        "Server connection is unavailable."
                );
            }

            return service.getFamilyMembers(employeeId);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to retrieve family details: "
                    + e.getMessage(), e
            );
        }
    }

    // 5. Update Family Member
    public String updateFamilyMember(FamilyMember familyMember) {

        try {
            if (service == null) {
                return "Server connection is unavailable.";
            }

            return service.updateFamilyMember(familyMember);

        } catch (Exception e) {
            return "Unable to update family member: "
                    + e.getMessage();
        }
    }

    // 6. Delete Family Member
    public String deleteFamilyMember(int familyId) {

        try {
            if (service == null) {
                return "Server connection is unavailable.";
            }

            return service.deleteFamilyMember(familyId);

        } catch (Exception e) {
            return "Unable to delete family member: "
                    + e.getMessage();
        }
    }
}
