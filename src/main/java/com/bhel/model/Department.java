
package com.bhel.model;

import java.io.Serializable;

public class Department implements Serializable {

    private static final long serialVersionUID = 1L;

    private int departmentId;
    private String departmentName;
    private String description;

    public Department(int departmentId, String departmentName,
                      String description) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.description = description;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
