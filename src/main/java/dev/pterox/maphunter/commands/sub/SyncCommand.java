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
import org.bukkit.entity.Player;

import java.util.*;

public class SyncCommand extends SubCommand {

    private final LeaderManager leaderManager;
    private final BetterTeamsIntegration integration;
    private final Set<UUID> pendingSync = new HashSet<>();

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
        return "/rmh sync [confirm]";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!integration.isEnabled()) {
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&cBetterTeams tidak ditemukan di server!"));
            return;
        }

        // Cek apakah sudah confirm
        boolean confirmed = args.length > 1 && args[1].equalsIgnoreCase("confirm");

        // Preview dulu (selalu tampilkan)
        Map<String, BetterTeamsIntegration.TeamData> teams = integration.readAllTeams();

        if (teams.isEmpty()) {
            sender.sendMessage(MessageUtil.color("&8&m                              "));
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&b&lSYNC DARI BETTERTEAMS"));
            sender.sendMessage(MessageUtil.color("&8&m                              "));
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&cTidak ada team ditemukan di BetterTeams!"));
            sender.sendMessage(MessageUtil.color("&8&m                              "));
            return;
        }

        // Tampilkan preview
        sender.sendMessage(MessageUtil.color(""));
        sender.sendMessage(MessageUtil.color("&8&m                              "));
        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&b&lSYNC DARI BETTERTEAMS"));
        sender.sendMessage(MessageUtil.color("&8&m                              "));
        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&fData yang akan di-sync:"));
        sender.sendMessage(MessageUtil.color(""));

        for (BetterTeamsIntegration.TeamData team : teams.values()) {
            String color = integration.convertColor(team.colorCode);
            org.bukkit.ChatColor clanColor = dev.pterox.maphunter.util.ColorUtil.getChatColor(color);

            String ownerName = Bukkit.getOfflinePlayer(team.ownerUuid).getName();
            if (ownerName == null) ownerName = "Unknown";

            String backupName = "Tidak ada";
            if (team.backupUuid != null) {
                OfflinePlayer backupOffline = Bukkit.getOfflinePlayer(team.backupUuid);
                backupName = backupOffline.getName() != null ? backupOffline.getName() : "Unknown";
            }

            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r" + clanColor + "Team: " + team.name));
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r  &fLeader: &e" + ownerName + " &7(OWNER)"));
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r  &fBackup: &e" + backupName + " &7(ADMIN)"));
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r  &fMembers: &e" + team.members.size() + " &7orang"));
            sender.sendMessage(MessageUtil.color(""));
        }

        int currentLeaders = leaderManager.getAllLeaders().size();

        if (!confirmed) {
            sender.sendMessage(MessageUtil.color("&8&m                              "));
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&c⚠ PERINGATAN"));
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&fData leader lama (&e" + currentLeaders + " &fleader) &cAkan dihapus!"));
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&fSemua data akan di-overwrite dari BetterTeams."));
            sender.sendMessage(MessageUtil.color(""));
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&fKetik &e/rmh sync confirm &funtuk melanjutkan."));
            sender.sendMessage(MessageUtil.color("&8&m                              "));
            
            // Simpan pending state
            if (sender instanceof Player) {
                pendingSync.add(((Player) sender).getUniqueId());
                // Auto-reset setelah 30 detik
                UUID uuid = ((Player) sender).getUniqueId();
                Bukkit.getScheduler().runTaskLater(plugin, () -> pendingSync.remove(uuid), 600L);
            }
            return;
        }

        // Cek confirm
        if (sender instanceof Player && !pendingSync.contains(((Player) sender).getUniqueId())) {
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&cKetik &e/rmh sync &fterlebih dahulu untuk melihat preview."));
            return;
        }

        // Lakukan sync
        pendingSync.remove(sender instanceof Player ? ((Player) sender).getUniqueId() : null);

        // Clear existing leaders
        for (LeaderData data : new ArrayList<>(leaderManager.getAllLeaders())) {
            leaderManager.removeLeader(data.getUuid());
        }

        int synced = 0;

        for (BetterTeamsIntegration.TeamData team : teams.values()) {
            String color = integration.convertColor(team.colorCode);

            OfflinePlayer ownerOffline = Bukkit.getOfflinePlayer(team.ownerUuid);
            String ownerName = ownerOffline.getName() != null ? ownerOffline.getName() : team.ownerName;

            LeaderData leaderData = new LeaderData(
                team.ownerUuid, ownerName, team.name, color, -1, System.currentTimeMillis()
            );

            if (team.backupUuid != null) {
                leaderData.setBackupUuid(team.backupUuid);
            }

            leaderManager.saveLeader(leaderData);
            synced++;

            String backupName = "Tidak ada";
            if (team.backupUuid != null) {
                OfflinePlayer backupOffline = Bukkit.getOfflinePlayer(team.backupUuid);
                backupName = backupOffline.getName() != null ? backupOffline.getName() : "Unknown";
            }

            org.bukkit.ChatColor clanColor = dev.pterox.maphunter.util.ColorUtil.getChatColor(color);
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r" + clanColor + "✓ " + team.name + " &7→ Leader: " + ownerName + " | Backup: " + backupName));

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
        if (args.length == 2) {
            List<String> options = new ArrayList<>();
            options.add("confirm");
            return filter(options, args[1]);
        }
        return new ArrayList<>();
    }

    private List<String> filter(List<String> list, String query) {
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(query.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}
