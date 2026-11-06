package com.bhel.model;

import java.io.Serializable;

public class FamilyMember implements Serializable {

    private static final long serialVersionUID = 1L;

    private int familyId;
    private int employeeId;
    private String name;
    private String relationship;
    private String contactNumber;
    private String dateOfBirth;

    public FamilyMember(int familyId, int employeeId,
                        String name, String relationship,
                        String contactNumber, String dateOfBirth) {
        this.familyId = familyId;
        this.employeeId = employeeId;
        this.name = name;
        this.relationship = relationship;
        this.contactNumber = contactNumber;
        this.dateOfBirth = dateOfBirth;
    }

    public int getFamilyId() {
        return familyId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getName() {
        return name;
    }

    public String getRelationship() {
        return relationship;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }
}