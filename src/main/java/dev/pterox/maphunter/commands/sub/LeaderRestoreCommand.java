package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.map.MapManager;
import dev.pterox.maphunter.util.MessageUtil;
import dev.pterox.maphunter.util.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LeaderRestoreCommand extends SubCommand {

    private final LeaderManager leaderManager;
    private final MapManager mapManager;

    public LeaderRestoreCommand(LeaderManager leaderManager, MapManager mapManager) {
        this.leaderManager = leaderManager;
        this.mapManager = mapManager;
    }

    @Override
    public String getName() {
        return "restore";
    }

    @Override
    public String getDescription() {
        return "Mengembalikan map ke leader utama dari backup";
    }

    @Override
    public String getSyntax() {
        return "/rmh leader restore <clanName>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&cSyntax: " + getSyntax()));
            return;
        }

        String clanName = args[2];
        
        LeaderData leaderData = null;
        for (LeaderData data : leaderManager.getAllLeaders()) {
            if (data.getClanName().equalsIgnoreCase(clanName)) {
                leaderData = data;
                break;
            }
        }

        if (leaderData == null) {
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&cClan tidak ditemukan."));
            return;
        }

        if (!leaderData.isReplacedByBackup()) {
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&cClan " + clanName + " tidak sedang dalam status digantikan backup."));
            return;
        }

        Player leaderPlayer = Bukkit.getPlayer(leaderData.getUuid());
        if (leaderPlayer == null || !leaderPlayer.isOnline()) {
            sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&cLeader utama clan " + clanName + " sedang offline."));
            return;
        }

        mapManager.restoreLeader(leaderPlayer);
        LogUtil.logRestore(leaderPlayer.getName(), clanName);

        // Broadcast stylish
        Bukkit.broadcastMessage(MessageUtil.color(""));
        Bukkit.broadcastMessage(MessageUtil.color("&a&m                              "));
        Bukkit.broadcastMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&a&l✓ MAP DIKEMBALIKAN"));
        Bukkit.broadcastMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&fMap clan &e" + clanName + " &fdikembalikan ke &b" + leaderPlayer.getName()));
        Bukkit.broadcastMessage(MessageUtil.color("&a&m                              "));
        Bukkit.broadcastMessage(MessageUtil.color(""));

        // Notifikasi ke leader utama
        String title = MessageUtil.color("&a&lMAP DIKEMBALIKAN");
        String subtitle = MessageUtil.color("&eKamu mendapatkan kembali map clan &b" + clanName);
        leaderPlayer.sendTitle(title, subtitle, 10, 60, 20);

        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&aBerhasil mengembalikan map ke " + leaderPlayer.getName() + " untuk clan " + clanName + "."));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 3) {
            // Suggest clan names that are replaced by backup
            List<String> clans = new ArrayList<>();
            for (LeaderData data : leaderManager.getAllLeaders()) {
                if (data.isReplacedByBackup()) {
                    clans.add(data.getClanName());
                }
            }
            return clans;
        }
        return new ArrayList<>();
    }
}
