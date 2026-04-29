package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.User;
import dao.UserDAO;
import dao.AWSCredentialDAO;
import utils.SceneNavigator;
import utils.Validator;

/**
 * LoginController  â€“  US-01b: Login user
 *
 * Structured Spec
 *   Preconditions : user record exists in DB
 *   Main flow     : enter credentials â†’ validate â†’ open Dashboard
 *   Alternate     : invalid credentials â†’ show error message
 */
public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     passwordTextField;   // visible-text twin
    @FXML private Button        togglePasswordButton;
    @FXML private Button        loginButton;
    @FXML private Label         errorLabel;

    private boolean passwordVisible = false;
    private final UserDAO          userDAO          = new UserDAO();
    private final AWSCredentialDAO awsCredentialDAO = new AWSCredentialDAO();

    @FXML
    private void initialize() {
        hideError();
        // Keep both text-fields in sync
        passwordTextField.textProperty()
                         .bindBidirectional(passwordField.textProperty());
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);
    }

    /* â”€â”€ Actions â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */

    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordButton.setText("ðŸ™ˆ");
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            togglePasswordButton.setText("ðŸ‘");
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (!Validator.isNotEmpty(username) || !Validator.isNotEmpty(password)) {
            showError("Please enter your username and password.");
            return;
        }
        
        // Super Admin Hardcoded Bypass
        if ("admin".equals(username) && "admin".equals(password)) {
            openSuperAdminDashboard();
            return;
        }

        User user;
        try {
            user = userDAO.authenticateUser(username, password);
        } catch (Exception ex) {
            showError("Database unavailable – please start MySQL/XAMPP and restart.");
            return;
        }
        if (user == null) {
            showError("Invalid username or password. Please try again.");
            return;
        }

        // Decide next screen: Credentials Setup if none saved, else Dashboard
        boolean hasCredentials;
        try {
            hasCredentials = awsCredentialDAO.getActiveCredentials(user.getUserId()) != null;
        } catch (Exception ex) {
            hasCredentials = false;
        }
        if (hasCredentials) {
            SceneNavigator.navigateTo("/views/dashboard.fxml",
                    "AWS Governance Dashboard – " + user.getUsername(),
                    (DashboardController ctrl) -> ctrl.setCurrentUser(user));
        } else {
            SceneNavigator.navigateTo("/views/credentials.fxml",
                    "AWS Governance Tool – Configure Credentials",
                    (CredentialsController ctrl) -> ctrl.setCurrentUser(user));
        }
    }

    @FXML
    private void handleSignup() {
        SceneNavigator.navigateTo("/views/signup.fxml", "AWS Governance Tool – Sign Up");
    }

    @FXML
    private void handleKeyPressed(javafx.scene.input.KeyEvent e) {
        if (e.getCode() == javafx.scene.input.KeyCode.ENTER) handleLogin();
    }

    private void openSuperAdminDashboard() {
        SceneNavigator.navigateTo("/views/super_admin_dashboard.fxml",
                "AWS Governance - Super Admin Dashboard");
    }

    /* â”€â”€ UI helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */

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
