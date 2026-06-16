package com.classroomscheduler.controller;

import com.classroomscheduler.dao.BlockDAO;
import com.classroomscheduler.dao.UserDAO;
import com.classroomscheduler.model.Block;
import com.classroomscheduler.model.User;
import com.classroomscheduler.util.AlertUtil;
import com.classroomscheduler.util.ValidationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BlockController implements Initializable {

    @FXML private TextField txtBlockName;
    @FXML private ComboBox<Block> cmbAssignBlock;
    @FXML private ComboBox<User> cmbAssignManager;

    // Blocks table
    @FXML private TableView<Block> tblBlocks;
    @FXML private TableColumn<Block, Integer> colBlockId;
    @FXML private TableColumn<Block, String> colBlockName;
    @FXML private TableColumn<Block, String> colManagers;

    // Assignments table
    @FXML private TableView<BlockManagerAssignment> tblAssignments;
    @FXML private TableColumn<BlockManagerAssignment, String> colAssignBlock;
    @FXML private TableColumn<BlockManagerAssignment, String> colAssignManager;
    @FXML private TableColumn<BlockManagerAssignment, Void> colAssignAction;

    private final BlockDAO blockDAO = new BlockDAO();
    private final UserDAO userDAO = new UserDAO();
    private Block selectedBlock = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBlocksTable();
        setupAssignmentsTable();
        setupComboBoxes();
        loadData();
    }

    private void setupBlocksTable() {
        colBlockId.setCellValueFactory(new PropertyValueFactory<>("blockId"));
        colBlockName.setCellValueFactory(new PropertyValueFactory<>("blockName"));
        colManagers.setCellValueFactory(cellData -> {
            Block block = cellData.getValue();
            // Find managers assigned to this block
            List<User> allManagers = userDAO.getUsersByRole("FACILITY_MANAGER");
            List<Block> managerBlocks;
            StringBuilder sb = new StringBuilder();
            for (User fm : allManagers) {
                managerBlocks = blockDAO.getBlocksByManager(fm.getUserId());
                for (Block b : managerBlocks) {
                    if (b.getBlockId() == block.getBlockId()) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(fm.getName());
                    }
                }
            }
            return new SimpleStringProperty(sb.length() > 0 ? sb.toString() : "— Unassigned —");
        });
    }

    private void setupAssignmentsTable() {
        colAssignBlock.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().blockName));
        colAssignManager.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().managerName));

        colAssignAction.setCellFactory(new Callback<>() {
            @Override
            public TableCell<BlockManagerAssignment, Void> call(TableColumn<BlockManagerAssignment, Void> param) {
                return new TableCell<>() {
                    private final Button btnRemove = new Button("Remove");
                    {
                        btnRemove.getStyleClass().add("nav-item-error");
                        btnRemove.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
                        btnRemove.setOnAction(event -> {
                            BlockManagerAssignment assignment = getTableView().getItems().get(getIndex());
                            blockDAO.removeManager(assignment.managerId, assignment.blockId);
                            AlertUtil.showInfo("Success", "Manager Removed", "Manager removed from block.");
                            loadData();
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btnRemove);
                    }
                };
            }
        });
    }

    private void setupComboBoxes() {
        cmbAssignBlock.setConverter(new StringConverter<>() {
            @Override public String toString(Block block) { return block != null ? block.getBlockName() : ""; }
            @Override public Block fromString(String string) { return null; }
        });

        cmbAssignManager.setConverter(new StringConverter<>() {
            @Override public String toString(User user) { return user != null ? user.getName() + " (" + user.getUsername() + ")" : ""; }
            @Override public User fromString(String string) { return null; }
        });
    }

    private void loadData() {
        // Load blocks table
        List<Block> blocks = blockDAO.getAllBlocks();
        tblBlocks.setItems(FXCollections.observableArrayList(blocks));

        // Load combo boxes
        cmbAssignBlock.setItems(FXCollections.observableArrayList(blocks));
        List<User> managers = userDAO.getUsersByRole("FACILITY_MANAGER");
        cmbAssignManager.setItems(FXCollections.observableArrayList(managers));

        // Load assignments table
        ObservableList<BlockManagerAssignment> assignments = FXCollections.observableArrayList();
        for (Block block : blocks) {
            for (User fm : managers) {
                List<Block> fmBlocks = blockDAO.getBlocksByManager(fm.getUserId());
                for (Block b : fmBlocks) {
                    if (b.getBlockId() == block.getBlockId()) {
                        assignments.add(new BlockManagerAssignment(
                            block.getBlockId(), block.getBlockName(),
                            fm.getUserId(), fm.getName()
                        ));
                    }
                }
            }
        }
        tblAssignments.setItems(assignments);
    }

    @FXML
    private void handleAddBlock() {
        String name = txtBlockName.getText();
        if (ValidationUtil.isEmpty(name)) {
            AlertUtil.showWarning("Validation Error", "Missing Field", "Please enter a block name.");
            return;
        }
        if (blockDAO.getBlockByName(name.trim()) != null) {
            AlertUtil.showWarning("Duplicate", "Block Exists", "A block with name '" + name + "' already exists.");
            return;
        }
        Block block = new Block();
        block.setBlockName(name.trim());
        if (blockDAO.addBlock(block)) {
            AlertUtil.showInfo("Success", "Block Created", "Block '" + name + "' was added.");
            clearForm();
            loadData();
        } else {
            AlertUtil.showError("Error", "Save Failed", "Could not save block to database.");
        }
    }

    @FXML
    private void handleUpdateBlock() {
        if (selectedBlock == null) {
            AlertUtil.showWarning("Selection Error", "No Selection", "Please select a block from the table.");
            return;
        }
        String name = txtBlockName.getText();
        if (ValidationUtil.isEmpty(name)) {
            AlertUtil.showWarning("Validation Error", "Missing Field", "Please enter a block name.");
            return;
        }
        Block existing = blockDAO.getBlockByName(name.trim());
        if (existing != null && existing.getBlockId() != selectedBlock.getBlockId()) {
            AlertUtil.showWarning("Duplicate", "Block Name Taken", "Another block with name '" + name + "' already exists.");
            return;
        }
        selectedBlock.setBlockName(name.trim());
        if (blockDAO.updateBlock(selectedBlock)) {
            AlertUtil.showInfo("Success", "Block Updated", "Block updated successfully.");
            clearForm();
            loadData();
        } else {
            AlertUtil.showError("Error", "Update Failed", "Could not update block.");
        }
    }

    @FXML
    private void handleDeleteBlock() {
        if (selectedBlock == null) {
            AlertUtil.showWarning("Selection Error", "No Selection", "Please select a block from the table.");
            return;
        }
        if (blockDAO.deleteBlock(selectedBlock.getBlockId())) {
            AlertUtil.showInfo("Success", "Block Deleted", "Block deleted successfully.");
            clearForm();
            loadData();
        } else {
            AlertUtil.showError("Error", "Delete Failed", "Could not delete block. It may have rooms assigned.");
        }
    }

    @FXML
    private void handleAssignManager() {
        Block block = cmbAssignBlock.getValue();
        User manager = cmbAssignManager.getValue();
        if (block == null || manager == null) {
            AlertUtil.showWarning("Validation Error", "Missing Selection", "Please select both a block and a manager.");
            return;
        }
        if (blockDAO.assignManager(manager.getUserId(), block.getBlockId())) {
            AlertUtil.showInfo("Success", "Manager Assigned", manager.getName() + " assigned to " + block.getBlockName() + ".");
            loadData();
        } else {
            AlertUtil.showWarning("Already Assigned", "Duplicate", "This manager is already assigned to this block.");
        }
    }

    @FXML
    private void handleRemoveManager() {
        Block block = cmbAssignBlock.getValue();
        User manager = cmbAssignManager.getValue();
        if (block == null || manager == null) {
            AlertUtil.showWarning("Validation Error", "Missing Selection", "Please select both a block and a manager.");
            return;
        }
        if (blockDAO.removeManager(manager.getUserId(), block.getBlockId())) {
            AlertUtil.showInfo("Success", "Manager Removed", manager.getName() + " removed from " + block.getBlockName() + ".");
            loadData();
        } else {
            AlertUtil.showWarning("Not Found", "No Assignment", "This manager was not assigned to this block.");
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleBlockTableClick() {
        selectedBlock = tblBlocks.getSelectionModel().getSelectedItem();
        if (selectedBlock != null) {
            txtBlockName.setText(selectedBlock.getBlockName());
        }
    }

    private void clearForm() {
        txtBlockName.clear();
        selectedBlock = null;
        tblBlocks.getSelectionModel().clearSelection();
    }

    // Inner class for assignment table data
    public static class BlockManagerAssignment {
        public final int blockId;
        public final String blockName;
        public final int managerId;
        public final String managerName;

        public BlockManagerAssignment(int blockId, String blockName, int managerId, String managerName) {
            this.blockId = blockId;
            this.blockName = blockName;
            this.managerId = managerId;
            this.managerName = managerName;
        }
    }
}
