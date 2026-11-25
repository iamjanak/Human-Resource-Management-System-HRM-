package com.bhel.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/** A saved monthly payroll statement. */
public class Payslip implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int employeeId;
    private final String employeeName;
    private final String department;
    private final String designation;
    private final String icPassport;
    private final String gender;
    private final java.time.LocalDate dateOfBirth;
    private final String phone;
    private final String email;
    private final String address;
    private final java.time.LocalDate joiningDate;
    private final String employmentStatus;
    private final YearMonth payPeriod;
    private final BigDecimal grossPay;
    private final BigDecimal deductions;
    private final BigDecimal netPay;
    private final LocalDate generatedOn;

    public Payslip(int employeeId, String employeeName, String department,
                   String designation, String icPassport, String gender,
                   java.time.LocalDate dateOfBirth, String phone,
                   String email, String address, java.time.LocalDate joiningDate,
                   String employmentStatus, YearMonth payPeriod, BigDecimal grossPay,
                   BigDecimal deductions, BigDecimal netPay, LocalDate generatedOn) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.designation = designation;
        this.icPassport = icPassport;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.joiningDate = joiningDate;
        this.employmentStatus = employmentStatus;
        this.payPeriod = payPeriod;
        this.grossPay = grossPay;
        this.deductions = deductions;
        this.netPay = netPay;
        this.generatedOn = generatedOn;
    }

    public int getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getDepartment() { return department; }
    public String getDesignation() { return designation; }
    public String getIcPassport() { return icPassport; }
    public String getGender() { return gender; }
    public java.time.LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public java.time.LocalDate getJoiningDate() { return joiningDate; }
    public String getEmploymentStatus() { return employmentStatus; }
    public YearMonth getPayPeriod() { return payPeriod; }
    public BigDecimal getGrossPay() { return grossPay; }
    public BigDecimal getDeductions() { return deductions; }
    public BigDecimal getNetPay() { return netPay; }
    public LocalDate getGeneratedOn() { return generatedOn; }
}
