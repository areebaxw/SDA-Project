package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.fxml.FXMLLoader;
import models.User;
import models.AWSCredential;
import dao.*;
import services.AlertService;
import services.RuleEvaluationService;
import aws.AWSClientFactory;
import aws.BillingService;

/**
 * DashboardController - Main dashboard controller
 * Implements MVC pattern and GRASP Controller pattern
 */
public class DashboardController {
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label ec2HeaderLabel;
    
    @FXML
    private Label totalEC2Label;
    
    @FXML
    private Label rdsHeaderLabel;
    
    @FXML
    private Label totalRDSLabel;
    
    @FXML
    private Label ecsHeaderLabel;
    
    @FXML
    private Label totalECSLabel;
    
    @FXML
    private Label sageMakerHeaderLabel;
    
    @FXML
    private Label totalSageMakerLabel;
    
    @FXML
    private Label alertsHeaderLabel;
    
    @FXML
    private Label totalAlertsLabel;
    
    @FXML
    private Label costHeaderLabel;
    
    @FXML
    private Label costTrendLabel;
    
    @FXML
    private Label totalCreditsHeaderLabel;
    
    @FXML
    private Label totalCreditsLabel;
    
    @FXML
    private VBox contentArea;
    
    // Button references
    @FXML
    private Button ec2Button;
    
    @FXML
    private Button rdsButton;
    
    @FXML
    private Button ecsButton;
    
    @FXML
    private Button sageMakerButton;
    
    @FXML
    private Button rulesButton;
    
    @FXML
    private Button alertsButton;
    
    @FXML
    private Button billingButton;
    
    private User currentUser;
    private EC2DAO ec2DAO;
    private RDSDAO rdsDAO;
    private ECSDAO ecsDAO;
    private SageMakerDAO sageMakerDAO;
    private AlertService alertService;
    
    private Button previousActiveButton;
    private Label previousActiveHeaderLabel;
    private Label previousActiveHeaderValue;
    private static final String ACTIVE_BUTTON_STYLE = "-fx-background-color: #ED7D27; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 13px; -fx-alignment: CENTER_LEFT; -fx-padding: 10;";
    private static final String INACTIVE_BUTTON_STYLE = "-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #FFFFFF; -fx-background-color: #000000; -fx-alignment: CENTER_LEFT; -fx-padding: 10;";
    private static final String ACTIVE_HEADER_LABEL_STYLE = "-fx-text-fill: #ED7D27; -fx-font-size: 12px;";
    private static final String INACTIVE_HEADER_LABEL_STYLE = "-fx-text-fill: #FFFFFF; -fx-font-size: 12px;";
    private static final String ACTIVE_HEADER_VALUE_STYLE = "-fx-text-fill: #ED7D27; -fx-font-size: 24px; -fx-font-weight: bold;";
    private static final String INACTIVE_HEADER_VALUE_STYLE = "-fx-text-fill: #FFFFFF; -fx-font-size: 24px; -fx-font-weight: bold;";
    
    public DashboardController() {
        this.ec2DAO = new EC2DAO();
        this.rdsDAO = new RDSDAO();
        this.ecsDAO = new ECSDAO();
        this.sageMakerDAO = new SageMakerDAO();
        this.alertService = AlertService.getInstance();
    }
    
