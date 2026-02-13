package com.university.parking.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Manages database connections and schema initialization.
 * Implements connection pooling for efficient database access.
 * Supports MySQL with configurable database name for production and testing.
 */
public class DatabaseManager {
    // MySQL connection settings for Laragon (default: localhost:3306)
    private static final String DEFAULT_DB_NAME = "parking_lot";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final int DEFAULT_POOL_SIZE = 10;

    private final String databaseName;
    private final String dbUrl;
    private final String user;
    private final String password;
    private final int poolSize;
    private final BlockingQueue<Connection> connectionPool;
    private boolean initialized = false;

    /**
     * Creates DatabaseManager with default production database name.
     */
    public DatabaseManager() throws SQLException {
        this(DEFAULT_DB_NAME);
    }

    /**
     * Creates DatabaseManager with specified database name.
     * Useful for testing with separate test database.
     * @param databaseName the name of the database to use
     */
    public DatabaseManager(String databaseName) throws SQLException {
        this(databaseName, DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_POOL_SIZE);
    }

    /**
     * Creates DatabaseManager with full configuration.
     * @param databaseName the name of the database
     * @param user the database user
     * @param password the database password
     * @param poolSize the connection pool size
     */
    public DatabaseManager(String databaseName, String user, String password, int poolSize) throws SQLException {
        this.databaseName = databaseName;
        // Use Asia/Singapore timezone (UTC+8) to match Malaysia/Singapore/China time
        this.dbUrl = "jdbc:mysql://localhost:3306/" + databaseName + 
                     "?useSSL=false&serverTimezone=Asia/Singapore&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true";
        this.user = user;
        this.password = password;
        this.poolSize = poolSize;
        this.connectionPool = new ArrayBlockingQueue<>(poolSize);
    }

    public synchronized void initializeDatabase() throws SQLException {
        if (initialized) {
            return;
        }
        createDatabaseIfNotExists();
        initializeConnectionPool();
        createTables();
        initialized = true;
    }

