package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import models.User;
import models.AWSCredential;
import dao.*;
import services.AlertService;
import aws.AWSClientFactory;

/**
 * DashboardController - Main dashboard controller
 * Implements MVC pattern and GRASP Controller pattern
 */
public class DashboardController {
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label totalEC2Label;
    
    @FXML
    private Label totalRDSLabel;
    
    @FXML
    private Label totalECSLabel;
    
    @FXML
    private Label totalSageMakerLabel;
    
    @FXML
    private Label totalAlertsLabel;
    
    @FXML
    private Label costTrendLabel;
    
    @FXML
    private VBox contentArea;
    
    private User currentUser;
    private EC2DAO ec2DAO;
    private RDSDAO rdsDAO;
    private ECSDAO ecsDAO;
    private SageMakerDAO sageMakerDAO;
    private AlertService alertService;
    
    public DashboardController() {
        this.ec2DAO = new EC2DAO();
        this.rdsDAO = new RDSDAO();
        this.ecsDAO = new ECSDAO();
        this.sageMakerDAO = new SageMakerDAO();
        this.alertService = AlertService.getInstance();
    }
    
    @FXML
    private void initialize() {
        loadDashboardData();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getFullName());
        
        // Load AWS credentials from database
        loadAWSCredentials();
        
        loadDashboardData();
    }
    
    private void loadAWSCredentials() {
        try {
            AWSCredentialDAO credentialDAO = new AWSCredentialDAO();
            AWSCredential credentials = credentialDAO.getActiveCredentials(currentUser.getUserId());
            
            if (credentials != null) {
                // Initialize AWS client factory with credentials
                AWSClientFactory factory = AWSClientFactory.getInstance();
                factory.initializeCredentials(
                    credentials.getAccessKey(),
                    credentials.getSecretKey(),
                    credentials.getRegion()
                );
                
                // Validate credentials
                if (factory.validateCredentials()) {
                    System.out.println("✓ AWS credentials loaded and validated successfully");
                    System.out.println("  Region: " + credentials.getRegion());
                } else {
                    System.err.println("✗ AWS credentials validation failed");
                    showAlert("Warning", "AWS credentials could not be validated. Some features may not work.");
                }
            } else {
                System.err.println("✗ No AWS credentials found for user");
                showAlert("Warning", "No AWS credentials configured. Please configure your AWS credentials.");
            }
        } catch (Exception e) {
            System.err.println("✗ Error loading AWS credentials: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void loadDashboardData() {
        try {
            // Load resource counts
            int ec2Count = ec2DAO.getTotalEC2Count();
            int rdsCount = rdsDAO.getTotalRDSCount();
            int ecsCount = ecsDAO.getTotalECSCount();
            int sageMakerCount = sageMakerDAO.getTotalEndpointCount();
            int alertCount = alertService.getTotalAlertCount(true);
            
            // Update labels
            totalEC2Label.setText(String.valueOf(ec2Count));
            totalRDSLabel.setText(String.valueOf(rdsCount));
            totalECSLabel.setText(String.valueOf(ecsCount));
            totalSageMakerLabel.setText(String.valueOf(sageMakerCount));
            totalAlertsLabel.setText(String.valueOf(alertCount));
            
            // Set cost trend (placeholder)
            costTrendLabel.setText("$545.75");
            
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleEC2Monitor() {
        loadView("/views/ec2.fxml", "EC2 Instances Monitor");
    }
    
    @FXML
    private void handleRDSMonitor() {
        loadView("/views/rds.fxml", "RDS Instances Monitor");
    }
    
    @FXML
    private void handleECSMonitor() {
        loadView("/views/ecs.fxml", "ECS Services Monitor");
    }
    
    @FXML
    private void handleSageMakerMonitor() {
        loadView("/views/sagemaker.fxml", "SageMaker Endpoints Monitor");
    }
    
    @FXML
    private void handleBillingReports() {
        loadView("/views/billing.fxml", "Billing Reports");
    }
    
    @FXML
    private void handleRulesManagement() {
        loadView("/views/rules.fxml", "Governance Rules");
    }
    
    @FXML
    private void handleAlertsView() {
        loadView("/views/alerts.fxml", "Alerts Management");
    }
    
    @FXML
    private void handleRefresh() {
        loadDashboardData();
        showInfo("Dashboard refreshed successfully!");
    }
    
    private void loadView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            VBox view = loader.load();
            
            // Pass current user to the loaded controller
            Object controller = loader.getController();
            if (controller instanceof EC2Controller) {
                ((EC2Controller) controller).setCurrentUser(currentUser);
            } else if (controller instanceof RDSController) {
                ((RDSController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof ECSController) {
                ((ECSController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof SageMakerController) {
                ((SageMakerController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof BillingController) {
                ((BillingController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof RuleController) {
                ((RuleController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof AlertController) {
                ((AlertController) controller).setCurrentUser(currentUser);
            }
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            
        } catch (Exception e) {
            System.err.println("Error loading view: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading " + title);
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
