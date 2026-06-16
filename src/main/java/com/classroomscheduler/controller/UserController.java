package com.classroomscheduler.controller;

import com.classroomscheduler.dao.UserDAO;
import com.classroomscheduler.model.User;
import com.classroomscheduler.service.AuthenticationService;
import com.classroomscheduler.service.CsvImportService;
import com.classroomscheduler.util.AlertUtil;
import com.classroomscheduler.util.ValidationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserController implements Initializable {

    @FXML private TextField txtName;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRole;
    @FXML private TextField txtSearch;

    // Extended fields
    @FXML private TextField txtProgramme;
    @FXML private ComboBox<String> cmbLevel;
    @FXML private ComboBox<String> cmbSemester;
    @FXML private TextField txtStaffId;
    @FXML private TextField txtDepartment;

    // Conditional field containers
    @FXML private VBox studentFields;
    @FXML private VBox staffFields;

    @FXML private TableView<User> tblUsers;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colProgramme;

    private final UserDAO userDAO = new UserDAO();
    private final AuthenticationService authService = new AuthenticationService();
    private final CsvImportService csvImportService = new CsvImportService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private User selectedUser = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupComboBox();
        loadUserData();
        setupRoleListener();
    }

    private void setupTableColumns() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        if (colProgramme != null) {
            colProgramme.setCellValueFactory(cellData -> {
                User u = cellData.getValue();
                String info = "";
                if (u.getProgramme() != null && !u.getProgramme().isEmpty()) {
                    info = u.getProgramme();
                    if (u.getLevel() != null && !u.getLevel().isEmpty()) {
                        info += " L" + u.getLevel();
                    }
                } else if (u.getDepartment() != null && !u.getDepartment().isEmpty()) {
                    info = u.getDepartment();
                }
                return new SimpleStringProperty(info);
            });
        }
    }

    private void setupComboBox() {
        cmbRole.setItems(FXCollections.observableArrayList(
                "MAIN_ADMIN",
                "FACILITY_MANAGER",
                "LECTURER",
                "STUDENT_REP",
                "STUDENT"
        ));

        if (cmbLevel != null) {
            cmbLevel.setItems(FXCollections.observableArrayList("100", "200", "300", "400"));
        }
        if (cmbSemester != null) {
            cmbSemester.setItems(FXCollections.observableArrayList("1", "2"));
        }
    }

    private void setupRoleListener() {
        cmbRole.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateFieldVisibility(newVal);
        });
    }

    private void updateFieldVisibility(String role) {
        if (role == null) return;
        boolean isStudentType = "STUDENT".equals(role) || "STUDENT_REP".equals(role);
        boolean isStaffType = "LECTURER".equals(role) || "FACILITY_MANAGER".equals(role);

        if (studentFields != null) {
            studentFields.setVisible(isStudentType);
            studentFields.setManaged(isStudentType);
        }
        if (staffFields != null) {
            staffFields.setVisible(isStaffType);
            staffFields.setManaged(isStaffType);
        }
    }

    private void loadUserData() {
        List<User> users = userDAO.getAllUsers();
        userList.setAll(users);
        tblUsers.setItems(userList);
    }

    @FXML
    private void handleAddUser() {
        String name = txtName.getText();
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String role = cmbRole.getValue();

        if (ValidationUtil.isEmpty(name) || ValidationUtil.isEmpty(username) || 
            ValidationUtil.isEmpty(password) || role == null) {
            AlertUtil.showWarning("Validation Error", "Missing Fields", "Please fill in all required fields.");
            return;
        }

        User user = new User(0, name.trim(), username.trim(), AuthenticationService.hashPassword(password), role);
        populateExtendedFields(user);
        user.setMustChangePassword(true);

        // Check if username already exists
        if (userDAO.getUserByUsername(user.getUsername()) != null) {
            AlertUtil.showWarning("Duplicate User", "Username Taken", "A user with username '" + username + "' already exists.");
            return;
        }

        if (userDAO.addUser(user)) {
            AlertUtil.showInfo("Success", "User Registered", "User account for " + name + " was registered successfully.");
            clearForm();
            loadUserData();
        } else {
            AlertUtil.showError("Database Error", "Save Failed", "Could not save user to database.");
        }
    }

    @FXML
    private void handleUpdateUser() {
        if (selectedUser == null) {
            AlertUtil.showWarning("Selection Error", "No Selection", "Please select a user from the table to update.");
            return;
        }

        String name = txtName.getText();
        String username = txtUsername.getText();
        String role = cmbRole.getValue();

        if (ValidationUtil.isEmpty(name) || ValidationUtil.isEmpty(username) || role == null) {
            AlertUtil.showWarning("Validation Error", "Missing Fields", "Please fill in all fields (excluding password).");
            return;
        }

        User existing = userDAO.getUserByUsername(username.trim());
        if (existing != null && existing.getUserId() != selectedUser.getUserId()) {
            AlertUtil.showWarning("Duplicate Username", "Username Taken", "Another user with username '" + username + "' already exists.");
            return;
        }

        selectedUser.setName(name.trim());
        selectedUser.setUsername(username.trim());
        selectedUser.setRole(role);
        populateExtendedFields(selectedUser);

        // Update password if typed
        String password = txtPassword.getText();
        if (!ValidationUtil.isEmpty(password)) {
            authService.updatePassword(selectedUser.getUserId(), password);
        }

        if (userDAO.updateUser(selectedUser)) {
            AlertUtil.showInfo("Success", "User Updated", "User details updated successfully.");
            clearForm();
            loadUserData();
        } else {
            AlertUtil.showError("Database Error", "Update Failed", "Could not update user details.");
        }
    }

    @FXML
    private void handleDeleteUser() {
        if (selectedUser == null) {
            AlertUtil.showWarning("Selection Error", "No Selection", "Please select a user from the table to delete.");
            return;
        }

        if (userDAO.deleteUser(selectedUser.getUserId())) {
            AlertUtil.showInfo("Success", "User Deleted", "User deleted successfully.");
            clearForm();
            loadUserData();
        } else {
            AlertUtil.showError("Database Error", "Delete Failed", "Could not delete user from database.");
        }
    }

    @FXML
    private void handleImportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Students from CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showOpenDialog(tblUsers.getScene().getWindow());
        if (file != null) {
            int count = csvImportService.importStudents(file);
            if (count > 0) {
                AlertUtil.showInfo("Import Complete", "Students Imported", count + " student records imported successfully.");
                loadUserData();
            } else {
                AlertUtil.showWarning("Import Result", "No Records", "No new student records were imported. They may already exist.");
            }
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    @FXML
    private void handleTableClick() {
        selectedUser = tblUsers.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            txtName.setText(selectedUser.getName());
            txtUsername.setText(selectedUser.getUsername());
            txtPassword.clear(); // Security: do not prefill password field
            cmbRole.setValue(selectedUser.getRole());

            if (txtProgramme != null) txtProgramme.setText(selectedUser.getProgramme() != null ? selectedUser.getProgramme() : "");
            if (cmbLevel != null) cmbLevel.setValue(selectedUser.getLevel());
            if (cmbSemester != null) cmbSemester.setValue(selectedUser.getSemester());
            if (txtStaffId != null) txtStaffId.setText(selectedUser.getStaffId() != null ? selectedUser.getStaffId() : "");
            if (txtDepartment != null) txtDepartment.setText(selectedUser.getDepartment() != null ? selectedUser.getDepartment() : "");

            updateFieldVisibility(selectedUser.getRole());
        }
    }

    @FXML
    private void handleSearchKey() {
        String query = txtSearch.getText();
        if (query == null || query.trim().isEmpty()) {
            loadUserData();
        } else {
            String q = query.trim().toLowerCase();
            List<User> searchResults = userDAO.getAllUsers().stream()
                    .filter(u -> u.getName().toLowerCase().contains(q) ||
                                 u.getUsername().toLowerCase().contains(q) ||
                                 u.getRole().toLowerCase().contains(q) ||
                                 (u.getProgramme() != null && u.getProgramme().toLowerCase().contains(q)) ||
                                 (u.getDepartment() != null && u.getDepartment().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
            userList.setAll(searchResults);
            tblUsers.setItems(userList);
        }
    }

    private void populateExtendedFields(User user) {
        if (txtProgramme != null && txtProgramme.getText() != null) user.setProgramme(txtProgramme.getText().trim());
        if (cmbLevel != null && cmbLevel.getValue() != null) user.setLevel(cmbLevel.getValue());
        if (cmbSemester != null && cmbSemester.getValue() != null) user.setSemester(cmbSemester.getValue());
        if (txtStaffId != null && txtStaffId.getText() != null) user.setStaffId(txtStaffId.getText().trim());
        if (txtDepartment != null && txtDepartment.getText() != null) user.setDepartment(txtDepartment.getText().trim());
    }

    private void clearForm() {
        txtName.clear();
        txtUsername.clear();
        txtPassword.clear();
        cmbRole.setValue(null);
        if (txtProgramme != null) txtProgramme.clear();
        if (cmbLevel != null) cmbLevel.setValue(null);
        if (cmbSemester != null) cmbSemester.setValue(null);
        if (txtStaffId != null) txtStaffId.clear();
        if (txtDepartment != null) txtDepartment.clear();
        selectedUser = null;
        tblUsers.getSelectionModel().clearSelection();

        if (studentFields != null) { studentFields.setVisible(false); studentFields.setManaged(false); }
        if (staffFields != null) { staffFields.setVisible(false); staffFields.setManaged(false); }
    }
}
