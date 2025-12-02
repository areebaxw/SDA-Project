package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.collections.FXCollections;
import models.User;
import models.AWSCredential;
import dao.UserDAO;
import dao.AWSCredentialDAO;
import utils.Validator;

/**
 * SignupController - Controller for signup view
 * Handles new user registration with AWS credentials
 */
public class SignupController {
    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private TextField accessKeyField;
    
    @FXML
    private PasswordField secretKeyField;
    
    @FXML
    private ComboBox<String> regionComboBox;
    
    @FXML
    private Button signupButton;
    
    @FXML
    private Hyperlink backToLoginLink;
    
    @FXML
    private Label errorLabel;
    
    private UserDAO userDAO;
    private AWSCredentialDAO awsCredentialDAO;
    
    public SignupController() {
        this.userDAO = new UserDAO();
        this.awsCredentialDAO = new AWSCredentialDAO();
    }
    
    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        
        // Populate AWS regions
        regionComboBox.setItems(FXCollections.observableArrayList(
            "us-east-1",
            "us-east-2",
            "us-west-1",
            "us-west-2",
            "eu-west-1",
            "eu-west-2",
            "eu-west-3",
            "eu-central-1",
            "ap-south-1",
            "ap-southeast-1",
            "ap-southeast-2",
            "ap-northeast-1",
            "ap-northeast-2",
            "sa-east-1",
            "ca-central-1"
        ));
        
        // Set default region
        regionComboBox.setValue("us-east-1");
    }
    
    @FXML
    private void handleSignup() {
        // Get all field values
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String accessKey = accessKeyField.getText().trim();
        String secretKey = secretKeyField.getText().trim();
        String region = regionComboBox.getValue();
        
        // Validate all fields
        if (!validateInputs(fullName, username, email, password, confirmPassword, accessKey, secretKey, region)) {
            return;
        }
        
        // Check if username already exists
        if (userDAO.usernameExists(username)) {
            showError("Username already exists. Please choose a different username.");
            return;
        }
        
        // Check if AWS credentials already exist
        if (awsCredentialDAO.accessKeyExists(accessKey)) {
            showError("AWS credentials already registered. Please use different credentials.");
            return;
        }
        
        // Create new user
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password); // In production, hash the password
        newUser.setRole("user");
        
        // Insert user into database
        int userId = userDAO.createUserAndGetId(newUser);
        
        if (userId > 0) {
            // User created successfully, now save AWS credentials
            AWSCredential credential = new AWSCredential(userId, accessKey, secretKey, region);
            credential.setActive(true);
            credential.setValidated(false);
            credential.setRemainingCredits(0.0);
            
            if (awsCredentialDAO.saveCredentials(credential)) {
                showSuccess("Account created successfully! Redirecting to login...");
                
                // Wait a moment then redirect to login
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(this::handleBackToLogin);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                showError("User created but failed to save AWS credentials. Please contact support.");
            }
        } else {
            showError("Failed to create account. Please try again.");
        }
    }
    
    private boolean validateInputs(String fullName, String username, String email, 
                                    String password, String confirmPassword,
                                    String accessKey, String secretKey, String region) {
        // Check if all fields are filled
        if (!Validator.isNotEmpty(fullName)) {
            showError("Please enter your full name");
            return false;
        }
        
        if (!Validator.isNotEmpty(username)) {
            showError("Please enter a username");
            return false;
        }
        
        if (username.length() < 3) {
            showError("Username must be at least 3 characters long");
            return false;
        }
        
        if (!Validator.isNotEmpty(email)) {
            showError("Please enter your email");
            return false;
        }
        
        if (!Validator.isValidEmail(email)) {
            showError("Please enter a valid email address");
            return false;
        }
        
        if (!Validator.isNotEmpty(password)) {
            showError("Please enter a password");
            return false;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return false;
        }
        
        if (!Validator.isNotEmpty(accessKey)) {
            showError("Please enter your AWS access key");
            return false;
        }
        
        if (!Validator.isNotEmpty(secretKey)) {
            showError("Please enter your AWS secret key");
            return false;
        }
        
        if (region == null || region.isEmpty()) {
            showError("Please select an AWS region");
            return false;
        }
        
        return true;
    }
    
    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(loader.load(), 800, 700);
            
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool - Login");
            stage.show();
        } catch (Exception e) {
            System.err.println("Error opening login page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        errorLabel.setVisible(true);
    }
    
    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
        errorLabel.setVisible(true);
    }
}
