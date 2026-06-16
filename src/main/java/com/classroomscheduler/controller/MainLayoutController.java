package com.classroomscheduler.controller;

import com.classroomscheduler.Main;
import com.classroomscheduler.dao.UserPreferencesDAO;
import com.classroomscheduler.model.User;
import com.classroomscheduler.service.NotificationService;
import com.classroomscheduler.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class MainLayoutController implements Initializable {

    public static MainLayoutController instance;

    @FXML private BorderPane mainBorderPane;
    @FXML private StackPane contentArea;

    // Sidebar navigation buttons
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavSchedule;
    @FXML private Button btnNavSearch;
    @FXML private Button btnNavBooking;
    @FXML private Button btnNavReservations;
    @FXML private Button btnNavApproval;
    @FXML private Button btnNavRoom;
    @FXML private Button btnNavUsers;
    @FXML private Button btnNavNotifications;
    @FXML private Button btnNavReports;
    @FXML private Button btnNavBlocks;
    @FXML private Button btnNavSettings;

    // Top status controls
    @FXML private Label topUserInitialLabel;
    @FXML private Label topUsernameLabel;
    @FXML private Label topUserRoleLabel;
    @FXML private Label lblNotifBadge;

    private final NotificationService notificationService = new NotificationService();
    private final UserPreferencesDAO prefsDAO = new UserPreferencesDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        setupSessionInfo();
        setupRoleAccess();
        applyDefaultTheme();
        updateNotificationBadge();

        // Load default starting view
        loadView("dashboard.fxml", btnNavDashboard);
    }

    public BorderPane getMainBorderPane() {
        return mainBorderPane;
    }

    private void setupSessionInfo() {
        if (SessionManager.isLoggedIn()) {
            User current = SessionManager.getCurrentUser();
            topUsernameLabel.setText(current.getName());
            topUserRoleLabel.setText(formatRole(current.getRole()));
            if (current.getName() != null && !current.getName().isEmpty()) {
                topUserInitialLabel.setText(current.getName().substring(0, 1).toUpperCase());
            }
        }
    }

    /** Called by SettingsController after name change */
    public void refreshSessionInfo() {
        setupSessionInfo();
    }

    private String formatRole(String role) {
        if (role == null) return "";
        switch (role) {
            case "MAIN_ADMIN": return "Main Admin";
            case "FACILITY_MANAGER": return "Facility Manager";
            case "LECTURER": return "Lecturer";
            case "STUDENT_REP": return "Student Rep";
            case "STUDENT": return "Student";
            default: return role;
        }
    }

    private void setupRoleAccess() {
        boolean isAdmin = SessionManager.isAdmin();
        boolean isFM = SessionManager.isFacilityManager();
        boolean isLecturer = SessionManager.isLecturer();
        boolean isRep = SessionManager.isStudentRep();
        boolean isStudent = SessionManager.isStudent();

        // Approvals: Admin and Facility Manager
        setNavVisible(btnNavApproval, isAdmin || isFM);

        // Room Management: Admin and Facility Manager
        setNavVisible(btnNavRoom, isAdmin || isFM);

        // User Management: Admin only
        setNavVisible(btnNavUsers, isAdmin);

        // Block Management: Admin only
        setNavVisible(btnNavBlocks, isAdmin);

        // Reports: Admin only
        setNavVisible(btnNavReports, isAdmin);

        // Booking: Admin, Lecturer, Student Rep
        boolean canBook = isAdmin || isLecturer || isRep;
        setNavVisible(btnNavBooking, canBook);

        // Reservations: Admin, Lecturer, Student Rep, Facility Manager
        setNavVisible(btnNavReservations, isAdmin || isLecturer || isRep || isFM);

        // Search/Availability: All except Student (view-only)
        setNavVisible(btnNavSearch, !isStudent);

        // Timetable: All roles
        setNavVisible(btnNavSchedule, true);

        // Notifications: All roles
        setNavVisible(btnNavNotifications, true);

        // Settings: All roles
        setNavVisible(btnNavSettings, true);

        // Dashboard: All roles
        setNavVisible(btnNavDashboard, true);
    }

    private void setNavVisible(Button btn, boolean visible) {
        if (btn != null) {
            btn.setVisible(visible);
            btn.setManaged(visible);
        }
    }

    public void updateNotificationBadge() {
        if (lblNotifBadge != null && SessionManager.isLoggedIn()) {
            int count = notificationService.getUnreadCount(SessionManager.getCurrentUser().getUserId());
            if (count > 0) {
                lblNotifBadge.setText(String.valueOf(count));
                lblNotifBadge.setVisible(true);
                lblNotifBadge.setManaged(true);
            } else {
                lblNotifBadge.setVisible(false);
                lblNotifBadge.setManaged(false);
            }
        }
    }

    private void applyDefaultTheme() {
        if (mainBorderPane == null) return;

        // Load saved preferences from database
        if (SessionManager.isLoggedIn()) {
            int userId = SessionManager.getCurrentUser().getUserId();
            Map<String, String> prefs = prefsDAO.getPreferences(userId);

            String theme = prefs.getOrDefault("theme", "dark");
            String accent = prefs.getOrDefault("accent_color", "indigo");

            mainBorderPane.getStyleClass().removeAll("theme-dark", "theme-light");
            mainBorderPane.getStyleClass().add("theme-" + theme);

            mainBorderPane.getStyleClass().removeAll("accent-indigo", "accent-teal", "accent-amber", "accent-rose");
            mainBorderPane.getStyleClass().add("accent-" + accent);
        } else {
            mainBorderPane.getStyleClass().add("theme-dark");
            mainBorderPane.getStyleClass().add("accent-indigo");
        }
    }

    private void loadView(String fxmlFile, Button activeBtn) {
        try {
            System.out.println("MainLayoutController: Swapping content view to: " + fxmlFile);
            URL url = Main.class.getResource("/fxml/" + fxmlFile);
            if (url == null) {
                System.err.println("MainLayoutController: Cannot find FXML file: " + fxmlFile);
                return;
            }
            Parent view = FXMLLoader.load(url);
            contentArea.getChildren().setAll(view);

            highlightNavButton(activeBtn);
            updateNotificationBadge();
        } catch (IOException e) {
            System.err.println("MainLayoutController: Failed to load view: " + fxmlFile);
            e.printStackTrace();
        }
    }

    private void highlightNavButton(Button activeBtn) {
        Button[] navButtons = {
            btnNavDashboard, btnNavSchedule, btnNavSearch, btnNavBooking,
            btnNavReservations, btnNavApproval, btnNavRoom, btnNavUsers,
            btnNavNotifications, btnNavReports, btnNavBlocks, btnNavSettings
        };
        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("nav-item-active");
                btn.getStyleClass().remove("nav-item");
                if (btn == activeBtn) {
                    btn.getStyleClass().add("nav-item-active");
                } else {
                    btn.getStyleClass().add("nav-item");
                }
            }
        }
    }

    // Navigation action handlers
    @FXML private void handleNavDashboard() { loadView("dashboard.fxml", btnNavDashboard); }
    @FXML private void handleNavSchedule() { loadView("timetable.fxml", btnNavSchedule); }
    @FXML private void handleNavSearch() { loadView("availability.fxml", btnNavSearch); }
    @FXML public void handleNavBooking() { loadView("booking.fxml", btnNavBooking); }
    @FXML private void handleNavReservations() { loadView("reservations.fxml", btnNavReservations); }
    @FXML private void handleNavApproval() { loadView("approval.fxml", btnNavApproval); }
    @FXML private void handleNavRoom() { loadView("room_management.fxml", btnNavRoom); }
    @FXML private void handleNavUsers() { loadView("users.fxml", btnNavUsers); }
    @FXML private void handleNavNotifications() { loadView("notifications.fxml", btnNavNotifications); }
    @FXML private void handleNavReports() { loadView("reports.fxml", btnNavReports); }
    @FXML private void handleNavBlocks() { loadView("block_management.fxml", btnNavBlocks); }
    @FXML private void handleNavSettings() { loadView("settings.fxml", btnNavSettings); }

    @FXML
    private void handleLogout() {
        System.out.println("MainLayoutController: Triggering logout...");
        SessionManager.clearSession();
        Main.setRoot("login.fxml");
    }
}
