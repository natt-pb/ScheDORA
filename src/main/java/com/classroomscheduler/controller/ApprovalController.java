package com.classroomscheduler.controller;

import com.classroomscheduler.dao.RoomDAO;
import com.classroomscheduler.model.Booking;
import com.classroomscheduler.model.Room;
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
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ApprovalController implements Initializable {

    @FXML private TableView<Booking> tblApprovals;
    @FXML private TableColumn<Booking, Integer> colBookingId;
    @FXML private TableColumn<Booking, String> colRequester;
    @FXML private TableColumn<Booking, String> colCourse;
    @FXML private TableColumn<Booking, String> colRoom;
    @FXML private TableColumn<Booking, String> colDate;
    @FXML private TableColumn<Booking, String> colTime;
    @FXML private TableColumn<Booking, String> colReason;
    @FXML private TableColumn<Booking, Void> colActions;

    private final BookingService bookingService = new BookingService();
    private final RoomDAO roomDAO = new RoomDAO();
    private final ObservableList<Booking> pendingList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadPendingApprovals();
    }

    private void setupTableColumns() {
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colRequester.setCellValueFactory(new PropertyValueFactory<>("representative"));
        colCourse.setCellValueFactory(new PropertyValueFactory<>("course"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        colTime.setCellValueFactory(cellData -> {
            Booking b = cellData.getValue();
            return new SimpleStringProperty(b.getStartTime() + " - " + b.getEndTime());
        });
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        Callback<TableColumn<Booking, Void>, TableCell<Booking, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Booking, Void> call(final TableColumn<Booking, Void> param) {
                return new TableCell<>() {
                    private final Button btnApprove = new Button("Approve");
                    private final Button btnReject = new Button("Reject");
                    private final HBox container = new HBox(8, btnApprove, btnReject);
                    {
                        btnApprove.getStyleClass().add("btn-primary");
                        btnApprove.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
                        btnApprove.setOnAction(event -> {
                            Booking booking = getTableView().getItems().get(getIndex());
                            int approverId = SessionManager.getCurrentUser().getUserId();
                            if (bookingService.approveBooking(booking.getBookingId(), approverId)) {
                                AlertUtil.showInfo("Approved", "Booking Approved", "The booking request has been approved.");
                                loadPendingApprovals();
                            } else {
                                AlertUtil.showError("Error", "Action Failed", "Could not approve booking.");
                            }
                        });

                        btnReject.getStyleClass().add("nav-item-error");
                        btnReject.setStyle("-fx-border-color: -theme-error; -fx-border-radius: 4px; -fx-border-width: 1; -fx-padding: 4 8; -fx-font-size: 11px;");
                        btnReject.setOnAction(event -> {
                            Booking booking = getTableView().getItems().get(getIndex());
                            showRejectDialog(booking);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(container);
                        }
                    }
                };
            }
        };

        colActions.setCellFactory(cellFactory);
    }

    private void showRejectDialog(Booking booking) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reject Booking");
        dialog.setHeaderText("Provide a rejection reason for booking #" + booking.getBookingId());

        ButtonType rejectBtn = new ButtonType("Reject", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(rejectBtn, ButtonType.CANCEL);

        TextArea txtReason = new TextArea();
        txtReason.setPromptText("Enter rejection reason...");
        txtReason.setPrefRowCount(3);

        // Alternative room selector
        ComboBox<Room> cmbAltRoom = new ComboBox<>();
        cmbAltRoom.setPromptText("Suggest alternative room (optional)");
        List<Room> rooms = roomDAO.getRooms();
        cmbAltRoom.setItems(FXCollections.observableArrayList(rooms));

        VBox content = new VBox(10, new Label("Reason:"), txtReason, new Label("Alternative Room (optional):"), cmbAltRoom);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == rejectBtn) {
                return txtReason.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            int altRoomId = cmbAltRoom.getValue() != null ? cmbAltRoom.getValue().getRoomId() : 0;
            int rejecterId = SessionManager.getCurrentUser().getUserId();
            if (bookingService.rejectBooking(booking.getBookingId(), reason, altRoomId, rejecterId)) {
                AlertUtil.showWarning("Rejected", "Booking Rejected", "The booking request has been rejected.");
                loadPendingApprovals();
            } else {
                AlertUtil.showError("Error", "Action Failed", "Could not reject booking.");
            }
        });
    }

    private void loadPendingApprovals() {
        List<Booking> list;
        if (SessionManager.isAdmin()) {
            list = bookingService.getPendingBookings();
        } else if (SessionManager.isFacilityManager()) {
            list = bookingService.getPendingBookingsForManager(SessionManager.getCurrentUser().getUserId());
        } else {
            list = bookingService.getPendingBookings();
        }
        pendingList.setAll(list);
        tblApprovals.setItems(pendingList);
    }

    @FXML
    private void handleRefresh() {
        loadPendingApprovals();
    }
}
