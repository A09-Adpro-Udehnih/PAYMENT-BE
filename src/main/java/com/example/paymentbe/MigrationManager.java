package com.example.paymentbe;

import org.flywaydb.core.Flyway;

public class MigrationManager {
    public static void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(DatabaseConnection.getDataSource())
                .locations("classpath:db/migration")
                .load();

        flyway.repair();        
        flyway.migrate();
    }
}
