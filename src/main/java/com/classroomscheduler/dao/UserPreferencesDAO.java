package com.classroomscheduler.dao;

import com.classroomscheduler.database.DatabaseConnection;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * DAO for user_preferences table.
 * Stores theme, accent color, and display settings per user.
 */
public class UserPreferencesDAO {

    /**
     * Get all preferences for a user. Returns a map with keys: theme, accent_color, font_size.
     * Returns defaults if no record exists.
     */
    public Map<String, String> getPreferences(int userId) {
        Map<String, String> prefs = new HashMap<>();
        prefs.put("theme", "dark");
        prefs.put("accent_color", "indigo");
        prefs.put("font_size", "Medium");

        String sql = "SELECT theme, accent_color, font_size FROM user_preferences WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    prefs.put("theme", rs.getString("theme") != null ? rs.getString("theme") : "dark");
                    prefs.put("accent_color", rs.getString("accent_color") != null ? rs.getString("accent_color") : "indigo");
                    prefs.put("font_size", rs.getString("font_size") != null ? rs.getString("font_size") : "Medium");
                }
            }
        } catch (SQLException e) {
            System.err.println("UserPreferencesDAO: Error reading preferences: " + e.getMessage());
        }
        return prefs;
    }

    /**
     * Save or update a single preference key for a user.
     */
    public boolean savePreference(int userId, String key, String value) {
        // Upsert: insert or replace
        String checkSql = "SELECT COUNT(*) FROM user_preferences WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean exists;
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    exists = rs.next() && rs.getInt(1) > 0;
                }
            }

            if (!exists) {
                String insertSql = "INSERT INTO user_preferences (user_id, theme, accent_color, font_size) VALUES (?, 'dark', 'indigo', 'Medium')";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setInt(1, userId);
                    pstmt.executeUpdate();
                }
            }

            String updateSql = "UPDATE user_preferences SET " + key + " = ? WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, value);
                pstmt.setInt(2, userId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("UserPreferencesDAO: Error saving preference: " + e.getMessage());
        }
        return false;
    }
}
