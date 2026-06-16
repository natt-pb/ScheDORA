package com.classroomscheduler.controller;

import com.classroomscheduler.dao.RoomDAO;
import com.classroomscheduler.model.Booking;
import com.classroomscheduler.model.Room;
import com.classroomscheduler.service.BookingService;
import com.classroomscheduler.util.AlertUtil;
import com.classroomscheduler.util.SessionManager;
import com.classroomscheduler.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class BookingController implements Initializable {

    @FXML
    private TextField txtCourse;
    @FXML
    private TextField txtLecturer;
    @FXML
    private TextField txtRepresentative;
    @FXML
    private ComboBox<String> cmbBookingType;
    @FXML
    private TextField txtOriginalRoom;
    @FXML
    private ComboBox<Room> cmbRequestedRoom;
    @FXML
    private DatePicker dpBookingDate;
    @FXML
    private ComboBox<String> cmbStartTime;
    @FXML
    private ComboBox<String> cmbEndTime;
    @FXML
    private TextArea txtReason;

    private final RoomDAO roomDAO = new RoomDAO();
    private final BookingService bookingService = new BookingService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setDefaultRequester();
        checkPrefillData();
    }

    private void setupComboBoxes() {
        cmbBookingType.setItems(FXCollections.observableArrayList("Lecture", "Quiz", "Lab"));
        cmbBookingType.setValue("Lecture");

        // Rooms list
        List<Room> rooms = roomDAO.getRooms();
        cmbRequestedRoom.setItems(FXCollections.observableArrayList(rooms));

        ObservableList<String> hours = FXCollections.observableArrayList(
                "06:30 AM", "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM",
                "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM",
                "11:30 AM", "12:00 PM", "12:30 PM", "01:00 PM", "01:30 PM",
                "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM", "04:00 PM",
                "04:30 PM", "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM",
                "07:00 PM", "07:30 PM", "08:00 PM", "08:30 PM"
        );
        cmbStartTime.setItems(hours);
        cmbEndTime.setItems(hours);
    }

    private void setDefaultRequester() {
        if (SessionManager.isLoggedIn()) {
            txtRepresentative.setText(SessionManager.getCurrentUser().getName());
        }
    }

    private void checkPrefillData() {
        if (AvailabilityController.prefillRoomName != null) {
            // Match room in cmb
            for (Room r : cmbRequestedRoom.getItems()) {
                if (r.getRoomName().equalsIgnoreCase(AvailabilityController.prefillRoomName)) {
                    cmbRequestedRoom.setValue(r);
                    break;
                }
            }
            if (AvailabilityController.prefillDate != null) {
                dpBookingDate.setValue(AvailabilityController.prefillDate);
            }
            if (AvailabilityController.prefillTimeSlot != null) {
                String[] times = AvailabilityController.prefillTimeSlot.split(" - ");
                if (times.length == 2) {
                    cmbStartTime.setValue(times[0].trim());
                    cmbEndTime.setValue(times[1].trim());
                }
            }

            // Reset prefill variables so they don't persist
            AvailabilityController.prefillRoomName = null;
            AvailabilityController.prefillDate = null;
            AvailabilityController.prefillTimeSlot = null;
        } else {
            dpBookingDate.setValue(LocalDate.now());
        }
    }

    @FXML
    private void handleReserveClassroom() {
        String course = txtCourse.getText();
        String lecturer = txtLecturer.getText();
        String repName = txtRepresentative.getText();
        String bookingType = cmbBookingType.getValue();
        String originalRoom = txtOriginalRoom.getText();
        Room reqRoom = cmbRequestedRoom.getValue();
        LocalDate date = dpBookingDate.getValue();
        String start = cmbStartTime.getValue();
        String end = cmbEndTime.getValue();
        String reason = txtReason.getText();

        if (ValidationUtil.isEmpty(course) || ValidationUtil.isEmpty(lecturer) ||
            ValidationUtil.isEmpty(repName) || bookingType == null ||
            reqRoom == null || date == null ||
            ValidationUtil.isEmpty(start) || ValidationUtil.isEmpty(end) ||
            ValidationUtil.isEmpty(reason)) {
            AlertUtil.showWarning("Form Incomplete", "Invalid Input", "All required fields completed must be filled.");
            return;
        }

        // Validate time format just in case
        if (!ValidationUtil.isValidTimeFormat(start) || !ValidationUtil.isValidTimeFormat(end)) {
            AlertUtil.showWarning("Validation Error", "Invalid Time Format", "Please select valid time ranges.");
            return;
        }

        // Build Booking Object
        Booking booking = new Booking();
        booking.setUserId(SessionManager.getCurrentUser().getUserId());
        booking.setRoomId(reqRoom.getRoomId());
        booking.setRoomName(reqRoom.getRoomName());
        booking.setCourse(course.trim());
        booking.setLecturer(lecturer.trim());
        booking.setRepresentative(repName.trim());
        booking.setBookingDate(date.toString());
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setBookingType(bookingType);
        booking.setReason(reason.trim());
        
        // Admin bookings are auto-approved, reps bookings are pending
        if (SessionManager.isAdmin()) {
            booking.setStatus("APPROVED");
        } else {
            booking.setStatus("PENDING");
        }

        // Call Booking Service
        String result = bookingService.requestBooking(booking);

        if (result.equalsIgnoreCase("Success")) {
            AlertUtil.showInfo("Booking Completed", "Success", "Booking completed successfully.");
            clearForm();
        } else if (result.contains("already occupied")) {
            AlertUtil.showWarning("Room Occupied", "Conflict", "Room already occupied during this period.");
        } else {
            AlertUtil.showError("Booking Failed", "Error", result);
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private void clearForm() {
        txtCourse.clear();
        txtLecturer.clear();
        setDefaultRequester();
        txtOriginalRoom.clear();
        cmbRequestedRoom.setValue(null);
        dpBookingDate.setValue(LocalDate.now());
        cmbStartTime.setValue(null);
        cmbEndTime.setValue(null);
        txtReason.clear();
    }
}