    private void createDatabaseIfNotExists() throws SQLException {
        // Use Asia/Singapore timezone (UTC+8) for database connection
        String baseUrl = "jdbc:mysql://localhost:3306?useSSL=false&serverTimezone=Asia/Singapore&allowPublicKeyRetrieval=true";
        try (Connection conn = DriverManager.getConnection(baseUrl, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS " + databaseName);
            System.out.println("Database '" + databaseName + "' created or already exists.");
        }
    }

    private void createTables() throws SQLException {
        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS parking_lots (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "total_floors INT NOT NULL DEFAULT 0, " +
                "total_revenue DECIMAL(10,2) NOT NULL DEFAULT 0.00, " +
                "current_fine_strategy VARCHAR(50) DEFAULT 'FIXED'" +
                ") ENGINE=InnoDB"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS floors (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "parking_lot_id BIGINT NOT NULL, " +
                "floor_number INT NOT NULL, " +
                "total_spots INT NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (parking_lot_id) REFERENCES parking_lots(id)" +
                ") ENGINE=InnoDB"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS parking_spots (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "floor_id BIGINT NOT NULL, " +
                "spot_id VARCHAR(50) NOT NULL UNIQUE, " +
                "spot_type VARCHAR(20) NOT NULL, " +
                "hourly_rate DECIMAL(10,2) NOT NULL, " +
                "status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', " +
                "current_vehicle_id BIGINT, " +
                "FOREIGN KEY (floor_id) REFERENCES floors(id)" +
                ") ENGINE=InnoDB"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS vehicles (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "license_plate VARCHAR(20) NOT NULL, " +
                "vehicle_type VARCHAR(20) NOT NULL, " +
                "is_handicapped BOOLEAN NOT NULL DEFAULT FALSE, " +
                "entry_time DATETIME, " +
                "exit_time DATETIME, " +
                "assigned_spot_id VARCHAR(50)" +
                ") ENGINE=InnoDB"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS fines (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "license_plate VARCHAR(20) NOT NULL, " +
                "fine_type VARCHAR(30) NOT NULL, " +
                "amount DECIMAL(10,2) NOT NULL, " +
                "issued_date DATETIME NOT NULL, " +
                "is_paid BOOLEAN NOT NULL DEFAULT FALSE" +
                ") ENGINE=InnoDB"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS payments (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "license_plate VARCHAR(20) NOT NULL, " +
                "parking_fee DECIMAL(10,2) NOT NULL, " +
                "fine_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00, " +
                "total_amount DECIMAL(10,2) NOT NULL, " +
                "payment_method VARCHAR(20) NOT NULL, " +
                "payment_date DATETIME NOT NULL" +
                ") ENGINE=InnoDB"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS reservations (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "license_plate VARCHAR(20) NOT NULL, " +
                "spot_id VARCHAR(50) NOT NULL, " +
                "start_time DATETIME NOT NULL, " +
                "end_time DATETIME NOT NULL, " +
                "is_active BOOLEAN NOT NULL DEFAULT TRUE, " +
                "created_at DATETIME NOT NULL, " +
                "prepaid_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00, " +
                "INDEX idx_license_plate (license_plate), " +
                "INDEX idx_spot_id (spot_id), " +
                "INDEX idx_time_range (start_time, end_time)" +
                ") ENGINE=InnoDB"
            );

            // Create VIEW for real-time elapsed time tracking
            stmt.execute(
                "CREATE OR REPLACE VIEW vehicles_with_duration AS " +
                "SELECT v.*, " +
                "CASE " +
                "  WHEN v.exit_time IS NOT NULL THEN TIMESTAMPDIFF(SECOND, v.entry_time, v.exit_time) " +
                "  WHEN v.entry_time IS NOT NULL THEN TIMESTAMPDIFF(SECOND, v.entry_time, CURRENT_TIMESTAMP) " +
                "  ELSE 0 " +
                "END as elapsed_seconds, " +
                "CASE " +
                "  WHEN v.exit_time IS NOT NULL THEN TIMESTAMPDIFF(MINUTE, v.entry_time, v.exit_time) " +
                "  WHEN v.entry_time IS NOT NULL THEN TIMESTAMPDIFF(MINUTE, v.entry_time, CURRENT_TIMESTAMP) " +
                "  ELSE 0 " +
                "END as elapsed_minutes, " +
                "CASE " +
                "  WHEN v.exit_time IS NOT NULL THEN TIMESTAMPDIFF(HOUR, v.entry_time, v.exit_time) " +
                "  WHEN v.entry_time IS NOT NULL THEN TIMESTAMPDIFF(HOUR, v.entry_time, CURRENT_TIMESTAMP) " +
                "  ELSE 0 " +
                "END as elapsed_hours, " +
                "CASE " +
                "  WHEN v.exit_time IS NOT NULL THEN TIMESTAMPDIFF(HOUR, v.entry_time, v.exit_time) > 24 " +
                "  WHEN v.entry_time IS NOT NULL THEN TIMESTAMPDIFF(HOUR, v.entry_time, CURRENT_TIMESTAMP) > 24 " +
                "  ELSE FALSE " +
                "END as is_overstay " +
                "FROM vehicles v"
            );

            System.out.println("All tables created successfully in MySQL database '" + databaseName + "'.");
            System.out.println("VIEW 'vehicles_with_duration' created for real-time elapsed time tracking.");
        } finally {
            releaseConnection(conn);
        }
    }

    private void initializeConnectionPool() throws SQLException {
        for (int i = 0; i < poolSize; i++) {
            connectionPool.offer(createConnection());
        }
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, user, password);
    }

    public Connection getConnection() throws SQLException {
        Connection conn = connectionPool.poll();
        if (conn == null || conn.isClosed()) {
            return createConnection();
        }
        return conn;
    }

    public void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    if (!connectionPool.offer(conn)) {
                        conn.close();
                    }
                }
            } catch (SQLException e) {
                // Ignore close errors
            }
        }
    }

    public void shutdown() {
        Connection conn;
        while ((conn = connectionPool.poll()) != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Ignore close errors
            }
        }
        initialized = false;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDatabaseName() {
        return databaseName;
    }
}
