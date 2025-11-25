package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Alert;
import models.User;
import services.AlertService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * AlertController - Controller for alerts view
 */
public class AlertController {
    @FXML
    private TableView<Alert> alertTable;
    
    @FXML
    private TableColumn<Alert, String> resourceIdColumn;
    
    @FXML
    private TableColumn<Alert, String> resourceTypeColumn;
    
    @FXML
    private TableColumn<Alert, String> alertTypeColumn;
    
    @FXML
    private TableColumn<Alert, String> severityColumn;
    
    @FXML
    private TableColumn<Alert, String> messageColumn;
    
    @FXML
    private TableColumn<Alert, LocalDateTime> createdAtColumn;
    
    @FXML
    private TableColumn<Alert, Boolean> resolvedColumn;
    
    @FXML
    private CheckBox showResolvedCheckBox;
    
    private User currentUser;
    private AlertService alertService;
    private ObservableList<Alert> alertData;
    
    public AlertController() {
        this.alertService = AlertService.getInstance();
        this.alertData = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        setupTableColumns();
        loadAlerts();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    private void setupTableColumns() {
        resourceIdColumn.setCellValueFactory(new PropertyValueFactory<>("resourceId"));
        resourceTypeColumn.setCellValueFactory(new PropertyValueFactory<>("resourceType"));
        alertTypeColumn.setCellValueFactory(new PropertyValueFactory<>("alertType"));
        severityColumn.setCellValueFactory(new PropertyValueFactory<>("severity"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        resolvedColumn.setCellValueFactory(new PropertyValueFactory<>("resolved"));
        
        // Format created at column
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdAtColumn.setCellFactory(column -> new TableCell<Alert, LocalDateTime>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });
        
        alertTable.setItems(alertData);
    }
    
    @FXML
    private void handleRefresh() {
        loadAlerts();
        showInfo("Alerts refreshed!");
    }
    
    private void loadAlerts() {
        try {
            List<Alert> alerts;
            if (showResolvedCheckBox.isSelected()) {
                alerts = alertService.getAllAlerts();
            } else {
                alerts = alertService.getUnresolvedAlerts();
            }
            
            alertData.clear();
            alertData.addAll(alerts);
            
            System.out.println("Loaded " + alerts.size() + " alerts");
        } catch (Exception e) {
            System.err.println("Error loading alerts: " + e.getMessage());
            showError("Error loading alerts");
        }
    }
    
    @FXML
    private void handleResolve() {
        Alert selected = alertTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an alert");
            return;
        }
        
        if (selected.isResolved()) {
            showWarning("Alert is already resolved");
            return;
        }
        
        boolean success = alertService.resolveAlert(selected.getAlertId());
        if (success) {
            showInfo("Alert resolved successfully");
            loadAlerts();
        } else {
            showError("Failed to resolve alert");
        }
    }
    
    @FXML
    private void handleDelete() {
        Alert selected = alertTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an alert");
            return;
        }
        
        javafx.scene.control.Alert confirmation = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Alert");
        confirmation.setContentText("Are you sure you want to delete this alert?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = alertService.deleteAlert(selected.getAlertId());
            if (success) {
                showInfo("Alert deleted successfully");
                loadAlerts();
            } else {
                showError("Failed to delete alert");
            }
        }
    }
    
    @FXML
    private void handleShowResolvedToggle() {
        loadAlerts();
    }
    
    private void showInfo(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
