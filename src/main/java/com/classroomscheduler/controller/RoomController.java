package com.classroomscheduler.controller;

import com.classroomscheduler.dao.BlockDAO;
import com.classroomscheduler.dao.RoomDAO;
import com.classroomscheduler.model.Block;
import com.classroomscheduler.model.Room;
import com.classroomscheduler.util.AlertUtil;
import com.classroomscheduler.util.SessionManager;
import com.classroomscheduler.util.ValidationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RoomController implements Initializable {

    @FXML private TextField txtRoomName;
    @FXML private ComboBox<Block> cmbBlock;
    @FXML private TextField txtCapacity;
    @FXML private ComboBox<String> cmbRoomType;
    @FXML private CheckBox chkProjector;
    @FXML private CheckBox chkAc;
    @FXML private CheckBox chkWhiteboard;
    @FXML private TextField txtSearch;

    @FXML private TableView<Room> tblRooms;
    @FXML private TableColumn<Room, Integer> colRoomId;
    @FXML private TableColumn<Room, String> colRoomName;
    @FXML private TableColumn<Room, String> colBlock;
    @FXML private TableColumn<Room, Integer> colCapacity;
    @FXML private TableColumn<Room, String> colRoomType;
    @FXML private TableColumn<Room, String> colFacilities;

    private final RoomDAO roomDAO = new RoomDAO();
    private final BlockDAO blockDAO = new BlockDAO();
    private final ObservableList<Room> roomList = FXCollections.observableArrayList();
    private Room selectedRoom = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupComboBoxes();
        loadRoomData();
    }

    private void setupTableColumns() {
        colRoomId.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        colRoomName.setCellValueFactory(new PropertyValueFactory<>("roomName"));
        colBlock.setCellValueFactory(cellData -> {
            Room r = cellData.getValue();
            if (r.getBlockName() != null && !r.getBlockName().isEmpty()) {
                return new SimpleStringProperty(r.getBlockName());
            }
            return new SimpleStringProperty(r.getBuilding() != null ? r.getBuilding() : "—");
        });
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        if (colFacilities != null) {
            colFacilities.setCellValueFactory(cellData -> {
                Room r = cellData.getValue();
                StringBuilder sb = new StringBuilder();
                if (r.isHasProjector()) sb.append("📽 ");
                if (r.isHasAc()) sb.append("❄ ");
                if (r.isHasWhiteboard()) sb.append("📝 ");
                return new SimpleStringProperty(sb.length() > 0 ? sb.toString().trim() : "—");
            });
        }
    }

    private void setupComboBoxes() {
        cmbRoomType.setItems(FXCollections.observableArrayList(
                "Lecture Theatre",
                "Classroom",
                "Lab",
                "Seminar Room",
                "Auditorium"
        ));

        // Load blocks from database
        List<Block> blocks = blockDAO.getAllBlocks();
        if (cmbBlock != null) {
            cmbBlock.setItems(FXCollections.observableArrayList(blocks));
            cmbBlock.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(Block block) { return block != null ? block.getBlockName() : ""; }
                @Override public Block fromString(String s) { return null; }
            });
        }
    }

    private void loadRoomData() {
        List<Room> rooms;
        if (SessionManager.isFacilityManager()) {
            // FM only sees rooms in their assigned blocks
            List<Block> managerBlocks = blockDAO.getBlocksByManager(SessionManager.getCurrentUser().getUserId());
            List<Integer> blockIds = managerBlocks.stream().map(Block::getBlockId).collect(Collectors.toList());
            rooms = roomDAO.getRooms().stream()
                    .filter(r -> blockIds.contains(r.getBlockId()))
                    .collect(Collectors.toList());
        } else {
            rooms = roomDAO.getRooms();
        }
        roomList.setAll(rooms);
        tblRooms.setItems(roomList);
    }

    @FXML
    private void handleAddRoom() {
        String name = txtRoomName.getText();
        Block block = cmbBlock != null ? cmbBlock.getValue() : null;
        String capacityStr = txtCapacity.getText();
        String type = cmbRoomType.getValue();

        if (ValidationUtil.isEmpty(name) || ValidationUtil.isEmpty(capacityStr) || type == null) {
            AlertUtil.showWarning("Validation Error", "Missing Fields", "Please fill in all required room fields.");
            return;
        }

        if (!ValidationUtil.isValidCapacity(capacityStr)) {
            AlertUtil.showWarning("Validation Error", "Invalid Capacity", "Capacity must be a positive integer.");
            return;
        }

        if (roomDAO.getRoomByName(name.trim()) != null) {
            AlertUtil.showWarning("Duplicate Room", "Room Exists", "A room with name '" + name + "' already exists.");
            return;
        }

        Room newRoom = new Room();
        newRoom.setRoomName(name.trim());
        newRoom.setBuilding(block != null ? block.getBlockName() : "");
        newRoom.setBlockId(block != null ? block.getBlockId() : 0);
        newRoom.setCapacity(Integer.parseInt(capacityStr.trim()));
        newRoom.setRoomType(type);
        newRoom.setHasProjector(chkProjector != null && chkProjector.isSelected());
        newRoom.setHasAc(chkAc != null && chkAc.isSelected());
        newRoom.setHasWhiteboard(chkWhiteboard != null && chkWhiteboard.isSelected());
        newRoom.setAvailabilityStatus("AVAILABLE");

        if (roomDAO.addRoom(newRoom)) {
            AlertUtil.showInfo("Success", "Room Created", "Room " + name + " was added successfully.");
            clearForm();
            loadRoomData();
        } else {
            AlertUtil.showError("Database Error", "Save Failed", "Could not save the room to database.");
        }
    }

    @FXML
    private void handleUpdateRoom() {
        if (selectedRoom == null) {
            AlertUtil.showWarning("Selection Error", "No Selection", "Please select a room from the table to update.");
            return;
        }

        String name = txtRoomName.getText();
        Block block = cmbBlock != null ? cmbBlock.getValue() : null;
        String capacityStr = txtCapacity.getText();
        String type = cmbRoomType.getValue();

        if (ValidationUtil.isEmpty(name) || ValidationUtil.isEmpty(capacityStr) || type == null) {
            AlertUtil.showWarning("Validation Error", "Missing Fields", "Please fill in all required room fields.");
            return;
        }

        if (!ValidationUtil.isValidCapacity(capacityStr)) {
            AlertUtil.showWarning("Validation Error", "Invalid Capacity", "Capacity must be a positive integer.");
            return;
        }

        Room existing = roomDAO.getRoomByName(name.trim());
        if (existing != null && existing.getRoomId() != selectedRoom.getRoomId()) {
            AlertUtil.showWarning("Duplicate Room", "Room Name Taken", "Another room with name '" + name + "' already exists.");
            return;
        }

        selectedRoom.setRoomName(name.trim());
        selectedRoom.setBuilding(block != null ? block.getBlockName() : "");
        selectedRoom.setBlockId(block != null ? block.getBlockId() : 0);
        selectedRoom.setCapacity(Integer.parseInt(capacityStr.trim()));
        selectedRoom.setRoomType(type);
        selectedRoom.setHasProjector(chkProjector != null && chkProjector.isSelected());
        selectedRoom.setHasAc(chkAc != null && chkAc.isSelected());
        selectedRoom.setHasWhiteboard(chkWhiteboard != null && chkWhiteboard.isSelected());

        if (roomDAO.updateRoom(selectedRoom)) {
            AlertUtil.showInfo("Success", "Room Updated", "Room was updated successfully.");
            clearForm();
            loadRoomData();
        } else {
            AlertUtil.showError("Database Error", "Update Failed", "Could not update room in database.");
        }
    }

    @FXML
    private void handleDeleteRoom() {
        if (selectedRoom == null) {
            AlertUtil.showWarning("Selection Error", "No Selection", "Please select a room from the table to delete.");
            return;
        }

        if (roomDAO.deleteRoom(selectedRoom.getRoomId())) {
            AlertUtil.showInfo("Success", "Room Deleted", "Room was deleted successfully.");
            clearForm();
            loadRoomData();
        } else {
            AlertUtil.showError("Database Error", "Delete Failed", "Could not delete room from database.");
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    @FXML
    private void handleTableClick() {
        selectedRoom = tblRooms.getSelectionModel().getSelectedItem();
        if (selectedRoom != null) {
            txtRoomName.setText(selectedRoom.getRoomName());
            txtCapacity.setText(String.valueOf(selectedRoom.getCapacity()));
            cmbRoomType.setValue(selectedRoom.getRoomType());

            // Set block dropdown
            if (cmbBlock != null && selectedRoom.getBlockId() > 0) {
                for (Block b : cmbBlock.getItems()) {
                    if (b.getBlockId() == selectedRoom.getBlockId()) {
                        cmbBlock.setValue(b);
                        break;
                    }
                }
            }

            // Set facility checkboxes
            if (chkProjector != null) chkProjector.setSelected(selectedRoom.isHasProjector());
            if (chkAc != null) chkAc.setSelected(selectedRoom.isHasAc());
            if (chkWhiteboard != null) chkWhiteboard.setSelected(selectedRoom.isHasWhiteboard());
        }
    }

    @FXML
    private void handleSearchKey() {
        String query = txtSearch.getText();
        if (query == null || query.trim().isEmpty()) {
            loadRoomData();
        } else {
            List<Room> searchResults = roomDAO.searchRoom(query.trim());
            roomList.setAll(searchResults);
            tblRooms.setItems(roomList);
        }
    }

    private void clearForm() {
        txtRoomName.clear();
        if (cmbBlock != null) cmbBlock.setValue(null);
        txtCapacity.clear();
        cmbRoomType.setValue(null);
        if (chkProjector != null) chkProjector.setSelected(false);
        if (chkAc != null) chkAc.setSelected(false);
        if (chkWhiteboard != null) chkWhiteboard.setSelected(false);
        selectedRoom = null;
        tblRooms.getSelectionModel().clearSelection();
    }
}
