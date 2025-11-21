package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.User;
import models.RDSInstance;
import dao.RDSDAO;
import aws.RDSService;
import services.IdleDetectionService;

import java.util.List;

/**
 * RDSController - Controller for RDS instances view
 */
public class RDSController {
    @FXML
    private TableView<RDSInstance> rdsTable;
    
    @FXML
    private TableColumn<RDSInstance, String> identifierColumn;
    
    @FXML
    private TableColumn<RDSInstance, String> classColumn;
    
    @FXML
    private TableColumn<RDSInstance, String> engineColumn;
    
    @FXML
    private TableColumn<RDSInstance, String> statusColumn;
    
    @FXML
    private TableColumn<RDSInstance, Integer> storageColumn;
    
    @FXML
    private TableColumn<RDSInstance, Double> cpuColumn;
    
    @FXML
    private TableColumn<RDSInstance, Integer> connectionsColumn;
    
    @FXML
    private TableColumn<RDSInstance, Boolean> idleColumn;
    
    private User currentUser;
    private RDSDAO rdsDAO;
    private RDSService rdsService;
    private IdleDetectionService idleDetectionService;
    private ObservableList<RDSInstance> rdsData;
    
    public RDSController() {
        this.rdsDAO = new RDSDAO();
        this.rdsService = new RDSService();
        this.idleDetectionService = new IdleDetectionService();
        this.rdsData = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        setupTableColumns();
        loadRDSInstances();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    private void setupTableColumns() {
        identifierColumn.setCellValueFactory(new PropertyValueFactory<>("dbInstanceIdentifier"));
        classColumn.setCellValueFactory(new PropertyValueFactory<>("dbInstanceClass"));
        engineColumn.setCellValueFactory(new PropertyValueFactory<>("engine"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("dbInstanceStatus"));
        storageColumn.setCellValueFactory(new PropertyValueFactory<>("allocatedStorage"));
        cpuColumn.setCellValueFactory(new PropertyValueFactory<>("cpuUtilization"));
        connectionsColumn.setCellValueFactory(new PropertyValueFactory<>("databaseConnections"));
        idleColumn.setCellValueFactory(new PropertyValueFactory<>("idle"));
        
        rdsTable.setItems(rdsData);
    }
    
    @FXML
    private void handleRefresh() {
        loadRDSInstances();
        showInfo("RDS instances refreshed!");
    }
    
    private void loadRDSInstances() {
        try {
            List<RDSInstance> instances = rdsDAO.getAllRDSInstances();
            rdsData.clear();
            rdsData.addAll(instances);
            
            System.out.println("Loaded " + instances.size() + " RDS instances");
        } catch (Exception e) {
            System.err.println("Error loading RDS instances: " + e.getMessage());
            showError("Error loading RDS instances");
        }
    }
    
    @FXML
    private void handleSyncFromAWS() {
        try {
            List<RDSInstance> instances = rdsService.getAllDBInstances();
            
            for (RDSInstance instance : instances) {
                instance.setUserId(currentUser.getUserId());
                rdsDAO.saveOrUpdateRDSInstance(instance);
            }
            
            loadRDSInstances();
            showInfo("Synced " + instances.size() + " RDS instances from AWS");
        } catch (Exception e) {
            System.err.println("Error syncing RDS instances: " + e.getMessage());
            showError("Error syncing from AWS: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDetectIdle() {
        try {
            idleDetectionService.detectIdleRDSInstances(7, 2);
            loadRDSInstances();
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
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
