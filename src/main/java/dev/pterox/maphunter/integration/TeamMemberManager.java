package dev.pterox.maphunter.integration;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.util.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamMemberManager {

    private final MapHunter plugin;
    private final BetterTeamsIntegration integration;
    
    // playerUUID → teamName
    private final Map<UUID, String> playerTeamMap = new HashMap<>();
    // teamName → list of playerUUID
    private final Map<String, List<UUID>> teamPlayersMap = new HashMap<>();

    public TeamMemberManager(MapHunter plugin, BetterTeamsIntegration integration) {
        this.plugin = plugin;
        this.integration = integration;
    }

    public void loadFromBetterTeams() {
        playerTeamMap.clear();
        teamPlayersMap.clear();
        
        if (!integration.isEnabled()) return;
        
        Map<String, BetterTeamsIntegration.TeamData> teams = integration.readAllTeams();
        
        for (BetterTeamsIntegration.TeamData team : teams.values()) {
            List<UUID> members = new ArrayList<>();
            for (UUID memberUuid : team.members) {
                playerTeamMap.put(memberUuid, team.name);
                members.add(memberUuid);
            }
            teamPlayersMap.put(team.name.toLowerCase(), members);
        }
        
        LogUtil.log("[TeamMember] Loaded " + playerTeamMap.size() + " members from " + teams.size() + " teams");
    }

    public String getPlayerTeam(UUID playerUuid) {
        return playerTeamMap.get(playerUuid);
    }

    public String getPlayerTeam(Player player) {
        return playerTeamMap.get(player.getUniqueId());
    }

    public List<UUID> getTeamMembers(String teamName) {
        return teamPlayersMap.getOrDefault(teamName.toLowerCase(), new ArrayList<>());
    }

    public boolean isPlayerInTeam(UUID playerUuid) {
        return playerTeamMap.containsKey(playerUuid);
    }

    public boolean isPlayerInTeam(Player player) {
        return playerTeamMap.containsKey(player.getUniqueId());
    }

    public Set<String> getAllTeamNames() {
        return teamPlayersMap.keySet();
    }

    public int getTotalMembers() {
        return playerTeamMap.size();
    }

    public void addPlayerToTeam(UUID playerUuid, String teamName) {
        playerTeamMap.put(playerUuid, teamName);
        teamPlayersMap.computeIfAbsent(teamName.toLowerCase(), k -> new ArrayList<>()).add(playerUuid);
    }

    public void removePlayerFromTeam(UUID playerUuid) {
        String teamName = playerTeamMap.remove(playerUuid);
        if (teamName != null) {
            List<UUID> members = teamPlayersMap.get(teamName.toLowerCase());
            if (members != null) {
                members.remove(playerUuid);
            }
        }
    }
}
