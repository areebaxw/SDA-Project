package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.User;
import models.EC2Instance;
import dao.EC2DAO;
import aws.EC2Service;
import services.IdleDetectionService;
import services.CombinedIdleStrategy;

import java.util.List;

/**
 * EC2Controller - Controller for EC2 instances view
 */
public class EC2Controller {
    @FXML
    private TableView<EC2Instance> ec2Table;
    
    @FXML
    private TableColumn<EC2Instance, String> instanceIdColumn;
    
    @FXML
    private TableColumn<EC2Instance, String> instanceTypeColumn;
    
    @FXML
    private TableColumn<EC2Instance, String> stateColumn;
    
    @FXML
    private TableColumn<EC2Instance, String> availabilityZoneColumn;
    
    @FXML
    private TableColumn<EC2Instance, Double> cpuColumn;
    
    @FXML
    private TableColumn<EC2Instance, Boolean> idleColumn;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button startButton;
    
    @FXML
    private Button stopButton;
    
    @FXML
    private Button terminateButton;
    
    @FXML
    private Button detectIdleButton;
    
    private User currentUser;
    private EC2DAO ec2DAO;
    private EC2Service ec2Service;
    private IdleDetectionService idleDetectionService;
    private ObservableList<EC2Instance> ec2Data;
    
    public EC2Controller() {
        this.ec2DAO = new EC2DAO();
        this.ec2Service = new EC2Service();
        this.idleDetectionService = new IdleDetectionService();
        this.ec2Data = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        setupTableColumns();
        loadEC2Instances();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    private void setupTableColumns() {
        instanceIdColumn.setCellValueFactory(new PropertyValueFactory<>("instanceId"));
        instanceTypeColumn.setCellValueFactory(new PropertyValueFactory<>("instanceType"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("instanceState"));
        availabilityZoneColumn.setCellValueFactory(new PropertyValueFactory<>("availabilityZone"));
        cpuColumn.setCellValueFactory(new PropertyValueFactory<>("cpuUtilization"));
        idleColumn.setCellValueFactory(new PropertyValueFactory<>("idle"));
        
        ec2Table.setItems(ec2Data);
    }
    
    @FXML
    private void handleRefresh() {
        loadEC2Instances();
        showInfo("EC2 instances refreshed!");
    }
    
    private void loadEC2Instances() {
        try {
            // Load from database
            List<EC2Instance> instances = ec2DAO.getAllEC2Instances();
            ec2Data.clear();
            ec2Data.addAll(instances);
            
            System.out.println("Loaded " + instances.size() + " EC2 instances");
        } catch (Exception e) {
            System.err.println("Error loading EC2 instances: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading EC2 instances");
        }
    }
    
    @FXML
    private void handleSyncFromAWS() {
        try {
            // ✅ CORRECT: Delegate ALL sync logic to service
            // Service handles: fetch from AWS, get metrics, save to DB
            int syncedCount = ec2Service.syncFromAWS(currentUser.getUserId());
            
            // ✅ CORRECT: Controller only reloads UI
            loadEC2Instances();
            showInfo("Synced " + syncedCount + " EC2 instances from AWS");
        } catch (Exception e) {
            System.err.println("Error syncing EC2 instances: " + e.getMessage());
            showError("Error syncing from AWS: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleStart() {
        EC2Instance selected = ec2Table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an instance");
            return;
        }
        
        boolean success = ec2Service.startInstance(selected.getInstanceId());
        if (success) {
            showInfo("Instance " + selected.getInstanceId() + " started");
            handleRefresh();
        } else {
            showError("Failed to start instance");
        }
    }
    
    @FXML
    private void handleStop() {
        EC2Instance selected = ec2Table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an instance");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Stop");
        confirmation.setHeaderText("Stop EC2 Instance");
        confirmation.setContentText("Are you sure you want to stop " + selected.getInstanceId() + "?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = ec2Service.stopInstance(selected.getInstanceId());
            if (success) {
                showInfo("Instance " + selected.getInstanceId() + " stopped");
                handleRefresh();
            } else {
                showError("Failed to stop instance");
            }
        }
    }
    
    @FXML
    private void handleTerminate() {
        EC2Instance selected = ec2Table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an instance");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Terminate");
        confirmation.setHeaderText("Terminate EC2 Instance");
        confirmation.setContentText("⚠️ WARNING: This will PERMANENTLY DELETE " + selected.getInstanceId() + "!\nAre you sure?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = ec2Service.terminateInstance(selected.getInstanceId());
            if (success) {
                showInfo("Instance " + selected.getInstanceId() + " terminated");
                handleRefresh();
            } else {
                showError("Failed to terminate instance");
            }
        }
    }
    
    @FXML
    private void handleDetectIdle() {
        try{
            idleDetectionService.setStrategy(new CombinedIdleStrategy());
           
            idleDetectionService.detectIdleEC2Instances(7, 5.0);
            
           
            loadEC2Instances();
            showInfo("Idle detection completed! Check alerts for idle instances.");
            
        } catch (IllegalStateException e) {
            System.err.println("Strategy error: " + e.getMessage());
            showError("Strategy not properly configured: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error detecting idle instances: " + e.getMessage());
            e.printStackTrace();
            showError("Error detecting idle instances: " + e.getMessage());
        }
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
