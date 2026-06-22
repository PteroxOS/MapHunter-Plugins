package dev.pterox.maphunter.integration;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.util.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class BetterTeamsIntegration {

    private final MapHunter plugin;
    private boolean enabled = false;

    public BetterTeamsIntegration(MapHunter plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void checkBetterTeams() {
        if (Bukkit.getPluginManager().getPlugin("BetterTeams") != null) {
            enabled = true;
            LogUtil.log("BetterTeams detected! Gunakan /rmh sync untuk sync data.");
            plugin.getLogger().info("[MapHunter] BetterTeams terdeteksi! Gunakan /rmh sync untuk sync data.");
        } else {
            enabled = false;
            LogUtil.log("BetterTeams tidak ditemukan. Mode manual.");
        }
    }

    /**
     * Baca semua team dari BetterTeams
     * @return Map<teamName, TeamData>
     */
    public Map<String, TeamData> readAllTeams() {
        Map<String, TeamData> teams = new HashMap<>();
        
        File betterTeamsFolder = new File(Bukkit.getPluginManager().getPlugin("BetterTeams").getDataFolder(), "teamInfo");
        if (!betterTeamsFolder.exists() || !betterTeamsFolder.isDirectory()) {
            plugin.getLogger().warning("[MapHunter] Folder teamInfo BetterTeams tidak ditemukan!");
            return teams;
        }
        
        File[] teamFiles = betterTeamsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (teamFiles == null) return teams;
        
        for (File file : teamFiles) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                
                String teamName = config.getString("name");
                if (teamName == null || teamName.isEmpty()) continue;
                
                String colorCode = config.getString("color", "f");
                
                List<String> playerStrings = config.getStringList("players");
                UUID ownerUuid = null;
                List<UUID> members = new ArrayList<>();
                
                for (String playerStr : playerStrings) {
                    String[] parts = playerStr.split(",");
                    if (parts.length >= 2) {
                        UUID playerUuid = UUID.fromString(parts[0]);
                        String rank = parts[1];
                        
                        members.add(playerUuid);
                        
                        if (rank.equalsIgnoreCase("OWNER")) {
                            ownerUuid = playerUuid;
                        }
                    }
                }
                
                if (ownerUuid != null) {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUuid);
                    String ownerName = owner.getName() != null ? owner.getName() : "Unknown";
                    
                    // Cari admin pertama sebagai backup candidate
                    UUID backupUuid = null;
                    for (String playerStr : playerStrings) {
                        String[] parts = playerStr.split(",");
                        if (parts.length >= 2 && parts[1].equalsIgnoreCase("ADMIN")) {
                            backupUuid = UUID.fromString(parts[0]);
                            break;
                        }
                    }
                    
                    teams.put(teamName.toLowerCase(), new TeamData(
                        teamName, ownerUuid, ownerName, colorCode, backupUuid, members
                    ));
                    
                    LogUtil.log("[BetterTeams] Team: " + teamName + " | Owner: " + ownerName + " | Members: " + members.size());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[MapHunter] Gagal baca team file: " + file.getName() + " - " + e.getMessage());
            }
        }
        
        return teams;
    }

    /**
     * Convert warna BetterTeams ke nama warna MapHunter
     */
    public String convertColor(String btColorCode) {
        if (btColorCode == null) return "WHITE";
        
        switch (btColorCode) {
            case "0": return "BLACK";
            case "1": return "BLUE";
            case "2": return "GREEN";
            case "3": return "AQUA";
            case "4": return "RED";
            case "5": return "PURPLE";
            case "6": return "YELLOW"; // GOLD = ORANGE
            case "7": return "WHITE";
            case "8": return "WHITE";
            case "9": return "BLUE";
            case "a": return "GREEN";
            case "b": return "AQUA";
            case "c": return "RED";
            case "d": return "PINK";
            case "e": return "YELLOW";
            case "f": return "WHITE";
            default: return "WHITE";
        }
    }

    public static class TeamData {
        public final String name;
        public final UUID ownerUuid;
        public final String ownerName;
        public final String colorCode;
        public final UUID backupUuid;
        public final List<UUID> members;

        public TeamData(String name, UUID ownerUuid, String ownerName, String colorCode, UUID backupUuid, List<UUID> members) {
            this.name = name;
            this.ownerUuid = ownerUuid;
            this.ownerName = ownerName;
            this.colorCode = colorCode;
            this.backupUuid = backupUuid;
            this.members = members;
        }
    }
}
