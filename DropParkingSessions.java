import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DropParkingSessions {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/parking_lot?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("✓ Connected to database successfully!");
            
            // Drop foreign key constraints from fines table
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE fines DROP FOREIGN KEY fines_ibfk_1");
                System.out.println("✓ Dropped foreign key constraint from fines table");
            } catch (SQLException e) {
                System.out.println("Note: Foreign key constraint may not exist");
            }
            
            // Drop foreign key constraints from payments table
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE payments DROP FOREIGN KEY payments_ibfk_1");
                System.out.println("✓ Dropped foreign key constraint from payments table");
            } catch (SQLException e) {
                System.out.println("Note: Foreign key constraint may not exist");
            }
            
            // Remove parking_session_id column from fines table
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE fines DROP COLUMN parking_session_id");
                System.out.println("✓ Removed parking_session_id column from fines table");
            } catch (SQLException e) {
                System.out.println("Note: Column may not exist");
            }
            
            // Remove parking_session_id column from payments table
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE payments DROP COLUMN parking_session_id");
                System.out.println("✓ Removed parking_session_id column from payments table");
            } catch (SQLException e) {
                System.out.println("Note: Column may not exist");
            }
            
            // Drop parking_sessions table
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DROP TABLE IF EXISTS parking_sessions");
                System.out.println("✓ Dropped parking_sessions table");
            }
            
            System.out.println("\n✓ Database updated successfully!");
            System.out.println("The parking_sessions table and related foreign keys have been removed.");
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
