package com.classroomscheduler.util;

import com.classroomscheduler.model.User;

public class SessionManager {
    private static User currentUser;

    public static void startSession(User user) {
        currentUser = user;
        System.out.println("SessionManager: Session started for user: " + user.getUsername() + " (Role: " + user.getRole() + ")");
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clearSession() {
        if (currentUser != null) {
            System.out.println("SessionManager: Session cleared for user: " + currentUser.getUsername());
        }
        currentUser = null;
    }

    public static boolean isAdmin() {
        return isLoggedIn() && "MAIN_ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean isFacilityManager() {
        return isLoggedIn() && "FACILITY_MANAGER".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean isLecturer() {
        return isLoggedIn() && "LECTURER".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean isStudentRep() {
        return isLoggedIn() && "STUDENT_REP".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean isStudent() {
        return isLoggedIn() && "STUDENT".equalsIgnoreCase(currentUser.getRole());
    }

    /** Returns true if current user can submit booking requests (Lecturer, Student Rep, or Admin). */
    public static boolean canRequestBooking() {
        return isAdmin() || isLecturer() || isStudentRep();
    }

    /** Returns true if current user can approve/reject bookings (Admin or Facility Manager). */
    public static boolean canApproveBooking() {
        return isAdmin() || isFacilityManager();
    }
}
