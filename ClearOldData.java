import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ClearOldData {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/parking_lot?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("✓ Connected to database successfully!");
            
            // Delete all payments
            try (Statement stmt = conn.createStatement()) {
                int deleted = stmt.executeUpdate("DELETE FROM payments");
                System.out.println("Deleted " + deleted + " payments");
            }
            
            // Delete all fines
            try (Statement stmt = conn.createStatement()) {
                int deleted = stmt.executeUpdate("DELETE FROM fines");
                System.out.println("Deleted " + deleted + " fines");
            }
            
            // Delete all vehicles
            try (Statement stmt = conn.createStatement()) {
                int deleted = stmt.executeUpdate("DELETE FROM vehicles");
                System.out.println("Deleted " + deleted + " vehicles");
            }
            
            // Reset all parking spots to AVAILABLE
            try (Statement stmt = conn.createStatement()) {
                int updated = stmt.executeUpdate("UPDATE parking_spots SET status = 'AVAILABLE'");
                System.out.println("Reset " + updated + " parking spots to AVAILABLE");
            }
            
            // Reset parking lot revenue to 0
            try (Statement stmt = conn.createStatement()) {
                int updated = stmt.executeUpdate("UPDATE parking_lots SET total_revenue = 0.00");
                System.out.println("Reset parking lot revenue to RM 0.00");
            }
            
            System.out.println("\n✓ Database cleaned successfully!");
            System.out.println("You can now test with fresh vehicle entries.");
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
