package com.classroomscheduler.controller;

import com.classroomscheduler.model.Booking;
import com.classroomscheduler.model.User;
import com.classroomscheduler.service.BookingService;
import com.classroomscheduler.dao.RoomDAO;
import com.classroomscheduler.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label lblTotalRooms;
    @FXML
    private Label lblPendingBookings;
    @FXML
    private Label lblApprovedBookings;

    // Upcoming relocations table
    @FXML
    private TableView<Booking> tblUpcoming;
    @FXML
    private TableColumn<Booking, String> colCourse;
    @FXML
    private TableColumn<Booking, String> colRoom;
    @FXML
    private TableColumn<Booking, String> colDate;
    @FXML
    private TableColumn<Booking, String> colTime;
    @FXML
    private TableColumn<Booking, String> colStatus;

    private final BookingService bookingService = new BookingService();
    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupWelcomeText();
        loadMetrics();
        setupTableColumns();
        loadUpcomingBookings();
    }

    private void setupWelcomeText() {
        if (SessionManager.isLoggedIn()) {
            User user = SessionManager.getCurrentUser();
            welcomeLabel.setText("Welcome back, " + user.getName());
        }
    }

    private void loadMetrics() {
        int totalRooms = roomDAO.getRooms().size();
        List<Booking> allBookings = bookingService.getAllBookings();
        
        long pendingCount = allBookings.stream()
                .filter(b -> "PENDING".equalsIgnoreCase(b.getStatus()))
                .count();
        long approvedCount = allBookings.stream()
                .filter(b -> "APPROVED".equalsIgnoreCase(b.getStatus()))
                .count();

        lblTotalRooms.setText(String.valueOf(totalRooms));
        lblPendingBookings.setText(String.valueOf(pendingCount));
        lblApprovedBookings.setText(String.valueOf(approvedCount));
    }

    private void setupTableColumns() {
        colCourse.setCellValueFactory(new PropertyValueFactory<>("course"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        // Create custom time string from start and end time
        colTime.setCellValueFactory(cellData -> {
            Booking b = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(b.getStartTime() + " - " + b.getEndTime());
        });
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadUpcomingBookings() {
        List<Booking> list;
        if (SessionManager.isAdmin()) {
            list = bookingService.getAllBookings();
        } else {
            // Show current user's bookings
            list = bookingService.getBookingsByUser(SessionManager.getCurrentUser().getUserId());
        }

        // Filter out cancelled or released bookings to show only active/pending ones
        List<Booking> activeList = list.stream()
                .filter(b -> !"CANCELLED".equalsIgnoreCase(b.getStatus()) && !"RELEASED".equalsIgnoreCase(b.getStatus()))
                .collect(Collectors.toList());

        ObservableList<Booking> data = FXCollections.observableArrayList(activeList);
        tblUpcoming.setItems(data);
    }
}
