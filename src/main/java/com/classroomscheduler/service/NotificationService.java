package com.classroomscheduler.service;

import com.classroomscheduler.dao.NotificationDAO;
import com.classroomscheduler.model.Notification;

import java.util.List;

public class NotificationService {
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public void notifyUser(int userId, String title, String message, String type) {
        Notification n = new Notification(userId, title, message, type);
        notificationDAO.addNotification(n);
    }

    public List<Notification> getUserNotifications(int userId) {
        return notificationDAO.getNotificationsByUser(userId);
    }

    public int getUnreadCount(int userId) {
        return notificationDAO.getUnreadCount(userId);
    }

    public void markAsRead(int notificationId) {
        notificationDAO.markAsRead(notificationId);
    }

    public void markAllAsRead(int userId) {
        notificationDAO.markAllAsRead(userId);
    }

    public void broadcastToRole(String role, String title, String message) {
        notificationDAO.broadcastToRole(role, title, message);
    }

    public void broadcastToAll(String title, String message) {
        notificationDAO.broadcastToAll(title, message);
    }
}
