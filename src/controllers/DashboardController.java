package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import models.User;
import models.AWSCredential;
import dao.AWSCredentialDAO;
import database.DBConnection;

import java.sql.*;

/**
 * DashboardController  â€“  US-03: Basic Dashboard (local DB data)
 *
 * Structured Spec
 *   Preconditions : user is logged in
 *   Main flow     : query local DB â†’ display resource counts + cost totals
 *   Sprint 1 note : EC2/RDS/ECS rows will be 0 (sync not yet done).
 *                   Billing shows $0.00 until Sprint 2 populates live data.
 *
 * US-01c â€“ Logout is handled by handleLogout().
 */
public class DashboardController {

    /* â”€â”€ FXML bindings â€“ top bar â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    @FXML private Label welcomeLabel;
    @FXML private Label roleBadge;

    /* â”€â”€ Stat bar labels â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    @FXML private Label userCountLabel;
    @FXML private Label credStatusLabel;
    @FXML private Label regionLabel;
    @FXML private Label ec2Label;
    @FXML private Label rdsLabel;
    @FXML private Label ecsLabel;
    @FXML private Label monthlyCostLabel;

    /* â”€â”€ Content area â€“ account card â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    @FXML private Label cardUsername;
    @FXML private Label cardFullName;
    @FXML private Label cardEmail;
    @FXML private Label cardRole;

    /* â”€â”€ Content area â€“ credential card â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    @FXML private Label credCardStatus;
    @FXML private Label credCardRegion;
    @FXML private Label credCardValidated;

    /* â”€â”€ Content area â€“ billing card â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    @FXML private Label billMonthLabel;
    @FXML private Label billTotalLabel;
    @FXML private Label billRecordsLabel;

    /* â”€â”€ Sidebar buttons â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    @FXML private Button btnOverview;
    @FXML private Button btnCredentials;

    /* â”€â”€ Layout â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    @FXML private VBox contentArea;
    @FXML private Label contentTitle;

    private User currentUser;
    private final AWSCredentialDAO awsCredentialDAO = new AWSCredentialDAO();

    @FXML private void initialize() { /* data loaded after setCurrentUser() */ }

    /** Called by LoginController or CredentialsController after scene creation. */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        populateTopBar();
        populateStatBar();
        populateAccountCard();
        populateCredentialCard();
        populateBillingCard();
    }

    /* â”€â”€ Sidebar handlers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */

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
            Stage stage = (Stage) btnCredentials.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool â€“ Credentials");
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
     * US-01c â€“ Logout: clear session and return to login screen.
     */
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(loader.load(), 800, 620);
            Stage stage = (Stage) btnOverview.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool â€“ Login");
            stage.setMinWidth(600);
            stage.setMinHeight(500);
            stage.show();
            System.out.println("âœ“ User logged out: " + currentUser.getUsername());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* â”€â”€ Data population â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */

    private void populateTopBar() {
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        roleBadge.setText(currentUser.getRole());
        boolean isAdmin = "admin".equalsIgnoreCase(currentUser.getRole());
        roleBadge.getStyleClass().setAll(isAdmin ? "badge-success" : "badge-warning");
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
            regionLabel.setText(cred.getRegion());
        } else {
            credStatusLabel.setText("None");
            credStatusLabel.getStyleClass().setAll("stat-value-warning");
            regionLabel.setText("â€”");
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
            credCardRegion.setText("â€”");
            credCardValidated.setText("â€”");
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

    /* â”€â”€ Sidebar active-state helper â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */

    private void setActiveButton(Button active) {
        for (Button b : new Button[]{btnOverview, btnCredentials}) {
            if (b == null) continue;
            b.getStyleClass().setAll(b == active ? "nav-button-active" : "nav-button");
        }
    }

    /* â”€â”€ DB utility helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */

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
        return (s == null || s.isBlank()) ? "â€”" : s;
    }
}
