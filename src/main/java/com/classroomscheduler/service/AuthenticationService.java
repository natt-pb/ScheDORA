package com.classroomscheduler.service;

import com.classroomscheduler.dao.UserDAO;
import com.classroomscheduler.model.User;
import com.classroomscheduler.util.SessionManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class AuthenticationService {
    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) {
        String hash = hashPassword(password);
        User user = userDAO.authenticate(username, hash);
        if (user != null) {
            SessionManager.startSession(user);
        }
        return user;
    }

    public boolean register(String name, String username, String password, String role) {
        if (userDAO.getUserByUsername(username) != null) {
            return false; // User already exists
        }
        String hash = hashPassword(password);
        User user = new User(0, name, username, hash, role);
        return userDAO.addUser(user);
    }

    public boolean updatePassword(int userId, String newPassword) {
        String hash = hashPassword(newPassword);
        return userDAO.updatePassword(userId, hash);
    }

    public void logout() {
        SessionManager.clearSession();
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error hashing password", ex);
        }
    }
}
