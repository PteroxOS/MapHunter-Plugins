package dev.pterox.maphunter.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.pterox.maphunter.MapHunter;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final MapHunter plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(MapHunter plugin) {
        this.plugin = plugin;
    }

    public void init() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File dbFolder = new File(dataFolder, "database");
        if (!dbFolder.exists()) {
            dbFolder.mkdirs();
        }

        File dbFile = new File(dbFolder, "leader_data.db");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setPoolName("MapHunter-SQLitePool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(60000);
        config.setConnectionTimeout(10000);
        
        // Optimize SQLite for concurrent reading/writing, using DELETE to avoid WAL file confusion
        config.addDataSourceProperty("journal_mode", "DELETE");
        config.addDataSourceProperty("synchronous", "NORMAL");

        this.dataSource = new HikariDataSource(config);

        createTables();
    }

    private void createTables() {
        String query = "CREATE TABLE IF NOT EXISTS leaders (" +
                "uuid TEXT PRIMARY KEY," +
                "player_name TEXT NOT NULL," +
                "clan_name TEXT NOT NULL," +
                "clan_color TEXT NOT NULL," +
                "map_id INTEGER DEFAULT -1," +
                "created_at INTEGER NOT NULL," +
                "backup_uuid TEXT" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(query);
            
            // Try adding backup_uuid column for existing databases
            try {
                stmt.execute("ALTER TABLE leaders ADD COLUMN backup_uuid TEXT;");
            } catch (SQLException ignore) {
                // Column already exists
            }
            
            // Try adding replaced_by_backup column for existing databases
            try {
                stmt.execute("ALTER TABLE leaders ADD COLUMN replaced_by_backup INTEGER DEFAULT 0;");
            } catch (SQLException ignore) {
                // Column already exists
            }
            
            plugin.getLogger().info("SQLite tables verified.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is null. DatabaseManager might not be initialized.");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
