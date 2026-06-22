package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.MapHunter;
import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.integration.BetterTeamsIntegration;
import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.util.LogUtil;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.*;

public class SyncCommand extends SubCommand {

    private final LeaderManager leaderManager;
    private final BetterTeamsIntegration integration;

    public SyncCommand(LeaderManager leaderManager, BetterTeamsIntegration integration) {
        this.leaderManager = leaderManager;
        this.integration = integration;
    }

    @Override
    public String getName() {
        return "sync";
    }

    @Override
    public String getDescription() {
        return "Sync data leader dari BetterTeams ke MapHunter";
    }

    @Override
    public String getSyntax() {
        return "/rmh sync";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!integration.isEnabled()) {
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&cBetterTeams tidak ditemukan di server!"));
            return;
        }

        sender.sendMessage(MessageUtil.color("&8&m                              "));
        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&b&lSYNC DARI BETTERTEAMS"));
        sender.sendMessage(MessageUtil.color("&8&m                              "));
        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&fMembaca data team..."));

        Map<String, BetterTeamsIntegration.TeamData> teams = integration.readAllTeams();

        if (teams.isEmpty()) {
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&cTidak ada team ditemukan di BetterTeams!"));
            return;
        }

        // Clear existing leaders (overwrite mode)
        for (LeaderData data : new ArrayList<>(leaderManager.getAllLeaders())) {
            leaderManager.removeLeader(data.getUuid());
        }

        int synced = 0;
        int skipped = 0;

        for (BetterTeamsIntegration.TeamData team : teams.values()) {
            String color = integration.convertColor(team.colorCode);

            // Check if owner is online
            OfflinePlayer ownerOffline = Bukkit.getOfflinePlayer(team.ownerUuid);
            String ownerName = ownerOffline.getName() != null ? ownerOffline.getName() : team.ownerName;
            
            // Add owner as leader
            LeaderData leaderData = new LeaderData(
                team.ownerUuid, ownerName, team.name, color, -1, System.currentTimeMillis()
            );
            
            // Set backup if available
            if (team.backupUuid != null) {
                leaderData.setBackupUuid(team.backupUuid);
            }
            
            leaderManager.saveLeader(leaderData);
            synced++;

            // Tampilkan info
            String backupName = "Tidak ada";
            if (team.backupUuid != null) {
                OfflinePlayer backupOffline = Bukkit.getOfflinePlayer(team.backupUuid);
                backupName = backupOffline.getName() != null ? backupOffline.getName() : "Unknown";
            }

            org.bukkit.ChatColor clanColor = dev.pterox.maphunter.util.ColorUtil.getChatColor(color);
            sender.sendMessage(MessageUtil.color(""));
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r" + clanColor + "Team: " + team.name));
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r  &fLeader: &e" + ownerName + " &7(OWNER)"));
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r  &fBackup: &e" + backupName + " &7(ADMIN)"));
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r  &fMembers: &e" + team.members.size() + " &7orang"));

            LogUtil.log("[Sync] Team: " + team.name + " | Leader: " + ownerName + " | Backup: " + backupName);
        }

        sender.sendMessage(MessageUtil.color(""));
        sender.sendMessage(MessageUtil.color("&8&m                              "));
        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&a&lSYNC BERHASIL"));
        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&fTotal: &e" + synced + " &fteam ter-sync dari BetterTeams"));
        sender.sendMessage(MessageUtil.color("&8&m                              "));

        LogUtil.log("[Sync] Selesai: " + synced + " team ter-sync");
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
