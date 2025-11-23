import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import database.DBConnection;
import services.AlertService;
import services.ConsoleAlertObserver;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Test database connection
            if (DBConnection.testConnection()) {
                System.out.println("Database connection successful!");
            } else {
                System.err.println("Failed to connect to database. Check DBConnection configuration.");
            }
            
            // Initialize AlertService with observer pattern
            AlertService alertService = AlertService.getInstance();
            alertService.registerObserver(new ConsoleAlertObserver());
            System.out.println("Alert service initialized with console observer");
            
            // Load SPLASH SCREEN FXML view instead of login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/splash.fxml"));
            Parent root = loader.load();
            
            // Setup scene and stage
            Scene scene = new Scene(root, 900, 700);
            primaryStage.setTitle("AWS Cloud Governance & Resource Monitoring Tool");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
            System.out.println("Application started successfully!");
            System.out.println("  Splash screen will display for 5 seconds...");
            System.out.println("  Login with: admin / admin123");
            
        } catch (Exception e) {
            System.err.println("Error starting application:");
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop() {
        // Cleanup on application close
        try {
            DBConnection.closeConnection();
            System.out.println("Database connection closed gracefully");
        } catch (Exception e) {
            System.err.println("Error during cleanup:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}