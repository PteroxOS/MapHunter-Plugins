package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.leader.LeaderData;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.map.MapManager;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MapRemoveCommand extends SubCommand {

    private final LeaderManager leaderManager;
    private final MapManager mapManager;

    public MapRemoveCommand(LeaderManager leaderManager, MapManager mapManager) {
        this.leaderManager = leaderManager;
        this.mapManager = mapManager;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Mengambil Hunter Map dari leader";
    }

    @Override
    public String getSyntax() {
        return "/rmh map remove <player>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageUtil.color("&cSyntax: " + getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            MessageUtil.sendConfigMessage(sender, "messages.player-not-found", "&cPlayer tidak ditemukan.", plugin);
            return;
        }

        if (!leaderManager.isLeader(target)) {
            MessageUtil.sendConfigMessage(sender, "messages.not-a-leader", "&cPlayer tersebut bukan leader.", plugin);
            return;
        }

        mapManager.removeHunterMap(target);
        sender.sendMessage(MessageUtil.color("&aRemoved Hunter Map from " + target.getName() + "."));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 3) {
            List<String> names = new ArrayList<>();
            for (LeaderData data : leaderManager.getAllLeaders()) {
                Player p = Bukkit.getPlayer(data.getUuid());
                if (p != null) names.add(p.getName());
            }
            return names;
        }
        return new ArrayList<>();
    }
}
