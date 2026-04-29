import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import database.DBConnection;
import utils.SceneNavigator;


public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Verify DB connectivity at startup
            if (DBConnection.testConnection()) {
                System.out.println("âœ“ Database connection established.");
            } else {
                System.err.println("âœ— Database connection failed â€“ check DBConnection.java config.");
            }

            // Sprint 1 starts directly at the Login screen (no splash)
            Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(root, 800, 620);
            SceneNavigator.setMainScene(scene);

            primaryStage.setTitle("AWS Cloud Governance - Monitoring Console");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(500);
            primaryStage.centerOnScreen();
            primaryStage.show();

            System.out.println("Application started. Open http://localhost â€“ visit login screen.");
        } catch (Exception e) {
            System.err.println("Error starting application:");
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            DBConnection.closeConnection();
            System.out.println("Database connection closed.");
        } catch (Exception e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

