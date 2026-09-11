package com.medchain.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Package: com.medchain.util
 * Purpose: Singleton utility that loads DB configurations and supplies JDBC Connection instances.
 */
public class DBConnection {
    // [CONCEPT: JDBC]
    private static Connection connection = null;

    private DBConnection() {}

    public static synchronized Connection getConnection() {
        if (connection == null) {
            try {
                Properties props = new Properties();
                // Load from root file path
                try (FileInputStream fis = new FileInputStream("db.properties")) {
                    props.load(fis);
                } catch (IOException e) {
                    props.setProperty("db.url", "jdbc:mysql://localhost:3306/medchain_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata");
                    props.setProperty("db.username", "root");
                    props.setProperty("db.password", "");
                }

                String url = props.getProperty("db.url");
                String user = props.getProperty("db.username");
                String pass = props.getProperty("db.password");

                // Register driver (explicitly for older JDBC compatibility)
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    System.err.println("MySQL Connector JDBC Driver not found in Classpath!");
                }

                connection = DriverManager.getConnection(url, user, pass);
            } catch (SQLException e) {
                System.err.println("Critical Error: Database connection failed! Message: " + e.getMessage());
                throw new RuntimeException("Database Connection Error", e);
            }
        } else {
            try {
                if (connection.isClosed()) {
                    connection = null;
                    return getConnection();
                }
            } catch (SQLException e) {
                connection = null;
                return getConnection();
            }
        }
        return connection;
    }
}
