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
import aws.CloudWatchService;
import services.IdleDetectionService;

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
    private CloudWatchService cloudWatchService;
    private IdleDetectionService idleDetectionService;
    private ObservableList<EC2Instance> ec2Data;
    
    public EC2Controller() {
        this.ec2DAO = new EC2DAO();
        this.ec2Service = new EC2Service();
        this.cloudWatchService = new CloudWatchService();
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
            List<EC2Instance> instances = ec2Service.getAllInstances();
            
            for (EC2Instance instance : instances) {
                // Fetch CPU utilization for running instances
                if ("running".equalsIgnoreCase(instance.getInstanceState())) {
                    double cpuUtilization = cloudWatchService.getEC2CPUUtilization(instance.getInstanceId(), 7);
                    instance.setCpuUtilization(cpuUtilization);
                }
                
                // Set idle to null when syncing from AWS
                instance.setIdle(null);
                instance.setUserId(currentUser.getUserId());
                ec2DAO.saveOrUpdateEC2Instance(instance);
            }
            
            loadEC2Instances();
            showInfo("Synced " + instances.size() + " EC2 instances from AWS");
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
        try {
            // Detect idle status for current instances in UI only
            for (EC2Instance instance : ec2Data) {
                if ("running".equalsIgnoreCase(instance.getInstanceState())) {
                    // Get CPU and network metrics
                    double cpuUtilization = cloudWatchService.getEC2CPUUtilization(instance.getInstanceId(), 7);
                    double networkIn = cloudWatchService.getEC2NetworkIn(instance.getInstanceId(), 7);
                    
                    // Update instance metrics
                    instance.setCpuUtilization(cpuUtilization);
                    instance.setNetworkIn(networkIn);
                    
                    // Determine if idle (CPU < 5% threshold)
                    boolean isIdle = cpuUtilization < 5.0;
                    instance.setIdle(isIdle);
                } else {
                    instance.setIdle(false);
                }
            }
            
            // Refresh table to show updated idle status (UI only, not saved to DB)
            ec2Table.refresh();
            showInfo("Idle detection completed!");
        } catch (Exception e) {
            System.err.println("Error detecting idle instances: " + e.getMessage());
            showError("Error detecting idle instances");
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
