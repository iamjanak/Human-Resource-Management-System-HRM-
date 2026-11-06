
package com.bhel.client;

import com.bhel.remote.HRMService;
import com.bhel.model.Employee;

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
    
    // EMPLOYEE LOGIN - RETURN EMPLOYEE OBJECT
public Employee loginEmployee(
        String username,
        String password) {

    try {

        if (service == null) {
            throw new IllegalStateException(
                    "Server connection is unavailable.");
        }

        return service.loginEmployee(username, password);

    } catch (Exception e) {

        throw new RuntimeException(
                "Employee login failed: " + e.getMessage(), e);
    }
}
}
