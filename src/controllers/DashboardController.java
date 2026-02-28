package controllers;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.util.Duration;
import models.User;
import models.AWSCredential;
import dao.AWSCredentialDAO;
import database.DBConnection;

import java.sql.*;

/**
 * DashboardController  Ã¢â‚¬â€œ  US-03: Basic Dashboard (local DB data)
 *
 * Structured Spec
 *   Preconditions : user is logged in
 *   Main flow     : query local DB Ã¢â€ â€™ display resource counts + cost totals
 *   Sprint 1 note : EC2/RDS/ECS rows will be 0 (sync not yet done).
 *                   Billing shows $0.00 until Sprint 2 populates live data.
 *
 * US-01c Ã¢â‚¬â€œ Logout is handled by handleLogout().
 */
public class DashboardController {

    /* Ã¢â€â‚¬Ã¢â€â‚¬ FXML bindings Ã¢â‚¬â€œ top bar Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬ */
    @FXML private Button btnMenuToggle;
    @FXML private VBox   drawerPane;
    @FXML private Pane   drawerOverlay;
    @FXML private Label  welcomeLabel;
    @FXML private Label  roleBadge;
    @FXML private Label  drawerUsername;
    @FXML private Label  drawerRole;

    private boolean drawerOpen = false;
    private static final double DRAWER_WIDTH = 260;

    /* Ã¢â€â‚¬Ã¢â€â‚¬ Stat bar labels Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬ */
    @FXML private Label userCountLabel;
    @FXML private Label credStatusLabel;
    @FXML private Label ec2Label;
    @FXML private Label rdsLabel;
    @FXML private Label ecsLabel;
    @FXML private Label monthlyCostLabel;

    /* Ã¢â€â‚¬Ã¢â€â‚¬ Content area Ã¢â‚¬â€œ account card Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬ */
    @FXML private Label cardUsername;
    @FXML private Label cardFullName;
    @FXML private Label cardEmail;
    @FXML private Label cardRole;

    /* Ã¢â€â‚¬Ã¢â€â‚¬ Content area Ã¢â‚¬â€œ credential card Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬ */
    @FXML private Label credCardStatus;
    @FXML private Label credCardRegion;
    @FXML private Label credCardValidated;

    /* Ã¢â€â‚¬Ã¢â€â‚¬ Content area Ã¢â‚¬â€œ billing card Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬ */
    @FXML private Label billMonthLabel;
    @FXML private Label billTotalLabel;
    @FXML private Label billRecordsLabel;

    /* Ã¢â€â‚¬Ã¢â€â‚¬ Sidebar buttons Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬ */
    @FXML private Button btnOverview;
    @FXML private Button btnCredentials;

    /* Ã¢â€â‚¬Ã¢â€â‚¬ Layout Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬ */
    @FXML private VBox contentArea;
    @FXML private Label contentTitle;

    private User currentUser;
    private final AWSCredentialDAO awsCredentialDAO = new AWSCredentialDAO();

    @FXML
    private void initialize() { /* data loaded after setCurrentUser() */ }

