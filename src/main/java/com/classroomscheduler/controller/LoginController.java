package com.classroomscheduler.controller;

import com.classroomscheduler.Main;
import com.classroomscheduler.model.User;
import com.classroomscheduler.service.AuthenticationService;
import com.classroomscheduler.service.ActivityLogService;
import com.classroomscheduler.util.AlertUtil;
import com.classroomscheduler.util.SessionManager;
import com.classroomscheduler.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField loginUsername;
    @FXML
    private PasswordField loginPassword;

    private final AuthenticationService authService = new AuthenticationService();
    private final ActivityLogService activityLogService = new ActivityLogService();

    @FXML
    private void handleSignIn() {
        String username = loginUsername.getText();
        String password = loginPassword.getText();

        if (ValidationUtil.isEmpty(username) || ValidationUtil.isEmpty(password)) {
            AlertUtil.showWarning("Login Failed", "Fields Missing", "Please enter both username and password.");
            return;
        }

        User authenticatedUser = authService.login(username.trim(), password);

        if (authenticatedUser != null) {
            System.out.println("LoginController: Login successful for " + authenticatedUser.getName());
            activityLogService.log(authenticatedUser.getUserId(), "LOGIN", "User logged in");

            // Check if user must change password on first login
            if (authenticatedUser.isMustChangePassword()) {
                Main.setRoot("change_password.fxml");
            } else {
                Main.setRoot("main_layout.fxml");
            }
        } else {
            AlertUtil.showError("Login Failed", "Invalid Credentials", "Incorrect username or password. Please try again.");
        }
    }
}
