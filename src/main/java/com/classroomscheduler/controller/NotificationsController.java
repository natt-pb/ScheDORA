package com.classroomscheduler.controller;

import com.classroomscheduler.model.Notification;
import com.classroomscheduler.service.NotificationService;
import com.classroomscheduler.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class NotificationsController implements Initializable {

    @FXML private TableView<Notification> tblNotifications;
    @FXML private TableColumn<Notification, String> colStatus;
    @FXML private TableColumn<Notification, String> colTitle;
    @FXML private TableColumn<Notification, String> colMessage;
    @FXML private TableColumn<Notification, String> colType;
    @FXML private TableColumn<Notification, String> colDate;
    @FXML private TableColumn<Notification, Void> colAction;

    private final NotificationService notificationService = new NotificationService();
    private final ObservableList<Notification> notificationList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        loadNotifications();
    }

    private void setupColumns() {
        colStatus.setCellValueFactory(cellData -> {
            Notification n = cellData.getValue();
            return new SimpleStringProperty(n.isRead() ? "✓" : "●");
        });
        colStatus.setStyle("-fx-alignment: CENTER;");

        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Mark as read button
        colAction.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Notification, Void> call(TableColumn<Notification, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Read");
                    {
                        btn.setStyle("-fx-padding: 2 8; -fx-font-size: 10px;");
                        btn.setOnAction(e -> {
                            Notification n = getTableView().getItems().get(getIndex());
                            if (!n.isRead()) {
                                notificationService.markAsRead(n.getNotificationId());
                                loadNotifications();
                            }
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Notification n = getTableView().getItems().get(getIndex());
                            btn.setVisible(!n.isRead());
                            setGraphic(btn);
                        }
                    }
                };
            }
        });

        // Style unread rows
        tblNotifications.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (!item.isRead()) {
                    setStyle("-fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void loadNotifications() {
        if (!SessionManager.isLoggedIn()) return;
        List<Notification> list = notificationService.getUserNotifications(SessionManager.getCurrentUser().getUserId());
        notificationList.setAll(list);
        tblNotifications.setItems(notificationList);
    }

    @FXML
    private void handleMarkAllRead() {
        if (!SessionManager.isLoggedIn()) return;
        notificationService.markAllAsRead(SessionManager.getCurrentUser().getUserId());
        loadNotifications();
    }

    @FXML
    private void handleRefresh() {
        loadNotifications();
    }
}
