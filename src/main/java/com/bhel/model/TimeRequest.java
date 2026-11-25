package com.bhel.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/** Employee request for a time-related schedule or attendance action. */
public class TimeRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int requestId;
    private final int employeeId;
    private final String employeeName;
    private final String requestType;
    private final LocalDate requestDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String reason;
    private final String status;
    private final String reviewedBy;
    private final LocalDate reviewedOn;

    public TimeRequest(int requestId, int employeeId, String employeeName,
                       String requestType, LocalDate requestDate,
                       LocalTime startTime, LocalTime endTime, String reason,
                       String status, String reviewedBy, LocalDate reviewedOn) {
        this.requestId = requestId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.requestType = requestType;
        this.requestDate = requestDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
        this.status = status;
        this.reviewedBy = reviewedBy;
        this.reviewedOn = reviewedOn;
    }

    public int getRequestId() { return requestId; }
    public int getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getRequestType() { return requestType; }
    public LocalDate getRequestDate() { return requestDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public String getReviewedBy() { return reviewedBy; }
    public LocalDate getReviewedOn() { return reviewedOn; }
}
