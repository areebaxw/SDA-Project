package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * SplashController - Controller for splash screen
 * Displays splash screen for 5 seconds, then transitions to login
 */
public class SplashController {
    @FXML
    private ProgressBar loadingProgress;
    
    @FXML
    private ImageView logoImage;
    
    @FXML
    private void initialize() {
        // Load and set logo image
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/awsense_logo.png"));
            logoImage.setImage(logo);
        } catch (Exception e) {
            System.err.println("Error loading logo image: " + e.getMessage());
            // Logo image is optional, continue without it
        }
        
        // Start the splash screen timer
        startSplashTimer();
    }
    
    private void startSplashTimer() {
        // Create a pause transition for 5 seconds
        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(5));
        
        // Animate progress bar while waiting
        pauseTransition.setOnFinished(event -> openLoginScreen());
        
        pauseTransition.play();
    }
    
    private void openLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 700);
            
            Stage stage = (Stage) loadingProgress.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Cloud Governance Tool - Login");
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            System.err.println("Error opening login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}