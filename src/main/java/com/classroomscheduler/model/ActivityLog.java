package com.classroomscheduler.model;

public class ActivityLog {
    private int logId;
    private int userId;
    private String action;
    private String details;
    private String timestamp;

    // UI helper
    private String userName;

    public ActivityLog() {}

    public ActivityLog(int userId, String action, String details) {
        this.userId = userId;
        this.action = action;
        this.details = details;
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
