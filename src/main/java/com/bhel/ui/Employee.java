
package com.bhel.model;

import java.io.Serializable;

public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private int employeeId;
    private String firstName;
    private String lastName;
    private String icPassport;
    private String username;

    public Employee(int employeeId, String firstName,
                    String lastName, String icPassport,
                    String username) {

        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.icPassport = icPassport;
        this.username = username;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getIcPassport() {
        return icPassport;
    }

    public String getUsername() {
        return username;
    }
}