    @FXML
    private void initialize() {
        // Don't load data here - currentUser is not set yet
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
            // Check if currentUser is set
            if (currentUser == null) {
                System.err.println("Current user is not set yet");
                return;
            }
            
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
            
            // Display credits used this month from AWS
            if (AWSClientFactory.getInstance().isInitialized()) {
                try {
                    BillingService billingService = new BillingService();
                    double monthlyCredits = billingService.getRemainingCredits();
                    
                    if (Double.isNaN(monthlyCredits) || monthlyCredits < 0) {
                        costTrendLabel.setText("$0.00");
                    } else {
                        costTrendLabel.setText(String.format("$%.2f", monthlyCredits));
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching credits from AWS: " + e.getMessage());
                    costTrendLabel.setText("$0.00");
                }
            } else {
                costTrendLabel.setText("$0.00");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setActiveButton(Button activeButton) {
        // Reset previous button to inactive style
        if (previousActiveButton != null) {
            previousActiveButton.setStyle(INACTIVE_BUTTON_STYLE);
        }
        
        // Set current button to active style
        activeButton.setStyle(ACTIVE_BUTTON_STYLE);
        
        // Update previous button reference
        previousActiveButton = activeButton;
    }
    
    private void setActiveHeaderLabel(Label activeHeaderLabel, Label activeValueLabel) {
        // Reset previous header label and value to inactive style
        if (previousActiveHeaderLabel != null) {
            previousActiveHeaderLabel.setStyle(INACTIVE_HEADER_LABEL_STYLE);
        }
        if (previousActiveHeaderValue != null) {
            previousActiveHeaderValue.setStyle(INACTIVE_HEADER_VALUE_STYLE);
        }
        
        // Set current labels to active style
        activeHeaderLabel.setStyle(ACTIVE_HEADER_LABEL_STYLE);
        activeValueLabel.setStyle(ACTIVE_HEADER_VALUE_STYLE);
        
        // Update previous header references
        previousActiveHeaderLabel = activeHeaderLabel;
        previousActiveHeaderValue = activeValueLabel;
    }
    
    private void resetAllHeaderLabels() {
        ec2HeaderLabel.setStyle(INACTIVE_HEADER_LABEL_STYLE);
        totalEC2Label.setStyle(INACTIVE_HEADER_VALUE_STYLE);
        
        rdsHeaderLabel.setStyle(INACTIVE_HEADER_LABEL_STYLE);
        totalRDSLabel.setStyle(INACTIVE_HEADER_VALUE_STYLE);
        
        ecsHeaderLabel.setStyle(INACTIVE_HEADER_LABEL_STYLE);
        totalECSLabel.setStyle(INACTIVE_HEADER_VALUE_STYLE);
        
        sageMakerHeaderLabel.setStyle(INACTIVE_HEADER_LABEL_STYLE);
        totalSageMakerLabel.setStyle(INACTIVE_HEADER_VALUE_STYLE);
        
        alertsHeaderLabel.setStyle(INACTIVE_HEADER_LABEL_STYLE);
        totalAlertsLabel.setStyle(INACTIVE_HEADER_VALUE_STYLE);
        
        costHeaderLabel.setStyle(INACTIVE_HEADER_LABEL_STYLE);
        costTrendLabel.setStyle(INACTIVE_HEADER_VALUE_STYLE);
        
        previousActiveHeaderLabel = null;
        previousActiveHeaderValue = null;
    }
    
    @FXML
    private void handleEC2Monitor() {
        setActiveButton(ec2Button);
        setActiveHeaderLabel(ec2HeaderLabel, totalEC2Label);
        loadView("/views/ec2.fxml", "EC2 Instances Monitor");
    }
    
    @FXML
    private void handleRDSMonitor() {
        setActiveButton(rdsButton);
        setActiveHeaderLabel(rdsHeaderLabel, totalRDSLabel);
        loadView("/views/rds.fxml", "RDS Instances Monitor");
    }
    
    @FXML
    private void handleECSMonitor() {
        setActiveButton(ecsButton);
        setActiveHeaderLabel(ecsHeaderLabel, totalECSLabel);
        loadView("/views/ecs.fxml", "ECS Services Monitor");
    }
    
    @FXML
    private void handleSageMakerMonitor() {
        setActiveButton(sageMakerButton);
        setActiveHeaderLabel(sageMakerHeaderLabel, totalSageMakerLabel);
        loadView("/views/sagemaker.fxml", "SageMaker Endpoints Monitor");
    }
    
    @FXML
    private void handleBillingReports() {
        setActiveButton(billingButton);
        // For Billing, highlight Monthly Cost
        setActiveHeaderLabel(costHeaderLabel, costTrendLabel);
        loadView("/views/billing.fxml", "Billing Reports");
    }
    
    @FXML
    private void handleRulesManagement() {
        setActiveButton(rulesButton);
        // For Rules, reset all labels to white
        resetAllHeaderLabels();
        loadView("/views/rules.fxml", "Governance Rules");
    }
    
    @FXML
    private void handleAlertsView() {
        setActiveButton(alertsButton);
        setActiveHeaderLabel(alertsHeaderLabel, totalAlertsLabel);
        loadView("/views/alerts.fxml", "Alerts Management");
    }
    
    @FXML
    private void handleRefresh() {
        System.out.println("=== Dashboard Refresh Started ===");
        
        // Evaluate all active rules and generate alerts
        System.out.println("Running rule evaluation...");
        try {
            RuleEvaluationService ruleEvaluationService = new RuleEvaluationService();
            ruleEvaluationService.evaluateAllRules();
        } catch (Exception e) {
            System.err.println("Error during rule evaluation: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Reload dashboard data to reflect any new alerts
        loadDashboardData();
        
        System.out.println("=== Dashboard Refresh Completed ===");
        showInfo("Dashboard refreshed and rules evaluated successfully!");
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