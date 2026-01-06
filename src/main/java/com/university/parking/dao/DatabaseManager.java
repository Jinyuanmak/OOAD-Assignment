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
 */
public class DatabaseManager {
    private static final String DEFAULT_DB_URL = "jdbc:h2:./parking_lot_db;DB_CLOSE_DELAY=-1";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";
    private static final int DEFAULT_POOL_SIZE = 10;

    private final String dbUrl;
    private final String user;
    private final String password;
    private final int poolSize;
    private final BlockingQueue<Connection> connectionPool;
    private boolean initialized = false;

    public DatabaseManager() {
        this(DEFAULT_DB_URL, DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_POOL_SIZE);
    }

    public DatabaseManager(String dbUrl) {
        this(dbUrl, DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_POOL_SIZE);
    }

    public DatabaseManager(String dbUrl, String user, String password, int poolSize) {
        this.dbUrl = dbUrl;
        this.user = user;
        this.password = password;
        this.poolSize = poolSize;
        this.connectionPool = new ArrayBlockingQueue<>(poolSize);
    }

    /**
     * Initializes the database by creating tables and connection pool.
     */
    public synchronized void initializeDatabase() throws SQLException {
        if (initialized) {
            return;
        }
        // Initialize pool first to keep connections alive for in-memory databases
        initializeConnectionPool();
        createTables();
        initialized = true;
    }

    /**
     * Creates all database tables if they don't exist.
     */
    private void createTables() throws SQLException {
        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement()) {
            
            // Create parking_lots table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS parking_lots (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "total_floors INT NOT NULL DEFAULT 0, " +
                "total_revenue DECIMAL(10,2) NOT NULL DEFAULT 0.00, " +
                "current_fine_strategy VARCHAR(50) DEFAULT 'FIXED')"
            );

            // Create floors table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS floors (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "parking_lot_id BIGINT NOT NULL, " +
                "floor_number INT NOT NULL, " +
                "total_spots INT NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (parking_lot_id) REFERENCES parking_lots(id))"
            );

            // Create parking_spots table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS parking_spots (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "floor_id BIGINT NOT NULL, " +
                "spot_id VARCHAR(50) NOT NULL UNIQUE, " +
                "spot_type VARCHAR(20) NOT NULL, " +
                "hourly_rate DECIMAL(10,2) NOT NULL, " +
                "status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', " +
                "current_vehicle_id BIGINT, " +
                "FOREIGN KEY (floor_id) REFERENCES floors(id))"
            );

            // Create vehicles table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS vehicles (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "license_plate VARCHAR(20) NOT NULL, " +
                "vehicle_type VARCHAR(20) NOT NULL, " +
                "is_handicapped BOOLEAN NOT NULL DEFAULT FALSE, " +
                "entry_time TIMESTAMP, " +
                "exit_time TIMESTAMP, " +
                "assigned_spot_id VARCHAR(50))"
            );

            // Create parking_sessions table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS parking_sessions (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "vehicle_id BIGINT NOT NULL, " +
                "spot_id VARCHAR(50) NOT NULL, " +
                "entry_time TIMESTAMP NOT NULL, " +
                "exit_time TIMESTAMP, " +
                "duration_hours INT, " +
                "ticket_number VARCHAR(100) NOT NULL UNIQUE, " +
                "FOREIGN KEY (vehicle_id) REFERENCES vehicles(id))"
            );

            // Create fines table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS fines (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "license_plate VARCHAR(20) NOT NULL, " +
                "fine_type VARCHAR(30) NOT NULL, " +
                "amount DECIMAL(10,2) NOT NULL, " +
                "issued_date TIMESTAMP NOT NULL, " +
                "is_paid BOOLEAN NOT NULL DEFAULT FALSE, " +
                "parking_session_id BIGINT, " +
                "FOREIGN KEY (parking_session_id) REFERENCES parking_sessions(id))"
            );

            // Create payments table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS payments (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "license_plate VARCHAR(20) NOT NULL, " +
                "parking_fee DECIMAL(10,2) NOT NULL, " +
                "fine_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00, " +
                "total_amount DECIMAL(10,2) NOT NULL, " +
                "payment_method VARCHAR(20) NOT NULL, " +
                "payment_date TIMESTAMP NOT NULL, " +
                "parking_session_id BIGINT, " +
                "FOREIGN KEY (parking_session_id) REFERENCES parking_sessions(id))"
            );

            // Note: Foreign key for parking_spots.current_vehicle_id is not added
            // because it would create a circular dependency with vehicles table
        } finally {
            releaseConnection(conn);
        }
    }

    /**
     * Initializes the connection pool with pre-created connections.
     */
    private void initializeConnectionPool() throws SQLException {
        for (int i = 0; i < poolSize; i++) {
            connectionPool.offer(createConnection());
        }
    }

    /**
     * Creates a new database connection.
     */
    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, user, password);
    }

    /**
     * Gets a connection from the pool.
     * @return a database connection
     */
    public Connection getConnection() throws SQLException {
        Connection conn = connectionPool.poll();
        if (conn == null || conn.isClosed()) {
            return createConnection();
        }
        return conn;
    }

    /**
     * Returns a connection to the pool.
     * @param conn the connection to return
     */
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

    /**
     * Closes all connections in the pool and shuts down the manager.
     */
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

    /**
     * Checks if the database manager is initialized.
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Gets the database URL.
     * @return the database URL
     */
    public String getDbUrl() {
        return dbUrl;
    }
}
