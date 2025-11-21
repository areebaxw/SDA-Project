import database.dbconnection;
import java.sql.Connection;

public class testdb {
    public static void main(String[] args) {
        Connection conn = dbconnection.getConnection();
        if (conn != null) {
            System.out.println("Connected to MySQL successfully!");
        } else {
            System.out.println("Connection failed.");
        }
    }
}
