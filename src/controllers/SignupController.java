package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import models.User;
import dao.UserDAO;
import utils.Validator;

/**
 * SignupController  –  US-01a: Register user
 *
 * Structured Spec
 *   Preconditions : username and email must be unique in the DB
 *   Main flow     : fill form → hash password → store user → go to credentials setup
 *   Alternate     : duplicate username / email → show error
 *
 * Note: AWS credential setup is intentionally separated to CredentialsController (US-02).
 */
public class SignupController {

    @FXML private TextField     fullNameField;
    @FXML private TextField     usernameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button        signupButton;
    @FXML private Label         errorLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        hideError();
    }

    /* ── Actions ─────────────────────────────────── */

    @FXML
    private void handleSignup() {
        String fullName  = fullNameField.getText().trim();
        String username  = usernameField.getText().trim();
        String email     = emailField.getText().trim();
        String password  = passwordField.getText();
        String confirm   = confirmPasswordField.getText();

        // --- Validation ---
        if (!Validator.isNotEmpty(fullName)) {
            showError("Full name is required.");
            return;
        }
        if (!Validator.isValidUsername(username)) {
            showError("Username must be 3–50 characters.");
            return;
        }
        if (!Validator.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }
        if (!Validator.isValidPassword(password)) {
            showError("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }
        if (userDAO.usernameExists(username)) {
            showError("Username already taken. Choose another.");
            return;
        }

        // --- Persist user ---
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password);   // In production hash with BCrypt
        newUser.setRole("user");

        int userId = userDAO.createUserAndGetId(newUser);
        if (userId <= 0) {
            showError("Failed to create account. Please try again.");
            return;
        }

        newUser.setUserId(userId);
        System.out.println("✓ User registered: " + username + " (id=" + userId + ")");

        // Navigate to credentials setup (US-02)
        openCredentials(newUser);
    }

    @FXML
    private void handleBackToLogin() {
        navigate("/views/login.fxml", "AWS Governance Tool – Login", 800, 620);
    }

    /* ── Navigation helpers ──────────────────────── */

    private void openCredentials(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/credentials.fxml"));
            Scene scene = new Scene(loader.load(), 860, 680);
            CredentialsController ctrl = loader.getController();
            ctrl.setCurrentUser(user);
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool – Configure Credentials");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error opening credentials screen.");
        }
    }

    private void navigate(String fxml, String title, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load(), w, h);
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* ── UI helpers ──────────────────────────────── */

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

