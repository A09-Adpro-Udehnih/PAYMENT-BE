package com.example.paymentbe;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConnection {
    private static final HikariDataSource dataSource;

    static {
        Dotenv dotenv = Dotenv.load();

        String dbUrl = dotenv.get("DATABASE_URL");
        String dbUsername = dotenv.get("DATABASE_USERNAME");
        String dbPassword = dotenv.get("DATABASE_PASSWORD");

        if (dbUrl == null || dbUsername == null || dbPassword == null) {
            throw new IllegalArgumentException("Database connection details are not set in .env file.");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(config);
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}