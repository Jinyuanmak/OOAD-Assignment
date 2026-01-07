import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DiagnosticCheck {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/parking_lot?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("✓ Connected to database successfully!");
            
            // Check vehicles table
            System.out.println("\n=== VEHICLES TABLE ===");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM vehicles WHERE exit_time IS NULL")) {
                
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("Vehicle " + count + ":");
                    System.out.println("  ID: " + rs.getLong("id"));
                    System.out.println("  License Plate: " + rs.getString("license_plate"));
                    System.out.println("  Type: " + rs.getString("vehicle_type"));
                    System.out.println("  Entry Time: " + rs.getTimestamp("entry_time"));
                    System.out.println("  Assigned Spot: " + rs.getString("assigned_spot_id"));
                    System.out.println();
                }
                
                if (count == 0) {
                    System.out.println("No active vehicles found in database.");
                } else {
                    System.out.println("Total active vehicles: " + count);
                }
            }
            
            // Check parking_spots table
            System.out.println("\n=== PARKING SPOTS TABLE ===");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM parking_spots WHERE status = 'OCCUPIED' LIMIT 5")) {
                
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("Occupied Spot " + count + ":");
                    System.out.println("  Spot ID: " + rs.getString("spot_id"));
                    System.out.println("  Status: " + rs.getString("status"));
                    System.out.println();
                }
                
                if (count == 0) {
                    System.out.println("No occupied spots found in database.");
                } else {
                    System.out.println("Total occupied spots: " + count);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
