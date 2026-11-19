
package com.bhel.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private int employeeId;
    private String firstName;
    private String lastName;
    private String icPassport;
    private String username;
    private int userId;
    private LocalDateTime createdAt;
    private String gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String email;
    private String address;
    private String department;
    private String designation;
    private LocalDate joiningDate;
    private String employmentStatus;
    private String attendanceSessionToken;

    public Employee(int employeeId, String firstName,
                    String lastName, String icPassport,
                    String username) {

        this(employeeId, 0, firstName, lastName, icPassport, username,
                null, null, null, null, null, null, null, null, null,
                null);
    }

    public Employee(int employeeId, int userId, String firstName,
                    String lastName, String icPassport, String username,
                    LocalDateTime createdAt, String gender,
                    LocalDate dateOfBirth, String phone, String email,
                    String address, String department, String designation,
                    LocalDate joiningDate, String employmentStatus) {

        this.employeeId = employeeId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.icPassport = icPassport;
        this.username = username;
        this.createdAt = createdAt;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.department = department;
        this.designation = designation;
        this.joiningDate = joiningDate;
        this.employmentStatus = employmentStatus;
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

    public int getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getGender() { return gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getDepartment() { return department; }
    public String getDesignation() { return designation; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public String getEmploymentStatus() { return employmentStatus; }
    public String getAttendanceSessionToken() { return attendanceSessionToken; }
    public void setAttendanceSessionToken(String token) { this.attendanceSessionToken = token; }
}
