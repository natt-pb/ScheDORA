package com.classroomscheduler.dao;

import com.classroomscheduler.database.DatabaseConnection;
import com.classroomscheduler.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {

    public boolean logActivity(ActivityLog log) {
        String sql = "INSERT INTO activity_log (user_id, action, details) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, log.getUserId());
            pstmt.setString(2, log.getAction());
            pstmt.setString(3, log.getDetails());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ActivityLogDAO: Error logging activity: " + e.getMessage());
        }
        return false;
    }

    public List<ActivityLog> getRecentActivity(int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT a.*, u.name as user_name FROM activity_log a " +
                     "LEFT JOIN users u ON a.user_id = u.user_id " +
                     "ORDER BY a.timestamp DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(extractLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ActivityLogDAO: Error getting recent activity: " + e.getMessage());
        }
        return logs;
    }

    public List<ActivityLog> getActivityByUser(int userId) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT a.*, u.name as user_name FROM activity_log a " +
                     "LEFT JOIN users u ON a.user_id = u.user_id " +
                     "WHERE a.user_id = ? ORDER BY a.timestamp DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(extractLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ActivityLogDAO: Error getting user activity: " + e.getMessage());
        }
        return logs;
    }

    private ActivityLog extractLog(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setLogId(rs.getInt("log_id"));
        log.setUserId(rs.getInt("user_id"));
        log.setAction(rs.getString("action"));
        log.setDetails(rs.getString("details"));
        log.setTimestamp(rs.getString("timestamp"));
        log.setUserName(rs.getString("user_name"));
        return log;
    }
}
