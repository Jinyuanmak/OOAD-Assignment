package com.university.parking.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.university.parking.dao.DatabaseManager;

/**
 * Utility class for managing test database configuration.
 * Provides methods to create, clean, and reset the test database.
 */
public class TestDatabaseConfig {
    private static final String TEST_DB_NAME = "parking_lot_test";
    
    /**
     * Creates a DatabaseManager configured for the test database.
     * @return DatabaseManager instance connected to test database
     * @throws SQLException if database connection fails
     */
    public static DatabaseManager createTestDatabaseManager() throws SQLException {
        DatabaseManager dbManager = new DatabaseManager(TEST_DB_NAME);
        dbManager.initializeDatabase();
        return dbManager;
    }
    
    /**
     * Cleans all data from the test database tables.
     * Deletes records in correct order to respect foreign key constraints.
     * @param dbManager the database manager
     * @throws SQLException if cleanup fails
     */
    public static void cleanDatabase(DatabaseManager dbManager) throws SQLException {
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            try (Statement stmt = conn.createStatement()) {
                // Delete in correct order (respect foreign keys)
                stmt.executeUpdate("DELETE FROM payments");
                stmt.executeUpdate("DELETE FROM fines");
                stmt.executeUpdate("DELETE FROM vehicles");
                stmt.executeUpdate("DELETE FROM parking_spots");
                stmt.executeUpdate("DELETE FROM floors");
                stmt.executeUpdate("DELETE FROM parking_lots");
            }
        } finally {
            if (conn != null) {
                dbManager.releaseConnection(conn);
            }
        }
    }
    
    /**
     * Resets the test database by cleaning all data and reinitializing tables.
     * @param dbManager the database manager
     * @throws SQLException if reset fails
     */
    public static void resetDatabase(DatabaseManager dbManager) throws SQLException {
        cleanDatabase(dbManager);
        // Tables are already created by initializeDatabase(), just cleaned
    }
    
    /**
     * Gets the test database name.
     * @return the test database name
     */
    public static String getTestDatabaseName() {
        return TEST_DB_NAME;
    }
}
