import javafx.application.Application;
import javafx.stage.Stage;
import database.dbconnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("JavaFX + MySQL Test");
        stage.show();

        try (Connection conn = dbconnection.getConnection()) {
            if (conn != null) {
                // Insert a test user (only username)
                String insertSQL = "INSERT INTO users (username) VALUES (?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                    pstmt.setString(1, "testuser");
                    pstmt.executeUpdate();
                    System.out.println("Inserted test user into database.");
                }

                // Query the last inserted user
                String querySQL = "SELECT * FROM users ORDER BY id DESC LIMIT 1";
                try (PreparedStatement pstmt = conn.prepareStatement(querySQL);
                     ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Last user: id=" + rs.getInt("id") +
                                           ", username=" + rs.getString("username"));
                    }
                }
            } else {
                System.out.println("Connection to MySQL failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}