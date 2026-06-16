package com.classroomscheduler.controller;

import com.classroomscheduler.model.Booking;
import com.classroomscheduler.service.BookingService;
import com.classroomscheduler.util.AlertUtil;
import com.classroomscheduler.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationsController implements Initializable {

    @FXML private TableView<Booking> tblReservations;
    @FXML private TableColumn<Booking, Integer> colBookingId;
    @FXML private TableColumn<Booking, String> colRoomName;
    @FXML private TableColumn<Booking, String> colCourse;
    @FXML private TableColumn<Booking, String> colDate;
    @FXML private TableColumn<Booking, String> colTime;
    @FXML private TableColumn<Booking, String> colStatus;
    @FXML private TableColumn<Booking, String> colRejectionReason;
    @FXML private TableColumn<Booking, Void> colAction;

    private final BookingService bookingService = new BookingService();
    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadReservationsData();
    }

    private void setupTableColumns() {
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colRoomName.setCellValueFactory(new PropertyValueFactory<>("roomName"));
        colCourse.setCellValueFactory(new PropertyValueFactory<>("course"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        colTime.setCellValueFactory(cellData -> {
            Booking b = cellData.getValue();
            return new SimpleStringProperty(b.getStartTime() + " - " + b.getEndTime());
        });
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Rejection reason column (shows reason if rejected)
        if (colRejectionReason != null) {
            colRejectionReason.setCellValueFactory(cellData -> {
                Booking b = cellData.getValue();
                String reason = b.getRejectionReason();
                String altRoom = b.getAlternativeRoomName();
                String display = "";
                if (reason != null && !reason.isEmpty()) {
                    display = reason;
                }
                if (altRoom != null && !altRoom.isEmpty()) {
                    display += (display.isEmpty() ? "" : " | ") + "Suggested: " + altRoom;
                }
                return new SimpleStringProperty(display);
            });
        }

        setupActionColumn();
    }

    private void setupActionColumn() {
        Callback<TableColumn<Booking, Void>, TableCell<Booking, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Booking, Void> call(final TableColumn<Booking, Void> param) {
                return new TableCell<>() {
                    private final Button btnCancel = new Button("Cancel");
                    private final Button btnRelease = new Button("Release");
                    private final HBox container = new HBox(8, btnCancel, btnRelease);
                    {
                        btnCancel.getStyleClass().add("nav-item-error");
                        btnCancel.setStyle("-fx-border-color: -theme-error; -fx-border-radius: 4px; -fx-border-width: 1; -fx-padding: 4 8; -fx-font-size: 11px;");
                        btnCancel.setOnAction(event -> {
                            Booking booking = getTableView().getItems().get(getIndex());
                            if (bookingService.cancelBooking(booking.getBookingId())) {
                                AlertUtil.showInfo("Cancelled", "Reservation Cancelled", "The booking request was cancelled.");
                                loadReservationsData();
                            } else {
                                AlertUtil.showError("Error", "Action Failed", "Could not cancel booking request.");
                            }
                        });

                        btnRelease.getStyleClass().add("btn-secondary");
                        btnRelease.setStyle("-fx-padding: 4 8; -fx-font-size: 11px;");
                        btnRelease.setOnAction(event -> {
                            Booking booking = getTableView().getItems().get(getIndex());
                            int userId = SessionManager.getCurrentUser().getUserId();
                            if (bookingService.releaseBooking(booking.getBookingId(), userId)) {
                                AlertUtil.showInfo("Released", "Room Released", "The classroom was released and is now available.");
                                loadReservationsData();
                            } else {
                                AlertUtil.showError("Error", "Action Failed", "Could not release classroom booking.");
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Booking booking = getTableView().getItems().get(getIndex());

                            if ("PENDING".equalsIgnoreCase(booking.getStatus())) {
                                btnCancel.setVisible(true);
                                btnCancel.setManaged(true);
                                btnRelease.setVisible(false);
                                btnRelease.setManaged(false);
                                setGraphic(container);
                            } else if ("APPROVED".equalsIgnoreCase(booking.getStatus())) {
                                btnCancel.setVisible(false);
                                btnCancel.setManaged(false);
                                btnRelease.setVisible(true);
                                btnRelease.setManaged(true);
                                setGraphic(container);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };

        colAction.setCellFactory(cellFactory);
    }

    private void loadReservationsData() {
        List<Booking> list;
        if (SessionManager.isAdmin()) {
            list = bookingService.getAllBookings();
        } else {
            list = bookingService.getBookingsByUser(SessionManager.getCurrentUser().getUserId());
        }
        bookingList.setAll(list);
        tblReservations.setItems(bookingList);
    }

    @FXML
    private void handleRefresh() {
        loadReservationsData();
    }
}