    /** Toggle the overlay drawer open/closed with a slide animation. */
    @FXML
    private void handleDrawerToggle() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(280), drawerPane);
        if (!drawerOpen) {
            // Show overlay
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

    /** Called by LoginController or CredentialsController after scene creation. */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        populateTopBar();
        populateStatBar();
        populateAccountCard();
        populateCredentialCard();
        populateBillingCard();
    }

    /* Ã¢â€â‚¬Ã¢â€â‚¬ Sidebar handlers Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬ */

    @FXML
    private void handleOverview() {
        setActiveButton(btnOverview);
    }

    /** US-02: Navigate to credentials setup from inside the dashboard. */
    @FXML
    private void handleCredentials() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/credentials.fxml"));
            Scene scene = new Scene(loader.load(), 860, 680);
            CredentialsController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) btnMenuToggle.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool Ã¢â‚¬â€œ Credentials");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        if (currentUser != null) setCurrentUser(currentUser);
    }

    /**
     * US-01c Ã¢â‚¬â€œ Logout: clear session and return to login screen.
     */
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(loader.load(), 800, 620);
            Stage stage = (Stage) btnMenuToggle.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool Ã¢â‚¬â€œ Login");
            stage.setMinWidth(600);
            stage.setMinHeight(500);
            stage.show();
            System.out.println("Ã¢Å“â€œ User logged out: " + currentUser.getUsername());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* Ã¢â€â‚¬Ã¢â€â‚¬ Data population Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬ */

    private void populateTopBar() {
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        roleBadge.setText(currentUser.getRole());
        boolean isAdmin = "admin".equalsIgnoreCase(currentUser.getRole());
        roleBadge.getStyleClass().setAll(isAdmin ? "badge-success" : "badge-warning");
        if (drawerUsername != null) drawerUsername.setText(currentUser.getUsername());
        if (drawerRole != null) drawerRole.setText(currentUser.getRole().toUpperCase());
    }

    private void populateStatBar() {
        // User count from DB
        int userCount = queryInt("SELECT COUNT(*) FROM users");
        userCountLabel.setText(String.valueOf(userCount));

        // Credential status
        AWSCredential cred = awsCredentialDAO.getActiveCredentials(currentUser.getUserId());
        if (cred != null) {
            credStatusLabel.setText("Saved");
            credStatusLabel.getStyleClass().setAll("stat-value-success");
        } else {
            credStatusLabel.setText("None");
            credStatusLabel.getStyleClass().setAll("stat-value-warning");
        }

        // US-03a: resource counts from local tables
        ec2Label.setText(String.valueOf(queryInt("SELECT COUNT(*) FROM ec2_instances")));
        rdsLabel.setText(String.valueOf(queryInt("SELECT COUNT(*) FROM rds_instances")));
        ecsLabel.setText(String.valueOf(queryInt("SELECT COUNT(*) FROM ecs_services")));

        // Monthly cost from billing table (will be $0 until Sprint 2 syncs live data)
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
            credCardValidated.setText("Pending (Sprint 2)");
            credCardValidated.getStyleClass().setAll("badge-warning");
        } else {
            credCardStatus.setText("Not configured");
            credCardStatus.getStyleClass().setAll("badge-error");
            credCardRegion.setText("Ã¢â‚¬â€");
            credCardValidated.setText("Ã¢â‚¬â€");
        }
    }

    private void populateBillingCard() {
        double monthly = queryDouble(
            "SELECT IFNULL(SUM(cost_amount),0) FROM billing_records " +
            "WHERE MONTH(start_date)=MONTH(NOW()) AND YEAR(start_date)=YEAR(NOW())");
        double total   = queryDouble("SELECT IFNULL(SUM(cost_amount),0) FROM billing_records");
        int    records = queryInt  ("SELECT COUNT(*) FROM billing_records");

        billMonthLabel.setText(String.format("$%.2f", monthly));
        billTotalLabel.setText(String.format("$%.2f", total));
        billRecordsLabel.setText(String.valueOf(records));
    }

    /* Ã¢â€â‚¬Ã¢â€â‚¬ Sidebar active-state helper Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬ */

    private void setActiveButton(Button active) {
        for (Button b : new Button[]{btnOverview, btnCredentials}) {
            if (b == null) continue;
            b.getStyleClass().setAll(b == active ? "nav-button-active" : "nav-button");
        }
    }

    /* Ã¢â€â‚¬Ã¢â€â‚¬ DB utility helpers Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬ */

    private int queryInt(String sql) {
        try (Connection c = DBConnection.getInstance().getConnection();
             Statement  s = c.createStatement();
             ResultSet  r = s.executeQuery(sql)) {
            return r.next() ? r.getInt(1) : 0;
        } catch (Exception e) {
            System.err.println("queryInt failed [" + sql + "]: " + e.getMessage());
            return 0;
        }
    }

    private double queryDouble(String sql) {
        try (Connection c = DBConnection.getInstance().getConnection();
             Statement  s = c.createStatement();
             ResultSet  r = s.executeQuery(sql)) {
            return r.next() ? r.getDouble(1) : 0.0;
        } catch (Exception e) {
            System.err.println("queryDouble failed [" + sql + "]: " + e.getMessage());
            return 0.0;
        }
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "Ã¢â‚¬â€" : s;
    }
}
