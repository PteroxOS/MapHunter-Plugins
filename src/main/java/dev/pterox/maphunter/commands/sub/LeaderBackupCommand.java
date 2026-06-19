package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LeaderBackupCommand extends SubCommand {

    private final LeaderManager leaderManager;

    public LeaderBackupCommand(LeaderManager leaderManager) {
        this.leaderManager = leaderManager;
    }

    @Override
    public String getName() {
        return "backup";
    }

    @Override
    public String getDescription() {
        return "Set leader cadangan untuk sebuah clan";
    }

    @Override
    public String getSyntax() {
        return "/rmh leader backup <clanName> <player>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(MessageUtil.color("&cSyntax: " + getSyntax()));
            return;
        }

        String clanName = args[2];
        Player target = Bukkit.getPlayer(args[3]);

        if (target == null) {
            MessageUtil.sendConfigMessage(sender, "messages.player-not-found", "&cPlayer tidak ditemukan.", plugin);
            return;
        }

        LeaderData leaderData = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            LeaderData data = leaderManager.getLeaderData(p);
            if (data != null && data.getClanName().equalsIgnoreCase(clanName)) {
                leaderData = data;
                break;
            }
        }
        
        // Cek offline leader
        if (leaderData == null) {
            for (LeaderData data : leaderManager.getAllLeaders()) {
                if (data.getClanName().equalsIgnoreCase(clanName)) {
                    leaderData = data;
                    break;
                }
            }
        }

        if (leaderData == null) {
            sender.sendMessage(MessageUtil.color("&cClan tidak ditemukan atau belum memiliki leader utama."));
            return;
        }
        
        if (leaderData.getUuid().equals(target.getUniqueId())) {
            sender.sendMessage(MessageUtil.color("&cPlayer tersebut sudah menjadi leader utama clan ini."));
            return;
        }

        leaderData.setBackupUuid(target.getUniqueId());
        leaderManager.saveLeader(leaderData);
        
        sender.sendMessage(MessageUtil.color("&aBerhasil menambahkan " + target.getName() + " sebagai leader cadangan untuk clan " + clanName + "."));
    }

    @Override
    public java.util.List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 3) {
            // Suggest clan names
            java.util.List<String> clans = new java.util.ArrayList<>();
            for (LeaderData data : leaderManager.getAllLeaders()) {
                clans.add(data.getClanName());
            }
            return clans;
        } else if (args.length == 4) {
            java.util.List<String> names = new java.util.ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            return names;
        }
        return new java.util.ArrayList<>();
    }
}