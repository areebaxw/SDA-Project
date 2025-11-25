package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.User;
import models.ECSService;
import dao.ECSDAO;
import aws.ECSAWSService;

import java.util.List;

/**
 * ECSController - Controller for ECS services view
 */
public class ECSController {
    @FXML
    private TableView<ECSService> ecsTable;
    
    @FXML
    private TableColumn<ECSService, String> clusterColumn;
    
    @FXML
    private TableColumn<ECSService, String> serviceColumn;
    
    @FXML
    private TableColumn<ECSService, String> statusColumn;
    
    @FXML
    private TableColumn<ECSService, Integer> desiredColumn;
    
    @FXML
    private TableColumn<ECSService, Integer> runningColumn;
    
    @FXML
    private TableColumn<ECSService, Double> cpuColumn;
    
    @FXML
    private TableColumn<ECSService, Boolean> idleColumn;
    
    private User currentUser;
    private ECSDAO ecsDAO;
    private ECSAWSService ecsAWSService;
    private ObservableList<ECSService> ecsData;
    
    public ECSController() {
        this.ecsDAO = new ECSDAO();
        this.ecsAWSService = new ECSAWSService();
        this.ecsData = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        setupTableColumns();
        loadECSServices();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    private void setupTableColumns() {
        clusterColumn.setCellValueFactory(new PropertyValueFactory<>("clusterName"));
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        desiredColumn.setCellValueFactory(new PropertyValueFactory<>("desiredCount"));
        runningColumn.setCellValueFactory(new PropertyValueFactory<>("runningCount"));
        cpuColumn.setCellValueFactory(new PropertyValueFactory<>("cpuUtilization"));
        idleColumn.setCellValueFactory(new PropertyValueFactory<>("idle"));
        
        ecsTable.setItems(ecsData);
    }
    
    @FXML
    private void handleRefresh() {
        loadECSServices();
        showInfo("ECS services refreshed!");
    }
    
    private void loadECSServices() {
        try {
            List<ECSService> services = ecsDAO.getAllECSServices();
            ecsData.clear();
            ecsData.addAll(services);
            
            System.out.println("Loaded " + services.size() + " ECS services");
        } catch (Exception e) {
            System.err.println("Error loading ECS services: " + e.getMessage());
            showError("Error loading ECS services");
        }
    }
    
    @FXML
    private void handleSyncFromAWS() {
        try {
            List<ECSService> services = ecsAWSService.getAllECSServices();
            
            for (ECSService service : services) {
                service.setUserId(currentUser.getUserId());
                ecsDAO.saveOrUpdateECSService(service);
            }
            
            loadECSServices();
            showInfo("Synced " + services.size() + " ECS services from AWS");
        } catch (Exception e) {
            System.err.println("Error syncing ECS services: " + e.getMessage());
            showError("Error syncing from AWS: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleStart() {
        ECSService selected = ecsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an ECS service");
            return;
        }
        
        // Prompt for desired count
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Start ECS Service");
        dialog.setHeaderText("Start ECS Service: " + selected.getServiceName());
        dialog.setContentText("Enter desired task count:");
        
        dialog.showAndWait().ifPresent(count -> {
            try {
                int desiredCount = Integer.parseInt(count);
                boolean success = ecsAWSService.startService(selected.getClusterName(), selected.getServiceName(), desiredCount);
                if (success) {
                    showInfo("ECS service " + selected.getServiceName() + " is starting with " + desiredCount + " tasks");
                    handleRefresh();
                } else {
                    showError("Failed to start ECS service");
                }
            } catch (NumberFormatException e) {
                showError("Invalid number format");
            }
        });
    }
    
    @FXML
    private void handleStop() {
        ECSService selected = ecsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an ECS service");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Stop");
        confirmation.setHeaderText("Stop ECS Service");
        confirmation.setContentText("Are you sure you want to stop " + selected.getServiceName() + "? This will set desired count to 0.");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = ecsAWSService.stopService(selected.getClusterName(), selected.getServiceName());
            if (success) {
                showInfo("ECS service " + selected.getServiceName() + " is stopping");
                handleRefresh();
            } else {
                showError("Failed to stop ECS service");
            }
        }
    }
    
    @FXML
    private void handleDelete() {
        ECSService selected = ecsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an ECS service");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete ECS Service");
        confirmation.setContentText("⚠️ WARNING: This will PERMANENTLY DELETE " + selected.getServiceName() + "!\nAre you sure?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = ecsAWSService.deleteService(selected.getClusterName(), selected.getServiceName());
            if (success) {
                showInfo("ECS service " + selected.getServiceName() + " is being deleted");
                handleRefresh();
            } else {
                showError("Failed to delete ECS service");
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
