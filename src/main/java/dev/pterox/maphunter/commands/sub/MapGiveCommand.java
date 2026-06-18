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

public class MapGiveCommand extends SubCommand {

    private final LeaderManager leaderManager;
    private final MapManager mapManager;

    public MapGiveCommand(LeaderManager leaderManager, MapManager mapManager) {
        this.leaderManager = leaderManager;
        this.mapManager = mapManager;
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getDescription() {
        return "Give Hunter Map to a specific leader";
    }

    @Override
    public String getSyntax() {
        return "/rmh map give <player>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MessageUtil.color("&cSyntax: " + getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtil.sendConfigMessage(sender, "messages.player-not-found", "&cPlayer tidak ditemukan.", plugin);
            return;
        }

        if (!leaderManager.isLeader(target)) {
            MessageUtil.sendConfigMessage(sender, "messages.not-a-leader", "&cPlayer tersebut bukan leader.", plugin);
            return;
        }

        mapManager.createHunterMap(target);
        sender.sendMessage(MessageUtil.color("&aGave Hunter Map to " + target.getName() + "."));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
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
