package dev.pterox.maphunter.commands.sub;

import dev.pterox.maphunter.commands.SubCommand;
import dev.pterox.maphunter.leader.LeaderManager;
import dev.pterox.maphunter.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeaderAddCommand extends SubCommand {

    private final LeaderManager leaderManager;

    public LeaderAddCommand(LeaderManager leaderManager) {
        this.leaderManager = leaderManager;
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Register a player as clan leader";
    }

    @Override
    public String getSyntax() {
        return "/rmh leader add <player> <clanName> <color>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(MessageUtil.color("&cSyntax: " + getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtil.sendConfigMessage(sender, "messages.player-not-found", "&cPlayer tidak ditemukan.", plugin);
            return;
        }

        if (leaderManager.isLeader(target)) {
            MessageUtil.sendConfigMessage(sender, "messages.already-leader", "&cPlayer tersebut sudah terdaftar sebagai leader.", plugin);
            return;
        }

        String clanName = args[2];
        String color = args[3];

        leaderManager.addLeader(target, clanName, color);
        sender.sendMessage(MessageUtil.color("&aAdded " + target.getName() + " as leader of " + clanName + " (" + color + ")."));
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            return names;
        } else if (args.length == 4) {
            return Arrays.asList("RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "ORANGE", "AQUA", "WHITE");
        }
        return new ArrayList<>();
    }
}
