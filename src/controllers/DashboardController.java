package controllers;

import dao.AWSCredentialDAO;
import aws.AWSClientFactory;
import database.DBConnection;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.AWSCredential;
import models.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * DashboardController - revamped monitoring dashboard
 * Monitors EC2, S3, SQS, and ALB resources.
 */
public class DashboardController {

    @FXML private Button btnMenuToggle;
    @FXML private VBox drawerPane;
    @FXML private Pane drawerOverlay;
    @FXML private Label roleBadge;
    @FXML private Label drawerUsername;
    @FXML private Label drawerRole;

    @FXML private Label credStatusLabel;
    @FXML private Label ec2Label;
    @FXML private Label s3Label;
    @FXML private Label sqsLabel;
    @FXML private Label albLabel;
    @FXML private Label monthlyCostLabel;

    @FXML private Label cardUsername;
    @FXML private Label cardFullName;
    @FXML private Label cardEmail;
    @FXML private Label cardRole;

    @FXML private Label credCardStatus;
    @FXML private Label credCardRegion;
    @FXML private Label credCardValidated;

    @FXML private Label billMonthLabel;
    @FXML private Label billTotalLabel;
    @FXML private Label billRecordsLabel;

    @FXML private Button btnOverview;
    @FXML private Button btnEC2;
    @FXML private Button btnS3;
    @FXML private Button btnSQS;
    @FXML private Button btnALB;
    @FXML private Button btnBilling;
    @FXML private Button btnRules;
    @FXML private Button btnAlerts;
    @FXML private Button btnCredentials;

    private boolean drawerOpen = false;
    private static final double DRAWER_WIDTH = 260;

    private User currentUser;
    private final AWSCredentialDAO awsCredentialDAO = new AWSCredentialDAO();

    @FXML
    private void initialize() {
        // Data loads after setCurrentUser() is called.
    }

