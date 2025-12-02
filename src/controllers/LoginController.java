package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import models.User;
import dao.UserDAO;
import utils.Validator;

/**
 * LoginController - Controller for login view
 * Implements MVC pattern
 */
public class LoginController {
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private TextField passwordTextField;
    
    @FXML
    private Button togglePasswordButton;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Hyperlink signupLink;
    
    private boolean isPasswordVisible = false;
    
    private UserDAO userDAO;
    private User currentUser;
    
    public LoginController() {
        this.userDAO = new UserDAO();
    }
    
    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        // Bind the text fields together
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
    }
    
    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        
        if (isPasswordVisible) {
            // Show password as text
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordButton.setText("🙈");  // Hide icon
        } else {
            // Hide password
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            togglePasswordButton.setText("👁");  // Show icon
        }
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validate input
        if (!Validator.isNotEmpty(username) || !Validator.isNotEmpty(password)) {
            showError("Please enter username and password");
            return;
        }
        
        // Authenticate user
        User user = userDAO.authenticateUser(username, password);
        
        if (user != null) {
            currentUser = user;
            System.out.println("Login successful: " + user.getUsername());
            openDashboard();
        } else {
            showError("Invalid username or password");
        }
    }
    
    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 900);
            
            DashboardController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Dashboard - " + currentUser.getUsername());
            stage.setMinWidth(1200);
            stage.setMinHeight(850);
            stage.setMaximized(false);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error opening dashboard: " + e.getMessage());
            e.printStackTrace();
            showError("Error opening dashboard");
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    @FXML
    private void handleKeyPressed(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            handleLogin();
        }
    }
    
    @FXML
    private void handleSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/signup.fxml"));
            Scene scene = new Scene(loader.load(), 800, 750);
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool - Sign Up");
            stage.show();
        } catch (Exception e) {
            System.err.println("Error opening signup page: " + e.getMessage());
            e.printStackTrace();
            showError("Error opening signup page");
        }
    }
}
