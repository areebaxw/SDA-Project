package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.User;
import models.SageMakerEndpoint;
import dao.SageMakerDAO;
import aws.SageMakerAWSService;
import services.IdleDetectionService;

import java.util.List;

/**
 * SageMakerController - Controller for SageMaker endpoints view
 */
public class SageMakerController {
    @FXML
    private TableView<SageMakerEndpoint> sageMakerTable;
    
    @FXML
    private TableColumn<SageMakerEndpoint, String> endpointNameColumn;
    
    @FXML
    private TableColumn<SageMakerEndpoint, String> statusColumn;
    
    @FXML
    private TableColumn<SageMakerEndpoint, String> modelColumn;
    
    @FXML
    private TableColumn<SageMakerEndpoint, String> instanceTypeColumn;
    
    @FXML
    private TableColumn<SageMakerEndpoint, Integer> invocationsColumn;
    
    @FXML
    private TableColumn<SageMakerEndpoint, Boolean> idleColumn;
    
    private User currentUser;
    private SageMakerDAO sageMakerDAO;
    private SageMakerAWSService sageMakerAWSService;
    private IdleDetectionService idleDetectionService;
    private ObservableList<SageMakerEndpoint> sageMakerData;
    
    public SageMakerController() {
        this.sageMakerDAO = new SageMakerDAO();
        this.sageMakerAWSService = new SageMakerAWSService();
        this.idleDetectionService = new IdleDetectionService();
        this.sageMakerData = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        setupTableColumns();
        loadSageMakerEndpoints();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    private void setupTableColumns() {
        endpointNameColumn.setCellValueFactory(new PropertyValueFactory<>("endpointName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("endpointStatus"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("modelName"));
        instanceTypeColumn.setCellValueFactory(new PropertyValueFactory<>("instanceType"));
        invocationsColumn.setCellValueFactory(new PropertyValueFactory<>("invocations"));
        idleColumn.setCellValueFactory(new PropertyValueFactory<>("idle"));
        
        sageMakerTable.setItems(sageMakerData);
    }
    
    @FXML
    private void handleRefresh() {
        loadSageMakerEndpoints();
        showInfo("SageMaker endpoints refreshed!");
    }
    
    private void loadSageMakerEndpoints() {
        try {
            List<SageMakerEndpoint> endpoints = sageMakerDAO.getAllEndpoints();
            sageMakerData.clear();
            sageMakerData.addAll(endpoints);
            
            System.out.println("Loaded " + endpoints.size() + " SageMaker endpoints");
        } catch (Exception e) {
            System.err.println("Error loading SageMaker endpoints: " + e.getMessage());
            showError("Error loading SageMaker endpoints");
        }
    }
    
    @FXML
    private void handleSyncFromAWS() {
        try {
          
            int syncedCount = sageMakerAWSService.syncFromAWS(currentUser.getUserId());
            
          
            loadSageMakerEndpoints();
            
            if (syncedCount > 0) {
                showInfo("Synced " + syncedCount + " SageMaker endpoints from AWS");
            } else {
                showInfo("No SageMaker endpoints found in your AWS account.");
            }
        } catch (Exception e) {
            System.err.println("Error syncing SageMaker endpoints: " + e.getMessage());
            e.printStackTrace();
            showError("Error syncing from AWS: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDetectIdle() {
        try {
            idleDetectionService.detectIdleSageMakerEndpoints(7, 10);
            loadSageMakerEndpoints();
            showInfo("Idle detection completed!");
        } catch (Exception e) {
            System.err.println("Error detecting idle endpoints: " + e.getMessage());
            showError("Error detecting idle endpoints");
        }
    }
    
    @FXML
    private void handleStart() {
        SageMakerEndpoint selected = sageMakerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a SageMaker resource");
            return;
        }
        
        if ("notebook".equals(selected.getResourceType())) {
            boolean success = sageMakerAWSService.startNotebookInstance(selected.getEndpointName());
            if (success) {
                showInfo("Notebook instance " + selected.getEndpointName() + " is starting");
                handleRefresh();
            } else {
                showError("Failed to start notebook instance");
            }
        } else {
            showWarning("Only notebook instances can be started. Endpoints are always running.");
        }
    }
    
    @FXML
    private void handleStop() {
        SageMakerEndpoint selected = sageMakerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a SageMaker resource");
            return;
        }
        
        if ("notebook".equals(selected.getResourceType())) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Stop");
            confirmation.setHeaderText("Stop Notebook Instance");
            confirmation.setContentText("Are you sure you want to stop " + selected.getEndpointName() + "?");
            
            if (confirmation.showAndWait().get() == ButtonType.OK) {
                boolean success = sageMakerAWSService.stopNotebookInstance(selected.getEndpointName());
                if (success) {
                    showInfo("Notebook instance " + selected.getEndpointName() + " is stopping");
                    handleRefresh();
                } else {
                    showError("Failed to stop notebook instance");
                }
            }
        } else {
            showWarning("Only notebook instances can be stopped. To stop an endpoint, delete it.");
        }
    }
    
    @FXML
    private void handleDelete() {
        SageMakerEndpoint selected = sageMakerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a SageMaker resource");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete SageMaker Resource");
        confirmation.setContentText("⚠️ WARNING: This will PERMANENTLY DELETE " + selected.getEndpointName() + "!\nAre you sure?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success;
            if ("notebook".equals(selected.getResourceType())) {
                success = sageMakerAWSService.deleteNotebookInstance(selected.getEndpointName());
            } else {
                success = sageMakerAWSService.deleteEndpoint(selected.getEndpointName());
            }
            
            if (success) {
                showInfo("SageMaker resource " + selected.getEndpointName() + " is being deleted");
                handleRefresh();
            } else {
                showError("Failed to delete SageMaker resource");
            }
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
