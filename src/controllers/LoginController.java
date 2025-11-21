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
    private Button loginButton;
    
    @FXML
    private Label errorLabel;
    
    private UserDAO userDAO;
    private User currentUser;
    
    public LoginController() {
        this.userDAO = new UserDAO();
    }
    
    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
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
}
