package com.classroomscheduler.controller;

import com.classroomscheduler.Main;
import com.classroomscheduler.service.AuthenticationService;
import com.classroomscheduler.util.AlertUtil;
import com.classroomscheduler.util.SessionManager;
import com.classroomscheduler.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class ChangePasswordController {

    @FXML private Label lblWelcome;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;

    private final AuthenticationService authService = new AuthenticationService();

    @FXML
    private void initialize() {
        if (SessionManager.isLoggedIn()) {
            lblWelcome.setText("Welcome, " + SessionManager.getCurrentUser().getName() + "! Please set a new password to continue.");
        }
    }

    @FXML
    private void handleChangePassword() {
        String newPass = txtNewPassword.getText();
        String confirmPass = txtConfirmPassword.getText();

        if (ValidationUtil.isEmpty(newPass) || ValidationUtil.isEmpty(confirmPass)) {
            AlertUtil.showWarning("Validation Error", "Fields Required", "Please fill in both password fields.");
            return;
        }

        if (newPass.length() < 6) {
            AlertUtil.showWarning("Validation Error", "Password Too Short", "Password must be at least 6 characters.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            AlertUtil.showWarning("Validation Error", "Passwords Don't Match", "The passwords you entered do not match.");
            return;
        }

        boolean success = authService.updatePassword(SessionManager.getCurrentUser().getUserId(), newPass);
        if (success) {
            AlertUtil.showInfo("Success", "Password Changed", "Your password has been updated successfully.");
            Main.setRoot("main_layout.fxml");
        } else {
            AlertUtil.showError("Error", "Update Failed", "Could not update password. Please try again.");
        }
    }
}
