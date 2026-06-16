package com.classroomscheduler.service;

import com.classroomscheduler.dao.ActivityLogDAO;
import com.classroomscheduler.model.ActivityLog;

import java.util.List;

public class ActivityLogService {
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public void log(int userId, String action, String details) {
        ActivityLog log = new ActivityLog(userId, action, details);
        activityLogDAO.logActivity(log);
    }

    public List<ActivityLog> getRecentActivity(int limit) {
        return activityLogDAO.getRecentActivity(limit);
    }

    public List<ActivityLog> getActivityByUser(int userId) {
        return activityLogDAO.getActivityByUser(userId);
    }
}
