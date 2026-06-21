package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class LeaderRemoveCommand extends SubCommand {

    private final LeaderManager leaderManager;

    public LeaderRemoveCommand(LeaderManager leaderManager) {
        this.leaderManager = leaderManager;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Menghapus player dari daftar leader";
    }

    @Override
    public String getSyntax() {
        return "/rmh leader remove <playerName>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageUtil.color("&cSyntax: " + getSyntax()));
            return;
        }

        String targetName = args[2];
        LeaderData targetData = null;
        for (LeaderData data : leaderManager.getAllLeaders()) {
            if (data.getPlayerName().equalsIgnoreCase(targetName)) {
                targetData = data;
                break;
            }
        }

        if (targetData == null) {
            MessageUtil.sendConfigMessage(sender, "messages.not-a-leader", "&cPlayer tersebut bukan leader.", plugin);
            return;
        }

        org.bukkit.entity.Player targetPlayer = org.bukkit.Bukkit.getPlayer(targetData.getUuid());
        if (targetPlayer != null && targetPlayer.isOnline()) {
            plugin.getMapManager().removeHunterMap(targetPlayer);
        }

        leaderManager.removeLeader(targetData.getUuid());
        sender.sendMessage(MessageUtil.color(""));
        sender.sendMessage(MessageUtil.color("&c&m                              "));
        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&c&l✗ LEADER DIHAPUS"));
        sender.sendMessage(MessageUtil.color("&8[&b&lMapHunter&8] &r&f" + targetName + " &fdihapus dari daftar leader."));
        sender.sendMessage(MessageUtil.color("&c&m                              "));
        sender.sendMessage(MessageUtil.color(""));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 3) {
            List<String> names = new ArrayList<>();
            for (LeaderData data : leaderManager.getAllLeaders()) {
                names.add(data.getPlayerName());
            }
            return names;
        }
        return new ArrayList<>();
    }
}
