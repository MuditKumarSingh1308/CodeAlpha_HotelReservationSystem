package com.hotel.reservation.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String CONFIG_FILE = "/db.properties";

    public static Connection getConnection() throws SQLException {

        Properties properties = new Properties();

        try (InputStream input = DatabaseConnection.class
                .getResourceAsStream(CONFIG_FILE)) {

            if (input == null) {
                throw new SQLException("Database configuration file not found");
            }

            properties.load(input);

        } catch (IOException e) {
            throw new SQLException("Unable to load database configuration", e);
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        if (url == null || username == null || password == null) {
            throw new SQLException("Database configuration is incomplete");
        }

        return DriverManager.getConnection(url, username, password);
    }
}