    @FXML
    private void handleDrawerToggle() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(280), drawerPane);
        if (!drawerOpen) {
            drawerOverlay.setVisible(true);
            drawerOverlay.setManaged(true);
            tt.setToX(0);
            tt.play();
            drawerOpen = true;
        } else {
            tt.setToX(-DRAWER_WIDTH);
            tt.setOnFinished(e -> {
                drawerOverlay.setVisible(false);
                drawerOverlay.setManaged(false);
            });
            tt.play();
            drawerOpen = false;
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        initializeAwsClientFromSavedCredentials();
        populateTopBar();
        populateStatBar();
        populateAccountCard();
        populateCredentialCard();
        populateBillingCard();
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

    @FXML
    private void handleOverview() {
        setActiveButton(btnOverview);
    }

    @FXML
    private void handleCredentials() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/credentials.fxml"));
            Scene scene = new Scene(loader.load(), 860, 680);
            CredentialsController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) btnMenuToggle.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool - Credentials");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleEC2() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ec2.fxml"));
            Scene scene = new Scene(loader.load(), 980, 700);
            EC2Controller ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) btnMenuToggle.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool - EC2 Monitoring");
            stage.show();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Unable to open EC2 Monitoring");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleS3() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/s3.fxml"));
            Scene scene = new Scene(loader.load(), 980, 700);
            S3Controller ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) btnMenuToggle.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool - S3 Monitoring");
            stage.show();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Unable to open S3 Monitoring");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleSQS() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/sqs.fxml"));
            Scene scene = new Scene(loader.load(), 980, 700);
            SQSController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) btnMenuToggle.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool - SQS Monitoring");
            stage.show();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Unable to open SQS Monitoring");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleALB() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/alb.fxml"));
            Scene scene = new Scene(loader.load(), 980, 700);
            ALBController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) btnMenuToggle.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool - ALB Monitoring");
            stage.show();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Unable to open ALB Monitoring");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleBilling() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/billing.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 760);
            BillingController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) btnMenuToggle.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool - Billing Reports");
            stage.show();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Unable to open Billing Reports");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleRules() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/rules.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 760);
            RuleController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) btnMenuToggle.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool - Rules");
            stage.show();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Unable to open Rules");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleAlerts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/alerts.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 760);
            AlertController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) btnMenuToggle.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool - Alerts");
            stage.show();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Unable to open Alerts");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleRefresh() {
        if (currentUser != null) {
            setCurrentUser(currentUser);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(loader.load(), 800, 620);
            Stage stage = (Stage) btnMenuToggle.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool - Login");
            stage.setMinWidth(600);
            stage.setMinHeight(500);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void populateTopBar() {
        if (roleBadge != null) {
            roleBadge.setText(currentUser.getRole());
            boolean isAdmin = "admin".equalsIgnoreCase(currentUser.getRole());
            roleBadge.getStyleClass().setAll(isAdmin ? "badge-success" : "badge-warning");
        }

        if (drawerUsername != null) {
            drawerUsername.setText(currentUser.getUsername());
        }
        if (drawerRole != null) {
            drawerRole.setText(currentUser.getRole().toUpperCase());
        }
    }

    private void populateStatBar() {
        AWSCredential cred = awsCredentialDAO.getActiveCredentials(currentUser.getUserId());
        if (cred != null) {
            credStatusLabel.setText("Saved");
            credStatusLabel.getStyleClass().setAll("stat-value-success");
        } else {
            credStatusLabel.setText("None");
            credStatusLabel.getStyleClass().setAll("stat-value-warning");
        }

        ec2Label.setText(String.valueOf(queryInt("SELECT COUNT(*) FROM ec2_instances")));
        s3Label.setText(String.valueOf(queryInt("SELECT COUNT(*) FROM s3_buckets")));
        sqsLabel.setText(String.valueOf(queryInt("SELECT COUNT(*) FROM sqs_queues")));
        albLabel.setText(String.valueOf(queryInt("SELECT COUNT(*) FROM alb_resources")));

        double monthly = queryDouble(
                "SELECT IFNULL(SUM(cost_amount),0) FROM billing_records " +
                "WHERE MONTH(start_date)=MONTH(NOW()) AND YEAR(start_date)=YEAR(NOW())");
        monthlyCostLabel.setText(String.format("$%.2f", monthly));
    }

    private void populateAccountCard() {
        cardUsername.setText(currentUser.getUsername());
        cardFullName.setText(currentUser.getFullName());
        cardEmail.setText(nvl(currentUser.getEmail()));
        cardRole.setText(currentUser.getRole());
    }

    private void populateCredentialCard() {
        AWSCredential cred = awsCredentialDAO.getActiveCredentials(currentUser.getUserId());
        if (cred != null) {
            credCardStatus.setText("Saved");
            credCardStatus.getStyleClass().setAll("badge-success");
            credCardRegion.setText(cred.getRegion());
            credCardValidated.setText("Validated");
            credCardValidated.getStyleClass().setAll("badge-success");
        } else {
            credCardStatus.setText("Not configured");
            credCardStatus.getStyleClass().setAll("badge-error");
            credCardRegion.setText("-");
            credCardValidated.setText("Pending");
            credCardValidated.getStyleClass().setAll("badge-warning");
        }
    }

    private void populateBillingCard() {
        double monthly = queryDouble(
                "SELECT IFNULL(SUM(cost_amount),0) FROM billing_records " +
                "WHERE MONTH(start_date)=MONTH(NOW()) AND YEAR(start_date)=YEAR(NOW())");
        double total = queryDouble("SELECT IFNULL(SUM(cost_amount),0) FROM billing_records");
        int records = queryInt("SELECT COUNT(*) FROM billing_records");

        billMonthLabel.setText(String.format("$%.2f", monthly));
        billTotalLabel.setText(String.format("$%.2f", total));
        billRecordsLabel.setText(String.valueOf(records));
    }

    private void setActiveButton(Button active) {
        for (Button b : new Button[]{
                btnOverview,
                btnEC2,
                btnS3,
                btnSQS,
                btnALB,
                btnBilling,
                btnRules,
                btnAlerts,
                btnCredentials
        }) {
            if (b == null) continue;
            b.getStyleClass().setAll(b == active ? "drawer-nav-active" : "drawer-nav");
        }
    }

    private int queryInt(String sql) {
        try (Connection c = DBConnection.getInstance().getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getInt(1) : 0;
        } catch (Exception e) {
            System.err.println("queryInt failed [" + sql + "]: " + e.getMessage());
            return 0;
        }
    }

    private double queryDouble(String sql) {
        try (Connection c = DBConnection.getInstance().getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getDouble(1) : 0.0;
        } catch (Exception e) {
            System.err.println("queryDouble failed [" + sql + "]: " + e.getMessage());
            return 0.0;
        }
    }

    private String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
