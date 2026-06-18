package dev.pterox.maphunter.storage;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.leader.LeaderData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LeaderRepository {

    private final DatabaseManager databaseManager;
    private final MapHunter plugin;

    public LeaderRepository(MapHunter plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public void save(LeaderData data) {
        String query = "INSERT OR REPLACE INTO leaders (uuid, player_name, clan_name, clan_color, map_id, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, data.getUuid().toString());
            ps.setString(2, data.getPlayerName());
            ps.setString(3, data.getClanName());
            ps.setString(4, data.getClanColor());
            ps.setInt(5, data.getMapId());
            ps.setLong(6, data.getCreatedAt());
            
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save LeaderData for " + data.getUuid() + ": " + e.getMessage());
        }
    }

    public void delete(UUID uuid) {
        String query = "DELETE FROM leaders WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete LeaderData for " + uuid + ": " + e.getMessage());
        }
    }

    public LeaderData findByUuid(UUID uuid) {
        String query = "SELECT * FROM leaders WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LeaderData(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("player_name"),
                            rs.getString("clan_name"),
                            rs.getString("clan_color"),
                            rs.getInt("map_id"),
                            rs.getLong("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to find LeaderData for " + uuid + ": " + e.getMessage());
        }
        return null;
    }

    public List<LeaderData> findAll() {
        List<LeaderData> list = new ArrayList<>();
        String query = "SELECT * FROM leaders";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new LeaderData(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getString("clan_name"),
                        rs.getString("clan_color"),
                        rs.getInt("map_id"),
                        rs.getLong("created_at")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load all LeaderData: " + e.getMessage());
        }
        return list;
    }
}
