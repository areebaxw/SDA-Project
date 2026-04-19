package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import models.User;
import models.AWSCredential;
import models.EC2Instance;
import dao.EC2DAO;
import dao.AWSCredentialDAO;
import aws.EC2Service;
import aws.AWSClientFactory;
import services.IdleDetectionService;
import services.CombinedIdleStrategy;

import java.util.List;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

    @FXML
    private Button backButton;
    
    private User currentUser;
    private EC2DAO ec2DAO;
    private AWSCredentialDAO awsCredentialDAO;
    private EC2Service ec2Service;
    private IdleDetectionService idleDetectionService;
    private ObservableList<EC2Instance> ec2Data;
    
    public EC2Controller() {
        this.ec2DAO = new EC2DAO();
        this.awsCredentialDAO = new AWSCredentialDAO();
        this.ec2Service = null;
        this.idleDetectionService = null;
        this.ec2Data = FXCollections.observableArrayList();
    }

    private EC2Service requireEC2Service() {
        if (!AWSClientFactory.getInstance().isInitialized()) {
            showError("AWS credentials not configured. Please configure credentials first.");
            return null;
        }

        if (ec2Service == null) {
            try {
                ec2Service = new EC2Service();
            } catch (Exception e) {
                showError("Failed to initialize AWS EC2 client: " + e.getMessage());
                return null;
            }
        }

        return ec2Service;
    }

    private IdleDetectionService requireIdleDetectionService() {
        if (!AWSClientFactory.getInstance().isInitialized()) {
            showError("AWS credentials not configured. Please configure credentials first.");
            return null;
        }

        if (idleDetectionService == null) {
            try {
                idleDetectionService = new IdleDetectionService();
            } catch (Exception e) {
                showError("Failed to initialize Idle Detection: " + e.getMessage());
                return null;
            }
        }

        return idleDetectionService;
    }
    
    @FXML
    private void initialize() {
        setupTableColumns();
        loadEC2Instances();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        initializeAwsClientFromSavedCredentials();
        loadEC2Instances();
    }

    private void initializeAwsClientFromSavedCredentials() {
        try {
            AWSCredential cred = awsCredentialDAO.getActiveCredentials(currentUser.getUserId());
            if (cred != null) {
                AWSClientFactory.getInstance().initializeCredentials(
                        cred.getAccessKey(),
                        cred.getSecretKey(),
                        cred.getRegion()
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize AWS client from saved credentials: " + e.getMessage());
        }
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

            // If local cache is empty, try syncing once from AWS.
            if (instances.isEmpty() && currentUser != null && AWSClientFactory.getInstance().isInitialized()) {
                EC2Service service = requireEC2Service();
                if (service != null) {
                    service.syncFromAWS(currentUser.getUserId());
                    instances = ec2DAO.getAllEC2Instances();
                }
            }

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
            EC2Service service = requireEC2Service();
            if (service == null) return;
           
            int syncedCount = service.syncFromAWS(currentUser.getUserId());
            
        
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

        EC2Service service = requireEC2Service();
        if (service == null) return;
        
        boolean success = service.startInstance(selected.getInstanceId());
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

        EC2Service service = requireEC2Service();
        if (service == null) return;
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Stop");
        confirmation.setHeaderText("Stop EC2 Instance");
        confirmation.setContentText("Are you sure you want to stop " + selected.getInstanceId() + "?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = service.stopInstance(selected.getInstanceId());
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

        EC2Service service = requireEC2Service();
        if (service == null) return;
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Terminate");
        confirmation.setHeaderText("Terminate EC2 Instance");
        confirmation.setContentText("⚠️ WARNING: This will PERMANENTLY DELETE " + selected.getInstanceId() + "!\nAre you sure?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = service.terminateInstance(selected.getInstanceId());
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
            IdleDetectionService detectionService = requireIdleDetectionService();
            if (detectionService == null) return;

            detectionService.setStrategy(new CombinedIdleStrategy());
           
            detectionService.detectIdleEC2Instances(7, 5.0);
            
           
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

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 820);
            DashboardController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Dashboard - " + currentUser.getUsername());
            stage.show();
        } catch (Exception e) {
            showError("Error returning to dashboard: " + e.getMessage());
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
