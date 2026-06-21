package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.util.ColorUtil;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LeaderListCommand extends SubCommand {

    private final LeaderManager leaderManager;

    public LeaderListCommand(LeaderManager leaderManager) {
        this.leaderManager = leaderManager;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Melihat daftar semua leader clan (detail lengkap)";
    }

    @Override
    public String getSyntax() {
        return "/rmh leader list";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        List<LeaderData> leaders = leaderManager.getAllLeaders();
        if (leaders.isEmpty()) {
            sender.sendMessage(MessageUtil.color("&cBelum ada leader yang terdaftar."));
            return;
        }

        sender.sendMessage(MessageUtil.color("&8&m                                                      "));
        sender.sendMessage(MessageUtil.color("&b&l MAP HUNTER &7- &eDaftar Leader (Admin)"));
        sender.sendMessage(MessageUtil.color("&8&m                                                      "));

        for (LeaderData data : leaders) {
            Player player = Bukkit.getPlayer(data.getUuid());
            boolean isOnline = player != null && player.isOnline();
            String statusIcon = isOnline ? "&a●" : "&c●";
            String statusText = isOnline ? "&aOnline" : "&cOffline";
            
            String coords = "";
            if (isOnline) {
                coords = String.format(" &7XYZ: &f%d, %d, %d", 
                    player.getLocation().getBlockX(), 
                    player.getLocation().getBlockY(), 
                    player.getLocation().getBlockZ());
            }
            
            String backupInfo = "";
            if (data.getBackupUuid() != null) {
                Player backupPlayer = Bukkit.getPlayer(data.getBackupUuid());
                if (backupPlayer != null && backupPlayer.isOnline()) {
                    backupInfo = " &7Backup: &e" + backupPlayer.getName() + " &a●";
                } else {
                    String backupName = Bukkit.getOfflinePlayer(data.getBackupUuid()).getName();
                    backupInfo = " &7Backup: &e" + (backupName != null ? backupName : "Unknown") + " &c●";
                }
            }
            
            org.bukkit.ChatColor clanColor = ColorUtil.getChatColor(data.getClanColor());
            
            // Tampilkan clan name dengan strikethrough jika digantikan backup
            String clanDisplay;
            if (data.isReplacedByBackup()) {
                clanDisplay = "&m" + clanColor + "[" + data.getClanName() + "]";
            } else {
                clanDisplay = clanColor + "[" + data.getClanName() + "]";
            }
            
            sender.sendMessage(MessageUtil.color(statusIcon + " " + clanDisplay));
            
            String replacedInfo = data.isReplacedByBackup() ? " &7| &c&mDIGANTIKAN BACKUP" : "";
            sender.sendMessage(MessageUtil.color("    &7Pemain: &f" + data.getPlayerName() + " &7| Status: " + statusText + replacedInfo));
            if (isOnline) {
                sender.sendMessage(MessageUtil.color("    &7Posisi:" + coords));
            }
            if (!backupInfo.isEmpty()) {
                sender.sendMessage(MessageUtil.color("   " + backupInfo));
            }
        }
        
        sender.sendMessage(MessageUtil.color("&8&m                                                      "));
        sender.sendMessage(MessageUtil.color("&7Total: &e" + leaders.size() + " &7leader terdaftar"));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
