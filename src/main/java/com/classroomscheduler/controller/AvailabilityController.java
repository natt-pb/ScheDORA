package com.classroomscheduler.controller;

import com.classroomscheduler.dao.BlockDAO;
import com.classroomscheduler.dao.RoomDAO;
import com.classroomscheduler.model.Block;
import com.classroomscheduler.model.Room;
import com.classroomscheduler.service.ConflictDetectionService;
import com.classroomscheduler.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AvailabilityController implements Initializable {

    @FXML
    private DatePicker dpDate;
    @FXML
    private ComboBox<String> cmbTimeSlot;
    @FXML
    private ComboBox<String> cmbBuilding;
    @FXML
    private TextField txtCapacity;

    @FXML
    private TableView<Room> tblAvailability;
    @FXML
    private TableColumn<Room, String> colRoomName;
    @FXML
    private TableColumn<Room, String> colBuilding;
    @FXML
    private TableColumn<Room, Integer> colCapacity;
    @FXML
    private TableColumn<Room, String> colRoomType;
    @FXML
    private TableColumn<Room, String> colStatus;
    @FXML
    private TableColumn<Room, Void> colAction;

    private final RoomDAO roomDAO = new RoomDAO();
    private final BlockDAO blockDAO = new BlockDAO();
    private final ConflictDetectionService conflictService = new ConflictDetectionService();

    // Prefill shared state variables
    public static String prefillRoomName = null;
    public static LocalDate prefillDate = null;
    public static String prefillTimeSlot = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupInputs();
        setupTableColumns();
    }

    private void setupInputs() {
        dpDate.setValue(LocalDate.now());

        cmbTimeSlot.setItems(FXCollections.observableArrayList(
                "06:30 AM - 08:30 AM",
                "08:30 AM - 10:30 AM",
                "10:30 AM - 12:30 PM",
                "12:30 PM - 02:30 PM",
                "02:30 PM - 04:30 PM",
                "04:30 PM - 06:30 PM",
                "06:30 PM - 08:30 PM"
        ));
        cmbTimeSlot.setValue("08:30 AM - 10:30 AM");

        // Load block names from database
        ObservableList<String> blockNames = FXCollections.observableArrayList("All Buildings");
        for (Block b : blockDAO.getAllBlocks()) {
            blockNames.add(b.getBlockName());
        }
        cmbBuilding.setItems(blockNames);
        cmbBuilding.setValue("All Buildings");
    }

    private void setupTableColumns() {
        colRoomName.setCellValueFactory(new PropertyValueFactory<>("roomName"));
        colBuilding.setCellValueFactory(new PropertyValueFactory<>("building"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        
        colStatus.setCellValueFactory(cellData -> {
            Room r = cellData.getValue();
            LocalDate date = dpDate.getValue();
            String slot = cmbTimeSlot.getValue();
            
            if (date == null || slot == null) {
                return new SimpleStringProperty("Search pending");
            }
            
            String[] times = slot.split(" - ");
            boolean hasConflict = conflictService.hasConflict(r.getRoomId(), date.toString(), times[0], times[1]);
            return new SimpleStringProperty(hasConflict ? "Occupied" : "Available");
        });

        setupActionColumn();
    }

    private void setupActionColumn() {
        Callback<TableColumn<Room, Void>, TableCell<Room, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Room, Void> call(final TableColumn<Room, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Book Now");
                    {
                        btn.getStyleClass().add("btn-primary");
                        btn.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
                        btn.setOnAction(event -> {
                            Room room = getTableView().getItems().get(getIndex());
                            LocalDate date = dpDate.getValue();
                            String slot = cmbTimeSlot.getValue();
                            
                            // Check if room is available
                            String[] times = slot.split(" - ");
                            boolean hasConflict = conflictService.hasConflict(room.getRoomId(), date.toString(), times[0], times[1]);
                            if (hasConflict) {
                                AlertUtil.showWarning("Booking Conflict", "Room Occupied", "This room is occupied during this time slot.");
                                return;
                            }

                            // Set Prefill State
                            prefillRoomName = room.getRoomName();
                            prefillDate = date;
                            prefillTimeSlot = slot;

                            // Switch to booking tab
                            if (MainLayoutController.instance != null) {
                                MainLayoutController.instance.handleNavBooking();
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Room room = getTableView().getItems().get(getIndex());
                            LocalDate date = dpDate.getValue();
                            String slot = cmbTimeSlot.getValue();
                            if (date != null && slot != null) {
                                String[] times = slot.split(" - ");
                                boolean hasConflict = conflictService.hasConflict(room.getRoomId(), date.toString(), times[0], times[1]);
                                btn.setVisible(!hasConflict);
                            }
                            setGraphic(btn);
                        }
                    }
                };
            }
        };

        colAction.setCellFactory(cellFactory);
    }

    @FXML
    private void handleSearch() {
        LocalDate date = dpDate.getValue();
        String slot = cmbTimeSlot.getValue();
        String building = cmbBuilding.getValue();
        String capStr = txtCapacity.getText();

        if (date == null || slot == null) {
            AlertUtil.showWarning("Search Error", "Criteria Missing", "Please select a date and time slot.");
            return;
        }

        List<Room> allRooms = roomDAO.getRooms();
        List<Room> matchingRooms = new ArrayList<>();

        for (Room r : allRooms) {
            // Filter by Building
            if (building != null && !building.equalsIgnoreCase("All Buildings")) {
                if (!r.getBuilding().equalsIgnoreCase(building)) {
                    continue;
                }
            }

            // Filter by capacity
            if (capStr != null && !capStr.trim().isEmpty()) {
                try {
                    int cap = Integer.parseInt(capStr.trim());
                    if (r.getCapacity() < cap) {
                        continue;
                    }
                } catch (NumberFormatException e) {
                    AlertUtil.showWarning("Search Error", "Invalid Capacity", "Capacity filter must be numeric.");
                    return;
                }
            }

            matchingRooms.add(r);
        }

        ObservableList<Room> data = FXCollections.observableArrayList(matchingRooms);
        tblAvailability.setItems(data);
        tblAvailability.refresh();
    }
}
