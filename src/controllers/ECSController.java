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
