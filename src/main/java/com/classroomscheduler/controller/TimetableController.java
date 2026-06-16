package com.classroomscheduler.controller;

import com.classroomscheduler.dao.BlockDAO;
import com.classroomscheduler.dao.TimetableDAO;
import com.classroomscheduler.model.Block;
import com.classroomscheduler.model.Timetable;
import com.classroomscheduler.model.User;
import com.classroomscheduler.service.CsvImportService;
import com.classroomscheduler.util.AlertUtil;
import com.classroomscheduler.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TimetableController implements Initializable {

    @FXML private ComboBox<String> departmentFilter;
    @FXML private ComboBox<String> levelFilter;
    @FXML private ComboBox<String> venueFilter;
    @FXML private TextField txtSearch;
    @FXML private Button btnImportCsv;

    @FXML private TableView<Timetable> tblTimetable;
    @FXML private TableColumn<Timetable, String> colCourseCode;
    @FXML private TableColumn<Timetable, String> colCourseName;
    @FXML private TableColumn<Timetable, String> colLecturer;
    @FXML private TableColumn<Timetable, String> colRoomName;
    @FXML private TableColumn<Timetable, String> colDay;
    @FXML private TableColumn<Timetable, String> colStartTime;
    @FXML private TableColumn<Timetable, String> colEndTime;

    private final TimetableDAO timetableDAO = new TimetableDAO();
    private final BlockDAO blockDAO = new BlockDAO();
    private final CsvImportService csvImportService = new CsvImportService();
    private final ObservableList<Timetable> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupFilters();
        setupAdminFeatures();
        loadTimetableData();
        autoFilterForCurrentUser();
    }

    private void setupTableColumns() {
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colLecturer.setCellValueFactory(new PropertyValueFactory<>("lecturer"));
        colRoomName.setCellValueFactory(new PropertyValueFactory<>("roomName"));
        colDay.setCellValueFactory(new PropertyValueFactory<>("day"));
        colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
    }

    private void setupFilters() {
        // Programme filter
        departmentFilter.setItems(FXCollections.observableArrayList(
                "All Programmes",
                "Computer Science",
                "Psychology",
                "Business School",
                "Mathematics",
                "Engineering"
        ));
        departmentFilter.setValue("All Programmes");

        // Level filter
        levelFilter.setItems(FXCollections.observableArrayList(
                "All Levels", "100", "200", "300", "400"
        ));
        levelFilter.setValue("All Levels");

        // Block/venue filter from database
        List<Block> blocks = blockDAO.getAllBlocks();
        ObservableList<String> blockNames = FXCollections.observableArrayList("All Blocks");
        for (Block b : blocks) {
            blockNames.add(b.getBlockName());
        }
        venueFilter.setItems(blockNames);
        venueFilter.setValue("All Blocks");
    }

    private void setupAdminFeatures() {
        // Only show import button for admin
        if (btnImportCsv != null) {
            boolean isAdmin = SessionManager.isAdmin();
            btnImportCsv.setVisible(isAdmin);
            btnImportCsv.setManaged(isAdmin);
        }
    }

    private void autoFilterForCurrentUser() {
        if (SessionManager.isLoggedIn()) {
            User user = SessionManager.getCurrentUser();
            // Auto-set programme/level filter for students and reps
            if (SessionManager.isStudent() || SessionManager.isStudentRep()) {
                if (user.getProgramme() != null && !user.getProgramme().isEmpty()) {
                    // Try to match programme in filter
                    for (String item : departmentFilter.getItems()) {
                        if (item.equalsIgnoreCase(user.getProgramme())) {
                            departmentFilter.setValue(item);
                            break;
                        }
                    }
                }
                if (user.getLevel() != null && !user.getLevel().isEmpty()) {
                    levelFilter.setValue(user.getLevel());
                }
                applyFiltersAndSearch();
            }
        }
    }

    private void loadTimetableData() {
        List<Timetable> schedules = timetableDAO.getTimetables();
        masterData.setAll(schedules);
        tblTimetable.setItems(masterData);
    }

    @FXML
    private void handleFilterChange() {
        applyFiltersAndSearch();
    }

    @FXML
    private void handleSearchKey() {
        applyFiltersAndSearch();
    }

    @FXML
    private void handleImportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Timetable from CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showOpenDialog(tblTimetable.getScene().getWindow());
        if (file != null) {
            int count = csvImportService.importTimetable(file);
            if (count > 0) {
                AlertUtil.showInfo("Import Complete", "Timetable Imported", count + " timetable entries imported successfully.");
                loadTimetableData();
            } else {
                AlertUtil.showWarning("Import Result", "No Records", "No new timetable entries were imported. Check room names exist.");
            }
        }
    }

    private void applyFiltersAndSearch() {
        String dept = departmentFilter.getValue();
        String level = levelFilter.getValue();
        String block = venueFilter.getValue();
        String search = txtSearch.getText();

        List<Timetable> filtered = timetableDAO.getTimetables();

        // Filter by programme
        if (dept != null && !dept.equals("All Programmes")) {
            String deptLower = dept.toLowerCase();
            filtered = filtered.stream()
                .filter(t -> t.getProgramme() != null && t.getProgramme().toLowerCase().contains(deptLower))
                .collect(Collectors.toList());
        }

        // Filter by level
        if (level != null && !level.equals("All Levels")) {
            filtered = filtered.stream()
                .filter(t -> t.getLevel() != null && t.getLevel().equals(level))
                .collect(Collectors.toList());
        }

        // Filter by block/venue
        if (block != null && !block.equals("All Blocks")) {
            String blockLower = block.toLowerCase();
            filtered = filtered.stream()
                .filter(t -> t.getRoomName() != null && t.getRoomName().toLowerCase().contains(blockLower))
                .collect(Collectors.toList());
        }
        
        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(t -> t.getCourseCode().toLowerCase().contains(q) ||
                                 t.getCourseName().toLowerCase().contains(q) ||
                                 t.getLecturer().toLowerCase().contains(q) ||
                                 t.getRoomName().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        tblTimetable.setItems(FXCollections.observableArrayList(filtered));
    }
}
