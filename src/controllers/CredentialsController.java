package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import aws.AWSClientFactory;
import models.AWSCredential;
import models.User;
import dao.AWSCredentialDAO;
import utils.Validator;

import java.util.List;

/**
 * CredentialsController  –  US-02: AWS Credential Setup (DB only, Sprint 1)
 *
 * Structured Spec
 *   Preconditions : user is logged in
 *   Main flow     : input keys → (optional mock test) → encrypt + store in DB → go to dashboard
 *   US-02c        : "Test Connection" shows mock success; real AWS SDK validation is Sprint 2
 */
public class CredentialsController {

    /* ── FXML bindings ───────────────────────────── */
    @FXML private TextField     accessKeyField;
    @FXML private PasswordField secretKeyField;
    @FXML private TextField     secretKeyVisibleField;
    @FXML private ComboBox<String> regionComboBox;

    @FXML private Button saveButton;
    @FXML private Button testButton;

    @FXML private VBox  statusBox;
    @FXML private HBox  statusStrip;
    @FXML private Label statusIcon;
    @FXML private Label statusLabel;
    @FXML private Label errorLabel;

    private boolean secretVisible = false;
    private User currentUser;
    private final AWSCredentialDAO awsCredentialDAO = new AWSCredentialDAO();

    /* ── FXML regions ────────────────────────────── */
    private static final List<String> AWS_REGIONS = List.of(
        "us-east-1", "us-east-2", "us-west-1", "us-west-2",
        "eu-west-1", "eu-west-2", "eu-west-3", "eu-central-1",
        "ap-south-1", "ap-southeast-1", "ap-southeast-2",
        "ap-northeast-1", "ap-northeast-2",
        "sa-east-1", "ca-central-1",
        "me-south-1", "af-south-1"
    );

    @FXML
    private void initialize() {
        hideError();
        hideStatus();

        regionComboBox.setItems(FXCollections.observableArrayList(AWS_REGIONS));
        regionComboBox.setValue("us-east-1");

        // Keep secret key fields in sync
        secretKeyVisibleField.textProperty()
                             .bindBidirectional(secretKeyField.textProperty());
        secretKeyVisibleField.setVisible(false);
        secretKeyVisibleField.setManaged(false);
    }

    /** Called by LoginController / SignupController after they create the scene. */
    public void setCurrentUser(User user) {
        this.currentUser = user;

        // Pre-fill existing credentials if any
        AWSCredential existing = awsCredentialDAO.getActiveCredentials(user.getUserId());
        if (existing != null) {
            accessKeyField.setText(existing.getAccessKey());
            regionComboBox.setValue(existing.getRegion());
        }
    }

    /* ── Actions ─────────────────────────────────── */

    /**
     * US-02c – Mock test connection.
     * Sprint 1 only validates field formats locally.
     * Real AWS SDK call (STS GetCallerIdentity) is planned for Sprint 2.
     */
    @FXML
    private void handleTestConnection() {
        hideStatus();
        hideError();

        String accessKey = accessKeyField.getText().trim();
        String secretKey = secretKeyField.getText().trim();
        String region    = regionComboBox.getValue();

        if (!validateInputs(accessKey, secretKey, region)) return;

        // Sprint 1: format-check only → always "OK"
        showStatus(true,
            "✓ Format check passed  –  Real AWS connection test will run in Sprint 2.");
        System.out.println("[Sprint 1] Mock connection test OK for region: " + region);
    }

    /** US-02b – Encrypt and store credentials in DB linked to the current user. */
    @FXML
    private void handleSave() {
        hideStatus();
        hideError();

        String accessKey = accessKeyField.getText().trim();
        String secretKey = secretKeyField.getText().trim();
        String region    = regionComboBox.getValue();

        if (!validateInputs(accessKey, secretKey, region)) return;

        AWSCredential cred = new AWSCredential(
            currentUser.getUserId(), accessKey, secretKey, region);
        cred.setActive(true);
        cred.setValidated(false);   // real validation in Sprint 2

        boolean saved = awsCredentialDAO.saveCredentials(cred);
        if (!saved) {
            showError("Failed to save credentials. Please try again.");
            return;
        }

        AWSClientFactory.getInstance().initializeCredentials(accessKey, secretKey, region);

        System.out.println("✓ AWS credentials saved for user: " + currentUser.getUsername());
        openDashboard();
    }

    /** US-01c-adjacent: skip credential setup for now and go straight to dashboard. */
    @FXML
    private void handleSkip() {
        openDashboard();
    }

    @FXML
    private void toggleSecretKey() {
        secretVisible = !secretVisible;
        if (secretVisible) {
            secretKeyVisibleField.setVisible(true);
            secretKeyVisibleField.setManaged(true);
            secretKeyField.setVisible(false);
            secretKeyField.setManaged(false);
        } else {
            secretKeyField.setVisible(true);
            secretKeyField.setManaged(true);
            secretKeyVisibleField.setVisible(false);
            secretKeyVisibleField.setManaged(false);
        }
    }

    /* ── Validation ──────────────────────────────── */

    private boolean validateInputs(String accessKey, String secretKey, String region) {
        if (!Validator.isNotEmpty(accessKey)) {
            showError("Please enter your AWS Access Key ID.");
            return false;
        }
        if (!Validator.isNotEmpty(secretKey)) {
            showError("Please enter your AWS Secret Access Key.");
            return false;
        }
        if (region == null || region.isBlank()) {
            showError("Please select an AWS region.");
            return false;
        }
        return true;
    }

    /* ── Navigation ──────────────────────────────── */

    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 820);
            DashboardController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Dashboard – " + currentUser.getUsername());
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error opening dashboard.");
        }
    }

    /* ── UI helpers ──────────────────────────────── */

    private void showStatus(boolean success, String message) {
        statusIcon.setText(success ? "✓" : "✗");
        statusIcon.getStyleClass().setAll(success ? "success-label" : "error-label");
        statusLabel.setText(message);
        statusLabel.getStyleClass().setAll(success ? "success-label" : "error-label");
        statusStrip.getStyleClass().setAll(success ? "status-strip" : "status-strip-warning");
        statusBox.setVisible(true);
        statusBox.setManaged(true);
    }

    private void hideStatus() {
        statusBox.setVisible(false);
        statusBox.setManaged(false);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
