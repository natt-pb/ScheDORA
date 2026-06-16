package com.classroomscheduler.controller;

import com.classroomscheduler.model.Booking;
import com.classroomscheduler.service.ReportService;
import com.classroomscheduler.util.AlertUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {

    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<String> cmbStatusFilter;
    @FXML private TextField txtRoomFilter;
    @FXML private Label lblResultCount;

    // Utilization table
    @FXML private TableView<Map.Entry<String, Integer>> tblUtilization;
    @FXML private TableColumn<Map.Entry<String, Integer>, String> colUtilRoom;
    @FXML private TableColumn<Map.Entry<String, Integer>, Integer> colUtilBookings;
    @FXML private TableColumn<Map.Entry<String, Integer>, String> colUtilBar;

    // History table
    @FXML private TableView<Booking> tblHistory;
    @FXML private TableColumn<Booking, Integer> colHistId;
    @FXML private TableColumn<Booking, String> colHistRoom;
    @FXML private TableColumn<Booking, String> colHistCourse;
    @FXML private TableColumn<Booking, String> colHistRequester;
    @FXML private TableColumn<Booking, String> colHistDate;
    @FXML private TableColumn<Booking, String> colHistTime;
    @FXML private TableColumn<Booking, String> colHistStatus;
    @FXML private TableColumn<Booking, String> colHistReason;

    private final ReportService reportService = new ReportService();
    private List<Booking> currentHistory = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilters();
        setupUtilizationTable();
        setupHistoryTable();
    }

    private void setupFilters() {
        dpStartDate.setValue(LocalDate.now().minusMonths(1));
        dpEndDate.setValue(LocalDate.now());

        cmbStatusFilter.setItems(FXCollections.observableArrayList(
            "All", "PENDING", "APPROVED", "REJECTED", "CANCELLED", "RELEASED"
        ));
        cmbStatusFilter.setValue("All");
    }

    private void setupUtilizationTable() {
        colUtilRoom.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getKey()));
        colUtilBookings.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getValue()).asObject());
        colUtilBar.setCellValueFactory(cellData -> {
            int count = cellData.getValue().getValue();
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < Math.min(count, 20); i++) {
                bar.append("█");
            }
            if (count > 20) bar.append("...");
            return new SimpleStringProperty(bar.toString() + " (" + count + ")");
        });
    }

    private void setupHistoryTable() {
        colHistId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colHistRoom.setCellValueFactory(new PropertyValueFactory<>("roomName"));
        colHistCourse.setCellValueFactory(new PropertyValueFactory<>("course"));
        colHistRequester.setCellValueFactory(new PropertyValueFactory<>("representative"));
        colHistDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        colHistTime.setCellValueFactory(cellData -> {
            Booking b = cellData.getValue();
            return new SimpleStringProperty(b.getStartTime() + " - " + b.getEndTime());
        });
        colHistStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colHistReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
    }

    @FXML
    private void handleGenerateReport() {
        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();

        if (start == null || end == null) {
            AlertUtil.showWarning("Missing Dates", "Date Required", "Please select both start and end dates.");
            return;
        }
        if (start.isAfter(end)) {
            AlertUtil.showWarning("Invalid Range", "Date Error", "Start date must be before or equal to end date.");
            return;
        }

        String startStr = start.toString();
        String endStr = end.toString();
        String status = cmbStatusFilter.getValue();
        String roomFilter = txtRoomFilter.getText();

        // Generate utilization report
        Map<String, Integer> utilization = reportService.getRoomUtilization(startStr, endStr);
        ObservableList<Map.Entry<String, Integer>> utilData = FXCollections.observableArrayList(utilization.entrySet());
        tblUtilization.setItems(utilData);

        // Generate booking history
        currentHistory = reportService.getBookingHistory(startStr, endStr, status, roomFilter);
        tblHistory.setItems(FXCollections.observableArrayList(currentHistory));
        lblResultCount.setText(currentHistory.size() + " record(s) found");
    }

    @FXML
    private void handleExportCsv() {
        if (currentHistory == null || currentHistory.isEmpty()) {
            AlertUtil.showWarning("No Data", "Nothing to Export", "Please generate a report first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report to CSV");
        fileChooser.setInitialFileName("booking_report.csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(tblHistory.getScene().getWindow());
        if (file != null) {
            if (reportService.exportToCsv(currentHistory, file)) {
                AlertUtil.showInfo("Export Complete", "CSV Exported", "Report exported to: " + file.getAbsolutePath());
            } else {
                AlertUtil.showError("Export Error", "Failed", "Could not export report to CSV.");
            }
        }
    }
}
