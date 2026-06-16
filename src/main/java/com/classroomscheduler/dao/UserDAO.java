package com.classroomscheduler.dao;

import com.classroomscheduler.database.DatabaseConnection;
import com.classroomscheduler.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticate(String username, String passwordHash) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDAO: Error authenticating user: " + e.getMessage());
        }
        return null;
    }

    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDAO: Error getting user by ID: " + e.getMessage());
        }
        return null;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDAO: Error getting user by username: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("UserDAO: Error listing users: " + e.getMessage());
        }
        return users;
    }

    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDAO: Error getting users by role: " + e.getMessage());
        }
        return users;
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO users (name, username, password, role, programme, level, semester, staff_id, department, must_change_password) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getRole());
            pstmt.setString(5, user.getProgramme());
            pstmt.setString(6, user.getLevel());
            pstmt.setString(7, user.getSemester());
            pstmt.setString(8, user.getStaffId());
            pstmt.setString(9, user.getDepartment());
            pstmt.setInt(10, user.isMustChangePassword() ? 1 : 0);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("UserDAO: Error adding user: " + e.getMessage());
        }
        return false;
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name = ?, username = ?, role = ?, programme = ?, level = ?, semester = ?, staff_id = ?, department = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getProgramme());
            pstmt.setString(5, user.getLevel());
            pstmt.setString(6, user.getSemester());
            pstmt.setString(7, user.getStaffId());
            pstmt.setString(8, user.getDepartment());
            pstmt.setInt(9, user.getUserId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDAO: Error updating user: " + e.getMessage());
        }
        return false;
    }

    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password = ?, must_change_password = 0 WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPasswordHash);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDAO: Error updating password: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDAO: Error deleting user: " + e.getMessage());
        }
        return false;
    }

    // User-Course assignments
    public List<String> getUserCourses(int userId) {
        List<String> courses = new ArrayList<>();
        String sql = "SELECT course_code FROM user_courses WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(rs.getString("course_code"));
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDAO: Error getting user courses: " + e.getMessage());
        }
        return courses;
    }

    public boolean setUserCourses(int userId, List<String> courseCodes) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Clear existing
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM user_courses WHERE user_id = ?")) {
                del.setInt(1, userId);
                del.executeUpdate();
            }
            // Insert new
            String sql = "INSERT INTO user_courses (user_id, course_code) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (String code : courseCodes) {
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, code);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("UserDAO: Error setting user courses: " + e.getMessage());
        }
        return false;
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setProgramme(rs.getString("programme"));
        user.setLevel(rs.getString("level"));
        user.setSemester(rs.getString("semester"));
        user.setStaffId(rs.getString("staff_id"));
        user.setDepartment(rs.getString("department"));
        user.setMustChangePassword(rs.getInt("must_change_password") == 1);
        return user;
    }
}
