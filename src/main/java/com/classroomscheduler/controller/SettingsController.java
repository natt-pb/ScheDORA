package com.classroomscheduler.controller;

import com.classroomscheduler.dao.UserDAO;
import com.classroomscheduler.dao.UserPreferencesDAO;
import com.classroomscheduler.model.User;
import com.classroomscheduler.service.AuthenticationService;
import com.classroomscheduler.util.AlertUtil;
import com.classroomscheduler.util.SessionManager;
import com.classroomscheduler.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    // Theme
    @FXML private VBox themeDarkOption;
    @FXML private VBox themeLightOption;

    // Accent swatches + rings
    @FXML private javafx.scene.layout.StackPane accentIndigo;
    @FXML private javafx.scene.layout.StackPane accentTeal;
    @FXML private javafx.scene.layout.StackPane accentAmber;
    @FXML private javafx.scene.layout.StackPane accentRose;
    @FXML private Circle accentIndigoRing;
    @FXML private Circle accentTealRing;
    @FXML private Circle accentAmberRing;
    @FXML private Circle accentRoseRing;

    // Display
    @FXML private ComboBox<String> cmbFontSize;
    @FXML private CheckBox chkCollapsedSidebar;
    @FXML private CheckBox chkCompactTables;

    // Account
    @FXML private TextField txtDisplayName;
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;

    // Notifications
    @FXML private CheckBox chkNotifApproved;
    @FXML private CheckBox chkNotifRejected;
    @FXML private CheckBox chkNotifAnnouncement;
    @FXML private CheckBox chkNotifTimetable;

    private final UserDAO userDAO = new UserDAO();
    private final AuthenticationService authService = new AuthenticationService();
    private final UserPreferencesDAO prefsDAO = new UserPreferencesDAO();

    private String currentTheme = "dark";
    private String currentAccent = "indigo";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDisplayPreferences();
        setupAccountInfo();
        setupThemeClicks();
        setupAccentClicks();
        loadSavedPreferences();
    }

    private void loadSavedPreferences() {
        if (!SessionManager.isLoggedIn()) return;

        int userId = SessionManager.getCurrentUser().getUserId();
        Map<String, String> prefs = prefsDAO.getPreferences(userId);

        // Apply saved theme
        String savedTheme = prefs.get("theme");
        if (savedTheme != null) {
            selectTheme(savedTheme, false);
        }

        // Apply saved accent
        String savedAccent = prefs.get("accent_color");
        if (savedAccent != null) {
            selectAccent(savedAccent, false);
        }

        // Apply saved font size
        String savedFontSize = prefs.get("font_size");
        if (savedFontSize != null) {
            cmbFontSize.setValue(savedFontSize);
        }
    }

    private void setupDisplayPreferences() {
        cmbFontSize.setItems(FXCollections.observableArrayList("Small", "Medium", "Large"));
        cmbFontSize.setValue("Medium");
    }

    private void setupAccountInfo() {
        if (SessionManager.isLoggedIn()) {
            User user = SessionManager.getCurrentUser();
            txtDisplayName.setText(user.getName());
        }
    }

    private void setupThemeClicks() {
        themeLightOption.setOnMouseClicked(event -> selectTheme("light", true));
        themeDarkOption.setOnMouseClicked(event -> selectTheme("dark", true));
    }

    private void selectTheme(String theme, boolean persist) {
        currentTheme = theme;

        // Update visual selection
        themeLightOption.getStyleClass().removeAll("theme-option", "theme-option-selected");
        themeDarkOption.getStyleClass().removeAll("theme-option", "theme-option-selected");

        if ("light".equals(theme)) {
            themeLightOption.getStyleClass().add("theme-option-selected");
            themeDarkOption.getStyleClass().add("theme-option");
        } else {
            themeDarkOption.getStyleClass().add("theme-option-selected");
            themeLightOption.getStyleClass().add("theme-option");
        }

        // Apply to main layout (live preview)
        if (MainLayoutController.instance != null && MainLayoutController.instance.getMainBorderPane() != null) {
            var pane = MainLayoutController.instance.getMainBorderPane();
            pane.getStyleClass().removeAll("theme-dark", "theme-light");
            pane.getStyleClass().add("theme-" + theme);
        }

        // Persist to database
        if (persist && SessionManager.isLoggedIn()) {
            prefsDAO.savePreference(SessionManager.getCurrentUser().getUserId(), "theme", theme);
        }
    }

    private void setupAccentClicks() {
        accentIndigo.setOnMouseClicked(e -> selectAccent("indigo", true));
        accentTeal.setOnMouseClicked(e -> selectAccent("teal", true));
        accentAmber.setOnMouseClicked(e -> selectAccent("amber", true));
        accentRose.setOnMouseClicked(e -> selectAccent("rose", true));
    }

    private void selectAccent(String accent, boolean persist) {
        currentAccent = accent;

        // Reset all rings
        accentIndigoRing.setStyle("-fx-fill: transparent; -fx-stroke: transparent; -fx-stroke-width: 2;");
        accentTealRing.setStyle("-fx-fill: transparent; -fx-stroke: transparent; -fx-stroke-width: 2;");
        accentAmberRing.setStyle("-fx-fill: transparent; -fx-stroke: transparent; -fx-stroke-width: 2;");
        accentRoseRing.setStyle("-fx-fill: transparent; -fx-stroke: transparent; -fx-stroke-width: 2;");

        // Highlight selected ring
        Circle selectedRing;
        switch (accent) {
            case "teal": selectedRing = accentTealRing; break;
            case "amber": selectedRing = accentAmberRing; break;
            case "rose": selectedRing = accentRoseRing; break;
            default: selectedRing = accentIndigoRing; break;
        }
        selectedRing.setStyle("-fx-fill: transparent; -fx-stroke: -theme-primary; -fx-stroke-width: 2;");

        // Apply accent to main layout
        if (MainLayoutController.instance != null && MainLayoutController.instance.getMainBorderPane() != null) {
            var pane = MainLayoutController.instance.getMainBorderPane();
            pane.getStyleClass().removeAll("accent-indigo", "accent-teal", "accent-amber", "accent-rose");
            pane.getStyleClass().add("accent-" + accent);
        }

        // Persist to database
        if (persist && SessionManager.isLoggedIn()) {
            prefsDAO.savePreference(SessionManager.getCurrentUser().getUserId(), "accent_color", accent);
        }
    }

    @FXML
    private void handleSaveAccount() {
        if (!SessionManager.isLoggedIn()) return;

        String name = txtDisplayName.getText();
        if (ValidationUtil.isEmpty(name)) {
            AlertUtil.showWarning("Validation Error", "Name Required", "Display name cannot be empty.");
            return;
        }

        User user = SessionManager.getCurrentUser();
        user.setName(name.trim());
        if (userDAO.updateUser(user)) {
            AlertUtil.showInfo("Success", "Profile Updated", "Your display name has been updated.");
            // Update top bar
            if (MainLayoutController.instance != null) {
                MainLayoutController.instance.refreshSessionInfo();
            }
        } else {
            AlertUtil.showError("Error", "Update Failed", "Could not update profile.");
        }
    }

    @FXML
    private void handleChangePassword() {
        if (!SessionManager.isLoggedIn()) return;

        String current = txtCurrentPassword.getText();
        String newPass = txtNewPassword.getText();
        String confirm = txtConfirmPassword.getText();

        if (ValidationUtil.isEmpty(current) || ValidationUtil.isEmpty(newPass) || ValidationUtil.isEmpty(confirm)) {
            AlertUtil.showWarning("Validation Error", "Missing Fields", "Please fill in all password fields.");
            return;
        }

        if (!newPass.equals(confirm)) {
            AlertUtil.showWarning("Validation Error", "Mismatch", "New password and confirmation do not match.");
            return;
        }

        if (newPass.length() < 6) {
            AlertUtil.showWarning("Validation Error", "Too Short", "Password must be at least 6 characters.");
            return;
        }

        // Verify current password
        String currentHash = AuthenticationService.hashPassword(current);
        User user = SessionManager.getCurrentUser();
        if (!currentHash.equals(user.getPassword())) {
            AlertUtil.showError("Authentication Error", "Wrong Password", "Current password is incorrect.");
            return;
        }

        if (authService.updatePassword(user.getUserId(), newPass)) {
            user.setPassword(AuthenticationService.hashPassword(newPass));
            AlertUtil.showInfo("Success", "Password Changed", "Your password has been updated.");
            txtCurrentPassword.clear();
            txtNewPassword.clear();
            txtConfirmPassword.clear();
        } else {
            AlertUtil.showError("Error", "Update Failed", "Could not update password.");
        }
    }
}
